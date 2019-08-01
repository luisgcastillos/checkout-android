/*
 * Copyright (c) 2019 optile GmbH
 * https://www.optile.net
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package net.optile.payment.ui.page;

import android.text.TextUtils;
import net.optile.payment.R;
import net.optile.payment.core.PaymentError;
import net.optile.payment.core.PaymentException;
import net.optile.payment.form.Operation;
import net.optile.payment.model.ErrorInfo;
import net.optile.payment.model.Interaction;
import net.optile.payment.model.InteractionCode;
import net.optile.payment.model.InteractionReason;
import net.optile.payment.model.ListResult;
import net.optile.payment.model.OperationResult;
import net.optile.payment.ui.PaymentResult;
import net.optile.payment.ui.PaymentUI;
import net.optile.payment.ui.dialog.MessageDialogFragment;
import net.optile.payment.ui.dialog.ThemedDialogFragment;
import net.optile.payment.ui.dialog.ThemedDialogFragment.ThemedDialogListener;
import net.optile.payment.ui.model.PaymentSession;
import net.optile.payment.ui.service.PaymentSessionListener;
import net.optile.payment.ui.service.PaymentSessionService;
import net.optile.payment.ui.service.OperationListener;
import net.optile.payment.ui.service.OperationService;

/**
 * The ProcessPaymentPresenter takes care of posting the operation to the Payment API.
 * First this presenter will load the list, checks if the operation is present in the list and then post the operation to the Payment API.
 */
final class ProcessPaymentPresenter implements PaymentSessionListener, OperationListener {

    private final ProcessPaymentView view;
    private final PaymentSessionService sessionService;
    private final OperationService operationService;
    
    private PaymentSession session;
    private String listUrl;
    private Operation operation;

    /**
     * Create a new ProcessPaymentPresenter
     *
     * @param view The ProcessPaymentView
     */
    ProcessPaymentPresenter(ProcessPaymentView view) {
        this.view = view;
        sessionService = new PaymentSessionService();
        sessionService.setListener(this);
        operationService = new OperationService();
        operationService.setListener(this);
    }

    /**
     * Start the PresetAccount presenter
     *
     * @param operation process this operation when the presenter is started
     */
    void onStart(Operation operation) {

        if (sessionService.isActive() || operationService.isActive()) {
            return;
        }
        this.operation = operation;
        this.listUrl = PaymentUI.getInstance().getListUrl();
        loadPaymentSession(this.listUrl);
    }

    /**
     * Notify this presenter that it should stop and cleanup its resources
     */
    void onStop() {
        sessionService.stop();
        operationService.stop();
    }

