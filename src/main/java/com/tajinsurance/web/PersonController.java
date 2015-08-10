package com.tajinsurance.web;

import com.google.gson.Gson;
import com.tajinsurance.domain.*;
import com.tajinsurance.domain.City;
import com.tajinsurance.dto.*;
import com.tajinsurance.exceptions.AgeException;
import com.tajinsurance.exceptions.CalculatePremiumException;
import com.tajinsurance.exceptions.NoEntityException;
import com.tajinsurance.exceptions.ResourceNotFoundException;
import com.tajinsurance.service.*;
import com.tajinsurance.utils.LanguageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * Created by berz on 24.03.14.
 */
@RequestMapping("/persons")
@Controller
@RooWebScaffold(path = "persons", formBackingObject = Person.class)
public class PersonController {

    @Autowired
    PersonService personService;

    @Autowired
    ContractService contractService;

    @Autowired
    PremiumService premiumService;

    @Autowired
    RiskService riskService;

    @Autowired
    ProductProcessor productProcessor;

    @Autowired
    InsuranceAreaService insuranceAreaService;

    @Autowired
    LanguageUtil languageUtil;

    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(
            @RequestParam(value = "cc_id", required = false)
            CatContract catContract,
            Model uiModel
    ) {
        if(catContract != null)
            uiModel.addAttribute("product", catContract.getProduct());
        else System.out.println("null cat contract");
        uiModel.addAttribute("person", new Person());
        return "persons/create";
    }


