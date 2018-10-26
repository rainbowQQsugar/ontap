package com.abinbev.dsa.model;

import android.text.TextUtils;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.PicklistUtils;
import com.salesforce.androidsyncengine.data.layouts.RecordTypeMapping;
import com.salesforce.androidsyncengine.data.model.PicklistValue;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Jakub Stefanowski on 30.01.2017.
 */

public abstract class TranslatableSFBaseObject extends SFBaseObject {

    private static final String FIELD_RECORD_TYPE_ID = "RecordTypeId";

    public static final String FIELD_RECORD_NAME = AbInBevConstants.AbInBevObjects.RECORD_TYPE +
            "." + AbInBevConstants.RecordTypeFields.NAME;

    private Map<String, String> translatedFieldsMap = new HashMap<>();

    public TranslatableSFBaseObject(String objectName, JSONObject json) {
        super(objectName, json);
    }

    public TranslatableSFBaseObject(String objectName) {
        super(objectName);
    }

    public void setTranslationMap(Map<String, String> translationMap) {
        translatedFieldsMap.clear();
        if (translationMap != null) {
            translatedFieldsMap.putAll(translationMap);
        }
    }

    public String getTranslatedStringValueForKey(String key) {
        String translatedValue = translatedFieldsMap.get(key);
        return translatedValue == null ? getStringValueForKey(key) : translatedValue;
    }

    public void setTranslatedStringValue(String key, String translatedValue) {
        translatedFieldsMap.put(key, translatedValue);
    }

    /** Adds translations to the TranslatableSFBaseObject list items. */
    public static void addTranslations(List<? extends TranslatableSFBaseObject> objects, String objectType, String... translatableFields) {
        if (objects == null || objects.isEmpty()) return;
        if (translatableFields == null || translatableFields.length == 0) return;

        HashMap<String, HashMap<String, List<PicklistValue>>> recordTypeToPicklistValues = new HashMap<>();

        for (TranslatableSFBaseObject translatableObject : objects) {
            String recordTypeId = translatableObject.getStringValueForKey(FIELD_RECORD_TYPE_ID);
            HashMap<String, List<PicklistValue>> currentPicklistValues = null;

            currentPicklistValues = recordTypeToPicklistValues.get(recordTypeId);

            if (currentPicklistValues == null) {
                HashMap<String, List<PicklistValue>> newPicklistValues = getPicklistValues(recordTypeId, objectType, translatableFields);
                if (newPicklistValues != null) {
                    currentPicklistValues = newPicklistValues;
                    recordTypeToPicklistValues.put(recordTypeId, currentPicklistValues);
                }
            }

            Map<String, String> translationMap = getTranslationMap(translatableObject, currentPicklistValues);
            translatableObject.setTranslationMap(translationMap);
        }
    }

    public static void addTranslations(TranslatableSFBaseObject object, String objectType, String... translatableFields) {
        if (object == null) return;
        addTranslations(Collections.singletonList(object), objectType, translatableFields);
    }

    public static void addRecordTypeTranslations(List<? extends TranslatableSFBaseObject> objects, String objectType, String fieldName) {
        if (objects == null || objects.isEmpty()) return;

        List<RecordTypeMapping> mappings = PicklistUtils.getRecordTypeMappings(objectType);
        if (mappings != null && !mappings.isEmpty()) {

            // Prepare map of recordTypeId and translated names
            Map<String, String> recordTypeTranslations = new HashMap<>();
            for (RecordTypeMapping mapping : mappings) {
                recordTypeTranslations.put(mapping.getRecordTypeId(), mapping.getName());
            }

            for (TranslatableSFBaseObject object : objects) {
                String recordTypeId = object.getStringValueForKey(FIELD_RECORD_TYPE_ID);

                if (!TextUtils.isEmpty(recordTypeId)) {
                    String translatedValue = recordTypeTranslations.get(recordTypeId);

                    if (!TextUtils.isEmpty(translatedValue)) {
                        object.setTranslatedStringValue(fieldName, translatedValue);
                    }
                }
            }
        }
    }

    /** Creates map with translated values for specific fields. */
    private static Map<String, String> getTranslationMap(TranslatableSFBaseObject object, HashMap<String, List<PicklistValue>> picklistMap) { //TODO Replace list with map for faster search
        if (picklistMap == null || picklistMap.isEmpty()) return null;
        Map<String, String> translationMap = new HashMap<>();

        for (String fieldName : picklistMap.keySet()) {
            String fieldValue = object.getStringValueForKey(fieldName);
            List<PicklistValue> fieldPicklists = picklistMap.get(fieldName);
            PicklistValue picklistValue = getPicklistByValue(fieldPicklists, fieldValue);

            if (picklistValue != null) {
                translationMap.put(fieldName, picklistValue.getLabel());
            }
        }

        return translationMap;
    }

    /** Find PicklistValue with matching value. */
    private static PicklistValue getPicklistByValue(List<PicklistValue> picklists, String value) {
        if (picklists == null) return null;

        for (PicklistValue picklist : picklists) {
            if (picklist.getValue() != null && picklist.getValue().equals(value)) {
                return picklist;
            }
        }

        return null;
    }

    /** Reads picklist values for specified object, record type and only for required fields. */
    private static HashMap<String, List<PicklistValue>> getPicklistValues(String recordTypeId, String objectType, String... translatableFields) {
        HashMap<String, List<PicklistValue>> result = null;

        // Take picklist values from layout file.
        if (recordTypeId != null) {
            result = PicklistUtils.getPicklistValues(objectType, recordTypeId, translatableFields);
            removeEmptyEntries(result);
        }

        // If there was no picklistValues try to take them from .meta file.
        if (result == null || result.isEmpty()) {
            result = PicklistUtils.getMetadataPicklistValues(objectType, translatableFields);
            removeEmptyEntries(result);
        }

        return result;
    }

    /** Remove empty lists from the map. */
    private static void removeEmptyEntries(HashMap<String, List<PicklistValue>> map) {
        if (map == null || map.isEmpty()) return;
        Set<Map.Entry<String, List<PicklistValue>>> entries = map.entrySet();
        Iterator<Map.Entry<String, List<PicklistValue>>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<PicklistValue>> entry = iterator.next();
            if (entry.getValue() == null || entry.getValue().isEmpty())
                iterator.remove();
        }
    }

    public String getRecordName() {
        JSONObject json = getJsonObject(AbInBevConstants.AbInBevObjects.RECORD_TYPE);
        if (json != null) {
            return json.optString(AbInBevConstants.RecordTypeFields.NAME);
        }
        return null;
    }

    public String getTranslatedRecordName() {
        String translated = getTranslatedStringValueForKey(FIELD_RECORD_NAME);
        return TextUtils.isEmpty(translated) ? getRecordName() : translated;
    }

    public String getRecordTypeId() {
        return getStringValueForKey(AbInBevConstants.RECORD_TYPE_ID);
    }

    public void setRecordTypeId(String id) {
        setStringValueForKey(AbInBevConstants.RECORD_TYPE_ID, id);
    }
}
