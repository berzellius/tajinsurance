package com.tajinsurance.domain;

import javax.persistence.*;

/**
 * Created by berz on 22.03.14.
 */
@Entity
@Table
@DiscriminatorValue(value = "CatContractStatusLocaleEntity")
public class CatContractStatusLocaleEntity extends LocaleEntity {

    public CatContractStatusLocaleEntity() {
    }

    public CatContractStatusLocaleEntity(String locale) {
        this.locale = locale;
    }

    protected String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ccs_id")
    protected CatContractStatus catContractStatus;

    public CatContractStatus getCatContractStatus(){
        return this.catContractStatus;
    }

    public void setCatContractStatus(CatContractStatus catContractStatus){
        this.catContractStatus = catContractStatus;
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof CatContractStatusLocaleEntity && getId().equals(((CatContractStatusLocaleEntity) obj).getId());
    }

}
