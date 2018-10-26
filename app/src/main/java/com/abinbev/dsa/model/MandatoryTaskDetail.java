package com.abinbev.dsa.model;

import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.MandatoryTaskDetailFields;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;

import static com.salesforce.dsa.utils.DSAConstants.Formats.SMART_SQL_FORMAT;

public class MandatoryTaskDetail extends SFBaseObject {

    public static final String TAG = MandatoryTaskDetail.class.getSimpleName();

    protected MandatoryTaskDetail() {
        super(AbInBevObjects.MANDATORY_TASK_DETAIL);
    }

    public MandatoryTaskDetail(JSONObject json) {
        super(AbInBevObjects.MANDATORY_TASK_DETAIL, json);
    }

    public boolean getMandatoryTaskGroupId() {
        return getBooleanValueForKey(MandatoryTaskDetailFields.MANDATORY_TASK_GROUP);
    }

    public String getErrorMessage() {
        return getStringValueForKey(MandatoryTaskDetailFields.ERROR_MESSAGE);
    }

    public String getMandatoryTaskFieldName() {
        return getStringValueForKey(MandatoryTaskDetailFields.MANDATORY_TASK_FIELD_NAME);
    }

    public String getMandatoryTaskObject() {
        return getStringValueForKey(MandatoryTaskDetailFields.MANDATORY_TASK_OBJECT);
    }

    public String getSurvey() {
        return getStringValueForKey(MandatoryTaskDetailFields.SURVEY);
    }

    public String getTaskType() {
        return getStringValueForKey(MandatoryTaskDetailFields.TASK_TYPE);
    }

    public static Observable<List<MandatoryTaskDetail>> getByGroup(MandatoryTaskGroup group) {
        return group == null ? Observable.just(Collections.emptyList()) : getByGroup(group.getId());
    }

    public static Observable<List<MandatoryTaskDetail>> getByGroup(String mandatoryTaskGroupId) {
        return Observable.fromCallable(() -> {
            String filter = String.format("{%s:%s} = '%s'",
                    AbInBevObjects.MANDATORY_TASK_DETAIL, MandatoryTaskDetailFields.MANDATORY_TASK_GROUP, mandatoryTaskGroupId);

            String smartSql = String.format(SMART_SQL_FORMAT, AbInBevObjects.MANDATORY_TASK_DETAIL, filter);

            DataManager dm = DataManagerFactory.getDataManager();
            return DataManagerUtils.fetchObjects(dm, smartSql, MandatoryTaskDetail.class);
        });
    }
}