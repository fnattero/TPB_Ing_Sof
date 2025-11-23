package org.udesa.giftcards.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
public class UserVault extends ModelEntity {

    @Column(unique = true)
    private String name;

    @Column(nullable = false)
    private String password;

    public UserVault() {
    }

    public UserVault(String name, String password) {
        this.name = name;
        this.password = password;
    }

    protected boolean same( Object o ) { return this.name.equals( getClass().cast( o ).getName() ); }
}
