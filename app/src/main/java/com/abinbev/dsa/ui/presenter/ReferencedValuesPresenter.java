package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.utils.AppScheduler;
import com.salesforce.androidsdk.smartstore.store.IndexSpec;
import com.salesforce.androidsdk.smartstore.store.SmartStore;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants.StdFields;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Single;

import static android.text.TextUtils.isEmpty;

public class ReferencedValuesPresenter extends AbstractRxPresenter<ReferencedValuesPresenter.ViewModel> {

    private static final String TAG = ReferencedValuesPresenter.class.getSimpleName();

    private static final int MAX_PAGE_SIZE = 100;

    public interface ViewModel {
        void setItems(List<ReferencedValue> items);
    }

    private String objectName;

    private String fieldName;

    private String filter;

    public ReferencedValuesPresenter(String objectName, String fieldName, String filter) {
        super();
        this.objectName = objectName;
        this.fieldName = fieldName;
        this.filter = filter;
    }

    public void getItems(final String searchText) {
        addSubscription(Single.fromCallable(
                () -> getReferencedValues(searchText, objectName, fieldName, filter))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        items -> viewModel().setItems(items),
                        error -> Log.e(TAG, "Error getting referenced values: ", error)
                ));
    }

    private List<ReferencedValue> getReferencedValues(String searchText, String object, String fieldName, String filter) {
        //negotiation sell-in
        if (object.equals("Product__c")) {
            return getReferencedValuesFromSoup(searchText, object, fieldName, filter);
        } else {
            if (hasIndexOn(Arrays.asList(StdFields.ID, fieldName), object)) {
                return getReferencedValuesFromIndexedFields(searchText, object, fieldName, filter);
            } else {
                return getReferencedValuesFromSoup(searchText, object, fieldName, filter);
            }
        }
    }

    private List<ReferencedValue> getReferencedValuesFromIndexedFields(String searchText, String object, String fieldName, String filter) {
        List<ReferencedValue> result = new ArrayList<>();

        String smartSql;

        if (isEmpty(searchText)) {
            if (isEmpty(filter)) {
                smartSql = String.format("SELECT {%1$s:Id}, {%1$s:%2$s}FROM {%1$s} " +
                                "WHERE {%1$s:%2$s} IS NOT NULL ORDER BY {%1$s:%2$s} ASC",
                        object, fieldName);
            } else {
                smartSql = String.format("SELECT {%1$s:Id}, {%1$s:%2$s} FROM {%1$s} " +
                                "WHERE {%1$s:%2$s} IS NOT NULL AND (%3$s) ORDER BY {%1$s:%2$s} ASC",
                        object, fieldName, filter);
            }
        } else {
            if (isEmpty(filter)) {
                smartSql = String.format("SELECT {%1$s:Id}, {%1$s:%3$s} FROM {%1$s} " +
                                "WHERE {%1$s:%3$s} LIKE '%%%2$s%%' ORDER BY {%1$s:%3$s} ASC",
                        object, searchText, fieldName);
            } else {
                smartSql = String.format("SELECT {%1$s:Id}, {%1$s:%3$s} FROM {%1$s} " +
                                "WHERE {%1$s:%3$s} LIKE '%%%2$s%%' AND (%4$s) ORDER BY {%1$s:%3$s} ASC",
                        object, searchText, fieldName, filter);
            }
        }

        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchSmartSQLQuery(smartSql, 0, MAX_PAGE_SIZE);

        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONArray array = recordsArray.getJSONArray(i);
                result.add(new ReferencedValue(array.optString(0), array.optString(1)));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting referenced values", e);
        }
        return result;
    }

    private List<ReferencedValue> getReferencedValuesFromSoup(String searchText, String object, String fieldName, String filter) {
        List<ReferencedValue> result = new ArrayList<>();
        String smartSql = String.format("SELECT {%1$s:_soup} FROM {%1$s}", object);
        if (!isEmpty(filter)) {
            smartSql += " WHERE " + filter;
        }

        boolean isDone = false;
        int page = 0;

        while (!isDone) {
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchSmartSQLQuery(smartSql, page, MAX_PAGE_SIZE);

            if (recordsArray == null || recordsArray.length() == 0) {
                isDone = true;
            } else {
                try {
                    for (int i = 0; i < recordsArray.length(); i++) {
                        JSONObject soup = recordsArray.getJSONArray(i).getJSONObject(0);
                        String name = soup.getString(fieldName);

                        if (isEmpty(searchText) || (!isEmpty(name)
                                && StringUtils.containsIgnoreCase(name, searchText))) {
                            if (object.equals("Product__c")) {
                                boolean isCompetitor = soup.getBoolean("Competitor_Flag__c");
                                String text = "";
                                if (isCompetitor) {
                                    text = ABInBevApp.getAppContext().getResources().getString(R.string.competitor_flag_c);
                                    result.add(new ReferencedValue(soup.getString(StdFields.ID), text + name));
                                } else {
                                    text = ABInBevApp.getAppContext().getResources().getString(R.string.competitor_flag_ABI);
                                    result.add(new ReferencedValue(soup.getString(StdFields.ID), text + name));
                                }
                            } else {
                                result.add(new ReferencedValue(soup.getString(StdFields.ID), name));
                            }
                        }
                    }

                    if (result.size() >= MAX_PAGE_SIZE) {
                        isDone = true;
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Exception in getting referenced values", e);
                    isDone = true;
                }
            }

            page++;
        }

        return result;
    }

    private static boolean hasIndexOn(List<String> fields, String objectName) {
        SmartStore smartStore = DataManagerFactory.getDataManager().getSmartStore();
        IndexSpec[] indexSpecs = smartStore.getSoupIndexSpecs(objectName);

        if (indexSpecs == null || indexSpecs.length == 0) {
            return false;
        }

        for (String field : fields) {
            if (!hasIndexOn(indexSpecs, field)) {
                Log.w(TAG, "Object: " + objectName + " doesn't have index on field: " + field);
                return false;
            }
        }

        return true;
    }

    private static boolean hasIndexOn(IndexSpec[] indexSpecs, String fieldName) {
        if (indexSpecs == null || indexSpecs.length == 0) {
            return false;
        }

        if (isEmpty(fieldName)) {
            return false;
        }

        for (IndexSpec spec : indexSpecs) {
            if (fieldName.equals(spec.path)) return true;
        }

        return false;
    }

    public static class ReferencedValue {
        String id;
        String name;

        public ReferencedValue(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }


        @Override
        public String toString() {
            return "ReferencedValue{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
