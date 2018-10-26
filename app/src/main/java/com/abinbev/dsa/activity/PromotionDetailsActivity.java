package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.ui.presenter.PromotionViewPresenter;
import com.abinbev.dsa.ui.view.negotiation.NegotiationDetailView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Diana Błaszczyk on 17/10/17.
 */

public class PromotionDetailsActivity extends AppBaseActivity implements PromotionViewPresenter.ViewModel {

    public static final String PROMOTION_ID = "PROMOTION_ID";

    private static final String TAG = AssetViewActivity.class.getSimpleName();

    protected String promotionId;
    protected PromotionViewPresenter promotionViewPresenter;

    @Bind(R.id.negotiation_detail_view)
    NegotiationDetailView negotiationDetailView;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null) {
            promotionId = getIntent().getStringExtra(PROMOTION_ID);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_activity_promotion));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    @Override
    protected void onResume() {
        super.onResume();
        promotionViewPresenter = new PromotionViewPresenter(promotionId);
        promotionViewPresenter.setViewModel(this);
        promotionViewPresenter.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        promotionViewPresenter.stop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        promotionViewPresenter.stop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.promotion_detail_view;
    }


    @Override
    public void setPromotion(CN_Product_Negotiation__c promotion) {
        if (promotion != null) {
        //  getSupportActionBar().setSubtitle(promotion.getName());
            negotiationDetailView.setPromotion(promotion);
        }
    }

}
