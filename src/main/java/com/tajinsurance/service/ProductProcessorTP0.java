package com.tajinsurance.service;

import com.tajinsurance.domain.*;
import com.tajinsurance.exceptions.CalculatePremiumException;
import com.tajinsurance.exceptions.NoEntityException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * Created by berz on 30.11.14.
 */
public interface ProductProcessorTP0 extends ProductProcessor {

    public Integer getMaxLength();

    BigDecimal calculateMult(ContractPremium contractPremium);

    BigDecimal getMultByAge(Person person);

    HashMap<Risk, String> getAddDataForRiskSumFields(List<Risk> risksToCalc, CatContract catContract);

    BigDecimal getMultByPeriod(Contract contract);

    BigDecimal getMultByTerritory(Contract contract);

    BigDecimal getMultByActivity(Contract contract);

    BigDecimal getMultByFranchise(Contract contract, Risk risk);

    boolean allowedAgeMult(Risk risk);

    boolean allowedPeriodMult(Risk risk);

    boolean allowedTerritoryMult(Risk risk);

    boolean allowedActivityMult(Risk risk);

    boolean allowedFranchiseMult(Risk risk);

    void premiumCorrection(Long id, Contract contract, BigDecimal q) throws NoEntityException, CalculatePremiumException;

    BigDecimal getInsuredSumFromRisk(Risk risk, CatContract cc, BigDecimal bigDecimal);

    BigDecimal getPremiumWOSavingWithAge(Contract contract, CatContract cc, Risk risk, BigDecimal bigDecimal, Integer age) throws CalculatePremiumException;

    BigDecimal getMultByAge(Integer age);

    HashMap<Risk,BigDecimal> getFixedSumsForRisks(List<Risk> risksToCalc, CatContract catContract, Partner partner);
}
