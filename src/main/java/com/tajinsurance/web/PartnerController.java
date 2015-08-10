package com.tajinsurance.web;

import com.tajinsurance.domain.*;
import com.tajinsurance.dto.AjaxPartnerListFilter;
import com.tajinsurance.exceptions.*;
import com.tajinsurance.service.CatContractService;
import com.tajinsurance.service.PartnerService;
import com.tajinsurance.service.RiskService;
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

import java.math.BigDecimal;
import java.util.*;
import java.util.Locale;

/**
 * Created by berz on 21.04.14.
 */
@Controller
@RequestMapping(value = "partners")
@RooWebScaffold(formBackingObject = Partner.class, path = "partners")
public class PartnerController {

    @Autowired
    PartnerService partnerService;

    @Autowired
    CatContractService catContractService;

    @Autowired
    LanguageUtil languageUtil;

    @Autowired
    RiskService riskService;

    @Autowired
    UserLoginUtil userLoginUtil;

    @RequestMapping(method = RequestMethod.POST)
    public String createPartner(Model uiModel, Partner partner) {

        try {
            partnerService.validateNewPartner(partner);
        } catch (BadNewPartnerDataException e) {
            return "redirect:/partners?form&reason=" + e.getMessage();
        }

        partnerService.savePartner(partner);


        return "redirect:/partners?list";
    }

    @RequestMapping(params = "form")
    public String getNewPartnerForm(
            Model uiModel,
            @RequestParam(value = "reason", required = false)
            String reason
    ) {

        Locale locale = LocaleContextHolder.getLocale();

        List<CatContract> allCatContracts = catContractService.getAllCatContracts(locale.getLanguage());

        uiModel.addAttribute("allcatcontracts", allCatContracts);

        uiModel.addAttribute("paymentAcceptOptions", Partner.PaymentAccept.values());

        if (reason != null) uiModel.addAttribute("reason", reason);

        return "partners/new";
    }

    @RequestMapping(params = "list")
    public String getPartnersList(Model uiModel) {

        Locale locale = LocaleContextHolder.getLocale();

        List<Partner> partners = partnerService.getPartnersLocalizated(locale.getLanguage());

        uiModel.addAttribute("partners", partners);

        LinkedHashMap<String, List> listVal = new LinkedHashMap<String, List>();
        listVal.put("catContracts", catContractService.getAllCatContracts(locale.getLanguage()));

        uiModel.addAttribute("listVal", listVal);

        return "partners/list";
    }

    @RequestMapping(params = "list", method = RequestMethod.POST)
    String getPartnersByFilter(
            @RequestParam(value = "filter", required = true)
            String filter,
            Model uiModel
    ) {
        Locale locale = LocaleContextHolder.getLocale();


        try {
            JSONObject json = (JSONObject) new JSONParser().parse(filter);
            AjaxPartnerListFilter ajaxFilter = new AjaxPartnerListFilter();

            try {
                ajaxFilter.partner = (String) json.get("partner");

                if (ajaxFilter.partner.equals("")) ajaxFilter.partner = null;
            } catch (RuntimeException e) {
                ajaxFilter.partner = null;
            }

            try {
                ArrayList<String> cc = (ArrayList<String>) json.get("catContracts");

                if (cc.size() > 0) {
                    ajaxFilter.catContracts = new ArrayList<Long>();

                    for (String s : cc) {
                        ajaxFilter.catContracts.add(Long.decode(s));
                    }
                }
            } catch (RuntimeException e) {
                ajaxFilter.catContracts = null;
            }


            try {
                ajaxFilter.orderColumn = (String) json.get("orderColumn");

                if (ajaxFilter.orderColumn.equals("")) ajaxFilter.orderColumn = null;
            } catch (RuntimeException e) {
                ajaxFilter.orderColumn = null;
            }

            try {
                ajaxFilter.orderType = (String) json.get("orderType");

                if (ajaxFilter.orderType.equals("")) ajaxFilter.orderType = null;
            } catch (RuntimeException e) {
                ajaxFilter.orderType = null;
            }

            try {
                ajaxFilter.orderColumn = (String) json.get("orderColumn");

                if (ajaxFilter.orderColumn.equals("")) ajaxFilter.orderColumn = null;
            } catch (RuntimeException e) {
                ajaxFilter.orderColumn = null;
            }

            try {
                ajaxFilter.orderType = (String) json.get("orderType");

                if (ajaxFilter.orderType.equals("")) ajaxFilter.orderType = null;
            } catch (RuntimeException e) {
                ajaxFilter.orderType = null;
            }


            List<Partner> partners = partnerService.getPartnersLocalizatedByFilter(locale.getLanguage(), ajaxFilter);

            uiModel.addAttribute("partners", partners);

            return "partners/ajax/list";
        } catch (ParseException e) {
            return getPartnersList(uiModel);
        }
    }


