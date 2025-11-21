package org.udesa.giftcards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.udesa.giftcards.model.EntityDrawer;
import org.udesa.giftcards.model.GiftCard;
import org.udesa.giftcards.model.ModelServiceTest;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CardServiceTest extends ModelServiceTest<GiftCard, CardService> {

    private static final int INITIAL_BALANCE = 20;
    private static final int UPDATED_BALANCE = 25;
    private static final int CHARGE_AMOUNT = 5;
    private static final String OWNER = "Alice";
    private static final String CHARGE_DESC = "UnCargo";
    private static final String INVALID_CODE = "InvalidCardCode";

    @Override protected GiftCard newSample() {
        return EntityDrawer.someGiftCard(INITIAL_BALANCE);
    }

    @Override protected GiftCard updateUser(GiftCard card) {
        card.setBalance(UPDATED_BALANCE);
        return card;
    }

    @Test public void findByCodeReturnsTheCard() {
        GiftCard card = savedSample();
        assertEquals(card, service.findByCode(card.getCode()));
    }

    @Test public void redeemAssignsOwner() {
        GiftCard card = savedSample();

        service.redeem(card.getCode(), OWNER);

        assertTrue(service.findByCode(card.getCode()).owned());
    }

    @Test public void chargePersistsBalanceAndCharge() {
        GiftCard card = savedSample();

        service.redeem(card.getCode(), OWNER);
        service.charge(card.getCode(), CHARGE_AMOUNT, CHARGE_DESC);

        GiftCard reloaded = service.findByCode(card.getCode());
        assertEquals(INITIAL_BALANCE - CHARGE_AMOUNT, reloaded.balance());
        assertEquals(CHARGE_DESC, reloaded.charges().getLast());
    }

    @Test public void invalidCodeThrows() {
        assertThrows(RuntimeException.class, () -> service.findByCode(INVALID_CODE));
    }
}
