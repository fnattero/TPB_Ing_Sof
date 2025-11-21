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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.udesa.giftcards.service.CardService;
import org.udesa.giftcards.service.MerchantService;
import org.udesa.giftcards.service.UserService;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GifCardFacadeTest {

    @Autowired private GifCardFacade facade;
    @Autowired private CardService cardService;
    @Autowired private MerchantService merchantService;
    @Autowired private UserService userService;
    @MockBean private Clock clock;

    private final String invalidGiftCardCode = "invalidGiftCardCode";
    private final String invalidMerchantCode = "invalidMerchantCode";
    private final String wrongPassword = "WrongPass";

    @BeforeEach public void beforeEach() {
        when( clock.now() ).then( it -> LocalDateTime.now() );
        when( clock.today() ).then( it -> LocalDate.now() );
    }

    @AfterAll
    void tearDown() {
        cardService.deleteAll();
        userService.deleteAll();
        merchantService.deleteAll();
    }

    private UserVault savedUser() {
        return userService.save(EntityDrawer.someUser());
    }

    private Merchant savedMerchant() {
        return merchantService.save(EntityDrawer.someMerchant());
    }

    private GiftCard savedGiftCard1() {
        return cardService.save(EntityDrawer.someGiftCard(10));
    }

    private GiftCard savedGiftCard2() {
        return cardService.save(EntityDrawer.someGiftCard(5));
    }

    private UUID login(UserVault user) {
        return facade.login(user.getName(), user.getPassword());
    }

    private UUID loginAndRedeem(UserVault user, GiftCard giftCard) {
        UUID token = login(user);
        facade.redeem(token, giftCard.getCode());
        return token;
    }

    @Test public void userCanOpenASession() {
        UserVault user = savedUser();
        assertNotNull(login(user));
    }

    @Test public void unkownUserCannorOpenASession() {
        assertThrows(RuntimeException.class, () -> facade.login("Stuart", "StuPass"));
    }

    @Test public void userCannotLoginWithWrongPassword() {
        UserVault user = savedUser();
        assertThrows(RuntimeException.class, () -> facade.login(user.getName(), wrongPassword));
    }

    @Test public void userCannotUseAnInvalidtoken() {
        GiftCard giftCard = savedGiftCard1();
        assertThrows(RuntimeException.class, () -> facade.redeem(UUID.randomUUID(), giftCard.getCode()));
        assertThrows(RuntimeException.class, () -> facade.balance(UUID.randomUUID(), giftCard.getCode()));
        assertThrows(RuntimeException.class, () -> facade.details(UUID.randomUUID(), giftCard.getCode()));
    }

    @Test public void userCannotCheckOnAlienCard() {
        UserVault user = savedUser();
        GiftCard giftCard = savedGiftCard1();
        UUID token = login(user);

        assertThrows(RuntimeException.class, () -> facade.balance(token, giftCard.getCode()));
    }

    @Test public void userCanRedeeemACard() {
        UserVault user = savedUser();
        GiftCard giftCard = savedGiftCard1();
        UUID token = loginAndRedeem(user, giftCard);
        assertEquals(10, facade.balance(token, giftCard.getCode()));
    }

    @Test public void userCanRedeeemASecondCard() {
        UserVault user = savedUser();
        GiftCard giftCard = savedGiftCard1();
        GiftCard secondGiftCard = savedGiftCard2();
        UUID token = login(user);

        facade.redeem(token, giftCard.getCode());
        facade.redeem(token, secondGiftCard.getCode());

        assertEquals(10, facade.balance(token, giftCard.getCode()));
        assertEquals(5, facade.balance(token, secondGiftCard.getCode()));
    }

    @Test public void multipleUsersCanRedeeemACard() {
        UserVault user = savedUser();
        UserVault secondUser = savedUser();
        GiftCard giftCard = savedGiftCard1();
        GiftCard secondGiftCard = savedGiftCard2();

        UUID userToken = loginAndRedeem(user, giftCard);
        UUID secondUserToken = loginAndRedeem(secondUser, secondGiftCard);

        assertEquals(10, facade.balance(userToken, giftCard.getCode()));
        assertEquals(5, facade.balance(secondUserToken, secondGiftCard.getCode()));
    }

    @Test public void unknownMerchantCantCharge() {
        GiftCard giftCard = savedGiftCard1();
        assertThrows(RuntimeException.class, () -> facade.charge(invalidMerchantCode, giftCard.getCode(), 2, "UnCargo"));
    }

    @Test public void merchantCantChargeUnredeemedCard() {
        Merchant merchant = savedMerchant();
        assertThrows(RuntimeException.class, () -> facade.charge(merchant.getCode(), invalidGiftCardCode, 2, "UnCargo"));
    }

    @Test public void merchantCanChargeARedeemedCard() {
        UserVault user = savedUser();
        GiftCard giftCard = savedGiftCard1();
        Merchant merchant = savedMerchant();

        UUID token = loginAndRedeem(user, giftCard);
        facade.charge(merchant.getCode(), giftCard.getCode(), 2, "UnCargo");

        assertEquals(8, facade.balance(token, giftCard.getCode()));
    }

    @Test public void merchantCannotOverchargeACard() {
        UserVault user = savedUser();
        GiftCard giftCard = savedGiftCard1();
        Merchant merchant = savedMerchant();

        loginAndRedeem(user, giftCard);

        assertThrows(RuntimeException.class, () -> facade.charge(merchant.getCode(), giftCard.getCode(), 11, "UnCargo"));
    }

    @Test public void userCanCheckHisEmptyCharges() {
        UserVault user = savedUser();
        GiftCard giftCard = savedGiftCard1();

        UUID token = loginAndRedeem(user, giftCard);

        assertTrue(facade.details(token, giftCard.getCode()).isEmpty());
    }

    @Test public void userCanCheckHisCharges() {
        UserVault user = savedUser();
        GiftCard giftCard = savedGiftCard1();
        Merchant merchant = savedMerchant();

        UUID token = loginAndRedeem(user, giftCard);

        facade.charge(merchant.getCode(), giftCard.getCode(), 2, "UnCargo");

        assertEquals("UnCargo", facade.details(token, giftCard.getCode()).getLast());
    }

    @Test public void userCannotCheckOthersCharges() {
        UserVault user = savedUser();
        UserVault secondUser = savedUser();
        GiftCard giftCard = savedGiftCard1();

        facade.redeem(login(user), giftCard.getCode());

        UUID token = login(secondUser);

        assertThrows(RuntimeException.class, () -> facade.details(token, giftCard.getCode()));
    }

    @Test public void tokenExpires() {
        LocalDateTime now = LocalDateTime.now();
        when(clock.now()).thenReturn(now, now.plusMinutes(16), now.plusMinutes(16));

        UserVault user = savedUser();
        GiftCard giftCard = savedGiftCard1();

        UUID token = facade.login(user.getName(), user.getPassword());

        assertThrows(RuntimeException.class, () -> facade.redeem(token, giftCard.getCode()));
    }
}
