package com.abinbev.dsa.ui.view.negotiation;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.AppBaseActivity;
import com.abinbev.dsa.bus.event.NegotiationEvent;
import com.abinbev.dsa.model.Negotiation_Item__c;
import com.abinbev.dsa.ui.presenter.MaterialPresenter;
import com.abinbev.dsa.utils.ContentUtils;
import com.abinbev.dsa.utils.SimpleTextWatcher;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by wandersonblough on 12/9/15.
 */
public class NegotiationItem extends RelativeLayout implements MaterialPresenter.ViewModel {

    @Inject
    Bus eventBus;

    @Bind(R.id.code)
    TextView code;

    @Bind(R.id.name)
    TextView name;

    @Bind(R.id.comment)
    TextView comment;

    @Bind(R.id.count_container)
    ViewGroup countContainer;

    @Bind(R.id.quantity)
    EditText quantity;

    @Bind(R.id.action_btn)
    ImageView actionBtn;

    Material__c material__c;
    Negotiation_Item__c negotiationItem;

    public NegotiationItem(Context context) {
        this(context, null);
    }

    public NegotiationItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NegotiationItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((AppBaseActivity) context).getAppComponent().inject(this);
        inflate(context, R.layout.negotiation_item, this);
        ButterKnife.bind(this);
    }

    public void setNegotiationItem(Negotiation_Item__c negotiationItem___c) {
        this.negotiationItem = negotiationItem___c;

        quantity.setText(negotiationItem.getQuantity());
        quantity.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (TextUtils.isEmpty(quantity.getText())) {
                        quantity.setError("Invalid");
                        eventBus.post(new NegotiationEvent.UpdateQuantity(material__c.getId(), -1));
                    }
                }
                return false;
            }
        });
        quantity.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                super.afterTextChanged(editable);
                String text = editable.toString();
                if (!TextUtils.isEmpty(text) && TextUtils.isDigitsOnly(text)) {
                    if (Integer.valueOf(text) == 0) {
                        eventBus.post(new NegotiationEvent.UpdateQuantity(material__c.getId(), -1));
                    } else {
                        eventBus.post(new NegotiationEvent.UpdateQuantity(material__c.getId(), Integer.parseInt(text)));
                    }
                }
            }
        });
        setMaterial__c(negotiationItem.material__c, negotiationItem);
    }

    public void setMaterial__c(Material__c material__c, Negotiation_Item__c negotiationItem, boolean isRecord) {
        this.material__c = material__c;
        code.setText(material__c.getCode());
        name.setText(material__c.getName());

        if (negotiationItem != null) {
            String commentValue = negotiationItem.getSavedComment();
            if (ContentUtils.isStringValid(commentValue)) {
                comment.setText(commentValue);
                comment.setVisibility(View.VISIBLE);
            } else {
                comment.setVisibility(View.GONE);
            }
        }
        countContainer.setVisibility(isRecord ? VISIBLE : GONE);
        actionBtn.setActivated(isRecord);
    }

    public Material__c getMaterial__c() {
        return material__c;
    }

    public void activate(Boolean activate) {
        if (activate) {
            actionBtn.setActivated(true);
        }
    }

    @OnClick(R.id.action_btn)
    public void onActionClicked(ImageView view) {
        if (view.isActivated()) {
            eventBus.post(new NegotiationEvent.RemoveItem(material__c));
            view.setActivated(false);
        } else {
            eventBus.post(new NegotiationEvent.AddItem(material__c));
            view.setActivated(true);
        }
    }

    public void updateItem(Material__c material__c, boolean isActivated) {
        if (this.material__c.getId().equals(material__c.getId())) {
            actionBtn.setActivated(isActivated);
        }
    }

    @Override
    public void setMaterial__c(Material__c material__c, Negotiation_Item__c negotiationItem) {
        setMaterial__c(material__c, negotiationItem, true);
    }

    public void setMaterial__c(Material__c material__c, boolean isRecord) {
        setMaterial__c(material__c, null, isRecord);
    }

    public void setEditable(boolean editable) {
        actionBtn.setVisibility(editable ? VISIBLE : GONE);
        quantity.setEnabled(editable);

    }
}
