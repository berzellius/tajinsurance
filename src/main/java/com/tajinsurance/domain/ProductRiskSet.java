package com.tajinsurance.domain;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Created by berz on 23.11.14.
 */
@Entity(name = "ProductRiskSet")
@Table(name = "product_risk_sets")
public class ProductRiskSet implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "product_risk_sets_id_generator")
    @SequenceGenerator(name = "product_risk_sets_id_generator", sequenceName = "product_risk_sets_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    private String name;

    @OneToOne
    @JoinColumn(name = "cc_id")
    private CatContract catContract;

    @OneToOne
    @JoinColumn(name = "partner_id")
    private Partner partner;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_risks_in_sets",
            joinColumns = {@JoinColumn(name = "set_id")},
            inverseJoinColumns = {@JoinColumn(name = "risk_id")}
    )
    private List<Risk> risks;

    private Boolean deleted;

    public ProductRiskSet() {
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

    public CatContract getCatContract() {
        return catContract;
    }

    public void setCatContract(CatContract catContract) {
        this.catContract = catContract;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public List<Risk> getRisks() {
        return risks;
    }

    public void setRisks(List<Risk> risks) {
        this.risks = risks;
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    @Override
    public boolean equals(Object obj){
        return this.getId().equals(
                ((ProductRiskSet) obj).getId()
        ) && obj instanceof ProductRiskSet;
    }

    @Override
    public String toString(){
        return this.getName();
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
