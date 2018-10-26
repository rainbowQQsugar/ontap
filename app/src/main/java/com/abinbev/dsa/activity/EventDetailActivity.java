package com.abinbev.dsa.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.Event_Catalog__c;
import com.abinbev.dsa.model.Event_Equipment__c;
import com.abinbev.dsa.ui.presenter.EventOperationPresenter;
import com.abinbev.dsa.utils.AbInBevConstants.VisitTypes;
import com.abinbev.dsa.utils.DateUtils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.Bind;
import butterknife.OnClick;

public class EventDetailActivity extends AppBaseActivity implements EventOperationPresenter.ViewModel {

    public static final String EVENT_ID_EXTRA = "event_id";
    private String eventId = "";
    private EventOperationPresenter eventOperationPresenter;
    private Event currentEvent;

    @Bind(R.id.event_details_image)
    ImageView eventDetailsImage;

    @Bind(R.id.event_name)
    TextView eventName;

    @Bind(R.id.event_date)
    TextView eventDate;

    @Bind(R.id.event_hour)
    TextView eventHour;

    @Bind(R.id.event_owner_name)
    TextView eventOwnerName;

    @Bind(R.id.account_phone)
    TextView accountPhone;

    @Bind(R.id.phone_icon)
    View phoneIcon;

    @Bind(R.id.event_accessories)
    TextView eventAccessories;

    @Bind(R.id.edit_event_button)
    FloatingActionButton editEventButton;

    @Bind(R.id.equipment_container)
    RelativeLayout equipmentContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.event_planning_header));

        if (getIntent() != null) {
            eventId = getIntent().getStringExtra(EventDetailActivity.EVENT_ID_EXTRA);

            eventOperationPresenter = new EventOperationPresenter(eventId);
            eventOperationPresenter.setViewModel(this);
            eventOperationPresenter.start();
        }
        editEventButton.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        eventOperationPresenter.start();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_event_detail;
    }

    @Override
    public void setEvent(Event event) {
        getSupportActionBar().setSubtitle(event.getAccount().getName());

        currentEvent = event;
        eventName.setText(event.getSubject());
        if(!event.getEndDate().isEmpty() && event.getEndDate() != null) {
            eventDate.setText(DateUtils.formatDateTimeEvents(event.getEndDate()));
            if(event.getStartDateTime() != null) {
                String toDateTime = DateUtils.dateToDateTime(event.getStartDateTime());
                String startHour = DateUtils.formatDateHoursMinutesAMPM(toDateTime);
                String endHour = DateUtils.formatDateHoursMinutesAMPM(event.getEndDate());
                eventHour.setText(startHour + " - " + endHour);
            }
        }

        if (!VisitTypes.OUT_OF_PLAN.equals(event.getVisitType())) {
            editEventButton.setVisibility(View.GONE);
        }
        else {
            editEventButton.setVisibility(View.VISIBLE);
        }


        eventOwnerName.setText(event.getAccount().getName());

        String phone = event.getAccount().getFirstAvailablePhone();
        if (TextUtils.isEmpty(phone)) {
            accountPhone.setVisibility(View.GONE);
            phoneIcon.setVisibility(View.GONE);
        }
        else {
            accountPhone.setVisibility(View.VISIBLE);
            phoneIcon.setVisibility(View.VISIBLE);
            accountPhone.setText(phone);
        }

        setAccountPhoto(Attachment.getAccountPhotoAttachment(event.getAccountId()), event.getAccountId());
    }

    @Override
    public void setEventCatalogRecord(Event_Catalog__c eventCatalogRecord) {
        if (eventCatalogRecord == null) {
            equipmentContainer.setVisibility(View.GONE);
        }
        else {
            equipmentContainer.setVisibility(eventCatalogRecord.getUsageOfEquipment() ? View.VISIBLE : View.GONE);
            if (!currentEvent.getEquipment().isEmpty() && currentEvent.getEquipment() != null) {
                Event_Equipment__c equipment = Event_Equipment__c.getById(currentEvent.getEquipment());
                eventAccessories.setText(equipment.getName());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eventOperationPresenter.stop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setAccountPhoto(Attachment attachment, String accountId) {
        if (attachment != null) {
            Uri imageUri = Uri.fromFile(new File(attachment.getFilePath(this, accountId)));
            Picasso.with(this)
                    .load(imageUri)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .placeholder(R.drawable.default_account_background)
                    .error(R.drawable.default_account_background)
                    .into(eventDetailsImage);
        }
    }

    @OnClick(R.id.edit_event_button)
    public void eventEditButtonClicked () {
        Intent intent = new Intent(this, EventEditActivity.class);
        intent.putExtra(EventEditActivity.EVENT_ID_EXTRA, eventId);
        startActivity(intent);
    }

}
