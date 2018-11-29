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

package net.optile.payment.ui.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import net.optile.payment.R;
import net.optile.payment.core.LanguageFile;
import net.optile.payment.core.PaymentInputType;
import net.optile.payment.model.InputElement;
import net.optile.payment.model.InputElementType;
import net.optile.payment.ui.theme.PaymentTheme;
import net.optile.payment.ui.theme.IconParameters;
import net.optile.payment.ui.theme.ThemeParameters;
import net.optile.payment.ui.model.PaymentCard;
import net.optile.payment.ui.widget.ButtonWidget;
import net.optile.payment.ui.widget.CheckBoxInputWidget;
import net.optile.payment.ui.widget.DateWidget;
import net.optile.payment.ui.widget.FormWidget;
import net.optile.payment.ui.widget.SelectInputWidget;
import net.optile.payment.ui.widget.TextInputWidget;
import net.optile.payment.ui.widget.WidgetPresenter;
import net.optile.payment.util.PaymentUtils;
import net.optile.payment.validation.ValidationResult;
import android.view.ContextThemeWrapper;

/**
 * The PaymentCardViewHolder holding the header and input widgets
 */
abstract class PaymentCardViewHolder extends RecyclerView.ViewHolder {

    final ViewGroup formLayout;
    final ListAdapter adapter;
    final WidgetPresenter presenter;
    final Map<String, FormWidget> widgets;

