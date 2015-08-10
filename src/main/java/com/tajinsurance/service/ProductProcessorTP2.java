package com.tajinsurance.service;

import com.tajinsurance.domain.CatContract;
import com.tajinsurance.domain.Contract;
import com.tajinsurance.domain.Risk;

import java.util.List;

/**
 * Created by berz on 19.01.15.
 */
public interface ProductProcessorTP2 extends ProductProcessor {
    Risk getRiskForThisContract(Contract contract);



    List<Risk> getRisksMayBeUsedForAddingProductSettings(CatContract catContract);


    List<Integer> getLengthValuesToAddProductSettings(CatContract catContract, Risk risk);
}
