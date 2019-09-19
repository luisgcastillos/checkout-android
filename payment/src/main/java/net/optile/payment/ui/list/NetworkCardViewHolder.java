/*
 * Copyright (c) 2019 optile GmbH
 * https://www.optile.net
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package net.optile.payment.ui.list;

import static net.optile.payment.core.Localization.ALLOW_RECURRENCE;
import static net.optile.payment.core.Localization.AUTO_REGISTRATION;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;
import net.optile.payment.R;
import net.optile.payment.core.Localization;
import net.optile.payment.core.PaymentInputType;
import net.optile.payment.ui.model.NetworkCard;
import net.optile.payment.ui.model.PaymentCard;
import net.optile.payment.ui.model.PaymentNetwork;
import net.optile.payment.ui.model.SmartSwitch;
import net.optile.payment.ui.theme.PaymentTheme;
import net.optile.payment.ui.widget.FormWidget;
import net.optile.payment.ui.widget.RegisterWidget;
import net.optile.payment.ui.widget.WidgetInflater;
import net.optile.payment.util.PaymentUtils;

/**
 * The NetworkCardViewHolder
 */
final class NetworkCardViewHolder extends PaymentCardViewHolder {

    private final TextSwitcher title;

    public NetworkCardViewHolder(ListAdapter adapter, View parent, NetworkCard networkCard) {
        super(adapter, parent);

        PaymentTheme theme = adapter.getPaymentTheme();
        this.title = parent.findViewById(R.id.textswitcher_title);
        initTextSwitcher(parent, theme);

        addNetworkLogos(parent, networkCard, theme);
        addElementWidgets(networkCard, theme);
        addRegisterWidgets(theme);
        addButtonWidget(theme);

        setLastImeOptions();
    }

    static ViewHolder createInstance(ListAdapter adapter, NetworkCard networkCard, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_networkcard, parent, false);
        return new NetworkCardViewHolder(adapter, view, networkCard);
    }

    private void addNetworkLogos(View parent, NetworkCard networkCard, PaymentTheme theme) {
        List<String> names = new ArrayList<>();
        List<PaymentNetwork> networks = networkCard.getPaymentNetworks();

        for (PaymentNetwork network : networks) {
            names.add(network.getCode());
        }
        addLogoViews(parent, names, theme);
    }

    void onBind(PaymentCard paymentCard) {

        if (!(paymentCard instanceof NetworkCard)) {
            throw new IllegalArgumentException("Expected Networkcard in onBind");
        }
        super.onBind(paymentCard);
        NetworkCard networkCard = (NetworkCard) paymentCard;
        PaymentNetwork network = networkCard.getVisibleNetwork();
        int size = networkCard.getPaymentNetworkSize();
        PaymentUtils.setTestId(itemView, "card", size == 1 ? "network" : "group");

        bindTitle(networkCard);
        bindLogos(networkCard);
        bindRegistrationWidget(network);
        bindRecurrenceWidget(network);
    }

    private void addRegisterWidgets(PaymentTheme theme) {
        FormWidget widget = WidgetInflater.inflateRegisterWidget(PaymentInputType.AUTO_REGISTRATION, formLayout, theme);
        addWidget(widget);
        widget = WidgetInflater.inflateRegisterWidget(PaymentInputType.ALLOW_RECURRENCE, formLayout, theme);
        addWidget(widget);
    }

    private void bindTitle(NetworkCard card) {
        SmartSwitch smartSwitch = card.getSmartSwitch();
        List<PaymentNetwork> networks = smartSwitch.getAllSelected();

        if (networks.size() == 0) {
            networks = card.getPaymentNetworks();
        }
        List<String> labels = new ArrayList<>();

        for (PaymentNetwork network : networks) {
            labels.add(network.getLabel());
        }
        title.setText(TextUtils.join(" / ", labels));
    }

    private void bindLogos(NetworkCard card) {
        List<PaymentNetwork> networks = card.getPaymentNetworks();
        SmartSwitch smartSwitch = card.getSmartSwitch();
        boolean selected;

        for (PaymentNetwork network : networks) {
            selected = !smartSwitch.hasSelected() || smartSwitch.isSelected(network);
            bindLogoView(network.getCode(), network.getLink("logo"), selected);
        }
    }

    private void bindRegistrationWidget(PaymentNetwork network) {
        RegisterWidget widget = (RegisterWidget) getFormWidget(PaymentInputType.AUTO_REGISTRATION);
        widget.setRegistrationType(network.getRegistration());
        widget.setLabel(Localization.translate(network.getCode(), AUTO_REGISTRATION, null));
    }

    private void bindRecurrenceWidget(PaymentNetwork network) {
        RegisterWidget widget = (RegisterWidget) getFormWidget(PaymentInputType.ALLOW_RECURRENCE);
        widget.setRegistrationType(network.getRecurrence());
        widget.setLabel(Localization.translate(network.getCode(), ALLOW_RECURRENCE, null));
    }

    private void initTextSwitcher(final View parent, final PaymentTheme theme) {
        final int style = theme.getListParameters().getNetworkCardTitleStyle();

        TextView tv = parent.findViewById(R.id.title0);
        PaymentUtils.setTextAppearance(tv, style);

        tv = parent.findViewById(R.id.title1);
        PaymentUtils.setTextAppearance(tv, style);
    }
}
