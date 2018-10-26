package com.abinbev.dsa.model;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.syncmanifest.JexlManifestProcessor;
import com.abinbev.dsa.syncmanifest.LocalQueryHelper;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.AccountStatus;
import com.abinbev.dsa.utils.AbInBevConstants.EventFields;
import com.abinbev.dsa.utils.AbInBevConstants.EventSubject;
import com.abinbev.dsa.utils.AbInBevConstants.VisitTypes;
import com.abinbev.dsa.utils.ContentUtils;
import com.abinbev.dsa.utils.DataUtils;
import com.abinbev.dsa.utils.DateUtils;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.abinbev.dsa.utils.datamanager.FormatValues;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.androidsdk.accounts.UserAccountManager;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.syncmanifest.ManifestProcessorFactory;
import com.salesforce.androidsyncengine.syncmanifest.processors.ManifestProcessingException;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants.StdFields;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants.DSAObjects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


/**
 * @author Jason Harris (jason@akta.com)
 */
public class Event extends SFBaseObject {

    private static final String TAG = Event.class.getSimpleName();

    private static final String SOUP_COLUMN = String.format("{%s:_soup}", DSAObjects.EVENT);

    public static final FormatValues OBJECT_FORMAT_VALUES = new FormatValues.Builder()
            .putTable("Event", AbInBevObjects.EVENT)
            .putColumn("WhatId", EventFields.WHAT_ID)
            .putColumn("StartDateTime", EventFields.START_DATE_TIME)
            .putColumn("EndDateTime", EventFields.END_DATE_TIME)
            .putColumn("VisitType", EventFields.VISIT_TYPE)
            .putColumn("VisitState", EventFields.ESTADO_DE_VISITA__C)
            .putColumn("ControlStart", EventFields.CONTROL_START_DATE_TIME)
            .build();

    private Account account;

    public Event() {
        super(AbInBevObjects.EVENT);
    }

    public Event(JSONObject json) {
        super(AbInBevObjects.EVENT, json);
    }

    public void setId(String id) {
        setStringValueForKey(StdFields.ID, id);
    }

    public void setControlStartDateTime(String controlStartDateTime) {
        setStringValueForKey(EventFields.CONTROL_START_DATE_TIME, controlStartDateTime);
    }

    public void setControlEndDateTime(String controlStartDateTime) {
        setStringValueForKey(EventFields.CONTROL_END_DATE_TIME, controlStartDateTime);
    }

    public void setCheckOutLongitude(String longitude) {
        setStringValueForKey(EventFields.CHECKOUT_LONGITUDE, longitude);
    }

    public void setCheckOutLatitude(String latitude) {
        setStringValueForKey(EventFields.CHECKOUT_LATITUDE, latitude);
    }

    public void setCheckOutLocation(LatLng latLng) {
        if (latLng == null) {
            setCheckOutLatitude(null);
            setCheckOutLongitude(null);
        } else {
            setCheckOutLatitude(Double.toString(latLng.latitude));
            setCheckOutLongitude(Double.toString(latLng.longitude));
        }
    }

    public void setLongitude(String longitude) {
        setStringValueForKey(EventFields.LONGITUDE, longitude);
    }

    public void setLatitude(String latitude) {
        setStringValueForKey(EventFields.LATITUDE, latitude);
    }

    public void setLocation(LatLng latLng) {
        if (latLng == null) {
            setLatitude(null);
            setLongitude(null);
        } else {
            setLatitude(Double.toString(latLng.latitude));
            setLongitude(Double.toString(latLng.longitude));
        }
    }

    public void setVisitCheckInDistance(double distance) {
        setDoubleValueForKey(EventFields.VISIT_CHECK_IN_DISTANCE, distance);
    }

    public double getVisitCheckInDistance() {
        return getDoubleValueForKey(EventFields.VISIT_CHECK_IN_DISTANCE);
    }

    public void setVisitCheckOutDistance(double distance) {
        setDoubleValueForKey(EventFields.VISIT_CHECK_OUT_DISTANCE, distance);
    }

    public double getVisitCheckOutDistance() {
        return getDoubleValueForKey(EventFields.VISIT_CHECK_OUT_DISTANCE);
    }

    public void setMorningMeetingCheckInDistance(double distance) {
        setDoubleValueForKey(EventFields.MORNING_MEETING_CHECK_IN_DISTANCE, distance);
    }

    public void setMorningMeetingCheckOutDistance(double distance) {
        setDoubleValueForKey(EventFields.MORNING_MEETING_CHECK_OUT_DISTANCE, distance);
    }

