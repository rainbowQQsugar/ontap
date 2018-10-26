package com.abinbev.dsa.ui.view.negotiation;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.ui.presenter.ObservationPresenter;
import com.abinbev.dsa.utils.SimpleTextWatcher;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wandersonblough on 12/9/15.
 */
public class ObservationsView extends LinearLayout implements ObservationPresenter.ViewModel {

    @Bind(R.id.observations_editable)
    EditText observationField;

    @Bind(R.id.observations)
    TextView observations;

//    @Bind(R.id.view_promo_codes)
//    TextView promoCodesButton;

    boolean editable;
    NegotiationHelper negotiationHelper;

    public ObservationsView(Context context) {
        this(context, null);
    }

    public ObservationsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ObservationsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.negotiations_observations, this);
        ButterKnife.bind(this);

        observationField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                super.afterTextChanged(editable);
                String text = editable.toString();
                if (negotiationHelper != null) {
                    negotiationHelper.updateObservations(text);
                }
            }
        });
    }

    public void setNegotiationHelper(NegotiationHelper negotiationHelper) {
        this.negotiationHelper = negotiationHelper;
    }

    public void setObservation(String observation) {
        if (editable) {
            observationField.setText(observation);
        } else {
            observations.setText(observation);
        }
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        observationField.setVisibility(editable ? VISIBLE : GONE);
        observations.setVisibility(editable ? GONE : VISIBLE);
//        promoCodesButton.setVisibility(editable ? VISIBLE : GONE);
    }

//    @OnClick(R.id.view_promo_codes)
//    public void viewPromoCodes() {
//        negotiationHelper.viewPromoCodes();
//    }

}
