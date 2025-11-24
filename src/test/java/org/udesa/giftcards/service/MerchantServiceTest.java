package org.udesa.giftcards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.udesa.giftcards.model.EntityDrawer;
import org.udesa.giftcards.entities.Merchant;

@SpringBootTest
public class MerchantServiceTest extends ModelServiceTest<Merchant, MerchantService> {

    private static final String invalidMerchantCode = "123456789";
    Merchant merchant;

    protected Merchant newSample() {
        return EntityDrawer.someMerchant();
    }

    protected Merchant updateSample(Merchant merchant) {
        merchant.setDescription("Updated merchant");
        return merchant;
    }

    @BeforeEach
    public void setUp() {
        merchant = savedSample();
    }

    @AfterAll
    public void tearDown() {
        service.deleteItemWithPrefix("M_");
    }

    @Test public void findByCodeReturnsMerchant() {
        assertEquals(merchant, service.findByCode(merchant.getCode()));
    }

    @Test public void deleteItemWithPrefixDeletesUser() {
        service.deleteItemWithPrefix("M_");
        assertThrows(RuntimeException.class, () -> service.findByCode(merchant.getCode()));
    }


    @Test public void unknownCodeThrows() {
        assertThrows(RuntimeException.class, () -> service.findByCode(invalidMerchantCode));
    }
}
