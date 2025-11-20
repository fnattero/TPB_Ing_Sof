package org.udesa.giftcards.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udesa.giftcards.service.CardService;
import org.udesa.giftcards.service.MerchantService;
import org.udesa.giftcards.service.UserService;

@Service
public class GifCardFacade {
    public static final String InvalidUser = "InvalidUser";
    public static final String InvalidMerchant = "InvalidMerchant";
    public static final String InvalidToken = "InvalidToken";

    @Autowired private CardService cardService;
    @Autowired private MerchantService merchantService;
    @Autowired private UserService userService;
    @Autowired private Clock clock;

    private final Map<UUID, UserSession> sessions = new HashMap<>();

//    public GifCardFacade(CardService cardService,
//                         MerchantService merchantService,
//                         UserService userService,
//                         Clock clock) {
//        this.cardService = cardService;
//        this.merchantService = merchantService;
//        this.userService = userService;
//        this.clock = clock;
//    }

    public UUID login( String userKey, String pass ) {
        String userName = validateUserCredentials(userKey, pass);
        UUID token = UUID.randomUUID();
        sessions.put( token, new UserSession( userName, clock ) );
        return token;
    }

    public void redeem( UUID token, String cardId ) {
        cardService.redeem(cardId, findUser(token));
    }

    public int balance( UUID token, String cardId ) {
        return ownedCard( token, cardId ).balance();
    }

    public void charge( String merchantKey, String cardId, int amount, String description ) {
        merchantService.getByCode( merchantKey );
        cardService.charge( cardId, amount, description );
    }

    public List<String> details( UUID token, String cardId ) {
        return ownedCard( token, cardId ).charges();
    }

    private GiftCard ownedCard( UUID token, String cardId ) {
        GiftCard card = cardService.getByCode( cardId );
        if ( !card.isOwnedBy( findUser( token ) ) ) throw new RuntimeException( InvalidToken );
        return card;
    }

    private String findUser( UUID token ) {
        return sessions.computeIfAbsent( token, key -> { throw new RuntimeException( InvalidToken ); } )
                       .userAliveAt( clock );
    }

    private String validateUserCredentials( String user, String password ) {
        if (!userService.findByName(user).getPassword().equals(password)) {
            throw new RuntimeException(InvalidUser);
        }
        return userService.findByName(user).getName();
    }
}
