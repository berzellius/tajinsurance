package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Created by berz on 12.09.14.
 */
@Entity(name = "ProductMoneyProperty")
@Table(name = "product_property")
public class ProductMoneyProperty implements ProductProperty {

    public ProductMoneyProperty() {
    }

    public PropertyType getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(PropertyType propertyName) {
        this.propertyName = propertyName;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public BigDecimal getMoneyValue() {
        return moneyValue;
    }

    public void setMoneyValue(BigDecimal moneyValue) {
        this.moneyValue = moneyValue;
    }

    public Boolean getUseProperty() {
        return useProperty;
    }

    public void setUseProperty(Boolean useProperty) {
        this.useProperty = useProperty;
    }

    public enum PropertyType{
        MP0_money_level_1,
        MP0_money_level_2,
        MP0_security_k,
        MP0_refund_payment_k,
        MP0_franchise_k,
        correlation,
        fixed_insurance_sum,
        TP2_use_extended_pack,
        TP0_D_franchise_sum,
        TP0_D_franchise_discount_percent,
        RU0_insured_sum,
        RU0_premium
    }

    public enum Source{
        PARTNER,
        GLOBAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "product_property_id_generator")
    @SequenceGenerator(name = "product_property_id_generator", sequenceName = "product_property_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    @Transient
    private Integer version;

    @JoinColumn(name = "cc_id")
    @OneToOne
    private CatContract catContract;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    @Column(name = "name")
    private PropertyType propertyName;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "source")
    private Source source;

    @Column(name = "value")
    private BigDecimal moneyValue;

    @Column(name = "use_property")
    private Boolean useProperty;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public CatContract getCatContract() {
        return catContract;
    }

    public void setCatContract(CatContract catContract) {
        this.catContract = catContract;
    }

    @Override
    public String toString(){
        return this.getPropertyName().toString();
    }

    @Override
    public boolean equals(Object obj){
        return this.getId().equals(((ProductMoneyProperty) obj).getId()) &&
                obj instanceof ProductMoneyProperty;
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }
}
