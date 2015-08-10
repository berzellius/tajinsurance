package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by berz on 04.03.14.
 */
@Entity(name = "LocaleEntity")
@Table(name = "locale")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "entity", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue(value = "LocaleEntity")
public abstract class LocaleEntity implements Serializable {

    protected LocaleEntity(){

    }



    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "locale_id_generator")
    @SequenceGenerator(name = "locale_id_generator", sequenceName = "locale_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    //private Long entity_id;

    @NotNull
    protected String locale;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LocaleEntity && getId() == ((LocaleEntity) obj).getId();
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    /*public Long getEntity_id() {
        return entity_id;
    }

    public void setEntity_id(Long entity_id) {
        this.entity_id = entity_id;
    }*/
}
