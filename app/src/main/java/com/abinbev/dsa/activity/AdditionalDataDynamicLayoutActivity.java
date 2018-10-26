package com.abinbev.dsa.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.ui.presenter.AdditionalDataDynamicLayoutPresenter;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.salesforce.androidsyncengine.data.layouts.LayoutSection;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import butterknife.OnClick;

/**
 * Created by mlangreder on 4/5/16.
 */
public class AdditionalDataDynamicLayoutActivity extends DynamicEditActivity implements AdditionalDataDynamicLayoutPresenter.ViewModel {

    public static final String PROSPECT_ID_EXTRA = "prospect_id";
    public static final String ACCOUNT_OBJECT_EXTRA = "account_object";
    private static final String TAG = "AdditionalDataDynAct";

    private String prospectId;
    private Account account;
    private AdditionalDataDynamicLayoutPresenter presenter;

    private static final Set<String> LOAN_FIELDS = new HashSet<>();
    static {
        LOAN_FIELDS.add(AbInBevConstants.AccountFields.LOAN_AMOUNT__C);
        LOAN_FIELDS.add(AbInBevConstants.AccountFields.LOAN_END_DATE__C);
    }

    private static final Set<String> PREFERRED_SERVICE_FIELDS = new HashSet<>();
    static {
        PREFERRED_SERVICE_FIELDS.add(AbInBevConstants.AccountFields.PREFERRED_SERVICE_DAYS__C);
        PREFERRED_SERVICE_FIELDS.add(AbInBevConstants.AccountFields.PREFERRED_SERVICE_HOURS__C);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.additional_data_dynamic_layout_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prospectId = getIntent().getStringExtra(PROSPECT_ID_EXTRA);
        account = (Account) getIntent().getSerializableExtra(ACCOUNT_OBJECT_EXTRA);

        setSectionHeaderLayout(R.layout.dynamic_prospect_header);
        setEditRowLayout(R.layout.dynamic_prospect_row);
        setSpinnerItemLayout(android.R.layout.simple_spinner_item);
        setSpinnerDropdownLayout(R.layout.support_simple_spinner_dropdown_item);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.datos_adicionales));
        getSupportActionBar().setSubtitle("");

        if(isNewCase()) {
            updateAccountInfo(account);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isModified()) {
                    askIfClose();
                    return true;
                }
                else {
                    return false;
                }

            default:
                return false;
        }
    }

    @Override
    protected Spinner createNewSpinner(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        return (Spinner) getLayoutInflater().inflate(R.layout.dynamic_prospect_spinner, root, false);
    }

    @Override
    protected CheckBox createNewCheckBox(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        return (CheckBox) getLayoutInflater().inflate(R.layout.dynamic_prospect_check_box, root, false);
    }

    @Override
    protected View createNewMultiSelectView(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        if (VIEW_TYPE_MULTIPICKLIST.equals(type) && PREFERRED_SERVICE_FIELDS.contains(fieldName)) {
            View view = getLayoutInflater().inflate(R.layout.dynamic_prospect_preferred_time, root, false);
            ImageView iconView = (ImageView) view.findViewById(R.id.preferred_time_icon);
            TextView labelView = (TextView) view.findViewById(R.id.preferred_time_label);

            if (AbInBevConstants.AccountFields.PREFERRED_SERVICE_DAYS__C.equals(fieldName)) {
                iconView.setImageResource(R.drawable.ic_date_range_primary_36dp);
                labelView.setText(R.string.dia_de_atencion);
            }
            else {
                iconView.setImageResource(R.drawable.ic_access_time_primary_36dp);
                labelView.setText(R.string.hora_de_atencion);
            }
            return view;
        }
        else {
            return super.createNewMultiSelectView(type, fieldName, isUpdateable, root);
        }
    }

    @Override
    protected TextView createNewTextView(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        TextView textView = super.createNewTextView(type, fieldName, isUpdateable, root);
        textView.setMinHeight(getResources().getDimensionPixelSize(R.dimen.prospect_text_height));
        textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        return textView;
    }

    @Override
    protected boolean acceptLayoutSection(int sectionNumber, LayoutSection editLayoutSection) {
        return sectionNumber > 0 && sectionNumber < 4; // Display only specific sections
    }

    @Override
    protected void setLayoutComponentData(View contentView, String viewType, String fieldName, CharSequence label, String value) {

        // For checkbox set label string as checkbox text.
        if (VIEW_TYPE_CHECK_BOX.equals(viewType)) {
            CheckBox checkBox = (CheckBox) contentView;
            checkBox.setText(label);
        }
        // Multi pick list is a custom view, so handle it correctly.
        else if (VIEW_TYPE_MULTIPICKLIST.equals(viewType) && PREFERRED_SERVICE_FIELDS.contains(fieldName)) {
            TextView labelView = (TextView) contentView.findViewById(R.id.preferred_time_value);
            if (!TextUtils.isEmpty(value)) {
                value = String.valueOf(value).replace(";", ", ");
            }
            labelView.setText(value);
        }
        else {
            super.setLayoutComponentData(contentView, viewType, fieldName, label, value);
        }

        // Disable loan specific fields depending on Loan__c value.
        if (LOAN_FIELDS.contains(fieldName)) {
            String currentLoanValue = getCurrentPicklistValueForFieldName(AbInBevConstants.AccountFields.LOAN__C);
            TextView textView = (TextView) contentView;
            boolean isEnabled = !AbInBevConstants.BOOLEAN_NO.equals(currentLoanValue);

            setViewEnabled(textView, isEnabled);
        }
    }

    @Override
    protected void setLayoutComponentLabel(View labelView, String viewType, String fieldName, CharSequence label, CharSequence value) {
        if (VIEW_TYPE_CHECK_BOX.equals(viewType)) {
            labelView.setVisibility(View.GONE);
        }
        else {
            super.setLayoutComponentLabel(labelView, viewType, fieldName, label, value);
        }
    }

    @Override
    protected void updateDependentPickLists(String fieldName, String fieldCurrentValue) {
        super.updateDependentPickLists(fieldName, fieldCurrentValue);

        // If loan is set to 'No' we can disable some of the fields.
        if (AbInBevConstants.AccountFields.LOAN__C.equals(fieldName)) {
            boolean isEnabled = !AbInBevConstants.BOOLEAN_NO.equals(fieldCurrentValue);
            for (String loanSpecificField : LOAN_FIELDS) {
                TextView view = (TextView) getViewByField(loanSpecificField);
                setViewEnabled(view, isEnabled);
            }
        }
    }

    /** Enable or disable specific view. */
    private void setViewEnabled(TextView view, boolean enabled) {
        if (view == null) return;

        if (enabled) {
            view.setEnabled(true);
            view.setTextColor(ContextCompat.getColor(this, R.color.sab_black));
            view.setBackground(ContextCompat.getDrawable(this, R.drawable.qty_bg));
        }
        else {
            view.setEnabled(false);
            view.setTextColor(ContextCompat.getColor(this, R.color.disabled_text));
            view.setBackground(ContextCompat.getDrawable(this, R.drawable.qty_disabled_bg));
        }
    }

    @Override
    public void updateAccountInfo(Account account) {
        getSupportActionBar().setSubtitle(account.getName());
        buildLayout("Account", account);
    }

    @Override
    public void onAccountUpdated(Boolean didUpdate) {
        SyncUtils.TriggerRefresh(this);
        Log.e("Babu", "Account Updated!: " + didUpdate.toString());
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (presenter == null) {
            presenter = new AdditionalDataDynamicLayoutPresenter(prospectId);
        }
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.stop();
    }

    @Override
    public void onBackPressed() {
        if (isModified()) {
            askIfClose();
        }
        else {
            super.onBackPressed();
        }
    }

    private void askIfClose() {
        new AlertDialog.Builder(this)
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

    @OnClick(R.id.save_additional_data_fab)
    public void onSave(View view) {
        if (containsValidValues()) {
            Account account = (Account) baseObject;
            Log.i(TAG, "original Values: " + account.toString());
            JSONObject updatedObject = getUpdatedJSONObject();
            Log.i(TAG, "updated Values: " + updatedObject.toString());

            try {
                Iterator it = updatedObject.keys();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    account.setValueForKey(key, updatedObject.get(key));
                }
            } catch (JSONException e) {
                showSnackbar(R.string.failed_to_save_case);
                return;
            }
            if(isNewCase()) {
                Intent intent = new Intent();
                intent.putExtra(ACCOUNT_OBJECT_EXTRA,account);
                this.setResult(RESULT_OK,intent);
                finish();
            }
            else {
                account.changeProspectStatusDataUpdated();

                presenter.saveAdditionalData(account);
            }
        }
    }

    private boolean isNewCase() {
        return prospectId == null;
    }

    private boolean containsValidValues() {
        JSONObject updatedObject = getUpdatedJSONObject();
        Log.i(TAG, "updated Values: " + updatedObject.toString());
        Log.i(TAG, "required fields: " + requiredFields);

        if (requiredFields == null) {
            // the buildLayout has failed or it is still in progress
            return false;
        }

        String loanValue = getCurrentValueForFieldName(updatedObject, AbInBevConstants.AccountFields.LOAN__C);
        boolean excludeLoanFields = AbInBevConstants.BOOLEAN_NO.equals(loanValue);

        for (String fieldName : requiredFields) {
            if (!(excludeLoanFields && LOAN_FIELDS.contains(fieldName))) {                      //Loan specific fields are not required if Loan is set to 'No'
                String currentValue = getCurrentValueForFieldName(updatedObject, fieldName);
                String fieldLabel = fieldLabels.get(fieldName);
                if (currentValue == null) {
                    Log.i(TAG, "Missing required field: " + fieldLabel + "(" + fieldName + ")");
                    showSnackbar(getString(R.string.missing_required_field) + " : " + fieldLabel);
                    return false;
                } else {
                    currentValue = currentValue.trim();
                    if (currentValue.isEmpty()) {
                        Log.i(TAG, "Missing required field: " + fieldLabel + "(" + fieldName + ")");
                        showSnackbar(getString(R.string.missing_required_field) + " : " + fieldLabel);
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
