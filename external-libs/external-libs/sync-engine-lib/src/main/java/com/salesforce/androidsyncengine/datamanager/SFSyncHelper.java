package com.salesforce.androidsyncengine.datamanager;

import android.content.Context;
import android.util.Log;

import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.table.CloudTable;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsyncengine.syncmanifest.FilterObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by bduggirala on 1/3/16.
 */
public class SFSyncHelper {

    public void preSync(Context context, RestClient client) throws Exception {
    }

    public void postDataSync(Context context, RestClient client) {
    }

    public void postContentSync(Context context) {
    }

    public void postOptionalContentSync(Context context) { }

    /**
     * Method be called before group fetch
     * @param context
     * @param objectGroup
     * @param isAutoSync
     */
    public void doBeforeGroupFetch(Context context, List<String> objectGroup, boolean isAutoSync) {
    }

    public List<FilterObject> getAdditionalFilters(Context context, String objectName) {
        return new ArrayList<FilterObject>();
    }

    public List<String> getAdditionalQueryFilters(Context context, String objectName) {
        return new ArrayList<String>();
    }

    public List<String> getDynamicContentFetchQueries(String accountId) {
        return new ArrayList<String>();
    }

    public void postDynamicFetch(String fetchName, Map<String, String> params, Set<String> fetchedObjects) {

    }

    public CloudBlobContainer getAzureContainer() {
        return null;
    }

    public CloudTable getAzureCloudTable() {
        return null;
    }

    public static SFSyncHelper getSFSyncHelperInstance(Context context) {
        try {
            return (SFSyncHelper)Class.forName(context.getPackageName() + ".sync." + "CustomSyncHelper").newInstance();
        }
        catch (Exception e) {
            Log.i("SFSyncHelper", "no custom sync helper");
            return new SFSyncHelper();
        }
    }

    /**
     * Method be called after group fetch
     * @param context
     * @param objectGroup
     * @param isAutoSync
     */
    public void doAfterGroupFetch(Context context, List<String> objectGroup,  boolean isAutoSync) {
    }
}
