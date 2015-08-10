package com.tajinsurance.service;

import com.tajinsurance.domain.CatContract;
import com.tajinsurance.domain.CatContractFixedSum;
import com.tajinsurance.domain.Risk;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by berz on 15.07.15.
 */
@Service
public interface CatContractFixedSumService {

    public void save(CatContract catContract, Risk risk, BigDecimal sum);
    public List<CatContractFixedSum> getCatContractFixedSumsForCatContract(CatContract catContract);

    CatContractFixedSum getCatContractFixedSumById(Long fixedSumId);

    void deleteCatContractFixedSum(CatContractFixedSum catContractFixedSum);

    List<CatContractFixedSum> getCatContractFixedSumsForRisk(Risk risk);
}
