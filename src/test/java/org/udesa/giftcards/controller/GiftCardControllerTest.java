package org.udesa.giftcards.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.udesa.giftcards.model.Clock;
import org.udesa.giftcards.model.EntityDrawer;
import org.udesa.giftcards.entities.GiftCard;
import org.udesa.giftcards.entities.Merchant;
import org.udesa.giftcards.entities.UserVault;
import org.udesa.giftcards.service.GiftCardService;
import org.udesa.giftcards.service.MerchantService;
import org.udesa.giftcards.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GiftCardControllerTest {

    private static final int initialBalance = 10;
    private static final int chargeAmount = 3;
    private static final String badAuthHeader = "badAuthHeader";
    private static final String firstCharge = "Charge1";
    private static final String secondCharge = "Charge2";
    private static final String invalidUser = "invalid user";
    private static final String invalidPassword = "invalid password";
    private static final String invalidCode = "invalid code";
    private static final String invalidMerchant = "invalid merchant";

    @Autowired MockMvc mockMvc;
    @Autowired UserService userService;
    @Autowired MerchantService merchantService;
    @Autowired GiftCardService giftCardService;
    @MockBean Clock clock;

    UserVault user;
    UserVault user2;
    GiftCard card;
    GiftCard card2;
    Merchant merchant;
    UUID token;
    UUID token2;

    @BeforeEach
    public void beforeEach() throws Exception {
        when( clock.now() ).then( it -> LocalDateTime.now() );
        when( clock.today() ).then( it -> LocalDate.now() );
        user = savedUser();
        user2 = savedUser();
        card = savedCard();
        card2 = savedCard();
        merchant = savedMerchant();
        token = UUID.fromString( loginOk( user ) );
        token2 = UUID.fromString( loginOk( user2 ) );
        redeemOk( token, card.getCode() );
    }

    @AfterAll
    public void tearDown() {
        giftCardService.deleteItemWithPrefix("GC");
        userService.deleteItemWithPrefix("User");
        merchantService.deleteItemWithPrefix("M_");

        // ver el swagger
        // ver el h2
    }

    @Test public void loginReturnsAToken() throws Exception {
        assertTrue( token.toString().length() > 0 );
    }

    @Test
    public void loginFailsWithInvalidUsername() throws Exception {
        UserVault invalidUser = new UserVault(GiftCardControllerTest.invalidUser, user.getPassword());
        loginFails( invalidUser );
    }

    @Test
    public void loginFailsWithInvalidPassword() throws Exception {
        UserVault invalidUser = new UserVault(user.getName(), invalidPassword);
        loginFails( invalidUser );
    }

    @Test public void redeemUsesBearerToken() throws Exception {
        assertEquals( user.getName(), giftCardService.findByCode( card.getCode() ).getOwner() );
    }

    @Test public void redeemTwiceWorks() throws Exception {
        redeemOk( token, card2.getCode() );
        assertEquals( user.getName(), giftCardService.findByCode( card.getCode() ).getOwner() );
        assertEquals( user.getName(), giftCardService.findByCode( card2.getCode() ).getOwner() );
    }

    @Test public void redeemFailsWithoutBearerHeader() throws Exception {
        mockMvc.perform( post( "/" + card.getCode() + "/redeem" )
                                 .header( "Authorization", badAuthHeader) )
                .andDo( print() )
                .andExpect( status().is( 500 ) );
    }

    @Test public void redeemFailsWithInvalidCardId() throws Exception {
        redeemFails( token, invalidCode);
    }

    @Test public void redeemFailsWithInvalidToken() throws Exception {
        redeemFails( UUID.randomUUID(), card.getCode() );
    }

    @Test public void balanceReflectsCharges() throws Exception {
        chargeOk( merchant.getCode(), card.getCode(), chargeAmount, "UnCargo" );

        assertEquals( initialBalance - chargeAmount, balanceOf( token, card.getCode() ) );
    }

    @Test public void balanceFailsWIthInvalidToken() throws Exception {
        chargeOk( merchant.getCode(), card.getCode(), chargeAmount, "UnCargo" );
        balanceFails( UUID.randomUUID(), card.getCode() );
    }

    @Test public void balanceFailsWithInvalidCardId() throws Exception {
        chargeOk( merchant.getCode(), card.getCode(), chargeAmount, "UnCargo" );
        balanceFails(token, invalidCode);
    }

    @Test public void detailsReturnCharges() throws Exception {
        chargeOk( merchant.getCode(), card.getCode(), 1, firstCharge);
        chargeOk( merchant.getCode(), card.getCode(), 1, secondCharge);

        List<String> details = detailsOk( token, card.getCode() );

        assertEquals(firstCharge, details.getFirst() );
        assertEquals(secondCharge, details.getLast() );
        assertEquals( 2, details.size() );
    }

    @Test public void detailsReturnChargesMultipleUsers() throws Exception {
        redeemOk(token2, card2.getCode());
        chargeOk( merchant.getCode(), card.getCode(), 1, firstCharge);
        chargeOk( merchant.getCode(), card.getCode(), 1, secondCharge);
        chargeOk( merchant.getCode(), card2.getCode(), 1, firstCharge);

        List<String> details = detailsOk( token, card.getCode() );
        List<String> details2 = detailsOk( token2, card2.getCode() );

        assertEquals(firstCharge, details.getFirst() );
        assertEquals(secondCharge, details.getLast() );
        assertEquals( 2, details.size() );

        assertEquals(firstCharge, details2.getFirst() );
        assertEquals( 1, details2.size() );
    }

    @Test public void detailsReturnsEmptyCharg() throws Exception {
        assertTrue(detailsOk( token, card.getCode() ).isEmpty() );
    }

    @Test public void detailsFailsWithInvalidToken() throws Exception {
        chargeOk( merchant.getCode(), card.getCode(), 1, firstCharge);
        detailsFails( UUID.randomUUID(), card.getCode() );
    }

    @Test public void detailsFailsWithInvalidCardId() throws Exception {
        chargeOk( merchant.getCode(), card.getCode(), 1, firstCharge);
        detailsFails( token, invalidCode);
    }

    @Test public void chargeFailsWithInvalidMerchant() throws Exception {
        chargeFails( invalidMerchant, card.getCode(), 1, firstCharge);
    }

    @Test public void chargeFailsWithInvalidCardId() throws Exception {
        chargeFails( merchant.getCode(), invalidCode, 1, firstCharge);
    }

    @Test public void chargeFailsWithInvalidAmount() throws Exception {
        chargeFails( merchant.getCode(), card.getCode(), initialBalance + 1, firstCharge);
    }

    private UserVault savedUser() {
        return userService.save( EntityDrawer.someUser() );
    }

    private GiftCard savedCard() {
        return giftCardService.save( EntityDrawer.someGiftCard(initialBalance) );
    }

    private Merchant savedMerchant() {
        return merchantService.save( EntityDrawer.someMerchant() );
    }

    private String loginOk( UserVault user ) throws Exception {
        String response = mockMvc.perform( post( "/login" )
                                                   .param( "user", user.getName() )
                                                   .param( "pass", user.getPassword() ) )
                                 .andDo( print() )
                                 .andExpect( status().is( 200 ) )
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString();

        return new ObjectMapper().readValue( response, HashMap.class ).get( "token" ).toString();
    }

    private void loginFails( UserVault user ) throws Exception {
        mockMvc.perform( post( "/login" )
                                 .param( "user", user.getName() )
                                 .param( "pass", user.getPassword() ) )
                .andDo( print() )
                .andExpect( status().is( 500 ) );
    }

    private void redeemOk( UUID token, String cardId ) throws Exception {
        mockMvc.perform( post( "/" + cardId + "/redeem" )
                                 .header( "Authorization", bearerOf( token ) ) )
                .andDo( print() )
                .andExpect( status().is( 200 ) );
    }

    private void redeemFails( UUID token, String cardId ) throws Exception {
        mockMvc.perform( post( "/" + cardId + "/redeem" )
                                 .header( "Authorization", bearerOf( token ) ) )
                .andDo( print() )
                .andExpect( status().is( 500 ) );
    }

    private int balanceOf( UUID token, String cardId ) throws Exception {
        String response = mockMvc.perform( get( "/" + cardId + "/balance" )
                                                   .header( "Authorization", bearerOf( token ) ) )
                                 .andDo( print() )
                                 .andExpect( status().is( 200 ) )
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString();

        return (Integer) new ObjectMapper().readValue( response, HashMap.class ).get( "balance" );
    }

    private void balanceFails( UUID token, String cardId ) throws Exception {
        mockMvc.perform( get( "/" + cardId + "/balance" )
                                .header( "Authorization", bearerOf( token ) ) )
                .andDo( print() )
                .andExpect( status().is( 500 ) );
    }

    private List<String> detailsOk( UUID token, String cardId ) throws Exception {
        String response = mockMvc.perform( get( "/" + cardId + "/details" )
                                                   .header( "Authorization", bearerOf( token ) ) )
                                 .andDo( print() )
                                 .andExpect( status().is( 200 ) )
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString();

        return (List<String>) new ObjectMapper().readValue( response, HashMap.class ).get( "details" );
    }

    private void detailsFails( UUID token, String cardId ) throws Exception {
        mockMvc.perform( get( "/" + cardId + "/details" )
                                .header( "Authorization", bearerOf( token ) ) )
                .andDo( print() )
                .andExpect( status().is( 500 ) );
    }

    private void chargeOk( String merchant, String cardId, int amount, String description ) throws Exception {
        mockMvc.perform( post( "/" + cardId + "/charge" )
                                 .param( "merchant", merchant )
                                 .param( "amount", "" + amount )
                                 .param( "description", description ) )
                .andDo( print() )
                .andExpect( status().is( 200 ) );
    }

    private void chargeFails( String merchant, String cardId, int amount, String description ) throws Exception {
        mockMvc.perform( post( "/" + cardId + "/charge" )
                                 .param( "merchant", merchant )
                                 .param( "amount", "" + amount )
                                 .param( "description", description ) )
                .andDo( print() )
                .andExpect( status().is( 500 ) );
    }

    private String bearerOf( UUID token ) {
        return "Bearer " + token;
    }
}
