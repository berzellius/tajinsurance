package com.tajinsurance.domain;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
/**
 * Created by berz on 01.12.14.
 */
@Entity
@Table(name = "currency_exchange")
public class CurrencyExchange {



    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "currency_exchange_id_generator")
    @SequenceGenerator(name = "currency_exchange_id_generator", sequenceName = "currency_exchange_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    @OneToOne
    @JoinColumn(name = "curr_id")
    private Currency currency;

    private BigDecimal course;

    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private Date date;

    public CurrencyExchange() {
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

    public BigDecimal getCourse() {
        return course;
    }

    public void setCourse(BigDecimal course) {
        this.course = course;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString(){
        return this.course.toString();
    }

    @Override
    public boolean equals(Object obj){
        return this.getId().equals(
                ((CurrencyExchange) obj).getId()
        ) && obj instanceof CurrencyExchange;
    }

    @Override
    public int hashCode(){
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }
}
