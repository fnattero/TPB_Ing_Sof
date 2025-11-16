package org.udesa.giftcards.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.udesa.giftcards.model.Merchant;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Optional<Merchant> findByCode(String code);
}
