package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by berz on 02.04.14.
 */
@Entity
@Table(name = "currency")
public class Currency  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "currency_id_generator")
    @SequenceGenerator(name = "currency_id_generator", sequenceName = "currency_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    public Currency() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private String val;

    private String name;

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return getVal();
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof Currency && getId().equals(((Currency) obj).getId());
    }
}
