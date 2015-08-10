package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Created by berz on 10.09.14.
 */
@Entity
@Table(name = "type_of_risk")
public class TypeOfRisk {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "type_of_risk_id_generator")
    @SequenceGenerator(name = "type_of_risk_id_generator", sequenceName = "type_of_risk_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    @Transient
    private Integer version;

    private String value;


    public TypeOfRisk() {
    }

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString(){
        return this.getValue();
    }


    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    @Override
    public boolean equals(Object obj){
        return getId().equals(((TypeOfRisk) obj).getId()) && obj instanceof TypeOfRisk;
    }
}
