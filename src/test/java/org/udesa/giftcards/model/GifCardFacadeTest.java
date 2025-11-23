package org.udesa.giftcards.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.udesa.giftcards.entities.GiftCard;
import org.udesa.giftcards.entities.Merchant;
import org.udesa.giftcards.entities.UserVault;
import org.udesa.giftcards.service.GiftCardService;
import org.udesa.giftcards.service.MerchantService;
import org.udesa.giftcards.service.UserService;

@SpringBootTest
public class GifCardFacadeTest {

    @Autowired private GifCardFacade facade;
    @Autowired private GiftCardService giftCardService;
    @Autowired private MerchantService merchantService;
    @Autowired private UserService userService;
    @MockBean private Clock clock;

    public static final String invalidUser = "Stuart";
    public static final String invalidPassword = "StuPass";
    private final String invalidGiftCardCode = "invalidGiftCardCode";
    private final String invalidMerchantCode = "invalidMerchantCode";
    private final String wrongPassword = "WrongPass";

    UserVault user;
    UserVault user2;
    GiftCard giftCard;
    GiftCard giftCard2;
    Merchant merchant;

    @BeforeEach public void beforeEach() {
        when( clock.now() ).then( it -> LocalDateTime.now() );
        when( clock.today() ).then( it -> LocalDate.now() );
        user = savedUser();
        user2 = savedUser();
        giftCard = savedGiftCard1();
        giftCard2 = savedGiftCard2();
        merchant = savedMerchant();
    }

    @AfterAll
    public void tearDown() {
        giftCardService.deleteItemWithPrefix("GC");
        userService.deleteItemWithPrefix("User");
        merchantService.deleteItemWithPrefix("M_");
    }

    @Test public void userCanOpenASession() {
        assertNotNull(login(user));
    }

    @Test public void unkownUserCannorOpenASession() {
        assertThrows(RuntimeException.class, () -> facade.login(invalidUser, invalidPassword));
    }

    @Test public void userCannotLoginWithWrongPassword() {
        assertThrows(RuntimeException.class, () -> facade.login(user.getName(), wrongPassword));
    }

    @Test public void userCannotUseAnInvalidtoken() {
        assertThrows(RuntimeException.class, () -> facade.redeem(UUID.randomUUID(), giftCard.getCode()));
        assertThrows(RuntimeException.class, () -> facade.balance(UUID.randomUUID(), giftCard.getCode()));
        assertThrows(RuntimeException.class, () -> facade.details(UUID.randomUUID(), giftCard.getCode()));
    }

    @Test public void userCannotCheckOnAlienCard() {
        UUID token = login(user);
        assertThrows(RuntimeException.class, () -> facade.balance(token, giftCard.getCode()));
    }

    @Test public void userCanRedeeemACard() {
        UUID token = loginAndRedeem(user, giftCard);
        assertEquals(10, facade.balance(token, giftCard.getCode()));
    }

    @Test public void userCanRedeeemASecondCard() {
        UUID token = login(user);
        facade.redeem(token, giftCard.getCode());
        facade.redeem(token, giftCard2.getCode());
        assertEquals(10, facade.balance(token, giftCard.getCode()));
        assertEquals(5, facade.balance(token, giftCard2.getCode()));
    }

    @Test public void multipleUsersCanRedeeemACard() {
        UUID userToken = loginAndRedeem(user, giftCard);
        UUID secondUserToken = loginAndRedeem(user2, giftCard2);
        assertEquals(10, facade.balance(userToken, giftCard.getCode()));
        assertEquals(5, facade.balance(secondUserToken, giftCard2.getCode()));
    }

    @Test public void unknownMerchantCantCharge() {
        assertThrows(RuntimeException.class, () -> facade.charge(invalidMerchantCode, giftCard.getCode(), 2, "UnCargo"));
    }

    @Test public void merchantCantChargeUnredeemedCard() {
        assertThrows(RuntimeException.class, () -> facade.charge(merchant.getCode(), invalidGiftCardCode, 2, "UnCargo"));
    }

    @Test public void merchantCanChargeARedeemedCard() {
        UUID token = loginAndRedeem(user, giftCard);
        facade.charge(merchant.getCode(), giftCard.getCode(), 2, "UnCargo");
        assertEquals(8, facade.balance(token, giftCard.getCode()));
    }

    @Test public void merchantCannotOverchargeACard() {
        loginAndRedeem(user, giftCard);
        assertThrows(RuntimeException.class, () -> facade.charge(merchant.getCode(), giftCard.getCode(), 11, "UnCargo"));
    }

    @Test public void userCanCheckHisEmptyCharges() {
        UUID token = loginAndRedeem(user, giftCard);
        assertTrue(facade.details(token, giftCard.getCode()).isEmpty());
    }

    @Test public void userCanCheckHisCharges() {
        UUID token = loginAndRedeem(user, giftCard);
        facade.charge(merchant.getCode(), giftCard.getCode(), 2, "UnCargo");
        assertEquals("UnCargo", facade.details(token, giftCard.getCode()).getLast());
    }

    @Test public void userCannotCheckOthersCharges() {
        facade.redeem(login(user), giftCard.getCode());
        UUID token = login(user2);
        assertThrows(RuntimeException.class, () -> facade.details(token, giftCard.getCode()));
    }

    @Test public void tokenExpires() {
        LocalDateTime now = LocalDateTime.now();
        when(clock.now()).thenReturn(now, now.plusMinutes(16), now.plusMinutes(16));
        UUID token = facade.login(user.getName(), user.getPassword());
        assertThrows(RuntimeException.class, () -> facade.redeem(token, giftCard.getCode()));
    }

    private UserVault savedUser() {
        return userService.save(EntityDrawer.someUser());
    }

    private Merchant savedMerchant() {
        return merchantService.save(EntityDrawer.someMerchant());
    }

    private GiftCard savedGiftCard1() {
        return giftCardService.save(EntityDrawer.someGiftCard(10));
    }

    private GiftCard savedGiftCard2() {
        return giftCardService.save(EntityDrawer.someGiftCard(5));
    }

    private UUID login(UserVault user) {
        return facade.login(user.getName(), user.getPassword());
    }

    private UUID loginAndRedeem(UserVault user, GiftCard giftCard) {
        UUID token = login(user);
        facade.redeem(token, giftCard.getCode());
        return token;
    }
}
