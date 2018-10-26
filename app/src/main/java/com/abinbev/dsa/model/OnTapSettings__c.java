package com.abinbev.dsa.model;

import android.support.annotation.NonNull;

import com.abinbev.dsa.activity.ContractsActivity;
import com.abinbev.dsa.utils.AbInBevConstants.OnTapSettingsFields;
import com.abinbev.dsa.utils.DateUtils;
import com.salesforce.androidsdk.accounts.UserAccountManager;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

import java.util.Date;

import static com.abinbev.dsa.utils.datamanager.DataManagerUtils.getByField;

public class OnTapSettings__c extends SFBaseObject {

    private static final int DEFAULT_EXPIRATION_PERIOD = 10;
    private static final int DEFAULT_ORDER_TIMEFRAME_VALUE = 3;
    private static final int DEFAULT_CHECK_IN_DISTANCE_TRESHOLD = 150;

    public OnTapSettings__c() {
        super("OnTapSettings__c");
    }

    public OnTapSettings__c(JSONObject jsonObject) {
        super("OnTapSettings__c", jsonObject);
    }

    @NonNull
    public static OnTapSettings__c getCurrentSettings() {
        DataManager dm = DataManagerFactory.getDataManager();
        User currentUser = User.getCurrentUser();

        // First search by User Id.
        OnTapSettings__c result = getByField(dm, OnTapSettingsFields.SETUP_OWNER_ID, currentUser.getId(), OnTapSettings__c.class);

        // If not present then by ProfileId.
        if (result == null) {
            result = getByField(dm, OnTapSettingsFields.SETUP_OWNER_ID, currentUser.getProfileId(), OnTapSettings__c.class);
        }

        // If not present then by OrgId.
        if (result == null) {
            String orgId = UserAccountManager.getInstance().getStoredOrgId();
            result = getByField(dm, OnTapSettingsFields.SETUP_OWNER_ID, orgId, OnTapSettings__c.class);
        }

        // Return empty object if there are no settings.
        if (result == null) {
            result = new OnTapSettings__c(new JSONObject());
        }

        return result;
    }

    /**
     * Number of days before expiration date when alert should be shown.
     * @return
     */
    public int getContractExpirationPeriod() {
        return isNullValue(OnTapSettingsFields.CONTRACT_EXPIRATION_PERIOD) ?
                DEFAULT_EXPIRATION_PERIOD : getIntValueForKey(OnTapSettingsFields.CONTRACT_EXPIRATION_PERIOD);
    }

    public int getOrderTimeFrameValue() {
        return isNullValue(OnTapSettingsFields.ORDER_TIMEFRAME_VALUE) ?
                DEFAULT_ORDER_TIMEFRAME_VALUE : getIntValueForKey(OnTapSettingsFields.ORDER_TIMEFRAME_VALUE);
    }

    public boolean getOrderTimeFrameDays() {
        return getBooleanValueForKey(OnTapSettingsFields.ORDER_TIMEFRAME_DAYS);
    }

    public boolean getOrderTimeFrameMonths() {
        return getBooleanValueForKey(OnTapSettingsFields.ORDER_TIMEFRAME_MONTHS);
    }

    public int getCheckInDistanceTreshold() {
        return isNullOrEmpty(OnTapSettingsFields.CHECK_IN_DISTANCE_TRESHOLD) ?
                DEFAULT_CHECK_IN_DISTANCE_TRESHOLD :
                getIntValueForKey(OnTapSettingsFields.CHECK_IN_DISTANCE_TRESHOLD);
    }

    public boolean isContractExpiring(CN_PBO_Contract__c contract) {
        boolean isExpiring = false;

        Date endDate = contract.getEndDate();
        if (endDate != null) {
            Date currentDate = new Date();
            int daysToEnd = DateUtils.daysBetween(currentDate, endDate);
            int dateDue = getContractExpirationPeriod();
            isExpiring = daysToEnd >= 0 && daysToEnd <= dateDue;
            if (isExpiring)
                contract.setDateDue(ContractsActivity.DATE_DUE, ""+dateDue);
        }

        return isExpiring;
    }
}