    @RequestMapping(value = "/{id}", params = "params_cat_contract")
    public String editPartnerCatContractParams(
            Model uiModel,
            @PathVariable(value = "id")
            Long partnerId,
            @RequestParam(value = "cc_id", required = true)
            Long catContractId,
            @RequestParam(value = "reason", required = false)
            String reason,
            @RequestParam(value = "property_not_saved", required = false)
            String property_reason
    ) {
        Locale locale = LocaleContextHolder.getLocale();

        User user = userLoginUtil.getCurrentLogInUser();

        if (reason != null) {
            uiModel.addAttribute("reason", reason);
        }

        if (property_reason != null) {
            uiModel.addAttribute("property_reason", property_reason);
        }

        Partner partner = (Partner) languageUtil.getLocalizatedObject(partnerService.getPartnerById(partnerId), locale.getLanguage());

        if (partner == null) return null;

        CatContract catContract = catContractService.getCatContractById(catContractId);

        if (catContract == null) return null;

        uiModel.addAttribute("catContract", catContract);
        uiModel.addAttribute("partner", partner);


        if (catContract.getSettingsWay().equals(CatContract.SettingsWay.GLOBAL)) {
            // Корреляции для рисков / категорий залогового имущества
            List<PartnerRiskCorrelation> risksCorrelationsExists = riskService.getPartnerRiskCorrelations(partner, catContract);
            List<Risk> potentialRisksForCorrelations = riskService.getPotentialPartnerRiskCorrelations(partner, catContract);

            uiModel.addAttribute("risksCorrelationsExists", risksCorrelationsExists);
            uiModel.addAttribute("potentialRisksForCorrelations", potentialRisksForCorrelations);

            if(catContract.getProduct().equals(CatContract.Product.TP0)){
                //List
                List<Risk> risksWherePolicyNotSet = partnerService.getRisksNotSetPolicy(catContract, partner);
                List<PartnerRiskPolicy> partnerRiskPolicyList = partnerService.getExistsPolicy(catContract, partner);

                uiModel.addAttribute("policyValues", PartnerRiskPolicy.Policy.values());
                uiModel.addAttribute("notSetRiskPolicy", risksWherePolicyNotSet);
                uiModel.addAttribute("existsRiskPolicy", partnerRiskPolicyList);
            }


        }
        else{
            // Уже рассчитанные риски
            List<CatContractRisk> calculatedRisk = riskService.getExistRisks(partner, catContract);
            // Риски, которые еще можно рассчитать
            List<Risk> potentialRisks = riskService.getPotentialRiskTermRisks(partner, catContract);

            uiModel.addAttribute("calculatedRisks", calculatedRisk);
            uiModel.addAttribute("potentialRisks", potentialRisks);
        }

        // Денежные параметры, которые уже заданы для партнера (по сериям) и те, которые могут быть заданы
        List<PartnerProductMoneyProperty> existsProperties = partnerService.getPartnerProductMoneyProperties(partner, catContract);
        List<ProductMoneyProperty> potentialProperties = partnerService.getPartnerProductMoneyPropertiesNotSet(partner, catContract);

        uiModel.addAttribute("existsProp", existsProperties);
        uiModel.addAttribute("potentialProp", potentialProperties);


        List<UserRole> allowedAuth = userLoginUtil.getAllowedRolesForUserToCreateLocalizated(user, locale.getLanguage());

        uiModel.addAttribute("allowedAuthorities", allowedAuth);

        List<User> users = userLoginUtil.getUsers(partnerId, null, null, locale.getLanguage());

        uiModel.addAttribute("users", users);

        uiModel.addAttribute("paymentAcceptOptions", Partner.PaymentAccept.values());


        if (catContract.getProduct().equals(CatContract.Product.MP0)) {
            PartnerProductMoneyProperty franchiseProperty = (PartnerProductMoneyProperty) partnerService.getPartnerProductMoneyPropertyByPropertyName(partner, ProductMoneyProperty.PropertyType.MP0_franchise_k, catContract);

            if (franchiseProperty != null && franchiseProperty.getUseProperty() != null && franchiseProperty.getUseProperty()) {
                uiModel.addAttribute("useFranchise", true);
            }
        }

        if(catContract.getProduct().equals(CatContract.Product.BA0)){
            List<ProductRiskSet> productRiskSets = riskService.getProductRiskSets(partner, catContract);

            uiModel.addAttribute("productRiskSets", productRiskSets);
        }

        return "partner/cc/properties";
    }

