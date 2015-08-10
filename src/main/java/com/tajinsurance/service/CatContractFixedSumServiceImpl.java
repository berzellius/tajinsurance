package com.tajinsurance.service;

import com.tajinsurance.domain.CatContract;
import com.tajinsurance.domain.CatContractFixedSum;
import com.tajinsurance.domain.Risk;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by berz on 15.07.15.
 */
@Service
@Transactional
public class CatContractFixedSumServiceImpl implements CatContractFixedSumService {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public void save(CatContract catContract, Risk risk, BigDecimal sum) {
        Assert.isTrue(catContract != null);
        Assert.isTrue(risk != null);
        Assert.isTrue(sum.compareTo(BigDecimal.ZERO) > 0);

        CatContractFixedSum catContractFixedSum = new CatContractFixedSum(catContract, risk, sum);
        entityManager.persist(catContractFixedSum);
    }

    @Override
    public List<CatContractFixedSum> getCatContractFixedSumsForCatContract(CatContract catContract) {
        return entityManager.createQuery(
                "SELECT o FROM CatContractFixedSum o WHERE o.catContract = :cc"
        )
                .setParameter("cc", catContract)
                .getResultList();
    }

    @Override
    public CatContractFixedSum getCatContractFixedSumById(Long fixedSumId) {
        CatContractFixedSum catContractFixedSum = (CatContractFixedSum) entityManager.find(CatContractFixedSum.class, fixedSumId);
        return catContractFixedSum;
    }

    @Override
    public void deleteCatContractFixedSum(CatContractFixedSum catContractFixedSum) {
        entityManager.remove(catContractFixedSum);
    }

    @Override
    public List<CatContractFixedSum> getCatContractFixedSumsForRisk(Risk risk) {
        return entityManager.createQuery(
                "SELECT o FROM CatContractFixedSum o WHERE o.risk = :r"
        )
                .setParameter("r", risk)
                .getResultList();
    }


}
