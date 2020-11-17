/*
 * Copyright (c) 2020 optile GmbH
 * https://www.optile.net
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package net.optile.payment.ui.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import net.optile.payment.R;
import net.optile.payment.ui.PaymentTheme;

public class PaymentThemeTest {

    @Test
    public void createBuilder() {
        PaymentTheme.Builder builder = PaymentTheme.createBuilder();
        assertNotNull(builder);
    }

    @Test
    public void createDefault() {
        PaymentTheme theme = PaymentTheme.createDefault();
        assertNotNull(theme);

        assertEquals(theme.getPaymentListTheme(), R.style.PaymentTheme_Toolbar);
        assertEquals(theme.getChargePaymentTheme(), R.style.PaymentTheme_NoToolbar);
    }

    @Test
    public void getPaymentListTheme() {
        int value = R.style.PaymentTheme;
        PaymentTheme theme = PaymentTheme.createBuilder().
            setPaymentListTheme(value).build();
        assertEquals(theme.getPaymentListTheme(), value);
    }

    @Test
    public void getChargePaymentTheme() {
        int value = R.style.PaymentTheme;
        PaymentTheme theme = PaymentTheme.createBuilder().
            setChargePaymentTheme(value).build();
        assertEquals(theme.getChargePaymentTheme(), value);
    }
}
