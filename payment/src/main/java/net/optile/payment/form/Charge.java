/*
 * Copyright(c) 2012-2018 optile GmbH. All Rights Reserved.
 * https://www.optile.net
 *
 * This software is the property of optile GmbH. Distribution  of  this
 * software without agreement in writing is strictly prohibited.
 *
 * This software may not be copied, used or distributed unless agreement
 * has been received in full.
 */

package net.optile.payment.form;

import org.json.JSONException;
import org.json.JSONObject;

import net.optile.payment.core.PaymentError;
import net.optile.payment.core.PaymentException;

/**
 * Class holding the Charge form values
 */
public class Charge {

    private final JSONObject charge;
    private final JSONObject account;

    public Charge() {
        charge = new JSONObject();
        account = new JSONObject();
    }

    public void putValue(String name, Object value) throws PaymentException {
        try {
            account.put(name, value);
        } catch (JSONException e) {
            String msg = "Charge.putValue failed for name: " + name;
            PaymentError error = new PaymentError("Charge", PaymentError.INTERNAL_ERROR, msg);
            throw new PaymentException(error, msg, e);
        }
    }

    public void putRegister(String name, boolean value) throws PaymentException {
        try {
            charge.put(name, value);
        } catch (JSONException e) {
            String msg = "Charge.putRegister failed for name: " + name;
            PaymentError error = new PaymentError("Charge", PaymentError.INTERNAL_ERROR, msg);
            throw new PaymentException(error, msg, e);
        }
    }

    public String toJson() throws JSONException {
        charge.put("account", account);
        return charge.toString();
    }
}
