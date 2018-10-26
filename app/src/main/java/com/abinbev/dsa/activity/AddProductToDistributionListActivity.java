package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Distribution;
import com.abinbev.dsa.ui.presenter.AddProductForDistributionListPresenter;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.DistributionFields;
import com.salesforce.androidsyncengine.data.layouts.Details;
import com.salesforce.androidsyncengine.data.layouts.LayoutItem;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

import java.util.Iterator;

import butterknife.OnClick;

public class AddProductToDistributionListActivity extends DynamicEditActivity implements AddProductForDistributionListPresenter.ViewModel {

    private static final String TAG = "AddProductToDisActivity";

    public static final String ARGS_PRODUCT_ID = "product_id";
    public static final String ARGS_ACCOUNT_ID = "account_id";
    private static final CharSequence CN_DISTRIBUTION_LIST_NAME_FIELD = "Name";

    private AddProductForDistributionListPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.add_to_distribution_list_title);
        getSupportActionBar().setSubtitle(R.string.distribution_list_title);

        Intent intent = getIntent();
        String productId = intent.getStringExtra(ARGS_PRODUCT_ID);
        String accountId = intent.getStringExtra(ARGS_ACCOUNT_ID);

        presenter = new AddProductForDistributionListPresenter(productId, accountId);
    }

    @Override
    protected boolean isUpdateable(LayoutItem layoutItem, Details details, String fieldName, String section) {

        if (DistributionFields.POC_ID.equals(fieldName)) {
            return false;
        }
        else {
            return super.isUpdateable(layoutItem, details, fieldName, section);
        }
    }

    @Override
    protected void setLayoutComponentLabel(View labelView, String viewType, String fieldName, CharSequence label, CharSequence value) {
        if (fieldName.equals(CN_DISTRIBUTION_LIST_NAME_FIELD)) {
            labelView.setVisibility(View.GONE);
        }
        else {
            super.setLayoutComponentLabel(labelView, viewType, fieldName, label, value);
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_add_product_to_distribution;
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.stop();
    }

    @Override /* AddProductForDistributionListPresenter.ViewModel */
    public void setDistribution(Distribution distribution) {
        buildLayout(AbInBevObjects.CN_DISTRIBUTION, distribution);
    }

    @Override /* AddProductForDistributionListPresenter.ViewModel */
    public void closeOnSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    @OnClick(R.id.save_button)
    void onSaveClicked() {
        Distribution distribution = (Distribution) baseObject;
        Log.i(TAG, "original Values: " + distribution.toJson().toString());

        JSONObject updatedObject = getUpdatedJSONObject();
        Log.i(TAG, "updated Values: " + updatedObject.toString());

        if (testRequiredFields(updatedObject)) {
            addValues(distribution, updatedObject);
            Log.i(TAG, "result: " + distribution.toJson().toString());
            presenter.saveDistribution(distribution);
        }
    }

    private void addValues(SFBaseObject originalObject, JSONObject updatedObject) {
        Iterator it = updatedObject.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            originalObject.setValueForKey(key, updatedObject.opt(key));
        }
    }

    protected void showSnackbar(int errorResId) {
        Snackbar snackbar = Snackbar.make(mainLayout, errorResId, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.dismiss, v -> snackbar.dismiss());
        snackbar.show();
    }
}
