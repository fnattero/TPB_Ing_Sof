package org.udesa.giftcards.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.udesa.giftcards.model.EntityDrawer;
import org.udesa.giftcards.entities.GiftCard;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GiftCardServiceTest extends ModelServiceTest<GiftCard, GiftCardService> {

    public static int initialGiftCardBalance = 10;
    public static String giftCardOwner = "giftCardOwner";
    public static String giftCardOwner2 = "giftCardOwner2";
    public static int chargeAmount = 3;
    public static String chargeDescription = "Charge Description 1";
    public static String invalidCardCode = "invalidCardCode";

    protected GiftCard updateSample( GiftCard sampleGiftCard ) {
        sampleGiftCard.setCode("Test code");
        return sampleGiftCard;
    }

    protected GiftCard newSample() {
        return EntityDrawer.someGiftCard(initialGiftCardBalance);
    }

    GiftCard card;

    @BeforeEach
    public void beforeEach() {
        card = savedSample();
    }

    @AfterAll
    public void tearDown() {
        service.deleteItemWithPrefix("GC");
    }

    @Test
    public void testFindByCode() {
        assertEquals(card, service.findByCode( card.getCode()));
    }

    @Test public void testDoublefind() {
        service.findByCode( card.getCode() );
        assertEquals(card, service.findByCode( card.getCode()));
        service.findByCode( card.getCode() );
        assertEquals(card, service.findByCode( card.getCode()));
    }

    @Test public void findFailsWithInvalidCode() {
        assertThrows( RuntimeException.class, () -> service.findByCode( invalidCardCode));
    }

    @Test public void redeemAssignsOwner() {
        assertFalse(service.findByCode(card.getCode()).owned());
        service.redeem(card.getCode(), giftCardOwner);
        assertTrue(service.findByCode(card.getCode()).owned());
        assertEquals(giftCardOwner, service.findByCode(card.getCode()).getOwner());
    }

    @Test public void redeemTwiceFails() {
        assertFalse(service.findByCode(card.getCode()).owned());
        service.redeem(card.getCode(), giftCardOwner);
        assertThrows(RuntimeException.class, () -> service.redeem(card.getCode(), giftCardOwner));
    }

    @Test public void redeemFailsWithInvalidCode() {
        assertThrows(RuntimeException.class, () -> service.redeem(invalidCardCode, giftCardOwner));
    }

    @Test public void redeemFailsWithRedeemedCard() {
        service.redeem(card.getCode(), giftCardOwner);
        assertThrows(RuntimeException.class, () -> service.redeem(card.getCode(), giftCardOwner2));
        assertEquals(giftCardOwner, service.findByCode(card.getCode()).getOwner());
    }

    @Test public void chargePersistsBalanceAndCharge() {
        service.redeem(card.getCode(), giftCardOwner);
        service.charge(card.getCode(), chargeAmount, chargeDescription);
        GiftCard reloaded = service.findByCode(card.getCode());
        assertEquals(initialGiftCardBalance - chargeAmount, reloaded.getBalance());
        assertEquals(chargeDescription, reloaded.charges().getLast());
    }

    @Test public void doubleChargePersistsBalanceAndCharge() {
        service.redeem(card.getCode(), giftCardOwner);
        service.charge(card.getCode(), chargeAmount, chargeDescription);
        GiftCard reloaded = service.findByCode(card.getCode());
        assertEquals(initialGiftCardBalance - chargeAmount, reloaded.getBalance());
        assertEquals(chargeDescription, reloaded.charges().getLast());
        service.charge(card.getCode(), chargeAmount, chargeDescription);
        GiftCard reloaded2 = service.findByCode(card.getCode());
        assertEquals(initialGiftCardBalance - chargeAmount - chargeAmount, reloaded2.getBalance());
        assertEquals(chargeDescription, reloaded2.charges().getLast());
    }

    @Test public void chargeFailsWithInvalidCardCode() {
        assertThrows(RuntimeException.class, () -> service.charge(invalidCardCode, chargeAmount, chargeDescription));
    }

    @Test public void chargeFailsWithInvalidChargeAmount() {
        service.redeem(card.getCode(), giftCardOwner);
        assertThrows(RuntimeException.class, () -> service.charge(card.getCode(), initialGiftCardBalance + 1, chargeDescription));
        assertEquals(initialGiftCardBalance, service.findByCode(card.getCode()).getBalance());
    }

    @Test public void chargeFailsWithUnredeemedCard() {
        assertThrows(RuntimeException.class, () -> service.charge(card.getCode(), chargeAmount, chargeDescription));
        assertEquals(initialGiftCardBalance, service.findByCode(card.getCode()).getBalance());
    }

    @Test public void deleteItemWithPrefixDeletesUser() {
        service.deleteItemWithPrefix("GC");
        assertThrows(RuntimeException.class, () -> service.findByCode(card.getCode()));
    }
}
