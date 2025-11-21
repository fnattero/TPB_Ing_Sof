package org.udesa.giftcards.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class UserSessionTest {

    private static final String USER = "Alice";
    private static final long TEN_MINUTES = 10;
    private static final long SIXTEEN_MINUTES = 16;

    private final Clock clock = Mockito.mock(Clock.class);

    @Test public void userIsAliveWithinFifteenMinutes() {
        LocalDateTime now = LocalDateTime.now();
        when(clock.now()).thenReturn(now, now.plusMinutes(TEN_MINUTES));

        UserSession session = new UserSession(USER, clock);

        assertEquals(USER, session.userAliveAt(clock));
    }

    @Test public void userExpiresAfterFifteenMinutes() {
        LocalDateTime now = LocalDateTime.now();
        when(clock.now()).thenReturn(now, now.plusMinutes(SIXTEEN_MINUTES));

        UserSession session = new UserSession(USER, clock);

        assertThrows(RuntimeException.class, () -> session.userAliveAt(clock));
    }
}
