package com.tajinsurance.dto;

import java.math.BigDecimal;

/**
 * Created by berz on 30.03.14.
 */
public class ContractPremiumAjax {


    public BigDecimal insuredSum;
    public BigDecimal premium;
    public Long premiumId;
    public String risk;

    public ContractPremiumAjax(String risk, BigDecimal insuredSum, BigDecimal premium, Long premiumId) {
        this.risk = risk;
        this.insuredSum = insuredSum;
        this.premium = premium;
        this.premiumId = premiumId;
    }

    public ContractPremiumAjax() {
    }

    public String getRisk(){
        return this.risk;
    }

    public Long getPremiumId(){
        return this.premiumId;
    }

    public BigDecimal getInsuredSum(){
        return this.insuredSum;
    }

    public BigDecimal getPremium(){
        return this.premium;
    }
}
