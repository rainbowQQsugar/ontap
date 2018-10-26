package com.salesforce.dsa.app.ui.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.utils.DateUtils;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import com.salesforce.androidsyncengine.utils.Guava.Joiner;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.DSAAppState;
import com.salesforce.dsa.app.R;
import com.salesforce.dsa.app.ui.adapter.CheckoutReviewListAdapter;
import com.salesforce.dsa.app.utils.ContentUtils;
import com.salesforce.dsa.data.model.Contact;
import com.salesforce.dsa.data.model.ContentVersion;
import com.salesforce.dsa.data.model.TrackedDocument;
import com.salesforce.dsa.location.LocationHandler;
import com.salesforce.dsa.location.LocationHandlerProvider;
import com.salesforce.dsa.location.LocationReceiver;
import com.salesforce.dsa.utils.DSAConstants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONObject;

public class CheckoutReviewActivity extends Activity implements LocationReceiver {

    private static final String TAG = "CheckoutReviewActivity";

    private static final Joiner JOINER = Joiner.on(",");
    private static final SimpleDateFormat soqlQueryDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    //	Define a request code to send to Google Play services
    //	This code is returned in Activity.onActivityResult
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final java.lang.String INTENT_EXTRA_ACCOUNT_ID = "AccountId";
    private LocationHandler locationHandler;

