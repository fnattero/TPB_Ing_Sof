package org.udesa.giftcards.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udesa.giftcards.model.GiftCard;
import org.udesa.giftcards.repository.GiftCardRepository;
import org.udesa.tuslibros.service.ModelService;

@Service
public class CardService extends ModelService<GiftCard, GiftCardRepository> {

    @Transactional
    public GiftCard save(GiftCard model) {
        return repository.save(model);
    }

    @Transactional
    public GiftCard redeem(String cardCode, String owner) {
        GiftCard card = getByCode(cardCode);
        card.redeem(owner);
        return repository.save(card);
    }

    @Transactional
    public GiftCard charge(String cardCode, int amount, String description) {
        GiftCard card = getByCode(cardCode);
        card.charge(amount, description);
        return repository.save(card);
    }

    @Transactional(readOnly = true)
    public GiftCard getByCode(String cardCode) {
        return repository.findByCode(cardCode)
                .orElseThrow(() -> new RuntimeException(GiftCard.InvalidCard));
    }

    @Transactional(readOnly = true)
    public int balance(String cardCode) {
        return getByCode(cardCode).balance();
    }

    @Transactional(readOnly = true)
    public List<String> details(String cardCode) {
        return getByCode(cardCode).charges();
    }

    @Transactional
    public void delete(long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void delete(GiftCard model) {
        repository.delete(model);
    }
}
