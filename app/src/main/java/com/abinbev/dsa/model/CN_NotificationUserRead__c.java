package com.abinbev.dsa.model;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.DateUtils;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

/**
 * User Message Record Class
 */
public class CN_NotificationUserRead__c extends SFBaseObject {

    private static String TAG = "CN_NotificationUserRead__c.class";

    //thirty day
    private static long THIRTH_DAYS = 30;

    public CN_NotificationUserRead__c(String objectName, JSONObject json) {
        super(AbInBevConstants.AbInBevObjects.CN_NotificationUserRead, json);
    }

    protected CN_NotificationUserRead__c(String objectName) {
        super(AbInBevConstants.AbInBevObjects.CN_NotificationUserRead);
    }

    protected CN_NotificationUserRead__c(JSONObject jsonObject) {
        super(AbInBevConstants.AbInBevObjects.CN_NotificationUserRead, jsonObject);

    }

    /**
     * set Id
     *
     * @param Id
     */
    public void setId(String Id) {
        setStringValueForKey(AbInBevConstants.CNNotificationUserReadFields.Id, Id);
    }

    /**
     * get Id
     *
     * @return
     */
    public String getId() {
        return getStringValueForKey(AbInBevConstants.CNNotificationUserReadFields.Id);
    }

    /**
     * set CN_Notification_Message__c
     *
     * @param CN_Notification_Message__c
     */
    public void setNotificationMessage(String CN_Notification_Message__c) {
        setStringValueForKey(AbInBevConstants.CNNotificationUserReadFields.CN_Notification_Message__c, CN_Notification_Message__c);
    }

    /**
     * get CN_Notification_Message__c
     *
     * @return
     */
    public String getNotificationMessage() {
        return getStringValueForKey(AbInBevConstants.CNNotificationUserReadFields.CN_Notification_Message__c);
    }

    /**
     * set User__c
     *
     * @param User__c
     */
    public void setNotificationUser(String User__c) {
        setStringValueForKey(AbInBevConstants.CNNotificationUserReadFields.User__c, User__c);
    }

    /**
     * get User__c
     *
     * @return
     */
    public String getNotificationUser() {
        return getStringValueForKey(AbInBevConstants.CNNotificationUserReadFields.User__c);
    }

    public synchronized static void getAll() {

        try {

            String smartSql = String.format("SELECT * FROM {%1$s}", AbInBevConstants.AbInBevObjects.CN_NotificationUserRead);

            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

            if (recordsArray != null && recordsArray.length() > 0) {

                for (int i = 0; i < recordsArray.length(); i++) {

                    JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(1);

                    String CN_Invalid_Date__c = jsonObject.optString("CN_Invalid_Date__c");
                    if (TextUtils.isEmpty(CN_Invalid_Date__c) || "null".equalsIgnoreCase(CN_Invalid_Date__c))
                        continue;

                    String Id = jsonObject.optString("Id");

                    Date currentDate = new Date();
                    //Message Time
                    Date messageDate = DateUtils.dateFromStringShortCN(CN_Invalid_Date__c);

                    int days = DateUtils.getGapCount(currentDate, messageDate);

                    if (days > THIRTH_DAYS) {
                        deleteDataByMessageID(Id);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getAll Error:" + e.getMessage());
        }

    }

    /**
     * Query whether this data exists in this table under the condition of messageID, and if so, the user has read this message
     *
     * @param messageID
     * @return
     */
    public static CN_NotificationUserRead__c isExistsRecord(String messageID) {

        try {

            String sqlFilter = String.format("{%s:%s} = '%s'", AbInBevConstants.AbInBevObjects.CN_NotificationUserRead, AbInBevConstants.CNNotificationUserReadFields.CN_Notification_Message__c, messageID);
            String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevConstants.AbInBevObjects.CN_NotificationUserRead, sqlFilter);

            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            if (recordsArray != null && recordsArray.length() > 0) {
                JSONObject jsonObject = recordsArray.getJSONArray(0).getJSONObject(0);

                if (jsonObject == null)
                    return null;
                return new CN_NotificationUserRead__c(jsonObject);
            }

        } catch (Exception e) {
            Log.e(TAG, "isExistsRecord Error:" + e.getMessage());
        }
        return null;
    }

    /**
     * Delete the corresponding message data according to the message ID
     *
     * @param messageID
     * @return
     */
    public static boolean deleteDataByMessageID(String messageID) {

        boolean isDelete = false;

        try {

            isDelete = DataManagerFactory.getDataManager().deleteRecord(AbInBevConstants.AbInBevObjects.CN_NotificationUserRead, messageID);

        } catch (Exception e) {
            Log.e(TAG, "deleteDataByMessageID Error;" + e.getMessage());
        }

        return isDelete;
    }


    /**
     * Now query the existence of records according to the ID, and insert if not
     *
     * @param messageID
     * @param userId
     * @return
     */
    public static boolean saveUserMessage(String messageID, String userId) {

        boolean isSuccess = false;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(AbInBevConstants.CNNotificationUserReadFields.CN_Notification_Message__c, messageID);
            jsonObject.put(AbInBevConstants.CNNotificationUserReadFields.User__c, userId);

            CN_NotificationUserRead__c read__c1 = new CN_NotificationUserRead__c(jsonObject);
            String result = read__c1.createRecord(DataManagerFactory.getDataManager());
            if (!TextUtils.isEmpty(result)) {
                isSuccess = true;
            }

        } catch (Exception e) {
            Log.e(TAG, "saveUserMessage Error:" + e.getMessage());
        }

        return isSuccess;
    }

    public String createRecord(DataManager dm) {
        String recordId = dm.createRecord(AbInBevConstants.AbInBevObjects.CN_NotificationUserRead, toJson());
        Log.e("CN_DSA_Azure_", "createRecord: " + recordId);
        setStringValueForKey(AbInBevConstants.PictureAuditStatusFields.ID, recordId);
        return recordId;
    }


}
