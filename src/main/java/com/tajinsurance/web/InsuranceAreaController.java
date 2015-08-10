package com.tajinsurance.web;

import com.tajinsurance.domain.*;
import com.tajinsurance.exceptions.NoEntityException;
import com.tajinsurance.service.ContractService;
import com.tajinsurance.service.InsuranceAreaService;
import com.tajinsurance.service.RiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

import java.util.List;
/**
 * Created by berz on 14.09.14.
 */
@Controller
@RequestMapping("/insarea")
public class InsuranceAreaController {
    @Autowired
    InsuranceAreaService insuranceAreaService;

    @Autowired
    ContractService contractService;

    @Autowired
    RiskService riskService;

    @RequestMapping(method = RequestMethod.POST, params = "new_area")
    public String saveInsArea(
            @RequestParam(value = "contract_id", required = true)
            Long contractId,
            @RequestParam(value = "address", required = true)
            String address,
            @RequestParam(value = "name", required = true)
            String name
    ){
        try {
            Contract contract = contractService.getContractById(contractId);
            InsuranceArea insuranceArea = new InsuranceArea();
            insuranceArea.setContract(contract);
            insuranceArea.setAddress(address);
            insuranceArea.setName(name);

            insuranceAreaService.saveInsuranceArea(insuranceArea);
        } catch (NoEntityException e) {
            //
        }
        finally {
            return "redirect:/contracts/" + contractId + "/?form";
        }
    }

    @RequestMapping(method = RequestMethod.POST, params = "new_object")
    public String newObject(
            @RequestParam(value = "name")
            String name,
            @RequestParam(value = "sum")
            BigDecimal sum,
            @RequestParam(value = "realsum")
            BigDecimal realsum,
            @RequestParam(value = "risk")
            Long riskId,
            @RequestParam(value = "contract_id")
            Long contractId,
            @RequestParam(value = "insurance_area_id")
            Long insuranceAreaId
    ){
        try {
            Contract contract = contractService.getContractById(contractId);
            Risk risk = riskService.getRiskById(riskId);
            InsuranceArea insuranceArea = insuranceAreaService.getInsuranceAreaById(insuranceAreaId);


            InsuranceObject insuranceObject = new InsuranceObject();
            insuranceObject.setName(name);
            insuranceObject.setSum(sum);
            insuranceObject.setRealsum(realsum);

            if(sum.compareTo(realsum) == 1) insuranceObject.setSum(realsum);

            insuranceObject.setRisk(risk);
            insuranceObject.setInsuranceArea(insuranceArea);

            insuranceAreaService.saveInsuranceObject(insuranceObject);
        } catch (NoEntityException e) {
            //
        }
        finally {
            return "redirect:/contracts/" + contractId + "/?form";
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, params = "delete_object")
    public String deleteObject(
            @RequestParam(value = "id")
            Long id
    ){
        InsuranceObject insuranceObject = insuranceAreaService.getInsuranceObjectById(id);
        Long contractId = insuranceObject.getInsuranceArea().getContract().getId();
        insuranceAreaService.deleteInsuranceObject(insuranceObject);

        return "redirect:/contracts/" + contractId + "/?form";
    }

    @RequestMapping(method = RequestMethod.POST, params = "add_security_system")
    public String addSecuritySystemToArea(
            @RequestParam(value = "insurance_area_id")
            Long insuranceAreaId,
            @RequestParam(value = "system")
            Long securitySystemId
    ){
        InsuranceArea insuranceArea = insuranceAreaService.getInsuranceAreaById(insuranceAreaId);
        SecuritySystem securitySystem = insuranceAreaService.getSecuritySystemById(securitySystemId);

        List<SecuritySystem> securitySystemList = insuranceArea.getSecuritySystems();
        securitySystemList.add(securitySystem);
        insuranceArea.setSecuritySystems(securitySystemList);

        insuranceAreaService.updateInsuranceArea(insuranceArea);

        return "redirect:/contracts/" + insuranceArea.getContract().getId() + "/?form";
    }

    @RequestMapping(method = RequestMethod.DELETE, params = "delete_security_system")
    public String deleteSecuritySystemFromArea(
            @RequestParam(value = "area_id")
            Long insuranceAreaId,
            @RequestParam(value = "security_system_id")
            Long securitySystemId
    ){
        InsuranceArea insuranceArea = insuranceAreaService.getInsuranceAreaById(insuranceAreaId);
        SecuritySystem securitySystem = insuranceAreaService.getSecuritySystemById(securitySystemId);

        List<SecuritySystem> securitySystemList = insuranceArea.getSecuritySystems();
        securitySystemList.remove(securitySystem);
        insuranceArea.setSecuritySystems(securitySystemList);

        insuranceAreaService.updateInsuranceArea(insuranceArea);

        return "redirect:/contracts/" + insuranceArea.getContract().getId() + "/?form";
    }

    @RequestMapping(method = RequestMethod.DELETE, params = "delete_area")
    public String deleteArea(
            @RequestParam(value = "area_id")
            Long insuranceAreaId
    ){
        InsuranceArea insuranceArea = insuranceAreaService.getInsuranceAreaById(insuranceAreaId);

        insuranceAreaService.deleteInsuranceArea(insuranceArea);

        return "redirect:/contracts/" + insuranceArea.getContract().getId() + "/?form";
    }

    @RequestMapping(method = RequestMethod.POST, params = "update_object")
    public String updateObject(
            @RequestParam(value = "id")
            Long insuranceObjectId,
            @RequestParam(value = "sum")
            BigDecimal sum,
            @RequestParam(value = "realsum")
            BigDecimal realsum
    ){
        //System.out.println("upd object " + realsum + ", " + sum);
        InsuranceObject insuranceObject = insuranceAreaService.getInsuranceObjectById(insuranceObjectId);
        insuranceObject.setSum(sum);
        insuranceObject.setRealsum(realsum);

        insuranceAreaService.updateObject(insuranceObject);

        return "redirect:/contracts/" + insuranceObject.getInsuranceArea().getContract().getId() + "/?get_ins_areas";
    }

}
