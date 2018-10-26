package com.abinbev.dsa.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.fragments.AddressDialogFragment;
import com.abinbev.dsa.fragments.ReferencedValuesFragment;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.PicklistUtils;
import com.google.gson.Gson;
import com.salesforce.androidsyncengine.data.layouts.DetailLayoutSection;
import com.salesforce.androidsyncengine.data.layouts.Details;
import com.salesforce.androidsyncengine.data.layouts.EditLayoutSection;
import com.salesforce.androidsyncengine.data.layouts.IndividualLayouts;
import com.salesforce.androidsyncengine.data.layouts.LayoutComponent;
import com.salesforce.androidsyncengine.data.layouts.LayoutItem;
import com.salesforce.androidsyncengine.data.layouts.LayoutRow;
import com.salesforce.androidsyncengine.data.layouts.LayoutSection;
import com.salesforce.androidsyncengine.data.layouts.RecordTypeMapping;
import com.salesforce.androidsyncengine.data.model.PicklistDependencyHolder;
import com.salesforce.androidsyncengine.data.model.PicklistValue;
import com.salesforce.androidsyncengine.datamanager.MetaDataProvider;
import com.salesforce.androidsyncengine.syncmanifest.FieldValueObject;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants.StdFields;
import com.salesforce.dsa.data.model.Address;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import butterknife.Bind;

