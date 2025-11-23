package org.udesa.giftcards.model;

import org.udesa.giftcards.entities.Charge;
import org.udesa.giftcards.entities.GiftCard;
import org.udesa.giftcards.entities.Merchant;
import org.udesa.giftcards.entities.UserVault;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public final class EntityDrawer {

    private static final AtomicInteger SEQ = new AtomicInteger((int) Instant.now().getEpochSecond());

    private EntityDrawer() {}

    private static String nextSuffix() {
        return String.valueOf(SEQ.incrementAndGet());
    }

    public static UserVault someUser() {
        return new UserVault("User" + nextSuffix(), "Pass" + nextSuffix());
    }

    public static Merchant someMerchant() {
        return new Merchant("M_" + nextSuffix(), "Merchant " + nextSuffix());
    }

    public static GiftCard someGiftCard() {
        return someGiftCard(100);
    }

    public static GiftCard someGiftCard(int initialBalance) {
        return new GiftCard("GC" + nextSuffix(), initialBalance);
    }

    public static GiftCard someRedeemedCard(String owner) {
        GiftCard card = someGiftCard();
        card.redeem(owner);
        return card;
    }

    public static GiftCard someRedeemedCard(String owner, int initialBalance) {
        GiftCard card = someGiftCard(initialBalance);
        card.redeem(owner);
        return card;
    }

    public static GiftCard someChargedCard(String owner, int amount) {
        GiftCard card = someRedeemedCard(owner);
        card.charge(amount, "Charge " + nextSuffix());
        return card;
    }

    public static Charge someCharge(GiftCard card) {
        Charge charge = new Charge("Charge " + nextSuffix());
        charge.setCard(card);
        return charge;
    }

    public static UserSession someSession(String user, Clock clock) {
        return new UserSession(user, clock);
    }
}