    @RequestMapping(params = "search", method = RequestMethod.POST)
    @ResponseBody
    byte[] searchPerson(Person person) {
        Gson gson = new Gson();
        String g = gson.toJson(personService.searchPersonsByPerson(person));
        try {
            return g.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // Это значит, что utf нет.
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.POST, params = "getCitiesAndIndexes")
    @ResponseBody
    public byte[] getCities(
            @RequestParam(value = "term")
            String str
    ) {
        Gson gson = new Gson();

        List<City> cities = personService.getCitiesByStr(str);

        String g = gson.toJson(cities);
        try {
            return g.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // Это значит, что utf нет.
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.GET, produces = "text/html", params = "ajax")
    @ResponseBody
    public byte[] getPersonDetailsAjax(
            @RequestParam(value = "id") Long id
    ) {
        Gson gson = new Gson();

        Person p = personService.getPersonById(id);

        if (p == null) throw new ResourceNotFoundException("there is no person with id=" + id);

        PersonContentObject res = new PersonContentObjectWithDates(p);

        String g = gson.toJson(res);
        try {
            return g.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // Это значит, что utf нет.
            return null;
        }
    }


    @RequestMapping(params = "new", method = RequestMethod.POST)
    @ResponseBody
    byte[] createPerson(Person person) {
        Gson gson = new Gson();
        PersonSaveAjaxAction res = new PersonSaveAjaxAction();
        try {
            Person p = personService.createNewPerson(person);
            res.person = new PersonContentObjectWithDates(p);
            res.success = true;
            res.message = "success adding";
       /* } catch (EntityNotSavedException e) {
            res.success = false;
            res.message = e.getMessage();*/
        } catch (AgeException e) {
            res.success = false;
            res.message = e.getMessage();
            res.ageProblem = true;
            if (e.errorCode != null) res.errorCode = e.errorCode;
        }
        String g = gson.toJson(res);
        try {
            return g.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // Это значит, что utf нет.
            return null;
        }
    }


    @RequestMapping(value = "/edit", produces = "text/html")
    public String editPersonForm(
            @RequestParam(value = "id") Long id,
            @RequestParam(value = "ajax", required = false) String ajax,
            @RequestParam(value = "cc_id", required = false) CatContract catContract,
            Model uiModel
    ) {

        Person person = personService.getPersonById(id);

        if (person == null) throw new ResourceNotFoundException("there is not Person with id=" + id);

        if(catContract != null){
            uiModel.addAttribute("product", catContract.getProduct());
        }
        uiModel.addAttribute("person", person);

        return (ajax == null) ? "persons/edit" : "ajax/persons/edit";
    }

    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    @ResponseBody()
    public byte[] editPerson(Model uiModel, Person person) {

        Gson gson = new Gson();
        //System.out.println("sex: "+person.getSex().toString());
        PersonUpdateAjaxAction pua = new PersonUpdateAjaxAction();

        try {
            Person p = personService.edit(person);
            pua.success = true;
            pua.person = new PersonContentObjectWithDates(p);
            pua.personAjax = new PersonAjax(p.getId(), p.toString());
        } finally {
            String res = gson.toJson(pua);
            try {
                return res.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                return res.getBytes(); // нет utf-8;
            }
        }

    }

    @RequestMapping(method = RequestMethod.POST, produces = "text/html", value = "/{id}")
    @ResponseBody()
    public byte[] editPersonPut(
            Model uiModel, Person person,
            @PathVariable(value = "id") Long id
    ) {
        person.setId(id);

        return editPerson(uiModel, person);
    }

    @RequestMapping(params = "print_claim")
    public String printClaim(
            @RequestParam(value = "id") Long personId,
            @RequestParam(value = "cid") Long contractId,
            Model uiModel
    ) {
        Contract contract;
        try {
            Locale locale = LocaleContextHolder.getLocale();

            contract = contractService.getContractById(contractId, locale.getLanguage());
            Person person = personService.getPersonById(personId);
            BigDecimal sumToPay;
            if (contract.getPremiumAdm() == null) {
                //sumToPay = riskService.getSumForAllRisksWithoutSaving(contract, contract.getSum(), contract.getLength());
                sumToPay = productProcessor.getProductProcessorImplementation(contract).getAllPremiumForContract(contract);
            } else {
                sumToPay = contract.getPremiumAdm();
            }
            List<CatContractRisk> risks = riskService.getRisksToCalcForContract(contract);

            BigDecimal insured_sum;
            if (contract.getInsuredSumAdm() == null) {
                insured_sum = productProcessor.getProductProcessorImplementation(contract).getIntegralInsuredSumForContract(contract);
            } else {
                insured_sum = contract.getInsuredSumAdm();
            }

            if (contract.getCatContract().getProduct().equals(CatContract.Product.MP0)) {


                if (contract.getInsuranceCostAdm() == null) {
                    uiModel.addAttribute("fullCost", productProcessor.getProductProcessorImplementation(contract).getFullCost(contract));
                } else {
                    uiModel.addAttribute("fullCost", contract.getInsuranceCostAdm());
                }

                if (contract.getCatContract().getUseInsuranceAreas() != null && contract.getCatContract().getUseInsuranceAreas()) {
                    List<InsuranceArea> insuranceAreas = insuranceAreaService.getInsuranceAreasForContract(contract);

                    // Рассчет снижения страховой суммы в отношении товарно-мат ценностей в обороте

                    // страховая сумма по мат ценностям в обороте по всем терриориям
                    BigDecimal propInTurnOverInsSum = BigDecimal.ZERO;

                    for (InsuranceArea insuranceArea : insuranceAreas) {
                        for (InsuranceObject insuranceObject : insuranceArea.getInsuranceObjectList()) {
                            if (insuranceObject.getRisk().getDet() != null && insuranceObject.getRisk().getDet().equals("TMO")) {
                                propInTurnOverInsSum = propInTurnOverInsSum.add(insuranceObject.getSum());
                            }
                        }
                    }

                    if (propInTurnOverInsSum.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal propInTurnOverInsSumDelta = propInTurnOverInsSum.divide(BigDecimal.valueOf(contract.getLength()), 0, RoundingMode.HALF_UP);
                        uiModel.addAttribute("propInTurnOverInsSumDelta", propInTurnOverInsSumDelta);
                    }

                    uiModel.addAttribute("insuranceAreas", insuranceAreas);
                }

                uiModel.addAttribute("contractPremiums", premiumService.getValidatedPremiums(contract));
            }

            Integer length = contract.getLength() * 30;


            List<ContractPremiumAjax> validatedPremiums = premiumService.getValidatedPremiums(contract);
            uiModel.addAttribute("validatedPremiums", validatedPremiums);

            if (validatedPremiums != null) {
                uiModel.addAttribute("premiums", premiumService.getValidatedCPremiums(contract));
            }

            uiModel.addAttribute("insured_sum", insured_sum.setScale(0, RoundingMode.HALF_UP));
            uiModel.addAttribute("contract", contract);
            uiModel.addAttribute("person", person);
            uiModel.addAttribute("sumToPay", sumToPay.setScale(0, RoundingMode.HALF_UP));
            uiModel.addAttribute("risks", risks);
            uiModel.addAttribute("length", length);

            if (contract != null) {

                if (contract.getCatContract().getProduct().equals(CatContract.Product.CU0))
                    return "persons/claim/cu0";
                if (contract.getCatContract().getProduct().equals(CatContract.Product.BA0))
                    return "persons/claim/ba0";
                if (contract.getCatContract().getProduct().equals(CatContract.Product.MP0))
                    return "persons/claim/mp0";


                return "persons/claim";
            }


        } catch (NoEntityException e) {

            e.printStackTrace();
        } catch (CalculatePremiumException e) {
            e.printStackTrace();
        }

        return "presons/claim";
    }

    @RequestMapping(method = RequestMethod.GET, params = "clean")
    public String getCleanWindow() {
        return "ajax/persons/clean";
    }
}
