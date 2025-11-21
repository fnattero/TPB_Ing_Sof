package org.udesa.giftcards.model;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class Clock {
    public LocalDate today() {
        return LocalDate.now();
    }
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
