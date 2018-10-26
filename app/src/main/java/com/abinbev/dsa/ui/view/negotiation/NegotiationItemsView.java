package com.abinbev.dsa.ui.view.negotiation;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.GiveGetFilter;
import com.abinbev.dsa.model.Negotiation_Item__c;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by wandersonblough on 12/16/15.
 */
public class NegotiationItemsView extends LinearLayout {

    @Bind(R.id.gets_container)
    LinearLayout getsContainer;

    @Bind(R.id.gives_container)
    LinearLayout givesContainer;

    @Nullable
    @Bind(R.id.all_give_gets)
    ViewGroup allGiveGets;

    @Nullable
    @Bind(R.id.add_gives_button)
    Button addGivesButton;

    @Nullable
    @Bind(R.id.add_gets_button)
    Button addGetsButton;

    private boolean editable;
    private NegotiationHelper negotiationHelper;

    public NegotiationItemsView(Context context) {
        this(context, null);
    }

    public NegotiationItemsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NegotiationItemsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.negotiations_items, this);
        ButterKnife.bind(this);
    }

    public void setNegotiationGives(List<Negotiation_Item__c> gives) {
        givesContainer.removeAllViews();
        if (gives.isEmpty()) {
            givesContainer.addView(emptyView(getContext().getString(R.string.no_gives)));
        } else {
            for (Negotiation_Item__c give : gives) {
                NegotiationItem negotiationItem = new NegotiationItem(getContext());
                negotiationItem.setNegotiationItem(give);
                negotiationItem.setEditable(editable);
                givesContainer.addView(negotiationItem);
            }
        }
    }

    public void setNegotiationGets(List<Negotiation_Item__c> gets) {
        getsContainer.removeAllViews();
        if (gets.isEmpty()) {
            getsContainer.addView(emptyView(getContext().getString(R.string.no_gets)));
        } else {
            for (Negotiation_Item__c get : gets) {
                NegotiationItem negotiationItem = new NegotiationItem(getContext());
                negotiationItem.setNegotiationItem(get);
                negotiationItem.setEditable(editable); 
                getsContainer.addView(negotiationItem);
            }
        }
    }

    private TextView emptyView(String message) {
        TextView textView = new TextView(getContext());
        LinearLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        textView.setLayoutParams(lp);
        textView.setPadding(16, 16, 16, 16);
        textView.setText(message);
        return textView;
    }

    @Nullable
    @OnClick(R.id.give_get_search)
    public void goToGiveGetSearch() {
        negotiationHelper.viewGiveGetSearch(null);
    }

    @Nullable
    @OnClick(R.id.add_gives_button)
     public void goToGivesList() {
        negotiationHelper.viewGiveGetSearch(GiveGetFilter.Type.GIVE);
    }

    @Nullable
    @OnClick(R.id.add_gets_button)
    public void goToGetsList() {
        negotiationHelper.viewGiveGetSearch(GiveGetFilter.Type.GET);
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        if (addGetsButton != null) addGetsButton.setVisibility(editable ? VISIBLE : GONE);
        if (addGivesButton != null) addGivesButton.setVisibility(editable ? VISIBLE : GONE);
        if (allGiveGets != null) allGiveGets.setVisibility(editable ? VISIBLE : GONE);
    }

    public void setNegotiationHelper(NegotiationHelper negotiationHelper) {
        this.negotiationHelper = negotiationHelper;
    }
}
