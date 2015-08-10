package com.tajinsurance.utils;

import com.tajinsurance.domain.CatContract;
import com.tajinsurance.domain.Contract;
import com.tajinsurance.service.ProductProcessor;
import com.tajinsurance.service.ProductProcessorBA0Impl;
import com.tajinsurance.service.ProductProcessorCU0Impl;
import com.tajinsurance.service.ProductProcessorMP0Impl;

/**
 * Created by berz on 15.09.14.
 */
public class ProductProcessorFactory {
    public static ProductProcessor getProductProcessorImpl(Contract contract){
        if(contract.getCatContract().getProduct().equals(CatContract.Product.CU0)) return ProductProcessorCU0Impl.getInstance();

        if(contract.getCatContract().getProduct().equals(CatContract.Product.BA0)) return ProductProcessorBA0Impl.getInstance();

        if(contract.getCatContract().getProduct().equals(CatContract.Product.MP0)) return ProductProcessorMP0Impl.getInstance();

        throw new UnsupportedOperationException("cant load proper processor for product (" + contract.getCatContract().getProduct().toString() + ") . Maybe you testing new product? Then just create ProductProcessor realization.");
    }
}
