package org.udesa.giftcards.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class GiftCardTest {

    private static final String CARD_CODE = "GC1";
    private static final int INITIAL_BALANCE = 10;
    private static final int CHARGE_AMOUNT = 2;
    private static final int OVERCHARGE_AMOUNT = 11;
    private static final String OWNER = "Bob";
    private static final String CHARGE_DESC = "Un cargo";

    @Test public void aSimpleCard() {
        assertEquals( INITIAL_BALANCE, newCard().balance() );
    }

    @Test public void aSimpleIsNotOwnedCard() {
        assertFalse( newCard().owned() );
    }

    @Test public void cannotChargeUnownedCards() {
        GiftCard aCard = newCard();
        assertThrows( RuntimeException.class, () -> aCard.charge( CHARGE_AMOUNT, CHARGE_DESC ) );
        assertEquals( INITIAL_BALANCE, aCard.balance() );
        assertTrue( aCard.charges().isEmpty() );
    }

    @Test public void chargeACard() {
        GiftCard aCard = newCard();
        aCard.redeem( OWNER );
        aCard.charge( CHARGE_AMOUNT, CHARGE_DESC );
        assertEquals( INITIAL_BALANCE - CHARGE_AMOUNT, aCard.balance() );
        assertEquals( CHARGE_DESC, aCard.charges().getLast() );
    }

    @Test public void cannotOverrunACard() {
        GiftCard aCard = newCard();
        assertThrows( RuntimeException.class, () -> aCard.charge( OVERCHARGE_AMOUNT, CHARGE_DESC ) );
        assertEquals( INITIAL_BALANCE, aCard.balance() );
    }

    private GiftCard newCard() {
        return new GiftCard( CARD_CODE, INITIAL_BALANCE );
    }

}
