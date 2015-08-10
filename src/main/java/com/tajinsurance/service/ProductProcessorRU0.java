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
public interface ProductProcessorRU0 extends ProductProcessor {

    public Integer getLengthDays();

    BigDecimal calculateMult(ContractPremium contractPremium);

    void premiumCorrection(Long id, Contract contract, BigDecimal q) throws NoEntityException, CalculatePremiumException;

    BigDecimal getInsuredSumFromRisk(Risk risk, CatContract cc, BigDecimal bigDecimal);

    HashMap<Risk,BigDecimal> getFixedSumsForRisks(List<Risk> risksToCalc, CatContract catContract, Partner partner);
}
