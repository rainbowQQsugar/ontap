package com.abinbev.dsa.activity;

/**
 * Created by lukaszwalukiewicz on 22.12.2015.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.OnClick;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.AssetsListAdapter;
import com.abinbev.dsa.model.Account_Asset__c;
import com.abinbev.dsa.model.AssetActions__c;
import com.abinbev.dsa.ui.presenter.AssetsListPresenter;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.PermissionManager;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.dsa.location.LocationHandler;
import com.salesforce.dsa.location.LocationHandlerProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssetsListActivity extends AppBaseDrawerActivity implements AssetsListPresenter.ViewModel {

    public static final String TAG = AssetsListActivity.class.getSimpleName();

    public static final String ACCOUNT_ID_EXTRA = "account_id";

    public static final int QRCODE_SCAN_REQUEST_CODE = 123;

    public static final String ARGS_ACCOUNT_ID = "account_id";
    public static final String ARGS_IS_CHECKED_IN = "checked_in";

    private AssetsListAdapter adapter;
    private AssetsListPresenter assetsListPresenter;
    private String accountId;
    private boolean isCheckedIn;
    private PopupMenu popupMenu;
    private List<AssetActions__c> actions;
    private AssetActions__c selectedAction;
    private LocationHandler locationHandler;
    private Set<String> wifiBSSID = new HashSet<>();
    private boolean pressWifiButton = true;
    final boolean[] broadcastReceived = {true};
    private BroadcastReceiver wifiReceiver;
    private static Handler handler = new Handler(Looper.getMainLooper());
    private WifiManager wifiManager;
    @Bind(R.id.new_caso)
    FloatingActionButton fab;

    @Bind(R.id.assets_list)
    ListView listView;

    @Bind(R.id.popup_menu_anchor)
    View popupMenuAnchor;

    @Bind(R.id.title)
    View title;

    @Bind(R.id.action_container)
    LinearLayout actionContainer;

    @Bind(R.id.actionSubmit)
    TextView submit;

    @Bind(R.id.actionCancel)
    TextView cancel;

    @Bind(R.id.instructions)
    TextView instructions;

    @Bind(R.id.assets_tracking_wifi_scan)
    Button scanWifiTag;

    @Bind(R.id.assets_tracking_add_new)
    Button addDeviceButton;

    @Bind(R.id.buttons_layout)
    LinearLayout buttonsLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("");
        Intent intent = getIntent();
        if (intent != null) {
            accountId = intent.getStringExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA);
            isCheckedIn = intent.getBooleanExtra(ARGS_IS_CHECKED_IN, false);
        }
        initAssetList();
        initCreateAssetAction();
        locationHandler = LocationHandlerProvider.createLocationHandler(this);
        initWifi();
        setButtonsVisible(isCheckedIn);
        if (isCheckedIn) {
            startScanWifi();
        }
    }

    private void setButtonsVisible(boolean visible) {
        if (visible) {
            buttonsLayout.setVisibility(View.VISIBLE);
        } else {
            buttonsLayout.setVisibility(View.GONE);
        }
    }

    private void initCreateAssetAction() {
        popupMenu = new PopupMenu(this, popupMenuAnchor);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item != null) {
                    launchNewCase(item.getItemId());
                }
                return true;
            }
        });
        if (!PermissionManager.getInstance().hasPermission(PermissionManager.CREATE_ASSETS)) {
            fab.setVisibility(View.GONE);
        }
    }

    private void initAssetList() {
        adapter = new AssetsListAdapter(this);
        listView.setAdapter(adapter);
        adapter.setAssetClickHandler(new AssetsListAdapter.AssetClickHandler() {
            @Override
            public void onAssetClick(AssetsListPresenter.AccountAsset asset, int position) {
                if (listView.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE) {
                    listView.clearChoices();
                    listView.setItemChecked(position, !listView.getCheckedItemPositions()
                            .get(position));
                    return;
                }
                if (asset != null)
                    showAssetDetails(asset.getAssetNeedToShow());
            }
        });
    }

    private void initWifi() {

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (broadcastReceived[0])
                    return;
                broadcastReceived[0] = true;
                List<ScanResult> results = wifiManager.getScanResults();
                for (ScanResult ap : results) {
                    wifiBSSID.add(ap.BSSID);
                }

                handler.postDelayed(() -> {
                    if (!isDestroyed()) {
                        scanWifiTag.setText(R.string.assets_wifi_scan);
                        scanWifiTag.setEnabled(true);
                        onWIFIScanFinish();
                        if (pressWifiButton) {
                            showInfoMsg(getString(R.string.asset_wifi_finish_prompt));
                        }
                    }
                }, 1000);
            }
        };
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void showInfoMsg(String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(AssetsListActivity.this).create();
        alertDialog.setTitle(R.string.warning);
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    private void onWIFIScanFinish() {
        adapter.changeAssetsStatus(wifiBSSID);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_assets_list_view;
    }

    @Override
    protected void onPause() {
        super.onPause();
        assetsListPresenter.stop();
    }

    @Override
    public void auditSuccess() {
        Toast.makeText(this, getString(R.string.audit_success), Toast.LENGTH_LONG).show();
        //SyncUtils.TriggerRefresh(this);
    }

    private void showAssetDetails(Account_Asset__c asset) {
        Intent intent = new Intent(AssetsListActivity.this, AssetViewActivity.class);
        intent.putExtra(AssetViewActivity.ASSET_ID_EXTRA, asset.getId());
        intent.putExtra(AssetsListActivity.ARGS_IS_CHECKED_IN, isCheckedIn);
        startActivity(intent);
    }

    @Override
    public void setAssets(List<AssetsListPresenter.AccountAsset> assets, String recordName) {
        adapter.setData(assets, recordName);
    }

    @Override
    public void setAssetActions(List<AssetActions__c> assetActions) {
        popupMenu.getMenu().clear();
        actions = assetActions;
        for (int i = 0; i < assetActions.size(); i++) {
            AssetActions__c assetAction = assetActions.get(i);
            popupMenu.getMenu().add(Menu.NONE, i, i, assetAction.getActionLabel());
        }
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
    protected void onResume() {
        super.onResume();
        //locationHandler.connect();
    }

    private void startScanWifi() {
        scanWifiTag.setText(R.string.assets_wifi_scan_in_progress);
        scanWifiTag.setEnabled(false);
        scanWifi();
    }

    @OnClick(R.id.assets_tracking_wifi_scan)
    public void onScanWifiTag() {
        pressWifiButton = true;
        scanWifiTag.setText(R.string.assets_wifi_scan_in_progress);
        scanWifiTag.setEnabled(false);
        scanWifi();
    }

    private void scanWifi() {
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            broadcastReceived[0] = false;
            wifiManager.startScan();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //locationHandler.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiReceiver != null) {
            unregisterReceiver(wifiReceiver);
            wifiReceiver = null;
        }
    }

    @Override
    public void onRefresh() {
        Intent intent = getIntent();
        if (intent != null) {
            accountId = intent.getStringExtra(ARGS_ACCOUNT_ID);
            if (assetsListPresenter == null) {
                assetsListPresenter = new AssetsListPresenter(accountId);
            }
            assetsListPresenter.setViewModel(this);
            assetsListPresenter.start();
        }
    }

    @OnClick(R.id.assets_tracking_add_new)
    public void onAddDeviceClick() {
        Intent intent = new Intent(AssetsListActivity.this, AssetAddActivity.class);
        intent.putExtra(AssetAddActivity.ACCOUNT_ID_EXTRA, accountId);
        intent.putExtra(AssetAddActivity.ARGS_IS_CHECKED_IN, isCheckedIn);
        startActivity(intent);
    }

    @OnClick(R.id.new_caso)
    public void onNewCasoClick() {
        popupMenu.show();
    }

    private void launchNewCase(int position) {
        selectedAction = actions.get(position);
        String action = selectedAction.getActionLabel();
        if (action != null && (AbInBevConstants.AssetActionFields.ACTION_ASSET_CASE.equals(selectedAction.getAction()) || AbInBevConstants.AssetActionFields.ACTION_AUDIT_CASE.equals(selectedAction.getAction()))) {
            showCreateNewCase(true);
        } else {
            Intent intent = new Intent(this, CasoEditActivity.class);
            intent.putExtra(CasoEditActivity.ACCOUNT_ID, accountId);
            intent.putExtra(CasoEditActivity.CASO_RECORD_TYPE, selectedAction.getRecordType());
            startActivityForResult(intent, CasoEditActivity.CASO_EDIT_REQUEST_CODE);
        }
    }

    private void showCreateNewCase(boolean show) {
        // clear checked items
        if (!show) {
            listView.clearChoices();

            for (int i = 0; i < listView.getChildCount(); i++) {
                if (listView.getChildAt(i) instanceof Checkable) {
                    ((Checkable) listView.getChildAt(i)).setChecked(false);
                }
            }
            adapter.notifyDataSetChanged();
        }
        if (show && AbInBevConstants.AssetActionFields.ACTION_ASSET_CASE.equals(selectedAction.getAction())) {
            instructions.setText(R.string.create_asset_case_instructions);
            submit.setText(R.string.create_case);
        } else {
            instructions.setText(R.string.audit_instructions);
            submit.setText(R.string.audit);
        }
        actionContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        instructions.setVisibility(show ? View.VISIBLE : View.GONE);
        title.setVisibility(show ? View.GONE : View.VISIBLE);
        fab.setVisibility(show ? View.GONE : View.VISIBLE);
        listView.setChoiceMode(show ? ListView.CHOICE_MODE_MULTIPLE : ListView.CHOICE_MODE_NONE);
    }

    @OnClick(R.id.actionCancel)
    public void cancelNewCase() {
        showCreateNewCase(false);
        selectedAction = null;
    }

    @OnClick(R.id.actionSubmit)
    public void createNewCase() {
        ArrayList<String> assets = new ArrayList<>();
        String assetId = null;
        if (listView.getCheckedItemCount() > 0) {
            SparseBooleanArray checked = listView.getCheckedItemPositions();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (checked.get(i)) {
                    assetId = adapter.getAssetWithPosition(i).getAssetNeedToShow()
                            .getId();
                    assets.add(assetId);
                }
            }
        }
        showCreateNewCase(false);
        if (AbInBevConstants.AssetActionFields.ACTION_AUDIT_CASE.equals(selectedAction.getAction())) {
            if (!assets.isEmpty()) {
                LatLng latlng = locationHandler.getCurrentLatLng();
                assetsListPresenter.auditAssets(assets, latlng);
            }
            return;
        }
        Intent intent = new Intent(this, AssetCaseEditActivity.class);
        intent.putExtra(CasoEditActivity.ACCOUNT_ID, accountId);
        intent.putExtra(CasoEditActivity.CASO_RECORD_TYPE, selectedAction.getActionLabel());
        intent.putExtra(AssetCaseEditActivity.CASO_ASSETS, assets);
        intent.putExtra(CasoEditActivity.ASSET_ID, assetId);
        startActivityForResult(intent, CasoEditActivity.CASO_EDIT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CasoEditActivity.CASO_EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, R.string.case_save_success, Toast.LENGTH_SHORT).show();
            //SyncUtils.TriggerRefresh(this);
        }
    }
}