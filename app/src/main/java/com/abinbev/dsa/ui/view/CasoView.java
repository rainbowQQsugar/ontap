package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.checkoutRules.CheckoutRule;

/**
 * Created by nchangnon on 12/1/15.
 */
public class CasoView extends CardView {

    @Bind(R.id.case_name)
    TextView caseName;

    @Bind(R.id.case_count)
    TextView caseCount;

    @Bind(R.id.caso_checkout_indicator)
    ImageView checkoutIndicator;
    private CheckoutRule checkoutRule;

    public CasoView(Context context) {
        this(context, null);
    }

    public CasoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CasoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp(context, attrs);
    }

    private void setUp(Context context, AttributeSet attrs) {
        inflate(context, R.layout.caso_view, this);
        ButterKnife.bind(this);
        setCardBackgroundColor(context.getResources().getColor(R.color.sab_white));

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CasoView,
                0, 0);

        try {
            setName(a.getString(R.styleable.CasoView_cardTitle));
            setCountColor(a.getColor(R.styleable.CasoView_scoreColor, getResources().getColor(R.color.sab_black)));
        } finally {
            a.recycle();
        }
    }

    public void setName(String name) {
        caseName.setText(name);
    }

    public void setCountColor(int colorId) {
        caseCount.setTextColor(colorId);
    }

    public void setCount(String count) {
        caseCount.setText(count);
    }

    public void setCheckoutRule(CheckoutRule checkoutRule) {
        this.checkoutRule = checkoutRule;
        setCheckoutIndicator(checkoutRule.isFulfilled());
    }

    public void setCheckoutIndicator(boolean isMandatoryTaskCompleted) {
        if (isMandatoryTaskCompleted) {
            checkoutIndicator.setImageResource(R.drawable.ic_done_black);
        } else {
            checkoutIndicator.setImageResource(R.drawable.ic_exclamation_mark_black);
        }
    }

    @OnClick(R.id.caso_checkout_indicator)
    public void displayInfoAboutMandatoryTask() {
        if (checkoutRule != null) {
            final Snackbar snackbar = Snackbar.make(this, checkoutRule.getInfoForUser(), Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }
}