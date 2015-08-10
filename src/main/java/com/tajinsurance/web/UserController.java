package com.tajinsurance.web;

import com.tajinsurance.domain.User;
import com.tajinsurance.domain.UserRole;
import com.tajinsurance.dto.AjaxUserListFilter;
import com.tajinsurance.exceptions.BadNewUserDataException;
import com.tajinsurance.exceptions.IllegalDataException;
import com.tajinsurance.service.PartnerService;
import com.tajinsurance.utils.LanguageUtil;
import com.tajinsurance.utils.UserLoginUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

/**
 * Created by berz on 09.04.14.
 */
@Controller
@RequestMapping(value = "users")
@RooWebScaffold(formBackingObject = User.class, path = "users")
public class UserController {

    @Autowired
    UserLoginUtil userLoginUtil;

    @Autowired
    PartnerService partnerService;

    @Autowired
    LanguageUtil languageUtil;


    @RequestMapping(params = "form")
    public String getForm(
            Model uiModel,
            @RequestParam(value = "reason", required = false)
            String reason
    ) {

        User user = userLoginUtil.getCurrentLogInUser();

        Locale locale = LocaleContextHolder.getLocale();


        User newUser = new User();

        if (user.getPartner() != null) {
            newUser.setPartner(user.getPartner());
        }

        if (reason != null) uiModel.addAttribute("reason", reason);

        uiModel.addAttribute("user", user);

        uiModel.addAttribute("partners", partnerService.getPartners());

        uiModel.addAttribute("allowedAuthorities", userLoginUtil.getAllowedRolesForUserToCreateLocalizated(user, locale.getLanguage()));

        return "users/new";
    }


    @RequestMapping(method = RequestMethod.POST)
    public String saveUser(
            User user,
            Model uiModel
    ) {
        try {
            userLoginUtil.validateNewUser(user);
        } catch (BadNewUserDataException e) {
            return "redirect:/users?form&reason=" + e.getMessage();
        } catch (IllegalDataException e) {
            return "redirect:/users?form&reason=" + e.getReason().getMsgCode();
        }

        userLoginUtil.createUser(user);
        return "redirect:/users?list";

    }


