package org.udesa.giftcards.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class UserSessionTest {

    private static final String validUser = "Alice";
    private static final long tenMinutes = 10;
    private static final long sixteenMinutes = 16;

    private final Clock clock = Mockito.mock(Clock.class);

    @Test public void userIsAliveWithinFifteenMinutes() {
        LocalDateTime now = LocalDateTime.now();
        when(clock.now()).thenReturn(now, now.plusMinutes(tenMinutes));

        UserSession session = new UserSession(validUser, clock);

        assertEquals(validUser, session.userAliveAt(clock));
    }

    @Test public void userExpiresAfterFifteenMinutes() {
        LocalDateTime now = LocalDateTime.now();
        when(clock.now()).thenReturn(now, now.plusMinutes(sixteenMinutes));

        UserSession session = new UserSession(validUser, clock);

        assertThrows(RuntimeException.class, () -> session.userAliveAt(clock));
    }
}
