package org.udesa.giftcards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.udesa.giftcards.model.EntityDrawer;
import org.udesa.giftcards.model.Merchant;
import org.udesa.giftcards.model.ModelServiceTest;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MerchantServiceTest extends ModelServiceTest<Merchant, MerchantService> {

    private static final String UPDATED_DESCRIPTION = "Updated merchant";
    private static final String UNKNOWN_CODE = "unknown Merchant";

    @Override protected Merchant newSample() {
        return EntityDrawer.someMerchant();
    }

    @Override protected Merchant updateUser(Merchant merchant) {
        merchant.setDescription(UPDATED_DESCRIPTION);
        return merchant;
    }

    @Test public void getByCodeReturnsMerchant() {
        Merchant merchant = savedSample();
        assertEquals(merchant, service.getByCode(merchant.getCode()));
    }

    @Test public void unknownCodeThrows() {
        assertThrows(RuntimeException.class, () -> service.getByCode(UNKNOWN_CODE));
    }
}
