package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by berz on 14.09.14.
 */
@Entity(name = "SecuritySystem")
@Table(name = "security_system")
public class SecuritySystem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "security_system_id_generator")
    @SequenceGenerator(name = "security_system_id_generator", sequenceName = "security_system_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    @Column(name = "tj_name")
    private String tjName;

    private String name;

    public SecuritySystem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj){
        return this.getId().equals(((SecuritySystem) obj).getId()) &&
                obj instanceof SecuritySystem;
    }

    @Override
    public String toString(){
        return this.getName();
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));

        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTjName() {
        return tjName;
    }

    public void setTjName(String tjName) {
        this.tjName = tjName;
    }
}
