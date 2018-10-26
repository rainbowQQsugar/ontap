package com.salesforce.dsa.async;

import android.os.AsyncTask;
import android.util.Log;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ModelDataFetcherTask<T extends SFBaseObject> extends AsyncTask<Void, Void, List<T>> {

    private static final String TAG = "ModelDataFetcherTask";

    private Class<T> type;
    private ModelDataFetcherCb<T> cb;
    private String smartSql;
    private boolean useSmartSql = false;


    public ModelDataFetcherTask(Class<T> type, ModelDataFetcherCb<T> cb) {
        this(type, null, cb);
    }

    public ModelDataFetcherTask(Class<T> type, String smartSqlFilter, ModelDataFetcherCb<T> cb) {
        this.type = type;
        this.cb = cb;

        // if we have a filter specified then make sure we use smart sql
        if (smartSqlFilter != null && !smartSqlFilter.isEmpty()) {
            this.smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, type.getSimpleName(), smartSqlFilter);
            useSmartSql = true;
        }
    }

    @Override
    protected List<T> doInBackground(Void... params) {
        // list to hold data
        List<T> list = new ArrayList<>();

        JSONArray recordsArray;
        if (!useSmartSql) {
            // get the data from data manager using the class name
            recordsArray = DataManagerFactory.getDataManager().fetchAllRecords(type.getSimpleName(), SyncEngineConstants.StdFields.ID);
        } else {
            // get the data from data manager using the smartsql
            recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        }

        // convert the json objects into model pojos
        for (int i = 0; i < recordsArray.length(); i++) {
            try {
                JSONObject jsonObject = !useSmartSql ? recordsArray.getJSONObject(i) : recordsArray.getJSONArray(i).getJSONObject(0);
                T obj = type.getConstructor(JSONObject.class).newInstance(jsonObject);
                list.add(obj);
            } catch (Exception e) {
                Log.e(TAG, "Exception in records array parsing", e);
                e.printStackTrace();
            }
        }

        return list;
    }

    @Override
    protected void onPostExecute(List<T> result) {
        cb.onData(result);
    }

    public interface ModelDataFetcherCb<T> {
        void onData(List<T> data);
    }
}
