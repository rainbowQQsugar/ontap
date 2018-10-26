package com.abinbev.dsa.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.Event_Catalog__c;
import com.abinbev.dsa.model.Event_Equipment__c;
import com.abinbev.dsa.ui.presenter.EventOperationPresenter;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.DateUtils;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.OnClick;

public class EventEditActivity extends EventAbstractActivity implements EventOperationPresenter.ViewModel {

    public static final String EVENT_ID_EXTRA = "event_id";
    private String eventId = "";
    private EventOperationPresenter eventOperationPresenter;

    Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);

        if (getIntent() != null) {
            eventId = getIntent().getStringExtra(EventDetailActivity.EVENT_ID_EXTRA);
            eventOperationPresenter = new EventOperationPresenter(eventId);
            eventOperationPresenter.setViewModel(this);
            eventOperationPresenter.start();
        }
    }

    @Override
    public void setEvent(Event event) {
        currentEvent = event;
        account = event.getAccount();
        eventHeader.setText(event.getAccount().getName() + getString(R.string.events_ending));
        eventHeaderText.setText(event.getSubject());
        bandSwitch.setChecked(event.getIsEventWithMusicBand() ? Boolean.TRUE : Boolean.FALSE);

        startTime = event.getStartDateTime();
        endTime = DateUtils.dateFromDateTimeString(event.getEndDate());

        if(!event.getEquipment().isEmpty() && event.getEquipment() != null) {
            Event_Equipment__c equipment = Event_Equipment__c.getById(event.getEquipment());
            eventEquipment.setText(equipment.getName());
        }

        if(!event.getEndDate().isEmpty() && event.getEndDate() != null) {
            eventStartDate.setText(DateUtils.formatDateTimeEvents(event.getEndDate()));
            eventEndDate.setText(DateUtils.formatDateTimeEvents(event.getEndDate()));
            if(event.getStartDateTime() != null) {
                String toDateTime = DateUtils.dateToDateTime(event.getStartDateTime());
                String startHour = DateUtils.formatDateHoursMinutesAMPM(toDateTime);
                String endHour = DateUtils.formatDateHoursMinutesAMPM(event.getEndDate());
                eventStartHour.setText(startHour);
                eventEndHour.setText(endHour);
            }
        }
    }

    @Override
    public void setEventCatalogRecord(Event_Catalog__c eventCatalogRecord) {
        if (eventCatalogRecord == null) {
            equipmentContainer.setVisibility(View.GONE);
        }
        else {
            equipmentContainer.setVisibility(eventCatalogRecord.getUsageOfEquipment() ? View.VISIBLE : View.GONE);
            this.eventCatalogRecord = eventCatalogRecord;
            eventHeaderText.setText(eventCatalogRecord.getName());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eventOperationPresenter.stop();
    }

    @Override
    void saveEvent() {
        JSONObject json = new JSONObject();
        Boolean success = false;
        try {
            if(startTime != null)
                json.put(AbInBevConstants.EventFields.START_DATE_TIME, DateUtils.SERVER_DATE_TIME_FORMAT.format(startTime));
            if(endTime != null)
                json.put(AbInBevConstants.EventFields.END_DATE_TIME, DateUtils.SERVER_DATE_TIME_FORMAT.format(endTime));


            if(!checkTime(startTime, endTime))
                return;

            if(selectedEquipment != -1 && !equipmentList.isEmpty()) {
                String equipmentId = equipmentList.get(selectedEquipment).getId();
                json.put(AbInBevConstants.EventFields.EQUIPMENT, equipmentId);
                if(!Event_Equipment__c.isEquipmentAvailableInDate(equipmentId, DateUtils.SERVER_DATE_TIME_FORMAT.format(startTime), DateUtils.SERVER_DATE_TIME_FORMAT.format(endTime))){
                    Toast.makeText(this, getResources().getString(R.string.equipment_not_available_for_date), Toast.LENGTH_SHORT).show();
                    return;
                }
            }


            json.put(AbInBevConstants.EventFields.WITH_MUSIC_BAND, bandSwitch.isChecked());
            success = Event.updateEvent(currentEvent.getId(), json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SyncUtils.TriggerRefresh(EventEditActivity.this);
        Log.e("Pawel", "Event Updated!: " + success.toString());
        Toast.makeText(this, getResources().getString(R.string.event_updated), Toast.LENGTH_SHORT).show();
        finish();
    }

    @OnClick(R.id.event_start_hour)
    public void eventStartHourClicked () {
        setHour(AbInBevConstants.EventFields.START_DATE_TIME, currentEvent.getStartDateTime());
    }

    @OnClick(R.id.event_end_hour)
    public void eventEndHourClicked() {
        setHour(AbInBevConstants.EventFields.END_DATE_TIME, DateUtils.dateFromString(currentEvent.getEndDate()));
    }

}
