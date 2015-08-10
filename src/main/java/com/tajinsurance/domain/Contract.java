package com.tajinsurance.domain;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
@Entity
public class Contract implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "contract_id_generator")
    @SequenceGenerator(name = "contract_id_generator", sequenceName = "contract_id_seq")
    @Column(name = "id")
    private Long id;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     */
    @Transient
    private String c_number;

    @Column(name = "c_number_counter")
    private Integer cNumberCounter;

    @Column(name = "related_contract")
    private String relatedContractNumber;

    /**
     */
    private String c_memo;

    private boolean deleted;

    @Column(name = "print_claim_flag")
    private Boolean printClaimFlag;

    @Column(name = "premium_reveived_flag")
    private Boolean premiumReceived;

    @Version
    @Column(name = "version")
    private Integer version;

    @JoinColumn(name = "cc_id")
    @OneToOne
    private CatContract catContract;

    @JoinColumn(name = "ccs_id")
    @OneToOne
    private CatContractStatus catContractStatus;

    @JoinColumn(name = "partner_id")
    @OneToOne
    private Partner partner;

    @JoinColumn(name = "creator_id")
    @OneToOne
    private User creator;

    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @Column(name = "print_date")
    private Date printDate;

    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @Column(name = "app_date")
    private Date appDate;

    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @Column(name = "pay_date")
    private Date payDate;

    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @Column(name = "start_date")
    private Date startDate;

    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @Column(name = "end_date")
    private Date endDate;

    @JoinColumn(name = "payment_way_id")
    @OneToOne
    private PaymentWay paymentWay;

    @JoinColumn(name = "person_id")
    @OneToOne
    private Person person;

    private Integer length;

    private BigDecimal sum;

    /**
     * Номер квитанции
     */
    @Column(name = "rec_number")
    private Integer receiptNumber;

    /**
     * В какой месяц была распечатана квитанция. ГГГГММ - Integer
     */
    @Column(name = "rec_month")
    private Integer receiptMonth;

    /**
     * Административно установленная страховая сумма
     */
    @Column(name = "insured_sum_adm")
    private BigDecimal insuredSumAdm;


    /**
     * Административно установленная премия
     */
    @Column(name = "premium_adm")
    private BigDecimal premiumAdm;

    @Column(name = "ins_cost_adm")
    private BigDecimal insuranceCostAdm;


    @Column(name = "insured_sum_info")
    private String insuredSumInfo;

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY)
    private List<ContractPremium> contractPremiums;

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY)
    private List<ContractImage> contractImages;

    @Column(name = "claim_signed")
    private Boolean claimSigned;

    // выплата страховки
    @Enumerated(value = EnumType.STRING)
    @Column(name = "payment_type")
    private PaymentType paymentType;

    private Integer franchise;

    @Column(name = "email_sent")
    private Boolean emailSent;

    @OneToOne
    @JoinColumn(name = "risk_set_id")
    private ProductRiskSet productRiskSet;

    @OneToOne
    @JoinColumn(name = "curr_id")
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "insurance_territory")
    private InsuranceTerritory insuranceTerritory;

    private Boolean schengen;

    private Boolean worldwide;

    private Boolean sport;

    @Column(name = "tp2_extended")
    private Boolean tp2Extended;

    private BigDecimal currencyExchange;

    public InsuranceTerritory getInsuranceTerritory() {
        return insuranceTerritory;
    }

    public void setInsuranceTerritory(InsuranceTerritory insuranceTerritory) {
        this.insuranceTerritory = insuranceTerritory;
    }

    public Boolean getSchengen() {
        return schengen;
    }

    public void setSchengen(Boolean shengen) {
        this.schengen = shengen;
    }

    public Boolean getWorldwide() {
        return worldwide;
    }

    public void setWorldwide(Boolean worldwide) {
        this.worldwide = worldwide;
    }

    public Boolean getSport() {
        return sport;
    }

    public void setSport(Boolean sport) {
        this.sport = sport;
    }

    public Boolean getTp2Extended() {
        return tp2Extended;
    }

    public void setTp2Extended(Boolean tp2Extended) {
        this.tp2Extended = tp2Extended;
    }

    public BigDecimal getCurrencyExchange() {
        return currencyExchange;
    }

    public void setCurrencyExchange(BigDecimal currencyExchange) {
        this.currencyExchange = currencyExchange;
    }

    public enum InsuranceTerritory{
        TERRITORY1,
        TERRITORY2,
        TERRITORY3
    }

    public Contract() {
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public Integer getFranchise() {
        return franchise;
    }

    public void setFranchise(Integer franchise) {
        this.franchise = franchise;
    }

    public List<ContractImage> getContractImages() {
        return contractImages;
    }

    public void setContractImages(List<ContractImage> contractImages) {
        this.contractImages = contractImages;
    }

    public Boolean getEmailSent() {
        return emailSent;
    }

    public void setEmailSent(Boolean emailSent) {
        this.emailSent = emailSent;
    }

    public BigDecimal getInsuranceCostAdm() {
        return insuranceCostAdm;
    }

    public void setInsuranceCostAdm(BigDecimal insuranceCostAdm) {
        this.insuranceCostAdm = insuranceCostAdm;
    }

    public ProductRiskSet getProductRiskSet() {
        return productRiskSet;
    }

    public void setProductRiskSet(ProductRiskSet productRiskSet) {
        this.productRiskSet = productRiskSet;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public enum PaymentType{
        PROPORTIONAL(BigDecimal.ONE),
        BY_FIRST_RISK(BigDecimal.valueOf(1.3));

        private BigDecimal moneyMult;

        PaymentType(BigDecimal moneyMult){
            this.moneyMult = moneyMult;
        }

        public BigDecimal getMoneyMult(){
            return this.moneyMult;
        }
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<ContractPremium> getContractPremiums() {
        return contractPremiums;
    }

    public void setContractPremiums(List<ContractPremium> contractPremiums) {
        this.contractPremiums = contractPremiums;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public Boolean isPrintClaimFlag() {
        return printClaimFlag;
    }

    public void setPrintClaimFlag(Boolean printClaimFlag) {
        this.printClaimFlag = printClaimFlag;
    }


    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof Contract && getId().equals(((Contract) obj).getId());
    }

    @PostLoad
    public void setContractNum(){
        if(this.getPartner() != null && this.getcNumberCounter() != null){
            this.setC_number(getContractNum(this.getPartner().getId(),  this.getcNumberCounter().longValue()));
        }
    }

    private String digits(long dg, int length) {
        String res = Long.toString(dg);
        int l = res.length();

        for (int i = 0; i < length - l; i++) res = "0" + res;

        return res;
    }

    private String getContractNum(long idPartner, long pcc) {
        String d1 = digits(idPartner, 2);
        return d1 + digits(pcc, 10 - d1.length());
    }

    public Boolean getClaimSigned() {
        return claimSigned;
    }

    public void setClaimSigned(Boolean claimSigned) {
        this.claimSigned = claimSigned;
    }

    public PaymentWay getPaymentWay() {
        return paymentWay;
    }

    public void setPaymentWay(PaymentWay paymentWay) {
        this.paymentWay = paymentWay;
    }

    public String getRelatedContractNumber() {
        return relatedContractNumber;
    }

    public void setRelatedContractNumber(String relatedContractNumber) {
        this.relatedContractNumber = relatedContractNumber;
    }

    public String getInsuredSumInfo() {
        return insuredSumInfo;
    }

    public void setInsuredSumInfo(String insuredSumInfo) {
        this.insuredSumInfo = insuredSumInfo;
    }

    public BigDecimal getInsuredSumAdm() {
        return insuredSumAdm;
    }

    public void setInsuredSumAdm(BigDecimal insuredSumAdm) {
        this.insuredSumAdm = insuredSumAdm;
    }

    public BigDecimal getPremiumAdm() {
        return premiumAdm;
    }

    public void setPremiumAdm(BigDecimal premiumAdm) {
        this.premiumAdm = premiumAdm;
    }

    public Integer getcNumberCounter() {
        return cNumberCounter;
    }

    public void setcNumberCounter(Integer cNumberCounter) {
        this.cNumberCounter = cNumberCounter;
    }

    public Boolean getPremiumReceived() {
        return premiumReceived;
    }

    public void setPremiumReceived(Boolean premiumReceived) {
        this.premiumReceived = premiumReceived;
    }

    public Integer getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(Integer receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public Integer getReceiptMonth() {
        return receiptMonth;
    }

    public void setReceiptMonth(Integer receiptMonth) {
        this.receiptMonth = receiptMonth;
    }

    public String getC_number() {
        if(this.c_number == null) this.setContractNum();

        return this.c_number;
    }

    public void setC_number(String c_number) {
        this.c_number = c_number;
    }

    public String getC_memo() {
        return this.c_memo;
    }

    public void setC_memo(String c_memo) {
        this.c_memo = c_memo;
    }

    public CatContract getCatContract() {
        return this.catContract;
    }

    public void setCatContract(CatContract catContract) {
        this.catContract = catContract;
    }

    public CatContractStatus getCatContractStatus() {
        return this.catContractStatus;
    }

    public void setCatContractStatus(CatContractStatus catContractStatus) {
        this.catContractStatus = catContractStatus;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public Date getPrintDate() {
        return printDate;
    }

    public void setPrintDate(Date printDate) {
        this.printDate = printDate;
    }

    public Date getAppDate() {
        return appDate;
    }

    public void setAppDate(Date appDate) {
        this.appDate = appDate;
    }

    public Date getPayDate() {
        return payDate;
    }

    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }
}
