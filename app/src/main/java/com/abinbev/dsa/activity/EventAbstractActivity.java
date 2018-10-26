package com.abinbev.dsa.activity;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Event_Catalog__c;
import com.abinbev.dsa.model.Event_Equipment__c;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by parak0 on 05.08.2016.
 */

public abstract class EventAbstractActivity extends AppBaseActivity {

    @Bind(R.id.event_header)
    TextView eventHeader;

    @Bind(R.id.event_start_date)
    TextView eventStartDate;

    @Bind(R.id.event_end_date)
    TextView eventEndDate;

    @Bind(R.id.event_start_hour)
    TextView eventStartHour;

    @Bind(R.id.event_end_hour)
    TextView eventEndHour;

    @Bind(R.id.event_header_text)
    EditText eventHeaderText;

    @Bind(R.id.switch_band)
    Switch bandSwitch;

    @Bind(R.id.event_accessories)
    TextView eventEquipment;

    @Bind(R.id.equipment_container)
    RelativeLayout equipmentContainer;

    Date startTime;
    Date endTime;
    int selectedEquipment = -1;
    List<Event_Equipment__c> equipmentList;
    String[] itemsToChoose;
    Event_Catalog__c eventCatalogRecord;
    Account account;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_event_edit;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.menu_save) {
            saveEvent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu, menu);
        return true;
    }

    public void setHour(final String dateField, final Date entryDate){
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);
        TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                Calendar c = Calendar.getInstance();
                if(dateField.equals(AbInBevConstants.EventFields.START_DATE_TIME)) {
                    c.setTime(entryDate);
                    c.set(Calendar.HOUR_OF_DAY, selectedHour);
                    c.set(Calendar.MINUTE, selectedMinute);
                    startTime = c.getTime();
                    eventStartHour.setText(String.format("%02d:%02d %s", selectedHour%12, selectedMinute, (c.get(Calendar.AM_PM) > 0 ? "PM" : "AM")));
                } else {
                    c.setTime(entryDate);
                    c.set(Calendar.HOUR_OF_DAY, selectedHour);
                    c.set(Calendar.MINUTE, selectedMinute);
                    endTime = c.getTime();
                    eventEndHour.setText(String.format("%02d:%02d %s", selectedHour%12, selectedMinute, (c.get(Calendar.AM_PM) > 0 ? "PM" : "AM")));
                }
            }
        }, hour, minute, false);
        timePicker.show();
    }

    @OnClick(R.id.equipment_container)
    public void pickEquipment() {

        if (equipmentList == null) {
            equipmentList = Event_Equipment__c.getActiveEquipmentForAccount(account);
            itemsToChoose = new String[equipmentList.size()];
            for (int i = 0; i < equipmentList.size(); i++)
                itemsToChoose[i] = equipmentList.get(i).getName();
        }

        if (!equipmentList.isEmpty())
            showEquipmentPicker(equipmentList);
        else
            Toast.makeText(this, getResources().getString(R.string.equipment_unavailable), Toast.LENGTH_SHORT).show();
    }

    void chooseEquipment(int index) {
        eventEquipment.setText(equipmentList.get(index).getName());
    }

    void showEquipmentPicker(List<Event_Equipment__c> values) {
        String[] labels = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            labels[i] = values.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pick_equipment)
                .setSingleChoiceItems(labels, selectedEquipment >= 0 ? selectedEquipment : 0,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                selectedEquipment = which;
                            }
                        })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //need to account for the user not actively clicking the first item since it
                        //is already selected
                        selectedEquipment = selectedEquipment == -1 ? 0 : selectedEquipment;
                        chooseEquipment(selectedEquipment);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .create()
                .show();
    }

    boolean checkTime(Date startTime, Date endTime) {

        if (startTime == null) {
            Toast.makeText(this, getResources().getString(R.string.start_time_is_not_set), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (endTime == null) {
            Toast.makeText(this, getResources().getString(R.string.end_time_is_not_set), Toast.LENGTH_SHORT).show();
            return false;
        }

        if(startTime.compareTo(endTime) > 0) {
            Toast.makeText(this, getResources().getString(R.string.wrong_event_time), Toast.LENGTH_SHORT).show();
            return false;
        }

        Long difference = DateUtils.differenceDates(startTime, endTime, DateUtils.TimeUnits.HOURS);
        Double maxDuration = eventCatalogRecord == null ? null : eventCatalogRecord.getMaxDuration();
        if (maxDuration == null || maxDuration.longValue() >= difference)
            return true;
        else {
            Toast.makeText(this, getResources().getString(R.string.exceeded_duration), Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    abstract void saveEvent();
}
