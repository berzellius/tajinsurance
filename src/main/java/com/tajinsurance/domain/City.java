package com.tajinsurance.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by berz on 04.11.14.
 */
@Entity(name = "City")
@Table(name = "city")
public class City  implements Serializable {

    @Id
    private Long id;

    private String label;

    private String value;

    public City() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString(){
        return getValue();
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof City && getId().equals(((City) obj).getId());
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }
}