    PaymentCardViewHolder(ListAdapter adapter, View parent) {
        super(parent);

        this.adapter = adapter;
        this.formLayout = parent.findViewById(R.id.layout_form);
        this.widgets = new LinkedHashMap<>();

        View view = parent.findViewById(R.id.layout_header);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.onItemClicked(getAdapterPosition());
            }
        });

        this.presenter = new WidgetPresenter() {
            @Override
            public void onActionClicked() {
                adapter.onActionClicked(getAdapterPosition());
            }

            @Override
            public void hideKeyboard() {
                adapter.hideKeyboard(getAdapterPosition());
            }

            @Override
            public void showKeyboard() {
                adapter.showKeyboard(getAdapterPosition());
            }

            @Override
            public void showDialogFragment(DialogFragment dialog, String tag) {
                adapter.showDialogFragment(getAdapterPosition(), dialog, tag);
            }

            @Override
            public ValidationResult validate(String type, String value1, String value2) {
                return adapter.validate(getAdapterPosition(), type, value1, value2);
            }

            @Override
            public PaymentTheme getPaymentTheme() {
                return adapter.getPaymentTheme();
            }
        };
    }

    static void addInputWidgets(ListAdapter adapter, PaymentCardViewHolder holder, PaymentCard card) {
        DateWidget dateWidget = null;
        List<InputElement> elements = card.getInputElements();
        boolean containsExpiryDate = PaymentUtils.containsExpiryDate(elements);
        
        for (InputElement element : elements) {
            if (!containsExpiryDate) {
                holder.addWidget(createInputWidget(adapter, element));
                continue;
            }
            switch (element.getName()) {
                case PaymentInputType.EXPIRY_MONTH:
                    if (dateWidget == null) {
                        dateWidget = createDateWidget(adapter);
                        holder.addWidget(dateWidget);
                    }
                    dateWidget.setMonthInputElement(element);
                    break;
                case PaymentInputType.EXPIRY_YEAR:
                    if (dateWidget == null) {
                        dateWidget = createDateWidget(adapter);
                        holder.addWidget(dateWidget);
                    }
                    dateWidget.setYearInputElement(element);
                    break;
                default:
                    holder.addWidget(createInputWidget(adapter, element));
            }
        }
    }

    static FormWidget createInputWidget(ListAdapter adapter, InputElement element) {
        FormWidget widget;
        String name = element.getName();
        Context context = adapter.getContext();
        ThemeParameters params = adapter.getPaymentTheme().getThemeParameters();
        
        switch (element.getType()) {
            case InputElementType.SELECT:
                View view = View.inflate(context, R.layout.widget_input_select, null);
                return new SelectInputWidget(name, view);
            case InputElementType.CHECKBOX:
                view = inflateWithTheme(context, params.getCheckBoxTheme(), R.layout.widget_input_checkbox);
                return new CheckBoxInputWidget(name, view);
            default:
                view = View.inflate(context, R.layout.widget_input_text, null);
                return new TextInputWidget(name, view);
        }
    }

    static ButtonWidget createButtonWidget(ListAdapter adapter) {
        PaymentTheme theme = adapter.getPaymentTheme();
        ThemeParameters params = theme.getThemeParameters();
        View view = inflateWithTheme(adapter.getContext(), params.getButtonTheme(), R.layout.widget_button);
        return new ButtonWidget(PaymentInputType.ACTION_BUTTON, view);
    }

    static DateWidget createDateWidget(ListAdapter adapter) {
        Context context = adapter.getContext();
        PaymentTheme theme = adapter.getPaymentTheme();
        View view = View.inflate(context, R.layout.widget_input_date, null);
        return new DateWidget(PaymentInputType.EXPIRY_DATE, view);
    }

    static View inflateWithTheme(Context context, int themeResId, int layoutResId) {
        return View.inflate(new ContextThemeWrapper(context, themeResId), layoutResId, null);
    }
    
    void expand(boolean expand) {
        formLayout.setVisibility(expand ? View.VISIBLE : View.GONE);
    }

    void addWidget(FormWidget widget) {
        String name = widget.getName();

        if (widgets.containsKey(name)) {
            return;
        }
        widget.setPresenter(presenter);
        widgets.put(name, widget);
        formLayout.addView(widget.getRootView());
    }

    FormWidget getFormWidget(String name) {
        return widgets.get(name);
    }

    void onBind(PaymentCard paymentCard) {
        bindInputWidgets(paymentCard);
        bindDateWidget(paymentCard);
        bindButtonWidget(paymentCard);
    }

    void bindInputWidgets(PaymentCard card) {

        FormWidget widget;
        for (InputElement element : card.getInputElements()) {
            widget = getFormWidget(element.getName());
            if (widget == null) {
                continue;
            }
            bindInputWidget(widget, element);
        }
    }

    void bindInputWidget(FormWidget widget, InputElement element) {
        bindIconResource(widget);
        widget.setLabel(element.getLabel());

        if (widget instanceof SelectInputWidget) {
            ((SelectInputWidget) widget).setSelectOptions(element.getOptions());
        } else if (widget instanceof TextInputWidget) {
            ((TextInputWidget) widget).setInputType(element.getType());
        }
    }

    void bindIconResource(FormWidget widget) {
        IconParameters params = adapter.getPaymentTheme().getIconParameters();
        widget.setIconResource(params.getInputTypeIcon(widget.getName()));
    }

    void bindDateWidget(PaymentCard card) {
        String name = PaymentInputType.EXPIRY_DATE;
        DateWidget widget = (DateWidget) getFormWidget(name);

        if (widget == null) {
            return;
        }
        LanguageFile pageLang = adapter.getPageLanguageFile();
        bindIconResource(widget);
        widget.setLabel(card.getLang().translateAccount(name));
        widget.setDialogButtonLabel(pageLang.translate(LanguageFile.KEY_BUTTON_DATE));
    }

    void bindButtonWidget(PaymentCard card) {
        String name = PaymentInputType.ACTION_BUTTON;
        ButtonWidget widget = (ButtonWidget) getFormWidget(name);

        if (widget == null) {
            return;
        }
        LanguageFile pageLang = adapter.getPageLanguageFile();
        widget.setButtonLabel(pageLang.translate(card.getButton()));
    }

    void setLastImeOptions() {
        List<String> keys = new ArrayList<>(widgets.keySet());
        Collections.reverse(keys);

        for (String key : keys) {
            FormWidget widget = widgets.get(key);
            if (widget.setLastImeOptionsWidget()) {
                break;
            }
        }
    }
}
