package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.ProductNegotiationFields;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Single;

/**
 * Created by lukaszwalukiewicz on 07.01.2016.
 */
public class PromotionsListPresenter extends AbstractRxPresenter<PromotionsListPresenter.ViewModel> implements Presenter<PromotionsListPresenter.ViewModel> {

    public interface ViewModel {
        void setData(List<CN_Product_Negotiation__c> promotions);
    }

    public static final String TAG = PromotionsListPresenter.class.getSimpleName();
    private String accountId;
    private String type;

    public PromotionsListPresenter(String accountId, String type) {
        super();
        this.accountId = accountId;
        this.type = type;
    }

    @Override
    public void start() {
        super.start();
        fetchPromotions();
    }

    private void fetchPromotions() {
        addSubscription(Single.fromCallable(
                () -> {
                    List<CN_Product_Negotiation__c> promotions = type.isEmpty() ? CN_Product_Negotiation__c.getActivePromotionsByAccountId(accountId)
                            : CN_Product_Negotiation__c.getActivePromotionsByAccountIdAndType(accountId, type);

                    String objectName = AbInBevObjects.PRODUCT_NEGOTIATIONS;
                    String fieldName = ProductNegotiationFields.PROMOTION_TYPE;
                    TranslatableSFBaseObject.addTranslations(promotions, objectName, fieldName);

                    return promotions;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        promotions -> viewModel().setData(promotions),
                        error -> Log.e(TAG, "Error getting all promotions: ", error)
                ));
    }
}
