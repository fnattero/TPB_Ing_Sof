package org.udesa.giftcards.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udesa.giftcards.model.UserVault;
import org.udesa.giftcards.repository.UserRepository;
import org.udesa.giftcards.model.ModelService;

@Service
public class UserService extends ModelService<UserVault, UserRepository> {

    @Transactional
    public UserVault save(UserVault model) {
        return repository.save(model);
    }

    @Transactional
    public void updateData(UserVault existingObject, UserVault updatedObject) {
        existingObject.setName( updatedObject.getName() );
        existingObject.setPassword( updatedObject.getPassword() );
    }

    @Transactional(readOnly = true)
    public UserVault findByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new RuntimeException("InvalidUser"));
    }

}
