/*
 * Copyright (c) 2020 optile GmbH
 * https://www.optile.net
 *
 * This file is open source and available under the MIT license.
 * See the LICENSE file for more information.
 */

package net.optile.payment.test.view;

import static androidx.test.espresso.intent.Checks.checkNotNull;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.google.android.material.textfield.TextInputLayout;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.matcher.BoundedMatcher;
import net.optile.payment.ui.list.PaymentCardViewHolder;
import net.optile.payment.ui.widget.FormWidget;

/**
 * Class holding espresso matchers to test the Android SDK
 */
public final class PaymentMatchers {

    /**
     * Check the card testId at the absolute position in the RecyclerView.
     *
     * @param position of the card in the RecyclerView
     * @param testId compared to the content description of the root view
     * @return newly created View Matcher
     */
    public static Matcher<View> isCardWithTestId(int position, String testId) {
        checkNotNull(testId);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("match holder testId %s at position %d", testId, position));
            }

            @Override
            protected boolean matchesSafely(final RecyclerView recyclerView) {
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    return false;
                }
                return testId.equals(viewHolder.itemView.getContentDescription().toString());
            }
        };
    }

    /**
     * Check the view inside a card at the absolute position in the RecyclerView.
     *
     * @param position of the card in the RecyclerView
     * @param viewMatcher to match the view against
     * @param viewResId resource ID of the view
     * @return newly created View Matcher
     */
    public static Matcher<View> isViewInCard(int position, Matcher<View> viewMatcher, int viewResId) {
        checkNotNull(viewMatcher);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("match view in holder at position %d", position));
            }

            @Override
            public boolean matchesSafely(final RecyclerView recyclerView) {
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    return false;
                }
                View view = viewHolder.itemView.findViewById(viewResId);
                if (view == null) {
                    return false;
                }
                return viewMatcher.matches(view);
            }
        };
    }

    /**
     * Check the view inside a card widget at the absolute position in the RecyclerView.
     *
     * @param position of the card in the RecyclerView
     * @param viewMatcher to match the view against
     * @param widgetName the name of the widget, i.e. number or holderName
     * @param viewResId resource ID of the view
     * @return newly created View Matcher
     */
    public static Matcher<View> isViewInWidget(final int position, final Matcher<View> viewMatcher, final String widgetName,
        final int viewResId) {
        checkNotNull(viewMatcher);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("match view in widget %s at position %d", widgetName, position));
            }

            @Override
            public boolean matchesSafely(RecyclerView recyclerView) {
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    return false;
                }
                FormWidget widget = ((PaymentCardViewHolder) viewHolder).getFormWidget(widgetName);
                if (widget == null) {
                    return false;
                }
                View formView = widget.getRootView().findViewById(viewResId);
                if (formView == null) {
                    return false;
                }
                return viewMatcher.matches(formView);
            }
        };
    }

    public static Matcher<View> hasTextInputLayoutHint(final String expectedHint) {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextInputLayout)) {
                    return false;
                }
                CharSequence hintSequence = ((TextInputLayout) view).getHint();
                String inputHint = "";
                if (hintSequence != null) {
                    inputHint = hintSequence.toString();
                }
                return expectedHint.equals(inputHint);
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }

    public static Matcher<View> hasTextInputLayoutError(final String expectedError) {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextInputLayout)) {
                    return false;
                }
                CharSequence errorSequence = ((TextInputLayout) view).getError();
                String inputError = "";
                if (errorSequence != null) {
                    inputError = errorSequence.toString();
                }
                return expectedError.equals(inputError);
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }
}
