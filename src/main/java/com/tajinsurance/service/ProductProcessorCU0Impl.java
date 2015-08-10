package com.tajinsurance.service;

import com.tajinsurance.domain.*;
import com.tajinsurance.dto.ContractPremiumAjax;
import com.tajinsurance.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by berz on 15.09.14.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProductProcessorCU0Impl implements ProductProcessorCU0 {
    public ProductProcessorCU0Impl() {
    }

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    ContractService contractService;

    @Autowired
    PremiumService premiumService;

    @Autowired
    RiskService riskService;

    @Autowired
    CatContractStatusService catContractStatusService;

    protected static ProductProcessorCU0Impl instance = null;

    public static ProductProcessorCU0Impl getInstance(){
        if(instance == null){
            instance = new ProductProcessorCU0Impl();
        }
        return (ProductProcessorCU0Impl) instance;
    }

    @Override
    public ProductProcessor getProductProcessorImplementation(Contract contract) {
        throw new UnsupportedOperationException("You cant use it to get processor. Use basic bean ProductProcessor");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = NoRelatedContractNumber.class)
    public void updateContract(Contract contract, MultipartFile file, ContractImage contractImage, Boolean managerMode) throws NoEntityException, CalculatePremiumException, NoPersonToContractException, BadContractDataException, NoRelatedContractNumber, IOException {

        if(file != null) throw new IllegalArgumentException("upload files not allowed for CU0!");

        Contract oldContract;


        oldContract = contractService.getContractById(contract.getId());
        contract.setVersion(oldContract.getVersion());


        // Проверяем, задан ли Страхователь
        if (contract.getPerson() == null) throw new NoPersonToContractException("person_not_filled");

        // Проверяем, задана ли сумма кредитного договора
        if( contract.getRelatedContractNumber() == null || contract.getRelatedContractNumber().equals("")) {
            throw new NoRelatedContractNumber("no_related_num");
        }


        if (contract.getStartDate() == null) throw new BadContractDataException("no_start_date");
        else {

            // Проверяем, будет ли Страхователь совершеннолетним на момент начала действия договора
            if (contract.getPerson().getAge(contract.getStartDate()) < 18)
                throw new BadContractDataException("age_less_18");


            if(contract.getLength() == null) throw new BadContractDataException("period_not_calc");
            else{
                // Определяем дату окончания договора
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(contract.getStartDate());
                // По ТЗ 1 месяц периода - ровно 30 дней
                calendar.add(Calendar.DATE, contract.getLength() * 30);
                contract.setEndDate(calendar.getTime());
            }

            // Проверяем возраст на момент окончания договора, 63 для мужчин, 58 для женщин
            if (contract.getPerson().getSex() == Person.Sex.MALE &&
                    contract.getPerson().getAge(contract.getEndDate()) > getMaxAgeForMale()
                    )
                throw new BadContractDataException("age_more_60");

            if (contract.getPerson().getSex() == Person.Sex.FEMALE &&
                    contract.getPerson().getAge(contract.getEndDate()) > getMaxAgeForFemale()
                    )
                throw new BadContractDataException("age_more_55");

        }


        if (contract.getLength() != null && contract.getSum() != null) {
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
            else if(contract.getClaimSigned() == null || !contract.getClaimSigned()){
                List<ContractPremiumAjax> contractPremiums = premiumService.getValidatedPremiums(contract);


                for(ContractPremiumAjax cpa : contractPremiums){
                    // Проходимся по рассчитанным рискам и делаем коррекцию
                    premiumService.premiumCorrection(cpa.getPremiumId(), contract );
                }

            }

        }


        // Можно ли перевести договор в статус НОВЫЙ?
        // TODO пересмотреть условия, часть из них выше на исклчючениях отвалится
        if (contract.getPerson() != null &&
                premiumService.getValidatedPremiums(contract) != null &&
                contract.getEndDate() != null &&


                contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.BEGIN) &&

                contract.getRelatedContractNumber() != null && !contract.getRelatedContractNumber().equals("")
                ) {


            // Можно. Присваиваем номер и меняем статус
            contract.setCatContractStatus(catContractStatusService.getCatContractStatusByCode(CatContractStatus.StatusCode.NEW));


            // Генерим номер договора
            Query setNumberQuery = entityManager.createNativeQuery("SELECT set_contract_number(" + contract.getId() + ")");

            contract.setcNumberCounter(Integer.valueOf(setNumberQuery.getSingleResult().toString()));

            // Ставим флаг "печать заявления при открытии договора"
            contract.setPrintClaimFlag(true);

        } else {

            if (
                    !contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.NEW) &&
                            !contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.ACCEPTED) &&
                            !contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.CANCELLED)
                    ){
                throw new BadContractDataException("wrong_data");
            }


        }

        // Поставили галочку "договор подписан"
        // если раньше claimSigned не был null, то сейчас он тем более не null
        if( (oldContract.getClaimSigned() == null || !oldContract.getClaimSigned()) && contract.getClaimSigned()){
            // Дата начала договора должна быть не ранее сегодняшней.
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            if(contract.getStartDate().compareTo(today.getTime()) < 0 ) throw new BadContractDataException("wrong_start_date");
        }

        entityManager.merge(contract);
    }

    @Override
    public void updateContract(Contract contract, Boolean managerMode) throws NoEntityException, CalculatePremiumException, NoPersonToContractException, BadContractDataException, NoRelatedContractNumber, IOException {
        updateContract(contract, null, null, managerMode);
    }

    @Override
    public BigDecimal getIntegralInsuredSumForContract(Contract contract) {
        return  riskService.getMajorInsuredSumForAllRisks(contract, contract.getSum());
    }

    @Override
    public BigDecimal getAllPremiumForContract(Contract contract) throws CalculatePremiumException {
        return riskService.getSumForAllRisksWithoutSaving(contract, contract.getSum(), contract.getLength());
    }

    @Override
    public BigDecimal getFullCost(Contract contract) {
        throw new UnsupportedOperationException("calculating full cost not allowed for CU0");
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
        return 63;
    }

    @Override
    public Integer getMaxAgeForFemale() {
        return 58;
    }

    @Override
    public List<Risk> getRisksSellerCanUse(Contract contract) {
        throw new UnsupportedOperationException("only for TP0!");
    }

    @Override
    public List<Risk> getRiskSomeSellerCanUse(CatContract catContract, Partner partner) {
        throw new UnsupportedOperationException("only for TP0!");
    }

    @Override
    public void addPremiumToContract(Contract contract, ContractPremium contractPremium) {
        throw new UnsupportedOperationException("only for TP0!");
    }

    @Override
    public List<Risk> getRisksMustBeAddedToContract(Contract contract) {
        throw new UnsupportedOperationException("only for TP0!");
    }

    @Override
    public String getPossibleValues(Risk risk) {
        throw new UnsupportedOperationException("only for TP0!");
    }

    @Override
    public Date printContract(Contract c) {
        return contractService.doDefaultPrintContract(c);
    }
}
