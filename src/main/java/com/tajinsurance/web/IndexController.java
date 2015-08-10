package com.tajinsurance.web;

import com.tajinsurance.domain.User;
import com.tajinsurance.domain.UserRole;
import com.tajinsurance.utils.UserLoginUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.LinkedList;

/**
 * Created by berz on 01.04.14.
 */
@Controller
@RequestMapping("/")
public class IndexController {
    @Autowired
    UserLoginUtil userLoginUtil;

    @RequestMapping(produces = "text/html", method = RequestMethod.GET)
    String index(Model uiModel){
        User u = userLoginUtil.getCurrentLogInUser();
        LinkedList<String> roles = new LinkedList<String>();
        for(UserRole r : u.getAuthorities()){
            if(r.getAuthority().equals("ROLE_USER_PARTNER")){
                return "redirect:/contracts?select_category";
            }
        }

        return "index";
    }
}
