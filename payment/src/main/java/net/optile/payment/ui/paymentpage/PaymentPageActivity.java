/*
 * Copyright(c) 2012-2018 optile GmbH. All Rights Reserved.
 * https://www.optile.net
 * <p>
 * This software is the property of optile GmbH. Distribution  of  this
 * software without agreement in writing is strictly prohibited.
 * <p>
 * This software may not be copied, used or distributed unless agreement
 * has been received in full.
 */

package net.optile.payment.ui.paymentpage;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import net.optile.payment.R;
import net.optile.payment.ui.PaymentResult;
import net.optile.payment.ui.PaymentUI;
import net.optile.payment.ui.dialog.MessageDialogFragment;
import net.optile.payment.ui.widget.FormWidget;
import net.optile.payment.validation.ValidationResult;
import net.optile.payment.validation.Validator;

/**
 * The PaymentPageActivity showing available payment methods
 */
public final class PaymentPageActivity extends AppCompatActivity implements PaymentPageView {

    private static final String TAG = "pay_PaymentPageActivity";
    private static final String EXTRA_LISTURL = "extra_listurl";

    private PaymentPagePresenter presenter;
    private String listUrl;
    private boolean active;
    private PaymentList paymentList;
    private ProgressBar progressBar;
    private int cachedListIndex;

    /**
     * Create the start intent for this Activity
     *
     * @param context Context to create the intent
     * @return newly created start intent
     */
    public static Intent createStartIntent(Context context, String listUrl) {
        final Intent intent = new Intent(context, PaymentPageActivity.class);
        intent.putExtra(EXTRA_LISTURL, listUrl);
        return intent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(Activity.RESULT_CANCELED, null);
        this.cachedListIndex = -1;

        if (savedInstanceState != null) {
            this.listUrl = savedInstanceState.getString(EXTRA_LISTURL);
        } else {
            Intent intent = getIntent();
            this.listUrl = intent.getStringExtra(EXTRA_LISTURL);
        }
        setContentView(R.layout.activity_paymentpage);
        this.progressBar = findViewById(R.id.progressbar);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        this.presenter = new PaymentPagePresenter(this);
        this.paymentList = new PaymentList(this, findViewById(R.id.recyclerview_paymentlist),
            findViewById(R.id.label_empty));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        this.cachedListIndex = paymentList.getSelected();
        savedInstanceState.putString(EXTRA_LISTURL, listUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        this.active = false;
        this.presenter.onStop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        this.active = true;
        presenter.load(this.listUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return this.active;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringRes(int resId) {
        return getString(resId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        if (!isActive()) {
            return;
        }
        paymentList.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showPaymentSession(PaymentSession session) {

        if (!isActive()) {
            return;
        }
        progressBar.setVisibility(View.GONE);

        if (this.cachedListIndex != -1) {
            session.setSelIndex(this.cachedListIndex);
            this.cachedListIndex = -1;
        }
        paymentList.showPaymentSession(session);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showLoading(boolean show) {
        if (!isActive()) {
            return;
        }
        if (show) {
            paymentList.setVisible(false);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            paymentList.setVisible(true);
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closePage(boolean success, PaymentResult result) {
        if (!isActive()) {
            return;
        }
        setActivityResult(success, result);
        finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showMessage(String message) {
        if (!isActive()) {
            return;
        }
        showMessageDialog(message, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showMessageAndClosePage(String message, boolean success, PaymentResult result) {
        if (!isActive()) {
            return;
        }
        setActivityResult(false, result);
        showMessageDialog(message, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActivityResult(boolean success, PaymentResult result) {
        if (!isActive()) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(PaymentUI.EXTRA_PAYMENT_RESULT, result);
        int activityResult = success ? Activity.RESULT_OK : Activity.RESULT_CANCELED;
        setResult(activityResult, intent);
    }
    
    void makeChargeRequest(PaymentGroup group, Map<String, FormWidget> widgets) {
        paymentList.hideKeyboard();
        presenter.charge(widgets, group);
    }

    ValidationResult validate(PaymentGroup group, String type, String value1, String value2) {
        PaymentItem item = group.getActivePaymentItem();
        Validator validator = PaymentUI.getInstance().getValidator();
        ValidationResult result = validator.validate(item.getPaymentMethod(), type, value1, value2);

        if (!result.isError()) {
            return result;
        }
        String msg = item.translateError(result.getError());

        if (TextUtils.isEmpty(msg)) {
            msg = getString(R.string.paymentpage_error_validation);
        }
        result.setMessage(msg);
        return result;
    }

    private void showMessageDialog(final String message, final boolean finish) {
        if (!isActive()) {
            return;
        }
        MessageDialogFragment dialog = new MessageDialogFragment();
        dialog.setMessage(message);
        dialog.setNeutralButton(getString(R.string.dialog_close_button));
        dialog.setListener(new MessageDialogFragment.MessageDialogListener() {
            @Override
            public void onNeutralButtonClick() {
                if (finish) {
                    finish();
                }
            }

            @Override
            public void onCancelled() {
                if (finish) {
                    finish();
                }
            }
        });
        dialog.show(getSupportFragmentManager(), "paymentpage_dialog");
    }

    private void showSnackBar(String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        Snackbar snackbar = Snackbar.make(findViewById(R.id.layout_paymentpage), message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
