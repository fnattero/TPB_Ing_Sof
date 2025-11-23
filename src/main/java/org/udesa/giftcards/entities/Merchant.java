package org.udesa.giftcards.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
public class Merchant extends ModelEntity {

    @Column(unique = true)
    private String code;

    @Column(nullable = false)
    private String description;

    public Merchant() {
    }

    public Merchant(String code, String description) {
        this.code = code;
        this.description = description;
    }

    protected boolean same( Object o ) { return this.code.equals( getClass().cast( o ).getCode() ); }
}
