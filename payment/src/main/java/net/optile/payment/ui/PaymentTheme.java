/*
 * Copyright (c) 2019 optile GmbH
 * https://www.optile.net
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package net.optile.payment.ui;

import net.optile.payment.R;

/**
 * Class to hold the theme settings of the payment screens in the Android SDK
 */
public final class PaymentTheme {
    private final int messageDialogTheme;
    private final int paymentListTheme;
    private final int chargePaymentTheme;

    private PaymentTheme(Builder builder) {
        this.messageDialogTheme = builder.messageDialogTheme;
        this.paymentListTheme = builder.paymentListTheme;
        this.chargePaymentTheme = builder.chargePaymentTheme;
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    public static PaymentTheme createDefault() {
        return createBuilder().
            setMessageDialogTheme(R.style.PaymentDialogTheme_Message).
            setPaymentListTheme(R.style.PaymentTheme_PaymentList).
            setChargePaymentTheme(R.style.PaymentTheme_ChargePayment).
            build();
    }

    public int getMessageDialogTheme() {
        return messageDialogTheme;
    }

    public int getPaymentListTheme() {
        return paymentListTheme;
    }

    public int getChargePaymentTheme() {
        return chargePaymentTheme;
    }

    public static final class Builder {
        int messageDialogTheme;
        int paymentListTheme;
        int chargePaymentTheme;

        Builder() {
        }

        public Builder setMessageDialogTheme(int messageDialogTheme) {
            this.messageDialogTheme = messageDialogTheme;
            return this;
        }

        public Builder setPaymentListTheme(int paymentListTheme) {
            this.paymentListTheme = paymentListTheme;
            return this;
        }

        public Builder setChargePaymentTheme(int chargePaymentTheme) {
            this.chargePaymentTheme = chargePaymentTheme;
            return this;
        }

        public PaymentTheme build() {
            return new PaymentTheme(this);
        }
    }
}