    public void setVisitLocationCompliance(boolean value) {
        setBooleanValueForKey(EventFields.VISIT_LOCATION_COMPLIANCE, value);
    }

    public boolean getVisitLocationCompliance() {
        return getBooleanValueForKey(EventFields.VISIT_LOCATION_COMPLIANCE);
    }

    public JSONObject setCheckOutWithParams(String date, LatLng latlng) {
        return setCheckOutWithParams(new JSONObject(), date, latlng);
    }

    public JSONObject setCheckOutWithParams(String date) {
        return setCheckOutWithParams(date, null);
    }

    public JSONObject setCheckOutWithParams(JSONObject eventJSONChangedObject, String date, LatLng latlng) {
        String controlStartDateString = getControlStartDateTime();
        Date controlStartDate = DateUtils.dateFromDateTimeString(controlStartDateString);
        Date controlEndDate = DateUtils.dateFromDateTimeString(date);
        long eventDurationInMinutes = DateUtils.differenceDates(controlStartDate, controlEndDate, DateUtils.TimeUnits.MINUTES);

        String latitude = null;
        String longitude = null;
        if (latlng != null) {
            latitude = Double.toString(latlng.latitude);
            longitude = Double.toString(latlng.longitude);
        }

        setCheckOutLatitude(latitude);
        setCheckOutLongitude(longitude);
        setControlEndDateTime(date);
        setMeetingDuration(eventDurationInMinutes);
        setVisitState(VisitState.completed);

        try {
            eventJSONChangedObject.put(EventFields.CHECKOUT_LATITUDE, latitude);
            eventJSONChangedObject.put(EventFields.CHECKOUT_LONGITUDE, longitude);
            eventJSONChangedObject.put(EventFields.CONTROL_END_DATE_TIME, date);
            eventJSONChangedObject.put(EventFields.DURATION, eventDurationInMinutes);
            eventJSONChangedObject.put(EventFields.ESTADO_DE_VISITA__C, VisitState.completed.getState());
        } catch (Exception e) {
            Log.e(TAG, "Exception trying to set check out parameters", e);
        }

        return eventJSONChangedObject;
    }

    public void checkIn(Date date, LatLng latlng) {
        String latitude = null;
        String longitude = null;
        if (latlng != null) {
            latitude = Double.toString(latlng.latitude);
            longitude = Double.toString(latlng.longitude);
        }
        setLatitude(latitude);
        setLongitude(longitude);
        setMeetingDuration(0);
        setVisitState(VisitState.open);

        String formattedDate = DateUtils.SERVER_DATE_TIME_FORMAT.format(date);
        setControlStartDateTime(formattedDate);
    }

    public void setCheckOutComment(String comment) {
        setStringValueForKey(EventFields.CHECKOUT_NOTE, comment);
    }

    public String getCheckOutComment() {
        return getStringValueForKey(EventFields.CHECKOUT_NOTE);
    }

    public void setCheckInDescription(String description) {
        setStringValueForKey(EventFields.CHECKIN_EXCEPTION_NOTE, description);
    }

    public String getVisitType() {
        return getStringValueForKey(EventFields.VISIT_TYPE);
    }

    public void setVisitType(String visitType) {
        setStringValueForKey(EventFields.VISIT_TYPE, visitType);
    }

    public void setCheckOutDescription(String description) {
        setStringValueForKey(EventFields.CHECKOUT_EXCEPTION_NOTE, description);
    }

    public Boolean getIsEventWithMusicBand() {
        return getBooleanValueForKey(AbInBevConstants.EventFields.WITH_MUSIC_BAND);
    }

    public String getEndDate() {
        return getStringValueForKey(EventFields.END_DATE_TIME);
    }

    public String getStartDate() {
        return getStringValueForKey(EventFields.START_DATE_TIME);
    }

    public String getAccountId() {
        return getStringValueForKey(EventFields.ACCOUNT_ID);
    }

    public String getSubject() {
        return getStringValueForKey(EventFields.SUBJECT);
    }

    public String getEventCatalog() {
        return getStringValueForKey(EventFields.CATALOG);
    }

    public String getControlStartDateTime() {
        return getStringValueForKey(EventFields.CONTROL_START_DATE_TIME);
    }

    public String getControlEndDateTime() {
        return getStringValueForKey(EventFields.CONTROL_END_DATE_TIME);
    }

    public String getLatitude() {
        return getStringValueForKey(EventFields.LATITUDE);
    }

    public String getLongitude() {
        return getStringValueForKey(EventFields.LONGITUDE);
    }

