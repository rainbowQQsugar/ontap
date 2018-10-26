package com.abinbev.dsa.model;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.TaskFields;
import com.abinbev.dsa.utils.DateUtils;
import com.salesforce.androidsdk.accounts.UserAccountManager;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.utils.DSAConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nchangnon on 12/1/15.
 */
public class Task extends TranslatableSFBaseObject {

    public static final String TAG = Task.class.getName();
    public static final String PRIORITY_NORMAL="Normal";
    public static final String PRIORITY_HIGH="High";
    public static final String PRIORITY_LOW="Low";
    public static final String STATE_COMPLETED="Completed";
    public static final String STATE_WAIT_SOMEONE_ELSE="Waiting on someone else";
    public static final String STATE_DEFERRED="Deferred";
    public static final String STATE_CANCELADO="Cancelado";
    public static final String STATE_CANCELADAS="Canceladas por Vencimiento";
    public static final String STATUS_NOT_STARTED = "Not Started";
    public static final String STATUS_WAITING_SOMEONE = "Waiting on someone else";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_DEFERRED = "Deferred";
    private Account account;

    public Task(JSONObject json) {
        super(AbInBevObjects.TASK, json);
    }

    public Task() {
        super(AbInBevObjects.TASK);
    }

    public void setId(String id) {
        try {
            toJson().put(SyncEngineConstants.StdFields.ID, id);
        } catch (JSONException e) {
            Log.e(TAG, "Exception trying to set id", e);
        }
    }

    public String getSubject() {
        return getStringValueForKey(TaskFields.SUBJECT);
    }

    public String getTranslatedSubject() {
        return getTranslatedStringValueForKey(TaskFields.SUBJECT);
    }

    public String getDueDate() {
        return getStringValueForKey(TaskFields.ACTIVITY_DATE);
    }

    public String getOwnerName() {
        if (getOwner() == null) return null;
        else return getOwner().getName();
    }

    public String getOwnerProfile() {
        if (getOwner() == null) return null;
        else return getOwner().getProfile();
    }

    public User getOwner() {
        String ownerId = getStringValueForKey(TaskFields.OWNER_ID);
        return User.getUserByUserId(ownerId);
    }

    public Account getAccount() {
        if (account == null) {
            String accountId = getStringValueForKey(TaskFields.WHAT_ID);
            account = Account.getById(accountId);
        }

        return account;
    }

    public void invalidateAccount() {
        account = null;
    }

    public String getComment() {
        return getStringValueForKey(TaskFields.DESCRIPTION);
    }
    public String getTaskResult() {
        return getStringValueForKey(TaskFields.TASK_RESULT);
    }
    public void setTaskResult(String value){
        try {
            toJson().put(TaskFields.TASK_RESULT, value);
        } catch (JSONException e) {
            Log.e(TAG, "Exception trying to set TaskResult", e);
        }
    }
    public boolean getScheduled() {
        return getBooleanValueForKey(TaskFields.SCHEDULED);
    }

    public String getActivityDate() {
        return getStringValueForKey(TaskFields.ACTIVITY_DATE);
    }

    public String getResult() {
        return getStringValueForKey(TaskFields.RESULT);
    }

    public String getState() {
        return getStringValueForKey(TaskFields.STATUS);
    }

    public String getTranslatedState() {
        return getTranslatedStringValueForKey(TaskFields.STATUS);
    }

    public String getPriority() {
        return getTranslatedStringValueForKey(TaskFields.PRIORITY);
    }

    public void setSubject(String subject) {
        try {
            toJson().put(TaskFields.SUBJECT, subject);
        } catch (JSONException e) {
            Log.e(TAG, "Exception trying to set subject", e);
        }
    }

    public void setStatus(String status) {
        try {
            toJson().put(TaskFields.STATUS, status);
        } catch (JSONException e) {
            Log.e(TAG, "Exception trying to set status", e);
        }
    }

    public void setWhatId(String whatId) {
        try {
            toJson().put(TaskFields.WHAT_ID, whatId);
        } catch (JSONException e) {
            Log.e(TAG, "Exception trying to set WhatId", e);
        }
    }

    public void setOwnerId(String ownerId) {
        try {
            toJson().put(TaskFields.OWNER_ID, ownerId);
        } catch (JSONException e) {
            Log.e(TAG, "Exception trying to set OwnerId", e);
        }
    }

    public void setPriority(String priority) {
        try {
            toJson().put(TaskFields.PRIORITY, priority);
        } catch (JSONException e) {
            Log.e(TAG, "Exception trying to set priority", e);
        }
    }

    public void setActivityDate(String activityDate) {
        try {
            toJson().put(TaskFields.ACTIVITY_DATE, activityDate);
        } catch (JSONException e) {
            Log.e(TAG, "Exception trying to set activity date", e);
        }
    }

    public void setResultado(String resultado) {
        try {
            toJson().put(TaskFields.RESULT, resultado);
        } catch (JSONException e) {
            Log.e(TAG, "Exception trying to set result", e);
        }
    }

    public void setRecordTypeId(String recordTypeId) {
        try {
            toJson().put(TaskFields.RECORD_TYPE_ID, recordTypeId);
        } catch (JSONException e) {
            Log.e(TAG, "Exception trying to set recordTypeId", e);
        }
    }

    public void setJobEffecive(String jobEffective) {
        try {
            toJson().put(TaskFields.JOB_EFFECTIVE, jobEffective);
        } catch (JSONException e) {
            Log.e(TAG, "Exception trying to set job effecive", e);
        }
    }

    public void setComment(String comment) {
        try {
            toJson().put(TaskFields.COMMENT, comment);
        } catch (JSONException e) {
            Log.e(TAG, "Exception trying to set comment", e);
        }
    }

