package com.abinbev.dsa.ui.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.adapter.MaterialGiveAdapter;
import com.abinbev.dsa.bus.event.AddProductEvent;
import com.abinbev.dsa.model.Order__c;
import com.squareup.otto.Bus;

import javax.inject.Inject;

public class ProductQuantityPresenter implements Presenter<ProductQuantityPresenter.ViewModel> {

    private static final String TAG = ProductQuantityPresenter.class.getSimpleName();

    public interface ViewModel {
        void updateImageButtonToAdd();
        void updateImageButtonToOutline();
    }

    @Inject
    Bus eventBus;
    MaterialGiveAdapter.LineItem lineItem;

    private ViewModel viewModel;

    public ProductQuantityPresenter() {
        super();
        ((ABInBevApp) ABInBevApp.getAppContext()).getAppComponent().inject(this);
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void setLineItem(MaterialGiveAdapter.LineItem lineItem) {
        this.lineItem = lineItem;
    }

    public void setProductCount(String count) {
        this.lineItem.quantity = Integer.valueOf(count);
    }

    @Override
    public void start() {
        eventBus.register(this);
    }

    @Override
    public void stop() {
        viewModel = null;
        eventBus.unregister(this);
    }

    public boolean validate(String count) {
        if (viewModel == null) return false;

        if (!TextUtils.isEmpty(count) && TextUtils.isDigitsOnly(count) && Integer.parseInt(count) > 0) {
            viewModel.updateImageButtonToAdd();
            return true;
        } else {
            viewModel.updateImageButtonToOutline();
            return false;
        }
    }

    public void addProduct() {
        lineItem.unitOfMeasure = Order__c.UnitOfMeasure.CS.name();
        eventBus.post(new AddProductEvent.AddProduct(lineItem));
        Log.d(TAG, "new AddProductEvent.AddProduct event posted");
    }

}