    public String getEquipment() {
        return getStringValueForKey(EventFields.EQUIPMENT);
    }

    public boolean isCheckOut() {
        return ContentUtils.isStringValid(getControlStartDateTime()) && ContentUtils.isStringValid(getControlEndDateTime()) && getVisitStateName().equalsIgnoreCase(VisitState.completed.getState());
    }

    public boolean isCheckedIn() {
        return ContentUtils.isStringValid(getControlStartDateTime()) && !ContentUtils.isStringValid(getControlEndDateTime()) && !getVisitStateName().equalsIgnoreCase(VisitState.completed.getState());
    }

    public String getVisitStateName() {
        return getStringValueForKey(EventFields.ESTADO_DE_VISITA__C);
    }

    public LatLng getLocation() {
        if (isNullValue(EventFields.LATITUDE) || isNullValue(EventFields.LONGITUDE)) {
            return null;
        } else {
            double lat = getDoubleValueForKey(EventFields.LATITUDE);
            double lon = getDoubleValueForKey(EventFields.LONGITUDE);

            return new LatLng(lat, lon);
        }
    }

    public VisitState getVisitState() {
        return VisitState.fromKey(getStringValueForKey(EventFields.ESTADO_DE_VISITA__C));
    }

    public String getNotTranslatedVisitState() {
        return getStringValueForKey(EventFields.ESTADO_DE_VISITA__C);
    }


    public void setMeetingDuration(long duration) {
        setLongValueForKey(EventFields.DURATION, duration);
    }

    public void setVisitState(VisitState state) {
        setStringValueForKey(EventFields.ESTADO_DE_VISITA__C, state.getState());
    }

    public Date getStartDateTime() {
        return getDateValueForKey(EventFields.START_DATE_TIME);
    }

    public Date getEndDateTime() {
        return getDateValueForKey(EventFields.END_DATE_TIME);
    }

    public boolean taskScheduled() {
        return getBooleanValueForKey(EventFields.TAREA_PROGRAMADA__C);
    }

    public double getEstimatedVisitTime() {
        return getDoubleValueForKey(EventFields.DURATION_IN_MINUTES);
    }

    public boolean visitClosed() {
        return getBooleanValueForKey(EventFields.VISITA_CERRADA__C);
    }

    public String getWhatId() {
        return getStringValueForKey(EventFields.WHAT_ID);
    }

    public void setWhatId(String whatId) {
        setStringValueForKey(EventFields.WHAT_ID, whatId);
    }

    public void setOwnerId(String ownerId) {
        setStringValueForKey(EventFields.OWNER_ID, ownerId);
    }

    public void setActivityDate(String activityDate) {
        setStringValueForKey(EventFields.ACTIVITY_DATE, activityDate);
    }

    public void setStartDateTime(String startDateTime) {
        setStringValueForKey(EventFields.START_DATE_TIME, startDateTime);
    }

    public void setEndDateTime(String endDateTime) {
        setStringValueForKey(EventFields.END_DATE_TIME, endDateTime);
    }

    public void setSubject(String subject) {
        setStringValueForKey(EventFields.SUBJECT, subject);
    }

    public void setProgramada(boolean programada) {
        setBooleanValueForKey(EventFields.PROGRAMADA__C, programada);
    }

