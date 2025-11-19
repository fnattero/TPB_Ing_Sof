package org.udesa.giftcards.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.udesa.tuslibros.model.ModelEntity;

@Entity
@Table(name = "gift_cards")
public class GiftCard extends ModelEntity {
    public static final String CargoImposible = "CargoImposible";
    public static final String InvalidCard = "InvalidCard";

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "balance", nullable = false)
    private int balance;

    @Column(name = "owner")
    private String owner;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gift_card_charges", joinColumns = @JoinColumn(name = "gift_card_id"))
    @Column(name = "description")
    private List<String> charges = new ArrayList<>();

    public GiftCard( String id, int initialBalance ) {
        this.code = id;
        balance = initialBalance;
    }

    public GiftCard() {}

    public GiftCard charge( int anAmount, String description ) {
        if ( !owned() || ( balance - anAmount < 0 ) ) throw new RuntimeException( CargoImposible );

        balance = balance - anAmount;
        charges.add( description );

        return this;
    }

    public GiftCard redeem( String newOwner ) {
        if ( owned() ) throw new RuntimeException( InvalidCard );

        owner = newOwner;
        return this;
    }

    // proyectors
    public boolean owned() {                            return owner != null;                   }
    public boolean isOwnedBy( String aPossibleOwner ) { return owner != null && owner.equals( aPossibleOwner );  }

    // accessors
    public String id() {            return code;      }
    public int balance() {          return balance; }
    public List<String> charges() { return charges; }

}
