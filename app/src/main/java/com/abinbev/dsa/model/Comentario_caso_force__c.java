package com.abinbev.dsa.model;


import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.CaseCommentFields;
import com.abinbev.dsa.utils.DateUtils;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Comentario_caso_force__c extends SFBaseObject {
    public static final String TAG = Comentario_caso_force__c.class.getSimpleName();

    public Comentario_caso_force__c(JSONObject json) {
        super(AbInBevObjects.CASE_COMMENTS, json);
    }

    protected Comentario_caso_force__c() {
        super(AbInBevObjects.CASE_COMMENTS);
    }

    public String getComment() {
        return getStringValueForKey(CaseCommentFields.COMMENT);
    }

    public String getSoupLastModified() {
        return getStringValueForKey("_soupLastModifiedDate");
    }

    public static Comentario_caso_force__c createComment(String comment, String caseId, String userId) {
        if (TextUtils.isEmpty(comment)) {
            Log.e(TAG, "Unable to create a comment without a comment body.");
            return null;
        }

        if (TextUtils.isEmpty(caseId)) {
            Log.e(TAG, "Unable to create a comment without a case id.");
            return null;
        }

//        if(TextUtils.isEmpty(userId)) {
//            Log.e(TAG, "Unable to create an order without a user id.");
//            return null;
//        }

        DataManager dataManager = DataManagerFactory.getDataManager();
        JSONObject json = new JSONObject();
        try {
//            json.put(SyncEngineConstants.StdFields.OWNERID, userId);
            json.put(CaseCommentFields.COMMENT, comment);
            json.put(CaseCommentFields.CASE_FORCE, caseId);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating new comment", e);
            return null;
        }

        String tempId = dataManager.createRecord(AbInBevObjects.CASE_COMMENTS, json);
        return new Comentario_caso_force__c(dataManager.exactQuery(AbInBevObjects.CASE_COMMENTS, SyncEngineConstants.StdFields.ID, tempId));
    }

    public static List<Comentario_caso_force__c> getTwoNewestCommentsForCase(String caseId) {

        String smartSqlFilter = String.format("{%s:%s} = '%s'",
                AbInBevObjects.CASE_COMMENTS, CaseCommentFields.CASE_FORCE, caseId);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.CASE_COMMENTS, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

        List<Comentario_caso_force__c> results = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                results.add(new Comentario_caso_force__c(jsonObject));
            }

            Collections.sort(results, new Comparator<Comentario_caso_force__c>() {

                @Override
                //createdDate, nulls first, then soupLastModifiedDate descending order
                public int compare(Comentario_caso_force__c lhs, Comentario_caso_force__c rhs) {

                    if ((lhs.getCreatedDate() == null || TextUtils.isEmpty(lhs.getCreatedDate()))
                            && (rhs.getCreatedDate() == null || TextUtils.isEmpty(rhs.getCreatedDate()))) {
                        Date lhsDate = new Date(Long.valueOf(lhs.getSoupLastModified()));
                        Date rhsDate = new Date(Long.valueOf(rhs.getSoupLastModified()));
                        return rhsDate.compareTo(lhsDate); //desc order
                    }

                    if (lhs.getCreatedDate() == null || TextUtils.isEmpty(lhs.getCreatedDate())) {
                        return -1;
                    }

                    if (rhs.getCreatedDate() == null || TextUtils.isEmpty(rhs.getCreatedDate())) {
                        return 1;
                    }

                    try {
                        Date lhsDate = DateUtils.SERVER_DATE_TIME_FORMAT.parse(lhs.getCreatedDate());
                        Date rhsDate = DateUtils.SERVER_DATE_TIME_FORMAT.parse(rhs.getCreatedDate());
                        return rhsDate.compareTo(lhsDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    return 0;
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Exception in getting two newest Case_Force_Comment__c records for case: " + caseId, e);
        }

        if (results.size() > 2) {
            return results.subList(0, 2);
        } else {
            return results;
        }
    }

}
