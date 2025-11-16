package org.udesa.giftcards.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udesa.giftcards.model.Merchant;
import org.udesa.giftcards.repository.MerchantRepository;
import org.udesa.tuslibros.service.ModelService;

@Service
public class MerchantService extends ModelService<Merchant, MerchantRepository> {

    @Transactional
    public Merchant save(Merchant model) {
        return repository.save(model);
    }

    @Transactional
    public Merchant update(Long id, Merchant updatedObject) {
        Merchant existing = getById(id);
        existing.setCode(updatedObject.getCode());
        existing.setDescription(updatedObject.getDescription());
        return repository.save(existing);
    }

    @Transactional
    public void delete(long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Merchant getByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("InvalidMerchant"));
    }
}
