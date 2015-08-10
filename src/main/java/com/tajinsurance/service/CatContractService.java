package com.tajinsurance.service;

import com.tajinsurance.domain.*;
import com.tajinsurance.dto.RiskAjax;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by berz on 20.03.14.
 */
@Service
public interface CatContractService {
    List<CatContract> getAllCatContracts(String locale);

    CatContract getCatContractById(Long id);

    List<CatContract> getAllowedCatContractsForUser(User u, String locale);

    List<RiskAjax> getAllowedRisksForCatContract(CatContract cc);

    @Secured({"ROLE_USER","ROLE_ADMIN"})
    void createNewCatContract(List<CatContractLocaleEntity> localeEntities, List<Risk> risks, Currency currency, Integer minTerm, Long typeOFRiskId, Boolean useInsuranceArea);

    List<Currency> getCurrencies();

    Currency getCurrencyById(Long currencyId);

    @Secured({"ROLE_USER","ROLE_ADMIN"})
    void updateCatContract(Long catContractId, List<CatContractLocaleEntity> catContractLocaleEntities, List<Risk> risks, Currency currency, Integer minTerm);
    @Secured({"ROLE_USER","ROLE_ADMIN"})
    void remove(CatContract catContract);

    List<CatContract> getCatContractsGlobalSettings(String language);

    List<CatContractPartnerFranchise> getFranchises(CatContract catContract);
}
