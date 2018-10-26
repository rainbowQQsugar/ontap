package com.salesforce.dsa.app.sync;

import android.os.AsyncTask;
import android.util.Log;

import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.Guava.Joiner;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CheckAccessFilePermissionTask<T> extends AsyncTask<String, Void, List<T>> {
    private Class<T> type;
    private OnCheckedCallBack<T> onCheckedCallBack;
    private String TAG = getClass().getSimpleName();

    public CheckAccessFilePermissionTask(OnCheckedCallBack<T> onCheckedCallBack, Class<T> type) {
        this.onCheckedCallBack = onCheckedCallBack;
        this.type = type;
    }

    @Override
    protected List<T> doInBackground(String... strings) {
        if (strings.length < 1) return null;
        List<T> list = new ArrayList<>();
        List ids = new ArrayList<>();
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(strings[0]);
        for (int i = 0; i < recordsArray.length(); i++) {
            try {
                String id = recordsArray.getJSONArray(i).getString(0);
                ids.add(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String filter = String.format("select {%1$s:_soup} from {%1$s} where {%1$s:Id} IN ('%2$s')",
                type.getSimpleName(), Joiner.on("','").join(ids));
        JSONArray array = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(filter);
//        Log.e(TAG, "doInBackground:1" + array.toString());
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject jsonObject = array.getJSONArray(i).getJSONObject(0);
                T obj = type.getConstructor(JSONObject.class).newInstance(jsonObject);
                list.add(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    protected void onPostExecute(List<T> jsonArray) {
        if (onCheckedCallBack != null) onCheckedCallBack.onCheckedCallBack(jsonArray);
    }

    public interface OnCheckedCallBack<T> {
        void onCheckedCallBack(List<T> jsonArray);
    }
}
