package com.abinbev.dsa.ui.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.abinbev.dsa.R;
import com.abinbev.dsa.utils.DateUtils;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Diana Błaszczyk on 24/10/17.
 */

public class DateRangeHeaderView extends LinearLayout {

    private static final String TAG = DateRangeHeaderView.class.getSimpleName();

    @Bind(R.id.start_date)
    protected
    TextView startField;

    @Bind(R.id.end_date)
    protected
    TextView endField;

    public Date startDate;
    public Date endDate;

    public DateRangeHeaderView(Context context) {
        this(context, null);
    }

    public DateRangeHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DateRangeHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.negotiation_header_view, this);
        ButterKnife.bind(this);
    }

    public boolean allowPastDate() {
        return true;
    }

    @OnClick(R.id.start_date)
    public void pickStartDate() {
        showPicker(startDate, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                if (!allowPastDate() && DateUtils.isBeforeToday(calendar)) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(getContext().getString(R.string.invalid_date))
                            .setMessage(getContext().getString(R.string.date_error_message))
                            .setPositiveButton(R.string.ok, null)
                            .create().show();
                    setStartDate(null);
                } else {
                    setStartDate(DateUtils.dateToDateString(calendar.getTime()));
                }
            }
        });
    }

    public void setStartDate(String date) {
        if (TextUtils.isEmpty(date)) {
            startDate = null;
            startField.setText("");
        } else {
            startDate = DateUtils.dateFromString(date);
            startField.setText(DateUtils.formatDateStringShort(DateUtils.dateToDateString(startDate)));
            if (endDate != null && startDate.after(endDate) || startDate.equals(endDate)) {
                setEndDate(null);
            }
        }
        postStartDate(date);
    }


    public void setEndDate(String date) {
        if (TextUtils.isEmpty(date)) {
            endDate = null;
            endField.setText("");
        } else {
            endDate = DateUtils.dateFromString(date);
            endField.setText(DateUtils.formatDateStringShort(DateUtils.dateToDateString(endDate)));
            if (startDate != null && endDate.before(startDate) || endDate.equals(startDate)) {
                setStartDate(null);
            }
            Calendar endDateCal = Calendar.getInstance();
            endDateCal.setTime(endDate);
            endDateCal.set(Calendar.HOUR_OF_DAY, 23);
            endDateCal.set(Calendar.MINUTE, 59);
            endDate = endDateCal.getTime();
        }
        postEndDate(date);
    }

    public void postStartDate(String date) {}

    public void postEndDate(String date) {}

    @OnClick(R.id.end_date)
    public void pickEndDate() {
        showPicker(endDate, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                if (!allowPastDate() && DateUtils.isBeforeToday(calendar)) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(getContext().getString(R.string.invalid_date))
                            .setMessage(getContext().getString(R.string.date_error_message))
                            .setPositiveButton(R.string.ok, null)
                            .create().show();
                    setEndDate(null);
                } else {
                    setEndDate(DateUtils.dateToDateString(calendar.getTime()));
                }
            }
        });
    }

    private void showPicker(final Date date, DatePickerDialog.OnDateSetListener onDateSetListener) {
        int year, month, day;
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
        } else {
            Calendar today = Calendar.getInstance();
            year = today.get(Calendar.YEAR);
            month = today.get(Calendar.MONTH);
            day = today.get(Calendar.DAY_OF_MONTH);
        }
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), onDateSetListener, year, month, day);
        datePickerDialog.show();
    }


}
