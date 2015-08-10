package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

import java.math.BigDecimal;

/**
 * Created by berz on 04.11.14.
 */
@Entity(name = "PartnerRiskCorrelation")
@Table(name = "partner_risk_correlation")
public class PartnerRiskCorrelation implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "partner_risk_correlation_id_generator")
    @SequenceGenerator(name = "partner_risk_correlation_id_generator", sequenceName = "partner_risk_correlation_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    @JoinColumn(name = "partner_id")
    @OneToOne
    private Partner partner;

    @JoinColumn(name = "cc_id")
    @OneToOne
    private CatContract catContract;

    @JoinColumn(name = "risk_id")
    @OneToOne
    private Risk risk;

    private BigDecimal correlation;

    @Column(name = "extra_info")
    private String extraInfo;

    @Override
    public boolean equals(Object obj){
        return this.getId().equals(((PartnerRiskCorrelation) obj).getId()) &&
                obj instanceof PartnerRiskCorrelation;
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    @Override
    public String toString(){
        return getId().toString();
    }

    public PartnerRiskCorrelation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
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

    public BigDecimal getCorrelation() {
        return correlation;
    }

    public void setCorrelation(BigDecimal correlation) {
        this.correlation = correlation;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }
}
