package org.udesa.giftcards.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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

    @BeforeEach
    void setUp() {
        Mockito.reset(clock);
        when(clock.now()).thenReturn(LocalDateTime.now());

        cardService.deleteAll();
        userService.deleteAll();
        merchantService.deleteAll();

        cardService.save(new GiftCard("GC1", 10));
        cardService.save(new GiftCard("GC2", 5));

        userService.save(new UserVault("Bob", "BobPass"));
        userService.save(new UserVault("Kevin", "KevPass"));

        merchantService.save(new Merchant("M1", "Merchant 1"));
    }

    @AfterAll
    void tearDown() {
        cardService.deleteAll();
        userService.deleteAll();
        merchantService.deleteAll();
    }


    @Test public void userCanOpenASession() {
        assertNotNull(facade.login("Bob", "BobPass"));
    }

    @Test public void unkownUserCannorOpenASession() {
        assertThrows(RuntimeException.class, () -> facade.login("Stuart", "StuPass"));
    }

    @Test public void userCannotUseAnInvalidtoken() {
        assertThrows(RuntimeException.class, () -> facade.redeem(UUID.randomUUID(), "GC1"));
        assertThrows(RuntimeException.class, () -> facade.balance(UUID.randomUUID(), "GC1"));
        assertThrows(RuntimeException.class, () -> facade.details(UUID.randomUUID(), "GC1"));
    }

    @Test public void userCannotCheckOnAlienCard() {
        UUID token = facade.login("Bob", "BobPass");

        assertThrows(RuntimeException.class, () -> facade.balance(token, "GC1"));
    }

    @Test public void userCanRedeeemACard() {
        UUID token = facade.login("Bob", "BobPass");

        facade.redeem(token, "GC1");
        assertEquals(10, facade.balance(token, "GC1"));
    }

    @Test public void userCanRedeeemASecondCard() {
        UUID token = facade.login("Bob", "BobPass");

        facade.redeem(token, "GC1");
        facade.redeem(token, "GC2");

        assertEquals(10, facade.balance(token, "GC1"));
        assertEquals(5, facade.balance(token, "GC2"));
    }

    @Test public void multipleUsersCanRedeeemACard() {
        UUID bobsToken = facade.login("Bob", "BobPass");
        UUID kevinsToken = facade.login("Kevin", "KevPass");

        facade.redeem(bobsToken, "GC1");
        facade.redeem(kevinsToken, "GC2");

        assertEquals(10, facade.balance(bobsToken, "GC1"));
        assertEquals(5, facade.balance(kevinsToken, "GC2"));
    }

    @Test public void unknownMerchantCantCharge() {
        assertThrows(RuntimeException.class, () -> facade.charge("Mx", "GC1", 2, "UnCargo"));
    }

    @Test public void merchantCantChargeUnredeemedCard() {
        assertThrows(RuntimeException.class, () -> facade.charge("M1", "GC1", 2, "UnCargo"));
    }

    @Test public void merchantCanChargeARedeemedCard() {
        UUID token = facade.login("Bob", "BobPass");

        facade.redeem(token, "GC1");
        facade.charge("M1", "GC1", 2, "UnCargo");

        assertEquals(8, facade.balance(token, "GC1"));
    }

    @Test public void merchantCannotOverchargeACard() {
        UUID token = facade.login("Bob", "BobPass");

        facade.redeem(token, "GC1");
        assertThrows(RuntimeException.class, () -> facade.charge("M1", "GC1", 11, "UnCargo"));
    }

    @Test public void userCanCheckHisEmptyCharges() {
        UUID token = facade.login("Bob", "BobPass");

        facade.redeem(token, "GC1");

        assertTrue(facade.details(token, "GC1").isEmpty());
    }

    @Test public void userCanCheckHisCharges() {
        UUID token = facade.login("Bob", "BobPass");

        facade.redeem(token, "GC1");
        facade.charge("M1", "GC1", 2, "UnCargo");

        assertEquals("UnCargo", facade.details(token, "GC1").getLast());
    }

    @Test public void userCannotCheckOthersCharges() {
        facade.redeem(facade.login("Bob", "BobPass"), "GC1");

        UUID token = facade.login("Kevin", "KevPass");

        assertThrows(RuntimeException.class, () -> facade.details(token, "GC1"));
    }

    @Test public void tokenExpires() {
        LocalDateTime now = LocalDateTime.now();
        when(clock.now()).thenReturn(now, now.plusMinutes(16), now.plusMinutes(16));

        UUID token = facade.login("Kevin", "KevPass");

        assertThrows(RuntimeException.class, () -> facade.redeem(token, "GC1"));
    }
}