    public Account getAccount() {
        if (account == null) {
            account = new Account(new JSONObject());
        }
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public boolean isTempObject() {
        return (this.getId().startsWith("EVE"));
    }

    private void doCheckIn(String whatId, LatLng latLng) {
        setWhatId(whatId);

        String userId = UserAccountManager.getInstance().getStoredUserId();
        setOwnerId(userId);

        Calendar cal = Calendar.getInstance();
        String date = DateUtils.dateToDateString(cal.getTime());
        setActivityDate(date);

        String startDateTime = DateUtils.SERVER_DATE_TIME_FORMAT.format(cal.getTime());
        setStartDateTime(startDateTime);
        setControlStartDateTime(startDateTime);

        cal.add(Calendar.MINUTE, 30);
        String endDateTime = DateUtils.SERVER_DATE_TIME_FORMAT.format(cal.getTime());
        setEndDateTime(endDateTime);

        setSubject(EventSubject.VISIT);
        setProgramada(false);
        setVisitState(VisitState.open);
        setMeetingDuration(0);
        setLocation(latLng);
    }

    private void doCheckOut(LatLng latLng) {
        Calendar cal = Calendar.getInstance();
        Date currentTime = cal.getTime();
        String dateTime = DateUtils.SERVER_DATE_TIME_FORMAT.format(currentTime);

        String controlStartDateString = getControlStartDateTime();
        Date controlStartDate = DateUtils.dateFromDateTimeString(controlStartDateString);
        long eventDurationInMinutes = DateUtils.differenceDates(controlStartDate, currentTime, DateUtils.TimeUnits.MINUTES);

        setCheckOutLocation(latLng);
        setControlEndDateTime(dateTime);
        setMeetingDuration(eventDurationInMinutes);
        setVisitState(VisitState.completed);
    }

    public void doMorningMeetingCheckIn(String morningMeetingId, LatLng latLng, String description) {
        doCheckIn(morningMeetingId, latLng);
        setVisitType(VisitTypes.MORNING_MEETING);
        setCheckInDescription(description);
    }

    public void doMorningMeetingCheckOut(LatLng latLng, String description) {
        doCheckOut(latLng);
        setCheckOutDescription(description);
    }

    public void doVisitCheckOut(LatLng latlng) {
        doCheckOut(latlng);
    }

    public static boolean isFirstVisitInMonth(String accountId) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        try {
            String smartSqlFilter = joinPredicates(
                    dateSincePredicate(calendar.getTime()),
                    accountPredicate(accountId),
                    statePredicate(VisitState.completed));

            String smartSql = queryInAllVisits(SOUP_COLUMN, smartSqlFilter);
            return getEventsForSql(smartSql).isEmpty();
        } catch (ManifestProcessingException e) {
            Log.w(TAG, "Couldn't load is first in month.", e);
            return true;
        }
    }

    public static List<Event> getVisits() {
        try {
            String smartSql = queryInAllVisits(SOUP_COLUMN, null);
            return getEventsForSql(smartSql, true);
        } catch (ManifestProcessingException e) {
            Log.w(TAG, e);
            return Collections.emptyList();
        }
    }

    public static Event getFinishedVisitToday(String accountId){
        Event actualEvent = null;

        try {
            String smartSqlFilter = joinPredicates(
                    accountPredicate(accountId),
                    statePredicate(VisitState.completed));

            String endDateTime = DateUtils.getMondayDate();
            String startDateTime = DateUtils.getNextMondayDate();

            String finalSmartSqlFilter = String.format("AND ({%s:%s} < '%s' AND {%s:%s} >= '%s')",
                    AbInBevObjects.EVENT, EventFields.START_DATE_TIME, startDateTime,
                    AbInBevObjects.EVENT, EventFields.END_DATE_TIME, endDateTime);

            String smartSql = queryInCurrentVisits(SOUP_COLUMN, smartSqlFilter + finalSmartSqlFilter);

            List<Event> events = getEventsForSql(smartSql);
            Date currentDate = DateUtils.dateFromString(DateUtils.currentDateString());

            for (Event event : events) {
                Date checkInDate = DateUtils.dateFromString(DateUtils.fromServerDateTimeToDate(event.getControlStartDateTime()));

                if (checkInDate.getTime() == currentDate.getTime() && event.getVisitType().equalsIgnoreCase(VisitTypes.IN_PLAN)) {
                    actualEvent =  event;
                    break;
                }

            }

        } catch (ManifestProcessingException e) {
            Log.w(TAG, "Couldn't load event for params.", e);
        }

        Log.d(TAG, "For accountId: " +  accountId + " Event has been finished today -> "+ actualEvent);
        return actualEvent;
    }

