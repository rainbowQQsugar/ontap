package com.abinbev.dsa.ui.presenter;

import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.abinbev.dsa.model.Account_Asset__c;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.model.checkoutRules.AccountAssetTracking__c;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.ui.presenter.AssetsListPresenter.AccountAsset;
import com.abinbev.dsa.utils.DateUtils;
import com.abinbev.dsa.utils.PicklistUtils;
import com.salesforce.androidsyncengine.data.model.PicklistValue;


import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Single;

import static com.abinbev.dsa.ui.presenter.AssetsListPresenter.Contact.ASSET_TRACKING_STATUS_NOT_THIS_POC;

public class AssetViewPresenter extends AbstractRxPresenter<AssetViewPresenter.ViewModel> implements Presenter<AssetViewPresenter.ViewModel> {

    private static final String TAG = AssetViewPresenter.class.getSimpleName();

    private String assetId;
    private String visitId;

    private Map<String, String> trackingAndUserName;

    public static class ViewState implements Serializable {
        private AccountAsset accountAsset;
        private Map<String, String> undetectedReasonMap;
        private transient Attachment attachment;            // This item is too big to be serialized.

        public ViewState(AccountAsset accountAsset, Attachment attachment, Map<String, String> undetectedReasonMap) {
            this.accountAsset = accountAsset;
            this.attachment = attachment;
            this.undetectedReasonMap = undetectedReasonMap == null ? Collections.emptyMap() : undetectedReasonMap;
        }

        public AccountAssetTracking__c getTracking() {
            return accountAsset.accountAssetTracking__c;
        }

        public Map<String, String> getUndetectedReasonMap() {
            return undetectedReasonMap;
        }

        public void setTracking(AccountAssetTracking__c tracking) {
            if (accountAsset != null) {
                accountAsset.accountAssetTracking__c = tracking;
            }
        }

        public Account_Asset__c getAccountAsset() {
            return accountAsset.getAssetNeedToShow();
        }

        public Account_Asset__c getRealAccountAsset() {
            return accountAsset.getRealAsset();
        }

        public Attachment getAttachment() {
            return attachment;
        }

    }

    public interface ViewModel {
        void setState(ViewState state);

        void setTrackComplete(boolean success);

        ViewState getViewState();
    }

    public AssetViewPresenter(String assetId) {
        super();
        this.assetId = assetId;
        trackingAndUserName = new HashMap<>();
    }

    @Override
    public void start() {
        super.start();
        if (assetId != null) {
            loadInitialData();
        }
    }

    public void setLeftValue(TextView textView) {
        addSubscription(Observable.fromCallable(
                () -> {
                    String v = "";
                    AccountAssetTracking__c tracking__c = viewModel().getViewState().getTracking();
                    if (tracking__c != null) {
                        final String userId = tracking__c.getLastModifiedById();
                        String userName=null;
                        if(userId!=null){
                            userName = trackingAndUserName.get(userId);
                            if (userName == null) {
                                User user = User.getUserByUserId(userId);
                                if(user!=null){
                                    userName = user.getName();
                                    trackingAndUserName.put(userId, userName);
                                }
                            }
                        }
                        if (!TextUtils.isEmpty(userName)) {
                            v = userName;
                        }
                        String time = tracking__c.getTrackingTime();
                        if (time != null) {
                            time = DateUtils.formatDateStringCNLong2(time);
                        }
                        if (!TextUtils.isEmpty(time)) {
                            if (!TextUtils.isEmpty(v)) {
                                v = v + ", ";
                            }
                            v = v + time;
                        }
                    }
                    return v;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        text -> {
                            if (textView != null) {
                                textView.setText(text);
                            }
                        },
                        error -> Log.e(TAG, "Error fetching Asset: " + assetId, error)
                ));
    }

    private void loadInitialData() {
        addSubscription(Observable.fromCallable(
                () -> {
                    final AccountAsset accountAsset = new AccountAsset();
                    final Account_Asset__c asset = Account_Asset__c.getById(assetId);
                    accountAsset.setAsset__c(asset);
                    List<AccountAssetTracking__c> tracking2 = AccountAssetTracking__c.getTrackingByAssetId(assetId);
                    if (tracking2 != null && !tracking2.isEmpty()) {
                        accountAsset.accountAssetTracking__c = tracking2.get(0);
                    }
                    Attachment attachment = Attachment.getAssetPhoto(assetId);
                    Map<String, String> stringStringMap = loadUndetectedReasonMap();
                    return new ViewState(accountAsset, attachment, stringStringMap);
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        viewState -> viewModel().setState(viewState),
                        error -> Log.e(TAG, "Error fetching Asset: " + assetId, error)
                ));
    }

    private Map<String, String> loadUndetectedReasonMap() {
        HashMap<String, List<PicklistValue>> values = PicklistUtils.getMetadataPicklistValues(
                AbInBevConstants.AbInBevObjects.ACCOUNT_ASSET_C, AbInBevConstants.AccountAssetFields.REASON);

        Map<String, String> reasons = new HashMap();
        for (Map.Entry<String, List<PicklistValue>> entry : values.entrySet()) {
            String key = entry.getKey();
            List<PicklistValue> vals = entry.getValue();

            if (key.equals(AbInBevConstants.AccountAssetFields.REASON)) {
                for (PicklistValue pv : vals) {
                    reasons.put(pv.getLabel(), pv.getValue());
                }
            }
        }
        return reasons;
    }

    public void setTracking(String status, String reasonStr, String commentStr) {
        addSubscription(Single.fromCallable(() ->
        {
            AccountAssetTracking__c tracking__c = viewModel().getViewState().getTracking();
            if (tracking__c == null ||
                    TextUtils.isEmpty(getVisitId()) ||ASSET_TRACKING_STATUS_NOT_THIS_POC.equals(tracking__c.getStatus())||
                    !getVisitId().equals(tracking__c.getVisitId())) {
                tracking__c = new AccountAssetTracking__c();
            }
            tracking__c.setParentId(assetId);
            tracking__c.setVisitId(getVisitId());
            tracking__c.setStatus(status);
            tracking__c.setReason(reasonStr);
            tracking__c.setComment(commentStr);
            tracking__c.setTrackingTime(DateUtils.fromDateTimeToServerDateTime(new Date(System.currentTimeMillis())));
            viewModel().getViewState().setTracking(tracking__c);
            return AccountAssetTracking__c.upsertAssetTrackingRecord(tracking__c);
        })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(success -> viewModel().setTrackComplete(success),
                        error -> Log.e(TAG, "Error setTracking: " + assetId, error)));
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
