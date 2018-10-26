package com.abinbev.dsa.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.service.AttachmentUploadService;
import com.abinbev.dsa.ui.presenter.BasicDataDynamicLayoutPresenter;
import com.abinbev.dsa.ui.view.ProspectGPSField;
import com.abinbev.dsa.ui.view.ProspectPhotoField;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AccountFields;
import com.abinbev.dsa.utils.AttachmentUtils;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.androidsyncengine.data.layouts.Details;
import com.salesforce.androidsyncengine.data.layouts.LayoutItem;
import com.salesforce.androidsyncengine.data.layouts.LayoutSection;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import com.salesforce.dsa.location.LocationHandler;
import com.salesforce.dsa.location.LocationHandlerProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.OnClick;

public class BasicDataDynamicLayoutActivity extends DynamicEditActivity implements BasicDataDynamicLayoutPresenter.ViewModel {

    public static final String ACCOUNT_ID_EXTRA = "account_id";

    private static final int REQUEST_CODE_EDIT_ADDITIONAL_DATA = 1;
    private static final String TAG = "BasicDataDynLaActivity";

    private String accountId;
    private LocationHandler locationHandler;
    private BasicDataDynamicLayoutPresenter presenter;

    private ProspectGPSField prospectGPSField;
    private ProspectPhotoField prospectAccountPhotoField;
    private ProspectPhotoField prospectLicensePhotoField;

    public enum ProspectPhotoSelect {
        NONE,
        ACCOUNT_PHOTO,
        LICENSE_PHOTO
    }

    private ProspectPhotoSelect photoSelect = ProspectPhotoSelect.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        accountId = getIntent().getStringExtra(ACCOUNT_ID_EXTRA);

        setSectionHeaderLayout(R.layout.dynamic_prospect_header);
        setEditRowLayout(R.layout.dynamic_prospect_row);
        setSpinnerItemLayout(android.R.layout.simple_spinner_item);
        setSpinnerDropdownLayout(R.layout.support_simple_spinner_dropdown_item);

