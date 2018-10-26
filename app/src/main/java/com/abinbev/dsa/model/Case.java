package com.abinbev.dsa.model;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.ui.presenter.CasosPresenter;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.CaseFields;
import com.abinbev.dsa.utils.AbInBevConstants.CaseStatus;
import com.abinbev.dsa.utils.AbInBevConstants.RecordTypeName;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.abinbev.dsa.utils.datamanager.FormatValues;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Case extends TranslatableSFBaseObject {

    public interface CasosStates {
        String OPEN = "Open";
        String PENDING = "Pending";
        String IN_PROGRESS = "In Progress";
        String CANCELLED = "Cancelled";
        String CLOSED = "Closed";
        String RESOLVED = "Resolved";
        String IN_APPROVAL = "In approval";

    }

    public static final String TAG = Case.class.getName();
    public static final String ASSET_CASE_TYPE_NAME = "Caso de Activos";

    public static final FormatValues OBJECT_FORMAT_VALUES = new FormatValues.Builder()
            .putTable("Case", AbInBevObjects.CASE)
                .putColumn("RecordTypeId", CaseFields.RECORD_TYPE_ID)
                .putColumn("RecordType.Name", CaseFields.RECORD_TYPE_NAME)
                .putColumn("Status", CaseFields.STATUS)
            .build();

    private Account account;

    public Case(JSONObject json) {
        super(AbInBevObjects.CASE, json);
    }

    public Case(String accountId) {
        this(accountId, null, null);
    }

    public Case(String accountId, String assetId) {
        this(accountId, assetId, null);
    }

    public Case(String accountId, String assetId, String parentId) {
        super(AbInBevObjects.CASE, new JSONObject());
        if(TextUtils.isEmpty(accountId)) {
            throw new IllegalArgumentException("Missing required info to create new case -  "
                    + "ensure you are providing a recordType, contact, and account");
        }

        setStringValueForKey(CaseFields.ACCOUNT_ID, accountId);
        setStringValueForKey(CaseFields.STATUS, CasosStates.OPEN);
        // setStringValueForKey(AbInBevConstants.CaseFields.PRIORITY, "2");
        // setStringValueForKey(AbInBevConstants.CaseFields.CASE_ORIGIN, "Mobile");
        if (!TextUtils.isEmpty(assetId)) {
            setStringValueForKey(AbInBevConstants.CasosFields.ASSET_C, assetId);
        }
        if(!TextUtils.isEmpty(parentId)) {
            setStringValueForKey(AbInBevConstants.CasosFields.PARENT_ID, parentId);
        }
    }

    public static String createNewCase(Case caso) {
        String recordTypeId = caso.getRecordTypeId();
        String accountId = caso.getAccountId();
        if (TextUtils.isEmpty(recordTypeId) || TextUtils.isEmpty(accountId)) {
            throw new IllegalArgumentException("Missing required info to create new case -  "
                    + "ensure you are providing a recordType, contact, and account");
        }

        // strip out RecordType Info before we create Record
        JSONObject updatedCaseObject = caso.toJson();
        updatedCaseObject.remove("RecordType");
        DataManager dataManager = DataManagerFactory.getDataManager();
        return dataManager.createRecord(AbInBevObjects.CASE, updatedCaseObject);
    }

    public Account getAccount() {
        if (account == null) {
            account = Account.getById(getAccountId());
        }

        return account;
    }

    public void invalidateAccount() {
        account = null;
    }

    public String getAccountId() {
        return getStringValueForKey(CaseFields.ACCOUNT_ID);
    }

    public String getContactName() {
        return getStringValueForKey(CaseFields.CONTACT_NAME);
    }

    public String getCaseNumber() {
        return getStringValueForKey(CaseFields.CONTACT_NAME);
    }

    public void setStatus(String status) {
        setStringValueForKey(CaseFields.STATUS, status);
    }

    public String getStatus() {
        return getStringValueForKey(CaseFields.STATUS);
    }

    public String getTranslatedStatus() {
        return getTranslatedStringValueForKey(CaseFields.STATUS);
    }

    public String getSLA1() {
        return getStringValueForKey(CaseFields.SLA1);
    }

    public void setOwnerId(String ownerId) {
        setStringValueForKey(CaseFields.OWNER, ownerId);
    }

    public void setRecordTypeId(String recordTypeId) {
        setStringValueForKey(CaseFields.RECORD_TYPE_ID, recordTypeId);
    }

    public void setContact(String contactId) {
        setStringValueForKey(CaseFields.CONTACT_NAME, contactId);
    }

    public static List<CasosPresenter.CasoCountWrapper> getOpenCasesWrapperByAccountId(String accountId) {

        String smartSqlFilter = String.format("{%s:%s} = '%s' GROUP BY {%s:%s}",
                AbInBevObjects.CASE, CaseFields.ACCOUNT_ID, accountId,
                AbInBevObjects.CASE, CaseFields.RECORD_TYPE_NAME);

        String smartSql = String.format("SELECT {%s:%2$s}, count(Id) FROM {%1$s} WHERE %3$s", AbInBevObjects.CASE, CaseFields.RECORD_TYPE_NAME, smartSqlFilter);

        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

        List<CasosPresenter.CasoCountWrapper> casos = new ArrayList<>();
        int caseNameIndex = 0;
        int caseCountIndex = 1;
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONArray tuple = recordsArray.getJSONArray(i);
                int caseCount = tuple.getInt(caseCountIndex);
                String caseName = tuple.getString(caseNameIndex);
                casos.add(new CasosPresenter.CasoCountWrapper(caseName, caseCount));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting casos by Account ID: " + accountId, e);
        }
        return casos;
    }

    public static List<Case> getOpenCasesByAccountId(String accountId) {
        String smartSqlFilter = String.format("{%s:%s} = ('%s') ORDER BY " +
                        "isOpen DESC, CASE isOpen WHEN 1 THEN {%s:%s} ELSE {%s:%s} END DESC",
                AbInBevObjects.CASE, CaseFields.ACCOUNT_ID, accountId,
                AbInBevObjects.CASE, SyncEngineConstants.StdFields.CREATED_DATE,
                AbInBevObjects.CASE, SyncEngineConstants.StdFields.LAST_MODIFIED_DATE);

        String smartSql = String.format("SELECT {%1$s:_soup}, " +
                        "CASE {%1s:%2s} WHEN '%4$s' THEN 1 WHEN '%5$s' THEN 1 WHEN '%6$s' THEN 1 WHEN '%7$s' THEN 1 WHEN '%8$s' THEN 1 ELSE 0 END as 'isOpen' " + // additional column 'isOpen' telling if case is open
                        "FROM {%1$s} WHERE %3$s", AbInBevObjects.CASE, CaseFields.STATUS, smartSqlFilter,
                CasosStates.IN_PROGRESS, CasosStates.OPEN, CasosStates.PENDING, "Abierto", "Pendiente"); //Some of the states are still in spanish.
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

        List<Case> casos = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                casos.add(new Case(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting All Cases for Account Id: " + accountId, e);
        }
        return casos;
    }

    public static List<String> getCaseIds() {
        String smartSql = String.format("SELECT {%1$s:Id} FROM {%1$s}", AbInBevObjects.CASE);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

        List<String> ids = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                ids.add(recordsArray.getJSONArray(i).getString(0));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting all case ids", e);
        }
        return ids;
    }

    public static List<Case> getOpenCasesByUserId(String userId) {
        String smartSqlFilter = String.format("{%s:%s} = ('%s') ORDER BY " +
                        "isOpen DESC, CASE isOpen WHEN 1 THEN {%s:%s} ELSE {%s:%s} END DESC",
                AbInBevObjects.CASE, CaseFields.OWNER, userId,
                AbInBevObjects.CASE, SyncEngineConstants.StdFields.LAST_MODIFIED_DATE,
                AbInBevObjects.CASE, SyncEngineConstants.StdFields.CREATED_DATE);

        String smartSql = String.format("SELECT {%1$s:_soup}, " +
                        "CASE {%1s:%2s} WHEN '%4$s' THEN 1 WHEN '%5$s' THEN 1 WHEN '%6$s' THEN 1 ELSE 0 END as 'isOpen' " + // additional column 'isOpen' telling if case is open
                        "FROM {%1$s} WHERE %3$s", AbInBevObjects.CASE, CaseFields.STATUS, smartSqlFilter,
                CasosStates.IN_PROGRESS, CasosStates.OPEN, CasosStates.PENDING);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

        List<Case> casos = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                casos.add(new Case(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting All Cases for User Id: " + userId, e);
        }
        return casos;
    }

    public static int getOpenCasesCount(String accountId) {

        String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} NOT IN ('%s', '%s', '%s')",
                AbInBevObjects.CASE, CaseFields.ACCOUNT_ID, accountId,
                AbInBevObjects.CASE, CaseFields.STATUS,
                CasosStates.CANCELLED, CasosStates.CLOSED, CasosStates.RESOLVED);

        String smartSql = String.format("SELECT count(Id) FROM {%1$s} WHERE %2$s", AbInBevObjects.CASE, smartSqlFilter);

        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

        if (recordsArray.length() > 0) {
            try {
                return recordsArray.getJSONArray(0).getInt(0);
            } catch (Exception e) {
                Log.e(TAG, "Exception in getting casos by Account ID: " + accountId, e);
            }
        }
        return 0;
    }

    public static int getOpenCasesCountByUser(String userId) {

        String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} NOT IN ('%s', '%s', '%s')",
                AbInBevObjects.CASE, CaseFields.OWNER, userId,
                AbInBevObjects.CASE, CaseFields.STATUS,
                CasosStates.CANCELLED, CasosStates.CLOSED, CasosStates.RESOLVED);

        String smartSql = String.format("SELECT count(Id) FROM {%1$s} WHERE %2$s", AbInBevObjects.CASE, smartSqlFilter);

        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

        if (recordsArray.length() > 0) {
            try {
                return recordsArray.getJSONArray(0).getInt(0);
            } catch (Exception e) {
                Log.e(TAG, "Exception in getting casos by Owner ID: " + userId, e);
            }
        }
        return 0;
    }

    public static Case getById(String id) {
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.getById(dm, AbInBevObjects.CASE, id, Case.class);
    }

    public static int getRejectedAccountChangesCount() {
        FormatValues fv = new FormatValues()
                .addAll(OBJECT_FORMAT_VALUES)
                .putValue("accountChangeRequest", RecordTypeName.ACCOUNT_CHANGE_REQUEST)
                .putValue("rejected", CaseStatus.REJECTED);

        String query = "SELECT count() FROM {Case}" +
                " WHERE {Case:RecordType.Name} = '{accountChangeRequest}'" +
                    " AND {Case:Status} = '{rejected}'";

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchInt(dm, query, fv);
    }
}
