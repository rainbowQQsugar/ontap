package com.abinbev.dsa.ui.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.AssetsListAdapter;
import com.abinbev.dsa.model.Account_Asset__c;
import com.abinbev.dsa.model.AssetActions__c;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.model.checkoutRules.AccountAssetTracking__c;
import com.abinbev.dsa.utils.AppScheduler;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.androidsdk.accounts.UserAccountManager;
import com.salesforce.androidsyncengine.utils.JSONUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by lukaszwalukiewicz on 22.12.2015.
 */
public class AssetsListPresenter extends AbstractRxPresenter<AssetsListPresenter.ViewModel> implements Presenter<AssetsListPresenter.ViewModel> {

    public static final String TAG = AssetsListPresenter.class.getSimpleName();

    private String accountId;
    private String visitId;

    public interface ViewModel {
        void setAssets(List<AccountAsset> assets, String recordName);

        void setAssetActions(List<AssetActions__c> assetActions);

        void auditSuccess();

        //void changeAssetsStatus(Set<String> wifiBSSID);
        //    void updateAssetStatus(String id);
        //
    }

    public interface Contact {
        String ASSET_TRACKING_STATUS_QUALIFIED = "Qualified";
        String ASSET_TRACKING_STATUS_UNDETECTED = "Undetected";
        String ASSET_TRACKING_STATUS_NOT_THIS_POC = "Not asset of this POC";
        String ASSET_TRACKING_STATUS_LOST = "Lost";
        String ASSET_TRACKING_REASON_QR_CODE_ISSUE = "QR Code Issue";
        String ASSET_TRACKING_REASON_WITHOUT_ANY_ASSET_TAG = "Without any asset tag";
        String ASSET_TRACKING_REASON_OTHER_ISSUES = "Other Issues";
    }

    public static class AccountAsset implements Serializable {

        private Account_Asset__c asset__c;
        /**
         * in list asset status need show accountAssetTracking__c.status
         * so assetNeedToShow.status is accountAssetTracking__c.status
         */
        private Account_Asset__c assetNeedToShow;

        public AccountAssetTracking__c accountAssetTracking__c;

        public Account_Asset__c getRealAsset() {
            return asset__c;
        }

        public Account_Asset__c getAssetNeedToShow() {
            if (assetNeedToShow == null) {
                assetNeedToShow = new Account_Asset__c(JSONUtils.deepCopy(asset__c.toJson()));
            }
            if (asset__c != null) {
                if (accountAssetTracking__c != null) {
                    final String trackingStatus = accountAssetTracking__c.getStatus();
                    if (trackingStatus != null) {
                        switch (trackingStatus) {
                            case Contact.ASSET_TRACKING_STATUS_QUALIFIED:
                                assetNeedToShow.setStatus(ABInBevApp.getAppContext().getString(R.string.account_asset_tracking_status_qualified));
                                break;
                            case Contact.ASSET_TRACKING_STATUS_UNDETECTED:
                                assetNeedToShow.setStatus(ABInBevApp.getAppContext().getString(R.string.account_asset_tracking_status_undetected));
                                break;
                            case Contact.ASSET_TRACKING_STATUS_NOT_THIS_POC:
                                if (Account_Asset__c.ASSET_STATUS_INSTALLED.equals(asset__c.getStatus()) ||
                                        Account_Asset__c.ASSET_STATUS_IN_STOCK.equals(asset__c.getStatus())) {
                                    assetNeedToShow.setStatus("");
                                } else {
                                    assetNeedToShow.setStatus(ABInBevApp.getAppContext().getString(R.string.account_asset_tracking_status_no_poc));
                                }
                                break;
                            case Contact.ASSET_TRACKING_STATUS_LOST:
                                assetNeedToShow.setStatus(ABInBevApp.getAppContext().getString(R.string.account_asset_tracking_status_lost));
                                break;
                            default:
                                assetNeedToShow.setStatus(trackingStatus);
                                break;
                        }
                    }
                    asset__c.setReason(accountAssetTracking__c.getReason());
                    asset__c.setComment(accountAssetTracking__c.getComment());

                } else {
                    assetNeedToShow.setStatus("");
                }
            }
            return assetNeedToShow;
        }

