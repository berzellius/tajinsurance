package com.tajinsurance.service;

import com.tajinsurance.domain.*;
import com.tajinsurance.dto.RiskAjax;
import com.tajinsurance.utils.LanguageUtil;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by berz on 20.03.14.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CatContractServiceImpl implements CatContractService {

    /*@PersistenceContext
    transient EntityManager entityManager;*/
    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    LanguageUtil languageUtil;

    @Autowired
    RiskService riskService;

    @Override
    public List<CatContract> getAllCatContracts(String locale) {
        List<CatContract> cts = entityManager.createQuery("SELECT o FROM CatContract o WHERE deleted = false AND product != null", CatContract.class).getResultList();
        return getLocalizations(cts, locale);
    }

    private List<CatContract> getLocalizations(List<CatContract> cts, String locale){
        for(CatContract ct : cts){
            for(CatContractLocaleEntity ctl : ct.getLocaleEntityList()){
                ct = (CatContract) languageUtil.getLocalizatedObject(ct, locale);
            }
        }
        return cts;
    }

    @Override
    public CatContract getCatContractById(Long id) {
        CatContract ct = entityManager.find(CatContract.class, id);
        if(ct == null) throw new EntityNotFoundException("cant find CatContract object with id: "+id);
        return ct;
    }

    @Override
    public List<CatContract> getAllowedCatContractsForUser(User u, String locale) {
        List<CatContract> catContracts = new LinkedList<CatContract>(); 
        if(u.getPartner() == null) return getAllCatContracts(locale);

        catContracts = u.getPartner().getCatContracts();

        for(CatContract catContract : catContracts) if(!catContract.getDeleted()){
            catContract = (CatContract) languageUtil.getLocalizatedObject(catContract, locale);
        }

        return catContracts;
        //return getLocalizations(u.getPartner().getCatContracts(),locale);
    }

    @Override
    public List<RiskAjax> getAllowedRisksForCatContract(CatContract cc) {
        Session session = entityManager.unwrap(Session.class);

        List<RiskAjax> ra = new LinkedList<RiskAjax>();
            List<CatContractRisk> ccr = (List<CatContractRisk>) session.createCriteria(CatContractRisk.class)
                    .add(Restrictions.eq("catContract", cc))
                    .list();
            for(CatContractRisk cr : ccr){
                ra.add(new RiskAjax(cr.getRisk().getValue(), cr.getRisk().getId(), cr.getRate()));
            }
        return ra;
    }

    @Override
    public void createNewCatContract(List<CatContractLocaleEntity> localeEntities, List<Risk> risks, Currency currency, Integer minTerm, Long typeOfRiskId, Boolean useInsuranceArea) {


        CatContract catContract = new CatContract();

        /* Пока что такое разделение рисков по типам. Это пока само разделение очень локализовано. Потом пееходить на нормальную иерархию */

        if(typeOfRiskId == 1){
            if(risks != null) catContract.setRisks(risks);
        }

        if(typeOfRiskId == 2){
            catContract.setRisks(riskService.getAllRisksByType(riskService.getTypeOfRiskById(2l)));
        }

        catContract.setCurrency(currency);
        catContract.setMinTerm(minTerm);
        catContract.setTypeOfRisk(riskService.getTypeOfRiskById(typeOfRiskId));
        catContract.setUseInsuranceAreas(useInsuranceArea);

        entityManager.persist(catContract);
        entityManager.flush();
        entityManager.refresh(catContract);

        for(CatContractLocaleEntity catContractLocaleEntity : localeEntities){
            catContractLocaleEntity.setCatContract(catContract);
            entityManager.persist(catContractLocaleEntity);
        }

    }

    @Override
    public List<Currency> getCurrencies() {
        return entityManager.createQuery("SELECT o FROM Currency o").getResultList();
    }

    @Override
    public Currency getCurrencyById(Long currencyId) {
        return entityManager.find(Currency.class, currencyId);
    }

    @Override
    public void updateCatContract(Long catContractId, List<CatContractLocaleEntity> catContractLocaleEntities, List<Risk> risks, Currency currency, Integer minTerm) {

        CatContract catContract = getCatContractById(catContractId);

        for(CatContractLocaleEntity catContractLocaleEntity : catContractLocaleEntities){
            CatContractLocaleEntity ccle = entityManager.find(CatContractLocaleEntity.class, catContractLocaleEntity.getId());
            ccle.setValue(catContractLocaleEntity.getValue());
            ccle.setName(catContractLocaleEntity.getName());

            entityManager.merge(ccle);
        }
        if(risks != null) catContract.setRisks(risks);

        catContract.setCurrency(currency);
        catContract.setMinTerm(minTerm);


        entityManager.merge(catContract);
    }

    @Override
    public void remove(CatContract catContract) {
        catContract.setDeleted(true);
        entityManager.merge(catContract);
    }

    @Override
    public List<CatContract> getCatContractsGlobalSettings(String language) {
        List<CatContract> catContracts = entityManager.createQuery("SELECT o FROM CatContract o WHERE settingsWay = :sw")
                .setParameter("sw", CatContract.SettingsWay.GLOBAL)
                .getResultList();
        return this.getLocalizations(catContracts, language);
    }

    @Override
    public List<CatContractPartnerFranchise> getFranchises(CatContract catContract) {
        return entityManager.createQuery(
                "SELECT o FROM CatContractPartnerFranchise o WHERE catContract = :cc",
                CatContractPartnerFranchise.class
        )
                .setParameter("cc", catContract)
                .getResultList();
    }


}
