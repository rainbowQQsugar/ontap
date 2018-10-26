package com.abinbev.dsa.ui.presenter;

import android.net.wifi.ScanResult;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.utils.AppScheduler;
import com.salesforce.androidsyncengine.utils.ChainedComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Single;

import static com.salesforce.androidsyncengine.utils.ComparatorUtils.compareStringsIgnoreCase;

/**
 * Created by lukaszwalukiewicz on 22.12.2015.
 */
public class WifiScanResultListPresenter extends AbstractRxPresenter<WifiScanResultListPresenter.ViewModel> implements Presenter<WifiScanResultListPresenter.ViewModel> {

    private static final String TAG = "WifiScanResultListPrese";

    public static class WifiScanResultRow {
        public final String name;
        public final String value;

        public WifiScanResultRow(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    public interface ViewModel {
        void setData(List<WifiScanResultRow> scanResultRows);
    }

    private final String requiredWifiTag;

    private final List<ScanResult> wifiScanResults;

    public WifiScanResultListPresenter(String requiredWifiTag, List<ScanResult> wifiScanResults) {
        super();
        this.wifiScanResults = new ArrayList<>(wifiScanResults);
        this.requiredWifiTag = requiredWifiTag;
    }

    @Override
    public void start() {
        super.start();
        loadData();
    }

    private void loadData() {
        addSubscription(Single.fromCallable(
                () -> {
                    // Convert results to row data.
                    List<WifiScanResultRow> resultRows = new ArrayList<>();
                    for (ScanResult scanResult : wifiScanResults) {
                        resultRows.add(new WifiScanResultRow(scanResult.SSID, scanResult.BSSID));
                    }

                    // Sort rows by name.
                    sortRows(resultRows);

                    // First row should tell what we are searching for.
                    String requiredTagLabel = getString(R.string.wifi_scan_asset_tag_label);
                    resultRows.add(0, new WifiScanResultRow(requiredTagLabel, requiredWifiTag));

                    return resultRows;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        resultRows -> viewModel().setData(resultRows),
                        error -> Log.w(TAG, error)
                ));
    }

    private void sortRows(List<WifiScanResultRow> rows) {
        Collections.sort(rows, new ChainedComparator<>(
                (a, b) -> compareStringsIgnoreCase(a.name, b.name),
                (a, b) -> compareStringsIgnoreCase(a.value, b.value)
        ));
    }

    private String getString(int stringRes) {
        return ABInBevApp.getAppContext().getString(stringRes);
    }
}
