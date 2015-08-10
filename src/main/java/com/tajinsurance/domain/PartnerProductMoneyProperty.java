package com.tajinsurance.domain;

/**
 * Created by berz on 12.09.14.
 */

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity(name = "PartnerProductMoneyProperty")
@Table(name = "partner_product_property")
public class PartnerProductMoneyProperty implements ProductProperty {


    public PartnerProductMoneyProperty() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "partner_product_property_id_generator")
    @SequenceGenerator(name = "partner_product_property_id_generator", sequenceName = "partner_product_property_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    @Transient
    private Integer version;

    @JoinColumn(name = "partner_id")
    @OneToOne
    private Partner partner;

    @JoinColumn(name = "pmp_id")
    @OneToOne
    private ProductMoneyProperty productMoneyProperty;

    @Column(name = "value")
    private BigDecimal moneyValue;

    @Column(name = "use_property")
    private Boolean useProperty;

    @Column(name = "extra_info")
    private String extraInfo;

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

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public BigDecimal getMoneyValue() {
        return moneyValue;
    }

    public void setMoneyValue(BigDecimal moneyValue) {
        this.moneyValue = moneyValue;
    }

    public ProductMoneyProperty getProductMoneyProperty() {
        return productMoneyProperty;
    }

    public void setProductMoneyProperty(ProductMoneyProperty productMoneyProperty) {
        this.productMoneyProperty = productMoneyProperty;
    }

    @Override
    public String toString(){
        return this.getProductMoneyProperty().getPropertyName().toString() +
                " partner " + this.getPartner().toString() + ": "
                + this.getMoneyValue().toString();
    }

    @Override
    public boolean equals(Object obj){
        return this.getId().equals(((PartnerProductMoneyProperty) obj).getId()) &&
                obj instanceof PartnerProductMoneyProperty;
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }


    public Boolean getUseProperty() {
        return useProperty;
    }

    public void setUseProperty(Boolean useProperty) {
        this.useProperty = useProperty;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }
}
