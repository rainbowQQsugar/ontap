package com.abinbev.dsa.model;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.DateUtils;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Test version control Class
 */
public class CN_App_Version__c extends SFBaseObject {

    private static String TAG = "com.abinbev.dsa.model.CN_App_Version__c.class";

    public CN_App_Version__c(String objectName, JSONObject json) {
        super(AbInBevObjects.Version_Controller, json);
    }

    protected CN_App_Version__c(String objectName) {
        super(AbInBevObjects.Version_Controller);
    }

    protected CN_App_Version__c(JSONObject jsonObject) {
        super(AbInBevObjects.Version_Controller, jsonObject);
    }

    /**
     * set Id
     *
     * @param Id
     */
    public void setId(String Id) {
        setStringValueForKey(AbInBevConstants.CNAppVersionFields.Id, Id);
    }

    /**
     * get Id
     *
     * @return
     */
    public String getId() {

        return getStringValueForKey(AbInBevConstants.CNAppVersionFields.Id);
    }

    /**
     * set name
     *
     * @param name
     */
    public void setName(String name) {
        setStringValueForKey(AbInBevConstants.CNAppVersionFields.Name, name);
    }

    /**
     * set Name
     *
     * @return
     */
    public String getName() {

        return getStringValueForKey(AbInBevConstants.CNAppVersionFields.Name);
    }

    /**
     * set CN_Release_Date__c
     *
     * @param releaseDate
     */
    public void setReleaseDate(String releaseDate) {
        setStringValueForKey(AbInBevConstants.CNAppVersionFields.CN_RELEASE_DATE__C, releaseDate);
    }

    /**
     * gte CN_Release_Date__c
     *
     * @return
     */
    public String getReleaseDate() {
        return getStringValueForKey(AbInBevConstants.CNAppVersionFields.CN_RELEASE_DATE__C);
    }

    /**
     * set CN_Release_Date__c
     *
     * @param releaseNotes
     */
    public void setReleaseNotes(String releaseNotes) {
        setStringValueForKey(AbInBevConstants.CNAppVersionFields.CN_RELEASE_NOTES__C, releaseNotes);
    }

    /**
     * get CN_Release_Date__c
     *
     * @return
     */
    public String getReleaseNotes() {
        return getStringValueForKey(AbInBevConstants.CNAppVersionFields.CN_RELEASE_NOTES__C);
    }

    /**
     * set CN_Download_Link__c
     *
     * @param downLoadLink
     */
    public void setDownLoadLink(String downLoadLink) {
        setStringValueForKey(AbInBevConstants.CNAppVersionFields.CN_DOWNLOAD_LINK_C, downLoadLink);
    }

    /**
     * get CN_Download_Link__c
     *
     * @return
     */
    public String getDownLoadLink() {
        return getStringValueForKey(AbInBevConstants.CNAppVersionFields.CN_DOWNLOAD_LINK_C);
    }


    /**
     * Gets the latest version control data
     *
     * @return
     */
    public static CN_App_Version__c getchVersionControlerAllData() {

        List<CN_App_Version__c> cn_app_version__cs = getAllData();

        try {
            if (cn_app_version__cs != null && cn_app_version__cs.size() > 0) {

                Collections.sort(cn_app_version__cs, new ComparatorDate());

                List<CN_App_Version__c> smaeData = getSamedata(cn_app_version__cs);

                CN_App_Version__c c = smaeData.get(0);

                return c;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * fetch same data
     * @param cn_app_version__cs
     * @return
     */
    private static List<CN_App_Version__c> getSamedata(List<CN_App_Version__c> cn_app_version__cs) {

        List<CN_App_Version__c> cn_app_version = new LinkedList<>();

        try {

            String timerTemp = cn_app_version__cs.get(0).getReleaseDate();

            for (CN_App_Version__c c:cn_app_version__cs) {

                String timer = c.getReleaseDate();

                if (timerTemp.equalsIgnoreCase(timer)) {
                    cn_app_version.add(c);
                }
            }

            if (cn_app_version != null && cn_app_version.size() > 0)
                Collections.sort(cn_app_version, new VersionComparatorDate());

        } catch (Exception e) {
            return null;
        }

        return cn_app_version;

    }

    private static void deleteVersionControler(List<CN_App_Version__c> cn_app_version__cs, String id) {

        try {

            if (cn_app_version__cs.size() == 1) {
                return;
            }
            Iterator<CN_App_Version__c> iterator = cn_app_version__cs.iterator();

            while (iterator.hasNext()) {
                CN_App_Version__c c = iterator.next();

                if (c != null) {
                    String deleteID = c.getId();

                    if (!TextUtils.isEmpty(deleteID) && !deleteID.equalsIgnoreCase(id)) {

                        boolean isDelete = DataManagerFactory.getDataManager().deleteRecord(AbInBevObjects.Version_Controller, id);

                        if (isDelete) {
                            Log.e(TAG, "Deleteed Success");
                        } else {
                            Log.e(TAG, "Deleteed Failed");
                        }

                    }
                }

            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Get all the data in CN_App_Version__c
     *
     * @return
     */
    public static List<CN_App_Version__c> getAllData() {
        List<CN_App_Version__c> controler__cList = new LinkedList<>();
        try {
            String smartSql = String.format("SELECT * FROM {%1$s}", AbInBevObjects.Version_Controller);
            JSONArray jsonArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            if (jsonArray != null && jsonArray.length() > 0) {

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONArray elements = jsonArray.getJSONArray(i);

                    if (elements != null && elements.length() > 0) {

                        JSONObject jsonObject = elements.getJSONObject(1);
                        CN_App_Version__c c = new CN_App_Version__c(jsonObject);
                        controler__cList.add(c);
                    }
                }
            }
        } catch (Exception e) {
            return controler__cList;
        }
        return controler__cList;
    }

    /**
     * date sort Desc
     */
    public static class ComparatorDate implements Comparator {

        public int compare(Object obj1, Object obj2) {
            try {
                String beginDate = ((CN_App_Version__c) obj1).getReleaseDate();
                String endDate = ((CN_App_Version__c) obj2).getReleaseDate();

                Date begin = DateUtils.dateFromStringShortCN(beginDate);
                Date end = DateUtils.dateFromStringShortCN(endDate);
                if (begin.after(end)) {
                    return -1;
                } else {
                    return 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    /**
     * date sort Desc
     */
    public static class VersionComparatorDate implements Comparator {


        public int compare(Object obj1, Object obj2) {
            try {
                String beginDate = ((CN_App_Version__c) obj1).getName().replace(".", "");
                String endDate = ((CN_App_Version__c) obj2).getName().replace(".", "");

                int be = Integer.parseInt(beginDate);
                int en = Integer.parseInt(endDate);

                if (be > en) {
                    return -1;
                } else if (be < en){
                    return 1;
                } else {
                    return 0;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

}
