package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.math.BigDecimal;

/**
 * Created by berz on 24.03.14.
 */
@Entity
@Table(name = "partner")
public class Partner implements Serializable {


    public PaymentAccept getPaymentAccept() {
        return paymentAccept;
    }

    public void setPaymentAccept(PaymentAccept paymentAccept) {
        this.paymentAccept = paymentAccept;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public BigDecimal getCorrelation() {
        return correlation;
    }

    public void setCorrelation(BigDecimal correlation) {
        this.correlation = correlation;
    }

    public enum PaymentAccept{
        PARTNER,
        COMPANY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "partner_id_generator")
    @SequenceGenerator(name = "partner_id_generator", sequenceName = "partner_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    private Boolean removed;

    private String memo;

    private String inn;

    private BigDecimal correlation;

    @Column(name = "payment_accept")
    @Enumerated(EnumType.STRING)
    private PaymentAccept paymentAccept;

    public Partner() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    protected String value;

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Transient
    private Integer version;

    @ManyToMany
    @JoinTable(
            name = "partner_cat_contract",
            joinColumns = {
                    @JoinColumn(name = "partner_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "cat_contract_id")
            }
    )
    protected List<CatContract> catContracts;

    public List<CatContract> getCatContracts() {
        return this.catContracts;
    }

    public void setCatContracts(List<CatContract> catContracts) {
        this.catContracts = catContracts;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getRemoved() {
        return removed;
    }

    public void setRemoved(Boolean removed) {
        this.removed = removed;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Partner && getId().equals(((Partner) obj).getId());
    }
}
