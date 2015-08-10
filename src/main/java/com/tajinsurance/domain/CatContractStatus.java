package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Created by berz on 22.03.14.
 */
@Entity(name = "CatContractStatus")
@Table(name = "cat_contract_status")
public class CatContractStatus implements Localizable,Serializable {

    public CatContractStatus() {
    }

    public enum StatusCode{
        NEW,
        CANCELLED,
        ACCEPTED,
        BEGIN,
        WAIT_PHOTO,
        WAIT_BIMA
    }


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "cat_contract_status_id_generator")
    @SequenceGenerator(name = "cat_contract_status_id_generator", sequenceName = "cat_contract_status_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Transient
    public String value;

    @Column(name = "code")
    @Enumerated(EnumType.STRING)
    @NotNull
    private StatusCode code;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @OneToMany(mappedBy = "catContractStatus", fetch = FetchType.LAZY)
    private List<CatContractStatusLocaleEntity> localeEntityList;


    @Override
    public List<CatContractStatusLocaleEntity> getLocaleEntityList() {
        return this.localeEntityList;
    }

    @Override
    public void setLocaleEntityList(List<? extends LocaleEntity> localeEntityList) {
        this.localeEntityList = (List<CatContractStatusLocaleEntity>) localeEntityList;
    }

    @Override
    public String toString(){
        return getValue();
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof CatContractStatus && getId().equals(((CatContractStatus) obj).getId());
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    public StatusCode getCode() {
        return code;
    }

    public void setCode(StatusCode code) {
        this.code = code;
    }
}
