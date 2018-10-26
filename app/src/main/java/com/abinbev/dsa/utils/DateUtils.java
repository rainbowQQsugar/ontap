package com.abinbev.dsa.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

    public static final String TAG = DateUtils.class.getSimpleName();

    public static final SimpleDateFormat SERVER_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    public static final SimpleDateFormat SERVER_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);

    private static final SimpleDateFormat DATE_STRING_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    public static final SimpleDateFormat DATE_STRING_FORMAT2 = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat DISPLAY_FORMAT_SHORT = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private static final SimpleDateFormat DISPLAY_FORMAT_LONG = new SimpleDateFormat("EEEE, dd/MM/yyyy");
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
    private static final SimpleDateFormat CN_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final SimpleDateFormat DATE_TIME_FORMAT_AM_PM = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss a", Locale.US);
    private static final SimpleDateFormat NEGOCIACION_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private static final SimpleDateFormat EVENT_DETAILS_DATE_FORMAT = new SimpleDateFormat("EEEE, MMM dd, yyyy ");
    private static final SimpleDateFormat CN_DATE_LONG_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
    private static final SimpleDateFormat CN_DATE_LONG_FORMAT2 = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.CHINA);
    public static final SimpleDateFormat CN_DATE_SHORT_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    public static final SimpleDateFormat HOURS_MINUTES = new SimpleDateFormat("KK:mm a");

    public static final TimeZone SERVER_TIME_ZONE = TimeZone.getTimeZone("UTC");

    static {
        SERVER_DATE_FORMAT.setTimeZone(SERVER_TIME_ZONE);
        SERVER_DATE_TIME_FORMAT.setTimeZone(SERVER_TIME_ZONE);
        NEGOCIACION_DATE_TIME_FORMAT.setTimeZone(SERVER_TIME_ZONE);
    }

    public static String formatDateStringShort(String dateString) {
        return formatString(DATE_STRING_FORMAT, DISPLAY_FORMAT_SHORT, dateString);
    }

    public static String formatDateStringCNLong(String dateString) {
        return formatString(SERVER_DATE_TIME_FORMAT, CN_DATE_LONG_FORMAT, dateString);
    }

    public static String formatDateStringCNLong2(String dateString) {
        return formatString(SERVER_DATE_TIME_FORMAT, CN_DATE_LONG_FORMAT2, dateString);
    }

    public static String formatDateStringLong(String dateString, Context context) {
        String language = AppPreferenceUtils.getLanguageLocale(context);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale(language));
        return formatString(DATE_STRING_FORMAT, dateFormat, dateString);
    }

    public static String fromServerDateTimeToDateTime(String dateTime) {
        return formatString(SERVER_DATE_TIME_FORMAT, DATE_TIME_FORMAT_AM_PM, dateTime);
    }

    public static String fromDateTimeToServerDateTime(Date dateTime) {
        return SERVER_DATE_TIME_FORMAT.format(dateTime);
    }

    public static String fromServerDateTimeToDate(String dateTime) {
        return formatString(SERVER_DATE_TIME_FORMAT, DATE_STRING_FORMAT, dateTime);
    }

    public static String formatDateTimeShort(String dateTime) {
        return formatString(DATE_TIME_FORMAT, DISPLAY_FORMAT_SHORT, dateTime);
    }

    public static String formatDateTimeLong(String dateTime) {
        return formatString(DATE_TIME_FORMAT, DISPLAY_FORMAT_LONG, dateTime);
    }

    public static String formatDateTimeAMPM(String dateTime) {
        return formatString(DATE_TIME_FORMAT, DATE_TIME_FORMAT_AM_PM, dateTime);
    }

    public static String formatDateTimeEvents(String dateTime) {
        return formatString(DATE_TIME_FORMAT, EVENT_DETAILS_DATE_FORMAT, dateTime);
    }

    public static String formatDateHoursMinutesAMPM(String dateTime) {
        return formatString(DATE_TIME_FORMAT, HOURS_MINUTES, dateTime);
    }

    public static Date twoDaysLater() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 2);
        return dateFromString(SERVER_DATE_FORMAT.format(c.getTime()));
    }

    private static synchronized String formatString(SimpleDateFormat parserFormat, SimpleDateFormat resultFormat, String date) {
        if (TextUtils.isEmpty(date) || "null".equals(date)) {
            return "";
        }

        try {
            return resultFormat.format(parserFormat.parse(date));
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + date, e);
            return "";
        }
    }


    public static synchronized Date dateFromStringTimeCN(String dateString) {
        try {
            CN_DATE_TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
            return CN_DATE_TIME_FORMAT.parse(dateString);
        } catch (ParseException e) {
            Log.e(TAG, "dateFromStringTimeCN: error parsing date from string " + dateString, e);
        }
        return null;
    }

    public static synchronized Date dateFromString(String dateString) {
        try {
            return DATE_STRING_FORMAT.parse(dateString);
        } catch (ParseException e) {
            Log.e(TAG, "dateFromString: error parsing date from string " + dateString, e);
        }
        return null;
    }

    public static synchronized Date dateFromStringShortCN(String dateString) {
        try {
            return CN_DATE_SHORT_FORMAT.parse(dateString);
        } catch (ParseException e) {
            Log.e(TAG, "dateFromStringShortCN: error parsing date from string " + dateString, e);
        }
        return null;
    }

    public static synchronized Date dateFromDateTimeString(String dateTimeString) {
        try {
            return DATE_TIME_FORMAT.parse(dateTimeString);
        } catch (ParseException e) {
            Log.e(TAG, "dateFromString: error parsing date from string " + dateTimeString, e);
        }
        return null;
    }

    public static synchronized String dateToDateString(Date date) {
        return DATE_STRING_FORMAT.format(date);
    }

    public static synchronized String currentDateString() {
        return dateToDateString(new Date());
    }

    public static synchronized String dateToDateTime(Date date) {
        return DATE_TIME_FORMAT.format(date);
    }

    public static synchronized String dateToNegociacionDateTime(Date date) {
        return NEGOCIACION_DATE_TIME_FORMAT.format(date);
    }

    public static long stripTime(long dateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static Date stripTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static long getNextSundayTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        calendar.add(Calendar.DATE, 7);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime().getTime();
    }

    public static String getMondayDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date time = calendar.getTime();

        return SERVER_DATE_TIME_FORMAT.format(time);
    }

    public static String getNextMondayDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 7);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date time = calendar.getTime();
        return SERVER_DATE_TIME_FORMAT.format(time);
    }

    public static String getNegociacionStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
        return dateToNegociacionDateTime(calendar.getTime());

    }

    public static long differenceDates(Date startDate, Date endDate, TimeUnits timeUnit) {
        try {
            long diff = endDate.getTime() - startDate.getTime();
            return timeUnit.value(diff);
        } catch (Exception e) {
            Log.e(TAG, "Exception in calculation date differences", e);
        }
        return 0;
    }

    public static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    public static boolean isBeforeToday(Calendar calendar) {
        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(stripTime(today.getTimeInMillis()));
        calendar.setTimeInMillis(stripTime(calendar.getTimeInMillis()));
        return calendar.before(today);
    }

    public static int daysBetween(Date start, Date end) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);
        startCalendar.set(Calendar.HOUR_OF_DAY, 11);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(end);
        endCalendar.set(Calendar.HOUR_OF_DAY, 11);
        endCalendar.set(Calendar.MINUTE, 0);
        endCalendar.set(Calendar.SECOND, 0);
        endCalendar.set(Calendar.MILLISECOND, 0);

        return (int) ((endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis()) / 86400000L);
    }

    public enum TimeUnits {
        SECONDS(TimeUnits.SECOND_IN_MILISECONDS),
        MINUTES(TimeUnits.MINUTE_IN_MILISECONDS),
        HOURS(TimeUnits.HOURS_IN_MILISECONDS),
        DAYS(TimeUnits.DAY_IN_MILISECONDS);
        public static final long SECOND_IN_MILISECONDS = 1000;
        public static final long MINUTE_IN_MILISECONDS = 60000;
        public static final long HOURS_IN_MILISECONDS = 3600000;
        public static final long DAY_IN_MILISECONDS = 8640000000L;

        private final long factor;

        TimeUnits(long factor) {
            this.factor = factor;
        }

        public long value(long diff) {
            return diff / factor;
        }
    }


    public static synchronized boolean isBirthdayToday(String birthdateAsString) {
        if (TextUtils.isEmpty(birthdateAsString) || "null".equals(birthdateAsString)) {
            return false;
        }

        Date birthdate = null;

        try {
            birthdate = DATE_STRING_FORMAT.parse(birthdateAsString);
        } catch (ParseException e) {
            Log.e(TAG, "Exception parsing birthdate: " + birthdateAsString);
            return false;
        }

        if (birthdate != null) {
            Calendar birthdayCalendar = Calendar.getInstance();
            birthdayCalendar.setTime(birthdate);

            int month = birthdayCalendar.get(Calendar.MONTH);
            int day = birthdayCalendar.get(Calendar.DAY_OF_MONTH);


            Calendar today = Calendar.getInstance();
            today.get(Calendar.DAY_OF_MONTH);
            today.get(Calendar.MONTH);

            return month == today.get(Calendar.MONTH) && day == today.get(Calendar.DAY_OF_MONTH);
        } else {
            return false;
        }
    }

    public static Calendar UTCnow() {
        Calendar now = Calendar.getInstance();
        now.setTimeZone(TimeZone.getTimeZone("UTC"));
        return now;
    }

    public static String getTimeZoneDate(String timeZoneFloating, SimpleDateFormat dateformat) {
        TimeZone time = TimeZone.getTimeZone("GMT" + timeZoneFloating);
        dateformat.setTimeZone(time);
        return dateformat.format(new Date());
    }

    /**
     * date sort Desc
     */
    public static class ComparatorDate implements Comparator {

        public int compare(Object beginDate, Object endDate) {
            try {
                String format = "yyyy-MM-dd";
                Date begin = new SimpleDateFormat(format).parse((String) beginDate);
                Date end = new SimpleDateFormat(format).parse((String) endDate);
                if (begin.after(end)) {
                    return -1;
                } else {
                    return 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    /**
     * Get day lag
     *
     * @param startDate current Timer
     * @param endDate   end timer
     * @return
     */
    public static int getGapCount(Date startDate, Date endDate) throws ParseException {

        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(startDate);
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(endDate);
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);

        return (int) ((fromCalendar.getTime().getTime() - toCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
    }

}
