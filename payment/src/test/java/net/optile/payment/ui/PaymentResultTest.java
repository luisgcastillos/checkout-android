/*
 * Copyright (c) 2020 optile GmbH
 * https://www.optile.net
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package net.optile.payment.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.os.Parcel;
import net.optile.payment.model.ErrorInfo;
import net.optile.payment.model.Interaction;
import net.optile.payment.model.InteractionCode;
import net.optile.payment.model.InteractionReason;
import net.optile.payment.model.OperationResult;

@RunWith(RobolectricTestRunner.class)
public class PaymentResultTest {

    @Test
    public void construct_withOperationResult() {
        Interaction interaction = new Interaction(InteractionCode.ABORT, InteractionReason.CLIENTSIDE_ERROR);
        OperationResult operationResult = new OperationResult();
        operationResult.setInteraction(interaction);
        operationResult.setResultInfo("resultInfo");
        PaymentResult paymentResult = new PaymentResult(operationResult);

        assertEquals(operationResult, paymentResult.getOperationResult());
        assertEquals(interaction, paymentResult.getInteraction());
        assertEquals("resultInfo", paymentResult.getResultInfo());
    }

    @Test
    public void construct_withErrorInfo() {
        Interaction interaction = new Interaction(InteractionCode.ABORT, InteractionReason.CLIENTSIDE_ERROR);
        ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.setInteraction(interaction);
        errorInfo.setResultInfo("resultInfo");
        PaymentResult paymentResult = new PaymentResult(errorInfo);

        assertEquals(errorInfo, paymentResult.getErrorInfo());
        assertEquals("resultInfo", paymentResult.getResultInfo());
        assertEquals(interaction, paymentResult.getInteraction());
    }

    @Test
    public void construct_withErrorInfoAndThrowable() {
        Interaction interaction = new Interaction(InteractionCode.ABORT, InteractionReason.CLIENTSIDE_ERROR);
        Throwable cause = new Throwable();
        ErrorInfo errorInfo = new ErrorInfo("resultInfo", interaction);
        PaymentResult paymentResult = new PaymentResult(errorInfo, cause);

        assertEquals(errorInfo, paymentResult.getErrorInfo());
        assertEquals(cause, paymentResult.getCause());
        assertEquals("resultInfo", paymentResult.getResultInfo());
        assertEquals(interaction, paymentResult.getInteraction());
    }

    @Test
    public void writeToParcel() {
        Interaction interaction = new Interaction(InteractionCode.ABORT, InteractionReason.CLIENTSIDE_ERROR);
        ErrorInfo errorInfo = new ErrorInfo("resultInfo", interaction);
        PaymentResult writeResult = new PaymentResult(errorInfo);

        Parcel parcel = Parcel.obtain();
        writeResult.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);
        PaymentResult readResult = PaymentResult.CREATOR.createFromParcel(parcel);
        assertEquals(readResult.toString(), writeResult.toString());
    }
}
