package com.tajinsurance.domain;

/**
 * Created by berz on 14.09.14.
 */

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

import java.util.List;

@Entity(name = "InsuranceArea")
@Table(name = "insurance_area")
public class InsuranceArea implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "insurance_area_id_generator")
    @SequenceGenerator(name = "insurance_area_id_generator", sequenceName = "insurance_area_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    private String name;

    private String address;

    @JoinColumn(name ="c_id")
    @NotNull
    @OneToOne
    private Contract contract;

    @OneToMany(mappedBy = "insuranceArea",fetch = FetchType.LAZY)
    private List<InsuranceObject> insuranceObjectList;

    @ManyToMany
    @JoinTable(
            name = "security_systems_to_insurance_area",
            joinColumns = {
                    @JoinColumn(name = "insurance_area_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "security_system_id")
            }
    )
    private List<SecuritySystem> securitySystems;

    public InsuranceArea() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    @Override
    public String toString(){
        return getAddress();
    }

    @Override
    public boolean equals(Object obj){
        return this.getId().equals(((InsuranceArea) obj).getId())
                && obj instanceof InsuranceArea;
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    public List<InsuranceObject> getInsuranceObjectList() {
        return insuranceObjectList;
    }

    public void setInsuranceObjectList(List<InsuranceObject> insuranceObjectList) {
        this.insuranceObjectList = insuranceObjectList;
    }

    public List<SecuritySystem> getSecuritySystems() {
        return securitySystems;
    }

    public void setSecuritySystems(List<SecuritySystem> securitySystems) {
        this.securitySystems = securitySystems;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