    /**
     * Let this presenter handle the back pressed.
     *
     * @return true when this presenter handled the back press, false otherwise
     */
    boolean onBackPressed() {
        view.showWarningMessage(getString(R.string.pmsnackbar_operation_interrupted));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPaymentSessionSuccess(PaymentSession session) {
        ListResult listResult = session.getListResult();
        Interaction interaction = listResult.getInteraction();

        switch (interaction.getCode()) {
            case InteractionCode.PROCEED:
                handleLoadInteractionProceed(session);
                break;
            default:
                PaymentResult result = new PaymentResult(listResult.getResultInfo(), interaction);
                closeSessionWithCanceledCode(result);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPaymentSessionError(Throwable cause) {
        if (cause instanceof PaymentException) {
            handleLoadPaymentError((PaymentException) cause);
            return;
        }
        closeSessionWithErrorCode(R.string.pmdialog_error_unknown, cause);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOperationSuccess(OperationResult operation) {
        PaymentResult result = new PaymentResult(operation);

        switch (operation.getInteraction().getCode()) {
            case InteractionCode.PROCEED:
                closeSessionWithOkCode(result);
                break;
            default:
                handleOperationInteractionError(result);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOperationError(Throwable cause) {
        if (cause instanceof PaymentException) {
            handleOperationPaymentError((PaymentException) cause);
            return;
        }
        closeSessionWithErrorCode(R.string.pmdialog_error_unknown, cause);
    }

    private void handleLoadInteractionProceed(PaymentSession session) {
        this.session = session;

        if (!session.containsLink("operation", operation.getURL())) {
            closeSessionWithErrorCode(R.string.pmdialog_error_missingoperation, null);
            return;
        }
        postOperation(operation);
    }

    private void handleLoadPaymentError(PaymentException cause) {
        PaymentError error = cause.error;
        ErrorInfo info = error.errorInfo;

        if (info != null) {
            closeSessionWithCanceledCode(new PaymentResult(info.getResultInfo(), info.getInteraction()));
        } else if (error.isError(PaymentError.CONN_ERROR)) {
            handleLoadConnError(cause);
        } else {
            closeSessionWithErrorCode(R.string.pmdialog_error_unknown, cause);
        }
    }

    private void handleOperationPaymentError(PaymentException cause) {
        PaymentError error = cause.error;
        ErrorInfo info = error.errorInfo;

        if (info != null) {
            handleOperationInteractionError(new PaymentResult(info.getResultInfo(), info.getInteraction()));
        } else if (error.isError(PaymentError.CONN_ERROR)) {
            handleOperationConnError(cause);
        } else {
            closeSessionWithErrorCode(R.string.pmdialog_error_unknown, cause);
        }
    }

    private void handleOperationInteractionError(PaymentResult result) {
        Interaction interaction = result.getInteraction();

        switch (interaction.getCode()) {
            case InteractionCode.ABORT:
                handleOperationInteractionAbort(result);
                break;
            default:
                closeSessionWithCanceledCode(result);
        }
    }

    private void handleOperationInteractionAbort(PaymentResult result) {
        Interaction interaction = result.getInteraction();

        switch (interaction.getReason()) {
            case InteractionReason.DUPLICATE_OPERATION:
                closeSessionWithOkCode(result);
                break;
            default:
                closeSessionWithCanceledCode(result);
        }
    }

    private void showInteractionMessage(Interaction interaction) {
        String msg = translateInteraction(interaction, null);

        if (!TextUtils.isEmpty(msg)) {
            view.showMessageDialog(msg, null);
        }
    }

    private void closeSessionWithOkCode(PaymentResult result) {
        view.setPaymentResult(PaymentUI.RESULT_CODE_OK, result);
        view.closePage();
    }

    private void closeSessionWithCanceledCode(PaymentResult result) {
        String msg = translateInteraction(result.getInteraction(), getString(R.string.pmdialog_error_unknown));
        view.setPaymentResult(PaymentUI.RESULT_CODE_CANCELED, result);
        closePageWithMessage(msg);
    }

    private void closeSessionWithErrorCode(int msgResId, Throwable cause) {
        PaymentResult result;

        if (cause instanceof PaymentException) {
            PaymentException pe = (PaymentException) cause;
            result = new PaymentResult(pe.getMessage(), pe.error);
        } else {
            String resultInfo = cause != null ? cause.toString() : getString(msgResId);
            PaymentError error = new PaymentError(PaymentError.INTERNAL_ERROR, resultInfo);
            result = new PaymentResult(resultInfo, error);
        }
        view.setPaymentResult(PaymentUI.RESULT_CODE_ERROR, result);
        closePageWithMessage(getString(msgResId));
    }

    private void loadPaymentSession(final String listUrl) {
        this.session = null;
        view.showProgress();
        sessionService.loadPaymentSession(listUrl, view.getContext());
    }

    private void postOperation(final Operation operation) {
        this.operation = operation;
        view.showProgress();
        operationService.postOperation(operation);
    }

    private void handleLoadConnError(final PaymentException pe) {
        PaymentResult result = new PaymentResult(pe.getMessage(), pe.error);
        view.showConnErrorDialog(new ThemedDialogListener() {
            @Override
            public void onButtonClicked(ThemedDialogFragment dialog, int which) {
                switch (which) {
                    case ThemedDialogFragment.BUTTON_NEUTRAL:
                        view.closePage();
                        break;
                    case ThemedDialogFragment.BUTTON_POSITIVE:
                        loadPaymentSession(listUrl);
                        break;
                }
            }
            @Override
            public void onDismissed(ThemedDialogFragment dialog) {
                view.closePage();
            }
            });
    }

    private void handleOperationConnError(final PaymentException pe) {
        PaymentResult result = new PaymentResult(pe.getMessage(), pe.error);
        view.showConnErrorDialog(new ThemedDialogListener() {
            @Override
            public void onButtonClicked(ThemedDialogFragment dialog, int which) {
                switch (which) {
                    case ThemedDialogFragment.BUTTON_POSITIVE:
                        postOperation(operation);
                        break;
                    default:
                        view.closePage();
                }
            }
            @Override
            public void onDismissed(ThemedDialogFragment dialog) {
                view.closePage();
            }
        });
    }

    private String translateInteraction(Interaction interaction, String defMessage) {

        if (session == null || interaction == null) {
            return defMessage;
        }
        String msg = session.getLang().translateInteraction(interaction);
        return TextUtils.isEmpty(msg) ? defMessage : msg;
    }

    private void closePageWithMessage(String message) {
        view.showMessageDialog(message, new ThemedDialogListener() {
            @Override
            public void onButtonClicked(ThemedDialogFragment dialog, int which) {
                view.closePage();
            }
            @Override
            public void onDismissed(ThemedDialogFragment dialog) {
                view.closePage();
            }
        });
    }

    private String getString(int resId) {
        return view.getContext().getString(resId);
    }
}