    private List<TrackedDocument> trackedDocuments;
    private Button doneButton;
    private EditText editText;
    private String accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.checkout_review_list);

        doneButton = (Button) findViewById(R.id.doneButton);

        editText = (EditText) findViewById(R.id.checkoutNotes);

        locationHandler = LocationHandlerProvider.createLocationHandler(this);

        trackedDocuments = DSAAppState.getInstance().getTrackedDocuments();
        ListView listView = (ListView) findViewById(R.id.documentsList);
        CheckoutReviewListAdapter adapter = new CheckoutReviewListAdapter(trackedDocuments);
        listView.setAdapter(adapter);

        // register checkout receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.salesforce.dsa.DocumentStoppedViewing");
        registerReceiver(checkoutReceiver, intentFilter);

        accountId = getIntent().getExtras().getString(INTENT_EXTRA_ACCOUNT_ID);
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationHandler.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DSAAppState.DocumentTrackingType trackingType = DSAAppState.getInstance().getTrackingType();

        if (trackingType == DSAAppState.DocumentTrackingType.DocumentTracking_DeferredContact) {
            doneButton.setText("Choose Contact");
        } else {
            doneButton.setText("Done");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationHandler.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(checkoutReceiver);
    }

    public void createReviewObjects() {

        DSAAppState.getInstance().setCheckInEnd(new Date());

        // get tracked documents
        List<TrackedDocument> trackedDocs = DSAAppState.getInstance().getTrackedDocuments();
        //TODO: save mailed contact ID's and get them if there is no current tracking contact
        // get current tracking contact
        Contact contact = DSAAppState.getInstance().getCurrentTrackingContact();
        DataManager dataManager = DataManagerFactory.getDataManager();
        List<String> titleList = new ArrayList<>();
        LatLng currentLatLng = locationHandler.getCurrentLatLng();

        // for each tracked document, create "DocumentReview" object and set relative properties
        for (TrackedDocument trackedDocument : trackedDocs) {
            ContentVersion contentVersion = trackedDocument.getContentVersion();
            titleList.add(contentVersion.getTitle());
            JSONObject json = new JSONObject();
            try {
                json.put(ManifestUtils.getNamespaceSupportedFieldName("ContentReview__c", "ContentTitle__c", this), contentVersion.getTitle());
                if (contact != null)json.put(ManifestUtils.getNamespaceSupportedFieldName("ContentReview__c", "ContactId__c", this), contact.getId());
                json.put(ManifestUtils.getNamespaceSupportedFieldName("ContentReview__c", "Rating__c", this), trackedDocument.getRating());
                json.put(ManifestUtils.getNamespaceSupportedFieldName("ContentReview__c", "ContentId__c", this), contentVersion.getId());
                json.put(ManifestUtils.getNamespaceSupportedFieldName("ContentReview__c", "Document_Emailed__c", this), trackedDocument.isMarkedToEmail());
                // Geolocation fields conflict with namespace prefix so not using the namespace is the cleanest possible way
                // right now to deal with it. The namespace will get appended to this field when we upload the queue
                if (currentLatLng != null)json.put("Geolocation__Latitude__s", currentLatLng.latitude);
                if (currentLatLng != null)json.put("Geolocation__Longitude__s", currentLatLng.longitude);

                // in edge or error cases, showing zero is better than showing a big negative number ...
                long totalSecondsViewed;
                if (trackedDocument.getEndTimeMillis() == 0) {
                    totalSecondsViewed = 0;
                } else {
                    totalSecondsViewed = (trackedDocument.getEndTimeMillis() - trackedDocument.getStartTimeInSecs()) / 1000;
                }
                json.put(ManifestUtils.getNamespaceSupportedFieldName("ContentReview__c", "TimeViewed__c", this), totalSecondsViewed);
            } catch (Exception e) {
                LOG.e(TAG, "Exception: ", e);
            }
            dataManager.createRecord("ContentReview__c", json);
        }

        // create event/ activity record
//        if (contact != null) {
        JSONObject json = new JSONObject();
        Date checkInStart = DSAAppState.getInstance().getCheckInStart();
        Date checkInEnd = DSAAppState.getInstance().getCheckInEnd();
        String description = String.format("Presented the following documents using DSA application: %s", JOINER.join(titleList));
        String checkoutNotes = editText.getText().toString();
        if (checkoutNotes.length() > 0) {
            description = String.format("%s; Notes: %s", description, checkoutNotes);
        }
//           long durationInMinutes = (checkInEnd.getTime() - checkInStart.getTime()) / 1000 / 60;
//            final String date = DateUtils.SERVER_DATE_FORMAT.format(Calendar.getInstance().getTime());
//            Event event = Event.getEventForParams(date, accountId, VisitState.open.getState());
        Event event = null;
        String eventId = getIntent().getExtras().getString("EventId");
        String smartSqlFilter = String.format("{Event:%s} = '%s'", SyncEngineConstants.StdFields.ID, eventId);
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, "Event", smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        if (recordsArray.length() != 1) {
            Log.e(TAG, "got a length other than 1 in fetching MobileAppConfigs. length is: " + recordsArray.length());
        }
        try {
            JSONObject jsonObject = recordsArray.getJSONArray(0).getJSONObject(0);
            event = new Event(jsonObject);
        } catch (Exception e) {
        }

        String dateTime = DateUtils.SERVER_DATE_TIME_FORMAT.format(Calendar.getInstance().getTime());
        JSONObject updatedEventJson = event.setCheckOutWithParams(dateTime);

        try {
            updatedEventJson.put("Subject", "DSA Presentation");
            if (contact != null) updatedEventJson.put("WhoId", contact.getId());
            updatedEventJson.put("WhatId", accountId);
            updatedEventJson.put("Description", description);
//               json.put("StartDateTime", soqlQueryDateFormat.format(checkInStart));
//               json.put("EndDateTime", soqlQueryDateFormat.format(checkInEnd));
//               updatedEventJson.put("DurationInMinutes", durationInMinutes);
            updatedEventJson.put("ActivityDateTime", soqlQueryDateFormat.format(checkInStart));
            updatedEventJson.put("Location", getCompleteAddressString(currentLatLng.latitude, currentLatLng.longitude));
        } catch (Exception e) {
            LOG.e(TAG, "Exception: ", e);
        }

        Event.updateEvent(event.getId(), updatedEventJson);
//        }
        // push changes to server
        SyncUtils.TriggerRefresh(this);
    }

    private void emailDocuments() {
        ArrayList<Uri> uris = new ArrayList<Uri>();

        final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);

        emailIntent.setType(getResources().getString(R.string.email_type));

        Contact contact = DSAAppState.getInstance().getCurrentTrackingContact();
        String email = contact != null ? contact.getEmail() : "";
        // Add recipient
        emailIntent.putExtra(Intent.EXTRA_EMAIL,
                new String[]{email});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.email_subject));
        emailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.email_body));

        for (TrackedDocument td : trackedDocuments) {
            if (td.isMarkedToEmail()) {
                Uri uri = Uri.parse("content://" + getResources().getString(R.string.provider)
                        + "/" + td.getContentVersion().getFilePath(this)
                        + "/" + td.getContentVersion().getTitle()
                        + "." + ContentUtils.getExtension(td.getContentVersion().getFileType()));
                uris.add(uri);
            }
        }
        if (uris.size() > 0) {
            emailIntent.putExtra(Intent.EXTRA_STREAM, uris);
            startActivity(emailIntent);
        }
    }

    @Override
    public Activity getReceivingActivity() {
        return this;
    }

    @Override
    public int getFailureResolutionRequestCode() {
        return CONNECTION_FAILURE_RESOLUTION_REQUEST;
    }

    @Override
    public void handleUnresolvedError(int errorCode) {
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

        String errorMessageString = GooglePlayServicesUtil.getErrorString(errorCode);
        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            // Show the error dialog in the DialogFragment
            errorDialog.show();
        }
        Toast.makeText(this, errorMessageString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNewLocationReceived(Location location) {

    }

    private String getCompleteAddressString(double latitude, double longitude) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(",");
                }
                strAdd = strReturnedAddress.toString();
                Log.d(TAG, "Address: " + strAdd);
            } else {
                Log.e(TAG, "No Address returned!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Canont get Address! ", e);
        }
        return strAdd;
    }

    private BroadcastReceiver checkoutReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("com.salesforce.dsa.DocumentStoppedViewing")) {
                createReviewObjects();
            }
        }
    };

    @Override
    public void onConnected() {

    }
}
