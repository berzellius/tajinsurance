package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity()
@Table(name = "forbidden_rec_numbers")
public class ForbiddenReceiptNumber implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "forbidden_rec_num_id_generator")
    @SequenceGenerator(name = "forbidden_rec_num_id_generator", sequenceName = "forbidden_rec_num_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    private Integer number;

    private Integer month;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof ForbiddenReceiptNumber
                && ((ForbiddenReceiptNumber) obj).getNumber().equals(this.getNumber())
                && ((ForbiddenReceiptNumber) obj).getMonth().equals(this.getMonth());
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

}