        public void setAsset__c(Account_Asset__c asset__c) {
            this.asset__c = asset__c;
        }


        /**
         * use ONTAP__AccountAssetTracking__c.ONTAP__Asset_Status__c value
         * to find ONTAP__AccountAssetTracking__c.ONTAP__Asset_Status__c key
         * for chinese value is "匹配" key is "Qualified"
         * for english value is "Qualified" key is "Qualified"
         *
         * @param showTrackingStatus ONTAP__AccountAssetTracking__c.ONTAP__Asset_Status__c value
         * @return ONTAP__AccountAssetTracking__c.ONTAP__Asset_Status__c key
         */
        public static String getTrackingStatus(String showTrackingStatus) {
            final String showQualified = ABInBevApp.getAppContext().getString(R.string.account_asset_tracking_status_qualified);
            final String showUndetected = ABInBevApp.getAppContext().getString(R.string.account_asset_tracking_status_undetected);
            final String showNotThisPoc = ABInBevApp.getAppContext().getString(R.string.account_asset_tracking_status_no_poc);
            final String showLost = ABInBevApp.getAppContext().getString(R.string.account_asset_tracking_status_lost);
            if (showQualified.equals(showTrackingStatus)) {
                return Contact.ASSET_TRACKING_STATUS_QUALIFIED;
            } else if (showUndetected.equals(showTrackingStatus)) {
                return Contact.ASSET_TRACKING_STATUS_UNDETECTED;
            } else if (showNotThisPoc.equals(showTrackingStatus)) {
                return Contact.ASSET_TRACKING_STATUS_NOT_THIS_POC;
            } else if (showLost.equals(showTrackingStatus)) {
                return Contact.ASSET_TRACKING_STATUS_LOST;
            }
            return showTrackingStatus;
        }
    }

    public AssetsListPresenter(String accountId) {
        super();
        this.accountId = accountId;
    }

    @Override
    public void start() {
        super.start();
        getCustomerAssets(AssetsListAdapter.SERIALIZED_RECORD_NAME);
        getCustomerAssets(AssetsListAdapter.NO_SERIALIZED_RECORD_NAME);
        getAssetActions();
    }

    private void getCustomerAssets(final String recordName) {
        addSubscription(Observable.fromCallable(
                () -> {
                    List<AccountAsset> accountAssets = new ArrayList<>();
                    List<Account_Asset__c> assets = Account_Asset__c.getCustomerAssets(accountId, recordName);
                    for (Account_Asset__c asset__c : assets) {
                        AccountAsset accountAsset = new AccountAsset();
                        accountAsset.asset__c = asset__c;
                        List<AccountAssetTracking__c> tracking2 = AccountAssetTracking__c.getTrackingByAssetId(asset__c.getId());
                        if (tracking2 != null && !tracking2.isEmpty()) {
                            accountAsset.accountAssetTracking__c = tracking2.get(0);
                        }
                        accountAssets.add(accountAsset);
                    }
                    return accountAssets;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        assets -> {
                            viewModel().setAssets(assets, recordName);
                        },
                        error -> Log.e(TAG, "Error getting all assets: ", error)
                ));
    }

    private void getAssetActions() {
        addSubscription(Observable.fromCallable(
                () -> {
                    String userId = UserAccountManager.getInstance().getStoredUserId();
                    User user = User.getUserByUserId(userId);
                    return AssetActions__c.getByCountryCode(user.getCountry());
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        assetActions -> viewModel().setAssetActions(assetActions),
                        error -> Log.e(TAG, "Error getting asset actions: ", error)
                ));
    }

    public void auditAssets(final List<String> assetIds, final LatLng latlng) {
        addSubscription(Observable.fromCallable(
                () -> {
                    for (String id : assetIds) {
                        Account_Asset__c.auditAsset(id, latlng);
                    }
                    return true;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        success -> viewModel().auditSuccess(),
                        error -> Log.e(TAG, "onError: ", error)
                ));
    }

    private String getVisitId() {
        if (visitId != null) {
            return visitId;
        }
        List<Event> events = Event.getAllCheckedInVisits();
        if (events.size() > 0) {
            Event e = events.get(0);
            if (e != null) {
                visitId = e.getId();
            }
            return visitId;
        }
        return "";
    }
}
