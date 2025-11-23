package org.udesa.giftcards.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
public class GiftCard extends ModelEntity {
    public static final String CargoImposible = "CargoImposible";
    public static final String InvalidCard = "InvalidCard";

    @Column(unique = true)
    private String code;

    @Column(nullable = false)
    private int balance;

    private String owner;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Charge> charges = new ArrayList<>();

    public GiftCard( String id, int initialBalance ) {
        this.code = id;
        balance = initialBalance;
    }

    public GiftCard() {}

    public GiftCard charge( int anAmount, String description ) {
        if ( !owned() || ( balance - anAmount < 0 ) ) throw new RuntimeException( CargoImposible );

        balance = balance - anAmount;
        Charge charge = new Charge( description );
        charge.setCard( this );
        charges.add( charge );

        return this;
    }

    public GiftCard redeem( String newOwner ) {
        if ( owned() ) throw new RuntimeException( InvalidCard );

        owner = newOwner;
        return this;
    }

    protected boolean same( Object o ) { return this.code.equals( getClass().cast( o ).getCode() ); }
    public boolean owned() {                            return owner != null;                   }
    public boolean isOwnedBy( String aPossibleOwner ) { return owner.equals( aPossibleOwner );  }
    public List<String> charges() { return charges.stream().map( Charge::getDescription ).toList(); }

}
