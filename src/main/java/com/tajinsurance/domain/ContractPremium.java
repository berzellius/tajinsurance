package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by berz on 24.03.14.
 */
@Entity
@Table(name = "contract_premium")
public class ContractPremium implements Serializable {
    public ContractPremium() {
    }

    public ContractPremium(Risk risk, Contract contract, BigDecimal sum) {
        this.setRisk(risk);
        this.setContract(contract);
        this.setInsuredSum(sum);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "contract_premium_id_generator")
    @SequenceGenerator(name = "contract_premium_id_generator", sequenceName = "contract_premium_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JoinColumn(name = "c_id")
    @ManyToOne
    private Contract contract;

    @JoinColumn(name = "risk_id")
    @OneToOne
    private Risk risk;


    private BigDecimal insuredSum;

    private BigDecimal premium;

    private boolean validated;

    private boolean deleted;

    @JoinColumn(name = "insobj_id")
    @OneToOne
    private InsuranceObject insuranceObject;

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public Risk getRisk() {
        return risk;
    }

    public void setRisk(Risk risk) {
        this.risk = risk;
    }

    public BigDecimal getInsuredSum() {
        return insuredSum;
    }

    public void setInsuredSum(BigDecimal insuredSum) {
        this.insuredSum = insuredSum;
    }

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString(){
        return getContract() + ", " + getInsuredSum() + ", " + getRisk();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ContractPremium && getId() == ((ContractPremium) obj).getId();
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }


    public InsuranceObject getInsuranceObject() {
        return insuranceObject;
    }

    public void setInsuranceObject(InsuranceObject insuranceObject) {
        this.insuranceObject = insuranceObject;
    }
}
