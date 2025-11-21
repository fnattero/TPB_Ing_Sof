package org.udesa.giftcards.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.udesa.giftcards.model.ModelEntity;

@Entity
@Table
@Getter
@Setter
public class UserVault extends ModelEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String password;

    public UserVault() {
    }

    public UserVault(String name, String password) {
        this.name = name;
        this.password = password;
    }
}
