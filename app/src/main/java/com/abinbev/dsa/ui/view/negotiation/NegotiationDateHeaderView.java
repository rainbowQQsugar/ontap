package com.abinbev.dsa.ui.view.negotiation;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.AppBaseActivity;
import com.abinbev.dsa.bus.event.NegotiationEvent;
import com.abinbev.dsa.model.Negotiation__c;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.ui.view.DateRangeHeaderView;
import com.abinbev.dsa.utils.AppScheduler;
import com.salesforce.androidsyncengine.data.model.PicklistValue;
import com.squareup.otto.Bus;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by wandersonblough on 12/8/15.
 */
public class NegotiationDateHeaderView extends DateRangeHeaderView {

    private static final String TAG = NegotiationDateHeaderView.class.getSimpleName();
    @Inject
    Bus eventBus;


    private SubmitListener submitListener;
    private Subscription subscription;

    public NegotiationDateHeaderView(Context context) {
        this(context, null);
    }

    public NegotiationDateHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NegotiationDateHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((AppBaseActivity) context).getAppComponent().inject(this);
        subscription = Subscriptions.empty();
    }

    @Override
    public boolean allowPastDate() {
        return false;
    }

    public void setupClassificationAdapter() {
        subscription = Observable.create(new Observable.OnSubscribe<List<PicklistValue>>() {
            @Override
            public void call(Subscriber<? super List<PicklistValue>> subscriber) {
                RecordType recordType = RecordType.getByName("Master RT");
                subscriber.onNext(Negotiation__c.getClassificationValues(recordType.getId()));
                subscriber.onCompleted();
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<PicklistValue>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onNext(List<PicklistValue> picklistValues) {
                        setupClassificationSpinner(picklistValues);
                    }
                });
    }

    private void setupClassificationSpinner(List<PicklistValue> picklistValues) {
        picklistValues.add(0, new PicklistValue());
        ArrayAdapter<PicklistValue> adapter = new ArrayAdapter<PicklistValue>(getContext(),
                R.layout.twoline_spinner_item, android.R.id.text1, picklistValues) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                TextView textView = (TextView) inflater.inflate(R.layout.dropdown_text_item, parent, false);
                PicklistValue picklistValue = getItem(position);
                textView.setText(picklistValue.getLabel());
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    view = inflater.inflate(R.layout.twoline_spinner_item, parent, false);
                }
                PicklistValue picklistValue = getItem(position);
                ((TextView) view.findViewById(android.R.id.text1)).setText(picklistValue.getLabel());
                return view;
            }
        };
    }

    public interface SubmitListener {
        void submit();

        void submitAndOrder();
    }

    public void setSubmitListener(SubmitListener submitListener) {
        this.submitListener = submitListener;
    }


    @Override
    public void postStartDate(String dateString) {
        eventBus.post(new NegotiationEvent.UpdateStartDate(dateString));
    }

    @Override
    public void postEndDate(String dateString) {
        eventBus.post(new NegotiationEvent.UpdateEndDate(dateString));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subscription.unsubscribe();
    }
}
