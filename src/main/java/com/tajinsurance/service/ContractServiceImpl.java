package com.tajinsurance.service;

import com.tajinsurance.domain.*;
import com.tajinsurance.dto.AjaxContractListFilter;
import com.tajinsurance.dto.ContractPremiumAjax;
import com.tajinsurance.dto.RiskAjax;
import com.tajinsurance.exceptions.*;
import com.tajinsurance.utils.CodeUtils;
import com.tajinsurance.utils.LanguageUtil;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.io.IOException;
import java.util.*;

/**
 * Created by berz on 27.02.14.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ContractServiceImpl implements ContractService {

    @Autowired
    CodeUtils codeUtils;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Autowired
    CatContractStatusService catContractStatusService;

    @Autowired
    CatContractService catContractService;

    @Autowired
    PremiumService premiumService;

    @Autowired
    RiskService riskService;

    @Autowired
    LanguageUtil languageUtil;

    @Autowired
    ForbiddenReceiptNumberService forbiddenReceiptNumberService;

    @Autowired
    MailerService mailerService;

    @Autowired
    ProductProcessor productProcessor;

    public ContractServiceImpl() {
    }


    @Override
    public List<Contract> localizeContractsList(List<Contract> cts, String locale) {

        if(cts == null){
            List<Contract> contracts = new LinkedList<Contract>();
            return contracts;
        }

        for (Contract c : cts) {
            c = (Contract) languageUtil.getLocalizatedObject(c, locale);
        }

        return cts;
    }

    @Override
    public Contract getContractById(Long id) throws NoEntityException {
        Contract c = entityManager.find(Contract.class, id);
        if (c == null) throw new NoEntityException("there is no such Contract!");
        else return c;
    }

    @Override
    public Contract getContractById(Long id, String locale) throws NoEntityException {
        return (Contract) languageUtil.getLocalizatedObject(getContractById(id), locale);
    }

    @Override
    public List<Contract> getContracts(int first, int count, String sortFieldName, String sortOrder) {
        //return Contract.findContractEntries(first, count, sortFieldName, sortOrder);
        return getContracts(null, first, count, sortFieldName, sortOrder);
    }

    @Override
    public List<Contract> getContracts(int first, int count) {
        //return Contract.findContractEntries(first, count);
        return getContracts(null, first, count);
    }

    private List<Contract> getContracts(AjaxContractListFilter filter, int first, int count, String sortFieldName, String sortOrder) {


        String jpaQuery = "SELECT o FROM Contract o WHERE deleted=false ";

        String filterWC = filterWhereClause(filter);

        if (!filterWC.trim().equals("")) jpaQuery += " AND ( " + filterWC + " )";
        else jpaQuery += " AND (ccs_id != 4) ";

        /*if (Contract.fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        } else {
            jpaQuery = jpaQuery + " ORDER BY startDate DESC";
        }*/

        if (filter.orderColumn != null) {
            jpaQuery += " ORDER BY " + filter.orderColumn + " " + ((filter.orderType != null) ? filter.orderType : " DESC");
        } else jpaQuery += " ORDER BY startDate DESC";


        Query query = entityManager.createQuery(jpaQuery, Contract.class);

        if (filter != null) query = applyFilter(filter, query);

        return query.setFirstResult(first).setMaxResults(count).getResultList();
    }

    private List<Contract> getContracts(AjaxContractListFilter filter, int first, int count) {

        String jpaQuery = "SELECT o FROM Contract o WHERE deleted=false ";

        String filterWC = filterWhereClause(filter);
        if (!filterWC.trim().equals("")) jpaQuery += " AND ( " + filterWC + " )";
        else jpaQuery += " AND (ccs_id != 4)";

        try {
            if (filter.orderColumn == null || filter.orderColumn.equals("")) {
                // TODO wtf????? NPE!
                jpaQuery += " ORDER BY startDate DESC";
            } else
                jpaQuery += " ORDER BY " + filter.orderColumn + " " + ((filter.orderType != null) ? filter.orderType : " DESC");
        } catch (NullPointerException e) {
            jpaQuery += " ORDER BY startDate DESC";
        }

        Query query = entityManager.createQuery(jpaQuery, Contract.class).setFirstResult(first).setMaxResults(count);


        if (filter != null) query = applyFilter(filter, query);


        return query.getResultList();
    }

    private String filterWhereClause(AjaxContractListFilter filter) {
        String w = "";

        if (filter == null) return w;

        if (filter.partner != null) w += " (partner.id = :p) ";

        if (filter.creator != null) w += ((w == "") ? "" : " AND ") + " (creator.id = :ctr) ";

        if (filter.partnerStr != null)
            w += ((w == "") ? "" : " AND ") + " (LOWER(partner.value) LIKE LOWER(:partnerStr)) ";

        if (filter.creatorStr != null)
            w += ((w == "") ? "" : " AND ") + " (LOWER(creator.username) LIKE LOWER(:creatorStr) OR LOWER(CONCAT(creator.id,'_',creator.username)) LIKE LOWER(:creatorStr)  OR LOWER(creator.fio) LIKE LOWER(:creatorStr)) ";

        if (filter.number != null) w += ((w == "") ? "" : " AND ") + " (c_number LIKE :n) ";

        String dts = "";
        if (filter.startDateFrom != null) dts += " startDate >= :sdf ";
        if (filter.startDateTo != null) dts += ((dts == "") ? "" : " AND ") + " startDate <= :sdt ";

        if (dts != "") w += ((w == "") ? "" : " AND ") + dts;


        if (filter.categories != null) {
            w += ((w == "") ? "" : " AND ") + " cc_id IN (:cc)";
        }

        if (filter.statuses != null) {
            w += ((w == "") ? "" : " AND ") + " ccs_id IN (:ccs)";
        } else {
            w += ((w == "") ? "" : " AND ") + " ccs_id != 4";
        }

        if (filter.paymentWays != null) {
            w += ((w == "") ? "" : " AND ") + " payment_way_id IN (:pws)";
        }

        if (filter.personMask != null) {
            w += ((w == "") ? "" : " AND ") + "LOWER( CONCAT( o.person.surname, ' ', o.person.name, ' ', o.person.middle) ) LIKE LOWER(:person) ";
        }

        if (filter.isApp != null)
            w += ((w == "") ? "" : "AND ") + " appDate IS " + ((filter.isApp) ? "NOT" : "") + " NULL ";
        if (filter.isPaid != null)
            w += ((w == "") ? "" : "AND ") + " payDate IS " + ((filter.isPaid) ? "NOT" : "") + " NULL ";
        if (filter.isPrinted != null)
            w += ((w == "") ? "" : "AND ") + " printDate IS " + ((filter.isPrinted) ? "NOT" : "") + " NULL ";


        return w;
    }

    private Query applyFilter(AjaxContractListFilter filter, Query query) {


        if (filter.partner != null) query.setParameter("p", filter.partner.getId());

        if (filter.creator != null) query.setParameter("ctr", filter.creator.getId());

        if (filter.creatorStr != null) query.setParameter("creatorStr", filter.creatorStr + "%");

        if (filter.partnerStr != null) query.setParameter("partnerStr", filter.partnerStr + "%");

        if (filter.number != null) query.setParameter("n", filter.number + "%");

        if (filter.startDateFrom != null) query.setParameter("sdf", filter.startDateFrom);

        if (filter.startDateTo != null) query.setParameter("sdt", filter.startDateTo);

        if (filter.categories != null) {
            query.setParameter("cc", filter.categories);
        }

        if (filter.statuses != null) {
            query.setParameter("ccs", filter.statuses);
        }

        if (filter.paymentWays != null) {
            query.setParameter("pws", filter.paymentWays);
        }

        if (filter.personMask != null) {
            query.setParameter("person", filter.personMask + "%");
        }

        return query;
    }

    @Override
    public List<Contract> getContractsPage(Integer numPage, Integer itemsToPage, String sortFieldName, String sortOrder) {
        int page = (numPage == null) ? 1 : numPage.intValue();
        int size = (itemsToPage == null) ? 50 : itemsToPage.intValue();

        int first = (page - 1) * size;


        if (sortFieldName != null) {
            return getContracts(null, first, size, sortFieldName, sortOrder);
        } else {
            return getContracts(null, first, size);
        }
    }

    @Override
    public List<Contract> getContractsPage(AjaxContractListFilter filter, Integer numPage, Integer itemsToPage, String sortFieldName, String sortOrder) {

        if (filter == null) return getContractsPage(numPage, itemsToPage, sortFieldName, sortOrder);

        int page = (numPage == null) ? 1 : numPage.intValue();
        int size = (itemsToPage == null) ? 50 : itemsToPage.intValue();

        int first = (page - 1) * size;


        if (sortFieldName != null) {
            return getContracts(filter, first, size, sortFieldName, sortOrder);
        } else {
            return getContracts(filter, first, size);
        }
    }

    @Override
    public Long countContracts() {
        return entityManager.createQuery("SELECT COUNT(o) FROM Contract o", Long.class).getSingleResult();
    }

    @Override
    public Long countContracts(AjaxContractListFilter filter, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT count(o) FROM Contract o WHERE deleted=false ";

        String filterWC = filterWhereClause(filter);

        if (!filterWC.trim().equals("")) jpaQuery += " AND ( " + filterWC + " )";
        else jpaQuery += " AND (ccs_id != 4) ";

        Query query = entityManager.createQuery(jpaQuery, Long.class);

        if (filter != null) query = applyFilter(filter, query);
        return (Long) query.getSingleResult();
    }

    @Override
    //@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Contract preparedContract(User u, CatContract cc) {


        Contract c = new Contract();

        c.setPartner(u.getPartner());
        c.setCreator(u);
        c.setC_number(null);
        if(!cc.getProduct().equals(CatContract.Product.TP2)){
            c.setStartDate(new Date());
        }
        else{
            c.setAppDate(new Date());
        }
        c.setCatContractStatus(catContractStatusService.getCatContractStatusByCode(CatContractStatus.StatusCode.BEGIN));

        c.setCatContract(cc);

        entityManager.persist(c);
        entityManager.flush();
        entityManager.refresh(c);


        return c;
    }

    private String digits(long dg, int length) {
        String res = Long.toString(dg);
        int l = res.length();

        for (int i = 0; i < length - l; i++) res = "0" + res;

        return res;
    }

    private String getContractNum(long idPartner, long pcc) {
        String d1 = digits(idPartner, 2);
        return d1 + digits(pcc, 10 - d1.length());
    }

    @Override
    public boolean checkIfTheRiskIsAllowed(Risk risk, Contract contract) {
        List<RiskAjax> ra = catContractService.getAllowedRisksForCatContract(contract.getCatContract());
        for (RiskAjax r : ra) if (r.riskId == risk.getId()) return true;
        return false;
    }

    @Override
    public Date printContract(Contract c) {

        return productProcessor.getProductProcessorImplementation(c).printContract(c);
    }

    @Override
    public Date doDefaultPrintContract(Contract c){
        if (c.getPrintDate() == null) {
            c.setPrintDate(new Date());
        }

        entityManager.persist(c);

        return c.getPrintDate();
    }

    @Override
    public void cancelContract(Contract c) {

        c.setCatContractStatus(catContractStatusService.getCatContractStatusByCode(CatContractStatus.StatusCode.CANCELLED));

        entityManager.persist(c);


    }



    @Override
    public Integer countPages(Integer size) {
        if (size == null || size <= 0) size = 50;
        float fp = countContracts().floatValue() / size.floatValue();

        return (int) ((fp == 0.0 || fp > (int) fp) ? fp + 1 : fp);
    }

    @Override
    public Integer countPages(AjaxContractListFilter filter, Integer page, Integer size, String sortFieldName, String sortOrder) {
        if (size == null || size <= 0) size = 50;
        float fp = countContracts(filter, sortFieldName, sortOrder).floatValue() / size.floatValue();

        return (int) ((fp == 0.0 || fp > (int) fp) ? fp + 1 : fp);
    }

    @Override
    public void save(Contract contract) throws EntityNotSavedException {


        entityManager.persist(contract);
        entityManager.flush();
        Long id = contract.getId();
        if (id == null) throw new EntityNotSavedException("не удалось создать объект " + contract.toString());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = NoRelatedContractNumber.class)
    public void update(Contract contract, Boolean managerMode) throws NoEntityException, CalculatePremiumException, NoPersonToContractException, BadContractDataException, NoRelatedContractNumber {


        Contract oldContract = getContractById(contract.getId());
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
                    contract.getPerson().getAge(contract.getEndDate()) > 63
                    )
                throw new BadContractDataException("age_more_60");

            if (contract.getPerson().getSex() == Person.Sex.FEMALE &&
                    contract.getPerson().getAge(contract.getEndDate()) > 58
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
                contract.getPerson().getAge(contract.getStartDate()) >= 18 &&
                (
                        (contract.getPerson().getAge(contract.getEndDate()) <= 63 && contract.getPerson().getSex() == Person.Sex.MALE)
                                || (contract.getPerson().getAge(contract.getEndDate()) <= 58 && contract.getPerson().getSex() == Person.Sex.FEMALE)
                ) &&

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
    public void delete(Long id) throws NoEntityException {
        Contract c = getContractById(id);


        if (c == null) throw new NoEntityException("не найден контракт с id = " + c.getId().toString());

        //c.remove();
        c.setDeleted(true);
        entityManager.persist(c);
    }

    @Override
    public Contract dropPrintClaimFlag(Contract c) {

        c.setPrintClaimFlag(false);

        entityManager.merge(c);
        entityManager.flush();
        entityManager.refresh(c);

        return c;
    }


    @Override
    public void setPersonToContract(Contract contract) {
        assert (contract.getPerson() != null);

        Contract thisContract = entityManager.find(Contract.class, contract.getId());

        if (thisContract.getPerson() != null) return;
        else {
            thisContract.setPerson(contract.getPerson());
            entityManager.merge(contract);
            contract.setVersion(thisContract.getVersion());
        }
    }

    @Override
    public List<PaymentWay> getAllPaymentWays() {
        return entityManager.createQuery("SELECT o FROM PaymentWay o", PaymentWay.class).getResultList();
    }

    @Override
    public PaymentWay getPaymentWayById(Long id) {
        assert (id > 0);

        return entityManager.find(PaymentWay.class, id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setReceiptNumber(Contract contract, Integer month) {
        if(month == null) month = forbiddenReceiptNumberService.getMaxGeneration();

        entityManager.createNativeQuery("SELECT set_contract_rec_number(" + month + ", " + contract.getId() + ")").getSingleResult();
    }

    @Override
    public void setReceiptNumber(Contract contract){
        Integer m = codeUtils.getDigitsForMonth(new Date());
        setReceiptNumber(contract, m);
    }

    @Override
    public ContractImage getContractImageById(Long id) {
        ContractImage contractImage = entityManager.find(ContractImage.class, id);

        if(contractImage.getExtension() == null) contractImage.setExtension(FilenameUtils.getExtension(contractImage.getPath()));
        return contractImage;
    }

    @Override
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public void acceptContract(Contract contract) {

         if(
                 contract.getCatContract().getProduct().equals(CatContract.Product.MP0) &&
                 contract.getCatContractStatus().getCode().equals(CatContractStatus.StatusCode.WAIT_BIMA) &&
                 contract.getContractImages() != null &&
                 contract.getContractImages().size() > 0
                 ){

             contract.setCatContractStatus(catContractStatusService.getCatContractStatusByCode(CatContractStatus.StatusCode.NEW));

             if(contract.getCreator().getEmail() != null){
                 try {
                     HashMap<String, String> emailParams = new LinkedHashMap<String, String>();
                     //emailParams.put("link", "/contracts/" + contract.getId().toString() + "/?form");

                     emailParams.put("number", contract.getC_number());
                     emailParams.put("user", contract.getCreator().getFio());
                     //emailParams.put("link", codeUtils.getProjectLink("/contracts/" + contract.getId().toString() + "/?form"));


                     mailerService.sendMailFromTmpl(contract.getCreator().getEmail(), MailerService.EMAIL_TMPL.MP0_CONTRACT_ACCEPTED, emailParams);

                     contract.setEmailSent(true);
                 } catch (org.springframework.mail.MailSendException e) {
                     contract.setEmailSent(false);
                 } catch (IOException e) {
                     contract.setEmailSent(false);
                 }

             }


             entityManager.merge(contract);
         }
    }

    @Override
    public void deleteImage(ContractImage contractImage) {
        entityManager.remove(contractImage);
    }

    @Override
    public void updateImage(ContractImage contractImage) {
        entityManager.merge(contractImage);
    }

    @Override
    public List<Contract> getAllContracts(String locale) {
        return localizeContractsList(entityManager.createQuery(
                "SELECT o FROM Contract o WHERE deleted = false AND catContractStatus != " +
                        "( SELECT o1 FROM CatContractStatus o1 WHERE o1.code = :ccsc)",
               Contract.class
        )
                .setParameter("ccsc", CatContractStatus.StatusCode.BEGIN)
                .getResultList(),
                locale);
    }

    @Override
    public List<Contract> getAllContracts(String language, Partner partner) {
        return localizeContractsList(entityManager.createQuery(
                "SELECT o FROM Contract o WHERE deleted = false AND catContractStatus != " +
                        "( SELECT o1 FROM CatContractStatus o1 WHERE o1.code = :ccsc) AND partner = :p",
                Contract.class
        )
                .setParameter("ccsc", CatContractStatus.StatusCode.BEGIN)
                .setParameter("p", partner)
                .getResultList(),
                language);
    }

    @Override
    public List<Contract> getAllContracts(String language, User user) {
        return localizeContractsList(entityManager.createQuery(
                "SELECT o FROM Contract o WHERE deleted = false AND catContractStatus != " +
                        "( SELECT o1 FROM CatContractStatus o1 WHERE o1.code = :ccsc) AND creator = :u",
                Contract.class
        )
                .setParameter("ccsc", CatContractStatus.StatusCode.BEGIN)
                .setParameter("u", user)
                .getResultList(),
                language);
    }

    @Override
    public void saveContractState(Contract contract) {
        entityManager.merge(contract);
    }


}
