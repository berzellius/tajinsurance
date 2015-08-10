package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by berz on 27.03.14.
 */
@Entity
@Table(name = "cat_contract_risk")
public class CatContractRisk implements Serializable {
    public CatContractRisk() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "cat_contract_risk_id_generator")
    @SequenceGenerator(name = "cat_contract_risk_id_generator", sequenceName = "cat_contract_risk_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    @OneToOne
    @JoinColumn(name = "cat_contract_id")
    private CatContract catContract;

    @OneToOne
    @JoinColumn(name = "risk_id")
    private Risk risk;

    private BigDecimal rate;

    @Column(name = "month_tarif")
    private BigDecimal monthTarif;


    @Column(name = "min_sum")
    private BigDecimal minSum;

    @Column(name = "max_sum")
    private BigDecimal maxSum;

    @Column(name = "fixed_premium")
    private BigDecimal fixedPremium;

    @Column(name = "length")
    private Integer length;

    @OneToOne
    @JoinColumn(name = "partner")
    private Partner partner;

    private BigDecimal correlation;

    private Boolean mandatory;

    @Override
    public boolean equals(Object obj){
        return obj instanceof CatContractRisk && getId().equals(((CatContractRisk) obj).getId());
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public CatContract getCatContract() {
        return catContract;
    }

    public void setCatContract(CatContract catContract) {
        this.catContract = catContract;
    }

    public Risk getRisk() {
        return risk;
    }

    public void setRisk(Risk risk) {
        this.risk = risk;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getMonthTarif() {
        return monthTarif;
    }

    public void setMonthTarif(BigDecimal monthTarif) {
        this.monthTarif = monthTarif;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public BigDecimal getMinSum() {
        return minSum;
    }

    public void setMinSum(BigDecimal minSum) {
        this.minSum = minSum;
    }

    public BigDecimal getMaxSum() {
        return maxSum;
    }

    public void setMaxSum(BigDecimal maxSum) {
        this.maxSum = maxSum;
    }

    public BigDecimal getCorrelation() {
        return correlation;
    }

    public void setCorrelation(BigDecimal correlation) {
        this.correlation = correlation;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public BigDecimal getFixedPremium() {
        return fixedPremium;
    }

    public void setFixedPremium(BigDecimal fixedPremium) {
        this.fixedPremium = fixedPremium;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }
}
