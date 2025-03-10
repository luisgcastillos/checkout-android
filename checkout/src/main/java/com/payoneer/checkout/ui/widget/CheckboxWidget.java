/*
 * Copyright (c) 2020 Payoneer Germany GmbH
 * https://www.payoneer.com
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package com.payoneer.checkout.ui.widget;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.payoneer.checkout.R;
import com.payoneer.checkout.core.PaymentException;
import com.payoneer.checkout.form.Operation;
import com.payoneer.checkout.markdown.MarkdownSpannableStringBuilder;
import com.payoneer.checkout.model.CheckboxMode;

import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Widget for showing the CheckBox input element
 */
public class CheckboxWidget extends FormWidget {

    SwitchMaterial switchView;
    TextView labelView;

    public CheckboxWidget(String category, String name) {
        super(category, name);
    }

    @Override
    public View inflate(ViewGroup parent) {
        inflateWidgetView(parent, R.layout.widget_checkbox);
        labelView = widgetView.findViewById(R.id.label_checkbox);
        switchView = widgetView.findViewById(R.id.switch_checkbox);
        labelView.setMovementMethod(LinkMovementMethod.getInstance());
        return widgetView;
    }

    @Override
    public void putValue(Operation operation) throws PaymentException {
        operation.putBooleanValue(category, name, switchView.isChecked());
    }

    /**
     * Bind this CheckboxWidget to the mode and label.
     * For now the required and required preselected are handled the same as the optional modes.
     * This is because there is no requireMsg yet defined for checkboxes in the localization files.
     *
     * @param mode checkbox mode
     * @param label shown to the user
     */
    public void onBind(String mode, String label) {
        labelView.setText(MarkdownSpannableStringBuilder.createFromText(label));
        switch (mode) {
            case CheckboxMode.OPTIONAL:
            case CheckboxMode.REQUIRED:
                setVisible(true);
                switchView.setVisibility(View.VISIBLE);
                switchView.setChecked(false);
                break;
            case CheckboxMode.OPTIONAL_PRESELECTED:
            case CheckboxMode.REQUIRED_PRESELECTED:
                setVisible(true);
                switchView.setVisibility(View.VISIBLE);
                switchView.setChecked(true);
                break;
            case CheckboxMode.FORCED:
                setVisible(false);
                switchView.setVisibility(View.GONE);
                switchView.setChecked(true);
                break;
            case CheckboxMode.FORCED_DISPLAYED:
                setVisible(true);
                switchView.setVisibility(View.GONE);
                switchView.setChecked(true);
                break;
            default:
                setVisible(false);
                switchView.setVisibility(View.GONE);
                switchView.setChecked(false);
        }
    }
}
