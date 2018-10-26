package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.MorningMeetingFields;
import com.abinbev.dsa.utils.PermissionManager;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.abinbev.dsa.utils.datamanager.FormatValues;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants.StdFields;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants.Formats;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.abinbev.dsa.utils.AbInBevConstants.MorningMeetingFields.OFFICE;
import static com.abinbev.dsa.utils.AbInBevConstants.MorningMeetingFields.WEEKDAYS;

/**
 * Created by Jakub Stefanowski on 03.06.2017.
 */

public class Morning_Meeting__c extends SFBaseObject {

    private static final String TAG = Morning_Meeting__c.class.getSimpleName();

    public static final FormatValues OBJECT_FORMAT_VALUES = new FormatValues.Builder()
            .putTable("MorningMeeting", AbInBevObjects.MORNING_MEETING)
                .putColumn("Id", MorningMeetingFields.ID)
            .build();

    public Morning_Meeting__c(JSONObject json) {
        super(AbInBevObjects.MORNING_MEETING, json);
    }

    public Morning_Meeting__c() {
        super(AbInBevObjects.MORNING_MEETING);
    }

    public String getOfficeId() {
        return getStringValueForKey(OFFICE);
    }

    public static Morning_Meeting__c getCurrentMeeting() {
        DataManager dataManager = DataManagerFactory.getDataManager();
        String dayOfWeek = getDayOfWeek();

        String sqlFilter = String.format("{%1$s:%2$s} LIKE '%%%3$s%%' ORDER BY {%1$s:%4$s} DESC LIMIT 1",
                AbInBevObjects.MORNING_MEETING, WEEKDAYS, dayOfWeek, StdFields.CREATED_DATE);
        String sql = String.format(Formats.SMART_SQL_FORMAT, AbInBevObjects.MORNING_MEETING, sqlFilter);


        Event lastMorningMeetingEvent = Event.getLastMorningMeetingEvent();
        if (lastMorningMeetingEvent != null && lastMorningMeetingEvent.isCheckOut()) {
            PermissionManager permissionManager = PermissionManager.getInstance();
            double minHoursBetweenMeetings = permissionManager.getMinimalMorningMeetingInterval();

            // There is a minimal gap between morning meetings.
            if (getHoursSince(lastMorningMeetingEvent.getStartDateTime()) >= minHoursBetweenMeetings) {

                // Load morning meeting.
                return DataManagerUtils.fetchObject(dataManager, sql, Morning_Meeting__c.class);
            }
        }
        else {

            // Load morning meeting.
            return DataManagerUtils.fetchObject(dataManager, sql, Morning_Meeting__c.class);
        }

        return null;
    }

    private static double getHoursSince(Date dateTime) {
        if (dateTime == null) return Double.MAX_VALUE;
        long dateMillis = dateTime.getTime();
        long currentDateMillis = System.currentTimeMillis();
        return (currentDateMillis - dateMillis)/ (1000.0 * 60.0 * 60.0);
    }

    public static List<Morning_Meeting__c> getAll() {
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchAllObjects(dm, AbInBevObjects.MORNING_MEETING, Morning_Meeting__c.class);
    }

    public static Morning_Meeting__c getById(String id) {
        DataManager dataManager = DataManagerFactory.getDataManager();
        return DataManagerUtils.getById(dataManager, AbInBevObjects.MORNING_MEETING, id, Morning_Meeting__c.class);
    }

    public static boolean isMorningMeetingFinished() {
        Event event = Event.getEventForMorningMeeting();

        if (event == null) {
            // If event does not exist check if there is a mandatory morning meeting for today.
            Morning_Meeting__c morningMeeting = Morning_Meeting__c.getCurrentMeeting();
            if (morningMeeting != null) {
                Meeting_Attendee__c attendee = Meeting_Attendee__c.getByMorningMeetingId(morningMeeting.getId());
                return attendee == null || !attendee.isMandatory();
            }
            else {
                // No morning meeting for today.
                return true;
            }
        }
        // If event exists check if it is checked out.
        else {
            return event.isCheckOut();
        }
    }

    private static String getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);
    }
}
