package com.abinbev.dsa.model.checkoutRules;

import static com.salesforce.androidsdk.smartstore.store.SmartStore.SOUP_LAST_MODIFIED_DATE;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.abinbev.dsa.activity.AssetsListActivity;
import com.abinbev.dsa.adapter.AssetsListAdapter;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Account_Asset__c;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.MandatoryTaskDetail;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Diana Błaszczyk on 06/10/17.
 */

public class AssetsTrackingCheckoutRule extends CheckoutRule {

    private static final String TAG = "AssetsCheckoutRule";

    private MandatoryTaskDetail mandatoryTaskDetail;
    private Account account;
    private Event event;

    AssetsTrackingCheckoutRule(Account account, Event event, MandatoryTaskDetail mandatoryTaskDetail) {
        this.mandatoryTaskDetail = mandatoryTaskDetail;
        this.account = account;
        this.event = event;
    }

    @Override
    public boolean isFulfilled() {

        String controlStartString = event.getControlStartDateTime();
        long controlStartLong = parseDate(controlStartString);

        List<Account_Asset__c> allAssets = Account_Asset__c.getCustomerAssets(account.getId(), AssetsListAdapter.SERIALIZED_RECORD_NAME);
        List<Account_Asset__c> assetsNonSer = Account_Asset__c.getCustomerAssets(account.getId(), AssetsListAdapter.NO_SERIALIZED_RECORD_NAME);

        allAssets.addAll(assetsNonSer);

        String query = "";
        int result = 0;
        boolean fulfilled = allAssets.size() == 0;

        for (Account_Asset__c asset : allAssets) {
            query = String.format("SELECT count() FROM {%1$s} WHERE " +
                            "{%1$s:%2$s} = '%3$s' AND " +
                            "({%1$s:%4$s} >= '%5$s' OR ({%1$s:%4$s} IS NULL AND {%1$s:%6$s} >= %7$d))",
                    AbInBevObjects.ACCOUNT_ASSET_TRACKING_C,
                    AbInBevConstants.AccountAssetTrackingFields.PARENT_ID, asset.getId(),
                    SyncEngineConstants.StdFields.LAST_MODIFIED_DATE, controlStartString,
                    SOUP_LAST_MODIFIED_DATE, controlStartLong);
            JSONArray array = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(query);
            try {
                result = array.getJSONArray(0).getInt(0);
                if (result == 0) {
                    fulfilled = false;
                    break;
                } else
                    fulfilled = true;
            } catch (JSONException e) {
                Log.w(TAG, e);
                fulfilled = true;
            }
        }
        return fulfilled;
    }

    @Override
    public void openScreen(Context context) {
        Intent intent = new Intent(context, AssetsListActivity.class);
        intent.putExtra(AssetsListActivity.ACCOUNT_ID_EXTRA, account.getId());
        context.startActivity(intent);
    }
}
