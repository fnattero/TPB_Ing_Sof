package org.udesa.giftcards.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udesa.giftcards.model.GifCardFacade;

import java.util.Map;
import java.util.UUID;

@RestController
public class GiftCardController {

    public String UnauthorizedHeader = "Invalid Authorization header";

    @Autowired GifCardFacade systemFacade;

//    POST /api/giftcards/login?user=aUser&pass=aPassword
//    Devuelve un token válido
    @PostMapping("/login") 
    public ResponseEntity<Map<String, Object>> login(@RequestParam String user, @RequestParam String pass ) {
        return ResponseEntity.ok(Map.of("token", systemFacade.login(user, pass).toString()));
    }   

//    POST /api/giftcards/{cardId}/redeem
//    Reclama una tarjeta (header Authorization: Bearer <token>)
    @PostMapping("/{cardId}/redeem") public ResponseEntity<String> redeemCard(@RequestHeader("Authorization") String header, @PathVariable String cardId ) {
        systemFacade.redeem(getTokenFromHeader(header), cardId);
        return ResponseEntity.ok("");
    }

//    GET /api/giftcards/{cardId}/balance
//    Consulta saldo de la tarjeta
    @GetMapping("/{cardId}/balance") public ResponseEntity<Map<String, Object>> balance( @RequestHeader("Authorization") String header, @PathVariable String cardId ) {
        return ResponseEntity.ok(Map.of("balance", systemFacade.balance(getTokenFromHeader(header), cardId)));
    }

//    GET /api/giftcards/{cardId}/details
//    Lista los movimientos de la tarjeta
    @GetMapping("/{cardId}/details") public ResponseEntity<Map<String, Object>> details( @RequestHeader("Authorization") String tokenHeader, @PathVariable String cardId ) {
        return ResponseEntity.ok(Map.of("details", systemFacade.details(getTokenFromHeader(tokenHeader), cardId)));
    }

//    POST /api/giftcards/{cardId}/charge?merchant=MerchantCode&amount=anAmount&description=aDescriptio
//     Un merchant hace un cargo sobre la tarjeta
    @PostMapping("/{cardId}/charge") public ResponseEntity<String> charge( @RequestParam String merchant, @RequestParam int amount, @RequestParam String description, @PathVariable String cardId ) {
        systemFacade.charge( merchant, cardId, amount, description );
        return ResponseEntity.ok("");
    }

    private UUID getTokenFromHeader(String header ) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException(UnauthorizedHeader);
        }
        return UUID.fromString(header.substring(7));
    }
}