    public static Event getCurrentVisitFor(String accountId) {
        Event actualEvent = null;

        try {
            String smartSqlFilter = joinPredicates(
                    accountPredicate(accountId),
                    statePredicate(VisitState.open));

            String endDateTime = DateUtils.getMondayDate();
            String startDateTime = DateUtils.getNextMondayDate();

            String finalSmartSqlFilter = String.format("AND ({%s:%s} < '%s' AND {%s:%s} >= '%s')",
                    AbInBevObjects.EVENT, EventFields.START_DATE_TIME, startDateTime,
                    AbInBevObjects.EVENT, EventFields.END_DATE_TIME, endDateTime);

            String smartSql = queryInCurrentVisits(SOUP_COLUMN, smartSqlFilter + finalSmartSqlFilter);

            List<Event> events = getEventsForSql(smartSql);
            List<Event> filterEvents = new ArrayList<>();
            for (Event event : events) {
                String startDateString = event.getStartDate();
                String endDateString = event.getEndDate();
                Date currentDate = DateUtils.dateFromString(DateUtils.currentDateString());
                Date startDate = DateUtils.dateFromString(DateUtils.fromServerDateTimeToDate(startDateString));
                Date endDate = DateUtils.dateFromString(DateUtils.fromServerDateTimeToDate(endDateString));

                if (event.isCheckedIn()) {
                    filterEvents.add(event);
                    continue;
                }

                if (startDate.getTime() == endDate.getTime() && startDate.getTime() == currentDate.getTime()) {
                    filterEvents.add(event);
                } else if (startDate.getTime() != endDate.getTime()) {
                    if (((currentDate.getTime() >= startDate.getTime()) && (currentDate.getTime() <= endDate.getTime()))) {
                        filterEvents.add(event);
                    }
                }

            }

            if (filterEvents != null && filterEvents.size() > 0) {
//                 If there are multiple events try to pick first that is checked in.
                if (filterEvents.size() > 1) {
                    for (int i = 0; i < filterEvents.size() && actualEvent == null; i++) {
                        if (filterEvents.get(i).isCheckedIn()) {
                            actualEvent = filterEvents.get(i);
                        }
                    }
                }

                // If event is still empty pick first from the list.
                if (actualEvent == null) {
                    actualEvent = filterEvents.get(0);
                }
            }
        } catch (ManifestProcessingException e) {
            Log.w(TAG, "Couldn't load event for params.", e);
        }
        return actualEvent;
    }

    public static List<Event> getCheckedInVisitsFor(String accountId) {
        try {
            String smartSqlFilter = joinPredicates(
                    accountPredicate(accountId),
                    statePredicate(VisitState.open),
                    controlStartExistsPredicate());

            String smartSql = queryInCurrentVisits(SOUP_COLUMN, smartSqlFilter);

            return getEventsForSql(smartSql);
        } catch (ManifestProcessingException e) {
            Log.w(TAG, "Couldn't load data for checked in events except " + accountId, e);
            return Collections.emptyList();
        }
    }

    public static List<Event> getAllCheckedInVisits() {
        try {
            String smartSqlFilter = joinPredicates(
                    statePredicate(VisitState.open),
                    controlStartExistsPredicate());

            String smartSql = queryInCurrentVisits(SOUP_COLUMN, smartSqlFilter);

            return getEventsForSql(smartSql);
        } catch (ManifestProcessingException e) {
            Log.w(TAG, "Couldn't load data for checked in events.", e);
            return Collections.emptyList();
        }
    }


    private static List<Event> getEventsForSql(String smartSql) {
        return getEventsForSql(smartSql, true /* ascending */);
    }

    private static List<Event> getEventsForSql(String smartSql, boolean ascending) {
        List<Event> events = new LinkedList<>();
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONArray jsonArray = recordsArray.getJSONArray(i);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                Event event = new Event(jsonObject);

                // Preload account.
                if (!TextUtils.isEmpty(event.getWhatId())) {
                    Account account = Account.getById(event.getWhatId());
                    event.setAccount(account);
                }
                events.add(event);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting events", e);
        }

        Comparator<Event> comparator = new StartDateComparator();
        if (!ascending) {
            comparator = Collections.reverseOrder(comparator);
        }
        Collections.sort(events, comparator);

