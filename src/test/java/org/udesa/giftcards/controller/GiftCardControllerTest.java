package org.udesa.giftcards.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.udesa.giftcards.model.GifCardFacade;

@WebMvcTest(GiftCardController.class)
public class GiftCardControllerTest {

    private static final String USER = "Alice";
    private static final String PASS = "Secret";
    private static final String CARD = "GC1";
    private static final String MERCHANT = "M1";
    private static final String DESCRIPTION = "UnCargo";
    private static final int AMOUNT = 5;
    private static final String CHARGE_ONE = "Charge1";
    private static final String CHARGE_TWO = "Charge2";
    private static final String BAD_AUTH_HEADER = "Basic abc";

    @Autowired private MockMvc mockMvc;
    @MockBean private GifCardFacade facade;

    @Test public void loginReturnsToken() throws Exception {
        UUID token = UUID.randomUUID();
        when(facade.login(USER, PASS)).thenReturn(token);

        mockMvc.perform(post("/login")
                        .param("user", USER)
                        .param("pass", PASS)
                        .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.token").value(token.toString()));

        verify(facade).login(USER, PASS);
    }

    @Test public void redeemUsesBearerToken() throws Exception {
        UUID token = UUID.randomUUID();
        String header = "Bearer " + token;

        mockMvc.perform(post("/" + CARD + "/redeem")
                        .header("Authorization", header))
               .andExpect(status().isOk());

        verify(facade).redeem(token, CARD);
    }

    @Test public void redeemFailsWithoutBearerHeader() throws Exception {
        mockMvc.perform(post("/" + CARD + "/redeem")
                        .header("Authorization", BAD_AUTH_HEADER))
               .andExpect(status().isInternalServerError());
    }

    @Test public void detailsReturnCharges() throws Exception {
        UUID token = UUID.randomUUID();
        String header = "Bearer " + token;
        List<String> details = List.of(CHARGE_ONE, CHARGE_TWO);
        when(facade.details(token, CARD)).thenReturn(details);

        mockMvc.perform(get("/" + CARD + "/details")
                        .header("Authorization", header)
                        .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.details[0]").value(details.get(0)))
               .andExpect(jsonPath("$.details[1]").value(details.get(1)));

        verify(facade).details(token, CARD);
    }

    @Test public void chargeDelegatesToFacade() throws Exception {
        mockMvc.perform(post("/" + CARD + "/charge")
                        .param("merchant", MERCHANT)
                        .param("amount", String.valueOf(AMOUNT))
                        .param("description", DESCRIPTION))
               .andExpect(status().isOk());

        verify(facade).charge(MERCHANT, CARD, AMOUNT, DESCRIPTION);
    }
}
