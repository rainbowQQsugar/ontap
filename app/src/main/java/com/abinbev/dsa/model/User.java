package com.abinbev.dsa.model;

import android.content.Context;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.UserFields;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.abinbev.dsa.utils.datamanager.FormatValues;
import com.salesforce.androidsdk.accounts.UserAccountManager;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants.DSAObjects;

import org.json.JSONObject;

import java.util.List;

import static com.salesforce.dsa.utils.DSAConstants.UserFields.NAME;

/**
 * Created by usanaga on 10/4/15.
 */
public class User extends SFBaseObject {
    public static final String TAG = User.class.getName();

    public static final FormatValues OBJECT_FORMAT_VALUES = new FormatValues.Builder()
            .putTable("User", AbInBevObjects.USER)
            .putColumn("Name", UserFields.NAME)
            .build();

//    private static final String SUPERVISOR = "supervisor";

    public User() {
        super(DSAObjects.USER);
    }

    public User(JSONObject jsonObject) {
        super(DSAObjects.USER, jsonObject);
    }

    public String getName() {
        return getStringValueForKey(NAME);
    }

    public String getCountry() {
        return getStringValueForKey(UserFields.COUNTRY);
    }

    public String getCity() {
        return getStringValueForKey(UserFields.CITY);
    }

    public String getProfile() {
        return getStringValueForKey(UserFields.PROFILE);
    }

    public String getProfileId() {
        return getStringValueForKey(UserFields.PROFILE_ID);
    }

    public String getZona() {
        return getStringValueForKey(UserFields.ZONA__C);
    }

    public String getTimeZone() {
        return getStringValueForKey(UserFields.TIME_ZONE);
    }

    public String getEmail() {
        return getStringValueForKey(UserFields.EMAIL);
    }

    public String getManagerId() {
        return getStringValueForKey(UserFields.ManagerId);
    }

    public String getBusinessUnit() {
        return getStringValueForKey(UserFields.BUSINESS_UNIT);
    }

    public String getRelatedKUser() {
        return getStringValueForKey(UserFields.CN_Related_KUser__c);
    }

    public boolean hasManager() {
        return !isNullValue(UserFields.MANAGER);
    }

    public static User getUserByUserId(String userId) {
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.getById(dm, userId, User.class);
    }

    public static User getCurrentUser() {
        String userId = UserAccountManager.getInstance().getStoredUserId();
        return User.getUserByUserId(userId);
    }

    public String getEmployeeCode() {
        return getStringValueForKey(UserFields.CN_Employee_Code__c);
    }

    public static List<User> searchUsersbyName(String search) {
        DataManager dm = DataManagerFactory.getDataManager();

        FormatValues fv = new FormatValues()
                .addAll(OBJECT_FORMAT_VALUES)
                .putValue("name", search);

        String sql = "SELECT {User:_soup} FROM {User} " +
                "WHERE {User:Name} LIKE '%{name}%'";

        return DataManagerUtils.fetchObjects(dm, sql, fv, User.class);
    }

    public static boolean isSupervisor(Context context) {
        // Quiz assignment feature is not enabled.
        return false;
//        String userProfile = AppPreferenceUtils.getUserProfile(context);
//
//        if (userProfile.toLowerCase().contains(SUPERVISOR)) {
//            return true;
//        }
//        return false;
    }

}
