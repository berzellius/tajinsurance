package com.tajinsurance.web;

import com.tajinsurance.domain.*;
import com.tajinsurance.exceptions.EntityNotSavedException;
import com.tajinsurance.exceptions.IllegalDataException;
import com.tajinsurance.exceptions.NoEntityException;
import com.tajinsurance.service.*;
import com.tajinsurance.utils.LanguageUtil;
import com.tajinsurance.utils.UserLoginUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
/**
 * Created by berz on 01.11.14.
 */
@RequestMapping("/productsettings")
@Controller
public class ProductSettingsController {


    @Autowired
    CatContractService catContractService;

    @Autowired
    CatContractFixedSumService catContractFixedSumService;

    @Autowired
    UserLoginUtil userLoginUtil;

    @Autowired
    RiskService riskService;

    @Autowired
    PartnerService partnerService;

    @Autowired
    LanguageUtil languageUtil;

    @Autowired
    ProductProcessorTP2 productProcessorTP2;

    @RequestMapping(method = RequestMethod.GET)
    public String chooseProduct(
            Model uiModel
    ){
        Locale locale = LocaleContextHolder.getLocale();
        List<CatContract> catContractList = catContractService.getCatContractsGlobalSettings(locale.getLanguage());
        uiModel.addAttribute("contractCategories", catContractList);

        return "product_settings/choose";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/edit/{id}")
    String editProduct(
            Model uiModel,
            @PathVariable("id") Long catContractId,
            @RequestParam(value = "reason", required = false)
            String reason,
            @RequestParam(value = "property_reason", required = false)
            String property_reason,
            @RequestParam(value = "fr_reason", required = false)
            String fr_reason
    ){

        Locale locale = LocaleContextHolder.getLocale();

        User user = userLoginUtil.getCurrentLogInUser();

        if(reason != null){
            uiModel.addAttribute("reason", reason);
        }

        if(property_reason != null){
            uiModel.addAttribute("property_reason", property_reason);
        }

        if(fr_reason != null){
            uiModel.addAttribute("fr_reason", fr_reason);
        }

        CatContract catContract = (CatContract) languageUtil.getLocalizatedObject(catContractService.getCatContractById(catContractId), locale.getLanguage());

        // Уже рассчитанные риски
        List<CatContractRisk> calculatedRisk = riskService.getExistRisks(null, catContract);
        // Риски, которые еще можно рассчитать
        List<Risk> potentialRisks = null;
        if(catContract.getProduct().equals(CatContract.Product.TP2)){
            potentialRisks = productProcessorTP2.getRisksMayBeUsedForAddingProductSettings(catContract);
            //List<Integer> ls = productProcessorTP2.getLengthValuesToAddProductSettings(catContract, riskService.getRiskById(19l));
            LinkedHashMap<Risk, List<Integer>> potRisksAndLength = new LinkedHashMap<Risk, List<Integer>>();
            for(Risk risk : potentialRisks){
                potRisksAndLength.put(risk, productProcessorTP2.getLengthValuesToAddProductSettings(catContract, risk));
                uiModel.addAttribute("potentialRisks", potRisksAndLength);
            }
        }
        else{
            potentialRisks = riskService.getPotentialRiskTermRisks(null, catContract);
            uiModel.addAttribute("potentialRisks", potentialRisks);
        }

        if(catContract.getFixedSumsPolicy() != null && !catContract.getFixedSumsPolicy().equals(CatContract.FixedSumsPolicy.NONE)){
            uiModel.addAttribute("risksToFixedSums", riskService.getParentRisksForCatContractToAnyPartnerIfPossible(catContract));
            uiModel.addAttribute("existsFixedSums", catContractFixedSumService.getCatContractFixedSumsForCatContract(catContract));
        }


        uiModel.addAttribute("calculatedRisks", calculatedRisk);


        uiModel.addAttribute("catContract", catContract);


        // Денежные параметры, которые уже заданы для партнера (по сериям) и те, которые могут быть заданы
        List<ProductMoneyProperty> existsProperties = partnerService.getProductMoneyProperties(catContract);
        List<ProductMoneyProperty> potentialProperties = partnerService.getProductMoneyPropertiesNotSet(catContract);

        uiModel.addAttribute("existsProp", existsProperties);
        uiModel.addAttribute("potentialProp", potentialProperties);


        if(catContract.getProduct().equals(CatContract.Product.TP0)){
            List<CatContractPartnerFranchise> catContractFranchises = catContractService.getFranchises(catContract);
            uiModel.addAttribute("franchises", catContractFranchises);
        }



        return "product_settings/edit";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/edit/{id}", params = "new_property")
    public String newProperty(
            @PathVariable(value = "id")
            Long catContractId,
            @RequestParam(value = "property_id")
            Long propertyId,
            @RequestParam(value = "value")
            BigDecimal value,
            @RequestParam(value = "use_property", required = false)
            Boolean useProperty,
            Model uiModel
    ){

        CatContract catContract = catContractService.getCatContractById(catContractId);
        ProductMoneyProperty productMoneyProperty = partnerService.getProductMoneyPropertyById(propertyId);
        productMoneyProperty.setMoneyValue(value);
        productMoneyProperty.setUseProperty(useProperty);
        try {
            partnerService.updateParametersForProductMoneyProperty(productMoneyProperty);
        } catch (EntityNotSavedException e) {
            uiModel.addAttribute("property_reason", e.getMessage());
            return "redirect:/productsettings/edit/"+catContractId;
        }

        return "redirect:/productsettings/edit/"+catContractId;
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/edit/{id}", params = "delete_property")
    public String deleteProperty(
            @PathVariable(value = "id")
            Long catContractId,
            @RequestParam(value = "pr_id")
            Long propertyId,
            Model uiModel
    ){

        partnerService.clearProductPropertyGlobalParams(propertyId);

        return "redirect:/productsettings/edit/"+catContractId;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/edit/{id}", params = "new_risk")
    public String newRisk(
            @PathVariable(value = "id")
            Long catContractId,
            @RequestParam(value = "risk")
            Long riskId,
            @RequestParam(value = "rate")
            BigDecimal rate,
            @RequestParam(value = "monthTarif")
            BigDecimal monthTarif,
            @RequestParam(value = "minSum")
            BigDecimal minSum,
            @RequestParam(value = "maxSum")
            BigDecimal maxSum,
            @RequestParam(value = "mandatory", required = false)
            Boolean mandatory,
            @RequestParam(value = "fixedPremium", required = false)
            BigDecimal fixedPremium,
            @RequestParam(value = "length", required = false)
            Integer length
    ){
        Risk risk = null;
        try {
            risk = riskService.getRiskById(riskId);

            CatContract catContract = catContractService.getCatContractById(catContractId);

            CatContractRisk catContractRisk = new CatContractRisk();
            catContractRisk.setCatContract(catContract);
            catContractRisk.setPartner(null);
            catContractRisk.setRisk(risk);
            catContractRisk.setMonthTarif(monthTarif);
            catContractRisk.setRate(rate);
            catContractRisk.setMinSum(minSum);
            catContractRisk.setMaxSum(maxSum);
            catContractRisk.setMandatory((mandatory != null)? mandatory : false);
            catContractRisk.setLength(length);

            riskService.newCatContractRisk(catContractRisk);
        } catch (NoEntityException e) {
            //
        }



        return "redirect:/productsettings/edit/"+catContractId;
    }


    @RequestMapping(method = RequestMethod.DELETE,value = "/edit/{id}", params = "delete_risk")
    public String deleteRisk(
            @PathVariable(value = "id")
            Long catContractId,
            @RequestParam(value = "id")
            Long catContractRiskId
    ){
        CatContractRisk catContractRisk = riskService.getCatContractRiskById(catContractRiskId);
        riskService.removeCatContractRisk(catContractRisk);

        return "redirect:/productsettings/edit/"+catContractId;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/edit/{id}", params = "add_franchise")
    public String addFranchise(
            @PathVariable(value = "id")
            Long catContractId,
            @RequestParam(value = "franchiseMoney")
            BigDecimal franchiseMoney,
            @RequestParam(value = "discountPercent")
            BigDecimal discountPercent,
            Model uiModel
    ){
        CatContract catContract = catContractService.getCatContractById(catContractId);

        if(catContract != null && franchiseMoney != null && discountPercent != null){
            CatContractPartnerFranchise catContractPartnerFranchise = new CatContractPartnerFranchise();
            catContractPartnerFranchise.setCatContract(catContract);
            catContractPartnerFranchise.setFranchiseMoney(franchiseMoney);
            catContractPartnerFranchise.setDiscount(discountPercent.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));

            try {
                partnerService.addProductFranchise(catContractPartnerFranchise);
            } catch (IllegalDataException e) {
                uiModel.addAttribute("fr_reason", e.getReason().getMsgCode());
            }
        }

        return "redirect:/productsettings/edit/"+catContractId;
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/edit/{id}", params = "del_franchise")
    public String addFranchise(
            @PathVariable(value = "id")
            Long catContractId,
            @RequestParam(value = "id")
            Long franchiseId
    ){
        CatContractPartnerFranchise catContractPartnerFranchise = partnerService.getCatContractPartnerFranchiseById(franchiseId);

        if(catContractPartnerFranchise != null)
            partnerService.deleteCatContractPartnerFranchise(catContractPartnerFranchise);

        return "redirect:/productsettings/edit/"+catContractId;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/edit/{id}", params = "add_fixed_sum")
    public String addFixedSum(
            @PathVariable(value = "id")
            Long catContractId,
            @RequestParam(value = "risk")
            Long riskId,
            @RequestParam(value = "sum")
            BigDecimal sum
    ) throws NoEntityException {
        CatContract catContract = catContractService.getCatContractById(catContractId);
        Risk risk = riskService.getRiskById(riskId);

        catContractFixedSumService.save(catContract, risk, sum);

        return "redirect:/productsettings/edit/"+catContractId;
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/edit/{id}", params = "del_fixed_sum")
    public String delFixedSum(
            @PathVariable(value = "id")
            Long catContractId,
            @RequestParam(value = "id")
            Long fixedSumId
    ){
        CatContractFixedSum catContractFixedSum = catContractFixedSumService.getCatContractFixedSumById(fixedSumId);

        if(catContractFixedSum != null)
            catContractFixedSumService.deleteCatContractFixedSum(catContractFixedSum);

        return "redirect:/productsettings/edit/"+catContractId;
    }

}
