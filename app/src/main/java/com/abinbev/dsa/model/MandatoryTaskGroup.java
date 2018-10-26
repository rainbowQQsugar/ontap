package com.abinbev.dsa.model;

import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.MandatoryTaskGroupFields;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONArray;
import org.json.JSONObject;

import rx.Observable;

import static com.salesforce.dsa.utils.DSAConstants.Formats.SMART_SQL_FORMAT;

public class MandatoryTaskGroup extends SFBaseObject {

    public static final String TAG = MandatoryTaskGroup.class.getSimpleName();

    protected MandatoryTaskGroup() {
        super(AbInBevObjects.MANDATORY_TASK_GROUP);
    }

    public MandatoryTaskGroup(JSONObject json) {
        super(AbInBevObjects.MANDATORY_TASK_GROUP, json);
    }

    public boolean getFirstMonthlyVisitOnlyFlag() {
        return getBooleanValueForKey(MandatoryTaskGroupFields.FIRST_MONTHLY_VISIT_ONLY);
    }

    public String getMandatoryTaskGroupName() {
        return getStringValueForKey(MandatoryTaskGroupFields.MANDATORY_TASK_GROUP_NAME);
    }

    public String getPocCategory() {
        return getStringValueForKey(MandatoryTaskGroupFields.POC_CATEGORY);
    }

    public String getPocChannel() {
        return getStringValueForKey(MandatoryTaskGroupFields.POC_CHANNEL);
    }

    public String getPocCity() {
        return getStringValueForKey(MandatoryTaskGroupFields.POC_CITY);
    }

    public String getUserProfile() {
        return getStringValueForKey(MandatoryTaskGroupFields.USER_PROFILE);
    }

    public static Observable<MandatoryTaskGroup> getBy(Account account, User user, boolean firstMonthVisit) {
        if (account == null || user == null) {
            return Observable.just(null);
        }
        else {
            return getBy(account.getCategory(), account.getChannel(), account.getCityRegion(), user.getProfile(), firstMonthVisit);
        }
    }

    public static Observable<MandatoryTaskGroup> getBy(String category, String channel, String city, String userProfile, boolean firstMonthVisit) {
        return Observable.fromCallable(() -> {
            String filterFormat = "({%1$s:%2$s} IS NULL OR {%1$s:%2$s} = '%3$s')";
            String filter = String.format(filterFormat,
                    AbInBevObjects.MANDATORY_TASK_GROUP, MandatoryTaskGroupFields.POC_CATEGORY, category);
            if (!firstMonthVisit) {
                filter += " AND ";
                filter += String.format("{%1$s:%2$s} = '%3$s'",
                        AbInBevObjects.MANDATORY_TASK_GROUP, MandatoryTaskGroupFields.FIRST_MONTHLY_VISIT_ONLY, "false");
            }

            filter += " AND ";
            filter += String.format(filterFormat,
                    AbInBevObjects.MANDATORY_TASK_GROUP, MandatoryTaskGroupFields.POC_CHANNEL, channel);
            filter += " AND ";
            filter += String.format(filterFormat,
                    AbInBevObjects.MANDATORY_TASK_GROUP, MandatoryTaskGroupFields.POC_CITY, city);
            filter += " AND ";
            filter += String.format(filterFormat,
                    AbInBevObjects.MANDATORY_TASK_GROUP, MandatoryTaskGroupFields.USER_PROFILE, userProfile);

            if (firstMonthVisit) {
                // We want to have first records with true, then records with false.
                filter += String.format(" ORDER BY {%1$s:%2$s} DESC", AbInBevObjects.MANDATORY_TASK_GROUP, MandatoryTaskGroupFields.FIRST_MONTHLY_VISIT_ONLY);
            }

            filter += " LIMIT 1";

            String smartSql = String.format(SMART_SQL_FORMAT, AbInBevObjects.MANDATORY_TASK_GROUP, filter);

            DataManager dm = DataManagerFactory.getDataManager();
            return DataManagerUtils.fetchObject(dm, smartSql, MandatoryTaskGroup.class);
        });
    }
}
