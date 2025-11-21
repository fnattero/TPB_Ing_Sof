package org.udesa.giftcards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.udesa.giftcards.model.EntityDrawer;
import org.udesa.giftcards.model.ModelServiceTest;
import org.udesa.giftcards.model.UserVault;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserServiceTest extends ModelServiceTest<UserVault, UserService> {

    private static final String UPDATED_PASSWORD = "DifferentPass";
    private static final String UNKNOWN_USER = "no-user";

    @Override protected UserVault newSample() {
        return EntityDrawer.someUser();
    }

    @Override protected UserVault updateUser(UserVault user) {
        user.setPassword(UPDATED_PASSWORD);
        return user;
    }

    @Test public void findByNameReturnsUser() {
        UserVault user = savedSample();
        assertEquals(user, service.findByName(user.getName()));
    }

    @Test public void unknownNameThrows() {
        assertThrows(RuntimeException.class, () -> service.findByName(UNKNOWN_USER));
    }
}
