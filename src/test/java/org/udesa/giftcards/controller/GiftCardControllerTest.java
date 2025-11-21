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
import java.util.Map;
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
    private static final String BAD_AUTH_HEADER = "Basic abc";
    private static final String FIRST_CHARGE = "Charge1";
    private static final String SECOND_CHARGE = "Charge2";

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
        UUID token = login( savedUser() );
        assertTrue( token.toString().length() > 0 );
    }

    @Test public void redeemUsesBearerToken() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard();
        UUID token = login( user );

        redeem( token, card.getCode() );

        assertEquals( user.getName(), cardService.findByCode( card.getCode() ).getOwner() );
    }

    @Test public void redeemFailsWithoutBearerHeader() throws Exception {
        GiftCard card = savedCard();
        mockMvc.perform( post( "/" + card.getCode() + "/redeem" )
                                 .header( "Authorization", BAD_AUTH_HEADER ) )
                .andDo( print() )
                .andExpect( status().is( 500 ) );
    }

    @Test public void balanceReflectsCharges() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard();
        Merchant merchant = savedMerchant();
        UUID token = login( user );

        redeem( token, card.getCode() );
        charge( merchant.getCode(), card.getCode(), A_CHARGE_AMOUNT, "UnCargo" );

        assertEquals( INITIAL_BALANCE - A_CHARGE_AMOUNT, balanceOf( token, card.getCode() ) );
    }

    @Test public void detailsReturnCharges() throws Exception {
        UserVault user = savedUser();
        GiftCard card = savedCard();
        Merchant merchant = savedMerchant();
        UUID token = login( user );

        redeem( token, card.getCode() );
        charge( merchant.getCode(), card.getCode(), 1, FIRST_CHARGE );
        charge( merchant.getCode(), card.getCode(), 1, SECOND_CHARGE );

        List<String> details = detailsOf( token, card.getCode() );

        assertEquals( FIRST_CHARGE, details.getFirst() );
        assertEquals( SECOND_CHARGE, details.getLast() );
        assertEquals( 2, details.size() );
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

    private UUID login( UserVault user ) throws Exception {
        String response = mockMvc.perform( post( "/login" )
                                                   .param( "user", user.getName() )
                                                   .param( "pass", user.getPassword() ) )
                                 .andDo( print() )
                                 .andExpect( status().is( 200 ) )
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString();

        return UUID.fromString( new ObjectMapper().readValue( response, HashMap.class ).get( "token" ).toString() );
    }

    private void redeem( UUID token, String cardId ) throws Exception {
        mockMvc.perform( post( "/" + cardId + "/redeem" )
                                 .header( "Authorization", bearerOf( token ) ) )
                .andDo( print() )
                .andExpect( status().is( 200 ) );
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

    private List<String> detailsOf( UUID token, String cardId ) throws Exception {
        String response = mockMvc.perform( get( "/" + cardId + "/details" )
                                                   .header( "Authorization", bearerOf( token ) ) )
                                 .andDo( print() )
                                 .andExpect( status().is( 200 ) )
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString();

        return (List<String>) new ObjectMapper().readValue( response, HashMap.class ).get( "details" );
    }

    private void charge( String merchant, String cardId, int amount, String description ) throws Exception {
        mockMvc.perform( post( "/" + cardId + "/charge" )
                                 .param( "merchant", merchant )
                                 .param( "amount", "" + amount )
                                 .param( "description", description ) )
                .andDo( print() )
                .andExpect( status().is( 200 ) );
    }

    private String bearerOf( UUID token ) {
        return "Bearer " + token;
    }
}
