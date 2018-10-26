package com.abinbev.dsa.activity;

import android.content.Intent;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.AssetInPocListAdapter;
import com.abinbev.dsa.model.Account_Asset__c;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.checkoutRules.AccountAssetTracking__c;
import com.abinbev.dsa.service.AttachmentUploadService;
import com.abinbev.dsa.ui.presenter.AssetViewPresenter;
import com.abinbev.dsa.ui.presenter.AssetsListPresenter;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AttachmentUtils;
import com.abinbev.dsa.utils.PermissionManager;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static com.abinbev.dsa.activity.AssetsListActivity.ARGS_IS_CHECKED_IN;
import static com.abinbev.dsa.activity.AssetsListActivity.QRCODE_SCAN_REQUEST_CODE;
import static com.abinbev.dsa.ui.presenter.AssetsListPresenter.Contact.ASSET_TRACKING_REASON_OTHER_ISSUES;
import static com.abinbev.dsa.ui.presenter.AssetsListPresenter.Contact.ASSET_TRACKING_STATUS_LOST;
import static com.abinbev.dsa.ui.presenter.AssetsListPresenter.Contact.ASSET_TRACKING_STATUS_QUALIFIED;
import static com.abinbev.dsa.ui.presenter.AssetsListPresenter.Contact.ASSET_TRACKING_STATUS_UNDETECTED;

public class AssetViewActivity extends DynamicViewActivity implements AssetViewPresenter.ViewModel {

    public static final String ASSET_ID_EXTRA = "ASSET_ID";

    private static final String SAVED_VIEW_STATE = "saved_view_state";
    private static final String STR_LAST_MODIFIED_BY = "上次修改人";
    private AssetViewPresenter.ViewState viewState;

    private String assetId;

    private boolean isCheckedIn;

    private AssetViewPresenter assetViewPresenter;
    /**
     * save photo uri if need to upload to service.
     */
    private Uri photoUri;
    private boolean fromActivityResult = false;
    @Bind(R.id.asset_image)
    ImageView assetImage;

    @Bind(R.id.assets_tracking_qr_scan)
    Button scanQRCode;

    @Bind(R.id.assets_tracking_lost)
    Button setToLostButton;

