package com.abinbev.dsa.model;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.NotifyMsgAdapter;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.DateUtils;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Notification Message
 */
public class CN_Notification_Message__c extends SFBaseObject {



    private static String TAG = "com.abinbev.dsa.model.CN_Notification_Message__c.class";
    private static String New_Task = "New Task";
    private static String System_Notification = "System Notification";
    private static String Contract_Renewal = "Contract Renewal";
    private static String jsonFormat = "{\"Id\": \"\",\"Name\": \"\",\"CN_Category__c\": \"\",\"CN_Description__c\": \"\",\"CN_Due_Date__c\": \"\",\t\"CN_Notify_Time__c\": \"\",\"CN_Related_ID__c\": \"\",\"CN_Title__c\": \"\",\"CN_Visibility_Type__c\": \"\",\"IsRead\": -1,\"OwnerId\": \"\",\"TypeId\": -1,\"ItemId\": -1,\"CN_Invalid_Date__c\":\"\"}";
    //ten day
    private static long TEN_DAYS = 10;
    public CN_Notification_Message__c(String objectName, JSONObject json) {
        super(AbInBevConstants.AbInBevObjects.CN_Notification_Message, json);
    }

    protected CN_Notification_Message__c(String objectName) {
        super(AbInBevConstants.AbInBevObjects.CN_Notification_Message);
    }

    protected CN_Notification_Message__c(JSONObject jsonObject) {
        super(AbInBevConstants.AbInBevObjects.CN_Notification_Message, jsonObject);

    }

    /**
     * set IsRead
     *
     * @param IsRead
     */
    public void setIsRead(int IsRead) {
        setIntValueForKey(AbInBevConstants.CNNotificationMessageFields.IsRead, IsRead);
    }

    /**
     * get IsRead
     *
     * @return
     */
    public int getIsRead() {

        return getIntValueForKey(AbInBevConstants.CNNotificationMessageFields.IsRead);
    }

    /**
     * set Title
     *
     * @param Title
     */
    public void setTitle(String Title) {
        setStringValueForKey(AbInBevConstants.CNNotificationMessageFields.CN_Title__c, Title);
    }

    /**
     * get Title
     *
     * @return
     */
    public String getTitle() {
        return getStringValueForKey(AbInBevConstants.CNNotificationMessageFields.CN_Title__c);
    }

    /**
     * set Description
     *
     * @param Description
     */
    public void setDescription(String Description) {
        setStringValueForKey(AbInBevConstants.CNNotificationMessageFields.CN_Description__c, Description);
    }

    /**
     * get Description
     *
     * @return
     */
    public String getDescription() {
        return getStringValueForKey(AbInBevConstants.CNNotificationMessageFields.CN_Description__c);
    }

    /**
     * set Due_Date
     *
     * @param Due_Date
     */
    public void setDue_Date(String Due_Date) {
        setStringValueForKey(AbInBevConstants.CNNotificationMessageFields.CN_Due_Date__c, Due_Date);
    }

    /**
     * get Due_Date
     *
     * @return
     */
    public String getDue_Date() {
        return getStringValueForKey(AbInBevConstants.CNNotificationMessageFields.CN_Due_Date__c);
    }