    @RequestMapping(params = "list", method = RequestMethod.POST)
    public String getUsersByFilter(
            Model uiModel,
            @RequestParam(value = "partner", required = false)
            Long partnerId,
            @RequestParam(value = "roles", required = false)
            List<String> roles,
            @RequestParam(value = "locked", required = false)
            Boolean locked,
            @RequestParam(value = "filter", required = false)
            String filter
    ){
        User loggedUser = userLoginUtil.getCurrentLogInUser();

        UserRole.AuthorityCode hAuthority = userLoginUtil.getMaxUserAuthorityCode(loggedUser);

        if (hAuthority == UserRole.AuthorityCode.ROLE_USER_PARTNER || hAuthority == UserRole.AuthorityCode.ROLE_ADMIN_PARTNER) {
            partnerId = loggedUser.getPartner().getId();
        }

        Locale locale = LocaleContextHolder.getLocale();


        if(filter != null){

            try {
                JSONObject json = (JSONObject)new JSONParser().parse(filter);

                AjaxUserListFilter ajaxFilter = new AjaxUserListFilter();

                try {
                    ajaxFilter.fio = (String) json.get("fio");
                    if(ajaxFilter.fio.equals("")) ajaxFilter.fio = null;
                }
                catch (RuntimeException e){
                    ajaxFilter.fio = null;
                }

                try{
                    ajaxFilter.login = (String) json.get("username");
                    if(ajaxFilter.login.equals("")) ajaxFilter.login = null;
                }
                catch (RuntimeException e){
                    ajaxFilter.login = null;
                }

                try{
                    ajaxFilter.partner = (String) json.get("partner");
                    if(ajaxFilter.partner.equals("")) ajaxFilter.partner = null;
                }
                catch (RuntimeException e){
                    ajaxFilter.partner = null;
                }

                try{
                    ArrayList<String> rls = (ArrayList<String>) json.get("authorities");
                    if(rls.size() > 0){
                        ajaxFilter.userRoles = new ArrayList<Long>();
                        for(String r : rls) ajaxFilter.userRoles.add(Long.decode(r));
                    }
                }
                catch(RuntimeException e){
                    ajaxFilter.userRoles = null;
                }

                try{
                    String email = (String) json.get("email");
                    if(email.equals("")) ajaxFilter.email = null;
                    else ajaxFilter.email = email;
                }
                catch(RuntimeException e){
                    ajaxFilter.email = null;
                }


                try{
                    String enabled = (String) json.get("enabled");
                    if(enabled.equals("yes")){
                        ajaxFilter.isEnabled = true;
                    }
                    else if(enabled.equals("no")){
                        ajaxFilter.isEnabled = false;
                    }
                    else ajaxFilter.isEnabled = null;
                }
                catch (RuntimeException e){
                    ajaxFilter.isEnabled = null;
                }

                try{
                    ajaxFilter.orderColumn = (String) json.get("orderColumn");

                    if(ajaxFilter.orderColumn.equals("")) ajaxFilter.orderColumn = null;
                }
                catch (RuntimeException e){
                    ajaxFilter.orderColumn = null;
                }

                try{
                    ajaxFilter.orderType = (String) json.get("orderType");

                    if(ajaxFilter.orderType.equals("")) ajaxFilter.orderType = null;
                }
                catch (RuntimeException e){
                    ajaxFilter.orderType = null;
                }




                uiModel.addAttribute("users", userLoginUtil.getUsers(partnerId, roles, locked, ajaxFilter, locale.getLanguage()));

                return "users/ajax/list";

            } catch (ParseException e) {
                return "redirect:/users?list";
            }

        }
        else{
            uiModel.addAttribute("users", userLoginUtil.getUsers(partnerId, roles, locked, locale.getLanguage()));
        }

        return "users/list";
    }

    @RequestMapping(params = "list", method = RequestMethod.GET)
    public String getUsers(
            Model uiModel,
            @RequestParam(value = "partner", required = false)
            Long partnerId,
            @RequestParam(value = "roles", required = false)
            List<String> roles,
            @RequestParam(value = "locked", required = false)
            Boolean locked
    ) {

        User loggedUser = userLoginUtil.getCurrentLogInUser();

        UserRole.AuthorityCode hAuthority = userLoginUtil.getMaxUserAuthorityCode(loggedUser);

        if (hAuthority == UserRole.AuthorityCode.ROLE_USER_PARTNER || hAuthority == UserRole.AuthorityCode.ROLE_ADMIN_PARTNER) {
            partnerId = loggedUser.getPartner().getId();
        }

        Locale locale = LocaleContextHolder.getLocale();




        LinkedHashMap<String, List> listVal = new LinkedHashMap<String, List>();

        List<UserRole> userRoles = userLoginUtil.getAllowedRolesForUserToCreate(loggedUser);

        for(UserRole r : userRoles){
            r = (UserRole) languageUtil.getLocalizatedObject(r, locale.getLanguage());
        }

        listVal.put("authorities", userRoles);

        uiModel.addAttribute("listVal", listVal);

        uiModel.addAttribute("users", userLoginUtil.getUsers(partnerId, roles, locked, locale.getLanguage()));

        return "users/list";
    }

