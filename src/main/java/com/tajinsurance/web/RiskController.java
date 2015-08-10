package com.tajinsurance.web;

import com.google.gson.Gson;
import com.tajinsurance.domain.*;
import com.tajinsurance.dto.CalculateRiskAjaxAction;
import com.tajinsurance.dto.CatContractRiskToCreate;
import com.tajinsurance.dto.ContractPremiumAjax;
import com.tajinsurance.dto.DeletePremuimAjaxAction;
import com.tajinsurance.exceptions.NoEntityException;
import com.tajinsurance.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * Created by berz on 30.03.14.
 */
@RequestMapping("/risk")
@Controller
public class RiskController {
    @Autowired
    ContractService contractService;

    @Autowired
    CatContractService catContractService;

    @Autowired
    RiskService riskService;

    @Autowired
    PremiumService premiumService;

    @Autowired
    PartnerService partnerService;

    @RequestMapping(params = "calc", method = RequestMethod.POST)
    @ResponseBody
    byte[] calcRisk(
            @RequestParam(value = "contract") Long contractId,
            @RequestParam(value = "risk") Long riskId,
            @RequestParam(value = "sum") BigDecimal sum,
            @RequestParam(value = "length") Integer length
    ) {
        Gson gson = new Gson();
        CalculateRiskAjaxAction calc = new CalculateRiskAjaxAction();

        try {
            Risk risk = riskService.getRiskById(riskId);
            Contract contract = contractService.getContractById(contractId);

            if (!contractService.checkIfTheRiskIsAllowed(risk, contract)) {
                calc.success = false;
                calc.message = "Risk not allowed!";
            }

            ContractPremium contractPremium = premiumService.calculatePremium(new ContractPremium(risk, contract, sum), length);

            calc.contractPremiumAjax = new ContractPremiumAjax(risk.getValue(), contractPremium.getInsuredSum(), contractPremium.getPremium(), contractPremium.getId());

            //calc.contractPremium = contractPremium;
            calc.success = true;

        } catch (NoEntityException e) {
            calc.success = false;
            calc.message = "object not found!";
        } finally {
            try {
                return gson.toJson(calc).getBytes("UTF-8");
            } catch (UnsupportedEncodingException e1) {
                return gson.toJson(calc).getBytes();
            }
        }


    }

    @RequestMapping(params = "del")
    @ResponseBody
    byte[] deletePremium(
            @RequestParam(value = "id") Long id
    ) {
        Gson gson = new Gson();
        DeletePremuimAjaxAction del = new DeletePremuimAjaxAction();

        try {
            del.success = premiumService.deletePremium(id);
            del.id = id;
        } catch (NoEntityException e) {
            del.success = false;
            del.message = "premium not found";
        } finally {
            try {
                return gson.toJson(del).getBytes("UTF-8");
            } catch (UnsupportedEncodingException e1) {
                return gson.toJson(del).getBytes();
            }
        }
    }

    @RequestMapping(params = "form")
    public String newRiskForm(
            Model uiModel
    ) {


        uiModel.addAttribute("risk", new Risk());
        return "risk/new";
    }


    @RequestMapping(params = "list")
    public String riskList(
            Model uiModel
    ) {
        Locale locale = LocaleContextHolder.getLocale();

        List<Risk> risks = riskService.getAllRisksByType(locale.getLanguage(), riskService.getTypeOfRiskById(1l));

        uiModel.addAttribute("risks", risks);
        return "risk/list";
    }

    @RequestMapping(params = "form", value = "/{id}")
    public String editForm(
            Model uiModel,
            @PathVariable(value = "id")
            Long id
    ){
        try {
            Locale locale = LocaleContextHolder.getLocale();

            Risk risk = riskService.getRiskById(id);
            uiModel.addAttribute("risk", risk);


            List<CatContractRiskToCreate> catContractRisksToCreate = (List<CatContractRiskToCreate>) riskService.getPotentialRiskTermPartnersAndCatContracts(risk, locale.getLanguage());

            if(catContractRisksToCreate.size() > 0 ){
                uiModel.addAttribute("risksToAdd", catContractRisksToCreate);
            }

            List<CatContractRisk> catContractRiskList = riskService.getExistRisks(risk, locale.getLanguage());

            if(catContractRiskList.size() > 0){
                uiModel.addAttribute("riskTerms", catContractRiskList);
            }

            return "risk/edit";
        } catch (NoEntityException e) {
            return "redirect:/risk?list";
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    String saveRisk(
            Model uiModel,
            Risk risk
    ){
        risk.setTypeOfRisk(riskService.getTypeOfRiskById(1l));
        risk = riskService.save(risk);

        return "redirect:/risk/"+risk.getId()+"?form";
    }

    @RequestMapping(method = RequestMethod.PUT)
    String updateRisk(
            Model uiModel,
            Risk risk
    ){
        riskService.update(risk);

        return "redirect:/risk/"+risk.getId()+"?form";
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public String deleteRisk(
            Model uiModel,
            @PathVariable(value = "id")
            Long riskId
    ){

        try {
            riskService.delete(riskService.getRiskById(riskId));
        } catch (NoEntityException e) {
            // fuck that
        }
        finally {
            return "redirect:/risk?list";
        }


    }

    @RequestMapping(params = "term", method = RequestMethod.DELETE, value = "/{risk}")
    String deleteCatContractRisk(
            @RequestParam(value = "id")
            Long id,
            @PathVariable(value = "risk")
            Long riskId
    ){
        CatContractRisk ccr = riskService.getCatContractRiskById(id);

        riskService.removeCatContractRisk(ccr);

        return "redirect:/risk/" + riskId +"?form";
    }

    @RequestMapping(params = "new_risk", method = RequestMethod.POST, value = "/{risk}")
    String newCatContractRisk(
            Model uiModel,
            @RequestParam(value = "rate")
            BigDecimal rate,
            @RequestParam(value = "monthTarif")
            BigDecimal monthTarif,
            @RequestParam(value = "minSum")
            BigDecimal minSum,
            @RequestParam(value = "maxSum")
            BigDecimal maxSum,
            @RequestParam(value = "partner")
            Long partnerId,
            @RequestParam(value = "catContract")
            Long catContractId,
            @PathVariable(value = "risk")
            Long riskId
    ){

        try {
            Risk risk = riskService.getRiskById(riskId);
            Partner p = partnerService.getPartnerById(partnerId);
            CatContract cc = catContractService.getCatContractById(catContractId);

            CatContractRisk ccr = new CatContractRisk();
            ccr.setCatContract(cc);
            ccr.setPartner(p);
            ccr.setRisk(risk);

            ccr.setRate(rate);
            ccr.setMonthTarif(monthTarif);
            ccr.setMinSum(minSum);
            ccr.setMaxSum(maxSum);

            riskService.newCatContractRisk(ccr);

        } catch (NoEntityException e) {
            //
        }
        finally {
            return "redirect:/risk/" + riskId +"?form";
        }


    }


}