        return events;
    }

    public static List<Event> getVisitsNewerThan(int days, String accountId, VisitState state) {
        Date endDate = new Date();
        Date startDate = DateUtils.addDays(endDate, -days);

        try {
            String smartSqlFilter = joinPredicates(
                    dateBetweenPredicate(startDate, endDate),
                    accountPredicate(accountId),
                    statePredicate(state));

            String smartSql = queryInAllVisits(SOUP_COLUMN, smartSqlFilter);
            return getEventsForSql(smartSql);
        } catch (ManifestProcessingException e) {
            Log.w(TAG, "Couldn't event newer than " + days, e);
            return Collections.emptyList();
        }
    }

    private static String accountPredicate(String accountId) {
        if (accountId == null) return null;
        return String.format("{%s:%s} = '%s' OR {%s:%s} = '%s'", DSAObjects.EVENT, EventFields.ACCOUNT_ID, accountId,
                DSAObjects.EVENT, EventFields.WHAT_ID, accountId);
    }

    private static String accountIsNotPredicate(String accountId) {
        if (accountId == null) return null;
        return String.format("{%s:%s} != '%s' AND {%s:%s} != '%s'", DSAObjects.EVENT, EventFields.ACCOUNT_ID, accountId,
                DSAObjects.EVENT, EventFields.WHAT_ID, accountId);
    }

    private static String statePredicate(VisitState state) {
        if (state == null) return null;
        return String.format("{%s:%s} = '%s'", DSAObjects.EVENT, EventFields.ESTADO_DE_VISITA__C, state.getState());
    }

    private static String controlStartExistsPredicate() {
        return DataManagerUtils.format("{Event:ControlStart} NOT IN ('' , 'null') AND {Event:ControlStart} IS NOT NULL", OBJECT_FORMAT_VALUES);
    }

    private static String dateBetweenPredicate(Date startDate, Date endDate) {
        return String.format("{%1$s:%2$s} >= '%3$s' AND {%1$s:%2$s} <= '%4$s'", DSAObjects.EVENT, EventFields.CONTROL_START_DATE_TIME,
                DateUtils.SERVER_DATE_TIME_FORMAT.format(startDate), DateUtils.SERVER_DATE_TIME_FORMAT.format(endDate));
    }

    private static String dateSincePredicate(Date startDate) {
        return String.format("{%1$s:%2$s} >= '%3$s'", DSAObjects.EVENT, EventFields.CONTROL_START_DATE_TIME,
                DateUtils.SERVER_DATE_TIME_FORMAT.format(startDate));
    }

    private static String joinPredicates(String... predicates) {
        if (predicates.length == 0) return null;

        StringBuilder sb = new StringBuilder();
        for (String predicate : predicates) {
            if (!TextUtils.isEmpty(predicate)) {
                if (sb.length() > 0) {
                    sb.append(" AND ");
                }

                sb.append('(').append(predicate).append(')');
            }
        }

        return sb.toString();
    }

    public static Event getById(String eventId) {
        DataManager dm = DataManagerFactory.getDataManager();
        Event event = DataManagerUtils.getById(dm, eventId, Event.class);

        if (event != null && !TextUtils.isEmpty(event.getWhatId())) {
            Account account = Account.getById(event.getWhatId());
            event.setAccount(account);
        }

        return event;
    }

    public static boolean checkoutEvent(Event event, String date, LatLng latlng) {
        JSONObject updatedEvent = event.setCheckOutWithParams(date, latlng);
        return updateEvent(event.getId(), updatedEvent);
    }

    public static void setAllCheckedInEventsAsCheckedOut(Date date, LatLng latlng) {
        String formattedDate = DateUtils.SERVER_DATE_TIME_FORMAT.format(date);
        List<Event> checkedInEvents = Event.getAllCheckedInVisits();
        for (Event checkedInEvent : checkedInEvents) {
            checkoutEvent(checkedInEvent, formattedDate, latlng);
        }
    }

    public static void setAllCheckedInEventsAsCheckedOut(Date date) {
        setAllCheckedInEventsAsCheckedOut(date, null);
    }

    public static synchronized List<Event> getCurrentInPlanVisits() {
        try {
            String endDate = DateUtils.getMondayDate();
            String startDate = DateUtils.getNextMondayDate();
            // We assume that StartDateTime is equal to ActivityDateTime.
            String smartSqlFilter = String.format("({%s:%s} < '%s' AND {%s:%s} >= '%s') AND ({%s:%s} = '%s') ORDER BY {%s:%s} DESC",
                    AbInBevObjects.EVENT, EventFields.START_DATE_TIME, startDate,
                    AbInBevObjects.EVENT, EventFields.END_DATE_TIME, endDate,
                    AbInBevObjects.EVENT, EventFields.VISIT_TYPE, VisitTypes.IN_PLAN,
                    AbInBevObjects.EVENT, EventFields.START_DATE_TIME);
            String smartSql = queryInCurrentVisits(SOUP_COLUMN, smartSqlFilter);
            List<Event> events = getEventsForSql(smartSql, false);

            removeInactiveAccounts(events);

            return events;
        } catch (ManifestProcessingException e) {
            Log.w(TAG, "Couldn't load data for current in plan visits.", e);
            return Collections.emptyList();
        }
    }

    public static synchronized List<Event> getCurrentOutOfPlanVisits() {
        try {
            String smartSqlFilter = String.format("{%1$s:%2$s} = '%3$s'",
                    DSAObjects.EVENT, EventFields.VISIT_TYPE, VisitTypes.OUT_OF_PLAN);

            String smartSql = queryInCurrentVisits(SOUP_COLUMN, smartSqlFilter);
            List<Event> events = getEventsForSql(smartSql, false);

            removeInactiveAccounts(events);

            return events;
        } catch (ManifestProcessingException e) {
            Log.w(TAG, "Couldn't load data for current out of plan visits.", e);
            return Collections.emptyList();
        }
    }

    private static void removeInactiveAccounts(List<Event> events) {
        ListIterator<Event> iterator = events.listIterator();
        while (iterator.hasNext()) {
            Account account = iterator.next().getAccount();

            if (account == null
                    || AccountStatus.BLOCKED.equals(account.getAccountStatus())
                    || AccountStatus.INACTIVE_CLIENT.equals(account.getAccountStatus())) {
                iterator.remove();
            }
        }
    }


    public static Event createEvent(Account account, LatLng latlng) {
        JSONObject json = Event.createJSONObjectEvent(account, latlng);
        DataManager dataManager = DataManagerFactory.getDataManager();
        String tempId = dataManager.createRecord(AbInBevObjects.EVENT, json);
        Event event = new Event(new JSONObject());
        event.setId(tempId);
        event.setAccount(account);
        return event;
    }

    public static Event createEvent(Account account) {
        return Event.createEvent(account, null);
    }

    public static JSONObject createJSONObjectEvent(Account account, LatLng latLng) {
        if (account == null || TextUtils.isEmpty(account.getId())) {
            Log.e(TAG, "Unable to create an event without an account.");
            return null;
        }
        return createJSONObjectEvent(account.getId(), latLng);
    }

    public static JSONObject createJSONObjectEvent(String whatId, LatLng latLng) {
        if (TextUtils.isEmpty(whatId)) {
            Log.e(TAG, "Unable to create an event without an account.");
            return null;
        }
        String id = UserAccountManager.getInstance().getStoredUserId();
        if (TextUtils.isEmpty(id)) {
            Log.e(TAG, "Unable to create an event without a user id.");
            return null;
        }
        JSONObject json = new JSONObject();
        try {
            json.put(EventFields.WHAT_ID, whatId);
            json.put(EventFields.OWNER_ID, id);
            Calendar cal = Calendar.getInstance();
            String date = DateUtils.dateToDateString(cal.getTime());
            json.put(EventFields.ACTIVITY_DATE, date);
            String now = DateUtils.SERVER_DATE_TIME_FORMAT.format(cal.getTime());

            // We assume that StartDateTime is equal to ActivityDateTime.
            json.put(EventFields.START_DATE_TIME, now);

            cal.add(Calendar.MINUTE, 30);
            String later = DateUtils.SERVER_DATE_TIME_FORMAT.format(cal.getTime());
            json.put(EventFields.END_DATE_TIME, later);
            json.put(EventFields.SUBJECT, EventSubject.VISIT);
            json.put(EventFields.PROGRAMADA__C, false);
            json.put(EventFields.ESTADO_DE_VISITA__C, VisitState.open.getState());
            json.put(EventFields.VISIT_TYPE, VisitTypes.OUT_OF_PLAN);
            json.put(EventFields.DURATION, 0);
            if (latLng != null) {
                json.put(EventFields.LATITUDE, latLng.latitude);
                json.put(EventFields.LONGITUDE, latLng.longitude);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error creating new event", e);
            return null;
        }
        return json;
    }

    public static boolean updateEvent(String id, JSONObject updatedObject) {
        DataManager dataManager = DataManagerFactory.getDataManager();
        return dataManager.updateRecord(AbInBevObjects.EVENT, id, updatedObject);
    }

    public static boolean updateEvent(Event event) {
        DataManager dataManager = DataManagerFactory.getDataManager();
        return dataManager.updateRecord(AbInBevObjects.EVENT, event.getId(), event.toJson());
    }

    // Event_Catalog__c != null - use this to fetch count for Events Title in combination with ActivityDate
    // TODO: Event_Catalog = null - use this for existing Event queries to filter only visits

    public static int getActiveEventCountWithEventCatalogue(String accountId) {
        return getActiveEventCountWithEventCatalogueByWhatId(accountId);
    }

    public static int getActiveEventCountWithEventCatalogueByUserId(String userId) {
        return getActiveEventCountWithEventCatalogueByWhatId(userId);
    }

    public static int getActiveEventCountWithEventCatalogueByWhatId(String whatId) {
        int count = 0;

        try {
            String select = "count()";
            String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} != 'null'",
                    AbInBevObjects.EVENT, EventFields.WHAT_ID, whatId,
                    AbInBevObjects.EVENT, EventFields.CATALOG);

            String smartSql = queryInCurrentVisits(select, smartSqlFilter);
            Log.e(TAG, "smartSql: " + smartSql);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            count = recordsArray.getJSONArray(0).getInt(0);
            Log.e(TAG, "count: " + count);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting active Market Program by Account ID: " + whatId, e);
        }

        return count;
    }

    public static List<Event> getEventsForEventPlanningList(String accountId) {
        return getEventsForEventPlanningListByWhatId(accountId);
    }

    public static List<Event> getEventsForEventPlanningListByUserId(String userId) {
        return getEventsForEventPlanningListByWhatId(userId);
    }

    public static List<Event> getEventsForEventPlanningListByWhatId(String whatId) {
        try {
            // We assume that StartDateTime is equal to ActivityDateTime.
            String endDate = DateUtils.getMondayDate();
            String startDate = DateUtils.getNextMondayDate();
            // We assume that StartDateTime is equal to ActivityDateTime.
            String smartSqlFilter = String.format("({%s:%s} = '%s') AND ({%s:%s} < '%s' AND {%s:%s} >= '%s') AND ({%s:%s} = '%s') ORDER BY {%s:%s} DESC",
                    AbInBevObjects.EVENT, EventFields.OWNER_ID, whatId,
                    AbInBevObjects.EVENT, EventFields.START_DATE_TIME, startDate,
                    AbInBevObjects.EVENT, EventFields.END_DATE_TIME, endDate,
                    AbInBevObjects.EVENT, EventFields.VISIT_TYPE, VisitTypes.IN_PLAN,
                    AbInBevObjects.EVENT, EventFields.START_DATE_TIME);

            String smartSql = queryInCurrentVisits(SOUP_COLUMN, smartSqlFilter);

            return Event.getEventsForSql(smartSql);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting Events for planning list: " + whatId, e);
            return Collections.emptyList();
        }
    }

    public static Event getEventForMorningMeeting() {
        LocalQueryHelper queryExecutor = getQueryExecutor();
        Event event = null;

        try {
            List<Object> result = queryExecutor.execute("getTodayMorningMeetingEvents", "{Event:_soup}");
            if (result != null && !result.isEmpty()) {
                event = new Event((JSONObject) result.get(0));
            }
        } catch (ManifestProcessingException e) {
            Log.w(TAG, "Couldn't load data for the morning meeting.", e);
        }

        return event;
    }

    public static Event getLastMorningMeetingEvent() {
        FormatValues fv = new FormatValues();
        fv.addAll(OBJECT_FORMAT_VALUES);
        fv.addAll(Morning_Meeting__c.OBJECT_FORMAT_VALUES);

        String query = "SELECT {Event:_soup} FROM {Event} " +
                "WHERE {Event:VisitType} = 'Morning Meeting' " +
                "ORDER BY {Event:StartDateTime} DESC " +
                "LIMIT 1";

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObject(dm, query, fv, Event.class);
    }

    private static String queryInCurrentVisits(String select, String where) throws ManifestProcessingException {
        LocalQueryHelper queryExecutor = getQueryExecutor();
        String currentVisitsQuery = queryExecutor.getQuery("getCurrentVisits", "*");

        // Select elements from current visit list.
        String smartSql = String.format("SELECT %s FROM (%s) as {%s}", select, currentVisitsQuery, DSAObjects.EVENT);
        if (!TextUtils.isEmpty(where)) {
            smartSql += " WHERE " + where;
        }
        return smartSql;
    }

    private static String queryInAllVisits(String select, String where) throws ManifestProcessingException {
        LocalQueryHelper queryExecutor = getQueryExecutor();
        String currentVisitsQuery = queryExecutor.getQuery("getAllVisits", "*");

        // Select elements from current visit list.
        String smartSql = String.format("SELECT %s FROM (%s) as {%s}", select, currentVisitsQuery, DSAObjects.EVENT);
        if (!TextUtils.isEmpty(where)) {
            smartSql += " WHERE " + where;
        }

        return smartSql;
    }

    private static LocalQueryHelper getQueryExecutor() {
        JexlManifestProcessor processor = (JexlManifestProcessor) ManifestProcessorFactory.getInstance().createProcessor();
        return processor.getLocalQueryHelper();
    }

    private static class StartDateComparator implements Comparator<Event> {

        @Override
        public int compare(Event lhs, Event rhs) {

            Date startDateTime = lhs.getStartDateTime();
            Date rhsStartDateTime = rhs.getStartDateTime();
            if (startDateTime == null && rhsStartDateTime == null) {
                return 0;
            }

            if (startDateTime == null) {
                return -1;
            }
            if (rhsStartDateTime == null) {
                return 1;
            }
            return startDateTime
                    .compareTo(rhsStartDateTime);
        }
    }
}
