package com.tajinsurance.service;

import com.tajinsurance.domain.*;
import com.tajinsurance.dto.ContractPremiumAjax;
import com.tajinsurance.exceptions.CalculatePremiumException;
import com.tajinsurance.exceptions.NoEntityException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by berz on 30.03.14.
 */
@Service
public interface PremiumService {

    ContractPremium savePremium(ContractPremium cp);

    ContractPremium getPremiumById(Long id) throws NoEntityException;

    List<ContractPremium> getValidatedCPremiums(Contract contract);


    List<ContractPremiumAjax> getValidatedPremiums(Contract contract);

    List<ContractPremiumAjax> getNotValidatedPremiums(Contract contract);

    ContractPremium calculatePremium(ContractPremium cp, Integer length) throws CalculatePremiumException;

    ContractPremium calculatePremium(ContractPremium cp, Integer length, BigDecimal q) throws CalculatePremiumException;

    ContractPremium calculatePremium(ContractPremium cp, Integer length, BigDecimal q, CatContractRisk r) throws CalculatePremiumException;

    boolean deletePremium(Long id) throws NoEntityException;

    BigDecimal calculatePremiumWOSaving(ContractPremium cp, Integer l) throws CalculatePremiumException;

    BigDecimal calculatePremiumWOSaving(ContractPremium cp, Integer l, BigDecimal q) throws CalculatePremiumException;

    BigDecimal calculatePremiumWOSaving(ContractPremium cp, Integer l, BigDecimal q, CatContractRisk r) throws CalculatePremiumException;

    BigDecimal getCorrellation(Risk risk, Partner partner, CatContract catContract);

    public void premiumCorrection(Long premiumId, Contract contract) throws CalculatePremiumException, NoEntityException;

    public void premiumCorrection(Long premiumId, Contract contract, BigDecimal q) throws CalculatePremiumException, NoEntityException;

    public void premiumCorrection(Long premiumId, Contract contract, BigDecimal q, CatContractRisk r) throws CalculatePremiumException, NoEntityException;

    public BigDecimal getAllInsuredSumForContractPremiums(Contract contract);

    List<Risk> getRisksInValidatedPremiums(Contract contract);
}
