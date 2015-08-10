package com.tajinsurance.dto;

import java.math.BigDecimal;

/**
 * Created by berz on 29.03.14.
 */
public class RiskAjax {

    public String getName(){ return this.name; }
    public Long getRiskId(){ return this.riskId; }

    public String name;
    public Long riskId;
    public BigDecimal rate;

    public RiskAjax() {
    }

    public RiskAjax(String n, Long id, BigDecimal r) {
        this.name = n;
        this.riskId = id;
        this.rate = r;
    }

    @Override
    public String toString(){
        return this.name + ": " + this.riskId.toString() + ", " + this.rate.toString();
    }
}
