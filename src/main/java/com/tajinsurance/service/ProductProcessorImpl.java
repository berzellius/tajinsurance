package com.tajinsurance.service;

import com.tajinsurance.domain.*;
import com.tajinsurance.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by berz on 15.09.14.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProductProcessorImpl implements ProductProcessor {

    @Autowired
    ProductProcessorCU0 productProcessorCU0;

    @Autowired
    ProductProcessorBA0 productProcessorBA0;

    @Autowired
    ProductProcessorMP0 productProcessorMP0;

    @Autowired
    ProductProcessorTP0 productProcessorTP0;

    @Autowired
    ProductProcessorTP2 productProcessorTP2;

    @Autowired
    ProductProcessorRU0 productProcessorRU0;

    public ProductProcessorImpl() {
    }


    @Override
    public ProductProcessor getProductProcessorImplementation(Contract contract) {
        if(contract.getCatContract().getProduct().equals(CatContract.Product.CU0)) return productProcessorCU0;

        if(contract.getCatContract().getProduct().equals(CatContract.Product.BA0)) return productProcessorBA0;

        if(contract.getCatContract().getProduct().equals(CatContract.Product.MP0)) return productProcessorMP0;

        if(contract.getCatContract().getProduct().equals(CatContract.Product.TP0)) return productProcessorTP0;

        if(contract.getCatContract().getProduct().equals(CatContract.Product.TP2)) return productProcessorTP2;

        if(contract.getCatContract().getProduct().equals(CatContract.Product.RU0)) return productProcessorRU0;

        throw new UnsupportedOperationException("cant load proper processor for product (" + contract.getCatContract().getProduct().toString() + ") . Maybe you testing new product? Then just create ProductProcessor realization.");
    }


    public void dummyRealization(){
        throw new UnsupportedOperationException("You cant use ProductProcessor to update contract. Use some specific interface (ProductProcessorCU0, etc) ");
    }

    @Override
    public void updateContract(Contract contract, Boolean managerMode) throws NoEntityException, CalculatePremiumException, NoPersonToContractException, BadContractDataException, NoRelatedContractNumber {
        dummyRealization();
    }

    @Override
    public void updateContract(Contract contract, MultipartFile file, ContractImage contractImage, Boolean managerMode) throws NoEntityException, CalculatePremiumException, NoPersonToContractException, BadContractDataException, NoRelatedContractNumber {
        dummyRealization();
    }

    @Override
    public BigDecimal getIntegralInsuredSumForContract(Contract contract) {
        dummyRealization();
        return null;
    }

    @Override
    public BigDecimal getAllPremiumForContract(Contract contract) throws CalculatePremiumException {
        dummyRealization();
        return null;
    }

    @Override
    public BigDecimal getFullCost(Contract contract) {
        dummyRealization();
        return null;
    }

    @Override
    public BigDecimal getAllPremiumForContractWithoutSaving(Contract contract, List<InsuranceArea> insuranceAreas) throws CalculatePremiumException {
        dummyRealization();
        return null;
    }

    @Override
    public BigDecimal getIntegralInsuredSumForContractWithoutSaving(Contract contract, List<InsuranceArea> insuranceAreas) {
        dummyRealization();
        return null;
    }

    @Override
    public Integer getMaxAgeForMale() {
        dummyRealization();
        return null;
    }

    @Override
    public Integer getMaxAgeForFemale() {
        dummyRealization();
        return null;
    }

    @Override
    public List<Risk> getRisksSellerCanUse(Contract contract) {
        dummyRealization();
        return null;
    }

    @Override
    public List<Risk> getRiskSomeSellerCanUse(CatContract catContract, Partner partner) {
        dummyRealization();
        return null;
    }

    @Override
    public void addPremiumToContract(Contract contract, ContractPremium contractPremium) {
        dummyRealization();
    }

    @Override
    public List<Risk> getRisksMustBeAddedToContract(Contract contract) {
        dummyRealization();
        return null;
    }

    @Override
    public String getPossibleValues(Risk risk) {
        dummyRealization();
        return null;
    }

    @Override
    public Date printContract(Contract c) {
        dummyRealization();
        return null;
    }
}