    public void setScheduledTask(boolean scheduledTask) {
        try {
            toJson().put(TaskFields.SCHEDULED, scheduledTask);
        } catch (JSONException e) {
            Log.e(TAG, "Exception trying to set scheduled task", e);
        }
    }

    public static int openTasksCountByAccountId(String accountId) {
        int count = 0;
        try {
            String smartSqlFilter = String.format("({%s:%s} = '%s' OR {%s:%s} = '%s' OR {%s:%s} = '%s' OR {%s:%s} = '%s') AND {%s:%s} = '%s'",
                    AbInBevObjects.TASK, TaskFields.STATUS, STATUS_NOT_STARTED,
                    AbInBevObjects.TASK, TaskFields.STATUS, STATUS_WAITING_SOMEONE,
                    AbInBevObjects.TASK, TaskFields.STATUS, STATUS_IN_PROGRESS,
                    AbInBevObjects.TASK, TaskFields.STATUS, STATUS_DEFERRED,
                    AbInBevObjects.TASK, TaskFields.WHAT_ID, accountId);

            String smartSql = String.format("SELECT count(Id) FROM {%1$s} WHERE %2$s", AbInBevObjects.TASK, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            count = recordsArray.getJSONArray(0).getInt(0);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting open Tasks by Account ID: " + accountId, e);
        }

        return count;
    }

    public static int openTasksCountByUserId(String userId) {
        int count = 0;
        try {
            String smartSqlFilter = String.format("({%s:%s} = '%s' OR {%s:%s} = '%s' OR {%s:%s} = '%s' OR {%s:%s} = '%s') AND {%s:%s} = '%s'",
                    AbInBevObjects.TASK, TaskFields.STATUS, STATUS_NOT_STARTED,
                    AbInBevObjects.TASK, TaskFields.STATUS, STATUS_WAITING_SOMEONE,
                    AbInBevObjects.TASK, TaskFields.STATUS, STATUS_IN_PROGRESS,
                    AbInBevObjects.TASK, TaskFields.STATUS, STATUS_DEFERRED,
                    AbInBevObjects.TASK, TaskFields.OWNER_ID, userId);

            String smartSql = String.format("SELECT count(Id) FROM {%1$s} WHERE %2$s", AbInBevObjects.TASK, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            count = recordsArray.getJSONArray(0).getInt(0);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting open Tasks by Owner ID: " + userId, e);
        }

        return count;
    }

    public static List<Task> TasksByAccountId(String accountId) {
        List<Task> results = new ArrayList<>();
        String smartSqlFilter = String.format("{%s:%s} = '%s' ORDER BY {%s:%s} ASC", AbInBevObjects.TASK,
                TaskFields.WHAT_ID, accountId,
                AbInBevObjects.TASK, TaskFields.ACTIVITY_DATE);
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.TASK, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        try {
            int recordsCount = recordsArray.length();
            for (int i = 0; i < recordsCount; i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                results.add(new Task(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting tasks", e);
        }

        return results;
    }

    public static List<Task> TasksByUserId(String userId) {
        List<Task> results = new ArrayList<>();
        String smartSqlFilter = String.format("{%s:%s} = '%s' ORDER BY {%s:%s} ASC", AbInBevObjects.TASK,
                TaskFields.OWNER_ID, userId,
                AbInBevObjects.TASK, TaskFields.ACTIVITY_DATE);
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.TASK, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        try {
            int recordsCount = recordsArray.length();
            for (int i = 0; i < recordsCount; i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                results.add(new Task(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting tasks", e);
        }

        return results;
    }

    public static Task getById(String id) {
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(AbInBevObjects.TASK, SyncEngineConstants.StdFields.ID, id);
        if (jsonObject != null) {
            return new Task(jsonObject);
        }
        return null;
    }

    public static JSONObject createJSONTask(String accountId, String recordTypeId) {
        if (accountId == null || TextUtils.isEmpty(accountId)) {
            Log.e(TAG, "Unable to create a task without an account.");
            return null;
        }
        String id = UserAccountManager.getInstance().getStoredUserId();
        if (TextUtils.isEmpty(id)) {
            Log.e(TAG, "Unable to create a task without a user id.");
            return null;
        }
        JSONObject json = new JSONObject();
        try {
            json.put(TaskFields.WHAT_ID, accountId);
            json.put(TaskFields.OWNER_ID, id);
            json.put(TaskFields.RECORD_TYPE_ID, recordTypeId);
            Calendar cal = Calendar.getInstance();
            String date = DateUtils.SERVER_DATE_FORMAT.format(cal.getTime());
            json.put(TaskFields.ACTIVITY_DATE, date);
            json.put(TaskFields.STATUS, TaskFields.STATUS_OPEN);
            json.put(TaskFields.PRIORITY, "2-Media");
        } catch (JSONException e) {
            Log.e(TAG, "Error creating new event", e);
            return null;
        }
        return json;
    }


    public static Task createDefaultTask(String accountId, String recordTypeId) {
        JSONObject jsonTask = Task.createJSONTask(accountId, recordTypeId);
        return new Task(jsonTask);
    }

    public static Task createTask(JSONObject taskJSON) {
        String tempId = DataManagerFactory.getDataManager().createRecord(AbInBevObjects.TASK, taskJSON);
        Task task = new Task(taskJSON);
        task.setId(tempId);

        return task;
    }

    public boolean updateTask() {
        JSONObject updatedObject = this.toJson();
        DataManager dataManager = DataManagerFactory.getDataManager();
        return dataManager.updateRecord(AbInBevObjects.TASK, this.getId(), updatedObject);
    }


}