    @Bind(R.id.image_layout)
    ViewGroup imageViewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null) {
            assetId = getIntent().getStringExtra(ASSET_ID_EXTRA);
            isCheckedIn = getIntent().getBooleanExtra(ARGS_IS_CHECKED_IN, false);
        }

        if (savedInstanceState != null) {
            viewState = (AssetViewPresenter.ViewState) savedInstanceState.getSerializable(SAVED_VIEW_STATE);
        }

        getSupportActionBar().setTitle(getString(R.string.title_activity_asset));
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (!isCheckedIn) {
            scanQRCode.setVisibility(GONE);
            setToLostButton.setVisibility(GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (assetViewPresenter == null) {
            assetViewPresenter = new AssetViewPresenter(assetId);
        }
        assetViewPresenter.setViewModel(this);
        if (!fromActivityResult) {
            assetViewPresenter.start();
            fromActivityResult = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        assetViewPresenter.stop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_VIEW_STATE, viewState);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.dynamic_asset;
    }

    @Override
    public void setState(AssetViewPresenter.ViewState viewState) {
        this.viewState = viewState;
        setAsset(viewState.getAccountAsset());
        setAssetImage(viewState.getAttachment());
        setButtons();
        //if (isAssetKnownNeedChangeTracking()) {
        //    assetViewPresenter.setTracking("", "", "");
        //}
    }

    @Override
    public void setTrackComplete(boolean success) {
        reFreshUI();
    }

    @Override
    public AssetViewPresenter.ViewState getViewState() {
        return viewState;
    }

    private void setButtons() {
        if (isAssetNotOfThisPoc()) {
            scanQRCode.setVisibility(GONE);
            setToLostButton.setVisibility(GONE);
        }
    }

    private void setAsset(Account_Asset__c asset) {
        if (asset != null) {
            getSupportActionBar().setSubtitle(asset.getName());
            buildLayout(AbInBevConstants.AbInBevObjects.ACCOUNT_ASSET_C, asset);
            removeModifyViewAndAddTrackingTimeView();
        }
    }

    private void removeModifyViewAndAddTrackingTimeView() {
        findView(mainLayout);
    }

    private void findView(ViewGroup view) {
        int viewCount = view.getChildCount();
        for (int i = 0; i < viewCount; i++) {
            View childView = view.getChildAt(i);
            if (childView instanceof ViewGroup) {
                findView((ViewGroup) childView);
            } else if (childView instanceof TextView) {
                String text = ((TextView) childView).getText().toString();
                if (STR_LAST_MODIFIED_BY.equals(text)) {
                    ((TextView) childView).setText(R.string.asset_tracking_last_modified);
                    if (childView.getParent() instanceof LinearLayout) {
                        TextView value = (TextView) ((LinearLayout) childView.getParent()).findViewById(R.id.right_value);
                        if (value != null) {
                            assetViewPresenter.setLeftValue(value);
                        }
                    }
                }
            }
        }
    }

    private void setAssetImage(Attachment attachment) {
        //if null, use default image, otherwise, display attachment
        if (attachment == null || assetId == null) {
            // assetImage.setImageDrawable(getResources().getDrawable(R.drawable.asset_detail_placeholder));
        } else {
            int width = imageViewLayout.getWidth();
            Picasso.with(this)
                    .load(new File(attachment.getAssetFilePath(this, assetId)))
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .resize(width, width)
                    .into(assetImage);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fromActivityResult = true;
        if (resultCode == RESULT_OK) {
            if (AttachmentUtils.SELECT_PHOTO_REQUEST_CODE == requestCode) {
                Uri uri = (data != null && data.getData() != null) ? data.getData() : AttachmentUtils.fileUri;
                if (uri == null) {
                    return;
                }
                photoUri = uri;
                showAssetStatusUndetectedDialog(viewState.getUndetectedReasonMap());
            } else if (requestCode == QRCODE_SCAN_REQUEST_CODE) {
                String result = data.getStringExtra("scan result");
                verifyQRCode(result);
            }
        }
    }

    private AlertDialog buildTakePhotoDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(AssetViewActivity.this).create();
        alertDialog.setTitle(R.string.warning);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.take_photo_button),
                (dialog, which) -> onTakePicture());
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                (dialog, which) -> showInfoMsg(getString(R.string.asset_photo_failure)));

        return alertDialog;
    }

    /**
     * current logic
     * when asset->status is one of "In Stock" or "Installed"
     * tracking->status is  "Not asset of this POC"
     * need change tracking status -> to be empty string.
     *
     * @return need change tracking.
     */
