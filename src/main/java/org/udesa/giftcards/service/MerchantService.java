package org.udesa.giftcards.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udesa.giftcards.entities.Merchant;
import org.udesa.giftcards.repository.MerchantRepository;

@Service
public class MerchantService extends ModelService<Merchant, MerchantRepository> {

    public void deleteItemWithPrefix(String prefix) {
        this.findAll().stream().filter(item -> item.getCode().startsWith(prefix)).forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public Merchant getByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("InvalidMerchant"));
    }
}
