package com.tajinsurance.service;

import com.tajinsurance.domain.*;
import com.tajinsurance.exceptions.CalculatePremiumException;
import com.tajinsurance.exceptions.NoEntityException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by berz on 30.03.14.
 */
@Service
public interface RiskService {
    Risk getRiskById(Long id) throws NoEntityException;

    BigDecimal getRateForRisk(Risk risk, CatContract cc);

    CatContractRisk getRisk(Risk risk, CatContract cc, Partner partner);

    List<Risk> getAllRisks(String locale);

    List<Risk> getAllRisksByType(String locale, TypeOfRisk typeOfRisk);

    List<Risk> getAllRisksByType(TypeOfRisk typeOfRisk);

    List<TypeOfRisk> getAllRiskTypes();

    TypeOfRisk getTypeOfRiskById(Long id);

    List<Risk> getRisksFromIds(List<Long> riskIds);

    Risk save(Risk risk);

    void update(Risk risk);

    void delete(Risk risk);

    Object getPotentialRiskTermPartnersAndCatContracts(Risk risk, String locale);

    List<CatContractRisk> getExistRisks(Risk risk, String language);

    void newCatContractRisk(CatContractRisk ccr);

    CatContractRisk getCatContractRiskById(Long id);

    void removeCatContractRisk(CatContractRisk ccr);

    List<CatContractRisk> getRisksToCalcForContract(Contract contract);

    List<CatContractRisk> getExistRisks(Partner partner, CatContract cc);

    List<Risk> getPotentialRiskTermRisks(Partner partner, CatContract cc);

    BigDecimal getSumForAllRisksWithoutSaving(Contract contract, BigDecimal sum, Integer length) throws CalculatePremiumException;

    BigDecimal getSumForAllRisksWithoutSaving(Contract contract) throws CalculatePremiumException;


    BigDecimal getAllInsuredSumForAllRisk(Contract contract, BigDecimal sum);

    BigDecimal getMajorInsuredSumForAllRisks(Contract contract, BigDecimal sum);

    BigDecimal getMaxSum(Contract contract);

    BigDecimal getMaxInsuredSum(Contract contract);

    BigDecimal getMinSum(Contract contract);

    BigDecimal getMinInsuredSum(Contract contract);

    Integer getMinTerm(Contract contract);

    boolean riskIsAllowed(CatContract catContract, Partner partner, Risk risk);

    List<Risk> getRisksForInsuranceArea();

    List<Risk> getRisksForInsuranceArea(Partner p, CatContract catContract);


    List<PartnerRiskCorrelation> getPartnerRiskCorrelations(Partner partner, CatContract catContract);

    List<Risk> getPotentialPartnerRiskCorrelations(Partner partner, CatContract catContract);

    void addNewRisksSet(Partner partner, CatContract catContract, String name, List<Risk> risks);

    List<ProductRiskSet> getProductRiskSets(Partner partner, CatContract catContract);

    void deleteRisksSet(Long setId);

    ProductRiskSet getProductRiskSetById(Long id);

    Risk getRiskByDet(String ext);

    List<CatContractRisk> getRisksForCatContractToAnyPartnerIfPossible(CatContract catContract);

    List<Risk> getParentRisksForCatContractToAnyPartnerIfPossible(CatContract catContract);
}
