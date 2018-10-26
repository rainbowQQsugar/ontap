package com.abinbev.dsa.ui.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.adapter.MaterialGiveAdapter;
import com.abinbev.dsa.bus.event.AddProductEvent;
import com.abinbev.dsa.model.Order__c;
import com.abinbev.dsa.utils.AppScheduler;
import com.salesforce.androidsyncengine.data.model.PicklistValue;
import com.squareup.otto.Bus;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public class ProductAllPresenter implements Presenter<ProductAllPresenter.ViewModel> {

    private static final String TAG = ProductAllPresenter.class.getSimpleName();

    public interface ViewModel {
        void updateImageButtonToAdd();
        void updateImageButtonToOutline();
        void setReasonPicklist(List<PicklistValue> picklistValues, String selection);
    }

    @Inject
    Bus eventBus;

    private MaterialGiveAdapter.LineItem lineItem;
    private ViewModel viewModel;
    private Subscription subscription;
    private String recordTypeId;
    public ProductAllPresenter(String recordTypeId) {
        super();
        this.recordTypeId = recordTypeId;
        subscription = Subscriptions.empty();
        ((ABInBevApp) ABInBevApp.getAppContext()).getAppComponent().inject(this);
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        eventBus.register(this);
        subscription.unsubscribe();

        fetchReasonPicklistValues()
            .subscribeOn(AppScheduler.background())
            .observeOn(AppScheduler.main())
            .subscribe(new Subscriber<List<PicklistValue>>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    Log.d(TAG, "Error getting reason picklist values:", e);
                }

                @Override
                public void onNext(List<PicklistValue> picklistValues) {
                    if (viewModel != null) {
                        viewModel.setReasonPicklist(picklistValues, lineItem.reason);
                    }
                }
            });
    }

    private Observable<List<PicklistValue>> fetchReasonPicklistValues() {
        return Observable.just(Order__c.getReasonPicklistMetadata(recordTypeId));
    }

    public void setLineItem(MaterialGiveAdapter.LineItem lineItem) {
        this.lineItem = lineItem;
    }

    public void setProductCount(String count) {
        this.lineItem.quantity = Integer.valueOf(count);
    }

    public void setReasonSelected(PicklistValue picklistValue) {
        this.lineItem.reason = picklistValue.getValue();
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.lineItem.unitOfMeasure = unitOfMeasure ;
    }


    @Override
    public void stop() {
        eventBus.unregister(this);
        subscription.unsubscribe();
        viewModel = null;
    }

    public boolean validate(String count, PicklistValue reasonSelection) {
        if (viewModel == null) return false;

        if (!TextUtils.isEmpty(count) && TextUtils.isDigitsOnly(count)
                && Integer.parseInt(count) > 0 && reasonSelection != null) {
            viewModel.updateImageButtonToAdd();
            return true;
        } else {
            viewModel.updateImageButtonToOutline();
            return false;
        }
    }

    public void addProduct(double quantity, PicklistValue picklistValue, String unitOfMeasure) {
        lineItem.reason = picklistValue.getValue();
        lineItem.unitOfMeasure = unitOfMeasure;
        eventBus.post(new AddProductEvent.AddProduct(lineItem));
        Log.d(TAG, "Product Added with productID: " + lineItem.product.getId() + " and quantity: " + quantity);
    }

}
