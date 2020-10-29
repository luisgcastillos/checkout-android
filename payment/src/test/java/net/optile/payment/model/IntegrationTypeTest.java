/*
 * Copyright (c) 2019 optile GmbH
 * https://www.optile.net
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package net.optile.payment.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * The IntegrationType test
 */
public class IntegrationTypeTest {

    @Test
    public void isIntegrationType_invalidValue_false() {
        assertFalse(IntegrationType.isValid("foo"));
    }

    @Test
    public void isIntegrationType_validValue_true() {
        assertTrue(IntegrationType.isValid(IntegrationType.MOBILE_NATIVE));
        assertTrue(IntegrationType.isValid(IntegrationType.DISPLAY_NATIVE));
        assertTrue(IntegrationType.isValid(IntegrationType.SELECTIVE_NATIVE));
        assertTrue(IntegrationType.isValid(IntegrationType.PURE_NATIVE));
        assertTrue(IntegrationType.isValid(IntegrationType.HOSTED));
    }
}
