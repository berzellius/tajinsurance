package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by berz on 10.10.14.
 */
@Entity(name = "CatContractPartnerFranchise")
@Table(name = "cc_partner_franchise")
public class CatContractPartnerFranchise implements Serializable {
    public CatContractPartnerFranchise() {
    }


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "cc_partner_franchise_id_generator")
    @SequenceGenerator(name = "cc_partner_franchise_id_generator", sequenceName = "cc_partner_franchise_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    @JoinColumn(name = "cc_id")
    @OneToOne
    private CatContract catContract;

    @JoinColumn(name = "partner_id")
    @OneToOne
    private Partner partner;

    @Column(name = "franchise_percent")
    private Integer franchisePercent;

    @Column(name = "franchise_money")
    private BigDecimal franchiseMoney;

    private BigDecimal discount;

    @Transient
    private BigDecimal discountPercent;

    @PostLoad
    public void setDiscPercent(){
        if(this.getDiscount() != null) this.setDiscountPercent(this.getDiscount().multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_DOWN));
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

    public Integer getFranchisePercent() {
        return franchisePercent;
    }

    public void setFranchisePercent(Integer franchisePercent) {
        this.franchisePercent = franchisePercent;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }


    @Override
    public String toString(){
        if(this.getFranchisePercent() != null)
            return this.franchisePercent.toString() + "% / " + this.discount.toString();
        else if(this.getFranchiseMoney() != null)
            return this.getFranchiseMoney() + " / " + this.getDiscount().toString();
        else return "";
    }

    @Override
    public boolean equals(Object obj){
        return this.getId().equals(((CatContractPartnerFranchise) obj).getId()) &&
                obj instanceof CatContractPartnerFranchise;
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal getFranchiseMoney() {
        return franchiseMoney;
    }

    public void setFranchiseMoney(BigDecimal franchiseMoney) {
        this.franchiseMoney = franchiseMoney;
    }
}
