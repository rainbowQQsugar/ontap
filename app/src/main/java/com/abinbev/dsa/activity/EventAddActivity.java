package com.abinbev.dsa.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Event_Catalog__c;
import com.abinbev.dsa.model.Event_Equipment__c;
import com.abinbev.dsa.ui.presenter.EventAddPresenter;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.DateUtils;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import butterknife.OnClick;

public class EventAddActivity extends EventAbstractActivity implements EventAddPresenter.ViewModel {

    public static final String EVENT_NAME_EXTRA = "event_name";
    public static final String ACCOUNT_ID_EXTRA = "account_id";
    public static final String EVENT_DATE_EXTRA = "event_date";
    public static final String RECORD_TYPE_ID_EXTRA = "record_type_id";

    private String accountId;
    private Long eventDate;
    private EventAddPresenter eventAddPresenter;
    private Date dateFromCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);

        if (getIntent() != null) {
            accountId = getIntent().getStringExtra(EventAddActivity.ACCOUNT_ID_EXTRA);
            eventDate = getIntent().getLongExtra(EventAddActivity.EVENT_DATE_EXTRA, 0);
            account = Account.getById(accountId);
            eventHeader.setText(account.getName() + getString(R.string.events_ending));

            if(eventDate > 0) {
                Date chosenDate = new Date(eventDate);
                setChosenDate(chosenDate);
            }

            eventAddPresenter = new EventAddPresenter(getIntent().getStringExtra(EventAddActivity.RECORD_TYPE_ID_EXTRA));
            eventAddPresenter.setViewModel(this);
            eventAddPresenter.start();
        }
    }

    @Override
    public void setChosenDate(Date date) {
        if (date != null) {
            startTime = date;
            endTime = date;
            dateFromCalendar = date;
            String dateToDisplay = DateUtils.formatDateTimeEvents(DateUtils.dateToDateTime(date));
            eventStartDate.setText(dateToDisplay);
            eventEndDate.setText(dateToDisplay);
            eventStartHour.setText(getString(R.string.hour_placeholder));
            eventEndHour.setText(getString(R.string.hour_placeholder));
        }
    }

    @Override
    public void setEventCatalogRecord(Event_Catalog__c eventCatalogRecord) {
        equipmentContainer.setVisibility(eventCatalogRecord.getUsageOfEquipment() ? View.VISIBLE : View.GONE);
        this.eventCatalogRecord = eventCatalogRecord;
        eventHeaderText.setText(eventCatalogRecord.getName());
    }

    @Override
    void saveEvent() {
        if(!checkTime(startTime, endTime))
            return;



        JSONObject json = new JSONObject();
        try {
            if(startTime != null) {
                json.put(AbInBevConstants.EventFields.START_DATE_TIME, DateUtils.SERVER_DATE_TIME_FORMAT.format(startTime));
            }
            if(endTime != null) {
                json.put(AbInBevConstants.EventFields.END_DATE_TIME,  DateUtils.SERVER_DATE_TIME_FORMAT.format(endTime));
            }
            if(selectedEquipment != -1 && !equipmentList.isEmpty()) {
                String equipmentId = equipmentList.get(selectedEquipment).getId();
                json.put(AbInBevConstants.EventFields.EQUIPMENT, equipmentId);
                if(!Event_Equipment__c.isEquipmentAvailableInDate(equipmentId, DateUtils.SERVER_DATE_TIME_FORMAT.format(startTime), DateUtils.SERVER_DATE_TIME_FORMAT.format(endTime))){
                    Toast.makeText(this, getResources().getString(R.string.equipment_not_available_for_date), Toast.LENGTH_SHORT).show();
                    return;
                }

            }


            if (eventCatalogRecord != null) {
                json.put(AbInBevConstants.EventFields.CATALOG, eventCatalogRecord.getId());
                json.put(AbInBevConstants.EventFields.SUBJECT, eventCatalogRecord.getName());
            }
            json.put(AbInBevConstants.EventFields.WITH_MUSIC_BAND, bandSwitch.isChecked());
            json.put(AbInBevConstants.EventFields.WHAT_ID, accountId);

            DataManagerFactory.getDataManager().createRecord("Event", json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SyncUtils.TriggerRefresh(EventAddActivity.this);
        Toast.makeText(this, getResources().getString(R.string.event_added), Toast.LENGTH_SHORT).show();
        finish();
    }

    @OnClick(R.id.event_start_hour)
    public void eventStartHourClicked() {
        setHour(AbInBevConstants.EventFields.START_DATE_TIME, dateFromCalendar);
    }

    @OnClick(R.id.event_end_hour)
    public void eventEndHourClicked() {
        setHour(AbInBevConstants.EventFields.END_DATE_TIME, dateFromCalendar);
    }

}

