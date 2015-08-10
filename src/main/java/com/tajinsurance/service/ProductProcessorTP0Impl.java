package com.tajinsurance.service;

import com.tajinsurance.domain.*;
import com.tajinsurance.dto.ContractPremiumAjax;
import com.tajinsurance.exceptions.*;
import org.joda.time.DateTime;
import org.joda.time.Days;
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
import java.util.*;

/**
 * Created by berz on 30.11.14.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProductProcessorTP0Impl implements ProductProcessorTP0 {
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

    @Autowired
    CatContractFixedSumService catContractFixedSumService;


    public ProductProcessorTP0Impl() {
    }

    @Override
    public ProductProcessor getProductProcessorImplementation(Contract contract) {
        throw new UnsupportedOperationException("allowed only for base ProductProcessorImpl!");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = NoRelatedContractNumber.class)
    public void updateContract(Contract contract, MultipartFile file, ContractImage contractImage, Boolean managerMode) throws NoEntityException, CalculatePremiumException, NoPersonToContractException, BadContractDataException, NoRelatedContractNumber, IOException {

        if (file != null) throw new IllegalArgumentException("upload files not allowed for TP0!");

        Contract oldContract;


        oldContract = contractService.getContractById(contract.getId());
        contract.setVersion(oldContract.getVersion());


        // Проверяем, задан ли Страхователь
        if (contract.getPerson() == null) throw new NoPersonToContractException("person_not_filled");

        /*
        if( contract.getRelatedContractNumber() == null || contract.getRelatedContractNumber().equals("")) {
            throw new NoRelatedContractNumber("no_related_num");
        }*/


        if (contract.getStartDate() == null) throw new BadContractDataException("no_start_date");
        else {

            /* Миимальный возраст для TP0 не задан
            if (contract.getPerson().getAge(contract.getStartDate()) < 18)
                throw new BadContractDataException("age_less_18");
            */


            // Должна быть указана либо длительность договора, либо дата окончания
            if (contract.getLength() == null && contract.getEndDate() == null)
                throw new BadContractDataException("period_not_calc");
            else {
                if (contract.getLength() != null) {
                    // Определяем дату окончания договора
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(contract.getStartDate());
                    calendar.add(Calendar.DATE, contract.getLength() - 1);
                    contract.setEndDate(calendar.getTime());
                } else if (contract.getEndDate() != null) {
                    if (
                            contract.getEndDate().after(contract.getStartDate()) ||
                                    contract.getEndDate().equals(contract.getStartDate())
                            ) {
                        DateTime d1 = new DateTime(contract.getStartDate());
                        DateTime d2 = new DateTime(contract.getEndDate());

                        /*if(Days.daysBetween(d1, d2).getDays() < 1)
                            throw new BadContractDataException("end_before_start");*/

                        contract.setLength(Days.daysBetween(d1, d2).getDays() + 1);
                    } else {
                        throw new BadContractDataException("end_before_start");
                    }
                }
            }

            // Проверяем возраст на момент окончания договора, 63 для мужчин, 58 для женщин
            if (contract.getPerson().getSex() == Person.Sex.MALE &&
                    contract.getPerson().getAge(contract.getEndDate()) > getMaxAgeForMale()
                    )
                throw new BadContractDataException("max_age_limit");

            if (contract.getPerson().getSex() == Person.Sex.FEMALE &&
                    contract.getPerson().getAge(contract.getEndDate()) > getMaxAgeForFemale()
                    )
                throw new BadContractDataException("max_age_limit");

        }


        if (contract.getLength() != null) {

            if (contract.getLength() > getMaxLength()) throw new BadContractDataException("max_length_limit");

            if (premiumService.getValidatedPremiums(contract) == null) {
                // Рассчитываем и сохраняем риски
                /*List<CatContractRisk> catContractRiskList = riskService.getRisksToCalcForContract(contract);
                if (catContractRiskList != null) {
                    for (CatContractRisk catContractRisk : catContractRiskList) {
                        ContractPremium cp = new ContractPremium(catContractRisk.getRisk(), contract, contract.getSum());
                        cp.setValidated(true);
                        cp = premiumService.calculatePremium(cp, contract.getLength());
                    }
                }*/

            } else if (contract.getClaimSigned() == null || !contract.getClaimSigned()) {
                List<ContractPremium> contractPremiums = premiumService.getValidatedCPremiums(contract);


                for (ContractPremium cp : contractPremiums) {
                    // Проходимся по рассчитанным рискам и делаем коррекцию
                    BigDecimal q = BigDecimal.ONE;
                    q = this.calculateMult(cp);
                    //premiumService.premiumCorrection(cp.getId(), contract, q);
                    this.premiumCorrection(cp.getId(), contract, q);
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

        } /*else {

            if (
                    !contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.NEW) &&
                            !contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.ACCEPTED) &&
                            !contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.CANCELLED)
                    ) {
                throw new BadContractDataException("wrong_data");
            }


        }*/

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
    public Integer getMaxLength() {
        return 366;
    }

    @Override
    public void updateContract(Contract contract, Boolean managerMode) throws NoEntityException, CalculatePremiumException, NoPersonToContractException, BadContractDataException, NoRelatedContractNumber, IOException {
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
        if (contract.getLength() == null) {
            throw new CalculatePremiumException("not set length", CalculatePremiumException.Reason.NO_LENGTH);
        }

        List<Risk> risks = new LinkedList<Risk>();

        List<Risk> existsRisks = premiumService.getRisksInValidatedPremiums(contract);

        List<CatContractRisk> fromGlobalSettings = riskService.getExistRisks(null, contract.getCatContract());

        for (CatContractRisk catContractRisk : fromGlobalSettings) {

            if (
                    (catContractRisk.getMandatory() != null && catContractRisk.getMandatory()) &&
                            !existsRisks.contains(catContractRisk.getRisk()) &&
                            !sameParentExists(existsRisks, catContractRisk.getRisk())
                    )

                risks.add(catContractRisk.getRisk());
        }

        List<PartnerRiskPolicy> partnerRiskPolicyList = partnerService.getExistsPolicy(contract.getCatContract(), contract.getPartner());

        for (PartnerRiskPolicy partnerRiskPolicy : partnerRiskPolicyList) {

            for (Risk childRisk : partnerRiskPolicy.getRisk().getChildRisks()) {

                if (!risks.contains(childRisk) && !existsRisks.contains(childRisk) && !sameParentExists(existsRisks, childRisk))
                    risks.add(childRisk);

                //System.out.println(partnerRiskPolicy.getRisk() + " :: " + childRisk.getCode());

                CatContractRisk catContractRiskForPolicy = riskService.getRisk(childRisk, contract.getCatContract(), contract.getPartner());

                if (catContractRiskForPolicy == null)
                    throw new CalculatePremiumException(
                            "absent some tarifs that need to clc premiums",
                            CalculatePremiumException.Reason.NO_TARIF
                    );
            }
        }

        return risks;
    }

    private boolean sameParentExists(List<Risk> risks, Risk risk) {
        for (Risk crisk : risks) {
            if (crisk.getParentRisk().equals(risk.getParentRisk())) return true;
        }
        return false;
    }

    @Override
    public List<Risk> getRiskSomeSellerCanUse(CatContract catContract, Partner partner) {
        List<Risk> risks = new LinkedList<Risk>();

        List<CatContractRisk> fromGlobalSettings = riskService.getExistRisks(null, catContract);

        for (CatContractRisk catContractRisk : fromGlobalSettings) {

            if (
                    (catContractRisk.getMandatory() != null && catContractRisk.getMandatory())
                    )

                risks.add(catContractRisk.getRisk());
        }

        List<PartnerRiskPolicy> partnerRiskPolicyList = partnerService.getExistsPolicy(catContract, partner);

        for (PartnerRiskPolicy partnerRiskPolicy : partnerRiskPolicyList) {
            for (Risk risk : partnerRiskPolicy.getRisk().getChildRisks())
                if (!risks.contains(risk))
                    risks.add(risk);
        }

        return risks;
    }

    @Override
    public void addPremiumToContract(Contract contract, ContractPremium contractPremium) throws CalculatePremiumException {
        List<Risk> allowedRiskList = getRisksSellerCanUse(contract);

        if (allowedRiskList.contains(contractPremium.getRisk())) {
            List<PartnerRiskPolicy> partnerRiskPolicyList = partnerService.getExistsPolicy(contract.getCatContract(), contract.getPartner());

            for (PartnerRiskPolicy partnerRiskPolicy : partnerRiskPolicyList) {
                if (partnerRiskPolicy.getRisk().equals(contractPremium.getRisk())) {
                    if (
                            partnerRiskPolicy.getFixedInsuredSum() != null &&
                                    partnerRiskPolicy.getFixedInsuredSum().compareTo(BigDecimal.ZERO) > 0
                            ) {
                        contractPremium.setInsuredSum(partnerRiskPolicy.getFixedInsuredSum());
                    }
                }
            }

            BigDecimal q = BigDecimal.ONE;

            q = this.calculateMult(contractPremium);

            premiumService.calculatePremium(contractPremium, contract.getLength(), q);
        }
    }

    @Override
    public List<Risk> getRisksMustBeAddedToContract(Contract contract) {
        List<Risk> toBeAdded = new LinkedList<Risk>();

        List<Risk> risksAlreadyAdded = getParentRisks(premiumService.getRisksInValidatedPremiums(contract));

        List<CatContractRisk> catContractRiskList = riskService.getExistRisks(null, contract.getCatContract());

        for (CatContractRisk catContractRisk : catContractRiskList) {
            if (catContractRisk.getRisk().getParentRisk().getMandatory() != null &&
                    catContractRisk.getRisk().getParentRisk().getMandatory() &&
                    !toBeAdded.contains(catContractRisk.getRisk().getParentRisk()) &&
                    !risksAlreadyAdded.contains(catContractRisk.getRisk().getParentRisk())) {
                toBeAdded.add(catContractRisk.getRisk().getParentRisk());
            }
        }

        List<PartnerRiskPolicy> partnerRiskPolicyList = partnerService.getExistsPolicy(contract.getCatContract(), contract.getPartner());

        for (PartnerRiskPolicy partnerRiskPolicy : partnerRiskPolicyList) {
            if (
                    !risksAlreadyAdded.contains(partnerRiskPolicy.getRisk()) &&
                            !toBeAdded.contains(partnerRiskPolicy.getRisk()) &&
                            partnerRiskPolicy.getPolicy().equals(PartnerRiskPolicy.Policy.FIXED)
                    ) {
                toBeAdded.add(partnerRiskPolicy.getRisk());
            }
        }

        return toBeAdded;
    }

    @Override
    public String getPossibleValues(Risk risk) {
        String str = "";

        List<CatContractFixedSum> catContractFixedSums = catContractFixedSumService.getCatContractFixedSumsForRisk(risk.getParentRisk());

        for(CatContractFixedSum catContractFixedSum : catContractFixedSums){
            System.out.println(catContractFixedSum.getSum());
            String s = catContractFixedSum.getSum().setScale(0, RoundingMode.HALF_UP).toString();
            str = str.concat((str.isEmpty())? s : new String(",").concat(s));
        }
        System.out.println(risk.getDet() + " :: " + str);
        return str;
    }

    private List<Risk> getParentRisks(List<Risk> risksInValidatedPremiums) {
        List<Risk> pRisks = new LinkedList<Risk>();
        for (Risk risk : risksInValidatedPremiums) {
            if (!pRisks.contains(risk.getParentRisk())) {
                pRisks.add(risk.getParentRisk());
            }
        }

        return pRisks;
    }

    private List<Risk> getRisksSameDetMandatory(List<CatContractRisk> catContractRiskList, CatContractRisk catContractRisk) {
        List<Risk> risks = new LinkedList<Risk>();
        for (CatContractRisk ccr : catContractRiskList) {
            if (
                    !ccr.equals(catContractRisk) &&
                            ccr.getRisk().getDet().equals(catContractRisk.getRisk().getDet()) &&
                            ccr.getMandatory() != null &&
                            ccr.getMandatory()
                    )
                risks.add(ccr.getRisk());
        }

        return risks;
    }

    @Override
    public Date printContract(Contract c) {
        if (c.getCurrency() != null && c.getPrintDate() == null) {
            c.setCurrencyExchange(exchangeCurrenciesService.getExchangeToCurrency(c.getCurrency()));
        }
        return contractService.doDefaultPrintContract(c);
    }

    @Override
    public BigDecimal calculateMult(ContractPremium contractPremium) {
        BigDecimal q = BigDecimal.ONE;

        assert contractPremium.getContract() != null;
        assert contractPremium.getContract().getPerson() != null;
        assert contractPremium.getContract().getLength() != null;
        assert contractPremium.getContract().getInsuranceTerritory() != null;

        q = q
                .multiply(
                        this.allowedAgeMult(contractPremium.getRisk()) ?
                                this.getMultByAge(contractPremium.getContract().getPerson()) : BigDecimal.ONE
                )
                .multiply(
                        this.allowedPeriodMult(contractPremium.getRisk()) ?
                                this.getMultByPeriod(contractPremium.getContract()) : BigDecimal.ONE
                )
                .multiply(
                        this.allowedTerritoryMult(contractPremium.getRisk()) ?
                                this.getMultByTerritory(contractPremium.getContract()) : BigDecimal.ONE
                )
                .multiply(
                        this.allowedActivityMult(contractPremium.getRisk()) ?
                                this.getMultByActivity(contractPremium.getContract()) : BigDecimal.ONE
                )
                .multiply(
                        this.allowedFranchiseMult(contractPremium.getRisk()) ?
                                this.getMultByFranchise(contractPremium.getContract(), contractPremium.getRisk()) : BigDecimal.ONE
                )
        ;


        return q;
    }

    @Override
    public BigDecimal getMultByAge(Person person) {

        return getMultByAge(person.getAge());
    }

    @Override
    public BigDecimal getMultByAge(Integer age) {
        BigDecimal q = BigDecimal.ONE;

        if (age >= 4 && age < 70) return q;

        if (age < 1) q = q.multiply(BigDecimal.valueOf(3.0));

        if (age >= 1 && age < 4) q = q.multiply(BigDecimal.valueOf(1.5));

        if (age >= 70 && age < 75) q = q.multiply(BigDecimal.valueOf(2.0));

        if (age >= 75) q = q.multiply(BigDecimal.valueOf(4.0));

        return q;
    }

    @Override
    public HashMap<Risk, BigDecimal> getFixedSumsForRisks(List<Risk> risksToCalc, CatContract catContract, Partner partner) {
        HashMap<Risk, BigDecimal> hm = new LinkedHashMap<Risk, BigDecimal>();

        List<PartnerRiskPolicy> policyList = partnerService.getExistsPolicy(catContract, partner);

        for (PartnerRiskPolicy partnerRiskPolicy : policyList) {
            if (partnerRiskPolicy.getFixedInsuredSum() != null) {
                hm.put(partnerRiskPolicy.getRisk(), partnerRiskPolicy.getFixedInsuredSum());
            }
        }

        return hm;
    }

    @Override
    public HashMap<Risk, String> getAddDataForRiskSumFields(List<Risk> risksToCalc, CatContract catContract) {
        HashMap<Risk, String> hm = new LinkedHashMap<Risk, String>();

        List<CatContractFixedSum> catContractFixedSums = catContractFixedSumService.getCatContractFixedSumsForCatContract(catContract);

        for(CatContractFixedSum catContractFixedSum : catContractFixedSums){
            String sum = catContractFixedSum.getSum().setScale(0, RoundingMode.HALF_UP).toString();

            if(!hm.containsKey(catContractFixedSum.getRisk())){
                hm.put(catContractFixedSum.getRisk(), sum);
            }
            else{
                String s = hm.get(catContractFixedSum.getRisk());
                hm.remove(catContractFixedSum.getRisk());
                hm.put(catContractFixedSum.getRisk(), s.concat(",").concat(sum));
            }
        }

        System.out.println(hm);

        return hm;
    }

    @Override
    public BigDecimal getMultByPeriod(Contract contract) {
        BigDecimal q = BigDecimal.ONE;

        if (contract.getLength() >= 6 && contract.getLength() <= 7) q = q.multiply(BigDecimal.valueOf(0.95));

        if (contract.getLength() >= 8 && contract.getLength() <= 10) q = q.multiply(BigDecimal.valueOf(0.8));

        if (contract.getLength() >= 11 && contract.getLength() <= 15) q = q.multiply(BigDecimal.valueOf(0.75));

        if (contract.getLength() >= 16 && contract.getLength() <= 30) q = q.multiply(BigDecimal.valueOf(0.7));

        if (contract.getLength() >= 31 && contract.getLength() <= 92) q = q.multiply(BigDecimal.valueOf(0.5));

        if (contract.getLength() >= 93 && contract.getLength() <= 150) q = q.multiply(BigDecimal.valueOf(0.4));

        if (contract.getLength() >= 151 && contract.getLength() <= 184) q = q.multiply(BigDecimal.valueOf(0.35));

        if (contract.getLength() >= 185) q = q.multiply(BigDecimal.valueOf(0.3));

        return q;
    }

    @Override
    public BigDecimal getMultByTerritory(Contract contract) {
        BigDecimal q = BigDecimal.ONE;

        if (contract.getInsuranceTerritory().equals(Contract.InsuranceTerritory.TERRITORY2))
            q = q.multiply(BigDecimal.valueOf(1.6));

        return q;
    }

    @Override
    public BigDecimal getMultByActivity(Contract contract) {
        BigDecimal q = BigDecimal.ONE;

        if (contract.getSport() != null && contract.getSport()) q = q.multiply(BigDecimal.valueOf(3.0));

        return q;
    }

    @Override
    public BigDecimal getMultByFranchise(Contract contract, Risk risk) {
        BigDecimal q = BigDecimal.ONE;


        List<CatContractPartnerFranchise> franchises = partnerService.getFranchiseValuesAllowed(contract.getCatContract(), null);


        if (risk.getParentRisk().getDet().equals("D")) {

            ProductMoneyProperty franchise = partnerService.getProductMoneyPropertyByPropertyName(
                    ProductMoneyProperty.PropertyType.TP0_D_franchise_sum,
                    contract.getCatContract()
            );

            ProductMoneyProperty discount = partnerService.getProductMoneyPropertyByPropertyName(
                    ProductMoneyProperty.PropertyType.TP0_D_franchise_discount_percent,
                    contract.getCatContract()
            );

            if (
                    franchise != null && discount != null && franchise.getMoneyValue() != null && discount.getMoneyValue() != null
                            && discount.getMoneyValue().compareTo(BigDecimal.valueOf(100)) < 0
                    ) {
                q = q.multiply(BigDecimal.ONE.add(
                        discount.getMoneyValue().divide(BigDecimal.valueOf(100)).negate()
                ));
            }
        } else {
            if (contract.getFranchise() != null) {
                for (CatContractPartnerFranchise catContractFranchise : franchises) {

                    if (catContractFranchise.getFranchiseMoney().setScale(0, RoundingMode.HALF_UP).equals(BigDecimal.valueOf(contract.getFranchise()))) {
                        q = q.multiply(
                                BigDecimal.ONE.add(
                                        catContractFranchise.getDiscount()
                                                .negate()
                                )
                        );
                    }
                }
            }
        }

        return q;
    }

    @Override
    public boolean allowedAgeMult(Risk risk) {

        if (
                risk.getParentRisk().getDet().equals("A") ||
                        risk.getParentRisk().getDet().equals("E")
                )
            return true;

        return false;
    }

    @Override
    public boolean allowedPeriodMult(Risk risk) {

        if (
                risk.getParentRisk().getDet().equals("C") || risk.getParentRisk().getDet().equals("B")
                )
            return false;

        return true;
    }

    @Override
    public boolean allowedTerritoryMult(Risk risk) {

        if (
                risk.getParentRisk().getDet().equals("A") ||
                        risk.getParentRisk().getDet().equals("D")
                )
            return true;

        return false;
    }

    @Override
    public boolean allowedActivityMult(Risk risk) {

        if (
                risk.getParentRisk().getDet().equals("A") ||
                        risk.getParentRisk().getDet().equals("E")
                )
            return true;

        return false;
    }

    @Override
    public boolean allowedFranchiseMult(Risk risk) {

        if (
                risk.getParentRisk().getDet().equals("A") || risk.getParentRisk().getDet().equals("D")
                )
            return true;

        return false;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void premiumCorrection(Long id, Contract contract, BigDecimal q) throws NoEntityException, CalculatePremiumException {
        ContractPremium cpExists = premiumService.getPremiumById(id);
        BigDecimal properPremium;


        properPremium = premiumService.calculatePremiumWOSaving(
                new ContractPremium(cpExists.getRisk(), contract, cpExists.getInsuredSum()),
                contract.getLength(), q
        );

        //System.out.println(cpExists.getRisk().toString().concat(" ").concat(properPremium.toString()));

        // Должна ли премия по данному риску быть пересчитана?
        if (!cpExists.getPremium().equals(properPremium)) {

            // Удаляем премию
            entityManager.remove(cpExists);


            ContractPremium cp = new ContractPremium(cpExists.getRisk(), contract, cpExists.getInsuredSum());

            cp.setValidated(true);

            this.addPremiumToContract(contract, cp);
        }
    }

    @Override
    public BigDecimal getInsuredSumFromRisk(Risk risk, CatContract cc, BigDecimal bigDecimal) {
        CatContractRisk catContractRisk = riskService.getRisk(risk, cc, null);

        if (catContractRisk == null) return BigDecimal.ZERO;

        return catContractRisk.getRate().multiply(bigDecimal);

    }

    @Override
    public BigDecimal getPremiumWOSavingWithAge(Contract contract, CatContract cc, Risk risk, BigDecimal bigDecimal, Integer age) throws CalculatePremiumException {
        BigDecimal q = BigDecimal.ONE;
        q = q
                .multiply(
                        this.allowedAgeMult(risk) ?
                                this.getMultByAge(age) : BigDecimal.ONE
                )
                .multiply(
                        this.allowedPeriodMult(risk) ?
                                this.getMultByPeriod(contract) : BigDecimal.ONE
                )
                .multiply(
                        this.allowedTerritoryMult(risk) ?
                                this.getMultByTerritory(contract) : BigDecimal.ONE
                )
                .multiply(
                        this.allowedActivityMult(risk) ?
                                this.getMultByActivity(contract) : BigDecimal.ONE
                )
                .multiply(
                        this.allowedFranchiseMult(risk) ?
                                this.getMultByFranchise(contract, risk) : BigDecimal.ONE
                )
        ;


        ContractPremium cp = new ContractPremium();
        cp.setRisk(risk);
        cp.setContract(contract);
        cp.setInsuredSum(bigDecimal);

        return premiumService.calculatePremiumWOSaving(cp, contract.getLength(), q);
    }


}