public class DynamicEditActivity extends AppBaseActivity implements AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener, AddressDialogFragment.OnAddressSavedListener,
        ReferencedValuesFragment.OnReferencedItemSelected {

    private static final String TAG = "DynamicEditActivity";

    public static String VIEW_TYPE_CHECK_BOX = "boolean";
    public static String VIEW_TYPE_REFERENCE = "reference";
    public static String VIEW_TYPE_STRING = "string";
    public static String VIEW_TYPE_URL = "url";
    public static String VIEW_TYPE_DOUBLE = "double";
    public static String VIEW_TYPE_CURRENCY = "currency";
    public static String VIEW_TYPE_PHONE = "phone";
    public static String VIEW_TYPE_EMAIL = "email";
    public static String VIEW_TYPE_DATE = "date";
    public static String VIEW_TYPE_DATETIME = "datetime";
    public static String VIEW_TYPE_TEXTAREA = "textarea";
    public static String VIEW_TYPE_ADDRESS = "address";
    public static String VIEW_TYPE_PICKLIST = "picklist";
    public static String VIEW_TYPE_MULTIPICKLIST = "multipicklist";

    private static SimpleDateFormat dateFormat;
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat();
    private static final SimpleDateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final SimpleDateFormat serverDateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static final String EMPTY_STRING = "";

    private static final String FRAGMENT_REFERENCED_VALUES = "referenced_values";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.main_layout)
    LinearLayout mainLayout;

    protected TranslatableSFBaseObject baseObject;

    LinearLayout.LayoutParams rowItemLayoutParam;
    int rowItemPadding;
    int marginTop;
    int marginBottom;
    int editTextPadding;
    int spinnerPadding;

    private AdapterItem emptyAdapterItem = new AdapterItem("--None--", null);

    //
    private List<EditTextHolder> editTextHolderList;

    private PicklistDependencyHolder picklistDependencyHolder;
    protected JSONObject updatedJSONObject;
    private Map<String, Integer> viewHashMap;
    private Map<String, View> labelHashMap;
    private Map detailsMap;

    protected List<String> requiredFields;

    /**
     * Maps from field name to label.
     */
    protected Map<String, String> fieldLabels = new HashMap<>();

    private String objectName;

    private int asteriskColor;

    @LayoutRes
    private int sectionHeaderLayoutId = R.layout.dynamic_section_header;

    @LayoutRes
    private int editRowLayoutId = R.layout.dynamic_edit_row;

    @LayoutRes
    private int spinnerItemLayoutId = android.R.layout.simple_spinner_item;

    @LayoutRes
    private int spinnerDropdownLayoutId = R.layout.support_simple_spinner_dropdown_item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rowItemLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        rowItemPadding = getResources().getDimensionPixelOffset(R.dimen.space_small);

        // the below values to match Basic Data Activity
        marginBottom = getResources().getDimensionPixelOffset(R.dimen.space1);
        marginTop = getResources().getDimensionPixelOffset(R.dimen.space);
        editTextPadding = getResources().getDimensionPixelOffset(R.dimen.space1);
        spinnerPadding = getResources().getDimensionPixelOffset(R.dimen.space2);

        emptyAdapterItem = new AdapterItem(getString(R.string.picklist_none), null);
        asteriskColor = getResources().getColor(R.color.asterisk);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        editTextHolderList = new ArrayList<EditTextHolder>();

        // we want to show only the date ...
        dateFormat = (SimpleDateFormat) android.text.format.DateFormat.getDateFormat(this);

    }

    public void setSectionHeaderLayout(@LayoutRes int layoutId) {
        this.sectionHeaderLayoutId = layoutId;
    }

    @LayoutRes
    public int getSectionHeaderLayoutId() {
        return this.sectionHeaderLayoutId;
    }

    public void setEditRowLayout(@LayoutRes int layoutId) {
        this.editRowLayoutId = layoutId;
    }

    @LayoutRes
    public int getEditRowLayoutId() {
        return this.editRowLayoutId;
    }

    public void setSpinnerItemLayout(@LayoutRes int layoutId) {
        this.spinnerItemLayoutId = layoutId;
    }

    @LayoutRes
    public int getSpinnerItemLayoutId() {
        return this.spinnerItemLayoutId;
    }

    public void setSpinnerDropdownLayout(@LayoutRes int layoutId) {
        this.spinnerDropdownLayoutId = layoutId;
    }

    @LayoutRes
    public int getSpinnerDropdownLayoutId() {
        return this.spinnerDropdownLayoutId;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.dynamic_main;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isModified()) {
                    askIfClose();
                    return true;
                } else {
                    finish();
                    return true;
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //save the value
        try {
            String fieldName = (String) buttonView.getTag();
            if (buttonView.isChecked()) {
                updatedJSONObject.put(fieldName, true);
            } else {
                updatedJSONObject.put(fieldName, false);
            }
        } catch (JSONException e) {

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        String name = (String) parent.getTag();
        Log.i(TAG, "onItemSelected for field name: " + name);
        String oldValue = baseObject.getStringValueForKey(name);
        AdapterItem adapterItem = (AdapterItem) parent.getItemAtPosition(pos);
        String newValue = adapterItem.getValue();

        if (oldValue != null && oldValue.equals(newValue)) {
            Log.i(TAG, "in oldValue equals newValue block");
            // we could get here when we revert back the value
            String currentValue = savePickListValue(name, newValue);
            updateDependentPickLists(name, currentValue);
            return;
        }

        if (picklistValidator(name, oldValue, newValue)) {
            String currentValue = savePickListValue(name, newValue);
            updateDependentPickLists(name, currentValue);
        } else {
            // set picklist to old value
            ArrayAdapter<AdapterItem> adapter = (ArrayAdapter<AdapterItem>) parent.getAdapter();
            int position = adapter.getPosition(AdapterItem.justValue(oldValue));
            parent.setSelection(position);
        }

    }

    @Override
    public void onBackPressed() {
        if (isModified()) {
            askIfClose();
        } else {
            super.onBackPressed();
        }
    }

    public boolean picklistValidator(String fieldName, String oldValue, String newValue) {
        return true;
    }

    public View getViewByField(String fieldName) {
        View view = null;
        Integer viewId = viewHashMap.get(nameSpaceRemovedFieldName(fieldName));
        if (viewId != null) {
            view = findViewById(viewId);
        }

        return view;
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback

        String name = (String) parent.getTag();

        Log.e(TAG, "onNothingSelected for field name: " + name);

    }

    private boolean isCreate() {
        return baseObject == null || baseObject.isNullOrEmpty(StdFields.ID);
    }

    private boolean isUpdate() {
        return !isCreate();
    }

    protected List<String> filterFields() {
        return new ArrayList<>();
    }

    protected void buildLayout(String objectType, TranslatableSFBaseObject baseObject) {
        buildLayout(objectType, baseObject, false);
    }

    protected void buildLayout(String objectType, TranslatableSFBaseObject baseObject, boolean useDetailsLayout) {

        try {
            this.baseObject = baseObject;
            this.objectName = objectType;

            TranslatableSFBaseObject.addRecordTypeTranslations(Arrays.asList(baseObject), objectType, TranslatableSFBaseObject.FIELD_RECORD_NAME);

            // String objectType = "Account";
            Gson gson = new Gson(); // use Builder

            setupPickListData(gson, objectType);

            String recordTypeId = baseObject.getStringValueForKey("RecordTypeId");

            Log.i(TAG, "Name: " + baseObject.getName() + " : " + baseObject.getId());
            Log.i(TAG, "RecordTypeId: " + recordTypeId);

            RecordTypeMapping recordTypeMapping = PicklistUtils.getRecordTypeMapping(this, gson, objectType, recordTypeId);

            if (recordTypeMapping == null) {
                Toast.makeText(this, R.string.no_record_type_mapping, Toast.LENGTH_LONG).show();
                return;
            }

            // Let us ignore the available value for now
            Log.i(TAG, recordTypeMapping.getRecordTypeId() + ":" + recordTypeMapping.getName() + " Available: " + recordTypeMapping.getAvailable());

            IndividualLayouts individualLayouts = MetaDataProvider.getMetaDataForIndividualLayout(this, gson, objectType, recordTypeMapping.getRecordTypeId());

            List<? extends LayoutSection> layoutSections;

            if (useDetailsLayout) {
                List<DetailLayoutSection> detailLayoutSections = individualLayouts.getDetailLayoutSections();
                detailsMap = PicklistUtils.buildDetailsMapForView(detailLayoutSections);
                layoutSections = detailLayoutSections;
            } else {
                List<EditLayoutSection> editLayoutSections = individualLayouts.getEditLayoutSections();
                detailsMap = PicklistUtils.buildDetailsMap(editLayoutSections);
                layoutSections = editLayoutSections;
            }

            picklistDependencyHolder = PicklistUtils.getPicklistDependencyHolder(detailsMap);

            requiredFields = new ArrayList<>();
            mainLayout.removeAllViews();

            int sectionIndex = 0;
            for (LayoutSection layoutSection : layoutSections) {
                if (acceptLayoutSection(sectionIndex, layoutSection)) {
                    buildLayoutSection(layoutSection, mainLayout);
                }
                sectionIndex++;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage(), e);
        }
    }

    protected boolean testRequiredFields(JSONObject updatedObject) {
        Log.i(TAG, "updated Values: " + updatedObject.toString());
        Log.i(TAG, "required fields: " + requiredFields);

        if (requiredFields == null) {
            // the buildLayout has failed or it is still in progress
            return true;
        }

        for (String fieldName : requiredFields) {
            String currentValue = getCurrentValueForFieldName(updatedObject, fieldName);
            if (currentValue == null || currentValue.trim().isEmpty()) {
                String fieldLabel = fieldLabels.get(fieldName);
                Log.i(TAG, "Missing required field: " + fieldLabel + "(" + fieldName + ")");
                showSnackbar(getString(R.string.missing_required_field) + " : " + fieldLabel);
                return false;
            }
        }

        return true;
    }

    protected String getCurrentValueForFieldName(JSONObject updatedObject, String fieldName) {
        String currentValue;
        try {
            currentValue = updatedObject.getString(fieldName);
            if ("null".equals(currentValue)) return null;
        } catch (JSONException je) {
            if (baseObject.isNullValue(fieldName)) {
                return null;
            } else {
                currentValue = baseObject.getStringValueForKey(fieldName);
            }
        }
        return currentValue;
    }

    protected void showSnackbar(String errorString) {

        final Snackbar snackbar = Snackbar.make(mainLayout, errorString, Snackbar.LENGTH_INDEFINITE);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        if (textView != null) textView.setMaxLines(3);  // show multiple line

        snackbar.setAction(R.string.dismiss, v -> snackbar.dismiss());

        snackbar.show();
    }

    protected void showSnackbar(int errorStringResourceId) {
        showSnackbar(errorStringResourceId, Snackbar.LENGTH_INDEFINITE);
    }

    protected void showSnackbarShort(int errorStringResourceId) {
        showSnackbar(errorStringResourceId, Snackbar.LENGTH_LONG);
    }

    protected void showSnackbar(int errorStringResourceId, int dutarion) {

        final Snackbar snackbar = Snackbar.make(mainLayout, errorStringResourceId, dutarion);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        if (textView != null) textView.setMaxLines(3);  // show multiple line

        snackbar.setAction(R.string.dismiss, v -> snackbar.dismiss());

        snackbar.show();
    }

    protected boolean acceptLayoutSection(int sectionNumber, LayoutSection layoutSection) {
        return true;
    }

    private void buildLayoutSection(LayoutSection editLayoutSection, ViewGroup parentLayout) {
        LayoutInflater layoutInflater = getLayoutInflater();

        Log.i(TAG, ">>> Section: " + editLayoutSection.getHeading() + "<<<");

        if (editLayoutSection.getUseHeading()) {
            TextView sectionHeader = (TextView) layoutInflater.inflate(sectionHeaderLayoutId, null);
            sectionHeader.setText(editLayoutSection.getHeading());
            parentLayout.addView(sectionHeader);
        }

        List<LayoutRow> layoutRows = editLayoutSection.getLayoutRows();
        for (LayoutRow layoutRow : layoutRows) {
            buildLayoutRow(layoutRow, parentLayout, editLayoutSection.getHeading());
        }
    }

    protected boolean isUpdateable(LayoutItem layoutItem, Details details, String fieldName, String section) {
        return isCreate() ? layoutItem.getEditableForNew() : layoutItem.getEditableForUpdate();
    }


    protected boolean isRequired(LayoutItem layoutItem, Details details, String fieldName) {
        return layoutItem.getRequired();
    }

    private void buildLayoutRow(LayoutRow layoutRow, ViewGroup parentLayout, String section) {
        LayoutInflater layoutInflater = getLayoutInflater();
        LinearLayout rowItem = (LinearLayout) layoutInflater.inflate(editRowLayoutId, null);

        TextView leftLabel = (TextView) rowItem.findViewById(R.id.left_label);
        TextView rightLabel = (TextView) rowItem.findViewById(R.id.right_label);

        // If there is data they will be shown later in code.
        leftLabel.setVisibility(View.GONE);
        rightLabel.setVisibility(View.GONE);

        View leftDummyView = rowItem.findViewById(R.id.left_dummy_view);
        View rightDummyView = rowItem.findViewById(R.id.right_dummy_view);

        // If there is data they will be shown later in code.
        leftDummyView.setVisibility(View.GONE);
        rightDummyView.setVisibility(View.GONE);

        List<LayoutItem> layoutItems = layoutRow.getLayoutItems();
        int column = 0;

        boolean gotSeparator = false;
        View newView = null;

        for (LayoutItem layoutItem : layoutItems) {

            List<LayoutComponent> layoutComponents = layoutItem.getLayoutComponents();
            for (LayoutComponent layoutComponent : layoutComponents) {
                Details details = layoutComponent.getDetails();

                if (details != null) {

                    String valueString = null;
                    String detailsNameString = details.getName();
                    // if prefix is present in the key then use a stripped version
                    detailsNameString = ManifestUtils.removeNamespaceFromField(objectName, detailsNameString, this);

                    boolean isUpdateable = isUpdateable(layoutItem, details, detailsNameString, section);
                    boolean isRequired = isRequired(layoutItem, details, detailsNameString);

                    String fieldName = nameSpaceRemovedFieldName(details.getName());
                    if (filterFields().contains(fieldName)) {
                        continue;
                    }
                    CharSequence labelString;
                    if (layoutItem.getLabel() != null) {
                        labelString = layoutItem.getLabel();
                    } else {
                        labelString = "";
                    }

                    if (VIEW_TYPE_REFERENCE.equalsIgnoreCase(details.getType())) {
                        List<String> listReference = details.getReferenceTo();
                        if (listReference.isEmpty()) {
                            Log.e(TAG, "Details: " + details.getLabel() + "\n" + "** No Reference **");
                        } else {
                            if (listReference.size() > 1) {
                                Log.e(TAG, "reference size is > 1");
                            }

                            for (String referenceString : listReference) {
                                if (detailsNameString.equals("RecordTypeId")) {
                                    valueString = baseObject.getTranslatedRecordName();
                                } else {
                                    String referencedObjectType = ManifestUtils.removeNamespaceFromObject(referenceString, this);
                                    String referencedObjectFieldName = getLookupFieldName(VIEW_TYPE_REFERENCE, fieldName, referencedObjectType);
                                    valueString = baseObject.getReferencedValueObjectField(referencedObjectType, detailsNameString, referencedObjectFieldName);
                                }
                                Log.v(TAG, "reference: " + referenceString + ", " + detailsNameString + " : " + valueString);
                                if (valueString != null) break;
                            }
                        }

                    } else if ("VisualforcePage".equalsIgnoreCase(details.getType())) {
                        // this is not really in the details block but ...
                        Log.e(TAG, "skipping VisualforcePage for: " + details.getName());
                    } else {
                        if (details.getHtmlFormatted()) {
                            valueString = "N/A";
                        } else if (Boolean.TRUE.equals(details.getCalculated()) && !TextUtils.isEmpty(details.getCalculatedFormula())) {
                            valueString = baseObject.getRelationalStringValueWithNamespace(getApplicationContext(), details.getCalculatedFormula());
                        } else {
                            valueString = baseObject.getStringValueForKey(nameSpaceRemovedFieldName(details.getName()));
                        }
                    }

                    fieldLabels.put(fieldName, TextUtils.isEmpty(labelString) ? fieldName : String.valueOf(labelString));

                    if (isRequired) {
                        labelString = new SpannableStringBuilder(labelString)
                                .append(" *", new ForegroundColorSpan(asteriskColor), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    if (valueString == null || ("null".equals(valueString))) {
                        valueString = "";
                    } else {
                        if ("currency".equalsIgnoreCase(details.getType())) {
                            try {
                                valueString = NumberFormat.getInstance().format(Long.parseLong(valueString));
                            } catch (Exception e) {
                                Log.e(TAG, "got exception while parsing currency. Ignoring it!");
                            }
                        } else if (VIEW_TYPE_DATE.equalsIgnoreCase(details.getType())) {
                            try {
                                // valueString = DateUtils.formatDateStringShort(valueString);
                                valueString = dateFormat.format(serverDateOnlyFormat.parse(valueString));
                            } catch (Exception e) {
                                Log.e(TAG, "got exception while parsing date. Ignoring it!");
                            }
                        } else if (VIEW_TYPE_DATETIME.equalsIgnoreCase(details.getType())) {
                            try {
                                // setting this value only once in onCreate does not seem to work
                                serverDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                                Date serverDate = serverDateFormat.parse(valueString);
                                String dateString = dateTimeFormat.format(serverDate);
                                valueString = dateString;
                            } catch (Exception e) {
                                Log.e(TAG, "got exception while parsing datetime. Ignoring it!");
                            }
                        }
                    }


                    if (gotSeparator) {
                        if (newView != null) {
                            ((TextView) newView).append(valueString);
                        }
                        gotSeparator = false;
                    } else {
                        newView = getViewForType(details, labelString, valueString, isUpdateable, isRequired, rowItem);

                        if (newView != null) {
                            if (isRequired) {
                                // Required field should have asterisk with different color.
                                requiredFields.add(fieldName);
                            }

                            if (column == 0) {
                                setLayoutComponentLabel(leftLabel, details.getType(), fieldName, labelString, valueString);
                                rowItem.removeView(leftDummyView);
                                rowItem.addView(newView, 1);

                            } else {
                                setLayoutComponentLabel(rightLabel, details.getType(), fieldName, labelString, valueString);
                                rowItem.removeView(rightDummyView);
                                rowItem.addView(newView);
                            }
                        }
                    }
                } else {

                    if ("Separator".equals(layoutComponent.getType())) {
                        gotSeparator = true;

                        if (newView != null) {
                            ((TextView) newView).append(layoutComponent.getValue());
                        }
                    }
                }
            }
            column++;
        }
        parentLayout.addView(rowItem);
    }

    private TextView buildTextView(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        fieldName = nameSpaceRemovedFieldName(fieldName);
        int viewId = View.generateViewId();
        TextView textView = createNewTextView(type, fieldName, isUpdateable, root);
        textView.setId(viewId);
        textView.setTag(fieldName);
        viewHashMap.put(fieldName, viewId);

        return textView;
    }

    protected TextView createNewTextView(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        TextView textView = new TextView(this);
        textView.setGravity(Gravity.LEFT);
        textView.setPadding(rowItemPadding, rowItemPadding, rowItemPadding, rowItemPadding);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(rowItemLayoutParam);
        layoutParams.setMargins(0, 0, 0, 0);
        textView.setLayoutParams(layoutParams);
        textView.setTextAppearance(this, R.style.Text_DynamicField);
        return textView;
    }

    private EditText buildEditText(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        fieldName = nameSpaceRemovedFieldName(fieldName);
        int viewId = View.generateViewId();
        EditText editText = createNewEditText(type, fieldName, isUpdateable, root);
        editText.setId(viewId);
        editText.setTag(fieldName);

        editTextHolderList.add(new EditTextHolder(editText, type));
        viewHashMap.put(fieldName, viewId);
        return editText;
    }

    protected EditText createNewEditText(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        EditText editText = new AppCompatEditText(this);
        editText.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        editText.setPadding(editTextPadding, editTextPadding, editTextPadding, editTextPadding);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(rowItemLayoutParam);
        layoutParams.setMargins(0, marginTop, 0, marginBottom);
        editText.setLayoutParams(layoutParams);
        editText.setBackgroundResource(R.drawable.qty_bg);
        return editText;
    }

    protected TextView createNewDisabledEditText(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        TextView textView = new TextView(this, null, R.attr.editTextStyle);
        textView.setFocusableInTouchMode(false);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        textView.setPadding(editTextPadding, editTextPadding, editTextPadding, editTextPadding);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(rowItemLayoutParam);
        layoutParams.setMargins(0, marginTop, 0, marginBottom);
        textView.setLayoutParams(layoutParams);
        textView.setBackgroundResource(R.drawable.qty_bg);
        return textView;
    }

    private CheckBox buildCheckBox(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        fieldName = nameSpaceRemovedFieldName(fieldName);
        int viewId = View.generateViewId();
        CheckBox checkBox = createNewCheckBox(type, fieldName, isUpdateable, root);
        checkBox.setId(viewId);
        checkBox.setTag(fieldName);
        viewHashMap.put(fieldName, viewId);

        return checkBox;
    }

    protected CheckBox createNewCheckBox(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        checkBox.setPadding(rowItemPadding, 0, rowItemPadding, 0);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(rowItemLayoutParam);
        layoutParams.setMargins(0, 0, 0, 0);
        checkBox.setLayoutParams(layoutParams);
        return checkBox;
    }

    private Spinner buildSpinner(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        fieldName = nameSpaceRemovedFieldName(fieldName);
        int viewId = View.generateViewId();
        Spinner spinner = createNewSpinner(type, fieldName, isUpdateable, root);
        spinner.setId(viewId);
        spinner.setTag(fieldName);
        viewHashMap.put(fieldName, viewId);
        return spinner;
    }

    protected Spinner createNewSpinner(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        Spinner spinner = new AppCompatSpinner(this);
        //spinner.setPadding(spinnerPadding, spinnerPadding, spinnerPadding, spinnerPadding);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(rowItemLayoutParam);
        layoutParams.setMargins(0, marginTop, 0, marginBottom);
        spinner.setLayoutParams(layoutParams);
        return spinner;
    }

    private View buildMultiSelectView(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        fieldName = nameSpaceRemovedFieldName(fieldName);
        int viewId = View.generateViewId();
        View textView = createNewMultiSelectView(type, fieldName, isUpdateable, root);
        textView.setId(viewId);
        textView.setTag(fieldName);
        viewHashMap.put(fieldName, viewId);

        return textView;
    }

    protected View createNewMultiSelectView(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        if (isUpdateable) {
            EditText editText = createNewEditText(type, fieldName, isUpdateable, root);
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_box, 0);
            editText.setFocusable(false);
            return editText;

        } else {
            return createNewTextView(type, fieldName, isUpdateable, root);
        }
    }

    private View buildReferencedValueView(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        fieldName = nameSpaceRemovedFieldName(fieldName);
        int viewId = View.generateViewId();
        View textView = createReferencedValueView(type, fieldName, isUpdateable, root);
        textView.setId(viewId);
        textView.setTag(fieldName);
        viewHashMap.put(fieldName, viewId);

        return textView;
    }

    protected View createReferencedValueView(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        if (isUpdateable) {
            TextView view = createNewDisabledEditText(type, fieldName, isUpdateable, root);
            view.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_box, 0);
            return view;
        } else {
            return createNewTextView(type, fieldName, isUpdateable, root);
        }
    }

    protected void setLayoutComponentData(View contentView, String viewType, String fieldName, CharSequence label, String value) {
        contentView.setVisibility(View.VISIBLE);

        if (VIEW_TYPE_CHECK_BOX.equalsIgnoreCase(viewType)) {
            CheckBox checkBox = (CheckBox) contentView;
            checkBox.setChecked("true".equalsIgnoreCase((String) value));
        } else if (VIEW_TYPE_REFERENCE.equalsIgnoreCase(viewType)) {
            TextView textView = (TextView) contentView;
            textView.setText(value);
        } else if (VIEW_TYPE_STRING.equalsIgnoreCase(viewType)
                || VIEW_TYPE_URL.equalsIgnoreCase(viewType)
                || VIEW_TYPE_DOUBLE.equalsIgnoreCase(viewType)
                || VIEW_TYPE_CURRENCY.equalsIgnoreCase(viewType)
                || VIEW_TYPE_PHONE.equalsIgnoreCase(viewType)
                || VIEW_TYPE_EMAIL.equalsIgnoreCase(viewType)
                || VIEW_TYPE_DATE.equalsIgnoreCase(viewType)
                || VIEW_TYPE_DATETIME.equalsIgnoreCase(viewType)
                || VIEW_TYPE_TEXTAREA.equalsIgnoreCase(viewType)) {

            if (contentView instanceof EditText) {
                EditText editText = (EditText) contentView;
                editText.setText(value);
            } else if (contentView instanceof TextView) {
                TextView textView = (TextView) contentView;
                textView.append(value);
            } else {
                throw new IllegalStateException("Unknown view type: " + contentView);
            }
        } else if (VIEW_TYPE_ADDRESS.equalsIgnoreCase(viewType)) {

            Address address = baseObject.getAddress(fieldName);
            String stringAddress = address == null ? null : address.getPrintableAddress();

            if (contentView instanceof EditText) {
                EditText editText = (EditText) contentView;
                editText.setText(stringAddress);
                editText.setTag(R.id.dynamic_view_address, address);
            } else if (contentView instanceof TextView) {
                TextView textView = (TextView) contentView;
                textView.append(stringAddress);
            } else {
                throw new IllegalStateException("Unknown view type: " + contentView);
            }
        } else if (VIEW_TYPE_PICKLIST.equalsIgnoreCase(viewType)) {
            if (contentView instanceof Spinner) {
                Spinner spinner = (Spinner) contentView;

                if (value != null && !value.equals("")) {
                    int currentSelection = ((ArrayAdapter) spinner.getAdapter()).getPosition(AdapterItem.justValue(value));
                    if (currentSelection == -1) {
                        spinner.setSelection(0, false);
                    } else {
                        spinner.setSelection(currentSelection, false);
                    }
                } else {
                    // this probably happens since it is a new view but ...
                    spinner.setSelection(0, false);
                }
            } else if (contentView instanceof TextView) {
                TextView textView = (TextView) contentView;
                List<AdapterItem> adapterItems = getPickListLabels(fieldName);
                String translateValue = null;
                for (AdapterItem item : adapterItems) {
                    if (TextUtils.equals(value, item.getValue())) {
                        translateValue = item.getLabel();
                    }
                }
                if (translateValue != null) {
                    textView.append(translateValue);
                } else {
                    textView.append(value);
                }
            } else {
                throw new IllegalStateException("Unknown view type: " + contentView);
            }
        } else if (VIEW_TYPE_MULTIPICKLIST.equalsIgnoreCase(viewType)) {
            if (contentView instanceof TextView) {
                TextView textView = (TextView) contentView;
                if (!TextUtils.isEmpty(value)) {
                    value = String.valueOf(value).replace(";", ", ");
                }
                textView.setText(value);
            } else {
                throw new IllegalStateException("Unknown view type: " + contentView);
            }
        }
    }

    protected void setLayoutComponentLabel(View labelView, String viewType, String fieldName, CharSequence label, CharSequence value) {
        TextView textView = (TextView) labelView;
        textView.setVisibility(View.VISIBLE);
        textView.setText(label);
        labelHashMap.put(fieldName, labelView);
    }

    protected View getLayoutComponentLabel(String fieldName) {
        return labelHashMap.get(nameSpaceRemovedFieldName(fieldName));
    }

    protected String getLookupFieldName(String viewType, String fieldName, String referredObjectType) {
        if (AbInBevConstants.AbInBevObjects.PRODUCT.equals(referredObjectType)) {
            return AbInBevConstants.ProductFields.PRODUCT_NAME;
        } else {
            return StdFields.NAME;
        }
    }

    protected String getLookupFilter(String fieldName, String referredObjectType) {
        return null;
    }

    private View getViewForType(Details details, CharSequence labelString, String valueString, boolean updateable, boolean isRequired, ViewGroup root) {
        String viewType = details.getType();
        String fieldName = nameSpaceRemovedFieldName(details.getName());

        if (VIEW_TYPE_CHECK_BOX.equalsIgnoreCase(viewType)) {
            CheckBox checkBox = buildCheckBox(viewType, fieldName, updateable, root);
            setLayoutComponentData(checkBox, viewType, fieldName, labelString, valueString);

            // set based on whether this value is updatable
            if (updateable) {
                checkBox.setEnabled(true);
            } else {
                checkBox.setEnabled(false);
            }

            checkBox.setOnCheckedChangeListener(this);
            return checkBox;
        }

        if (VIEW_TYPE_REFERENCE.equalsIgnoreCase(viewType)) {
            View view = buildReferencedValueView(viewType, fieldName, updateable, root);
            setLayoutComponentData(view, viewType, fieldName, labelString, valueString);
            if (updateable) {
                List<String> references = details.getReferenceTo();
                String referredObjectType = references == null || references.isEmpty() ?
                        null : references.get(0);
                if (referredObjectType != null) {
                    referredObjectType = ManifestUtils.removeNamespaceFromObject(referredObjectType, getApplicationContext());
                }
                String lookupFieldName = getLookupFieldName(viewType, fieldName, referredObjectType);
                view.setOnClickListener(new ReferenceOnClickListener(this, labelString, fieldName,
                        lookupFieldName, referredObjectType));
            }
            return view;
        }

        if (VIEW_TYPE_STRING.equalsIgnoreCase(viewType)) {
            if (updateable) {
                EditText editText = buildEditText(viewType, fieldName, updateable, root);
                setLayoutComponentData(editText, viewType, fieldName, labelString, valueString);
                editText.setLines(2);
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(details.getLength())});
                return editText;

            } else {
                TextView textView = buildTextView(viewType, fieldName, updateable, root);
                setLayoutComponentData(textView, viewType, fieldName, labelString, valueString);
                return textView;
            }
        }

        if (VIEW_TYPE_URL.equalsIgnoreCase(viewType)) {
            if (updateable) {
                EditText editText = buildEditText(viewType, fieldName, updateable, root);
                setLayoutComponentData(editText, viewType, fieldName, labelString, valueString);
                editText.setLines(2);
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(details.getLength())});
                return editText;

            } else {
                TextView textView = buildTextView(viewType, fieldName, updateable, root);
                setLayoutComponentData(textView, viewType, fieldName, labelString, valueString);
                return textView;
            }
        }

        if (VIEW_TYPE_DOUBLE.equalsIgnoreCase(viewType)) {
            if (updateable) {
                EditText editText = buildEditText(viewType, fieldName, updateable, root);
                setLayoutComponentData(editText, viewType, fieldName, labelString, valueString);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                return editText;

            } else {
                TextView textView = buildTextView(viewType, fieldName, updateable, root);
                setLayoutComponentData(textView, viewType, fieldName, labelString, valueString);
                return textView;
            }
        }

        if (VIEW_TYPE_CURRENCY.equalsIgnoreCase(viewType)) {
            if (updateable) {
                EditText editText = buildEditText(viewType, fieldName, updateable, root);
                setLayoutComponentData(editText, viewType, fieldName, labelString, valueString);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                return editText;

            } else {
                TextView textView = buildTextView(viewType, fieldName, updateable, root);
                setLayoutComponentData(textView, viewType, fieldName, labelString, valueString);
                return textView;
            }
        }

        if (VIEW_TYPE_PHONE.equalsIgnoreCase(viewType)) {
            if (updateable) {
                EditText editText = buildEditText(viewType, fieldName, updateable, root);
                setLayoutComponentData(editText, viewType, fieldName, labelString, valueString);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                return editText;

            } else {
                TextView textView = buildTextView(viewType, fieldName, updateable, root);
                setLayoutComponentData(textView, viewType, fieldName, labelString, valueString);
                return textView;
            }
        }

        if (VIEW_TYPE_EMAIL.equalsIgnoreCase(viewType)) {
            if (updateable) {
                EditText editText = buildEditText(viewType, fieldName, updateable, root);
                setLayoutComponentData(editText, viewType, fieldName, labelString, valueString);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(details.getLength())});
                return editText;

            } else {
                TextView textView = buildTextView(viewType, fieldName, updateable, root);
                setLayoutComponentData(textView, viewType, fieldName, labelString, valueString);
                return textView;
            }
        }

        if (VIEW_TYPE_DATE.equalsIgnoreCase(viewType)) {
            if (updateable) {
                EditText editText = buildEditText(viewType, fieldName, updateable, root);
                setLayoutComponentData(editText, viewType, fieldName, labelString, valueString);
                editText.setInputType(InputType.TYPE_CLASS_DATETIME); // | InputType.TYPE_DATETIME_VARIATION_DATE);
                editText.setFocusable(false);
                editText.setOnClickListener(dateOnClickListener);
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_date, 0);
                return editText;

            } else {
                TextView textView = buildTextView(viewType, fieldName, updateable, root);
                setLayoutComponentData(textView, viewType, fieldName, labelString, valueString);
                return textView;
            }
        }

        if (VIEW_TYPE_DATETIME.equalsIgnoreCase(viewType)) {
            if (updateable) {
                EditText editText = buildEditText(viewType, fieldName, updateable, root);
                setLayoutComponentData(editText, viewType, fieldName, labelString, valueString);
                editText.setInputType(InputType.TYPE_CLASS_DATETIME); // | InputType.TYPE_DATETIME_VARIATION_DATE);
                editText.setFocusable(false);
                editText.setOnClickListener(dateTimeOnClickListener);
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_date, 0);
                return editText;

            } else {
                TextView textView = buildTextView(viewType, fieldName, updateable, root);
                setLayoutComponentData(textView, viewType, fieldName, labelString, valueString);
                return textView;
            }
        }

        if (VIEW_TYPE_TEXTAREA.equalsIgnoreCase(viewType)) {
            if (updateable) {
                EditText editText = buildEditText(viewType, fieldName, updateable, root);
                setLayoutComponentData(editText, viewType, fieldName, labelString, valueString);
                editText.setLines(5);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(details.getLength())});
                return editText;

            } else {
                TextView textView = buildTextView(viewType, fieldName, updateable, root);
                setLayoutComponentData(textView, viewType, fieldName, labelString, valueString);
                return textView;
            }
        }

        if (VIEW_TYPE_ADDRESS.equalsIgnoreCase(viewType)) {
            if (updateable) {
                EditText editText = buildEditText(viewType, fieldName, updateable, root);
                setLayoutComponentData(editText, viewType, fieldName, labelString, valueString);
                editText.setFocusable(false);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                editText.setOnClickListener(addressOnClickListener);
                return editText;

            } else {
                TextView textView = buildTextView(viewType, fieldName, updateable, root);
                setLayoutComponentData(textView, viewType, fieldName, labelString, valueString);
                return textView;
            }
        }

        if (VIEW_TYPE_PICKLIST.equalsIgnoreCase(viewType)) {
            if (updateable) {
                Spinner spinner = buildSpinner(viewType, fieldName, updateable, root);

                List<AdapterItem> pickListLabels = getPickListLabels(fieldName);

                Log.v(TAG, "pickListLabels: " + pickListLabels);
                Log.v(TAG, "value: " + valueString);

                if (pickListLabels.size() == 0) {
                    spinner.setEnabled(false);
                }

                if (isPickListNillable(isRequired, details)) {
                    pickListLabels.add(0, emptyAdapterItem);
                }
                // Create an ArrayAdapter using the string array and a default spinner layout
                // ArrayAdapter<String> pedidoListAdapter = new ArrayAdapter<String>(this, R.layout.dynamic_simple_spinner, pickListLabels);
                // Specify the layout to use when the list of choices appears
                // pedidoListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the pedidoListAdapter to the spinner

                ArrayAdapter<AdapterItem> adapter = new ArrayAdapter<AdapterItem>(this, spinnerItemLayoutId, pickListLabels);
                //android.R.layout.simple_spinner_item, android.R.id.text1
                adapter.setDropDownViewResource(spinnerDropdownLayoutId);

                spinner.setAdapter(adapter);

                setLayoutComponentData(spinner, viewType, fieldName, labelString, valueString);
                spinner.setOnItemSelectedListener(this);

                return spinner;
            } else {
                TextView textView = buildTextView(viewType, fieldName, updateable, root);
                setLayoutComponentData(textView, viewType, fieldName, labelString, valueString);
                return textView;
            }
        }

        if (VIEW_TYPE_MULTIPICKLIST.equalsIgnoreCase(viewType)) {
            View view = buildMultiSelectView(viewType, fieldName, updateable, root);
            setLayoutComponentData(view, viewType, fieldName, labelString, valueString);
            if (updateable) {
                view.setOnClickListener(new MultiSelectOnClickListener(this, fieldName,
                        details.getPicklistValues(), String.valueOf(labelString)));
            }
            return view;
        }

        Log.e(TAG, "Unhandled Type: " + viewType);
        return null;

    }

    /**
     * Return true to allow to select no value.
     */
    protected boolean isPickListNillable(boolean isRequired, Details details) {
        return details.getNillable() && !AbInBevConstants.CasosFields.ESTADO__C.equals(details.getName());
    }

    private void setupPickListData(Gson gson, String objectType) {

        // pickListDependenyHashMap = MetaDataProvider.getPicklistDependency(this, objectType);
        // create a map of all fields for later lookup
//        fieldMap = new HashMap();
//        for (int i = 0; i < fields.size(); i++) {
//            fieldMap.put(fields.get(i).getName(), fields.get(i));
//        }

        updatedJSONObject = new JSONObject();
        viewHashMap = new HashMap<String, Integer>();
        labelHashMap = new HashMap<>();
        detailsMap = new HashMap();
        editTextHolderList = new ArrayList<EditTextHolder>();
    }

    private String savePickListValue(String fieldName, String value) {

        Log.v(TAG, "in savePickListValue: " + fieldName + ":" + value);

        if (value == emptyAdapterItem.getValue()) {
            try {
                updatedJSONObject.put(nameSpaceRemovedFieldName(fieldName), JSONObject.NULL);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        Details field = (Details) detailsMap.get(nameSpacedFieldName(fieldName));
        if (field == null) {
            Log.e(TAG, "null value for getPickListLabels for fieldName: " + fieldName);
            field = (Details) detailsMap.get(fieldName);
            if (field == null) {
                return null;
            }
        }

        try {
            updatedJSONObject.put(nameSpaceRemovedFieldName(fieldName), value);
            return value;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void updateDependentPickLists(String fieldName, String fieldCurrentValue) {

        // TODO: Optimize this. We dont have to loop thru this.
        for (Object value : detailsMap.values()) {
            Details field = (Details) value;
            if (nameSpacedFieldName(fieldName).equals(field.getControllerName())) {

                Log.v(TAG, "fieldName: " + field.getName() + " controllerName: " + fieldName);
                List<AdapterItem> pickListLabels = getPickListLabels(field.getName());

                // get View associated with this fieldName
                Integer viewId = viewHashMap.get(nameSpaceRemovedFieldName(field.getName()));
                if (viewId != null) {
                    Spinner spinner = (Spinner) findViewById(viewId);

                    if (pickListLabels.size() == 0) {
                        Log.v(TAG, "disabled spinner for field name: " + field.getName());
                        spinner.setEnabled(false);
                    } else {
                        spinner.setEnabled(true);
                    }

                    int defaultSelection = 0;
                    if (pickListLabels.size() == 1) {
                        defaultSelection = 1;
                    }

                    pickListLabels.add(0, emptyAdapterItem);

                    // update pedidoListAdapter with new list

                    // this is not triggering the onItemSelected so cascading wont work ...
//
//                ArrayAdapter<String> pedidoListAdapter  = (ArrayAdapter<String>) spinner.getAdapter();
//                pedidoListAdapter.clear();
//                pedidoListAdapter.addAll(pickListLabels);
//
//                pedidoListAdapter.notifyDataSetChanged();

                    // Create an ArrayAdapter using the string array and a default spinner layout
                    ArrayAdapter<AdapterItem> adapter = new ArrayAdapter<AdapterItem>(this, spinnerItemLayoutId, pickListLabels);
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(spinnerDropdownLayoutId);
                    // Apply the pedidoListAdapter to the spinner

                    spinner.setAdapter(adapter);

                    if (fieldCurrentValue == null) {
                        spinner.setSelection(0, true);
                    } else {
                        String valueString = getCurrentPicklistValueForFieldName(field.getName());

                        if (valueString != null && !valueString.equals("")) {
                            int currentSelection = adapter.getPosition(AdapterItem.justValue(valueString));
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
    }

    public String getCurrentPicklistValueForFieldName(String fieldName) {
        String currentValue = null;
        currentValue = updatedJSONObject.optString(nameSpaceRemovedFieldName(fieldName), null);
        if (currentValue == null) {
            currentValue = baseObject.getStringValueForKey(nameSpaceRemovedFieldName(fieldName));
        }
        return currentValue;
    }

    private List<AdapterItem> getPickListLabels(String fieldName) {

        Details field = (Details) detailsMap.get(nameSpacedFieldName(fieldName));

        if (field == null) {
            Log.e(TAG, "null value for getPickListLabels for fieldName: " + fieldName);
            field = (Details) detailsMap.get(fieldName);
            if (field == null) {
                return new ArrayList<AdapterItem>();
            }
        }

        List<PicklistValue> picklistValueList = field.getPicklistValues();

        if (field.getDependentPicklist() == false) {
            List<AdapterItem> validLabels = new ArrayList<AdapterItem>();
            for (PicklistValue picklistValue : picklistValueList) {
                if (picklistValue.getActive())
                    validLabels.add(new AdapterItem(picklistValue.getLabel(), picklistValue.getValue()));
            }
            return validLabels;
            // use value to store and use label to display
        } else {
            // get controller
            Log.e("Babu", "field.getControllerName() = " + field.getControllerName());
            Details controller = (Details) detailsMap.get(nameSpacedFieldName((String) field.getControllerName()));

            if (controller == null) {
                Log.v(TAG, "controller is null for field: " + field.getName());
                return new ArrayList<AdapterItem>();
            }
            Log.e("Babu", "controller.getName() = " + controller.getName());
            // get controller current value and get associated label
            String controllerName = controller.getName();
            controllerName = nameSpaceRemovedFieldName(controllerName);

            String controllerValue = getCurrentPicklistValueForFieldName(controller.getName());

            if (controllerValue == null || controllerValue.equals("")) {
                Log.v(TAG, "controllerValue null for field: " + field.getName());
                return new ArrayList<AdapterItem>();
            }

            FieldValueObject fieldValueObject = new FieldValueObject(controller.getName(), controllerValue);
            List<FieldValueObject> availableFieldValueObjects = picklistDependencyHolder.get(fieldValueObject);

            if (availableFieldValueObjects == null) {
                Log.v(TAG, "availableFieldValueObjects null for field: " + field.getName());
                return new ArrayList<AdapterItem>();
            }

            HashSet<String> validValues = new HashSet<String>();

            for (FieldValueObject item : availableFieldValueObjects) {
                if (item.getField().equals(field.getName())) {
                    validValues.add(item.getFieldValue());
                }
            }

            List<AdapterItem> validLabels = new ArrayList<AdapterItem>();
            for (PicklistValue picklistValue : picklistValueList) {
                if (validValues.contains(picklistValue.getValue())) {
                    if (picklistValue.getActive())
                        validLabels.add(new AdapterItem(picklistValue.getLabel(), picklistValue.getValue()));
                }
            }

            return validLabels;

        }
    }

    protected JSONObject getUpdatedJSONObject() {
        addEditTextUpdatesToJSONObject();
        return updatedJSONObject;
    }

    /**
     * Returns true if current document was modified.
     */
    protected boolean isModified() {
        JSONObject updatedObject = getUpdatedJSONObject();
        if (updatedObject == null) {
            return false;
        }

        JSONObject originalObject = baseObject == null ? new JSONObject() : baseObject.toJson();

        Iterator it = updatedObject.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object oldValue = originalObject.opt(key);
            Object newValue = updatedObject.opt(key);
            oldValue = emptyStringToNull(oldValue);
            newValue = emptyStringToNull(newValue);

            if (!Objects.equals(oldValue, newValue)) {
                Log.v(TAG, "First found modified field: " + key + " old val: " + oldValue + " new val: " + newValue);
                return true;
            }
        }

        return false;
    }

    @Nullable
    private Object emptyStringToNull(Object o) {
        return (EMPTY_STRING.equals(o) || JSONObject.NULL.equals(o)) ? null : o;
    }

    private void addEditTextUpdatesToJSONObject() {
        // findViewByIds are expensive
        // TextWatcher for each EditText is also expensive

        for (EditTextHolder editTextHolder : editTextHolderList) {
            EditText editText = editTextHolder.editText;
            String type = editTextHolder.editTextType;

            if (VIEW_TYPE_DATE.equals(type)) {
                addDateUpdatesToJson(editText, baseObject, updatedJSONObject);
            } else if (VIEW_TYPE_DATETIME.equals(type)) {
                addDateTimeUpdatesToJson(editText, baseObject, updatedJSONObject);
            } else if (VIEW_TYPE_ADDRESS.equals(type)) {
                addAddressUpdatesToJson(editText, baseObject, updatedJSONObject);
            } else {
                addStringUpdatesToJson(editText, baseObject, updatedJSONObject);
            }
        }
    }

    private void addAddressUpdatesToJson(EditText editText, SFBaseObject baseObject, JSONObject updatedJson) {
        String fieldName = (String) editText.getTag();
        String addressFieldPrefix = Address.getAddressFieldPrefix(fieldName);
        Address address = (Address) editText.getTag(R.id.dynamic_view_address);

        for (String addressFieldSuffix : Address.ADDRESS_FIELD_SUFFIXES) {
            Object value = address.getValue(addressFieldSuffix.toLowerCase());
            String addressFieldName = addressFieldPrefix + addressFieldSuffix;

            addStringUpdatesToJson(addressFieldName, value, baseObject, updatedJson);
        }
    }

    private void addDateTimeUpdatesToJson(EditText editText, SFBaseObject baseObject, JSONObject updatedJson) {
        String fieldName = (String) editText.getTag();
        String fieldValue = editText.getText().toString();

        try {
            fieldValue = serverDateFormat.format(dateTimeFormat.parse(fieldValue));
        } catch (Exception e) {
            Log.e("Babu", "saving data time error in date parsing. value: " + fieldValue);
        }

        addStringUpdatesToJson(fieldName, fieldValue, baseObject, updatedJson);
    }

    private void addDateUpdatesToJson(EditText editText, SFBaseObject baseObject, JSONObject updatedJson) {
        String fieldName = (String) editText.getTag();
        String fieldValue = editText.getText().toString();

        try {
            fieldValue = serverDateOnlyFormat.format(dateFormat.parse(fieldValue));
        } catch (Exception e) {
            Log.e("Babu", "saving data error in date parsing. value: " + fieldValue);
        }

        addStringUpdatesToJson(fieldName, fieldValue, baseObject, updatedJson);
    }

    private void addStringUpdatesToJson(EditText editText, SFBaseObject baseObject, JSONObject updatedJson) {
        String fieldName = (String) editText.getTag();
        String fieldValue = editText.getText().toString();
        addStringUpdatesToJson(fieldName, fieldValue, baseObject, updatedJson);
    }

    private void addStringUpdatesToJson(String fieldName, Object fieldValue, SFBaseObject baseObject, JSONObject updatedJson) {
        boolean updateField;


        if (fieldValue instanceof String) {
            String newValue = (String) fieldValue;
            String currentValue = baseObject.getStringValueForKey(fieldName);
            boolean isCurrentEmpty = TextUtils.isEmpty(currentValue) || "null".equals(currentValue);
            boolean isNewEmpty = TextUtils.isEmpty(newValue) || "null".equals(newValue);

            if (isNewEmpty && isCurrentEmpty) {
                updateField = false;
            } else {
                updateField = !Objects.equals(fieldValue, currentValue);
            }
        } else {
            Object currentValue = baseObject.getValueForKey(fieldName);
            updateField = Objects.equals(fieldValue, currentValue);
        }

        if (updateField) {
            try {
                updatedJson.put(fieldName, fieldValue);
            } catch (JSONException e) {
                Log.w(TAG, e);
            }
        } else {
            updatedJson.remove(fieldName);
        }
    }

    protected String nameSpaceRemovedFieldName(String fieldName) {
        // if prefix is present in the key then use a stripped version
        return ManifestUtils.removeNamespaceFromField(objectName, fieldName, this);
    }

    // this has a performance hit compared to the previous method but this is correct and ca
    protected String nameSpacedFieldName(String fieldName) {
        if (objectName == null) return fieldName;
        else {
            return ManifestUtils.getNamespaceSupportedFieldName(objectName, fieldName, this);
        }
    }

    private View.OnClickListener dateOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectedDateView = ((EditText) v);
            String fieldValue = selectedDateView.getText().toString();
            Date date;
            try {
                date = dateFormat.parse(fieldValue);
            } catch (Exception e) {
                date = null;
            }
            showPicker(date, onDateListener);
        }
    };

    private View.OnClickListener dateTimeOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectedDateView = ((EditText) v);
            String fieldValue = selectedDateView.getText().toString();
            Date date;
            try {
                date = dateTimeFormat.parse(fieldValue);
            } catch (Exception e) {
                date = null;
            }
            showDateTimeDialog(v, date);
        }
    };

    private static final String FRAGMENT_ADDRESS = "fragment_address";

    private View.OnClickListener addressOnClickListener = v -> {
        EditText editText = ((EditText) v);
        String fieldName = (String) editText.getTag();
        Address address = (Address) editText.getTag(R.id.dynamic_view_address);

        if (address == null) {
            address = new Address();
        }

        AddressDialogFragment.createInstance(fieldName, address)
                .show(getSupportFragmentManager(), FRAGMENT_ADDRESS);
    };

    private static class MultiSelectOnClickListener implements View.OnClickListener {

        private final DynamicEditActivity parent;

        private final String fieldName;

        private final List<PicklistValue> values;

        private final String viewLabel;

        public MultiSelectOnClickListener(DynamicEditActivity parent, String fieldName,
                                          List<PicklistValue> values, String viewLabel) {
            this.parent = parent;
            this.fieldName = fieldName;
            this.values = values;
            this.viewLabel = viewLabel;
        }

        @Override
        public void onClick(View v) {
            parent.showMultiSelectPicklist(fieldName, values, viewLabel, v);
        }
    }

    private static class ReferenceOnClickListener implements View.OnClickListener {

        final DynamicEditActivity activity;

        final String referredObjectType;

        final CharSequence label;

        final String fieldName;

        final String lookupFieldName;

        private ReferenceOnClickListener(DynamicEditActivity activity, CharSequence label,
                                         String fieldName, String lookupFieldName,
                                         String referredObjectType) {
            this.referredObjectType = referredObjectType;
            this.fieldName = fieldName;
            this.label = label;
            this.lookupFieldName = lookupFieldName;
            this.activity = activity;
        }

        @Override
        public void onClick(View v) {
            String lookupFilter = activity.getLookupFilter(fieldName, referredObjectType);
            ReferencedValuesFragment fragment = ReferencedValuesFragment.newInstance(label,
                    fieldName, lookupFieldName, referredObjectType, lookupFilter);
            fragment.show(activity.getSupportFragmentManager(), FRAGMENT_REFERENCED_VALUES);
        }
    }

    private void showPicker(final Date date, DatePickerDialog.OnDateSetListener onDateSetListener) {
        int year, month, day;
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
        } else {
            Calendar today = Calendar.getInstance();
            year = today.get(Calendar.YEAR);
            month = today.get(Calendar.MONTH);
            day = today.get(Calendar.DAY_OF_MONTH);
        }
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener, year, month, day);
        datePickerDialog.show();
    }

    private void showMultiSelectPicklist(final String fieldName, final List<PicklistValue> values, final String viewLabel, final View view) {
        final List<String> checkedValues = splitMultiSelectValues(getCurrentPicklistValueForFieldName(fieldName));

        if (values.isEmpty()) {
            int color = getResources().getColor(R.color.sab_gray);
            CharSequence message = new SpannableStringBuilder()
                    .append(getString(R.string.empty), new ForegroundColorSpan(color), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            new AlertDialog.Builder(this)
                    .setTitle(viewLabel)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok, null)
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show();
        } else {
            String[] labels = new String[values.size()];
            final boolean[] checkedIndexes = new boolean[values.size()];
            for (int i = 0; i < values.size(); i++) {
                PicklistValue pv = values.get(i);
                labels[i] = pv.getLabel();
                checkedIndexes[i] = checkedValues.contains(pv.getValue());
            }

            new AlertDialog.Builder(this)
                    .setTitle(viewLabel)
                    .setMultiChoiceItems(labels, checkedIndexes,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    if (isChecked) {
                                        checkedValues.add(values.get(which).getValue());
                                    } else {
                                        checkedValues.remove(values.get(which).getValue());
                                    }
                                }
                            })
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                String newValue = joinMultiSelectValues(checkedValues);
                                updatedJSONObject.put(fieldName, newValue);
                                setLayoutComponentData(view, VIEW_TYPE_MULTIPICKLIST, fieldName, viewLabel, newValue);
                            } catch (JSONException e) {
                                Log.w(TAG, e);
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show();
        }
    }

    @Override
    public void onReferencedItemSelected(CharSequence label, String fieldName, String itemId, String itemName) {
        try {
            updatedJSONObject.put(fieldName, itemId);
            Integer viewId = viewHashMap.get(fieldName);
            if (viewId != null) {
                View view = findViewById(viewId);
                setLayoutComponentData(view, VIEW_TYPE_REFERENCE, fieldName, label, itemName);
            }
        } catch (JSONException e) {
            Log.w(TAG, e);
        }
    }

    private List<String> splitMultiSelectValues(String value) {
        if (TextUtils.isEmpty(value)) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(Arrays.asList(TextUtils.split(value, ";")));
        }
    }

    private String joinMultiSelectValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        } else {
            return TextUtils.join(";", values);
        }
    }

    public void showDateTimeDialog(View v, Date date) {
        final View dialogView = View.inflate(this, R.layout.dynamic_date_time, null);

        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minutes = cal.get(Calendar.MINUTE);

            DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.datepicker);
            TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.timepicker);
            datePicker.updateDate(year, month, day);
            timePicker.setCurrentHour(hour);
            timePicker.setCurrentMinute(minutes);
        }

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setPositiveButton(getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.datepicker);
                                TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.timepicker);
                                int day = datePicker.getDayOfMonth();
                                int month = datePicker.getMonth();
                                int year = datePicker.getYear();
                                int hour = timePicker.getCurrentHour();
                                int minute = timePicker.getCurrentMinute();
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year, month, day, hour, minute);
                                selectedDateView.setText(dateTimeFormat.format(calendar.getTime()));
                            }
                        })
                .setNegativeButton(getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss
                            }
                        })
                .setView(dialogView)
                .create();

        alertDialog.show();
    }

    @Override /* AddressDialogFragment.OnAddressSavedListener */
    public void onAddressSaved(String fieldName, Address address) {
        Integer id = viewHashMap.get(fieldName);
        if (id != null) {
            EditText editText = (EditText) findViewById(id);
            editText.setText(address.getPrintableAddress());
            editText.setTag(R.id.dynamic_view_address, address);
        }
    }

    private void askIfClose() {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setMessage(R.string.are_you_sure_to_discard)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    protected void removeAllViews() {
        mainLayout.removeAllViews();
    }

    private EditText selectedDateView;

    private DatePickerDialog.OnDateSetListener onDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            selectedDateView.setText(dateFormat.format(calendar.getTime()));
        }
    };


    private static class AdapterItem {
        private final String label;
        private final String value;

        public AdapterItem(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public static AdapterItem justValue(String value) {
            return new AdapterItem(null, value);
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AdapterItem that = (AdapterItem) o;

            return value != null ? value.equals(that.value) : that.value == null;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }

    private class EditTextHolder {
        EditText editText;
        String editTextType;

        public EditTextHolder(EditText editText, String editTextType) {
            this.editText = editText;
            this.editTextType = editTextType;
        }
    }
}
