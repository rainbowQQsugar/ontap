package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.ProductNegotiationFields;
import com.abinbev.dsa.utils.AppScheduler;

import rx.Single;

/**
 * Created by Diana Błaszczyk on 17/10/17.
 */

public class PromotionViewPresenter extends AbstractRxPresenter<PromotionViewPresenter.ViewModel> implements Presenter<PromotionViewPresenter.ViewModel> {

    public interface ViewModel {
        void setPromotion(CN_Product_Negotiation__c promotion);
    }

    private static final String TAG = PromotionViewPresenter.class.getSimpleName();
    private String promotionId;


    public PromotionViewPresenter(String promotionId) {
        super();
        this.promotionId = promotionId;
    }

    @Override
    public void start() {
        super.start();
        if (promotionId != null) {
            fetchPromotion();
        }
    }

    private void fetchPromotion() {
        addSubscription(Single.fromCallable(
                () -> {
                    CN_Product_Negotiation__c promotion = CN_Product_Negotiation__c.getById(promotionId);

                    TranslatableSFBaseObject.addTranslations(promotion,
                            AbInBevObjects.PRODUCT_NEGOTIATIONS,
                                ProductNegotiationFields.PROMOTION_TYPE,
                                ProductNegotiationFields.STATUS);

                    return promotion;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        promotion -> viewModel().setPromotion(promotion),
                        error -> Log.e(TAG, "Error fetching promotion: " + promotionId, error)
                ));
    }
}
