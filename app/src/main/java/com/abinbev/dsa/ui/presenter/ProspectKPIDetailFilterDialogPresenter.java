package com.abinbev.dsa.ui.presenter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

import com.abinbev.dsa.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ProspectKPIDetailFilterDialogPresenter extends AbstractRxPresenter<ProspectKPIDetailFilterDialogPresenter.ViewModel> implements Presenter<ProspectKPIDetailFilterDialogPresenter.ViewModel> {

    private static final String TAG = ProspectKPIDetailFilterDialogPresenter.class.getSimpleName();

    public interface ViewModel {
        void setStartDate(Date date, String formatDate);

        void setEndDate(Date date, String formatDate);
    }

    private Context context;
    private SimpleDateFormat dateFormat;

    public ProspectKPIDetailFilterDialogPresenter(Context context) {
        super();
        this.context = context;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private void showDatePicker(Date date, DatePickerDialog.OnDateSetListener onDateListener) {
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
        DatePickerDialog datePickerDialog = new DatePickerDialog(this.context, onDateListener, year, month, day);
        datePickerDialog.show();
    }

    private DatePickerDialog.OnDateSetListener startDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth,0,0,0);
            calendar.set(Calendar.MILLISECOND,1);
            Date date = calendar.getTime();
            viewModel().setStartDate(date, dateFormat.format(date));
        }
    };

    public void selectStartDate(Date date) {
        showDatePicker(date, startDateSetListener);
    }

    private DatePickerDialog.OnDateSetListener endDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth,23,59,59);
            calendar.set(Calendar.MILLISECOND,999);
            Date date = calendar.getTime();
            viewModel().setEndDate(date, dateFormat.format(date));
        }
    };

    public void selectEndDate(Date date) {
        showDatePicker(date, endDateSetListener);
    }

    public void clear() {
        viewModel().setStartDate(null, null);
        viewModel().setEndDate(null, null);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }
}
