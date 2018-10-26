package com.abinbev.dsa.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.amap.api.maps2d.model.Marker;
import com.google.gson.Gson;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.RecordType;
import com.salesforce.androidsyncengine.data.layouts.Details;
import com.salesforce.androidsyncengine.data.layouts.EditLayoutSection;
import com.salesforce.androidsyncengine.data.layouts.IndividualLayouts;
import com.salesforce.androidsyncengine.data.layouts.LayoutComponent;
import com.salesforce.androidsyncengine.data.layouts.LayoutItem;
import com.salesforce.androidsyncengine.data.layouts.LayoutRow;
import com.salesforce.androidsyncengine.data.layouts.ObjectLayouts;
import com.salesforce.androidsyncengine.data.layouts.RecordTypeMapping;
import com.salesforce.androidsyncengine.data.model.PicklistDependencyHolder;
import com.salesforce.androidsyncengine.data.model.PicklistValue;
import com.salesforce.androidsyncengine.datamanager.MetaDataProvider;
import com.salesforce.androidsyncengine.syncmanifest.FieldValueObject;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by lukaszwalukiewicz on 14.01.2016.
 */
public class MetadataPicklistHelper implements AdapterView.OnItemSelectedListener {
    public static final String TAG = MetadataPicklistHelper.class.getSimpleName();
    private PicklistDependencyHolder picklistDependencyHolder;
    private JSONObject updatedJSONObject;
    private Map<String, Integer> viewHashMap;
    private Map detailsMap;
    private SFBaseObject baseObject;
    private Context context;
    private String noneLabel;

    public MetadataPicklistHelper(String objectType, SFBaseObject baseObject, Context context) {
        this.updatedJSONObject = new JSONObject();
        this.viewHashMap = new HashMap<String, Integer>();
        this.detailsMap = new HashMap();
        this.context = context;
        this.baseObject = baseObject;
        this.noneLabel = context.getResources().getString(R.string.picklist_none);
        setup(objectType, baseObject);
    }

    private void setup(String objectType, SFBaseObject baseObject) {
        Gson gson = new Gson();
        RecordTypeMapping recordTypeMapping = getRecordTypeMapping(objectType, gson, baseObject);
        if (recordTypeMapping != null) {
            IndividualLayouts individualLayouts = MetaDataProvider.getMetaDataForIndividualLayout(context, gson, objectType, recordTypeMapping.getRecordTypeId());
            List<EditLayoutSection> editLayoutSections = individualLayouts.getEditLayoutSections();
            buildDetailsMap(editLayoutSections);
        }
        savePickListDataUsingDetailsMap();
    }

    private RecordTypeMapping getRecordTypeMapping(String objectType, Gson gson, SFBaseObject baseObject) {
        String accountRecordTypeId = null;
        if (baseObject != null) {
            accountRecordTypeId = baseObject.getStringValueForKey(AbInBevConstants.TaskFields.RECORD_TYPE_ID);
        }
        if (accountRecordTypeId == null) {
            accountRecordTypeId = RecordType.getDefaultRecordTypeId(context, objectType);
        }
        ObjectLayouts objectLayouts = MetaDataProvider.getMetaDataForLayouts(context, gson, objectType);
        if (objectLayouts != null) {
            List<RecordTypeMapping> recordTypeMappingList = objectLayouts.getRecordTypeMappings();
            for (RecordTypeMapping recordTypeMappingItem : recordTypeMappingList) {
                if (recordTypeMappingItem.getRecordTypeId().equals(accountRecordTypeId)) {
                    return recordTypeMappingItem;
                }
            }
        }
        Log.e(TAG, "got null recordTypeMapping");
        return null;
    }

    private void buildDetailsMap(List<EditLayoutSection> editLayoutSections) {
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
    }

    public Map<String, String> getPickListMapValues(String fieldName) {
        Map<String, String> map = new HashMap<>();
        Details field = (Details) detailsMap.get(fieldName);
        // Investigate root cause of this; there might be valid reasons for this
        // Example a certain RecordType may not have this field
        if (field == null) {
            Log.e(TAG, "in getPickListValues got null value for: " + fieldName);
            return map;
        }

        List<PicklistValue> picklistValueList = field.getPicklistValues();
        if (field.getDependentPicklist() == false) {
            for (PicklistValue picklistValue : picklistValueList) {
                if (picklistValue.getActive())
                    map.put(picklistValue.getLabel(), picklistValue.getValue());
            }
        }
        return map;
    }

    public List<String> getPickListValues(String fieldName) {
        List<String> validValues = new ArrayList<String>();
        Details field = (Details) detailsMap.get(fieldName);
        // Investigate root cause of this; there might be valid reasons for this
        // Example a certain RecordType may not have this field
        if (field == null) {
            Log.e(TAG, "in getPickListValues got null value for: " + fieldName);
            return validValues;
        }

        List<PicklistValue> picklistValueList = field.getPicklistValues();
        if (field.getDependentPicklist() == false) {
            for (PicklistValue picklistValue : picklistValueList) {
                if (picklistValue.getActive())
                    validValues.add(picklistValue.getValue());
            }
        }
        return validValues;
    }

