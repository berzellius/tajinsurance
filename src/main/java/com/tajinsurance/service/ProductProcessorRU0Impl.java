package com.tajinsurance.service;

import com.tajinsurance.domain.*;
import com.tajinsurance.dto.ContractPremiumAjax;
import com.tajinsurance.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created by berz on 14.07.15.
 */
@Service
@Transactional
public class ProductProcessorRU0Impl implements ProductProcessorRU0 {

    @Autowired
    ContractService contractService;

    @Autowired
    RiskService riskService;

    @Autowired
    PremiumService premiumService;

    @Autowired
    CatContractService catContractService;

    @Autowired
    ExchangeCurrenciesService exchangeCurrenciesService;

    @Autowired
    CatContractStatusService catContractStatusService;

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public Integer getLengthDays() {
        return 30;
    }

    @Override
    public BigDecimal calculateMult(ContractPremium contractPremium) {
        return BigDecimal.ONE;
    }

    @Override
    public void premiumCorrection(Long id, Contract contract, BigDecimal q) throws NoEntityException, CalculatePremiumException {

    }

    @Override
    public BigDecimal getInsuredSumFromRisk(Risk risk, CatContract cc, BigDecimal bigDecimal) {
        return null;
    }

    @Override
    public HashMap<Risk, BigDecimal> getFixedSumsForRisks(List<Risk> risksToCalc, CatContract catContract, Partner partner) {
        return null;
    }

    @Override
    public ProductProcessor getProductProcessorImplementation(Contract contract) {
        throw new UnsupportedOperationException("allowed only for base ProductProcessorImpl!");
    }

    @Override
    public void updateContract(Contract contract, Boolean managerMode) throws NoEntityException, CalculatePremiumException, NoPersonToContractException, BadContractDataException, NoRelatedContractNumber, IOException, IllegalDataException {
        updateContract(contract, null, null, managerMode);
    }

    @Override
    public void updateContract(Contract contract, MultipartFile file, ContractImage contractImage, Boolean managerMode) throws NoEntityException, CalculatePremiumException, NoPersonToContractException, BadContractDataException, NoRelatedContractNumber, IOException, IllegalDataException {
        if (file != null) throw new IllegalArgumentException("upload files not allowed for TP0!");

        Contract oldContract;


        oldContract = contractService.getContractById(contract.getId());
        contract.setVersion(oldContract.getVersion());

        // Проверяем, задан ли Страхователь
        if (contract.getPerson() == null) throw new NoPersonToContractException("person_not_filled");


        if (contract.getStartDate() == null) throw new BadContractDataException("no_start_date");
        else {

            // Проверяем возраст
            if (contract.getPerson().getAge(contract.getStartDate()) < 18)
                throw new BadContractDataException("age_less_18");

            if (
                    (
                            contract.getPerson().getAge(contract.getStartDate()) > getMaxAgeForFemale() &&
                                    contract.getPerson().getSex().equals(Person.Sex.FEMALE)
                    ) ||
                            (
                                    contract.getPerson().getAge(contract.getStartDate()) > getMaxAgeForMale() &&
                                            contract.getPerson().equals(Person.Sex.MALE)
                            )
                    ) {
                throw new BadContractDataException("max_age_limit");
            }

            contract.setLength(getLengthDays());

            if (contract.getLength() != null) {
                // Определяем дату окончания договора
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(contract.getStartDate());
                calendar.add(Calendar.DATE, contract.getLength() - 1);
                contract.setEndDate(calendar.getTime());

                if (premiumService.getValidatedPremiums(contract) == null) {
                    // Рассчитываем и сохраняем риски
                    List<CatContractRisk> catContractRiskList = riskService.getRisksToCalcForContract(contract);
                    if (catContractRiskList != null) {
                        for (CatContractRisk catContractRisk : catContractRiskList) {
                            ContractPremium cp = new ContractPremium(catContractRisk.getRisk(), contract, contract.getSum());
                            cp.setValidated(true);
                            cp = premiumService.calculatePremium(cp, contract.getLength());
                        }
                    }
                }
            }


        }

        // Можно ли перевести договор в статус НОВЫЙ?
        // TODO пересмотреть условия, часть из них выше на исклчючениях отвалится
        if (contract.getPerson() != null &&
                premiumService.getValidatedPremiums(contract) != null &&
                contract.getEndDate() != null &&
                getRisksMustBeAddedToContract(contract).isEmpty() &&

                contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.BEGIN)
                ) {


            // Можно. Присваиваем номер и меняем статус
            contract.setCatContractStatus(catContractStatusService.getCatContractStatusByCode(CatContractStatus.StatusCode.NEW));


            // Генерим номер договора
            Query setNumberQuery = entityManager.createNativeQuery("SELECT set_contract_number(" + contract.getId() + ")");

            contract.setcNumberCounter(Integer.valueOf(setNumberQuery.getSingleResult().toString()));

            // Задаем курс обмена валюты
            if (
                    contract.getCurrency() != null && contract.getPrintDate() == null) {
                contract.setCurrencyExchange(exchangeCurrenciesService.getExchangeToCurrency(contract.getCurrency()));
            }

            // Не ставим флаг "печать заявления при открытии договора"
            //contract.setPrintClaimFlag(true);

        }

