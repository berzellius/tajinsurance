package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Created by berz on 03.12.14.
 */
@Entity(name = "PartnerRiskPolicy")
@Table(name = "partner_risk_policy")
public class PartnerRiskPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "cc_partner_risk_policy_id_generator")
    @SequenceGenerator(name = "cc_partner_risk_policy_id_generator", sequenceName = "cc_partner_risk_policy_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    @OneToOne
    @JoinColumn(name = "cc_id")
    private CatContract catContract;

    @OneToOne
    @JoinColumn(name = "partner_id")
    private Partner partner;

    @OneToOne
    @JoinColumn(name = "risk_id")
    private Risk risk;

    @Column(name = "fixed_insured_sum")
    private BigDecimal fixedInsuredSum;

    @Column(name = "use_fixed_sums")
    private Boolean useFixedSums;

    @Enumerated(EnumType.STRING)
    private Policy policy;

    public PartnerRiskPolicy() {
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

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public BigDecimal getFixedInsuredSum() {
        return fixedInsuredSum;
    }

    public void setFixedInsuredSum(BigDecimal fixedInsuredSum) {
        this.fixedInsuredSum = fixedInsuredSum;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public Risk getRisk() {
        return risk;
    }

    public void setRisk(Risk risk) {
        this.risk = risk;
    }

    public Boolean getUseFixedSums() {
        return useFixedSums;
    }

    public void setUseFixedSums(Boolean useFixedSums) {
        this.useFixedSums = useFixedSums;
    }

    public enum Policy{
        ALLOWED,
        FIXED;
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof PartnerRiskPolicy && getId().equals(((PartnerRiskPolicy) obj).getId());
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    @Override
    public String toString(){
        return policy.toString().concat(" ").concat(fixedInsuredSum.toString());
    }

}
