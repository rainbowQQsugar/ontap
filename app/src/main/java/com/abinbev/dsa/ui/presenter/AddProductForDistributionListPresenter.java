package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.model.Distribution;
import com.abinbev.dsa.model.Product;
import com.abinbev.dsa.utils.AbInBevConstants.DistributionCategory;
import com.abinbev.dsa.utils.AppScheduler;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;

import org.json.JSONObject;

import rx.Observable;

public class AddProductForDistributionListPresenter extends AbstractRxPresenter<AddProductForDistributionListPresenter.ViewModel> {

    private static final String TAG = "AddProductForDPresenter";

    public interface ViewModel {
        void setDistribution(Distribution distribution);

        void closeOnSuccess();
    }

    private final String productId;

    private final String accountId;

    private boolean isFirstStart = true;

    public AddProductForDistributionListPresenter(String productId, String accountId) {
        super();
        this.productId = productId;
        this.accountId = accountId;
    }

    @Override
    public void start() {
        super.start();
        if (isFirstStart) {
            loadData();
            isFirstStart = false;
        }
    }

    private void loadData() {
        addSubscription(Observable.fromCallable(
                () -> {
                    Product product = Product.getById(productId);
                    Distribution distribution = new Distribution(new JSONObject());
                    if (product.competitorFlag()) {
                        distribution.setCategory(DistributionCategory.COMPETITION_BRAND);
                    }
                    else {
                        distribution.setCategory(DistributionCategory.ABI_BRAND);
                    }
                    distribution.setBrand(product.getChBrand());
                    distribution.setSKUName(product.getProductShortName());
                    distribution.setIsActive(true);
                    distribution.setPackage(product.getPackage());
                    distribution.setUnit(product.getProductUnit());
                    distribution.setAccountId(accountId);
                    distribution.setProductId(product.getId());

                    return distribution;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        distribution -> viewModel().setDistribution(distribution),
                        error -> Log.e(TAG, "Error creating distribution: ", error)
                ));
    }

    public void saveDistribution(Distribution distribution) {
        addSubscription(Observable.fromCallable(
                () -> {
                    String newId = distribution.createRecord();
                    SyncUtils.TriggerRefresh(ABInBevApp.getAppContext());
                    return newId;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        newId -> viewModel().closeOnSuccess(),
                        error -> Log.e(TAG, "Error saving distribution: ", error)
                ));
    }
}
