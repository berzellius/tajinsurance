package com.tajinsurance.dto;

import com.tajinsurance.domain.Person;

import java.text.SimpleDateFormat;

/**
 * Created by berz on 17.06.14.
 */
public class PersonContentObjectWithDates extends PersonContentObject {

    public String born;

    public String docDate;

    public Long id;


    public PersonContentObjectWithDates() {
    }

    public PersonContentObjectWithDates(Person p) {
        super(p);

        if (p.getBorn() != null) {
            born = new SimpleDateFormat("dd.MM.yyyy").format(p.getBorn());
        }

        if (p.getDocDate() != null) {
            docDate = new SimpleDateFormat("dd.MM.yyyy").format(p.getDocDate());
        }
    }
}
