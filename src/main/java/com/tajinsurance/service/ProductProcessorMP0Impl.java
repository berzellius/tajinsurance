package com.tajinsurance.service;

import com.tajinsurance.domain.*;
import com.tajinsurance.dto.ContractPremiumAjax;
import com.tajinsurance.exceptions.*;
import com.tajinsurance.utils.CodeUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by berz on 15.09.14.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProductProcessorMP0Impl implements ProductProcessorMP0 {


    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    ContractService contractService;

    @Autowired
    PremiumService premiumService;

    @Autowired
    RiskService riskService;

    @Autowired
    InsuranceAreaService insuranceAreaService;

    @Autowired
    CatContractStatusService catContractStatusService;

    @Autowired
    PartnerService partnerService;

    @Autowired
    MailerService mailerService;

    @Autowired
    CodeUtils codeUtils;

    protected static ProductProcessorMP0Impl instance = null;

    public ProductProcessorMP0Impl() {
    }

    public static ProductProcessorMP0Impl getInstance() {
        if (instance == null) {
            instance = new ProductProcessorMP0Impl();
        }
        return (ProductProcessorMP0Impl) instance;
    }

    @Override
    public ProductProcessor getProductProcessorImplementation(Contract contract) {
        throw new UnsupportedOperationException("allowed only for base ProductProcessorImpl!");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = NoRelatedContractNumber.class)
    public void updateContract(Contract contract, MultipartFile file, ContractImage contractImage, Boolean managerMode) throws NoEntityException, CalculatePremiumException, NoPersonToContractException, BadContractDataException, NoRelatedContractNumber, IOException, IllegalDataException {
        Contract oldContract = contractService.getContractById(contract.getId());
        contract.setVersion(oldContract.getVersion());


        if (file != null) {
            this.processFile(file, contractImage, contract);
            if (contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.WAIT_PHOTO))
                contract.setCatContractStatus(catContractStatusService.getCatContractStatusByCode(CatContractStatus.StatusCode.NEW));
        }

        // Проверяем, задан ли Страхователь
        if (contract.getPerson() == null) throw new NoPersonToContractException("person_not_filled");

        if (contract.getStartDate() == null) throw new BadContractDataException("no_start_date");
        else {

            // Проверяем, будет ли Страхователь совершеннолетним на момент начала действия договора
            if (contract.getPerson().getAge(contract.getStartDate()) < 18)
                throw new BadContractDataException("age_less_18");


            if (contract.getLength() == null) throw new BadContractDataException("period_not_calc");
            else {
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

        if (contract.getLength() != null) {

            BigDecimal q = BigDecimal.valueOf(1);

            // Франшиза
            q = q.multiply(partnerService.getFranchiseMult(contract.getPartner(), contract.getCatContract(), contract.getFranchise()));

            // Выплата возмещения
            q = q.multiply(partnerService.getRefundPaymentMult(contract.getPartner(), contract.getCatContract(), contract.getPaymentType()));

            if (premiumService.getValidatedPremiums(contract) == null) {
                List<InsuranceArea> insuranceAreas = insuranceAreaService.getInsuranceAreasForContract(contract);

                int countObjects = 0;
                for (InsuranceArea insuranceArea : insuranceAreas)
                    countObjects += insuranceArea.getInsuranceObjectList().size();

                if (countObjects == 0) throw new BadContractDataException("no_ins_objects");

                for (InsuranceArea insuranceArea : insuranceAreas) {
                    int countSec = (insuranceArea.getSecuritySystems() != null)? insuranceArea.getSecuritySystems().size() : 0;
                    BigDecimal qSec = partnerService.getSecuritySystemsMult(contract.getPartner(), contract.getCatContract(), countSec);

                    for (InsuranceObject insuranceObject : insuranceArea.getInsuranceObjectList()) {
                        ContractPremium cp = new ContractPremium(insuranceObject.getRisk(), contract, insuranceObject.getSum());
                        cp.setValidated(true);
                        cp.setInsuranceObject(insuranceObject);
                        cp = premiumService.calculatePremium(cp, contract.getLength(), qSec.multiply(q));
                    }
                }

            } else if (contract.getClaimSigned() == null || !contract.getClaimSigned()) {


                // Коррекция существующих премий.

                List<ContractPremium> contractPremiumList = premiumService.getValidatedCPremiums(contract);




                if (!contractPremiumList.isEmpty()) {
                    for (ContractPremium contractPremium : contractPremiumList) {
                        int countSec = (contractPremium.getInsuranceObject().getInsuranceArea().getSecuritySystems() != null)? contractPremium.getInsuranceObject().getInsuranceArea().getSecuritySystems().size() : 0;

                        BigDecimal qSec = partnerService.getSecuritySystemsMult(contract.getPartner(), contract.getCatContract(), countSec);

                        premiumService.premiumCorrection(contractPremium.getId(), contract, qSec.multiply(q));
                    }
                }

                // Досчитываем недостающие

                List<InsuranceObject> insuranceObjects = insuranceAreaService.getInsuranceObjectsWithoutPremium(contract);

                for (InsuranceObject insuranceObject : insuranceObjects) {
                    int countSec = (insuranceObject.getInsuranceArea().getSecuritySystems() != null)? insuranceObject.getInsuranceArea().getSecuritySystems().size() : 0;
                    BigDecimal qSec = partnerService.getSecuritySystemsMult(contract.getPartner(), contract.getCatContract(), countSec);

                    ContractPremium cp = new ContractPremium(insuranceObject.getRisk(), contract, insuranceObject.getSum());
                    cp.setValidated(true);
                    cp.setInsuranceObject(insuranceObject);
                    cp = premiumService.calculatePremium(cp, contract.getLength(), qSec.multiply(q));
                }

            }

        }


        // Определяем пороги
        ProductProperty limit1 = partnerService.getPartnerProductMoneyPropertyByPropertyName(
                contract.getPartner(),
                ProductMoneyProperty.PropertyType.MP0_money_level_1,
                contract.getCatContract()
        );

        ProductProperty limit2 = partnerService.getPartnerProductMoneyPropertyByPropertyName(
                contract.getPartner(),
                ProductMoneyProperty.PropertyType.MP0_money_level_2,
                contract.getCatContract()
        );

        if (limit1 == null || limit2 == null || limit1.getMoneyValue() == null || limit2.getMoneyValue() == null)
            throw new BadContractDataException("no_limits");

        if (
                premiumService.getAllInsuredSumForContractPremiums(contract).compareTo(
                        limit2.getMoneyValue()
                ) == 1 && contract.getClaimSigned() && !oldContract.getClaimSigned()
                ) {
            // Превышен второй порог
            contract.setCatContractStatus(catContractStatusService.getCatContractStatusByCode(CatContractStatus.StatusCode.WAIT_BIMA));

            try {
                HashMap<String, String> emailParams = new LinkedHashMap<String, String>();
                //emailParams.put("link", "/contracts/" + contract.getId().toString() + "/?form");

                emailParams.put("number", contract.getC_number());
                emailParams.put("date", new SimpleDateFormat("dd.MM.yyyy (HH:mm:ss)").format(new Date()));
                emailParams.put("partner", contract.getPartner().toString());
                emailParams.put("user", contract.getCreator().getFio());
                //emailParams.put("link", codeUtils.getProjectLink("/contracts/" + contract.getId().toString() + "/?form"));


                mailerService.sendMailFromTmpl("mail@bima.tj", MailerService.EMAIL_TMPL.MP0_WAIT_NOTIFICATION, emailParams);
                //mailerService.sendMailFromTmpl("berzellius@yandex.ru", MailerService.EMAIL_TMPL.MP0_WAIT_NOTIFICATION, emailParams);
                contract.setEmailSent(true);
            } catch (org.springframework.mail.MailSendException e) {
                contract.setEmailSent(false);
            }
        }

        // Можно ли перевести договор в статус НОВЫЙ?
        // TODO пересмотреть условия, часть из них выше на исклчючениях отвалится
        if (contract.getPerson() != null &&
                premiumService.getValidatedPremiums(contract) != null &&
                contract.getEndDate() != null &&


                contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.BEGIN)
                ) {


            // Можно. Присваиваем номер и меняем статус


            if (
                    premiumService.getAllInsuredSumForContractPremiums(contract).compareTo(
                            limit1.getMoneyValue()
                    ) == 1 &&
                            premiumService.getAllInsuredSumForContractPremiums(contract).compareTo(
                                    limit2.getMoneyValue()
                            ) < 1 &&
                            contract.getContractImages() == null
                    ) {
                // Превышен первый порог
                contract.setCatContractStatus(catContractStatusService.getCatContractStatusByCode(CatContractStatus.StatusCode.WAIT_PHOTO));
            } else {
                contract.setCatContractStatus(catContractStatusService.getCatContractStatusByCode(CatContractStatus.StatusCode.NEW));
            }


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
                    ) {

                if (contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.WAIT_PHOTO)) {
                    throw new BadContractDataException("still_wait_photo");
                } else if (contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.WAIT_BIMA)) {
                    if (oldContract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.WAIT_BIMA) && !managerMode) {
                        throw new BadContractDataException("still_wait_bima");
                    }
                } else throw new BadContractDataException("wrong_data");
            }


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
    public void updateContract(Contract contract, Boolean managerMode) throws NoEntityException, CalculatePremiumException, NoPersonToContractException, BadContractDataException, NoRelatedContractNumber, IOException, IllegalDataException {
        updateContract(contract, null,  null, managerMode);
    }

    @Override
    public BigDecimal getIntegralInsuredSumForContract(Contract contract) {
        return entityManager.createQuery("SELECT SUM(o.sum) FROM InsuranceObject o WHERE o.insuranceArea.contract = :c", BigDecimal.class)
                .setParameter("c", contract)
                .getSingleResult();
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
        BigDecimal cost = entityManager
                .createQuery(
                        "SELECT sum(realsum) FROM InsuranceObject o WHERE insuranceArea.contract = :c and risk.det != 'TMO'"
                        , BigDecimal.class)
                .setParameter("c", contract)
                .getSingleResult();
        return (cost == null || cost.equals(BigDecimal.ZERO)) ? null : cost;
    }

    @Override
    public BigDecimal getAllPremiumForContractWithoutSaving(Contract contract, List<InsuranceArea> insuranceAreas) throws CalculatePremiumException {
        BigDecimal premium = BigDecimal.ZERO;

        BigDecimal q = BigDecimal.ONE;

        q = q.multiply(partnerService.getFranchiseMult(contract.getPartner(), contract.getCatContract(), contract.getFranchise()));

        q = q.multiply(partnerService.getRefundPaymentMult(contract.getPartner(), contract.getCatContract(), contract.getPaymentType()));

        int countObjects = 0;
        for (InsuranceArea insuranceArea : insuranceAreas)
            countObjects += insuranceArea.getInsuranceObjectList().size();


        for (InsuranceArea insuranceArea : insuranceAreas) {
            int countSecSyst = (insuranceArea.getSecuritySystems() != null)? insuranceArea.getSecuritySystems().size() : 0;

            BigDecimal qSec = partnerService.getSecuritySystemsMult(contract.getPartner(), contract.getCatContract(), countSecSyst);

            for (InsuranceObject insuranceObject : insuranceArea.getInsuranceObjectList())
                if (insuranceObject.getSum() != null) {
                    ContractPremium cp = new ContractPremium(insuranceObject.getRisk(), contract, insuranceObject.getSum());
                    cp.setValidated(true);
                    cp.setInsuranceObject(insuranceObject);
                    premium = premium.add(
                            premiumService.calculatePremiumWOSaving(cp, contract.getLength(), qSec.multiply(q))
                    );
                }
        }

        return premium;
    }

    @Override
    public BigDecimal getIntegralInsuredSumForContractWithoutSaving(Contract contract, List<InsuranceArea> insuranceAreas) {
        BigDecimal isum = BigDecimal.ZERO;

        for (InsuranceArea insuranceArea : insuranceAreas) {
            for (InsuranceObject insuranceObject : insuranceArea.getInsuranceObjectList()) {
                if (insuranceObject.getSum() != null) isum = isum.add(insuranceObject.getSum());
            }
        }


        return isum;
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

    private String generateFilename(MultipartFile file) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] bytes = messageDigest.digest(
                    ((String) file.getOriginalFilename() + file.getSize() + new Date().toString()).getBytes("UTF-8")
            );
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytes.length; ++i) {
                sb.append(Integer.toHexString((bytes[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
        } catch (NoSuchAlgorithmException e) {
            return file.getOriginalFilename();
        } catch (UnsupportedEncodingException e) {
            return file.getOriginalFilename();
        }

    }



    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void processFile(MultipartFile file, ContractImage contractImage, Contract contract) throws IOException, IllegalDataException {
        if (file.getContentType().equals("image/gif") ||
                file.getContentType().equals("image/jpeg") ||
                file.getContentType().equals("image/png")
                ) {


            if(file.getSize() > 2*1024*1024) throw new IllegalImageDataException("File is very big!", IllegalDataException.Reason.WRONG_FILE_SIZE);

            contractImage.setContract(contract);

            contractImage.setPath(this.generateFilename(file));
            contractImage.setExtension(FilenameUtils.getExtension(contractImage.getPath()));

            entityManager.persist(contractImage);

            File fileToSave = new File(codeUtils.getUploadImagesDirPath() + contractImage.getPath());


            file.transferTo(fileToSave);
        } else throw new IllegalImageDataException("Allowed only images png/jpeg/gif!", IllegalDataException.Reason.WRONG_IMAGE_FORMAT);

    }
}
