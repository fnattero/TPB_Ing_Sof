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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.udesa.giftcards.model.Clock;
import org.udesa.giftcards.model.EntityDrawer;
import org.udesa.giftcards.model.GiftCard;
import org.udesa.giftcards.model.Merchant;
import org.udesa.giftcards.model.UserVault;
import org.udesa.giftcards.service.CardService;
import org.udesa.giftcards.service.MerchantService;
import org.udesa.giftcards.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
public class GiftCardControllerTest {

    private static final int INITIAL_BALANCE = 10;
    private static final int A_CHARGE_AMOUNT = 3;
    private static final String BAD_AUTH_HEADER = "badAuthHeader";
    private static final String FIRST_CHARGE = "Charge1";
    private static final String SECOND_CHARGE = "Charge2";
    private static final String invalid_user = "invalid user";
    private static final String invalid_password = "invalid password";
    private static final String invalid_code = "invalid code";
    private static final String invalidMerchant = "invalid merchant";

    @Autowired MockMvc mockMvc;
    @Autowired UserService userService;
    @Autowired MerchantService merchantService;
    @Autowired CardService cardService;
    @MockBean Clock clock;

    @BeforeEach
    public void beforeEach() {
        when( clock.now() ).then( it -> LocalDateTime.now() );
        when( clock.today() ).then( it -> LocalDate.now() );
    }

    @Test public void loginReturnsAToken() throws Exception {
        UUID token = UUID.fromString( loginOk( savedUser() ) );
        assertTrue( token.toString().length() > 0 );
    }

    @Test
    public void loginFailsWithInvalidUsername() throws Exception {
        UserVault validUser = savedUser();
        UserVault invalidUser = new UserVault(invalid_user, validUser.getPassword());
        loginFails( invalidUser );
    }

    @Test
    public void loginFailsWithInvalidPassword() throws Exception {
        UserVault validUser = savedUser();
        UserVault invalidUser = new UserVault(validUser.getName(), invalid_password);
        loginFails( invalidUser );
    }

    @Test public void redeemUsesBearerToken() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard();
        UUID token = UUID.fromString( loginOk( user ) );

        redeemOk( token, card.getCode() );

