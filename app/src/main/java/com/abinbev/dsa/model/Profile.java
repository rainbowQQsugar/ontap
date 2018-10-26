package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.ProfileFields;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

public class Profile extends SFBaseObject {

    public static final String TAG = Profile.class.getName();

    public Profile() {
        super(AbInBevObjects.PROFILE);
    }

    public Profile(JSONObject jsonObject) {
        super(AbInBevObjects.PROFILE, jsonObject);
    }

    public String getName() {
        return getStringValueForKey(ProfileFields.NAME);
    }

    public static Profile getProfileById(String profileId) {
        JSONObject jsonObject = DataManagerFactory
                .getDataManager().exactQuery(AbInBevObjects.PROFILE, AbInBevConstants.ID, profileId);
        return jsonObject == null ? null : new Profile(jsonObject);
    }
}
