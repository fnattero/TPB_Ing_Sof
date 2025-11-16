package org.udesa.giftcards.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class GiftCardController {

//    POST /api/giftcards/login?user=aUser&pass=aPassword
//    Devuelve un token válido
    @PostMapping("/login") public ResponseEntity<Map<String, Object>> login(@RequestParam String user, @RequestParam String pass ) {


    }

//    POST /api/giftcards/{cardId}/redeem
//    Reclama una tarjeta (header Authorization: Bearer <token>)
    @PostMapping("/{cardId}/redeem") public ResponseEntity<String> redeemCard(@RequestHeader("Authorization") String header, @PathVariable String cardId ) {

    }

//    GET /api/giftcards/{cardId}/balance
//    Consulta saldo de la tarjeta
    @GetMapping("/{cardId}/balance") public ResponseEntity<Map<String, Object>> balance( @RequestHeader("Authorization") String header, @PathVariable String cardId ) {

    }

//    GET /api/giftcards/{cardId}/details
//    Lista los movimientos de la tarjeta
    @GetMapping("/{cardId}/details") public ResponseEntity<Map<String, Object>> details( @RequestHeader("Authorization") String tokenHeader, @PathVariable String cardId ) {

    }

//    POST /api/giftcards/{cardId}/charge?merchant=MerchantCode&amount=anAmount&description=aDescriptio
//     Un merchant hace un cargo sobre la tarjeta
    @PostMapping("/{cardId}/charge") public ResponseEntity<String> charge( @RequestParam String merchant, @RequestParam int amount, @RequestParam String description, @PathVariable String cardId ) {

    }
}

