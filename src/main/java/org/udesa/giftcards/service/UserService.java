package org.udesa.giftcards.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udesa.giftcards.model.UserVault;
import org.udesa.giftcards.repository.UserRepository;
import org.udesa.tuslibros.service.ModelService;

@Service
public class UserService extends ModelService<UserVault, UserRepository> {

    @Transactional
    public UserVault save(UserVault model) {
        return repository.save(model);
    }

    @Transactional
    public UserVault update(Long id, UserVault updatedObject) {
        UserVault existing = getById(id);
        existing.setName(updatedObject.getName());
        existing.setPassword(updatedObject.getPassword());
        return repository.save(existing);
    }

    @Transactional
    public void delete(long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void delete(UserVault model) {
        repository.delete(model);
    }

    @Transactional(readOnly = true)
    public UserVault findByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new RuntimeException("InvalidUser"));
    }

    @Transactional(readOnly = true)
    public UserVault validateCredentials(String name, String password) {
        UserVault user = findByName(name);
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("InvalidUser");
        }
        return user;
    }
}
