package com.tajinsurance.web;

import com.tajinsurance.domain.CatContract;
import com.tajinsurance.domain.CatContractLocaleEntity;
import com.tajinsurance.domain.Currency;
import com.tajinsurance.domain.Risk;
import com.tajinsurance.service.CatContractService;
import com.tajinsurance.service.RiskService;
import com.tajinsurance.utils.LanguageUtil;
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
 * Created by berz on 22.04.14.
 */
@Controller
@RooWebScaffold(formBackingObject = CatContract.class, path = "/catcontracts")
@RequestMapping(value = "/catcontracts")
public class CatContractController {

    @Autowired
    RiskService riskService;

    @Autowired
    CatContractService catContractService;

    @Autowired
    LanguageUtil languageUtil;

    public CatContractController() {
    }

    @RequestMapping(params = "form")
    public String createCatContractForm(
            Model uiModel,
            @RequestParam(value = "reason", required = false)
            String reason
    ){
        Locale locale = LocaleContextHolder.getLocale();

        /* Attension! id = 1 - simple risk */
        List<Risk> allRisks = riskService.getAllRisksByType(locale.getLanguage(), riskService.getTypeOfRiskById(1l));

        List<Currency> currencies = catContractService.getCurrencies();
        uiModel.addAttribute("currencies", currencies);

        uiModel.addAttribute("allrisktypes", riskService.getAllRiskTypes());

        uiModel.addAttribute("allrisks", allRisks);

        if(reason != null){
            uiModel.addAttribute("reason", reason);
        }

        List<String> languages = new LinkedList<String>();
        languages.add("ru");
        languages.add("en");

        uiModel.addAttribute("langs", languages);

        return "catcontracts/new";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String saveCatContract(
            Model uiModel,
            @RequestParam(value = "localeEntityNamesList")
            ArrayList<String> localeEntityNamesList,
            @RequestParam(value = "localeEntityValuesList")
            ArrayList<String> localeEntityValuesList,
            @RequestParam(value = "localeEntityLangList")
            ArrayList<String> localeEntityLangList,
            @RequestParam(value = "risks", required = false)
            List<Long> riskIds,
            @RequestParam(value = "currency")
            Long currencyId,
            @RequestParam(value = "minTerm")
            Integer minTerm,
            @RequestParam(value = "typeOfRisk")
            Long typeOfRiskId,
            @RequestParam(value = "useInsuranceAreas", required = false)
            Boolean useInsuranceAreas
    ){
        List<Risk> risks = null;
        if(riskIds != null){
            risks = riskService.getRisksFromIds(riskIds);
        }

        List<CatContractLocaleEntity> catContractLocaleEntities = new LinkedList<CatContractLocaleEntity>();
        Currency currency = catContractService.getCurrencyById(currencyId);

        for(int i = 0; i < localeEntityLangList.size(); i++){
            CatContractLocaleEntity catContractLocaleEntity = new CatContractLocaleEntity();
            catContractLocaleEntity.setLocale(localeEntityLangList.get(i));
            catContractLocaleEntity.setName(localeEntityNamesList.get(i));
            catContractLocaleEntity.setValue(localeEntityValuesList.get(i));

            catContractLocaleEntities.add(catContractLocaleEntity);
        }

        catContractService.createNewCatContract(catContractLocaleEntities, risks, currency, minTerm, typeOfRiskId, useInsuranceAreas);

        return "redirect:/catcontracts?list";
    }


    @RequestMapping(method = RequestMethod.PUT)
    public String updateCatContract(
            Model uiModel,
            @RequestParam(value = "id")
            Long id,
            @RequestParam(value = "localeEntityNamesList")
            ArrayList<String> localeEntityNamesList,
            @RequestParam(value = "localeEntityValuesList")
            ArrayList<String> localeEntityValuesList,
            @RequestParam(value = "localeEntityLangList")
            ArrayList<String> localeEntityLangList,
            @RequestParam(value = "localeEntityIdList")
            ArrayList<String> localeEntityIdList,
            @RequestParam(value = "risks", required = false)
            List<Long> riskIds,
            @RequestParam(value = "currency")
            Long currencyId,
            @RequestParam(value = "minTerm")
            Integer minTerm
    ){



        List<Risk> risks = null;
        if(riskIds != null) risks = riskService.getRisksFromIds(riskIds);
        List<CatContractLocaleEntity> catContractLocaleEntities = new LinkedList<CatContractLocaleEntity>();
        Currency currency = catContractService.getCurrencyById(currencyId);

        for(int i = 0; i < localeEntityLangList.size(); i++){
            CatContractLocaleEntity catContractLocaleEntity = new CatContractLocaleEntity();
            catContractLocaleEntity.setId(Long.decode(localeEntityIdList.get(i)));
            catContractLocaleEntity.setLocale(localeEntityLangList.get(i));
            catContractLocaleEntity.setName(localeEntityNamesList.get(i));
            catContractLocaleEntity.setValue(localeEntityValuesList.get(i));

            catContractLocaleEntities.add(catContractLocaleEntity);
        }



        catContractService.updateCatContract(id, catContractLocaleEntities, risks, currency, minTerm);

        return "redirect:/catcontracts?list";
    }

    @RequestMapping(params = "list")
    public String getCatContracts(Model uiModel){

        Locale locale = LocaleContextHolder.getLocale();

        List<CatContract> catContracts = catContractService.getAllCatContracts(locale.getLanguage());

        uiModel.addAttribute("catcontracts", catContracts);



        return "catcontracts/list";
    }

    @RequestMapping(params = "form", value = "/{id}")
    public String getEditForm(
            Model uiModel,
            @PathVariable(value = "id")
            Long id,
            @RequestParam(value = "reason", required = false)
            String reason
    ){

        Locale locale = LocaleContextHolder.getLocale();
        CatContract catContract = catContractService.getCatContractById(id);
        catContract = (CatContract) languageUtil.getLocalizatedObject(catContract, locale.getLanguage());

        HashMap<String, HashMap<String, String>> localeObjects = new HashMap<String, HashMap<String, String>>();
        for(CatContractLocaleEntity catContractLocaleEntity : catContract.getLocaleEntityList()){
            HashMap<String, String> loc = new HashMap<String, String>();
            loc.put("name", catContractLocaleEntity.getName());
            loc.put("value", catContractLocaleEntity.getValue());
            loc.put("id", catContractLocaleEntity.getId().toString());

            localeObjects.put(catContractLocaleEntity.getLocale(), loc);
        }

        uiModel.addAttribute("catcontract", catContractService.getCatContractById(id));

        uiModel.addAttribute("localeObjects", localeObjects);

        uiModel.addAttribute("catContract", catContract);

        List<Risk> allRisks = riskService.getAllRisks(locale.getLanguage());

        List<Currency> currencies = catContractService.getCurrencies();
        uiModel.addAttribute("currencies", currencies);

        uiModel.addAttribute("allrisks", allRisks);

        if(reason != null){
            uiModel.addAttribute("reason", reason);
        }

        List<String> languages = new LinkedList<String>();
        languages.add("ru");
        languages.add("en");

        uiModel.addAttribute("langs", languages);

        return "catcontracts/edit";
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public String deleteCatContract(
            @PathVariable(value = "id")
            Long catContractId){

        catContractService.remove(catContractService.getCatContractById(catContractId));

        return "redirect:/catcontracts?list";
    }
}