        assertEquals( user.getName(), cardService.findByCode( card.getCode() ).getOwner() );
    }

    @Test public void redeemFailsWithoutBearerHeader() throws Exception {
        GiftCard card = savedCard();
        mockMvc.perform( post( "/" + card.getCode() + "/redeem" )
                                 .header( "Authorization", BAD_AUTH_HEADER ) )
                .andDo( print() )
                .andExpect( status().is( 500 ) );
    }

    @Test public void redeemFailsWithInvalidCardId() throws Exception {
        UserVault user = savedUser();
        UUID token = UUID.fromString( loginOk( user ) );
        redeemFails( token, invalid_code );
    }

    @Test public void redeemFailsWithInvalidToken() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard();

        redeemFails( UUID.randomUUID(), card.getCode() );
    }

    @Test public void balanceReflectsCharges() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard();
        Merchant merchant = savedMerchant();
        UUID token = UUID.fromString( loginOk( user ) );

        redeemOk( token, card.getCode() );
        chargeOk( merchant.getCode(), card.getCode(), A_CHARGE_AMOUNT, "UnCargo" );

        assertEquals( INITIAL_BALANCE - A_CHARGE_AMOUNT, balanceOf( token, card.getCode() ) );
    }

    @Test public void balanceFailsWIthInvalidToken() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard();
        Merchant merchant = savedMerchant();
        UUID token = UUID.fromString( loginOk( user ) );

        redeemOk( token, card.getCode() );
        chargeOk( merchant.getCode(), card.getCode(), A_CHARGE_AMOUNT, "UnCargo" );

        balanceFails( UUID.randomUUID(), card.getCode() );
    }

    @Test public void balanceFailsWithInvalidCardId() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard();
        Merchant merchant = savedMerchant();
        UUID token = UUID.fromString( loginOk( user ) );

        redeemOk( token, card.getCode() );
        chargeOk( merchant.getCode(), card.getCode(), A_CHARGE_AMOUNT, "UnCargo" );

        balanceFails(token, invalid_code );
    }

    @Test public void detailsReturnCharges() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard();
        Merchant merchant = savedMerchant();
        UUID token = UUID.fromString( loginOk( user ) );

        redeemOk( token, card.getCode() );
        chargeOk( merchant.getCode(), card.getCode(), 1, FIRST_CHARGE );
        chargeOk( merchant.getCode(), card.getCode(), 1, SECOND_CHARGE );

        List<String> details = detailsOk( token, card.getCode() );

        assertEquals( FIRST_CHARGE, details.getFirst() );
        assertEquals( SECOND_CHARGE, details.getLast() );
        assertEquals( 2, details.size() );
    }

    @Test public void detailsReturnChargesMultipleUsers() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard();

        UserVault user2 = savedUser();
        GiftCard card2 = savedCard();

        Merchant merchant = savedMerchant();

        UUID token = UUID.fromString( loginOk( user ) );
        UUID token2 = UUID.fromString( loginOk( user2 ) );

        redeemOk( token, card.getCode() );
        redeemOk( token2, card2.getCode() );

        chargeOk( merchant.getCode(), card.getCode(), 1, FIRST_CHARGE );
        chargeOk( merchant.getCode(), card.getCode(), 1, SECOND_CHARGE );
        chargeOk( merchant.getCode(), card2.getCode(), 1, FIRST_CHARGE );

        List<String> details = detailsOk( token, card.getCode() );
        List<String> details2 = detailsOk( token2, card2.getCode() );

        assertEquals( FIRST_CHARGE, details.getFirst() );
        assertEquals( SECOND_CHARGE, details.getLast() );
        assertEquals( 2, details.size() );

        assertEquals( FIRST_CHARGE, details2.getFirst() );
        assertEquals( 1, details2.size() );
    }

    @Test public void detailsFailsWithInvalidToken() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard();
        Merchant merchant = savedMerchant();
        UUID token = UUID.fromString( loginOk( user ) );

        redeemOk( token, card.getCode() );
        chargeOk( merchant.getCode(), card.getCode(), 1, FIRST_CHARGE );

        detailsFails( UUID.randomUUID(), card.getCode() );
    }

    @Test public void detailsFailsWithInvalidCardId() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard();
        Merchant merchant = savedMerchant();
        UUID token = UUID.fromString( loginOk( user ) );

        redeemOk( token, card.getCode() );
        chargeOk( merchant.getCode(), card.getCode(), 1, FIRST_CHARGE );

        detailsFails( token, invalid_code );
    }

    @Test public void chargeFailsWithInvalidMerchant() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard();
        UUID token = UUID.fromString( loginOk( user ) );

        redeemOk( token, card.getCode() );
        chargeFails( invalidMerchant, card.getCode(), 1, FIRST_CHARGE );
    }

    @Test public void chargeFailsWithInvalidCardId() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard();
        Merchant merchant = savedMerchant();
        UUID token = UUID.fromString( loginOk( user ) );

        redeemOk( token, card.getCode() );
        chargeFails( merchant.getCode(), invalid_code, 1, FIRST_CHARGE );
    }

    @Test public void chargeFailsWithInvalidAmount() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard();
        Merchant merchant = savedMerchant();
        UUID token = UUID.fromString( loginOk( user ) );

        redeemOk( token, card.getCode() );
        chargeFails( merchant.getCode(), card.getCode(), INITIAL_BALANCE + 1, FIRST_CHARGE );
    }

    private UserVault savedUser() {
        return userService.save( EntityDrawer.someUser() );
    }

    private GiftCard savedCard() {
        return cardService.save( EntityDrawer.someGiftCard( INITIAL_BALANCE ) );
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
