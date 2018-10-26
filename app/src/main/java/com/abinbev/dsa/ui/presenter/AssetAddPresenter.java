package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Account_Asset__c;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.checkoutRules.AccountAssetTracking__c;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.DateUtils;

import java.util.Date;
import java.util.List;

import rx.Observable;

import static com.abinbev.dsa.adapter.AssetsListAdapter.NO_SERIALIZED_RECORD_NAME;
import static com.abinbev.dsa.ui.presenter.AssetsListPresenter.Contact.ASSET_TRACKING_STATUS_NOT_THIS_POC;

public class AssetAddPresenter extends AbstractRxPresenter<AssetAddPresenter.ViewModel> implements Presenter<AssetAddPresenter.ViewModel> {

    public static final String TAG = AssetAddPresenter.class.getSimpleName();
    private static String trackingStatus = ASSET_TRACKING_STATUS_NOT_THIS_POC;
    private String visitId;
    private ViewModel view;

    public interface ViewModel {
        void saveAssetAndTrackingFinish(boolean success);
    }

    public AssetAddPresenter(ViewModel viewModel) {
        super();
        this.view = viewModel;
    }

    @Override
    public void start() {
        super.start();
    }

    public static class AssetMsg {
        public Account_Asset__c asset__c;
        public Account account;
        public boolean isExist = false;
    }

    public AssetMsg checkAssetExits(String accountId, String qrCode) {
        AssetMsg msg = new AssetMsg();
        Account_Asset__c asset__c = Account_Asset__c.getAccount_AssetByQrCode(accountId, qrCode);
        if (asset__c != null) {
            msg.isExist = true;
            msg.asset__c = asset__c;
            msg.account = Account.getById(asset__c.getAccountId());
        }
        return msg;
    }

    public void saveAssetAndTrackingToDb(String accountId, String qrCode, String assetName, String assetType) {
        Observable.fromCallable(() -> {
            visitId = getVisitId();
            String assetId = Account_Asset__c.getAccount_AssetBySqrCode(accountId, qrCode);
            if (assetId == null) {
                assetId = Account_Asset__c.createAccountAsset(NO_SERIALIZED_RECORD_NAME, ASSET_TRACKING_STATUS_NOT_THIS_POC, accountId, qrCode, assetName, assetType);
                AccountAssetTracking__c.createAssetTrackingRecord(assetId, visitId, trackingStatus, "", "", DateUtils.fromDateTimeToServerDateTime(new Date(System.currentTimeMillis())));
            } else {
                Account_Asset__c.updateAccountAsset(NO_SERIALIZED_RECORD_NAME, assetId, assetName, assetType);
                List<Account_Asset__c> assetList = Account_Asset__c.getAssetByAccountId(accountId);
                for (int i = 0; i < assetList.size(); i++) {
                    if (assetList.get(i).getId().equals(assetId)) {
                        List<AccountAssetTracking__c> trackingList = AccountAssetTracking__c.getTrackingByAssetIdAndVisitId(assetId, visitId);
                        if (trackingList.size() > 0) {
                            AccountAssetTracking__c.updateAssetTrackingRecord(trackingList.get(0).getId(), DateUtils.fromDateTimeToServerDateTime(new Date(System.currentTimeMillis())));
                        } else {
                            AccountAssetTracking__c.createAssetTrackingRecord(assetId, visitId, trackingStatus, "", "", DateUtils.fromDateTimeToServerDateTime(new Date(System.currentTimeMillis())));
                        }
                    }
                }
            }
            return assetId;
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(assetId -> view.saveAssetAndTrackingFinish(true),
                        error -> Log.e(TAG, "Error save Asset and tracking ", error));
    }

    private String getVisitId() {
        List<Event> events = Event.getAllCheckedInVisits();
        if (events.size() > 0) {
            Event e = events.get(0);
            if (e != null)
                return e.getId();
        }
        return "";
    }
}
