package com.tajinsurance.domain;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import com.tajinsurance.domain.City;

/**
 * Created by berz on 24.03.14.
 */
@Entity
@Table(name = "person")
public class Person implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "person_id_generator")
    @SequenceGenerator(name = "person_id_generator", sequenceName = "person_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    public Person() {
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityResident() {
        return cityResident;
    }

    public void setCityResident(String cityResident) {
        this.cityResident = cityResident;
    }

    public enum Sex {
        MALE,
        FEMALE
    }

    /*@Version*/
    @Column(name = "version")
    private Integer version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private String surname;

    private String name;

    private String middle;

    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private Date born;

    @Column(name = "sex")
    @Enumerated(EnumType.STRING)
    @NotNull
    private Sex sex;

    @Column(name = "doc_series")
    private String docSeries;

    @Column(name = "doc_number")
    private String docNumber;

    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @Column(name = "doc_date")
    private Date docDate;

    @Column(name = "doc_department")
    private String docDepartment;

    @Column(name = "city")
    private String city;

    @Column(name = "city_res")
    private String cityResident;

    @Column(name = "index_regisrt")
    private String indexRegistr;

    @Column(name = "addr_registr")
    private String addrRegistr;

    @Column(name = "index_resident")
    private String indexResident;

    @Column(name = "addr_resident")
    private String addrResident;

    @Column(name = "phone_home")
    private String phoneHome;

    @Column(name = "phone_mod")
    private String phoneMob;

    @Column(name = "phone_work")
    private String phoneWork;

    private String email;

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMiddle() {
        return middle;
    }

    public void setMiddle(String middle) {
        this.middle = middle;
    }

    public Date getBorn() {
        return born;
    }

    public void setBorn(Date born) {
        this.born = born;
    }

    public String getDocSeries() {
        return docSeries;
    }

    public void setDocSeries(String docSeries) {
        this.docSeries = docSeries;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public Date getDocDate() {
        return docDate;
    }

    public void setDocDate(Date docDate) {
        this.docDate = docDate;
    }

    public String getDocDepartment() {
        return docDepartment;
    }

    public void setDocDepartment(String docDepartment) {
        this.docDepartment = docDepartment;
    }

    public String getIndexRegistr() {
        return indexRegistr;
    }

    public void setIndexRegistr(String indexRegistr) {
        this.indexRegistr = indexRegistr;
    }

    public String getAddrRegistr() {
        return addrRegistr;
    }

    public void setAddrRegistr(String addrRegistr) {
        this.addrRegistr = addrRegistr;
    }

    public String getIndexResident() {
        return indexResident;
    }

    public void setIndexResident(String indexResident) {
        this.indexResident = indexResident;
    }

    public String getAddrResident() {
        return addrResident;
    }

    public void setAddrResident(String addrResident) {
        this.addrResident = addrResident;
    }

    public String getPhoneHome() {
        return phoneHome;
    }

    public void setPhoneHome(String phoneHome) {
        this.phoneHome = phoneHome;
    }

    public String getPhoneMob() {
        return phoneMob;
    }

    public void setPhoneMob(String phoneMob) {
        this.phoneMob = phoneMob;
    }

    public String getPhoneWork() {
        return phoneWork;
    }

    public void setPhoneWork(String phoneWork) {
        this.phoneWork = phoneWork;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge(){
        return getAge(new Date());
    }

    public int getAge(Date dt) {
        Calendar dob = Calendar.getInstance();
        if (getBorn() == null) return 0;
        dob.setTime(getBorn());
        Calendar today = Calendar.getInstance();
        if (dt != null) today.setTime(dt);
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
            age--;
        } else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
            age--;
        }

        return age;
    }

    public String getFio(){
        return this.getSurname() + " " + this.getName() + " " + this.getMiddle();
    }

    public String getFioWithBorn(){
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy");
        return this.getFio() + " " + (this.getBorn() != null ? f.format(this.getBorn()) : "");
    }

    @Override
    public String toString() {
        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy");
        String s = this.getSurname() + " " + this.getName() + " " + this.getMiddle(); //+ " " + (this.getBorn() != null ? f.format(this.getBorn()) : "");
               /* + ", " + (this.getBorn() != null ? f.format(this.getBorn()) : "") + "; ";
        s += this.getDocSeries() + " № " + this.getDocNumber() + " " + this.getDocDepartment() + " / " + (this.getDocDate() != null?  f.format(this.getDocDate()) : "");*/
        return s;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Person && getId().equals(((Person) obj).getId());
    }
}