    public List<String> getPickListLabels(String fieldName) {
        List<String> validLabels = new ArrayList<String>();
        Details field = (Details) detailsMap.get(fieldName);
        // Investigate root cause of this; there might be valid reasons for this
        // Example a certain RecordType may not have this field
        if (field == null) {
            Log.e(TAG, "in getPickListLabels got null value for: " + fieldName);
            return validLabels;
        }

        List<PicklistValue> picklistValueList = field.getPicklistValues();
        if (field.getDependentPicklist() == false) {
            for (PicklistValue picklistValue : picklistValueList) {
                if (picklistValue.getActive())
                    validLabels.add(picklistValue.getLabel());
            }
            return validLabels;
            // use value to store and use label to display
        } else {
            // get controller
            Details controller = (Details) detailsMap.get(field.getControllerName());

            if (controller == null) {
                Log.e(TAG, "controller is null for field: " + field.getName());
                return new ArrayList<String>();
            }
            // get controller current value and get associated label
            String controllerValue = getCurrentPicklistValueForFieldName(controller.getName());

            if (controllerValue == null || controllerValue.equals("")) {
                Log.e(TAG, "controllerValue null for field: " + field.getName());
                return new ArrayList<String>();
            }

            FieldValueObject fieldValueObject = new FieldValueObject(controller.getName(), controllerValue);
            List<FieldValueObject> availableFieldValueObjects = picklistDependencyHolder.get(fieldValueObject);

            if (availableFieldValueObjects == null) {
                Log.e(TAG, "availableFieldValueObjects null for field: " + field.getName());
                return new ArrayList<String>();
            }

            HashSet<String> validValues = new HashSet<String>();

            for (FieldValueObject item : availableFieldValueObjects) {
                if (item.getField().equals(field.getName())) {
                    validValues.add(item.getFieldValue());
                }
            }

            for (PicklistValue picklistValue : picklistValueList) {
                if (validValues.contains(picklistValue.getValue())) {
                    if (picklistValue.getActive())
                        validLabels.add(picklistValue.getLabel());
                }
            }

            return validLabels;
        }
    }

    private int getSelectedIndex(List<String> values, ArrayAdapter adapter, String parameterName) {
        int index = 0;
        if (baseObject != null) {
            JSONObject jsonObject = baseObject.toJson();
            String selectedValue = null;
            try {
                selectedValue = jsonObject.getString(parameterName);
            } catch (Exception e) {
                Log.e(TAG, "Error getting parameter: ", e);
            }
            List<String> labels;
            if (selectedValue != null) {
                for (String value : values) {
                    if (selectedValue.equalsIgnoreCase(value)) {
                        index = adapter.getPosition(value);
                        return index;
                    }
                }
            }
        }
        return index;
    }
    public int setupSpinnerWithoutNoneLabelValues(Spinner spinner, String parameterName, AdapterView.OnItemSelectedListener onItemSelectedListener) {
        List<String> labels = getPickListLabels(parameterName);
        List<String> values = getPickListValues(parameterName);
        final ArrayAdapter<String> spinnerLabelAdapter = getSpinnerArrayAdapter(labels);
        final ArrayAdapter<String> spinnerValuesAdapter = getSpinnerArrayAdapter(values);
        if (labels.size() == 0) {
            spinner.setEnabled(false);
        }
        int currentSelection = getSelectedIndex(values, spinnerValuesAdapter, parameterName);
        spinner.setAdapter(spinnerLabelAdapter);
        spinner.setSelection(currentSelection, false);
        if (onItemSelectedListener != null) {
            spinner.setOnItemSelectedListener(onItemSelectedListener);
        } else {
            spinner.setOnItemSelectedListener(this);
        }
        spinner.setTag(parameterName);
        int viewId = View.generateViewId();
        spinner.setId(viewId);
        viewHashMap.put(parameterName, viewId);
        return currentSelection;
    }

    public int setupSpinnerWithValues(Spinner spinner, String parameterName, AdapterView.OnItemSelectedListener onItemSelectedListener) {
        List<String> labels = getPickListLabels(parameterName);
        List<String> values = getPickListValues(parameterName);
        labels.add(0, noneLabel);
        values.add(0, noneLabel);
        final ArrayAdapter<String> spinnerLabelAdapter = getSpinnerArrayAdapter(labels);
        final ArrayAdapter<String> spinnerValuesAdapter = getSpinnerArrayAdapter(values);
        if (labels.size() == 1) {
            spinner.setEnabled(false);
        }
        int currentSelection = getSelectedIndex(values, spinnerValuesAdapter, parameterName);
        spinner.setAdapter(spinnerLabelAdapter);
        spinner.setSelection(currentSelection, false);
        if (onItemSelectedListener != null) {
            spinner.setOnItemSelectedListener(onItemSelectedListener);
        } else {
            spinner.setOnItemSelectedListener(this);
        }
        spinner.setTag(parameterName);
        int viewId = View.generateViewId();
        spinner.setId(viewId);
        viewHashMap.put(parameterName, viewId);
        return currentSelection;
    }

