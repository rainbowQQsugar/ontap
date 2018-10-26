package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.activity.ProductNegotiationDetailsActivity;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.model.Product;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.utils.DSAConstants;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;


/**
 * Created by Diana Błaszczyk on 19/10/17.
 */

public class PromotionPresenter extends AbstractRxPresenter<PromotionPresenter.ViewModel> implements Presenter<PromotionPresenter.ViewModel> {

    private static final String TAG = "NegotiationPresenter";

    public interface ViewModel {
        void setProducts(List<Product> products);
        void askIfClose();
        void submitted();
        void setNegotiation(CN_Product_Negotiation__c negotiation);
        void updateAccountInfo(Account account);
    }

    public List<CN_Product_Negotiation__c> negotiationItems;

    private String negotiationId;
    private String accountId;
    private String description = "";
    private String type;
    private String status;
    private String category;
    private String productId;
    private String pckg;
    private String brand;
    private String unit;
    private String ptr;
    private String recordTypeId;


    public PromotionPresenter(String negotiationId, String accountId) {
        super();
        this.accountId = accountId;
        this.negotiationId = negotiationId;
        negotiationItems = new ArrayList<>();
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public void setPromotionType(String type) {
        this.type = type;
    }

    public void setRecordTypeId(String id) {
        this.recordTypeId = id;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setPackage(String pckg) {
        this.pckg = pckg;
    }

    public void setPTR(String ptr) {
        this.ptr = ptr;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setProductId(String id) {
        this.productId = id;
    }

    @Override
    public void start() {
       super.start();
       getAccount();
       fetchProducts();
       fetchNegotiation();
    }

    public void getAccount() {
        addSubscription(Observable.fromCallable(
                () -> {
                    Account account = new Account(DataManagerFactory.getDataManager().exactQuery(DSAConstants.DSAObjects.ACCOUNT, "Id", accountId));
                    return account;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        account -> viewModel().updateAccountInfo(account),
                        error -> Log.e(TAG, "Error on getting account info: ", error)
                ));
    }

    private void fetchProducts() {
        addSubscription(Observable.fromCallable(
                () -> {
                    List<Product> products = Product.getAllProducts();
                    return products;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        products -> viewModel().setProducts(products),
                        error -> Log.e(TAG, "onError: ", error)
                ));
    }

    private void fetchNegotiation() {
        addSubscription(Observable.fromCallable(
                () -> {
                    if (negotiationId.equals(ProductNegotiationDetailsActivity.NEW_NEGOTIATION)) {
                        return null;
                    } else {
                        CN_Product_Negotiation__c negotiation = CN_Product_Negotiation__c.getById(negotiationId);
                        if (negotiation != null) {
                            description =  negotiation.getDescription();
                            type =  negotiation.getType();
                            status = negotiation.getStatus();
                            productId =  negotiation.getProductId();
                            category = negotiation.getCategory();
                            brand = negotiation.getBrand();
                            unit = negotiation.getUnit();
                            ptr = negotiation.getPTR();
                            pckg = negotiation.getPackage();
                            recordTypeId = negotiation.getRecordTypeId();
                        }
                        return negotiation;
                    }
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        negotiation -> viewModel().setNegotiation(negotiation),
                        error -> Log.e(TAG, "onError: ", error)
                ));
    }

    @Override
    public void stop() {
        super.stop();
    }

    public boolean isModified() {
        return !description.isEmpty();
    }

    public boolean onBackPressed() {
        if (isModified()) {
            viewModel().askIfClose();
            return true;
        }
        else {
            return false;
        }
    }


    public void saveSubmitNegotiation() {
        addSubscription(Observable.fromCallable(
                () -> {
                    String negotiationId;
                    if (recordTypeId.equals(RecordType.getByNameAndObjectType("Promotion", AbInBevConstants.AbInBevObjects.PRODUCT_NEGOTIATIONS).getId()))
                        negotiationId = CN_Product_Negotiation__c.createPromotionNegotiationForAccount(accountId, status, type, category, description,
                            productId, brand, unit);
                    else
                        negotiationId = CN_Product_Negotiation__c.createSellInNegotiationForAccount(accountId, status, productId, pckg,
                                ptr, brand, unit);
                    return negotiationId;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        negotiationId -> viewModel().submitted(),
                        error -> Log.e(TAG, "onError: Error creating Negotiation", error)
                ));
    }

    public void updateNegotiation(final String negotiationId) {
        addSubscription(Observable.fromCallable(
                () -> {
                    boolean success = false;
                    if (recordTypeId.equals(RecordType.getByNameAndObjectType("Promotion", AbInBevConstants.AbInBevObjects.PRODUCT_NEGOTIATIONS).getId()))
                        success = CN_Product_Negotiation__c.updatePromotionNegotiation(accountId, negotiationId, status, type, category, description,
                            productId, brand, unit);
                    else
                        success = CN_Product_Negotiation__c.updateSellInNegotiation(negotiationId, accountId, status, productId, pckg,
                                ptr, brand, unit);
                    return success;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        success -> viewModel().submitted(),
                        error -> Log.e(TAG, "onError: Error updating Negotiation", error)
                ));
    }

}
