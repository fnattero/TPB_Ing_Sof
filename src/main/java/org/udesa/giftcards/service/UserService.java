package org.udesa.giftcards.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udesa.giftcards.entities.UserVault;
import org.udesa.giftcards.repository.UserRepository;

@Service
public class UserService extends ModelService<UserVault, UserRepository> {

    public void deleteItemWithPrefix(String prefix) {
        this.findAll().stream().filter(item -> item.getName().startsWith(prefix)).forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public UserVault findByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new RuntimeException("InvalidUser"));
    }

}
