package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by berz on 14.09.14.
 */

@Entity(name = "InsuranceObject")
@Table(name = "insurance_object")
public class InsuranceObject implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "insurance_object_id_generator")
    @SequenceGenerator(name = "insurance_object_id_generator", sequenceName = "insurance_object_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    private String name;

    private BigDecimal sum;

    private BigDecimal realsum;

    @JoinColumn(name = "insur_area_id")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private InsuranceArea insuranceArea;

    @JoinColumn(name = "risk_id")
    @OneToOne
    @NotNull
    private Risk risk;

    public InsuranceObject() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InsuranceArea getInsuranceArea() {
        return insuranceArea;
    }

    public void setInsuranceArea(InsuranceArea insuranceArea) {
        this.insuranceArea = insuranceArea;
    }

    public Risk getRisk() {
        return risk;
    }

    public void setRisk(Risk risk) {
        this.risk = risk;
    }

    @Override
    public String toString(){
        return this.getName();
    }

    @Override
    public boolean equals(Object obj){
        return this.getId().equals(((InsuranceObject) obj).getId()) &&
                obj instanceof InsuranceObject;
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public BigDecimal getRealsum() {
        return realsum;
    }

    public void setRealsum(BigDecimal realsum) {
        this.realsum = realsum;
    }
}
