package com.tajinsurance.service;

import com.tajinsurance.domain.ForbiddenReceiptNumber;
import com.tajinsurance.domain.ReceiptNumberGeneration;
import com.tajinsurance.exceptions.AddForbiddenReceiptNumberException;
import com.tajinsurance.utils.CodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by berz on 07.07.14.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ForbiddenReceiptNumberServiceImpl implements ForbiddenReceiptNumberService {


    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    CodeUtils codeUtils;

    public ForbiddenReceiptNumberServiceImpl() {
    }

    @Override
    public List<ForbiddenReceiptNumber> getNumbersForCurrentMonth() {
        List<ForbiddenReceiptNumber> numbers = (List<ForbiddenReceiptNumber>) entityManager.createQuery("SELECT o FROM ForbiddenReceiptNumber o WHERE month = :m")
                .setParameter("m", getMaxGeneration()).getResultList();

        return numbers;
    }

    @Override
    public void addNumberForCurrentMonth(Integer number) throws AddForbiddenReceiptNumberException {

        Integer month = getMaxGeneration();

        Query queryUsed = entityManager.createQuery("SELECT o FROM Contract o WHERE receiptNumber = :n AND receiptMonth = :m")
                .setParameter("n", number)
                .setParameter("m", month);


        Query queryExists = entityManager.createQuery("SELECT o FROM ForbiddenReceiptNumber o WHERE month = :m AND number = :n")
                .setParameter("n", number)
                .setParameter("m", month);

        if(queryUsed.getResultList().size() > 0) throw new AddForbiddenReceiptNumberException(AddForbiddenReceiptNumberException.Reason.USED);

        if(queryExists.getResultList().size() > 0) throw new AddForbiddenReceiptNumberException(AddForbiddenReceiptNumberException.Reason.EXISTS);

        ForbiddenReceiptNumber forbiddenReceiptNumber = new ForbiddenReceiptNumber();
        forbiddenReceiptNumber.setMonth(month);
        forbiddenReceiptNumber.setNumber(number);

        entityManager.persist(forbiddenReceiptNumber);
    }

    @Override
    public void deleteNumberForCurrentMonth(Integer number) {
        Query q = entityManager.createQuery("DELETE FROM ForbiddenReceiptNumber f WHERE month = :m AND number = :n");
        q.setParameter("m", codeUtils.getDigitsForMonth(new Date()));
        q.setParameter("n", number);

        q.executeUpdate();
    }

    @Override
    public void deleteNumberById(Long id) {
        entityManager.createQuery("DELETE FROM ForbiddenReceiptNumber f WHERE id = :i")
                .setParameter("i", id)
                .executeUpdate();
    }

    @Override
    public void addNewGeneration() {
        ReceiptNumberGeneration receiptNumberGeneration = new ReceiptNumberGeneration();

        receiptNumberGeneration.setGeneration(getMaxGeneration() + 1);

        entityManager.persist(receiptNumberGeneration);
    }

    @Override
    public Integer getMaxGeneration() {
        List<ReceiptNumberGeneration> receiptNumberGenerations = getAllReceiptNumberGenerationsMaxGeneration();

        Integer max = null;

        if(receiptNumberGenerations.size() > 0) max = receiptNumberGenerations.get(0).getGeneration();
        else max = 201500;

        return max;
    }

    private List<ReceiptNumberGeneration> getAllReceiptNumberGenerationsMaxGeneration(){
        return entityManager.createQuery("SELECT o FROM ReceiptNumberGeneration o WHERE generation = (select max(o1.generation) from ReceiptNumberGeneration o1)").getResultList();
    }

}
