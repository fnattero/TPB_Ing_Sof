package org.udesa.giftcards.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udesa.giftcards.model.Merchant;
import org.udesa.giftcards.repository.MerchantRepository;
import org.udesa.giftcards.model.ModelService;

@Service
public class MerchantService extends ModelService<Merchant, MerchantRepository> {

    @Transactional
    public Merchant save(Merchant model) {
        return repository.save(model);
    }

    @Transactional
    public void updateData(Merchant existingObject, Merchant updatedObject) {
        existingObject.setCode(updatedObject.getCode());
        existingObject.setDescription(updatedObject.getDescription());
    }

    @Transactional(readOnly = true)
    public Merchant getByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("InvalidMerchant"));
    }
}
