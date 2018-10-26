package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.graphics.Paint;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Event;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wandersonblough on 12/1/15.
 */
public class CollapsedAccountInfo extends LinearLayout {

    @Bind(R.id.txtName)
    TextView name;

    @Bind(R.id.txtDuration)
    TextView duration;

    @Bind(R.id.txtAccount)
    TextView details;

    @Bind(R.id.txtAddress)
    TextView address;

    @Bind(R.id.txtNeighborhood)
    TextView neighborhood;

    @Bind(R.id.txtCity)
    TextView city;

    @Bind(R.id.txtPhone)
    TextView phone;

    AccountInfoCallback callback;
    Event event;

    public interface AccountInfoCallback {
        void onDetailsClick(Event event);

        void onGetDirections(Event event);
    }

    public CollapsedAccountInfo(Context context) {
        this(context, null);
    }

    public CollapsedAccountInfo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CollapsedAccountInfo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.merge_collapsed_account_info, this);
        ButterKnife.bind(this);
    }

    public void setCallback(AccountInfoCallback callback) {
        this.callback = callback;
    }

    public void setEvent(Event event) {
        this.event = event;
        Account account = event.getAccount();
        name.setText(account.getName());
        details.setPaintFlags(details.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        address.setText(account.getAddress());
        neighborhood.setText(account.getNeighborhood());
        city.setText(account.getCity());
        duration.setText(getResources().getQuantityString(R.plurals.duration, (int) event.getEstimatedVisitTime(), (int) event.getEstimatedVisitTime()));

        String phoneString = account.getFirstAvailablePhone();
        if (TextUtils.isEmpty(phoneString)) {
            phone.setVisibility(GONE);
        }
        else {
            phone.setVisibility(VISIBLE);
            phone.setText(PhoneNumberUtils.formatNumber(phoneString));
        }
    }

    @OnClick(R.id.address_container)
    public void getDirections() {
        if (callback != null && event != null) {
            callback.onGetDirections(event);
        }
    }

    @OnClick(R.id.txtAccount)
    public void showDetails() {
        if (callback != null && event != null) {
            callback.onDetailsClick(event);
        }
    }
}
