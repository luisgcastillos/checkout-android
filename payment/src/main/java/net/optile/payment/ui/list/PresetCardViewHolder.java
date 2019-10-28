/*
 * Copyright (c) 2019 optile GmbH
 * https://www.optile.net
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package net.optile.payment.ui.list;

import static net.optile.payment.localization.LocalizationKey.LIST_PRESET_TEXT;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import net.optile.payment.R;
import net.optile.payment.localization.Localization;
import net.optile.payment.model.AccountMask;
import net.optile.payment.ui.PaymentTheme;
import net.optile.payment.ui.model.PaymentCard;
import net.optile.payment.ui.model.PresetCard;
import net.optile.payment.util.PaymentUtils;

/**
 * The PresetCardViewHolder class holding and binding views for an PresetCard
 */
final class PresetCardViewHolder extends PaymentCardViewHolder {

    private final TextView title;
    private final TextView subTitle;

    private PresetCardViewHolder(ListAdapter adapter, View parent, PresetCard presetCard) {
        super(adapter, parent);
        this.title = parent.findViewById(R.id.text_title);
        this.subTitle = parent.findViewById(R.id.text_subtitle);
        addLogoView(parent, presetCard.getCode());
        PaymentTheme theme = adapter.getPaymentTheme();
        addButtonWidget(theme);
        addLabelWidget(theme);
    }

    static RecyclerView.ViewHolder createInstance(ListAdapter adapter, PresetCard presetCard, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_presetcard, parent, false);
        return new PresetCardViewHolder(adapter, view, presetCard);
    }

    void onBind(PaymentCard paymentCard) {

        if (!(paymentCard instanceof PresetCard)) {
            throw new IllegalArgumentException("Expected PresetCard in onBind");
        }
        super.onBind(paymentCard);
        PaymentUtils.setTestId(itemView, "card", "preset");
        PresetCard card = (PresetCard) paymentCard;
        AccountMask mask = card.getMaskedAccount();
        bindMaskedTitle(title, mask, card.getPaymentMethod());
        bindMaskedSubTitle(subTitle, mask);
        bindLogoView(paymentCard.getCode(), card.getLink("logo"), true);
        bindLabelWidget(Localization.translate(LIST_PRESET_TEXT));
    }
}