    public String getDueDateFormat() {

        String result = getDue_Date();

        try {
            if (!TextUtils.isEmpty(result)) {

                Date date = DateUtils.dateFromStringShortCN(result);
                SimpleDateFormat forFormat = new SimpleDateFormat("yyyy/MM/dd");
                result = forFormat.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * set Id
     *
     * @param Id
     */
    public void setId(String Id) {
        setStringValueForKey(AbInBevConstants.CNNotificationMessageFields.Id, Id);
    }

    /**
     * get Id
     *
     * @return
     */
    public String getId() {
        return getStringValueForKey(AbInBevConstants.CNNotificationMessageFields.Id);
    }


    /**
     * set TypeId
     *
     * @param TypeId
     */
    public void setTypeId(String TypeId) {
        setStringValueForKey(AbInBevConstants.CNNotificationMessageFields.TypeId, TypeId);
    }


    /**
     * get Category
     *
     * @return
     */
    public String getCategory() {
        return getStringValueForKey(AbInBevConstants.CNNotificationMessageFields.CN_Category__c);
    }

    /**
     * set Category
     *
     * @param Category
     */
    public void setCategory(String Category) {
        setStringValueForKey(AbInBevConstants.CNNotificationMessageFields.CN_Category__c, Category);
    }


    /**
     * get ItemId
     *
     * @return
     */
    public int getItemId() {
        return getIntValueForKey(AbInBevConstants.CNNotificationMessageFields.ItemId);
    }

    /**
     * set ItemId
     *
     * @param ItemId
     */
    public void setItemId(String ItemId) {
        setStringValueForKey(AbInBevConstants.CNNotificationMessageFields.ItemId, ItemId);
    }

    /**
     * get TypeId
     *
     * @return
     */
    public int getTypeId() {
        return getIntValueForKey(AbInBevConstants.CNNotificationMessageFields.TypeId);
    }

    /**
     * set NotifyTime
     *
     * @param timer
     */
    public void setNotifyTime(String timer) {
        setStringValueForKey(AbInBevConstants.CNNotificationMessageFields.CN_Notify_Time__c, timer);
    }

    /**
     * get NotifyTime
     *
     * @return
     */
    public String getNotifyTime() {
        return getStringValueForKey(AbInBevConstants.CNNotificationMessageFields.CN_Notify_Time__c);
    }

    /**
     * set CN_Related_ID__c
     *
     * @param RelatedId
     */
    public void setRelatedId(String RelatedId) {
        setStringValueForKey(AbInBevConstants.CNNotificationMessageFields.CN_Related_ID__c, RelatedId);
    }

    /**
     * get CN_Related_ID__c
     *
     * @return
     */
    public String getRelatedId() {
        return getStringValueForKey(AbInBevConstants.CNNotificationMessageFields.CN_Related_ID__c);
    }

    /**
     * set InvalidDate
     *
     * @param InvalidDate
     */
    public void setInvalidDate(String InvalidDate) {
        setStringValueForKey(AbInBevConstants.CNNotificationMessageFields.CN_Invalid_Date__c, InvalidDate);
    }


    /**
     * get InvalidDate
     *
     * @return
     */
    public String getInvalidDate() {
        return getStringValueForKey(AbInBevConstants.CNNotificationMessageFields.CN_Invalid_Date__c);
    }


    public String getNotifyTimeFormat() {
        String timer = getNotifyTime();
        String result = "";
        if (!TextUtils.isEmpty(timer)) {
            SimpleDateFormat aa = new SimpleDateFormat("yyyy-MM-dd KK:mm aa", Locale.ENGLISH);
            result = "" + aa.format(convertStrToDate(timer));
        }
        return result;
    }


    public synchronized static List<CN_Notification_Message__c> getCNNotitycationMessageAllData() {

        List<CN_Notification_Message__c> message__cs = getCNNotitycationMessage();
        try {
            //sort by notify timer
            if (message__cs != null && message__cs.size() > 0) {
                Collections.sort(message__cs, new Comparator<CN_Notification_Message__c>() {
                    @Override
                    public int compare(CN_Notification_Message__c o1, CN_Notification_Message__c o2) {
                        String t1 = o1.getNotifyTime();
                        String t2 = o2.getNotifyTime();

                        Date date1 = convertStrToDate(t1);
                        Date date2 = convertStrToDate(t2);
                        if (date1.after(date2)) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
                categoryTypeData(message__cs);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "" + e.getMessage());
        }

        return message__cs;
    }


    public synchronized static int getUnReadRecoderCount() {

        List<CN_Notification_Message__c> message__cs = getCNNotitycationMessage();

        int count = 0;
        if (message__cs != null && message__cs.size() > 0) {

            for (CN_Notification_Message__c c : message__cs) {

                CN_NotificationUserRead__c isRead = CN_NotificationUserRead__c.isExistsRecord(c.getId());

                if (isRead == null) {
                    count++;
                }
            }
        }
        return message__cs == null ? 0 : count;
    }

    /**
     * Filter classification
     *
     * @param message__cs
     */
    private static void categoryTypeData(List<CN_Notification_Message__c> message__cs) {

        try {

            List<CN_Notification_Message__c> contractRenewal = new LinkedList<>();
            JSONObject contractJsonObject = new JSONObject(convertStandardJSONString(jsonFormat));
            contractJsonObject.put(AbInBevConstants.CNNotificationMessageFields.CN_Category__c, Contract_Renewal);
            CN_Notification_Message__c contract = new CN_Notification_Message__c(contractJsonObject);
            contract.setIntValueForKey(AbInBevConstants.CNNotificationMessageFields.TypeId, NotifyMsgAdapter.TYPE_CONTRACT_RENEWAL);
            contract.setIntValueForKey(AbInBevConstants.CNNotificationMessageFields.ItemId, NotifyMsgAdapter.TYPE_TITLE);
            contractRenewal.add(contract);

            List<CN_Notification_Message__c> newTaskData = new LinkedList<>();
            JSONObject newTaskJsonObject = new JSONObject(convertStandardJSONString(jsonFormat));
            newTaskJsonObject.put(AbInBevConstants.CNNotificationMessageFields.CN_Category__c, New_Task);
            CN_Notification_Message__c newTask = new CN_Notification_Message__c(newTaskJsonObject);
            newTask.setIntValueForKey(AbInBevConstants.CNNotificationMessageFields.TypeId, NotifyMsgAdapter.TYPE_NEW_TASK);
            newTask.setIntValueForKey(AbInBevConstants.CNNotificationMessageFields.ItemId, NotifyMsgAdapter.TYPE_TITLE);
            newTaskData.add(newTask);


            List<CN_Notification_Message__c> systemNotification = new LinkedList<>();
            JSONObject notifyJsonObject = new JSONObject(convertStandardJSONString(jsonFormat));
            notifyJsonObject.put(AbInBevConstants.CNNotificationMessageFields.CN_Category__c, System_Notification);
            CN_Notification_Message__c notifycation = new CN_Notification_Message__c(notifyJsonObject);
            notifycation.setIntValueForKey(AbInBevConstants.CNNotificationMessageFields.TypeId, NotifyMsgAdapter.TYPE_SYSTEM_NOTIFICATION);
            notifycation.setIntValueForKey(AbInBevConstants.CNNotificationMessageFields.ItemId, NotifyMsgAdapter.TYPE_TITLE);
            systemNotification.add(notifycation);

            if (message__cs != null && message__cs.size() > 0) {
                Iterator<CN_Notification_Message__c> iterator = message__cs.iterator();

                while (iterator.hasNext()) {

                    CN_Notification_Message__c c = iterator.next();

                    if (c != null) {

                        String id = c.getId();
                        //current Time
                        Date currentDate = new Date();
                        //Message Time
                        String timer = c.getInvalidDate();

                        Date messageDate = DateUtils.dateFromStringShortCN(timer);

                        if (messageDate.before(currentDate)) {
                            CN_NotificationUserRead__c isRead = CN_NotificationUserRead__c.isExistsRecord(id);

                            if (isRead != null) {

                                iterator.remove();
                                /**
                                 * If the current time is greater than the valid date and is more than ten day, delete the record
                                 */
                                int days = DateUtils.getGapCount(currentDate, messageDate);
                                if (days > TEN_DAYS) {
                                    boolean isSuccess = deleteRecoderById(id);
                                }
                            }
                        } else {
                            CN_NotificationUserRead__c isRead = CN_NotificationUserRead__c.isExistsRecord(id);

                            if (isRead != null) {
                                iterator.remove();
                            }
                        }
                    }
                }
            }
            for (CN_Notification_Message__c c : message__cs) {

                if (c.getTypeId() == NotifyMsgAdapter.TYPE_NEW_TASK) {
                    c.setIntValueForKey(AbInBevConstants.CNNotificationMessageFields.ItemId, NotifyMsgAdapter.TYPE_CONTENT);
                    newTaskData.add(c);
                } else if (c.getTypeId() == NotifyMsgAdapter.TYPE_SYSTEM_NOTIFICATION) {
                    c.setIntValueForKey(AbInBevConstants.CNNotificationMessageFields.ItemId, NotifyMsgAdapter.TYPE_CONTENT);
                    systemNotification.add(c);
                } else if (c.getTypeId() == NotifyMsgAdapter.TYPE_CONTRACT_RENEWAL) {
                    c.setIntValueForKey(AbInBevConstants.CNNotificationMessageFields.ItemId, NotifyMsgAdapter.TYPE_CONTENT);
                    contractRenewal.add(c);
                }
            }

            message__cs.clear();

            if (contractRenewal.size() > 1)

                message__cs.addAll(contractRenewal);

            if (newTaskData.size() > 1)

                message__cs.addAll(newTaskData);

            if (systemNotification.size() > 1)

                message__cs.addAll(systemNotification);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }


    /**
     * Delete the record specified in the table according to the ID
     * @param id
     * @return
     */
    public static boolean deleteRecoderById(String id) {
        boolean isSuccess = false;
        try {

            if (TextUtils.isEmpty(id))
                throw new NullPointerException("Id Parameters cannot be empty");

            isSuccess = DataManagerFactory.getDataManager().deleteRecord(AbInBevConstants.AbInBevObjects.CN_Notification_Message, id);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return isSuccess;

    }

    /**
     * Update the record specified in the table according to the ID
     * @param id
     * @param jsonObject
     * @return
     */
    public synchronized static boolean updateRecoderById(String id, JSONObject jsonObject) {
        boolean isSuccess = false;
        try {
            if (TextUtils.isEmpty(id) || jsonObject == null)
                throw new NullPointerException("Id or jsonObject Parameters cannot be empty");
            isSuccess = DataManagerFactory.getDataManager().updateRecord(AbInBevConstants.AbInBevObjects.CN_Notification_Message, id, jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSuccess;
    }


    /**
     * timer String convert date
     * @param valueString
     * @return
     */
    public static Date convertStrToDate(String valueString) {

        Date serverDate = null;
        try {

            serverDate = DateUtils.dateFromStringTimeCN(valueString);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverDate;
    }

    /**
     * Get all the information from the database
     *
     * @return
     */
    public synchronized static List<CN_Notification_Message__c> getCNNotitycationMessage() {

        List<CN_Notification_Message__c> message__cs = new LinkedList<CN_Notification_Message__c>();

        try {

            String smartSql = String.format("SELECT * FROM {%1$s}", AbInBevConstants.AbInBevObjects.CN_Notification_Message);

            JSONArray jsonArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

            if (jsonArray != null && jsonArray.length() > 0) {

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONArray elements = jsonArray.getJSONArray(i);

                    if (elements != null && elements.length() > 0) {

                        JSONObject jsonObject = elements.getJSONObject(1);
                        if (jsonObject != null) {
                            String Id = jsonObject.optString(AbInBevConstants.CNNotificationMessageFields.Id);
                            String Name = jsonObject.optString(AbInBevConstants.CNNotificationMessageFields.Name);
                            String CN_Category__c = jsonObject.optString(AbInBevConstants.CNNotificationMessageFields.CN_Category__c);
                            String CN_Description__c = jsonObject.optString(AbInBevConstants.CNNotificationMessageFields.CN_Description__c);
                            String CN_Due_Date__c = jsonObject.optString(AbInBevConstants.CNNotificationMessageFields.CN_Due_Date__c);
                            String CN_Notify_Time__c = jsonObject.optString(AbInBevConstants.CNNotificationMessageFields.CN_Notify_Time__c);
                            String CN_Related_ID__c = jsonObject.optString(AbInBevConstants.CNNotificationMessageFields.CN_Related_ID__c);
                            String CN_Title__c = jsonObject.optString(AbInBevConstants.CNNotificationMessageFields.CN_Title__c);
                            String CN_Visibility_Type__c = jsonObject.optString(AbInBevConstants.CNNotificationMessageFields.CN_Visibility_Type__c);
//                            int IsRead = jsonObject.optInt(AbInBevConstants.CNNotificationMessageFields.IsRead, -1);
                            String OwnerID = jsonObject.optString(AbInBevConstants.CNNotificationMessageFields.OwnerID);
                            String CN_Invalid_Date__c = jsonObject.optString(AbInBevConstants.CNNotificationMessageFields.CN_Invalid_Date__c);

                            int TypeId = NotifyMsgAdapter.TYPE_DEFAULT;
                            if (New_Task.equalsIgnoreCase(CN_Category__c)) {
                                TypeId = NotifyMsgAdapter.TYPE_NEW_TASK;
                            } else if (System_Notification.equalsIgnoreCase(CN_Category__c)) {
                                TypeId = NotifyMsgAdapter.TYPE_SYSTEM_NOTIFICATION;
                            } else if (Contract_Renewal.equalsIgnoreCase(CN_Category__c)) {
                                TypeId = NotifyMsgAdapter.TYPE_CONTRACT_RENEWAL;
                            }

                            JSONObject object = new JSONObject();
                            object.put(AbInBevConstants.CNNotificationMessageFields.Id, Id);
                            object.put(AbInBevConstants.CNNotificationMessageFields.Name, Name);
                            object.put(AbInBevConstants.CNNotificationMessageFields.CN_Category__c, CN_Category__c);
                            object.put(AbInBevConstants.CNNotificationMessageFields.CN_Description__c, CN_Description__c);
                            object.put(AbInBevConstants.CNNotificationMessageFields.CN_Due_Date__c, CN_Due_Date__c);
                            object.put(AbInBevConstants.CNNotificationMessageFields.CN_Notify_Time__c, CN_Notify_Time__c);
                            object.put(AbInBevConstants.CNNotificationMessageFields.CN_Related_ID__c, CN_Related_ID__c);
                            object.put(AbInBevConstants.CNNotificationMessageFields.CN_Title__c, CN_Title__c);
                            object.put(AbInBevConstants.CNNotificationMessageFields.CN_Visibility_Type__c, CN_Visibility_Type__c);
//                            object.put(AbInBevConstants.CNNotificationMessageFields.IsRead, IsRead);
                            object.put(AbInBevConstants.CNNotificationMessageFields.OwnerID, OwnerID);
                            object.put(AbInBevConstants.CNNotificationMessageFields.TypeId, TypeId);
                            object.put(AbInBevConstants.CNNotificationMessageFields.CN_Invalid_Date__c, CN_Invalid_Date__c);
                            object.put(AbInBevConstants.CNNotificationMessageFields.ItemId, NotifyMsgAdapter.TYPE_CONTENT);
                            CN_Notification_Message__c c = new CN_Notification_Message__c(object);
                            message__cs.add(c);

                        }
                    }
                }
            }


//            CN_NotificationUserRead__c.getAll();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "" + e.getMessage());
        }
        return message__cs;
    }



    public static String convertStandardJSONString(String data_json) {
        data_json = data_json.replaceAll("\\\\r\\\\n", "");
        data_json = data_json.replace("\"{", "{");
        data_json = data_json.replace("}\",", "},");
        data_json = data_json.replace("}\"", "}");
        return data_json;
    }



}
