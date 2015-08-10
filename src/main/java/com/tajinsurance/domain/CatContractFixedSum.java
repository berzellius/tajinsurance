package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by berz on 15.07.15.
 */
@Entity(name = "CatContractFixedSum")
@Table(name = "cat_contract_fixed_sum")
public class CatContractFixedSum implements Serializable {

    @Deprecated
    public CatContractFixedSum(){}

    public CatContractFixedSum(CatContract catContract, Risk risk, BigDecimal sum){
        this.setCatContract(catContract);
        this.setRisk(risk);
        this.setSum(sum);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "cat_contract_fixed_sum_id_generator")
    @SequenceGenerator(name = "cat_contract_fixed_sum_id_generator", sequenceName = "cat_contract_fixed_sum_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    @OneToOne
    @JoinColumn(name = "cc_id")
    private CatContract catContract;

    @OneToOne
    @JoinColumn(name = "risk_id")
    private Risk risk;

    private BigDecimal sum;

    @Override
    public String toString(){
        return getCatContract().toString() + ": " + getRisk().toString();
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof CatContractFixedSum && getId().equals(((CatContractFixedSum) obj).getId());
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

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public Risk getRisk() {
        return risk;
    }

    public void setRisk(Risk risk) {
        this.risk = risk;
    }
}
