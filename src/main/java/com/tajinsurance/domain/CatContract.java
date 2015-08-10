package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Created by berz on 20.03.14.
 */
@Entity(name = "CatContract")
@Table(name = "cat_contract")
public class CatContract implements Localizable,Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "cat_contract_id_generator")
    @SequenceGenerator(name = "cat_contract_id_generator", sequenceName = "cat_contract_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    @Transient
    public String name = "N/A";

    @Transient
    public String value = "N/A";

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public SettingsWay getSettingsWay() {
        return settingsWay;
    }

    public void setSettingsWay(SettingsWay settingsWay) {
        this.settingsWay = settingsWay;
    }

    public List<Currency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies;
    }

    public FixedSumsPolicy getFixedSumsPolicy() {
        return fixedSumsPolicy;
    }

    public void setFixedSumsPolicy(FixedSumsPolicy fixedSumsPolicy) {
        this.fixedSumsPolicy = fixedSumsPolicy;
    }

    public enum Product{
        CU0,
        BA0,
        MP0,
        TP2,
        TP0,
        RU0
    }

    public static enum SettingsWay{
        GLOBAL,
        PARTNER
    }

    public static enum FixedSumsPolicy{
        NONE,
        PARENT_RISKS
    }

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private Product product;

    @JoinColumn(name = "currency_id")
    @OneToOne
    private Currency currency;

    @ManyToMany
    @JoinTable(
            name = "cat_contract_currencies",
            joinColumns = {
                    @JoinColumn(name = "cc_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "curr_id")
            }
    )
    private List<Currency> currencies;


    private Boolean deleted = false;

    @Transient
    private Integer version;

    @OneToMany(mappedBy = "catContract", fetch = FetchType.LAZY)
    private List<CatContractLocaleEntity> localeEntityList;

    @ManyToMany
    @JoinTable(
        name = "cat_contract_to_risk",
        joinColumns = {
                @JoinColumn(name = "cc_id")
        },
        inverseJoinColumns = {
                @JoinColumn(name = "risk_id")
        }
    )
    private List<Risk> risks;

    @Column(name = "min_term")
    private Integer minTerm;

    @JoinColumn(name = "tpr_id")
    @OneToOne
    private TypeOfRisk typeOfRisk;

    @Column(name = "insurance_areas")
    private Boolean useInsuranceAreas;

    @Column(name = "settings_way")
    @Enumerated(value = EnumType.STRING)
    private SettingsWay settingsWay;

    @Column(name = "fixed_sums_policy")
    @Enumerated(value = EnumType.STRING)
    private FixedSumsPolicy fixedSumsPolicy;

    public CatContract() {
    }

    @Override
    public String toString(){
        return getName();
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof CatContract && getId().equals(((CatContract) obj).getId());
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public List<CatContractLocaleEntity> getLocaleEntityList(){
        return localeEntityList;
    }

    @Override
    public void setLocaleEntityList(List<? extends LocaleEntity> localeEntityList) {
        this.localeEntityList = (List<CatContractLocaleEntity>) localeEntityList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public List<Risk> getRisks() {
        return risks;
    }

    public void setRisks(List<Risk> risks) {
        this.risks = risks;
    }


    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Integer getMinTerm() {
        return minTerm;
    }

    public void setMinTerm(Integer minTerm) {
        this.minTerm = minTerm;
    }

    public TypeOfRisk getTypeOfRisk() {
        return typeOfRisk;
    }

    public void setTypeOfRisk(TypeOfRisk typeOfRisk) {
        this.typeOfRisk = typeOfRisk;
    }

    public Boolean getUseInsuranceAreas() {
        return useInsuranceAreas;
    }

    public void setUseInsuranceAreas(Boolean useInsuranceAreas) {
        this.useInsuranceAreas = useInsuranceAreas;
    }
}
