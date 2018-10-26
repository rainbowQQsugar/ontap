package com.abinbev.dsa.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.PicklistUtils;
import com.google.gson.Gson;
import com.salesforce.androidsyncengine.data.layouts.DetailLayoutSection;
import com.salesforce.androidsyncengine.data.layouts.Details;
import com.salesforce.androidsyncengine.data.layouts.IndividualLayouts;
import com.salesforce.androidsyncengine.data.layouts.LayoutComponent;
import com.salesforce.androidsyncengine.data.layouts.LayoutItem;
import com.salesforce.androidsyncengine.data.layouts.LayoutRow;
import com.salesforce.androidsyncengine.data.layouts.ObjectLayouts;
import com.salesforce.androidsyncengine.data.layouts.RecordTypeMapping;
import com.salesforce.androidsyncengine.data.model.PicklistValue;
import com.salesforce.androidsyncengine.datamanager.MetaDataProvider;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.Address;
import com.salesforce.dsa.data.model.SFBaseObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import butterknife.Bind;

public class DynamicViewActivity extends AppBaseActivity {

    private static final String TAG = "DynamicViewActivity";

    private static SimpleDateFormat dateFormat;
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat();
    private static final SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final SimpleDateFormat serverDateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.main_layout)
    LinearLayout mainLayout;

    protected SFBaseObject baseObject;

    private String currencySymbol;

    private String notAvailableValue;

    private List<String> filterFields;

    private Map detailsMap;

    private String objectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // TODO: Compute this based on object or user??
        // Fix this
        currencySymbol = "$";

        notAvailableValue = getString(R.string.not_available);

        // we want to show only the date ...
        dateFormat = (SimpleDateFormat) android.text.format.DateFormat.getDateFormat(this);

        String pattern1 = ((SimpleDateFormat) dateFormat).toLocalizedPattern();
        String pattern2 = ((SimpleDateFormat) dateTimeFormat).toLocalizedPattern();

        Log.e("Babu", "pattern1 : " + pattern1 + " pattern2: " + pattern2);

        filterFields = new ArrayList<>();

    }

    @Override
    public int getLayoutResId() {
        return R.layout.dynamic_main;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected List<String> filterFields() {
        return new ArrayList<>();
    }

    protected void buildLayout(String objectType, TranslatableSFBaseObject baseObject) {

        TranslatableSFBaseObject.addRecordTypeTranslations(Arrays.asList(baseObject), objectType, TranslatableSFBaseObject.FIELD_RECORD_NAME);

        objectName = objectType;
        LayoutInflater layoutInflater = getLayoutInflater();
        try {

            mainLayout.removeAllViews();

            // String objectType = "Account";
            Gson gson = new Gson(); // use Builder

            LinearLayout.LayoutParams rowItemLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            int rowItemPadding = getResources().getDimensionPixelOffset(R.dimen.space_small);

            String recordTypeId = baseObject.getStringValueForKey("RecordTypeId");

            Log.i(TAG, "Name: " + baseObject.getName() + " : " + baseObject.getId());
            Log.i(TAG, "RecordTypeId: " + recordTypeId);

            ObjectLayouts objectLayouts = MetaDataProvider.getMetaDataForLayouts(this, gson, objectType);

            RecordTypeMapping recordTypeMapping = null;

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
                    Toast.makeText(this, R.string.no_record_type_mapping, Toast.LENGTH_LONG).show();
                    return;
                }
            }

            // Unnecessary but this should free up some memory
            objectLayouts = null;

            // Let us ignore the available value for now
            Log.i(TAG, recordTypeMapping.getRecordTypeId() + ":" + recordTypeMapping.getName() + " Available: " + recordTypeMapping.getAvailable());

            IndividualLayouts individualLayouts = MetaDataProvider.getMetaDataForIndividualLayout(this, gson, objectType, recordTypeMapping.getRecordTypeId());

            List<DetailLayoutSection> detailLayoutSections = individualLayouts.getDetailLayoutSections();
            int size = detailLayoutSections.size();

            detailsMap = PicklistUtils.buildDetailsMapForView(detailLayoutSections);

            for (DetailLayoutSection detailLayoutSection : detailLayoutSections) {
                detailLayoutSection.getRows();
                detailLayoutSection.getColumns();
                detailLayoutSection.getHeading();
                detailLayoutSection.getUseHeading();

                Log.i(TAG, ">>> Section: " + detailLayoutSection.getHeading() + "<<<");

                if (detailLayoutSection.getUseHeading() == true) {

                    TextView sectionHeader = (TextView) layoutInflater.inflate(R.layout.dynamic_section_header, null);
                    sectionHeader.setText(detailLayoutSection.getHeading());
                    mainLayout.addView(sectionHeader);


                }

                List<LayoutRow> layoutRows = detailLayoutSection.getLayoutRows();
                int rowId = 1;
                for (LayoutRow layoutRow : layoutRows) {

                    LinearLayout rowItem = (LinearLayout) layoutInflater.inflate(R.layout.dynamic_row, null);

                    TextView leftLabel = (TextView) rowItem.findViewById(R.id.left_label);
                    TextView leftValue = (TextView) rowItem.findViewById(R.id.left_value);
                    TextView rightLabel = (TextView) rowItem.findViewById(R.id.right_label);
                    TextView rightValue = (TextView) rowItem.findViewById(R.id.right_value);

                    // If there is data they will be shown later in code.
                    leftLabel.setVisibility(View.GONE);
                    leftValue.setVisibility(View.GONE);
                    rightLabel.setVisibility(View.GONE);
                    rightValue.setVisibility(View.GONE);

                    Log.v(TAG, "> Row " + rowId);
                    rowId++;
                    List<LayoutItem> layoutItems = layoutRow.getLayoutItems();
                    int column = 0;
                    for (LayoutItem layoutItem : layoutItems) {
                        layoutItem.getLabel();
                        layoutItem.getEditableForUpdate();
                        layoutItem.getEditableForNew();
                        layoutItem.getRequired();

                        List<LayoutComponent> layoutComponents = layoutItem.getLayoutComponents();
                        for (LayoutComponent layoutComponent : layoutComponents) {
                            layoutComponent.getType();
                            layoutComponent.getValue();

                            Details details = layoutComponent.getDetails();

                            if (details != null) {
                                String valueString = null;
                                String detailsNameString = details.getName();
                                // if prefix is present in the key then use a stripped version
                                detailsNameString = ManifestUtils.removeNamespaceFromField(objectType, detailsNameString, this);
                                if (filterFields().contains(detailsNameString)) {
                                    continue;
                                }
                                if ("reference".equalsIgnoreCase(details.getType())) {
                                    List<String> listReference = details.getReferenceTo();
                                    if (listReference.isEmpty()) {
                                        Log.e(TAG, "Details: " + details.getLabel() + "\n" + "** No Reference **");
                                    } else {

                                        if (listReference.size() > 1) {
                                            Log.v(TAG, "reference size is > 1");
                                            Log.v(TAG, detailsNameString + " list reference: " + listReference);
                                        }

                                        for (String referenceString : listReference) {
                                            if (detailsNameString.equals("RecordTypeId")) {
                                                valueString = baseObject.getTranslatedRecordName();
                                            } else {
                                                String referencedObjectType = ManifestUtils.removeNamespaceFromObject(referenceString, this);
                                                String referencedObjectFieldName = getLookupFieldName(detailsNameString, referencedObjectType);
                                                valueString = baseObject.getReferencedValueObjectField(referencedObjectType, detailsNameString, referencedObjectFieldName);
                                            }
                                            Log.v(TAG, "reference: " + referenceString + ", " + detailsNameString + " : " + valueString);
                                            if (valueString != null) break;
                                        }
                                    }

                                } else if ("VisualforcePage".equalsIgnoreCase(details.getType())) {
                                    // this is not really in the details block but ...
                                    Log.v(TAG, "skipping VisualforcePage for: " + detailsNameString);
                                } else if ("address".equalsIgnoreCase(details.getType())) {
                                    Address address = baseObject.getAddress(detailsNameString);
                                    valueString = address == null ? notAvailableValue : address.getPrintableAddress();
                                } else {
                                    if (details.getHtmlFormatted()) {
                                        valueString = notAvailableValue;
                                    } else {
                                        valueString = baseObject.getStringValueForKey(detailsNameString);
                                    }
                                }

                                Log.v(TAG, "Details: " + detailsNameString + ": " + valueString +  " : " + details.getType());

                                String labelString;
                                if (layoutItem.getLabel() != null) {
                                    labelString = layoutItem.getLabel();
                                } else {
                                    labelString = "";
                                }

                                if (valueString == null || ("null".equals(valueString))) {
                                    valueString = "";
                                } else {
                                    if ("currency".equalsIgnoreCase(details.getType())) {
                                        try {
                                            valueString = currencySymbol + NumberFormat.getInstance().format(Long.parseLong(valueString));
                                        } catch (Exception e) {
                                            Log.e(TAG, "got exception while parsing currency. Ignoring it!");
                                        }
                                    } else if ("datetime".equalsIgnoreCase(details.getType())) {
                                        try {
                                            // setting this value only once in onCreate does not seem to work
                                            serverDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                                            Date serverDate = serverDateFormat.parse(valueString);
                                            String dateString = dateTimeFormat.format(serverDate);
                                            valueString = dateString;
                                        } catch (Exception e) {
                                            Log.e(TAG, "got exception while parsing datetime. Ignoring it!");
                                        }
                                    } else if("date".equalsIgnoreCase(details.getType())) {
                                        try {
                                            // valueString = DateUtils.formatDateStringShort(valueString);
                                            valueString = dateFormat.format(serverDateOnlyFormat.parse(valueString));
                                        } catch (Exception e) {
                                            Log.e(TAG, "got exception while parsing date. Ignoring it!");
                                        }
                                    } else if ("picklist".equalsIgnoreCase(details.getType())) {
                                        // get picklist label from pick list value
                                        Log.e("Babu", "calculating for : " + details.getName() + " value: " + valueString);
                                        valueString = getPickListLabelForValue(details.getName(), valueString);

                                    }
                                }

                                boolean replaceView = false;
                                // this should move to a function that gets a view based on
                                // the type
                                View newView = null;

                                if ("boolean".equalsIgnoreCase(details.getType())) {
                                    CheckBox checkBox = new CheckBox(this);
                                    checkBox.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                                    checkBox.setPadding(rowItemPadding, 0, rowItemPadding, 0);
                                    checkBox.setLayoutParams(rowItemLayoutParam);

                                    if ("false".equalsIgnoreCase(valueString)) {
                                        checkBox.setChecked(false);
                                    } else if ("true".equalsIgnoreCase(valueString)) {
                                        checkBox.setChecked(true);
                                    } else {
                                        checkBox.setChecked(false);
                                    }

                                    checkBox.setEnabled(false);

                                    newView = checkBox;
                                    replaceView = true;
                                }

                                if (column == 0) {
                                    leftLabel.setText(labelString);
                                    leftLabel.setVisibility((View.VISIBLE));
                                    if (replaceView) {
                                        rowItem.removeView(leftValue);
                                        rowItem.addView(newView, 1);
                                    } else {
                                        leftValue.append(valueString);
                                        leftValue.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    rightLabel.setText(labelString);
                                    rightLabel.setVisibility((View.VISIBLE));
                                    if (replaceView) {
                                        rowItem.removeView(rightValue);
                                        rowItem.addView(newView);
                                    } else {
                                        rightValue.append(valueString);
                                        rightValue.setVisibility(View.VISIBLE);
                                    }
                                }
                            } else {

                                if ("Separator".equals(layoutComponent.getType())) {
                                    if (column < 1) {
                                        leftValue.append(layoutComponent.getValue());
                                    } else {
                                        rightValue.append(layoutComponent.getValue());
                                    }
                                }
                            }
                        }
                        column++;
                    }
                    mainLayout.addView(rowItem);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private String getPickListLabelForValue(String fieldName, String fieldValue) {
        Details field = (Details) detailsMap.get(nameSpacedFieldName(fieldName));

        if (field == null) {
            Log.e(TAG, "null value for getPickListLabels for fieldName: " + fieldName);
            field = (Details) detailsMap.get(fieldName);
            if (field == null) {
                return fieldValue;
            }
        }

        List<PicklistValue> picklistValueList = field.getPicklistValues();

        for (PicklistValue picklistValue : picklistValueList) {
            if (fieldValue.equals(picklistValue.getValue())) {
                return picklistValue.getLabel();
            }
        }

        return fieldValue;
    }

    // this has a performance hit compared to the previous method but this is correct and ca
    protected String nameSpacedFieldName(String fieldName) {
        if (objectName == null) return fieldName;
        else {
            String nameSpacedFieldName = ManifestUtils.getNamespaceSupportedFieldName(objectName, fieldName, this);
            return nameSpacedFieldName;
        }
    }


    protected String getLookupFieldName(String fieldName, String referredObjectType) {
        if (AbInBevConstants.AbInBevObjects.PRODUCT.equals(referredObjectType)) {
            return AbInBevConstants.ProductFields.PRODUCT_NAME;
        }
        else {
            return SyncEngineConstants.StdFields.NAME;
        }
    }

}