//    private boolean isAssetKnownNeedChangeTracking() {
//        final String assetStatus = viewState.getRealAccountAsset().getStatus();
//        AccountAssetTracking__c tracking__c = viewState.getTracking();
//        return (AssetInPocListAdapter.ASSET_STATUS_IN_STOCK.equals(assetStatus) ||
//                AssetInPocListAdapter.ASSET_STATUS_INSTALLED.equals(assetStatus)) &&
//                tracking__c != null &&
//                AssetsListPresenter.Contact.ASSET_TRACKING_STATUS_NOT_THIS_POC.equals(tracking__c.getStatus());
//    }
    private boolean isAssetNotOfThisPoc() {
        final String assetStatus = viewState.getRealAccountAsset().getStatus();
        AccountAssetTracking__c tracking__c = viewState.getTracking();
        return !Account_Asset__c.ASSET_STATUS_IN_STOCK.equals(assetStatus) &&
                !Account_Asset__c.ASSET_STATUS_INSTALLED.equals(assetStatus) &&
                tracking__c != null &&
                AssetsListPresenter.Contact.ASSET_TRACKING_STATUS_NOT_THIS_POC.equals(tracking__c.getStatus());
    }

    private AlertDialog buildSetToLostDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(AssetViewActivity.this).create();
        alertDialog.setTitle(R.string.warning);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.set_to_lost_button),
                (dialog, which) -> setToLost());
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                (dialog, which) -> dialog.dismiss());

        return alertDialog;
    }

    private void setToLost() {
        assetViewPresenter.setTracking(ASSET_TRACKING_STATUS_LOST, "", "");
    }

    private void askToTakePhotoForQr(String name, boolean detected) {
        AlertDialog alertDialog = buildTakePhotoDialog(detected ?
                getString(R.string.asset_photo_trigger, name) : getString(R.string.asset_detection_failed));
        alertDialog.show();
    }

    private void updateQrAssetStatus(boolean detected) {
        if (!detected) {
            if (isPhotoTakingRequired()) {
                askToTakePhotoForQr(viewState.getAccountAsset().getAssetName(), false);
            } else {
                setAssetStatusUndetected();
            }
        } else {
            setAssetStatusQualified();
        }
    }

    private void verifyQRCode(String result) {
        Account_Asset__c asset = viewState.getAccountAsset();
        updateQrAssetStatus(asset.getQRcode().equals(result));
    }

    /**
     * when undetected do not need to take photo use.
     */
    private void setAssetStatusUndetected() {
        assetViewPresenter.setTracking(ASSET_TRACKING_STATUS_UNDETECTED, ASSET_TRACKING_REASON_OTHER_ISSUES, "");
    }

    private void showAssetStatusUndetectedDialog(Map<String, String> reasonsMap) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_asset_scan_undetected, null);
        builder.setView(dialogView);

        EditText comment = (EditText) dialogView.findViewById(R.id.commentEt);
        comment.setInputType(InputType.TYPE_CLASS_TEXT);
        Spinner reason = (Spinner) dialogView.findViewById(R.id.reasonsSp);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item,
                new ArrayList<>(reasonsMap.keySet()));
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reason.setAdapter(spinnerArrayAdapter);

        builder.setPositiveButton(R.string.save, (dialog, which) -> {

            TextView textView = (TextView) reason.getSelectedView();
            final String reasonStr = textView.getText().toString();
            final String reasonNeedToStr = reasonsMap.get(reasonStr);
            final String commentStr = comment.getText().toString();
            assetViewPresenter.setTracking(ASSET_TRACKING_STATUS_UNDETECTED, reasonNeedToStr, commentStr);

            if (photoUri != null) {
                setAssetImage(photoUri);
                Intent intent = AttachmentUploadService.uploadAssetPhoto(this, photoUri, assetId);
                startService(intent);
            }
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> showInfoMsg(getString(R.string.asset_photo_failure)));
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void setAssetStatusQualified() {
        assetViewPresenter.setTracking(ASSET_TRACKING_STATUS_QUALIFIED, "", "");
        Toast.makeText(this, R.string.asset_tracking_success, Toast.LENGTH_SHORT).show();
    }

    private void showInfoMsg(String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(AssetViewActivity.this).create();
        alertDialog.setTitle(R.string.warning);
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    public boolean isPhotoTakingRequired() {
        return PermissionManager.getInstance().hasPermission(PermissionManager.ASSET_PHOTO_REQUIRED);
    }

    @OnClick(R.id.assets_tracking_qr_scan)
    public void onScanQRCode() {
        Intent i = new Intent(this, QRCodeScanActivity.class);
        startActivityForResult(i, QRCODE_SCAN_REQUEST_CODE);
    }

    @OnClick(R.id.assets_tracking_lost)
    public void setAssetLost() {
        AlertDialog dialog = buildSetToLostDialog(getString(R.string.set_to_lost_msg));
        dialog.show();
    }

    private void reFreshUI() {
        setAsset(viewState.getAccountAsset());
    }

    public void onTakePicture() {
        AttachmentUtils.takePhoto(this);
    }

    public void onMoreInfo(String requiredTag, List<ScanResult> results) {
        ArrayList<ScanResult> arrayList = new ArrayList<>(results);
        startActivity(WifiScanResultListActivity
                .createIntent(this, requiredTag, arrayList));
    }

    private void setAssetImage(Uri uri) {
        int width = imageViewLayout.getWidth();
        Picasso.with(this)
                .load(uri)
                .memoryPolicy(MemoryPolicy.NO_CACHE).resize(width, width)
                .into(assetImage);
    }
}
