package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Created by berz on 19.09.14.
 */
@Entity
@Table(name = "contract_image")
public class ContractImage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "contract_image_id_generator")
    @SequenceGenerator(name = "contract_image_id_generator", sequenceName = "contract_image_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    public ContractImage() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JoinColumn(name = "c_id")
    @ManyToOne
    private Contract contract;

    private String path;

    private String extension;

    private String description;

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    @Override
    public boolean equals(Object obj){
        return this.getId().equals(((ContractImage) obj).getId()) &&
                obj instanceof ContractImage;
    }

    @Override
    public String toString(){
        return contract.toString() + " image: " + this.getPath();
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
