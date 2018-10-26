package com.abinbev.dsa.model;

import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.abinbev.dsa.utils.AbInBevConstants.MeetingAttendeesFields.ATTENDEE_NAME;
import static com.abinbev.dsa.utils.AbInBevConstants.MeetingAttendeesFields.MANDATORY;
import static com.abinbev.dsa.utils.AbInBevConstants.MeetingAttendeesFields.MORNING_MEETING;

/**
 * Created by Jakub Stefanowski on 03.06.2017.
 */

public class Meeting_Attendee__c extends SFBaseObject {

    private static final String TAG = Meeting_Attendee__c.class.getSimpleName();

    public Meeting_Attendee__c(JSONObject json) {
        super(AbInBevObjects.MEETING_ATTENDEE, json);
    }

    public Meeting_Attendee__c() {
        super(AbInBevObjects.MEETING_ATTENDEE);
    }

    public String getAttendeeId() {
        return getStringValueForKey(ATTENDEE_NAME);
    }

    public String getMorningMeetingId() {
        return getStringValueForKey(MORNING_MEETING);
    }

    public boolean isMandatory() {
        return getBooleanValueForKey(MANDATORY);
    }

    public static Meeting_Attendee__c getByMorningMeetingId(String morningMeetingId) {
        DataManager dataManager = DataManagerFactory.getDataManager();
        String sqlFilter = String.format("{%1$s:%2$s} = '%3$s' LIMIT 1",
                AbInBevObjects.MEETING_ATTENDEE, MORNING_MEETING, morningMeetingId);

        Meeting_Attendee__c result = null;
        JSONArray recordsArray = dataManager.fetchAllSmartSQLQuery(
                String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.MEETING_ATTENDEE, sqlFilter));

        if (recordsArray != null && recordsArray.length() > 0) {
            try {
                JSONObject jsonObject = recordsArray.getJSONArray(0).getJSONObject(0);
                result = new Meeting_Attendee__c(jsonObject);
            }
            catch (Exception e) {
                Log.w(TAG, "Couldn't load data for the morning meeting attendee.", e);
            }
        }

        return result;
    }
}
