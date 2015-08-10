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
import java.util.LinkedList;
import java.util.List;

/**
 * Created by berz on 19.01.15.
 */
@Service
@Transactional
public class ProductProcessorTP2Impl implements  ProductProcessorTP2 {
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

    @Autowired
    PartnerService partnerService;

    @Autowired
    ExchangeCurrenciesService exchangeCurrenciesService;


    public ProductProcessorTP2Impl() {
    }

    @Override
    public ProductProcessor getProductProcessorImplementation(Contract contract) {
        throw new UnsupportedOperationException("allowed only for base ProductProcessorImpl!");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = NoRelatedContractNumber.class)
    public void updateContract(Contract contract, MultipartFile file, ContractImage contractImage, Boolean managerMode) throws NoEntityException, CalculatePremiumException, NoPersonToContractException, BadContractDataException, NoRelatedContractNumber, IOException, IllegalDataException {
        if (file != null) throw new IllegalArgumentException("upload files not allowed for TP2!");

        Contract oldContract;


        oldContract = contractService.getContractById(contract.getId());
        contract.setVersion(oldContract.getVersion());


        // Проверяем, задан ли Страхователь
        if (contract.getPerson() == null) throw new NoPersonToContractException("person_not_filled");

        /*
        if( contract.getRelatedContractNumber() == null || contract.getRelatedContractNumber().equals("")) {
            throw new NoRelatedContractNumber("no_related_num");
        }*/

        // Дата начала страхования = дата вступления договора в силу = не ранее чем дата оплаты + 5 календарных дней
        //if (contract.getStartDate() == null) throw new BadContractDataException("no_start_date");


        if (contract.getStartDate() != null) {

            //Миимальный возраст для TP2 стандартный
            if (contract.getPerson().getAge(contract.getStartDate()) < 18)
                throw new BadContractDataException("age_less_18");



            // Должна быть указана либо длительность договора, либо дата окончания
            if (contract.getLength() == null)
                throw new BadContractDataException("period_not_calc");
            else {

                    // Определяем дату окончания договора
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(contract.getStartDate());
                    calendar.add(Calendar.MONTH, contract.getLength());
                    contract.setEndDate(calendar.getTime());
            }

            // Проверяем возраст на момент окончания договора, для мужчин и для женщин
            if (contract.getPerson().getSex() == Person.Sex.MALE &&
                    contract.getPerson().getAge(contract.getEndDate()) > getMaxAgeForMale()
                    )
                throw new BadContractDataException("max_age_limit");

            if (contract.getPerson().getSex() == Person.Sex.FEMALE &&
                    contract.getPerson().getAge(contract.getEndDate()) > getMaxAgeForFemale()
                    )
                throw new BadContractDataException("max_age_limit");

        }

        Risk risk = getRiskForThisContract(contract);

        if (contract.getLength() != null && risk != null) {


            if (contract.getCurrency() != null && contract.getPrintDate() == null) {
                contract.setCurrencyExchange(exchangeCurrenciesService.getExchangeToCurrency(contract.getCurrency()));
            }

            List<CatContractRisk> catContractRiskList = riskService.getExistRisks(null, contract.getCatContract());

            CatContractRisk catContractRiskForContract = null;

            if(!catContractRiskList.isEmpty()){
                for(CatContractRisk catContractRisk : catContractRiskList){
                    if(catContractRisk.getLength().equals(contract.getLength()) && catContractRisk.getRisk().equals(risk))
                        catContractRiskForContract = catContractRisk;
                }
            }

            if(catContractRiskForContract == null) throw new BadContractDataException("no_conditions");

            ProductMoneyProperty productMoneyProperty = partnerService.getProductMoneyPropertyByPropertyName(ProductMoneyProperty.PropertyType.fixed_insurance_sum, contract.getCatContract());

            if(
                    productMoneyProperty == null ||
                            productMoneyProperty.getMoneyValue() == null
                    )
                throw new BadContractDataException("no_conditions");
            else{
                contract.setSum(productMoneyProperty.getMoneyValue());
            }

            BigDecimal q = BigDecimal.ONE;


            if (premiumService.getValidatedPremiums(contract) == null) {
                // Рассчитываем и сохраняем риски
                ContractPremium cp = new ContractPremium(risk, contract, contract.getSum());
                cp.setValidated(true);
                cp = premiumService.calculatePremium(cp, contract.getLength(), q, catContractRiskForContract);

            } else if (contract.getClaimSigned() == null || !contract.getClaimSigned()) {
                List<ContractPremium> contractPremiums = premiumService.getValidatedCPremiums(contract);


                for (ContractPremium cp : contractPremiums) {
                    // Проходимся по рассчитанным рискам и делаем коррекцию.
                    // Если риск не соответствует выбранному, удаляем премию

                    if(cp.getRisk().equals(risk)){
                        premiumService.premiumCorrection(cp.getId(), contract, q, catContractRiskForContract);
                    }
                    else{
                        premiumService.deletePremium(cp.getId());
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



            // Не ставим флаг "печать заявления при открытии договора"
            //contract.setPrintClaimFlag(true);

        } /*else {

            if (
                    !contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.NEW) &&
                            !contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.ACCEPTED) &&
                            !contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.CANCELLED)
                    ) {
                throw new BadContractDataException("wrong_data");
            }


        }*/


        if (
               /* (oldContract.getClaimSigned() == null || !oldContract.getClaimSigned()) && contract.getClaimSigned() &&*/
                        contract.getPayDate() != null && contract.getStartDate() != null) {
            // Дата начала договора должна быть не ранее чем дата оплаты + 5 дней.
            Calendar startDateMin = Calendar.getInstance();
            startDateMin.setTime(contract.getPayDate());
            startDateMin.add(Calendar.DATE, 5);

            if (contract.getStartDate().compareTo(startDateMin.getTime()) < 0)
                throw new BadContractDataException("wrong_start_date_tp2");

            Calendar endDate = Calendar.getInstance();
            endDate.setTime(contract.getStartDate());
            endDate.add(Calendar.MONTH, contract.getLength());
            endDate.add(Calendar.DATE, -1);
            contract.setEndDate(endDate.getTime());
        }

        if(contract.getStartDate() != null && contract.getPayDate() == null){
                throw new BadContractDataException("wrong_start_date_tp2");
        }

        entityManager.merge(contract);
    }


    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Risk getRiskForThisContract(Contract contract) {

        if(contract.getTp2Extended() != null && contract.getTp2Extended()){
            return riskService.getRiskByDet("EXT");
        }
        else return riskService.getRiskByDet("STD");

    }


    @Override
    public void updateContract(Contract contract, Boolean managerMode) throws NoEntityException, CalculatePremiumException, NoPersonToContractException, BadContractDataException, NoRelatedContractNumber, IOException, IllegalDataException {
        updateContract(contract, null, null, managerMode);
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
                premium = premium.add(contractPremium.getPremium());
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
        return 65;
    }

    @Override
    public Integer getMaxAgeForFemale() {
        return 65;
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
        List<Risk> risks = new LinkedList<Risk>();
        Risk risk = getRiskForThisContract(contract);
        if(contract.getContractPremiums() != null && !contract.getContractPremiums().contains(risk)){
            risks.add(risk);
        }
        return risks;
    }

    @Override
    public String getPossibleValues(Risk risk) {
        throw new UnsupportedOperationException("not needed for TP2");
    }

    @Override
    public Date printContract(Contract c) {
        if (c.getCurrency() != null && c.getPrintDate() == null) {
            c.setCurrencyExchange(exchangeCurrenciesService.getExchangeToCurrency(c.getCurrency()));
        }
        return contractService.doDefaultPrintContract(c);
    }

    @Override
    public List<Risk> getRisksMayBeUsedForAddingProductSettings(CatContract catContract) {
        Query q = entityManager.createNativeQuery(
                "select r.* from " +
                        "risk r, " +
                        "(select unnest(ARRAY[3,6,9,12]) as l) lr " +
                        "where " +
                        "exists(" +
                        "  select * from cat_contract_to_risk where risk_id = r.id and cc_id = :cc" +
                        "  ) " +
                        "and " +
                        "not exists (" +
                        "select * from cat_contract_risk where risk_id = r.id and cat_contract_id = :cc and length = l" +
                        ")" +
                        "group by r.id",
                Risk.class
        );
        q.setParameter("cc", catContract.getId());

        return q.getResultList();
    }

    @Override
    public List<Integer> getLengthValuesToAddProductSettings(CatContract catContract, Risk risk) {
        Query q = entityManager.createNativeQuery(
            "select l from " +
                    "risk r, " +
                    " (select unnest(ARRAY[3,6,9,12]) as l) lr " +
                    "where " +
                    " exists(" +
                    "  select * from cat_contract_to_risk where risk_id = r.id and cc_id = :cc" +
                    "  ) " +
                    "and " +
                    " not exists (" +
                    "  select * from cat_contract_risk where risk_id = r.id and cat_contract_id = :cc and length = l" +
                    " ) " +
                    "and r.id = :r"
        );
        q.setParameter("cc", catContract.getId());
        q.setParameter("r", risk.getId());
        List<Integer> res = (List<Integer>) (q.getResultList());
        return res;
    }
}
