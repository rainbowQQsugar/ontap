package com.abinbev.dsa.ui.view.negotiation;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.AppBaseActivity;
import com.abinbev.dsa.bus.event.NegotiationEvent;
import com.abinbev.dsa.model.Negotiation_Item__c;
import com.abinbev.dsa.model.Material_Get__c;
import com.abinbev.dsa.model.Material_Give__c;
import com.abinbev.dsa.model.Paquetes_por_segmento__c;
import com.abinbev.dsa.ui.presenter.GiveGetPresenter;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wandersonblough on 12/9/15.
 */
public class PackagePageView extends LinearLayout implements GiveGetPresenter.ViewModel {

    @Inject
    Bus eventBus;

    @Bind(R.id.gives)
    LinearLayout giveContainer;

    @Bind(R.id.gets)
    LinearLayout getContainer;

    @Bind(R.id.gives_header)
    TextView givesHeader;

    Paquetes_por_segmento__c paquete;
    GiveGetPresenter giveGetPresenter;

    public PackagePageView(Context context) {
        this(context, null);
    }

    public PackagePageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PackagePageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((AppBaseActivity) context).getAppComponent().inject(this);
        inflate(context, R.layout.negotiations_packages_page, this);
        ButterKnife.bind(this);
        eventBus.register(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        giveGetPresenter.stop();
        eventBus.unregister(this);
    }

    public void setPaquete(Paquetes_por_segmento__c paquete) {
        this.paquete = paquete;
        giveGetPresenter = new GiveGetPresenter(paquete.getPackageId());
        giveGetPresenter.setViewModel(this);
        giveGetPresenter.start();
    }

    @Override
    public void setGives(List<Material_Give__c> gives) {
        giveContainer.removeAllViews();
        for (Material_Give__c give : gives) {
            NegotiationItem negotiationItem = new NegotiationItem(getContext());
            negotiationItem.setMaterial__c(give, false);
            giveContainer.addView(negotiationItem);
        }
    }

    @Override
    public void setGets(List<Material_Get__c> gets) {
        getContainer.removeAllViews();
        for (Material_Get__c get : gets) {
            NegotiationItem negotiationItem = new NegotiationItem(getContext());
            negotiationItem.setMaterial__c(get, false);
            getContainer.addView(negotiationItem);
        }
    }

    public void setCustomerName(String customerName) {
        givesHeader.setText(String.format(getResources().getString(R.string.gives_header), customerName));
    }

    @Subscribe
    public void itemRemoved(NegotiationEvent.RemoveItem event) {
        updateItems(event.getMaterial__c(), false);
    }

    @Subscribe
    public void itemAdded(NegotiationEvent.AddItem event) {
        updateItems(event.getMaterial__c(), true);
    }

    @Subscribe
    public void itemsUpdated(NegotiationEvent.UpdateItems event) {
        givesCheck(event.getItems());
        getsCheck(event.getItems());
    }

    public void givesCheck(List<Negotiation_Item__c> gives) {
        for (int i = 0; i < giveContainer.getChildCount(); i++) {
            NegotiationItem negotiationItem = (NegotiationItem) giveContainer.getChildAt(i);
            boolean match = false;
            for (Negotiation_Item__c item : gives) {
                if (item.material__c.getId().equals(negotiationItem.getMaterial__c().getId())) {
                    match = true;
                    break;
                }
            }
            negotiationItem.activate(match);
        }
    }

    public void getsCheck(List<Negotiation_Item__c> gets) {
        for (int i = 0; i < getContainer.getChildCount(); i++) {
            NegotiationItem negotiationItem = (NegotiationItem) getContainer.getChildAt(i);
            for (Negotiation_Item__c item : gets) {
                negotiationItem.activate(item.material__c.getId().equals(negotiationItem.getMaterial__c().getId()));
            }
        }
    }

    private void updateItems(Material__c material__c, boolean isActivated) {
        if (material__c instanceof Material_Get__c) {
            for (int i = 0; i < getContainer.getChildCount(); i++) {
                NegotiationItem item  = (NegotiationItem) getContainer.getChildAt(i);
                item.updateItem(material__c, isActivated);
            }
        } else {
            for (int i = 0; i < giveContainer.getChildCount(); i++) {
                NegotiationItem item  = (NegotiationItem) giveContainer.getChildAt(i);
                item.updateItem(material__c, isActivated);
            }
        }
    }
}
