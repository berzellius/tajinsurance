package com.tajinsurance.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by berz on 18.11.14.
 */
@Entity
@Table(name = "rec_number_generations")
public class ReceiptNumberGeneration implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "rec_number_generations_id_generator")
    @SequenceGenerator(name = "rec_number_generations_id_generator", sequenceName = "rec_number_generations_id_seq")
    @NotNull
    @Column(updatable = false, columnDefinition = "bigint")
    private Long id;

    private Integer generation;

    public ReceiptNumberGeneration() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getGeneration() {
        return generation;
    }

    public void setGeneration(Integer generation) {
        this.generation = generation;
    }
}
