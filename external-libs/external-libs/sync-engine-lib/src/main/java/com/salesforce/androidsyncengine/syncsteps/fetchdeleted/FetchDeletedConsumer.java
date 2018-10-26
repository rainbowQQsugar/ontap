package com.salesforce.androidsyncengine.syncsteps.fetchdeleted;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.salesforce.androidsyncengine.datamanager.SyncHelper;
import com.salesforce.androidsyncengine.datamanager.exceptions.RuntimeSyncException;
import com.salesforce.androidsyncengine.datamanager.exceptions.UnsupportedEncodingSyncException;
import com.salesforce.androidsyncengine.datamanager.synchelper.Subrequest;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import com.salesforce.androidsyncengine.syncsteps.SyncControls;
import com.salesforce.androidsyncengine.syncsteps.common.SendBatchRequestsAction;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * Class that receives stream of object names, converts them to requests and depending on the response
 * deletes local records.
 *
 * Created by Jakub Stefanowski on 09.10.2017.
 */
public class FetchDeletedConsumer implements Observable.Transformer<String, Boolean> {

    private static final String TAG = FetchDeletedConsumer.class.getSimpleName();

    private static final SimpleDateFormat soqlQueryDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private static final SimpleDateFormat deleteDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00", Locale.US);

    static {
        soqlQueryDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        deleteDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final Context context;

    private final SyncHelper syncHelper;

    private final SyncControls syncControls;

    private final String apiVersion;

    public FetchDeletedConsumer(SyncControls syncControls) {
        this.syncControls = syncControls;
        this.context = syncControls.getContext();
        this.syncHelper = syncControls.getSyncHelper();
        this.apiVersion = syncControls.getApiVersion();
    }

    @Override
    public Observable<Boolean> call(Observable<String> objectNameStream) {
        return objectNameStream

                // Convert object names to subrequests.
                .map(objectName -> {
                    try {
                        boolean containsContentFiles = PreferenceUtils.containsBinaryField(objectName, context);
                        Subrequest subrequest = createFetchDeletedSubrequest(objectName);
                        subrequest.setResponseHandler(new DeleteRecordsResponseHandler(objectName, containsContentFiles, syncControls));
                        return subrequest;
                    }
                    catch (UnsupportedEncodingSyncException e) {
                        throw RuntimeSyncException.wrap(e);
                    }
                })

                // Create list.
                .toList()

                // Take only first element (just to be sure).
                .first()

                // Send collection of subrequests.
                .doOnNext(new SendBatchRequestsAction(syncHelper, true /* halt on error*/))

                // Return true.
                .flatMap(requests -> Observable.just(true));
    }

    private Subrequest createFetchDeletedSubrequest(String objectName) throws UnsupportedEncodingSyncException {

        long currentTime = System.currentTimeMillis();
        String lastRefreshTime = PreferenceUtils.getLastRefreshTime(objectName, context);
        Date startDate = calculateRequestStartDate(objectName, lastRefreshTime, currentTime);

        String startTimeString = urlEncode(deleteDateFormat.format(startDate), "UTF-8");
        String endTimeString = urlEncode(deleteDateFormat.format(new Date(currentTime)), "UTF-8");
        String namespacedObject = ManifestUtils.getNamespaceSupportedObjectName(objectName, context);

        String path = String.format("/services/data/%s/sobjects/%s/deleted?start=%s&end=%s",
                apiVersion, namespacedObject, startTimeString, endTimeString);

        Log.d(TAG, "getDeleted path is: " + path);

        return syncHelper.getRawSubrequest(path);
    }

    private Date calculateRequestStartDate(String objectName, String lastRefreshTime, long currentTime) {
        Date startDate = new Date(currentTime - TimeUnit.DAYS.toMillis(29)); // Cannot take older than 30 days.

        // if this time stamp is null or empty then use the last refresh time of the object as the
        // start time end time will always be now
        String lastFetchDeletedTime = PreferenceUtils.getLastDeletedTime(objectName, context);
        if (TextUtils.isEmpty(lastFetchDeletedTime)) {
            lastFetchDeletedTime = lastRefreshTime;
        }

        if (!TextUtils.isEmpty(lastFetchDeletedTime)) {
            try {
                Date lastFetchDeletedDate = soqlQueryDateFormat.parse(lastFetchDeletedTime);

                // If last fetch was done later than 29 days ago use it as a start date.
                if (lastFetchDeletedDate.after(startDate)) {
                    startDate = lastFetchDeletedDate;

                    // StartDate must be at least one minute greater than endDate. If not the server
                    // returns an error.
                    if (currentTime - startDate.getTime() < 60000) {
                        startDate = new Date(currentTime - 60000); // subtract a minute
                    }
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error while parsing date for fetch deleted records.", e);
            }
        }

        return startDate;
    }

    private static String urlEncode(String s, String charsetName) throws UnsupportedEncodingSyncException {
        try {
            return URLEncoder.encode(s, charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedEncodingSyncException(e, "Charset: " + charsetName + " is not supported.");
        }
    }
}
