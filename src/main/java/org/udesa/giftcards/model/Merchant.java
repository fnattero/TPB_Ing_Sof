package org.udesa.giftcards.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.udesa.giftcards.model.ModelEntity;

@Entity
@Table
@Getter
@Setter
public class Merchant extends ModelEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String description;

    public Merchant() {
    }

    public Merchant(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
