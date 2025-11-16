package org.udesa.giftcards.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.udesa.giftcards.model.UserVault;

public interface UserRepository extends JpaRepository<UserVault, Long> {
    Optional<UserVault> findByName(String name);
}
