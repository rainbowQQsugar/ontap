package com.abinbev.dsa.ui.view.negotiation;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.model.Negotiation__c;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wandersonblough on 1/12/16.
 */
public class NegotiationDetailView extends LinearLayout {


    @Bind(R.id.type)
    TextView type;

    @Bind(R.id.status)
    TextView status;

    @Bind(R.id.product)
    TextView product;

    @Bind(R.id.description)
    TextView description;

    public NegotiationDetailView(Context context) {
        this(context, null);
    }

    public NegotiationDetailView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NegotiationDetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.negotiation_detail_view, this);
        ButterKnife.bind(this);
        setOrientation(VERTICAL);
    }

    public void setPromotion(Negotiation__c negotiation) {
        type.setText(negotiation.getType());
        status.setText(negotiation.getStatus());
    }

    public void setPromotion(CN_Product_Negotiation__c negotiation) {
        type.setText(negotiation.getTranslatedType());
        status.setText(negotiation.getTranslatedStatus());
        description.setText(negotiation.getDescription());
        product.setText(negotiation.getProductName());
    }

}
