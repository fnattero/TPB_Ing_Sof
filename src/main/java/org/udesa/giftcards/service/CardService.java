package org.udesa.giftcards.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udesa.giftcards.model.GiftCard;
import org.udesa.giftcards.model.Merchant;
import org.udesa.giftcards.repository.GiftCardRepository;
import org.udesa.giftcards.model.ModelService;

@Service
public class CardService extends ModelService<GiftCard, GiftCardRepository> {

    @Transactional
    public GiftCard save(GiftCard model) {
        return repository.save(model);
    }

    @Transactional
    public GiftCard redeem(String cardCode, String ownerName) {
        GiftCard card = findByCode(cardCode);
        card.redeem(ownerName);
        return repository.save(card);
    }

    @Transactional
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

    @Transactional
    public void updateData(GiftCard existingObject, GiftCard updatedObject) {
        existingObject.setCode(updatedObject.getCode());
        existingObject.setOwner(updatedObject.getOwner());
        existingObject.setBalance(updatedObject.getBalance());
        existingObject.setCharges(updatedObject.getCharges());
    }
}
