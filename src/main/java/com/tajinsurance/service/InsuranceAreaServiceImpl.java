package com.tajinsurance.service;

/**
 * Created by berz on 14.09.14.
 */

import com.tajinsurance.domain.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class InsuranceAreaServiceImpl implements InsuranceAreaService {
    @PersistenceContext
    EntityManager entityManager;

    public InsuranceAreaServiceImpl() {
    }

    @Override
    public InsuranceArea getInsuranceAreaById(Long id) {
        return entityManager.find(InsuranceArea.class, id);
    }

    @Override
    public InsuranceObject getInsuranceObjectById(Long id) {
        return entityManager.find(InsuranceObject.class, id);
    }

    @Override
    public SecuritySystem getSecuritySystemById(Long id) {
        return entityManager.find(SecuritySystem.class, id);
    }

    @Override
    public void saveInsuranceArea(InsuranceArea insuranceArea) {
        entityManager.persist(insuranceArea);
    }

    @Override
    public void saveInsuranceObject(InsuranceObject insuranceObject) {
        entityManager.persist(insuranceObject);
    }

    @Override
    public void addSecuritySystemToArea(SecuritySystem securitySystem, InsuranceArea insuranceArea) {

    }

    @Override
    public void deleteInsuranceArea(InsuranceArea insuranceArea) {
        for(InsuranceObject insuranceObject : insuranceArea.getInsuranceObjectList()){
            this.deleteInsuranceObject(insuranceObject);
        }

        for(SecuritySystem securitySystem : insuranceArea.getSecuritySystems()){
            this.deleteSecuritySystemFromArea(securitySystem, insuranceArea);
        }

        entityManager.remove(insuranceArea);
    }

    @Override
    public void deleteInsuranceObject(InsuranceObject insuranceObject) {
        try{
            ContractPremium contractPremiumForObject = entityManager
                    .createQuery("SELECT o FROM ContractPremium o WHERE insuranceObject = :io AND deleted = false AND validated = true", ContractPremium.class)
                    .setParameter("io", insuranceObject)
                    .getSingleResult();
            entityManager.remove(contractPremiumForObject);
        }
        catch (EmptyResultDataAccessException e){
            // ok
        }

        entityManager.remove(insuranceObject);
    }

    @Override
    public void deleteSecuritySystemFromArea(SecuritySystem securitySystem, InsuranceArea insuranceArea) {
        // TODO
    }

    @Override
    public List<InsuranceArea> getInsuranceAreasForContract(Contract c) {
        return entityManager.createQuery("SELECT o FROM InsuranceArea o WHERE contract = :c", InsuranceArea.class)
                .setParameter("c", c)
                .getResultList();
    }

    @Override
    public List<SecuritySystem> getSecuritySystemsAvailable(InsuranceArea insuranceArea) {
        if (insuranceArea.getSecuritySystems() != null && !insuranceArea.getSecuritySystems().isEmpty()) {
            return entityManager.createQuery("SELECT o FROM SecuritySystem o WHERE o NOT IN (:ss)", SecuritySystem.class)
                    .setParameter("ss", insuranceArea.getSecuritySystems())
                    .getResultList();
        } else {
            return getAllSecuritySystems();
        }
    }

    @Override
    public void updateInsuranceArea(InsuranceArea insuranceArea) {
        entityManager.merge(insuranceArea);
    }

    @Override
    public List<InsuranceObject> getInsuranceObjectsWithoutPremium(Contract contract) {
        return entityManager.createQuery("SELECT o FROM InsuranceObject o WHERE insuranceArea.contract = :c AND " +
                "NOT EXISTS (SELECT o1 FROM ContractPremium o1 WHERE o1.insuranceObject = o)", InsuranceObject.class)
                .setParameter("c", contract)
                .getResultList();
    }

    @Override
    public List<SecuritySystem> getAllSecuritySystems() {
        return entityManager.createQuery("SELECT o FROM SecuritySystem o", SecuritySystem.class).getResultList();
    }

    @Override
    public void updateObject(InsuranceObject insuranceObject) {
        entityManager.merge(insuranceObject);
    }
}
