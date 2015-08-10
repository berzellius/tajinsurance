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
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by berz on 15.09.14.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProductProcessorBA0Impl implements ProductProcessorBA0 {

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

    protected static ProductProcessorBA0Impl instance = null;

    public ProductProcessorBA0Impl() {
    }

    public static ProductProcessorBA0Impl getInstance(){
        if(instance == null){
            instance = new ProductProcessorBA0Impl();
        }
        return (ProductProcessorBA0Impl) instance;
    }

    @Override
    public ProductProcessor getProductProcessorImplementation(Contract contract) {
        throw new UnsupportedOperationException("allowed only for base ProductProcessorImpl!");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = NoRelatedContractNumber.class)
    public void updateContract(Contract contract, MultipartFile file, ContractImage contractImage, Boolean managerMode) throws NoEntityException, CalculatePremiumException, NoPersonToContractException, BadContractDataException, NoRelatedContractNumber, IOException {
        if(file != null) throw new IllegalArgumentException("upload files not allowed for BA0");

        Contract oldContract = contractService.getContractById(contract.getId());
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

            // Проверяем возраст на момент окончания договора, 70 для мужчин, 70 для женщин
            if (contract.getPerson().getSex() == Person.Sex.MALE &&
                    contract.getPerson().getAge(contract.getEndDate()) > getMaxAgeForMale()
                    )
                throw new BadContractDataException("max_age_limit");

            if (contract.getPerson().getSex() == Person.Sex.FEMALE &&
                    contract.getPerson().getAge(contract.getEndDate()) > getMaxAgeForFemale()
                    )
                throw new BadContractDataException("max_age_limit");

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
                List<ContractPremium> contractPremiums = premiumService.getValidatedCPremiums(contract);

                List<Risk> calculatedRisks = new LinkedList<Risk>();

                for(ContractPremium cp : contractPremiums){
                    // Проходимся по рассчитанным рискам и делаем коррекцию, если риск в списке необходимых для рассчета,
                    // либо удаляем премию, если риска нет в списке
                    CatContractRisk catContractRisk = riskService.getRisk(cp.getRisk(), contract.getCatContract(), contract.getPartner());

                    if( riskService.getRisksToCalcForContract(contract).contains(catContractRisk))
                        premiumService.premiumCorrection(cp.getId(), contract );
                    else premiumService.deletePremium(cp.getId());

                    calculatedRisks.add(cp.getRisk());
                }


                // Проходимся по рискам, которые должны быть рассчитаны
                for(CatContractRisk catContractRisk : riskService.getRisksToCalcForContract(contract)){
                    if(!calculatedRisks.contains(catContractRisk.getRisk())){
                        // Риск не рассчитан
                        ContractPremium cp = new ContractPremium(catContractRisk.getRisk(), contract, contract.getSum());
                        cp.setValidated(true);
                        cp = premiumService.calculatePremium(cp, contract.getLength());
                    }
                }

            }

        }


        // Можно ли перевести договор в статус НОВЫЙ?
        // TODO пересмотреть условия, часть из них выше на исключениях отвалится
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
        return riskService.getMajorInsuredSumForAllRisks(contract, contract.getSum());
    }

    @Override
    public BigDecimal getAllPremiumForContract(Contract contract) throws CalculatePremiumException {
        //return riskService.getSumForAllRisksWithoutSaving(contract, contract.getSum(), contract.getLength());
        List<ContractPremiumAjax> validatedPremiums = premiumService.getValidatedPremiums(contract);
        if (validatedPremiums != null) {
            BigDecimal premium = BigDecimal.ZERO;
            for (ContractPremiumAjax contractPremium : validatedPremiums) {
                premium = premium.add(contractPremium.getPremium());
            }

            return premium.setScale(0, RoundingMode.HALF_UP);
        }
        else return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getFullCost(Contract contract) {
        throw new UnsupportedOperationException("calculating full cost not allowed for BA0");
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
        return 70;
    }

    @Override
    public Integer getMaxAgeForFemale() {
        return 70;
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
