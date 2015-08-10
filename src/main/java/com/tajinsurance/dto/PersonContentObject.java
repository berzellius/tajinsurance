package com.tajinsurance.dto;

import com.tajinsurance.domain.Person;

/**
 * Created by berz on 17.06.14.
 */
public class PersonContentObject extends DomainObjectContentImpl {

    public String surname;

    public String name;

    public String middle;

    public Person.Sex sex;

    public String docSeries;

    public String docNumber;

    public String docDepartment;

    public String city;

    public String indexRegistr;

    public String addrRegistr;

    public String indexResident;

    public String addrResident;

    public String phoneHome;

    public String phoneMob;

    public String phoneWork;

    public String email;

    public PersonContentObject() {
    }

    public PersonContentObject(Person p) {
        setFields(p);
    }
}
