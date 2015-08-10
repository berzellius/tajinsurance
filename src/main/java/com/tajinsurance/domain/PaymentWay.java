package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by berz on 10.05.14.
 */
@Entity
@Table(name = "payment_way")
public class PaymentWay implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "payment_way_id_generator")
    @SequenceGenerator(name = "payment_way_id_generator", sequenceName = "payment_way_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private String way;

    public String getWay() {
        return way;
    }

    public void setWay(String way) {
        this.way = way;
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof PaymentWay && getId().equals(((PaymentWay) obj).getId());
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    @Override
    public String toString(){
        return way;
    }
}
