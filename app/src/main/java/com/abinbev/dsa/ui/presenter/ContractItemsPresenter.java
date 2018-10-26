package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.CN_PBO_Contract_Item__c;
import com.abinbev.dsa.model.Product;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Observable;

/**
 * Created by Jakub Stefanowski
 */
public class ContractItemsPresenter extends AbstractRxPresenter<ContractItemsPresenter.ViewModel> {

    public static final String TAG = ContractItemsPresenter.class.getSimpleName();

    public interface ViewModel {
        void setData(List<CN_PBO_Contract_Item__c> contractItems);
    }

    String contractId;

    public ContractItemsPresenter(String contractId) {
        super();
        this.contractId = contractId;
    }

    @Override
    public void start() {
        super.start();
        addSubscription(Observable.fromCallable(
                () -> {
                    List<CN_PBO_Contract_Item__c> contractItems = CN_PBO_Contract_Item__c.getByContract(contractId);
                    for (CN_PBO_Contract_Item__c contractItem: contractItems) {
                        Product product = Product.getById(contractItem.getProductId());

                        if (product != null) {
                            contractItem.setProductName(product.getProductShortName());
                        }
                    }

                    return contractItems;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        contractItems -> viewModel().setData(contractItems),
                        error -> Log.e(TAG, "Error getting all negotiations: ", error)
                ));
    }
}