        // Поставили галочку "договор подписан"
        // если раньше claimSigned не был null, то сейчас он тем более не null
        if ((oldContract.getClaimSigned() == null || !oldContract.getClaimSigned()) && contract.getClaimSigned()) {
            // Дата начала договора должна быть не ранее сегодняшней.
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            if (contract.getStartDate().compareTo(today.getTime()) < 0)
                throw new BadContractDataException("wrong_start_date");
        }

        entityManager.merge(contract);

    }

    @Override
    public BigDecimal getIntegralInsuredSumForContract(Contract contract) {
        List<ContractPremiumAjax> validatedPremiums = premiumService.getValidatedPremiums(contract);
        BigDecimal insured = BigDecimal.ZERO;
        if (validatedPremiums != null) {
            for (ContractPremiumAjax contractPremium : validatedPremiums) {
                insured = insured.add(contractPremium.getInsuredSum());
            }
        }
        return insured;
    }

    @Override
    public BigDecimal getAllPremiumForContract(Contract contract) throws CalculatePremiumException {
        List<ContractPremiumAjax> validatedPremiums = premiumService.getValidatedPremiums(contract);
        BigDecimal premium = BigDecimal.ZERO;
        if (validatedPremiums != null) {
            for (ContractPremiumAjax contractPremium : validatedPremiums) {
                premium = premium.add(contractPremium.getPremium().setScale(0, RoundingMode.HALF_UP));
            }
        }
        return premium;
    }

    @Override
    public BigDecimal getFullCost(Contract contract) {
        throw new UnsupportedOperationException("calculating full cost not allowed for TP0");
    }

    @Override
    public BigDecimal getAllPremiumForContractWithoutSaving(Contract contract, List<InsuranceArea> insuranceAreas) throws CalculatePremiumException {
        return riskService.getSumForAllRisksWithoutSaving(contract);
    }

    @Override
    public BigDecimal getIntegralInsuredSumForContractWithoutSaving(Contract contract, List<InsuranceArea> insuranceAreas) {
        return getIntegralInsuredSumForContract(contract);
    }

    @Override
    public Integer getMaxAgeForMale() {
        return 80;
    }

    @Override
    public Integer getMaxAgeForFemale() {
        return 80;
    }

    @Override
    public List<Risk> getRisksSellerCanUse(Contract contract) throws CalculatePremiumException {
        throw new UnsupportedOperationException("Not realized yet");
    }

    @Override
    public List<Risk> getRiskSomeSellerCanUse(CatContract catContract, Partner partner) {
        throw new UnsupportedOperationException("Not realized yet");
    }

    @Override
    public void addPremiumToContract(Contract contract, ContractPremium contractPremium) throws CalculatePremiumException {
        throw new UnsupportedOperationException("Not realized yet");
    }

    @Override
    public List<Risk> getRisksMustBeAddedToContract(Contract contract) {
        List<Risk> toBeAdded = new LinkedList<Risk>();

        List<Risk> risksAlreadyAdded = premiumService.getRisksInValidatedPremiums(contract);

        List<CatContractRisk> catContractRiskList = riskService.getExistRisks(null, contract.getCatContract());

        for (CatContractRisk catContractRisk : catContractRiskList) {
            if (catContractRisk.getRisk().getParentRisk().getMandatory() != null &&
                    catContractRisk.getRisk().getParentRisk().getMandatory() &&
                    !toBeAdded.contains(catContractRisk.getRisk().getParentRisk()) &&
                    !risksAlreadyAdded.contains(catContractRisk.getRisk().getParentRisk())) {
                toBeAdded.add(catContractRisk.getRisk().getParentRisk());
            }
        }

        return toBeAdded;
    }

    @Override
    public String getPossibleValues(Risk risk) {
        return null;
    }

    @Override
    public Date printContract(Contract c) {
        if (c.getCurrency() != null && c.getPrintDate() == null) {
            c.setCurrencyExchange(exchangeCurrenciesService.getExchangeToCurrency(c.getCurrency()));
        }
        return contractService.doDefaultPrintContract(c);
    }
}
