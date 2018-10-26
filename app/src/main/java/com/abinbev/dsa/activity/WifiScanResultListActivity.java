package com.abinbev.dsa.activity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.ListView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.WifiScanResultAdapter;
import com.abinbev.dsa.ui.presenter.WifiScanResultListPresenter;
import com.abinbev.dsa.ui.presenter.WifiScanResultListPresenter.WifiScanResultRow;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * Created by lukaszwalukiewicz on 12.01.2016.
 */
public class WifiScanResultListActivity extends AppBaseDrawerActivity implements WifiScanResultListPresenter.ViewModel {

    private static final String ARGS_REQUIRED_WIFI_TAG = "required_wifi_tag";
    private static final String ARGS_WIFI_SCAN_RESULTS = "wifi_scan_results";

    WifiScanResultListPresenter presenter;

    WifiScanResultAdapter adapter;

    @Bind(R.id.list_view)
    ListView listView;

    public static Intent createIntent(Context context, String requiredWifiTag, ArrayList<ScanResult> scanResults) {
        return new Intent(context, WifiScanResultListActivity.class)
                .putExtra(ARGS_REQUIRED_WIFI_TAG, requiredWifiTag)
                .putExtra(ARGS_WIFI_SCAN_RESULTS, scanResults);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_wifi_scan_result_list;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String requiredWifiTag = getIntent().getStringExtra(ARGS_REQUIRED_WIFI_TAG);
        ArrayList<ScanResult> scanResults = getIntent().getParcelableArrayListExtra(ARGS_WIFI_SCAN_RESULTS);

        adapter = new WifiScanResultAdapter();
        listView.setAdapter(adapter);

        presenter = new WifiScanResultListPresenter(requiredWifiTag, scanResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.stop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override /* WifiScanResultListPresenter.ViewModel */
    public void setData(List<WifiScanResultRow> scanResultRows) {
        adapter.setData(scanResultRows);
    }

    @Override
    public void onRefresh() { }
}
