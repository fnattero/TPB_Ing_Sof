package org.udesa.giftcards.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
public class Charge extends ModelEntity {
    private String description;

    @ManyToOne
    @JoinColumn(name = "card_id")
    private GiftCard card;

    protected Charge(){}
    public Charge( String description ) {
        this.description = description;
    }

}
