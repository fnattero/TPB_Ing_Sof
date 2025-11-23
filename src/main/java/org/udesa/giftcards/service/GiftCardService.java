package org.udesa.giftcards.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udesa.giftcards.entities.GiftCard;
import org.udesa.giftcards.repository.GiftCardRepository;

@Service
public class GiftCardService extends ModelService<GiftCard, GiftCardRepository> {

    public GiftCard redeem(String cardCode, String ownerName) {
        GiftCard card = findByCode(cardCode);
        card.redeem(ownerName);
        return repository.save(card);
    }

    public GiftCard charge(String cardCode, int amount, String description) {
        GiftCard card = findByCode(cardCode);
        card.charge(amount, description);
        return repository.save(card);
    }

    @Transactional(readOnly = true)
    public GiftCard findByCode(String cardCode) {
        return repository.findByCode(cardCode)
                .orElseThrow(() -> new RuntimeException(GiftCard.InvalidCard));
    }

    public void deleteItemWithPrefix(String prefix) {
        this.findAll().stream().filter(item -> item.getCode().startsWith(prefix)).forEach(this::delete);
    }
}
