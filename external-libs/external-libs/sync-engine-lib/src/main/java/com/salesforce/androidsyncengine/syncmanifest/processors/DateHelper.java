package com.salesforce.androidsyncengine.syncmanifest.processors;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Jakub Stefanowski on 27.03.2017.
 */
public class DateHelper {

    private final SimpleDateFormat dateFormat;

    private final Calendar calendar;

    public DateHelper(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
        this.calendar = Calendar.getInstance();

        this.calendar.setFirstDayOfWeek(Calendar.MONDAY);
    }

    public DateHelper(SimpleDateFormat dateFormat, TimeZone timeZone) {
        this.dateFormat = dateFormat;
        this.calendar = Calendar.getInstance(timeZone);

        this.calendar.setFirstDayOfWeek(Calendar.MONDAY);
    }

    public DateBuilder fromTimestamp(long timestamp) {
        calendar.setTimeInMillis(timestamp);
        return createBuilder();
    }

    public DateBuilder now() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return createBuilder();
    }

    public DateBuilder today() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return createBuilder();
    }

    public DateBuilder thisWeekMonday() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return createBuilder();
    }

    public DateBuilder thisWeekTuesday() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        return createBuilder();
    }

    public DateBuilder thisWeekWednesday() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        return createBuilder();
    }

    public DateBuilder thisWeekThursday() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        return createBuilder();
    }

    public DateBuilder thisWeekFriday() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        return createBuilder();
    }

    public DateBuilder thisWeekSaturday() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        return createBuilder();
    }

    public DateBuilder thisWeekSunday() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return createBuilder();
    }

    /**
     * Sets the date to the beginning of current month.
     */
    public DateBuilder thisMonth() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return createBuilder();
    }

    public DateBuilder thisMonthEnd() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH));
        return createBuilder();
    }

    public DateBuilder thisYear() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        return createBuilder();
    }

    private DateBuilder createBuilder() {
        return new DateBuilder(dateFormat, (Calendar) calendar.clone());
    }

    public static class DateBuilder {


        private final SimpleDateFormat dateFormat;

        private final Calendar calendar;

        public DateBuilder(SimpleDateFormat dateFormat, Calendar calendar) {
            this.dateFormat = dateFormat;
            this.calendar = calendar;
        }

        public DateBuilder addHours(int hours) {
            calendar.add(Calendar.HOUR_OF_DAY, hours);
            return this;
        }

        public DateBuilder setHours(int hours) {
            calendar.set(Calendar.HOUR_OF_DAY, hours);
            return this;
        }

        public DateBuilder addDays(int days) {
            calendar.add(Calendar.DAY_OF_YEAR, days);
            return this;
        }

        public DateBuilder setDayOfYear(int days) {
            calendar.set(Calendar.DAY_OF_YEAR, days);
            return this;
        }

        public DateBuilder setDayOfMonth(int days) {
            calendar.set(Calendar.DAY_OF_MONTH, days);
            return this;
        }

        public DateBuilder addWeeks(int weeks) {
            calendar.add(Calendar.WEEK_OF_YEAR, weeks);
            return this;
        }

        public DateBuilder setWeeks(int weeks) {
            calendar.set(Calendar.WEEK_OF_YEAR, weeks);
            return this;
        }

        public DateBuilder addMonths(int months) {
            calendar.add(Calendar.MONTH, months);
            return this;
        }

        public DateBuilder setMonths(int months) {
            calendar.set(Calendar.MONTH, months);
            return this;
        }

        public DateBuilder addYears(int years) {
            calendar.add(Calendar.YEAR, years);
            return this;
        }

        public DateBuilder setYears(int years) {
            calendar.set(Calendar.YEAR, years);
            return this;
        }

        public boolean isGreaterThan(DateBuilder other) {
            if (other == null) throw new NullPointerException();
            return calendar.getTimeInMillis() > other.calendar.getTimeInMillis();
        }

        public boolean isGreaterOrEqual(DateBuilder other) {
            if (other == null) throw new NullPointerException();
            return calendar.getTimeInMillis() >= other.calendar.getTimeInMillis();
        }

        public int daysBetween(DateBuilder other) {
            if (other == null) throw new NullPointerException();
            return daysBetween(calendar, other.calendar);
        }

        private static int daysBetween(Calendar c1, Calendar c2) {
            Calendar smallerCal;
            Calendar greaterCal;
            int days = 0;
            int sign;

            if (c1.getTimeInMillis() > c2.getTimeInMillis()) {
                smallerCal = c2;
                greaterCal = (Calendar) c1.clone();  //We will be modifying that so new instance is required
                sign = -1;
            } else {
                smallerCal = c1;
                greaterCal = (Calendar) c2.clone();  //We will be modifying that so new instance is required
                sign = 1;
            }

            while (greaterCal.get(Calendar.YEAR) > smallerCal.get(Calendar.YEAR)) {
                int dayOfYear = greaterCal.get(Calendar.DAY_OF_YEAR);
                greaterCal.add(Calendar.DAY_OF_YEAR, -dayOfYear);
                days += dayOfYear;
            }

            days += greaterCal.get(Calendar.DAY_OF_YEAR) - smallerCal.get(Calendar.DAY_OF_YEAR);

            return days * sign;
        }

        @Override
        public String toString() {
            return dateFormat.format(calendar.getTime());
        }
    }
}
