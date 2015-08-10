package com.tajinsurance.service;

import com.tajinsurance.domain.CatContractStatus;
import com.tajinsurance.domain.CatContractStatusLocaleEntity;
import com.tajinsurance.utils.LanguageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.List;

/**
 * Created by berz on 22.03.14.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CatContractStatusServiceImpl implements CatContractStatusService {
    @Autowired
    EntityManager entityManager;

    @Autowired
    LanguageUtil languageUtil;


    @Override
    public List<CatContractStatus> getAllCatContractStatuses(String locale) {
        List<CatContractStatus> cts = entityManager.createQuery("SELECT o FROM CatContractStatus o", CatContractStatus.class).getResultList();
        for(CatContractStatus ct : cts){
            for(CatContractStatusLocaleEntity ctl : ct.getLocaleEntityList()){
                ct = (CatContractStatus) languageUtil.getLocalizatedObject(ct, locale);
            }
        }
        return cts;
    }

    @Override
    public CatContractStatus getCatContractStatusById(Long id) {
        CatContractStatus ct = entityManager.find(CatContractStatus.class, id);
        if(ct == null) throw new EntityNotFoundException("cant find CatContractStatus object with id: "+id);
        return ct;
    }

    @Override
    public CatContractStatus getCatContractStatusByCode(CatContractStatus.StatusCode code) {
        CatContractStatus ccs = entityManager.createQuery("SELECT o FROM CatContractStatus o WHERE  code = :c", CatContractStatus.class)
                .setParameter("c", code)
                .getSingleResult();
        return ccs;
    }


}
