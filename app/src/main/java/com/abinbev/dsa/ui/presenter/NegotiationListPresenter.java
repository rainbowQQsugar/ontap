package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.abinbev.dsa.activity.ProductNegotiationDetailsActivity;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.ProductNegotiationFields;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Single;

public class NegotiationListPresenter extends AbstractRxPresenter<NegotiationListPresenter.ViewModel> implements Presenter<NegotiationListPresenter.ViewModel> {

    public static final String TAG = NegotiationListPresenter.class.getSimpleName();

    public interface ViewModel {
        void setData(List<CN_Product_Negotiation__c> negotiations);
    }

    private String accountId;


    public NegotiationListPresenter(String accountId) {
        super();
        this.accountId = accountId;
    }

    @Override
    public void start() {
        super.start();
        addSubscription(Single.fromCallable(
                () -> {
                    List<CN_Product_Negotiation__c> negotiations = CN_Product_Negotiation__c.getNegotiationsByAccountId(accountId);

                    String objectName = AbInBevObjects.PRODUCT_NEGOTIATIONS;
                    String fieldName = ProductNegotiationFields.STATUS;
                    TranslatableSFBaseObject.addTranslations(negotiations, objectName, fieldName);

                    TranslatableSFBaseObject.addRecordTypeTranslations(negotiations, objectName, CN_Product_Negotiation__c.FIELD_RECORD_NAME);

                    return negotiations;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        negotiations -> viewModel().setData(negotiations),
                        error -> Log.e(TAG, "Error getting all negotiations: ", error)
                ));
    }

    @Override
    public void stop() {
        super.stop();
    }

    public void createNewProductNegotiation(Context context, String recordType) {
        Intent intent = new Intent(context, ProductNegotiationDetailsActivity.class);
        intent.putExtra(ProductNegotiationDetailsActivity.ARGS_ACCOUNT_ID, accountId);
        intent.putExtra(ProductNegotiationDetailsActivity.ARGS_NEGOTIATION_ID, ProductNegotiationDetailsActivity.NEW_NEGOTIATION);
        intent.putExtra(ProductNegotiationDetailsActivity.ARGS_RECORD_TYPE_ID, recordType);
        context.startActivity(intent);
    }

    public void goToNegotiationDetail(Context context, String negotiationId) {
        Intent intent = new Intent(context, ProductNegotiationDetailsActivity.class);
        intent.putExtra(ProductNegotiationDetailsActivity.ARGS_ACCOUNT_ID, accountId);
        intent.putExtra(ProductNegotiationDetailsActivity.ARGS_NEGOTIATION_ID, negotiationId);
        context.startActivity(intent);
    }

}