        locationHandler = LocationHandlerProvider.createLocationHandler(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.datos_basicos));
        getSupportActionBar().setSubtitle("");

        prospectGPSField = new ProspectGPSField(this);
        prospectGPSField.setOnClickListener(onGPSClickListener);
        prospectAccountPhotoField = new ProspectPhotoField(this, R.string.account_photo);
        prospectAccountPhotoField.setListener(prospectsPhotoFieldListener);
        prospectLicensePhotoField = new ProspectPhotoField(this, R.string.license_photo);
        prospectLicensePhotoField.setVisibility(View.GONE);
        prospectLicensePhotoField.setListener(prospectsPhotoFieldListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add_additional) {
            additionalDataClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isNewCase()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.add_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CODE_EDIT_ADDITIONAL_DATA == requestCode &&
                resultCode == RESULT_OK) {
            Account account = (Account) data.getSerializableExtra(AdditionalDataDynamicLayoutActivity.ACCOUNT_OBJECT_EXTRA);
            updateAccountInfo(account);
        } else if (AttachmentUtils.SELECT_PHOTO_REQUEST_CODE == requestCode) {
            if (resultCode == RESULT_OK) {
                Uri uri = AttachmentUtils.fileUri != null ? AttachmentUtils.fileUri : data.getData();

                if (uri == null) {
                    Log.e("Babu", "data is null!");
                    return;
                }
                switch (photoSelect) {
                    case ACCOUNT_PHOTO: {
                        prospectAccountPhotoField.setPhoto(uri);
                    }
                    break;
                    case LICENSE_PHOTO: {
                        prospectLicensePhotoField.setPhoto(uri);
                    }
                    break;
                }

            }
        }
        photoSelect = ProspectPhotoSelect.NONE;
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void additionalDataClicked() {
        Account account = (Account) baseObject;

        Log.i(TAG, "original Values: " + account.toJson().toString());
        JSONObject updatedObject = getUpdatedJSONObject();
        Log.i(TAG, "updated Values: " + updatedObject.toString());

        try {
            Iterator it = updatedObject.keys();
            while (it.hasNext()) {
                String key = (String) it.next();
                account.setValueForKey(key, updatedObject.get(key));
            }
        } catch (JSONException e) {
        }
        Intent intent = new Intent(this, AdditionalDataDynamicLayoutActivity.class);
        intent.putExtra(AdditionalDataDynamicLayoutActivity.ACCOUNT_OBJECT_EXTRA, account);
        startActivityForResult(intent, REQUEST_CODE_EDIT_ADDITIONAL_DATA);
    }

    @Override
    protected boolean isUpdateable(LayoutItem layoutItem, Details details, String fieldName, String section) {
        if (AccountFields.PARENT_ID.equals(fieldName)) {
            return false;
        } else {
            return super.isUpdateable(layoutItem, details, fieldName, section);
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
    protected TextView createNewTextView(String type, String fieldName, boolean isUpdateable, ViewGroup root) {
        TextView textView = super.createNewTextView(type, fieldName, isUpdateable, root);
        textView.setMinHeight(getResources().getDimensionPixelSize(R.dimen.prospect_text_height));
        textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        return textView;
    }

    @Override
    protected boolean acceptLayoutSection(int sectionNumber, LayoutSection editLayoutSection) {
        return sectionNumber == 0; // Display only first section
    }

    @Override
    protected void setLayoutComponentData(View contentView, String viewType, String fieldName, CharSequence label, String value) {
        if (VIEW_TYPE_CHECK_BOX.equals(viewType)) {
            CheckBox checkBox = (CheckBox) contentView;
            checkBox.setText(label);
        } else {
            super.setLayoutComponentData(contentView, viewType, fieldName, label, value);
        }
    }

    @Override
    protected void setLayoutComponentLabel(View labelView, String viewType, String fieldName, CharSequence label, CharSequence value) {
        // This label should be hidden.
        if (VIEW_TYPE_CHECK_BOX.equals(viewType)) {
            labelView.setVisibility(View.GONE);
        } else {
            super.setLayoutComponentLabel(labelView, viewType, fieldName, label, value);
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.basic_data_dynamic_layout_activity;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (presenter == null) {
            presenter = new BasicDataDynamicLayoutPresenter();
        }
        presenter.setViewModel(this);
        presenter.setAccountId(accountId);
        presenter.start();
        locationHandler.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.stop();
        locationHandler.disconnect();
    }

    @OnClick(R.id.save_basic_data_fab)
    public void onSave(View view) {
        if (containsValidValues()) {
            Account account = (Account) baseObject;

            Log.i(TAG, "original Values: " + account.toJson().toString());
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

            account.changeProspectStatusDataUpdated();

            if (isNewCase()) {
                presenter.createAccount(account);
            } else {
                if (null != prospectAccountPhotoField.getImageUri()) {
                    Intent intent = AttachmentUploadService.uploadAccountPhoto(this, prospectAccountPhotoField.getImageUri(), accountId);
                    startService(intent);
                }
                if (null != prospectLicensePhotoField.getImageUri()) {
                    Intent intent = AttachmentUploadService.uploadLicensePhoto(this, prospectLicensePhotoField.getImageUri(), accountId);
                    startService(intent);
                }
                presenter.updateAccount(account);
            }
        }
    }

    private boolean isNewCase() {
        return accountId == null;
    }

    @Override
    public void createUpdateAccountSuccess(String accountId) {
        // this is a new account so we will have to take them to the detail activity
//        if (accountId == null) {
//            Intent intent = new Intent(this, ProspectDetailActivity.class);
////            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            intent.putExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA, accountId);
//            startActivity(intent);
//        }
        SyncUtils.TriggerRefresh(this);
        finish();
    }

    @Override
    protected List<String> filterFields() {
        List<String> filterFields = new ArrayList<>();
        filterFields.add(AccountFields.PARENT_ID);
        filterFields.add(AccountFields.OWNER_ID);
        return filterFields;
    }

    @Override
    public void updateAccountInfo(Account account) {
        getSupportActionBar().setSubtitle(account.getName());
        buildLayout("Account", account);
        RecordType recordType = RecordType.getByName(AbInBevConstants.AccountRecordType.PROSPECT);
        if (TextUtils.equals(account.getRecordTypeId(), recordType.getId())) {
            prospectGPSField.setLatLng(account.getLatitude(), account.getLongitude());
            mainLayout.addView(prospectGPSField);
            if (!TextUtils.isEmpty(account.getId())) {
                mainLayout.addView(prospectAccountPhotoField);
                mainLayout.addView(prospectLicensePhotoField);
            }
        }

//        for (String fieldName : HIDE_EDIT_FIELDS) {
//            if (null != getLayoutComponentLabel(fieldName)) {
//                getLayoutComponentLabel(fieldName).setVisibility(View.GONE);
//            }
//            if (null != getViewByField(fieldName)) {
//                getViewByField(fieldName).setVisibility(View.GONE);
//            }
//        }

    }

    @Override
    public void setAccountPhoto(Attachment attachment) {
        prospectAccountPhotoField.setPhoto(attachment, accountId);
    }

    @Override
    public void setAccountLicensePhoto(Attachment attachment) {
        prospectLicensePhotoField.setPhoto(attachment, accountId);
    }

    private View.OnClickListener onGPSClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Account account = (Account) baseObject;
            LatLng currentLatLng = locationHandler.getCurrentLatLng();
            account.setLocation(currentLatLng);
            prospectGPSField.setLatLng(account.getLatitude(), account.getLongitude());
        }
    };

    private ProspectPhotoField.ProspectsPhotoFieldListener prospectsPhotoFieldListener = new ProspectPhotoField.ProspectsPhotoFieldListener() {
        @Override
        public void onPhotoFieldClicked(View view) {
            if (prospectAccountPhotoField == view) {
                photoSelect = ProspectPhotoSelect.ACCOUNT_PHOTO;
            } else if (prospectLicensePhotoField == view) {
                photoSelect = ProspectPhotoSelect.LICENSE_PHOTO;
            }
            AlertDialog alertDialog = AttachmentUtils.createPhotoChooserDialog(BasicDataDynamicLayoutActivity.this);
            alertDialog.show();
        }
    };

    private boolean containsValidValues() {
        JSONObject updatedObject = getUpdatedJSONObject();
        Log.i(TAG, "updated Values: " + updatedObject.toString());
        Log.i(TAG, "required fields: " + requiredFields);

        if (requiredFields == null) {
            // the buildLayout has failed or it is still in progress
            return false;
        }

        for (String fieldName : requiredFields) {
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
        //Prospect
        RecordType recordType = RecordType.getByName(AbInBevConstants.AccountRecordType.PROSPECT);
        if (TextUtils.equals(baseObject.getRecordTypeId(), recordType.getId())) {
//            Account account = (Account) baseObject;
//            if (Double.isNaN(account.getLatitude()) && Double.isNaN(account.getLongitude())) {
//                showSnackbar(getString(R.string.missing_required_field) + " : " + prospectGPSField.getFieldName());
//                return false;
//            }
            if (!isNewCase()) {
                if (prospectAccountPhotoField.getImageUri() == null &&
                        Attachment.getAccountPhotoAttachment(accountId) == null) {
                    showSnackbar(getString(R.string.missing_required_field) + " : " + prospectAccountPhotoField.getFieldName());
                    return false;
                }
//                if (prospectLicensePhotoField.getImageUri() == null &&
//                        Attachment.getAccountLicensePhotoAttachment(accountId) == null) {
//                    showSnackbar(getString(R.string.missing_required_field) + " : " + prospectLicensePhotoField.getFieldName());
//                    return false;
//                }
            }
        }

        return true;
    }
}