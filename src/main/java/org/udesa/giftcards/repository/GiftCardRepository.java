package org.udesa.giftcards.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.udesa.giftcards.entities.GiftCard;

public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {
    Optional<GiftCard> findByCode(String code);
}
