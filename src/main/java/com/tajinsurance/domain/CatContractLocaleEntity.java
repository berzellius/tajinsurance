package com.tajinsurance.domain;

import javax.persistence.*;

/**
 * Created by berz on 04.03.14.
 */
@Entity
@Table
@DiscriminatorValue(value = "CatContractLocaleEntity")
public class CatContractLocaleEntity extends LocaleEntity {
    public CatContractLocaleEntity(){

    }

    public CatContractLocaleEntity(String locale){
        this.locale = locale;
    }


    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "cc_id")
    private CatContract catContract;

    private String value;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String val) {
        this.value = val;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CatContractLocaleEntity && getId().equals(((CatContractLocaleEntity) obj).getId());
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    @Override
    public String toString(){
        return getName() + " - " + getValue();
    }


    public CatContract getCatContract() {
        return catContract;
    }

    public void setCatContract(CatContract catContract) {
        this.catContract = catContract;
    }
}