    public void updateDependentPickLists(String fieldName, String fieldCurrentValue) {
        for (Object value : detailsMap.values()) {
            Details field = (Details) value;
            if (fieldName.equals(field.getControllerName())) {
                List<String> pickListLabels = getPickListLabels(field.getName());
                Spinner spinner = (Spinner) ((Activity) context).getWindow().getDecorView().findViewById(viewHashMap.get(field.getName()));
                if (pickListLabels.size() == 0) {
                    spinner.setEnabled(false);
                } else {
                    spinner.setEnabled(true);
                }
                int defaultSelection = 0;
                if (pickListLabels.size() == 1) {
                    defaultSelection = 1;
                }
                pickListLabels.add(0, noneLabel);
                final ArrayAdapter<String> adapter = getSpinnerArrayAdapter(pickListLabels);
                spinner.setAdapter(adapter);
                if (fieldCurrentValue == null) {
                    spinner.setSelection(0, true);
                } else {
                    String valueString = getCurrentPicklistValueForFieldName(field.getName());
                    if (valueString != null && !valueString.equals("")) {
                        int currentSelection = adapter.getPosition(valueString);
                        if (currentSelection == -1) {
                            spinner.setSelection(defaultSelection, true);
                        } else {
                            spinner.setSelection(currentSelection, true);
                        }
                    } else {
                        spinner.setSelection(defaultSelection, true);
                    }
                }
            }
        }
    }

    private ArrayAdapter<String> getSpinnerArrayAdapter(List<String> values) {
        return new ArrayAdapter<String>(context, R.layout.twoline_spinner_item, android.R.id.text1, values) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                TextView textView = (TextView) inflater.inflate(R.layout.dropdown_text_item, parent, false);
                String value = getItem(position);
                textView.setText(value);
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    view = inflater.inflate(R.layout.twoline_spinner_item, parent, false);
                }
                String value = getItem(position);
                ((TextView) view.findViewById(android.R.id.text1)).setText(value);
                return view;
            }
        };
    }

    private String getCurrentPicklistValueForFieldName(String fieldName) {
        String currentValue = null;
        currentValue = updatedJSONObject.optString(fieldName, null);
        if (currentValue == null && baseObject != null) {
            currentValue = baseObject.getStringValueForKey(fieldName);
        }
        return currentValue;
    }

    private String savePickListValue(String fieldName, String currentLabel) {
        if (currentLabel.equals(noneLabel)) {
            try {
                updatedJSONObject.put(fieldName, JSONObject.NULL);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        Details field = (Details) detailsMap.get(fieldName);
        List<PicklistValue> picklistValueList = field.getPicklistValues();
        String currentValue = null;
        for (PicklistValue picklistValue : picklistValueList) {
            if (currentLabel.equals(picklistValue.getLabel())) {
                currentValue = picklistValue.getValue();
                break;
            }
        }
        try {
            updatedJSONObject.put(fieldName, currentValue);
            return currentValue;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void savePickListDataUsingDetailsMap() {

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
            picklistDependencyHolder = new PicklistDependencyHolder();
            for (Object value : detailsMap.values()) {
                Details field = (Details) value;
                // check whether this is a dependent picklist
                if (field.getDependentPicklist()) {
                    // get the controller by name
                    Details controller = (Details) detailsMap.get(field.getControllerName());
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
                            if ("picklist".equalsIgnoreCase(controller.getType())) {
                                // if the controller is a picklist, list all
                                // controlling values for which this entry is valid

                                List<PicklistValue> controllerPickListValues = controller.getPicklistValues();
                                int controllerPickListSize = controllerPickListValues.size();
                                for (int k = 0; k < validFor.size(); k++) {
                                    if (validFor.testBit(k)) {
                                        // if bit k is set, this entry is valid for the
                                        // for the controlling entry at index k

                                        if (k >= controllerPickListSize) {
                                            //     Log.e(TAG, "problem k > controllerPickListSize : " + k + " > " + controllerPickListSize);
                                        } else {
                                            // System.out.println(controllerPickListValues.get(k).getLabel());
                                            FieldValueObject fieldValueObject = new FieldValueObject(controller.getName(), controllerPickListValues.get(k).getValue());
                                            ArrayList<FieldValueObject> availableValues = (ArrayList<FieldValueObject>) picklistDependencyHolder.get(fieldValueObject);
                                            if (availableValues == null)
                                                availableValues = new ArrayList<FieldValueObject>();
                                            FieldValueObject valueElement = new FieldValueObject(field.getName(), picklistValues.get(j).getValue());
                                            availableValues.add(valueElement);
                                            picklistDependencyHolder.put(fieldValueObject, availableValues);
                                        }
                                    }
                                }
                            } else if ("Boolean".equalsIgnoreCase(controller.getType())) {
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
            //    Log.e(TAG, " " + ce.getMessage());
            ce.printStackTrace();
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String name = (String) parent.getTag();
        String value = (String) parent.getItemAtPosition(pos);
        String currentValue = savePickListValue(name, value);
        updateDependentPickLists(name, currentValue);
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

}
