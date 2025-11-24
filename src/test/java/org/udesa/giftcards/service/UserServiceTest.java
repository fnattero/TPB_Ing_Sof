package org.udesa.giftcards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.udesa.giftcards.model.EntityDrawer;
import org.udesa.giftcards.entities.UserVault;

@SpringBootTest
public class UserServiceTest extends ModelServiceTest<UserVault, UserService> {

    private static final String invalidUser = "invalidUser";
    UserVault user;

    protected UserVault newSample() {
        return EntityDrawer.someUser();
    }

    protected UserVault updateSample(UserVault user) {
        user.setPassword("DifferentPass");
        return user;
    }

    @BeforeEach
    public void setUp() {
        user = savedSample();
    }

    @AfterAll
    public void tearDown() {
        service.deleteItemWithPrefix("User");
    }

    @Test public void findByNameReturnsUser() {
        assertEquals(user, service.findByName(user.getName()));
    }

    @Test public void deleteItemWithPrefixDeletesUser() {
        service.deleteItemWithPrefix("User");
        assertThrows(RuntimeException.class, () -> service.findByName(user.getName()));
    }

    @Test public void unknownNameThrows() {
        assertThrows(RuntimeException.class, () -> service.findByName(invalidUser));
    }
}