    @RequestMapping(params = "form", value = "/{id}")
    String getUserForm(
            Model uiModel,
            @PathVariable(value = "id")
            Long id,
            @RequestParam(value = "reason", required = false)
            String reason
    ) {
        User user = userLoginUtil.getUserById(id);

        Locale locale = LocaleContextHolder.getLocale();
        if (user == null) return "redirect:/users?list";

        if (reason != null) uiModel.addAttribute("reason", reason);

        uiModel.addAttribute("user", user);

        if(userLoginUtil.getMaxUserAuthorityCode(user).equals(UserRole.AuthorityCode.ROLE_ADMIN_PARTNER) || userLoginUtil.getMaxUserAuthorityCode(user).equals(UserRole.AuthorityCode.ROLE_USER_PARTNER) )
            uiModel.addAttribute("allowedAuthorities", userLoginUtil.getclientManagerRolesLocalizated(locale.getLanguage()));

        uiModel.addAttribute("code", userLoginUtil.getMaxUserAuthorityCode(user));

        return "users/edit";
    }

    @RequestMapping(value = "/{id}", params = "restore")
    public String restore(
        @PathVariable(value = "id")
        Long id
    )
    {
        User user = userLoginUtil.getUserById(id);

        User loggedUser = userLoginUtil.getCurrentLogInUser();

        if (
                (!userLoginUtil.isUserHasAuthority(loggedUser, UserRole.AuthorityCode.ROLE_ADMIN_PARTNER))
                        && (!userLoginUtil.isUserHasAuthority(loggedUser, UserRole.AuthorityCode.ROLE_USER))
                        && (!userLoginUtil.isUserHasAuthority(loggedUser, UserRole.AuthorityCode.ROLE_ADMIN))
                ) return "redirect:/users?list";

        userLoginUtil.restoreUser(user);

        return "redirect:/users?list";
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public String disable(
            @PathVariable(value = "id")
            Long id
    ) {
        User user = userLoginUtil.getUserById(id);

        User loggedUser = userLoginUtil.getCurrentLogInUser();

        if (
                (!userLoginUtil.isUserHasAuthority(loggedUser, UserRole.AuthorityCode.ROLE_ADMIN_PARTNER))
                        && (!userLoginUtil.isUserHasAuthority(loggedUser, UserRole.AuthorityCode.ROLE_USER))
                        && (!userLoginUtil.isUserHasAuthority(loggedUser, UserRole.AuthorityCode.ROLE_ADMIN))
                ) return "redirect:/users?list";

        userLoginUtil.disableUser(user);


        return "redirect:/users?list";
    }

    @RequestMapping(method = RequestMethod.PUT)
    String editUser(
            User user,
            @RequestParam(value = "auth", required = false)
            UserRole userRole
            ) {
        if (user.getId() == null) return "redirect:/users?list";

        User actualUser = userLoginUtil.getUserById(user.getId());


        if(!user.getPassword().equals("")){
            try {
                user.setPassword(userLoginUtil.changePass(user.getPassword()));
            } catch (BadNewUserDataException e) {
                return "redirect:/users/" + user.getId() + "/?form&reason=" + e.getMessage();
            }
        }
        else{
            user.setPassword(actualUser.getPassword());
        }

        List<UserRole> userRoles = null;
        if(userRole != null){
            userRoles = new LinkedList<UserRole>();
            userRoles.add(userRole);
        }

        user.setAuthorities((userRoles == null)? actualUser.getAuthorities() : userRoles);

        try {
            userLoginUtil.updateUser(user);
        } catch (IllegalDataException e) {
            //System.out.println(e.getReason().getMsgCode() + " :: " + e.getMessage());
            return "redirect:/users/" + user.getId() + "/?form&reason="+e.getReason().getMsgCode();
        }

        return "redirect:/users/" + user.getId() + "/?form&reason=ok";
    }

    @RequestMapping(value = "/{id}")
    String showUser(
            @PathVariable(value = "id") Long id,
            Model uiModel
    ){
        User user = userLoginUtil.getUserById(id);
        if(user == null) return "redirect:/users?list";

        Locale locale = LocaleContextHolder.getLocale();

        user = (User) languageUtil.getLocalizatedObject(user, locale.getLanguage());

        uiModel.addAttribute("user", user);

        return "users/ajax/show";
    }
}
