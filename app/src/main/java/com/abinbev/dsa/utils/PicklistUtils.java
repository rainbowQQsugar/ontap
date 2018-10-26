package com.abinbev.dsa.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.google.gson.Gson;
import com.salesforce.androidsyncengine.data.layouts.DetailLayoutSection;
import com.salesforce.androidsyncengine.data.layouts.Details;
import com.salesforce.androidsyncengine.data.layouts.EditLayoutSection;
import com.salesforce.androidsyncengine.data.layouts.IndividualLayouts;
import com.salesforce.androidsyncengine.data.layouts.LayoutComponent;
import com.salesforce.androidsyncengine.data.layouts.LayoutItem;
import com.salesforce.androidsyncengine.data.layouts.LayoutRow;
import com.salesforce.androidsyncengine.data.layouts.ObjectLayouts;
import com.salesforce.androidsyncengine.data.layouts.RecordTypeMapping;
import com.salesforce.androidsyncengine.data.model.DescribeSObjectResult;
import com.salesforce.androidsyncengine.data.model.Field;
import com.salesforce.androidsyncengine.data.model.PicklistDependencyHolder;
import com.salesforce.androidsyncengine.data.model.PicklistValue;
import com.salesforce.androidsyncengine.datamanager.MetaDataProvider;
import com.salesforce.androidsyncengine.syncmanifest.FieldValueObject;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PicklistUtils {

    public static String TAG = "PicklistUtils";

    public static PicklistDependencyHolder getPicklistDependencyHolder(Map detailsMap) {

        PicklistDependencyHolder picklistDependencyHolder = new PicklistDependencyHolder() ;

        class Bitset {
            byte[] data;

            public Bitset(byte[] data) {
                this.data = data == null ? new byte[0] : data;
            }

            public boolean testBit(int n) {
                return (data[n >> 3] & (0x80 >> n % 8)) != 0;
            }

            public int size() {
                return data.length * 8;
            }
        }

        try {

            for (Object value : detailsMap.values()) {
                Details field = (Details) value;
                // check whether this is a dependent picklist
                if (field.getDependentPicklist()) {
                    // get the controller by name
                    Details controller = (Details)detailsMap.get(field.getControllerName());

                    String controllerType;
                    if (controller == null) {
                        Log.e(TAG, "got null for controller: " + field.getControllerName());
                        controllerType = null;
                    } else {
                        controllerType = controller.getType();
                    }

//					System.out.println("Field '" + field.getLabel() + "' depends on '" +
//							controller.getLabel() + "'");
                    List<PicklistValue> picklistValues = field.getPicklistValues();
                    for (int j = 0; j < picklistValues.size(); j++) {
                        // for each PicklistEntry: list all controlling values for which it is valid
//						System.out.println("Item: '" + picklistValues.get(j).getLabel() +
//								"' is valid for: ");

                        String validForStringValue = (String) picklistValues.get(j).getValidFor();

                        if (validForStringValue == null || "null".equalsIgnoreCase(validForStringValue)) {
                            //Ignore the null values
                        } else {
                            Bitset validFor = new Bitset(Base64.decode(validForStringValue, Base64.DEFAULT));
                            if ("picklist".equalsIgnoreCase(controllerType)) {
                                // if the controller is a picklist, list all
                                // controlling values for which this entry is valid

                                List<PicklistValue> controllerPickListValues = controller.getPicklistValues();
                                int controllerPickListSize = controllerPickListValues.size();
                                for (int k = 0; k < validFor.size(); k++) {
                                    if (validFor.testBit(k)) {
                                        // if bit k is set, this entry is valid for the
                                        // for the controlling entry at index k

                                        if (k >= controllerPickListSize) {
                                            Log.e(TAG, "problem k > controllerPickListSize : " + k + " > " + controllerPickListSize);
                                        } else {
                                            // System.out.println(controllerPickListValues.get(k).getLabel());
                                            FieldValueObject fieldValueObject = new FieldValueObject(controller.getName(), controllerPickListValues.get(k).getValue());
                                            ArrayList<FieldValueObject> availableValues = (ArrayList<FieldValueObject>) picklistDependencyHolder.get(fieldValueObject);
                                            if (availableValues == null) availableValues = new ArrayList<FieldValueObject>();
                                            FieldValueObject valueElement = new FieldValueObject(field.getName(), picklistValues.get(j).getValue());
                                            availableValues.add(valueElement);
                                            picklistDependencyHolder.put(fieldValueObject, availableValues);
                                        }
                                    }
                                }
                            } else if ("Boolean".equalsIgnoreCase(controllerType)) {
                                // the controller is a checkbox
                                // if bit 1 is set this entry is valid if the controller is checked
                                if (validFor.testBit(1)) {
                                    System.out.println(" checked");
                                }
                                // if bit 0 is set this entry is valid if the controller is not checked
                                if (validFor.testBit(0)) {
                                    System.out.println(" unchecked");
                                }
                            }
                        }
                    }
                }
            }


        } catch (Exception ce) {
            Log.e(TAG, " " + ce.getMessage());
            ce.printStackTrace();
        }
        return picklistDependencyHolder;
    }

    public static Map buildDetailsMap(List<EditLayoutSection> editLayoutSections) {
        Map detailsMap = new HashMap();
        for (EditLayoutSection editLayoutSection : editLayoutSections) {
            List<LayoutRow> layoutRows = editLayoutSection.getLayoutRows();
            for (LayoutRow layoutRow : layoutRows) {
                List<LayoutItem> layoutItems = layoutRow.getLayoutItems();
                for (LayoutItem layoutItem : layoutItems) {
                    List<LayoutComponent> layoutComponents = layoutItem.getLayoutComponents();
                    for (LayoutComponent layoutComponent : layoutComponents) {
                        Details details = layoutComponent.getDetails();
                        if (details != null) {
                            detailsMap.put(details.getName(), details);
                        }
                    }
                }
            }
        }
        return detailsMap;
    }

    public static Map buildDetailsMapForView(List<DetailLayoutSection> detailLayoutSections) {
        Map detailsMap = new HashMap();
        for (DetailLayoutSection detailLayoutSection : detailLayoutSections) {
            List<LayoutRow> layoutRows = detailLayoutSection.getLayoutRows();
            for (LayoutRow layoutRow : layoutRows) {
                List<LayoutItem> layoutItems = layoutRow.getLayoutItems();
                for (LayoutItem layoutItem : layoutItems) {
                    List<LayoutComponent> layoutComponents = layoutItem.getLayoutComponents();
                    for (LayoutComponent layoutComponent : layoutComponents) {
                        Details details = layoutComponent.getDetails();
                        if (details != null) {
                            detailsMap.put(details.getName(), details);
                        }
                    }
                }
            }
        }
        return detailsMap;
    }

    public static RecordTypeMapping getRecordTypeMapping(Context context, Gson gson, String objectType, String recordTypeId) {

        RecordTypeMapping recordTypeMapping = null;

        ObjectLayouts objectLayouts = MetaDataProvider.getMetaDataForLayouts(context, gson, objectType);

        if (objectLayouts != null) {
            List<RecordTypeMapping> recordTypeMappingList = objectLayouts.getRecordTypeMappings();
            // number of RecordTypeMapping are typically small so ok to loop rather than cache
            for (RecordTypeMapping recordTypeMappingItem : recordTypeMappingList) {
                if (recordTypeMappingItem.getRecordTypeId().equals(recordTypeId)) {
                    recordTypeMapping = recordTypeMappingItem;
                    break;
                }

                // if none is specified then pick the one that is default
                if (TextUtils.isEmpty(recordTypeId)) {
                    if (recordTypeMappingItem.getDefaultRecordTypeMapping()) {
                        recordTypeMapping = recordTypeMappingItem;
                        break;
                    }
                }
            }

            if (recordTypeMapping == null) {
                return null;
            }
        }

        return recordTypeMapping;
    }

    public static List<RecordTypeMapping> getRecordTypeMappings(String objectType) {

        List<RecordTypeMapping> recordTypeMappings = new ArrayList<>();
        Gson gson = new Gson();
        ObjectLayouts objectLayouts = MetaDataProvider.getMetaDataForLayouts(ABInBevApp.getAppContext(), gson, objectType);

        if (objectLayouts != null) {
            List<RecordTypeMapping> recordTypeMappingList = objectLayouts.getRecordTypeMappings();
            for (RecordTypeMapping recordTypeMappingItem : recordTypeMappingList) {
                recordTypeMappings.add(recordTypeMappingItem);
            }
        }

        return recordTypeMappings;
    }

    public static HashMap<String, List<PicklistValue>> getPicklistValues(String objectType, String recordTypeId, String... fieldNames) {

        HashMap<String, List<PicklistValue>> map = new HashMap<>();

        Map detailsMap;

        Gson gson = new Gson();

        RecordTypeMapping recordTypeMapping = PicklistUtils.getRecordTypeMapping(ABInBevApp.getAppContext(), gson, objectType, recordTypeId);

        if (recordTypeMapping == null) {
            Log.v(TAG, "no recordTypeMapping available for RecordType");
            return map;
        }

        Log.v(TAG, recordTypeMapping.getRecordTypeId() + ":" + recordTypeMapping.getName() + " Available: " + recordTypeMapping.getAvailable());

        IndividualLayouts individualLayouts = MetaDataProvider.getMetaDataForIndividualLayout(ABInBevApp.getAppContext(), gson, objectType, recordTypeMapping.getRecordTypeId());

        if (individualLayouts == null) {
            Log.v(TAG, "no individualLayouts available for RecordType");
            return map;
        }
        List<EditLayoutSection> editLayoutSections = individualLayouts.getEditLayoutSections();

        if (editLayoutSections == null) {
            Log.v(TAG, "no editLayoutSections available for RecordType");
            return map;
        }

        detailsMap = PicklistUtils.buildDetailsMap(editLayoutSections);

        for (String fieldName : fieldNames) {
            String namespacedfieldName = ManifestUtils.getNamespaceSupportedFieldName(objectType, fieldName, ABInBevApp.getAppContext());
            Details field = (Details) detailsMap.get(namespacedfieldName);
            if (field != null) {
                List<PicklistValue> picklistValueList = field.getPicklistValues();
                List<PicklistValue> validPickListValues =  new ArrayList<>();
                for (PicklistValue picklistValue : picklistValueList) {
                    if (picklistValue.getActive()) validPickListValues.add(picklistValue);
                }

                map.put(fieldName, validPickListValues);
            }
        }

        return map;

    }

    public static HashMap<String, List<PicklistValue>> getMetadataPicklistValues(String objectType, String... fieldNames) {
        if (TextUtils.isEmpty(objectType)) return new HashMap<>();
        if (fieldNames == null || fieldNames.length == 0) return new HashMap<>();

        Context context = ABInBevApp.getAppContext();
        HashMap<String, List<PicklistValue>> picklistsMap = new HashMap<>();

        // Get all fields from metadata.
        DescribeSObjectResult describeSObjectResult = MetaDataProvider.getMetaData(context, new Gson(), objectType);
        if (describeSObjectResult != null) {
            List<Field> fields = describeSObjectResult.getFields();              // Maps field name to list of PicklistValues.
            HashMap<String, String> namespacedFieldMap = new HashMap<>();                       // Maps namespaced to normal name.

            // Prepare collection of field names.
            for (String fieldName : fieldNames) {
                String namespacedFieldName = ManifestUtils.getNamespaceSupportedFieldName(objectType, fieldName, context);
                namespacedFieldMap.put(namespacedFieldName, fieldName);
            }

            // Add fields with matching name to picklists map.
            for (Field field : fields) {
                String fieldName = namespacedFieldMap.get(field.getName());
                if (fieldName != null) {
                    picklistsMap.put(fieldName, new ArrayList<>(field.getPicklistValues()));
                }
            }
        }

        return picklistsMap;
    }

    // TODO: rearrange the code to be more efficient
    public static List<PicklistValue> getPicklistDependentValues(String objectType, String recordTypeId, String fieldName, String controllerName, String controllerValue) {

        HashMap<String, List<PicklistValue>> map = new HashMap<>();

        Map detailsMap;

        ArrayList<PicklistValue> emptyList = new ArrayList<PicklistValue>();

        Gson gson = new Gson();

        String nameSpacedFieldName = ManifestUtils.getNamespaceSupportedFieldName(objectType, fieldName, ABInBevApp.getAppContext());
        String nameSpacedControllerName = ManifestUtils.getNamespaceSupportedFieldName(objectType, controllerName, ABInBevApp.getAppContext());

        RecordTypeMapping recordTypeMapping = PicklistUtils.getRecordTypeMapping(ABInBevApp.getAppContext(), gson, objectType, recordTypeId);

        if (recordTypeMapping == null) {
            Log.v(TAG, "no recordTypeMapping available for RecordType");
            return emptyList;
        }

        Log.v(TAG, recordTypeMapping.getRecordTypeId() + ":" + recordTypeMapping.getName() + " Available: " + recordTypeMapping.getAvailable());

        IndividualLayouts individualLayouts = MetaDataProvider.getMetaDataForIndividualLayout(ABInBevApp.getAppContext(), gson, objectType, recordTypeMapping.getRecordTypeId());

        if (individualLayouts == null) {
            Log.v(TAG, "no individualLayouts available for RecordType");
            return emptyList;
        }
        List<EditLayoutSection> editLayoutSections = individualLayouts.getEditLayoutSections();

        if (editLayoutSections == null) {
            Log.v(TAG, "no editLayoutSections available for RecordType");
            return emptyList;
        }

        detailsMap = PicklistUtils.buildDetailsMap(editLayoutSections);

        Details field = (Details) detailsMap.get(nameSpacedFieldName);

        List<PicklistValue> picklistValueList = field.getPicklistValues();

        PicklistDependencyHolder picklistDependencyHolder = PicklistUtils.getPicklistDependencyHolder(detailsMap);

        if (controllerValue == null || controllerValue.equals("")) {
            Log.v(TAG, "controllerValue null for field: " + fieldName);
            return emptyList;
        }

        FieldValueObject fieldValueObject = new FieldValueObject(nameSpacedControllerName, controllerValue);
        List<FieldValueObject> availableFieldValueObjects = picklistDependencyHolder.get(fieldValueObject);

        if (availableFieldValueObjects == null) {
            Log.v(TAG, "availableFieldValueObjects null for field: " + fieldName);
            return emptyList;
        } else {
            Log.v(TAG, "availableFieldValueObjects size is: " + availableFieldValueObjects.size());
        }

        HashSet<String> validValues = new HashSet<String>();

        for (FieldValueObject item : availableFieldValueObjects ) {
            if (item.getField().equals(nameSpacedFieldName)) {
                validValues.add(item.getFieldValue());
            }
        }

        if (field.getDependentPicklist() == false) {
            List<PicklistValue> validPickListValues = new ArrayList<>();
            for (PicklistValue picklistValue : picklistValueList) {
                if (picklistValue.getActive()) validPickListValues.add(picklistValue);
            }

            return validPickListValues;
        } else {
            List<PicklistValue> validPickListValues = new ArrayList<>();
            for (PicklistValue picklistValue : picklistValueList) {
                if (validValues.contains(picklistValue.getValue())) {
                    if (picklistValue.getActive())
                        validPickListValues.add(picklistValue);
                }
            }
            return validPickListValues;
        }

    }

    public static boolean hasValueInMultipicklist(String value, String multipicklistData) {
        return !TextUtils.isEmpty(value)
                && !TextUtils.isEmpty(multipicklistData)
                // Regex to find value in names divided by semicolons.
                && Pattern.compile("(^|;)" + Pattern.quote(value) + "(;|$)")
                    .matcher(multipicklistData)
                    .find();
    }
}