    @RequestMapping(value = "/{id}", params = "delete_correlation", method = RequestMethod.DELETE)
    public String deleteCorrelation(
            @PathVariable(value = "id")
            Long partnerId,
            @RequestParam(value = "id")
            Long corrId,
            @RequestParam(value = "cc_id")
            Long catContractId
    ){
        partnerService.deletePartnerRiskCorrelation(corrId);

        return "redirect:/partners/" + partnerId + "?params_cat_contract&cc_id=" + catContractId;
    }

    @RequestMapping(value = "/{id}", params = "add_correlation", method = RequestMethod.POST)
    public String addCorrelation(
            @PathVariable(value = "id")
            Long partnerId,
            @RequestParam(value = "catContract")
            Long catContractId,
            @RequestParam(value = "risk")
            Long riskId,
            @RequestParam(value = "correlation")
            BigDecimal correlation,
            @RequestParam(value = "extraInfo")
            String extraInfo
    ){
        partnerService.addCorrelationForPartnerCatContractRisk(partnerId, catContractId, riskId, correlation, extraInfo);

        return "redirect:/partners/" + partnerId + "?params_cat_contract&cc_id=" + catContractId;
    }

    @RequestMapping(value = "/{id}", params = "form")
    public String editPartnerForm(
            Model uiModel,
            @PathVariable(value = "id")
            Long partnerId,
            @RequestParam(value = "reason", required = false)
            String reason,
            @RequestParam(value = "user_reason", required = false)
            String user_reason,
            @RequestParam(value = "property_not_saved", required = false)
            String property_reason
    ) {
        Locale locale = LocaleContextHolder.getLocale();

        User user = userLoginUtil.getCurrentLogInUser();

        if (reason != null) {
            uiModel.addAttribute("reason", reason);
        }

        if (user_reason != null) {
            uiModel.addAttribute("user_reason", user_reason);
        }

        if (property_reason != null) {
            uiModel.addAttribute("property_reason", property_reason);
        }

        Partner partner = (Partner) languageUtil.getLocalizatedObject(partnerService.getPartnerById(partnerId), locale.getLanguage());

        if (partner == null) return "redirect:/partners?list";

        uiModel.addAttribute("partner", partner);

        List<CatContract> allCatContracts = catContractService.getAllCatContracts(locale.getLanguage());

        uiModel.addAttribute("allcatcontracts", allCatContracts);

        // Уже рассчитанные риски
        HashMap<String, List<CatContractRisk>> calculatedRisk = new HashMap<String, List<CatContractRisk>>();
        // Риски, которые еще можно рассчитать
        HashMap<String, List<Risk>> potentialRisks = new HashMap<String, List<Risk>>();

        //System.out.println(partner.getCatContracts());

        for (CatContract cc : partner.getCatContracts()) {
            calculatedRisk.put(cc.getName(), riskService.getExistRisks(partner, cc));
            potentialRisks.put(cc.getName(), riskService.getPotentialRiskTermRisks(partner, cc));
        }

        uiModel.addAttribute("calculatedRisks", calculatedRisk);
        uiModel.addAttribute("potentialRisks", potentialRisks);


        // Денежные параметры, которые уже заданы для партнера (по сериям) и те, которые могут быть заданы
        HashMap<String, List<PartnerProductMoneyProperty>> existsProperties = new HashMap<String, List<PartnerProductMoneyProperty>>();
        HashMap<String, List<ProductMoneyProperty>> potentialProperties = new HashMap<String, List<ProductMoneyProperty>>();

        for (CatContract cc : partner.getCatContracts()) {
            existsProperties.put(cc.getName(), partnerService.getPartnerProductMoneyProperties(partner, cc));
            potentialProperties.put(cc.getName(), partnerService.getPartnerProductMoneyPropertiesNotSet(partner, cc));
        }

        uiModel.addAttribute("existsProp", existsProperties);
        uiModel.addAttribute("potentialProp", potentialProperties);


        List<UserRole> allowedAuth = userLoginUtil.getAllowedRolesForUserToCreateLocalizated(user, locale.getLanguage());

        uiModel.addAttribute("allowedAuthorities", allowedAuth);

        List<User> users = userLoginUtil.getUsers(partnerId, null, null, locale.getLanguage());

        uiModel.addAttribute("users", users);

        uiModel.addAttribute("paymentAcceptOptions", Partner.PaymentAccept.values());

        return "partners/edit";
    }


