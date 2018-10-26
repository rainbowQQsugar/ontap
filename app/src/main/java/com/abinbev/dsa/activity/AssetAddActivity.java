package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account_Asset__c;
import com.abinbev.dsa.ui.presenter.AssetAddPresenter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class AssetAddActivity extends AppBaseActivity implements AssetAddPresenter.ViewModel {

    private static final String TAG = AssetAddActivity.class.getSimpleName();
    private static final int QRCODE_SCAN_REQUEST_CODE = 123;
    public static final String ARGS_IS_CHECKED_IN = "checkIn";
    public static final String ACCOUNT_ID_EXTRA = "accountId";
    private String accountId;
    private AssetAddPresenter presenter;
    @Bind(R.id.qr_code_text)
    TextView qrCodeTextView;

    @Bind(R.id.asset_name_edit)
    EditText assetNameEdit;

    @Bind(R.id.asset_category_spinner)
    Spinner assetCategorySpinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            String tasks = getString(R.string.asset_add_activity_title);
            getSupportActionBar().setTitle(tasks.toUpperCase());
        }
        if (getIntent() != null) {
            accountId = getIntent().getStringExtra(ACCOUNT_ID_EXTRA);
        }
        initAssetAddPresenter();
        initSpinner();
    }

    private void initAssetAddPresenter() {
        presenter = new AssetAddPresenter(this);
        presenter.start();
    }

    private void initSpinner() {
        List<String> category = new ArrayList<>();
        category.add(getString(R.string.account_asset_capacity_none));
        category.add(getString(R.string.account_asset_capacity_refrigerator));
        category.add(getString(R.string.account_asset_capacity_barrel_beer));
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.asset_add_spinner_item,
                category);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assetCategorySpinner.setAdapter(spinnerArrayAdapter);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_asset_add_view;
    }

    @OnClick(R.id.save)
    public void saveAsset() {
        final String qrCode = qrCodeTextView.getText().toString();
        final String assetName = assetNameEdit.getText().toString();
        final String assetType = ((TextView) assetCategorySpinner.getSelectedView()).getText().toString();
        if (checkIfCanSave(qrCode, assetName, assetType)) {
            AlertDialog dialog = buildSaveDialog(getString(R.string.asset_add_activity_warning));
            dialog.show();
        }

    }

    private boolean checkIfCanSave(String qrCode, String assetName, String assetType) {
        boolean ok = true;
        if (StringUtils.isEmpty(qrCode)) {
            ok = false;
            showInfoMsg(getResources().getString(R.string.asset_add_activity_error_qr_code));
        } else if (StringUtils.isEmpty(assetName)) {
            ok = false;
            showInfoMsg(getResources().getString(R.string.asset_add_activity_error_asset_name));
        } else if (getString(R.string.account_asset_capacity_none).equals(assetType)) {
            ok = false;
            showInfoMsg(getResources().getString(R.string.asset_add_activity_error_asset_type));
        }
        return ok;
    }

    private AlertDialog buildSaveDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(AssetAddActivity.this).create();
        alertDialog.setTitle(R.string.warning);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.save),
                (dialog, which) -> {
                    final String qrCode = qrCodeTextView.getText().toString();
                    final String assetName = assetNameEdit.getText().toString();
                    final String assetType = ((TextView) assetCategorySpinner.getSelectedView()).getText().toString();
                    presenter.saveAssetAndTrackingToDb(accountId, qrCode, assetName, assetType);
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                (dialog, which) -> dialog.dismiss());

        return alertDialog;
    }

    private String getAssetMsg(AssetAddPresenter.AssetMsg msg) {
        return getString(R.string.asset_add_exit_error_msg, msg.asset__c != null ? msg.asset__c.getAssetName() : "");
    }

    private String getAssetSqrMsg(AssetAddPresenter.AssetMsg msg) {
        return getString(R.string.asset_add_exit_sqr_error_msg, msg.asset__c != null ? msg.asset__c.getAssetName() : "");
    }

    @OnClick(R.id.qr_code_button)
    public void onQrCodeClick() {
        Intent i = new Intent(this, QRCodeScanActivity.class);
        startActivityForResult(i, QRCODE_SCAN_REQUEST_CODE);
    }

    private void showQrcodeInfoMsg(Account_Asset__c asset, String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(AssetAddActivity.this).create();
        alertDialog.setTitle(R.string.warning);
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });
        alertDialog.show();
    }

    private void showSqrcodeInfoMsg(Account_Asset__c asset, String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(AssetAddActivity.this).create();
        alertDialog.setTitle(R.string.warning);
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                (dialog, which) -> {
                    dialog.dismiss();
                    if (!"".equals(asset.getSupplementaryQrCode()) || asset.getSupplementaryQrCode() != null) {
                        qrCodeTextView.setText(asset.getSupplementaryQrCode());
                        assetNameEdit.setText(asset.getAssetName());
                        if (asset.getType().equals(getString(R.string.account_asset_capacity_refrigerator))) {
                            assetCategorySpinner.setSelection(1);
                        } else if (asset.getType().equals(getString(R.string.account_asset_capacity_barrel_beer))) {
                            assetCategorySpinner.setSelection(2);
                        }
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        alertDialog.show();
    }

    private void showInfoMsg(String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(AssetAddActivity.this).create();
        alertDialog.setTitle(R.string.warning);
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @OnClick(R.id.cancel)
    public void cancel() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == QRCODE_SCAN_REQUEST_CODE) {
                String result = data.getStringExtra("scan result");
                AssetAddPresenter.AssetMsg msg = presenter.checkAssetExits(accountId, result);
                if (msg.isExist) {
                    if ("".equals(msg.asset__c.getSupplementaryQrCode()) || msg.asset__c.getSupplementaryQrCode() == null) {
                        showQrcodeInfoMsg(msg.asset__c, getAssetMsg(msg));
                    } else {
                        showSqrcodeInfoMsg(msg.asset__c, getAssetSqrMsg(msg));
                    }
                } else {
                    qrCodeTextView.setText(result);
                }

            }
        }
    }

    @Override
    public void saveAssetAndTrackingFinish(boolean success) {
        if (success) {
            finish();
        }
    }
}