    @RequestMapping(method = RequestMethod.PUT)
    public String updatePartner(
            Model uiModel,
            Partner partner
    ) {
        try {
            partnerService.update(partner);
        } catch (BadNewPartnerDataException e) {
            return "redirect:/partners/" + partner.getId() + "/?form&reason=" + e.getMessage();
        }


        return "redirect:/partners/" + partner.getId() + "/?form";
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public String deletePartner(
            Model uiModel,
            @PathVariable(value = "id")
            Long partnerId
    ) {

        partnerService.remove(partnerService.getPartnerById(partnerId));


        return "redirect:/partners?list";
    }

    @RequestMapping(method = RequestMethod.POST, params = "new_user", value = "/{id}")
    public String addUser(
            @PathVariable(value = "id")
            Long partnerId,
            @RequestParam(value = "username")
            String username,
            @RequestParam(value = "password")
            String password,
            @RequestParam(value = "memo")
            String memo,
            @RequestParam(value = "fio")
            String fio,
            @RequestParam(value = "authorities")
            List<UserRole> authorities,
            @RequestParam(value = "email")
            String email
    ) {

        User newUser = new User();

        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setFio(fio);
        newUser.setMemo(memo);
        newUser.setEmail(email);

        newUser.setPartner(partnerService.getPartnerById(partnerId));
        newUser.setAuthorities(authorities);

        try {
            userLoginUtil.validateNewUser(newUser);
        } catch (BadNewUserDataException e) {
            return "redirect:/partners/" + partnerId + "?form&user_reason=" + e.getMessage();
        } catch (IllegalDataException e) {
            return "redirect:/partners/" + partnerId + "?form&user_reason=" + e.getReason().getMsgCode();
        }

        userLoginUtil.createUser(newUser);

        return "redirect:/partners/" + partnerId + "?form";
    }

    @RequestMapping(method = RequestMethod.POST, params = "new_property", value = "/{id}")
    public String addProperty(
            @RequestParam(value = "property_id")
            Long propertyId,
            @RequestParam(value = "value")
            BigDecimal value,
            @RequestParam(value = "use_property", required = false)
            Boolean useProperty,
            @RequestParam(value = "extra_info", required = false)
            String extraInfo,
            @PathVariable(value = "id")
            Long partnerId,
            Model uiModel
    ) {

        ProductMoneyProperty productMoneyProperty = partnerService.getProductMoneyPropertyById(propertyId);

        try {
            PartnerProductMoneyProperty partnerProductMoneyProperty = new PartnerProductMoneyProperty();
            partnerProductMoneyProperty.setPartner(partnerService.getPartnerById(partnerId));
            partnerProductMoneyProperty.setProductMoneyProperty(productMoneyProperty);
            partnerProductMoneyProperty.setMoneyValue(value);
            partnerProductMoneyProperty.setUseProperty(useProperty);
            partnerProductMoneyProperty.setExtraInfo(extraInfo);

            partnerService.savePartnerProductMoneyProperty(partnerProductMoneyProperty);

            return "redirect:/partners/" + partnerId + "?params_cat_contract&cc_id=" + productMoneyProperty.getCatContract().getId();
        } catch (EntityNotSavedException e) {
            uiModel.addAttribute("property_not_saved", e.getMessage());
            return "redirect:/partners/" + partnerId + "?params_cat_contract&cc_id=" + productMoneyProperty.getCatContract().getId();
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, params = "product_property", value = "/{id}")
    public String deleteProperty(
            @RequestParam(value = "id")
            Long productPropertyId,
            @PathVariable(value = "id")
            Long partnerId
    ) {
        PartnerProductMoneyProperty partnerProductMoneyProperty = partnerService.getPartnerProductMoneyPropertyById(productPropertyId);

        ProductMoneyProperty productMoneyProperty = partnerProductMoneyProperty.getProductMoneyProperty();
        partnerService.removeProperty(partnerProductMoneyProperty);

        return "redirect:/partners/" + partnerId + "?params_cat_contract&cc_id=" + productMoneyProperty.getCatContract().getId();
    }

    @RequestMapping(method = RequestMethod.POST, params = "new_risk", value = "/{id}")
    public String addRisk(
            @RequestParam(value = "risk")
            Long riskId,
            @RequestParam(value = "catContract")
            Long catContractId,
            @RequestParam(value = "rate")
            BigDecimal rate,
            @RequestParam(value = "monthTarif")
            BigDecimal monthTarif,
            @RequestParam(value = "minSum")
            BigDecimal minSum,
            @RequestParam(value = "maxSum")
            BigDecimal maxSum,
            @PathVariable(value = "id")
            Long partnerId
    ) {
        try {
            Risk risk = riskService.getRiskById(riskId);
            Partner partner = partnerService.getPartnerById(partnerId);
            CatContract catContract = catContractService.getCatContractById(catContractId);

            CatContractRisk catContractRisk = new CatContractRisk();
            catContractRisk.setCatContract(catContract);
            catContractRisk.setPartner(partner);
            catContractRisk.setRisk(risk);
            catContractRisk.setMonthTarif(monthTarif);
            catContractRisk.setRate(rate);
            catContractRisk.setMinSum(minSum);
            catContractRisk.setMaxSum(maxSum);

            riskService.newCatContractRisk(catContractRisk);


        } catch (NoEntityException e) {
            //
        } finally {
            return "redirect:/partners/" + partnerId + "?params_cat_contract&cc_id=" + catContractId;
        }
    }


    @RequestMapping(params = "term", method = RequestMethod.DELETE, value = "/{id}")
    public String deleteRisk(
            @RequestParam(value = "id")
            Long ccrId,
            @PathVariable(value = "id")
            Long partnerId

    ) {

        CatContractRisk catContractRisk = riskService.getCatContractRiskById(ccrId);
        riskService.removeCatContractRisk(catContractRisk);

        return "redirect:/partners/" + partnerId + "?form";
    }

    @RequestMapping(params = "delete_franchise", method = RequestMethod.DELETE)
    public String deleteFranchise(
            @RequestParam(value = "franchise_id")
            Long franchiseId,
            Model uiModel
    ) {
        CatContractPartnerFranchise catContractPartnerFranchise = partnerService.getCatContractPartnerFranchiseById(franchiseId);

        if (catContractPartnerFranchise != null) {
            partnerService.deleteCatContractPartnerFranchise(catContractPartnerFranchise);

            uiModel.addAttribute("cc_id", catContractPartnerFranchise.getCatContract().getId());

            return "redirect:/partners/" + catContractPartnerFranchise.getPartner().getId().toString() + "?franchises";
        } else return null;
    }

    @RequestMapping(params = "franchises", value = "/{id}", method = RequestMethod.GET)
    public String getPartnerFranchises(
            @PathVariable(value = "id")
            Long partnerId,
            @RequestParam(value = "cc_id")
            Long catContractId,
            @RequestParam(value = "reason", required = false)
            String reason,
            Model uiModel
    ) {
        if (reason != null) uiModel.addAttribute("reason", reason);

        Partner partner = partnerService.getPartnerById(partnerId);

        CatContract catContract = catContractService.getCatContractById(catContractId);

        if (partner == null || catContract == null) return null;

        List<CatContractPartnerFranchise> catContractPartnerFranchises = partnerService.getCatContractPartnerFranchises(partner, catContract);

        uiModel.addAttribute("ccp_fr", catContractPartnerFranchises);
        uiModel.addAttribute("partner", partner);
        uiModel.addAttribute("catContract", catContract);

        return "partner/cc/franchises";
    }

    @RequestMapping(params = "new_franchise", value = "/{id}", method = RequestMethod.POST)
    public String addFranchise(
            @PathVariable(value = "id")
            Long partnerId,
            @RequestParam(value = "cc_id")
            Long catContractId,
            @RequestParam(value = "franchisePercent")
            Integer franchisePercent,
            @RequestParam(value = "discount")
            Integer discountPercent,
            Model uiModel
    ) {
        BigDecimal discount = BigDecimal.valueOf(discountPercent).divide(BigDecimal.valueOf(100));


        CatContractPartnerFranchise catContractPartnerFranchise = new CatContractPartnerFranchise();
        catContractPartnerFranchise.setCatContract(catContractService.getCatContractById(catContractId));
        catContractPartnerFranchise.setPartner(partnerService.getPartnerById(partnerId));
        catContractPartnerFranchise.setFranchisePercent(franchisePercent);
        catContractPartnerFranchise.setDiscount(discount);

        try {
            partnerService.addPartnerFranchise(catContractPartnerFranchise);
        } catch (IllegalDataException e) {
            uiModel.addAttribute("reason", e.getReason().getMsgCode());
        }

        uiModel.addAttribute("cc_id", catContractId);

        return "redirect:/partners/" + partnerId + "?franchises";
    }

    @RequestMapping(value = "/{id}", params = "new_risks_set", method = RequestMethod.POST)
    public String addRisksSet(
            @PathVariable(value = "id")
            Long partnerId,
            @RequestParam(value = "cc_id")
            Long catContractId,
            @RequestParam(value = "name")
            String name,
            @RequestParam(value = "risks[]")
            List<Risk> risks
    ){


        CatContract catContract = catContractService.getCatContractById(catContractId);
        Partner partner = partnerService.getPartnerById(partnerId);

        riskService.addNewRisksSet(partner, catContract, name, risks);

        return "redirect:/partners/" + partnerId + "?params_cat_contract&cc_id=" + catContractId;
    }

    @RequestMapping(value = "/{id}", params = "delete_risks_set", method = RequestMethod.DELETE)
    public String deleteRisksSet(
            @PathVariable(value = "id")
            Long partnerId,
            @RequestParam(value = "id")
            Long setId,
            @RequestParam(value = "cc_id")
            Long catContractId
    ){
        riskService.deleteRisksSet(setId);

        return "redirect:/partners/" + partnerId + "?params_cat_contract&cc_id=" + catContractId;
    }

    @RequestMapping(value = "/{id}", params = "add_policy", method = RequestMethod.POST)
    public String addPolicy(
            @PathVariable(value = "id")
            Long partnerId,/*,
            @RequestParam(required = false)
            PartnerRiskPolicy partnerRiskPolicy*/
            @RequestParam(value = "catContract", required = true)
            Long catContractId,
            @RequestParam(value = "risk", required = true)
            Long riskId,
            @RequestParam(value = "policy", required = true)
            PartnerRiskPolicy.Policy policy,
            @RequestParam(value = "useFixedSums", required = false)
            Boolean useFixedSums
    ){
        User currentUser = userLoginUtil.getCurrentLogInUser();


        PartnerRiskPolicy partnerRiskPolicy = new PartnerRiskPolicy();
        try {
            partnerRiskPolicy.setRisk(riskService.getRiskById(riskId));
        } catch (NoEntityException e) {
            return null;
        }
        partnerRiskPolicy.setPartner(partnerService.getPartnerById(partnerId));
        partnerRiskPolicy.setCatContract(catContractService.getCatContractById(catContractId));
        partnerRiskPolicy.setPolicy(policy);
        partnerRiskPolicy.setUseFixedSums(useFixedSums);

        partnerService.addPartnerRiskPolicy(partnerRiskPolicy);

        return "redirect:/partners/" + partnerId + "?params_cat_contract&cc_id=" + partnerRiskPolicy.getCatContract().getId();
    }

    @RequestMapping(value = "/{id}", params = "delete_policy", method = RequestMethod.DELETE)
    public String delPolicy(
            @PathVariable(value = "id")
            Long partnerId,
            @RequestParam(value = "id", required = true)
            Long policyId,
            @RequestParam(value = "cc_id", required = true)
            Long catContractId
    ){


        partnerService.delPartnerRiskPolicy(policyId);

        return "redirect:/partners/" + partnerId + "?params_cat_contract&cc_id=" + catContractId;
    }

}
