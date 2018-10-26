package com.abinbev.dsa.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account_Asset__c;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.model.checkoutRules.AccountAssetTracking__c;
import com.abinbev.dsa.ui.presenter.AssetsListPresenter;
import com.abinbev.dsa.ui.presenter.AssetsListPresenter.AccountAsset;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abinbev.dsa.ui.presenter.AssetsListPresenter.Contact.ASSET_TRACKING_STATUS_NOT_THIS_POC;
import static com.abinbev.dsa.ui.presenter.AssetsListPresenter.Contact.ASSET_TRACKING_STATUS_QUALIFIED;
import static com.abinbev.dsa.ui.presenter.AssetsListPresenter.Contact.ASSET_TRACKING_STATUS_UNDETECTED;

/**
 * Created by lukaszwalukiewicz on 22.12.2015.
 */
public class AssetsListAdapter extends BaseAdapter {

    public static final String TAG = AssetsListAdapter.class.getSimpleName();
    final protected CompositeSubscription subscriptions = new CompositeSubscription();
    private ViewGroup parentView;
    public static final String SERIALIZED_RECORD_NAME = "Serialized Asset";
    public static final String NO_SERIALIZED_RECORD_NAME = "Non-Serialized";
    private static final int TYPE_ASSET_ITEM = 0;
    private List<AccountAsset> serializedAssets;
    private List<AccountAsset> noSerializedAssets;
    Context context;
    private AssetClickHandler assetClickHandler;
    private String visitId;
    private Map<String, String> trackingAndUserName;
    public AssetsListAdapter(Context applicationContext) {
        serializedAssets = new ArrayList<>();
        noSerializedAssets = new ArrayList<>();
        this.context = applicationContext;
        trackingAndUserName=new HashMap<>();
    }

    public interface AssetClickHandler {
        void onAssetClick(AccountAsset asset, int position);
    }

    public void setAssetClickHandler(AssetClickHandler assetClickHandler) {
        this.assetClickHandler = assetClickHandler;
    }

    /**
     * refresh singleRow
     *
     * @param mListView listView
     * @param position  position
     */
    private void updateSingleRow(ListView mListView, int position) {
        if (mListView != null) {
            int visiblePos = mListView.getFirstVisiblePosition();
            int offset = position - visiblePos;
            int length = mListView.getChildCount();
            if ((offset < 0) || (offset >= length)) return;
            View convertView = mListView.getChildAt(offset);
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            if (getItemViewType(position) == TYPE_ASSET_ITEM && viewHolder != null) {
                final AccountAsset asset = getAssetWithPosition(position);
                if (asset != null) {
                    viewHolder.setupWithAsset(asset);
                    convertView.setOnClickListener((v) -> {
                        if (assetClickHandler != null) {
                            assetClickHandler.onAssetClick(asset, position);
                        }
                    });
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ASSET_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getCount() {
        return serializedAssets.size() + noSerializedAssets.size();
    }

    @Override
    public Object getItem(int position) {
        return getAssetWithPosition(position);
    }

    public void changeAssetsStatus(Set<String> wifiBSSID) {
        upsertRecord(noSerializedAssets, wifiBSSID);
        upsertRecord(serializedAssets, wifiBSSID);
    }

    private void upsertRecord(List<AccountAsset> accountAssets, Set<String> wifiBSSID) {
        subscriptions.add(Observable.create((subscriber) -> {
            for (AccountAsset accountAsset : accountAssets) {
                AccountAssetTracking__c assetTracking = accountAsset.accountAssetTracking__c;
                if(ASSET_TRACKING_STATUS_NOT_THIS_POC.equals(accountAsset.getRealAsset().getStatus())){
                    continue;
                }
                if (wifiBSSID.contains(accountAsset.getRealAsset().getWifiTag())) {
                    if (assetTracking == null ||
                            TextUtils.isEmpty(getVisitId()) ||
                            !getVisitId().equals(assetTracking.getVisitId())) {
                        assetTracking = new AccountAssetTracking__c();
                    }
                    assetTracking.setStatus(ASSET_TRACKING_STATUS_QUALIFIED);
                    assetTracking.setParentId(accountAsset.getRealAsset().getId());
                    assetTracking.setVisitId(getVisitId());
                    assetTracking.setComment("");
                    assetTracking.setReason("");
                    assetTracking.setTrackingTime(DateUtils.fromDateTimeToServerDateTime(new Date(System.currentTimeMillis())));
                    AccountAssetTracking__c.upsertAssetTrackingRecord(assetTracking);
                    accountAsset.accountAssetTracking__c = assetTracking;
                    subscriber.onNext(accountAsset);
                } else {
                    boolean insert=assetTracking == null || !getVisitId().equals(assetTracking.getVisitId());
                    insert=insert||ASSET_TRACKING_STATUS_NOT_THIS_POC.equals(assetTracking.getStatus());
                    if (insert) {
                        assetTracking = new AccountAssetTracking__c();
                        assetTracking.setStatus(ASSET_TRACKING_STATUS_UNDETECTED);
                        assetTracking.setParentId(accountAsset.getRealAsset().getId());
                        assetTracking.setVisitId(getVisitId());
                        assetTracking.setComment("");
                        assetTracking.setReason("");
                        assetTracking.setTrackingTime(DateUtils.fromDateTimeToServerDateTime(new Date(System.currentTimeMillis())));
                        AccountAssetTracking__c.upsertAssetTrackingRecord(assetTracking);
                        accountAsset.accountAssetTracking__c = assetTracking;
                        subscriber.onNext(accountAsset);
                    }
                }
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(assets -> updateAdapter((AccountAsset) assets)
                        , error -> Log.e(TAG, "Error upsertRecord, ", error)));

    }

    private void updateAdapter(AccountAsset assets) {
        int pos = getPosition(assets);
        if (parentView != null && parentView instanceof ListView) {
            updateSingleRow((ListView) parentView, pos);
        }
        //notifyDataSetChanged();
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

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        if (getAssetWithPosition(position) == null) return false;
        else return true;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        parentView = parent;
        ViewHolder holder = null;
        int rowType = getItemViewType(position);
        if (convertView == null) {
            switch (rowType) {
                case TYPE_ASSET_ITEM:
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    convertView = inflater.inflate(R.layout.asset_list_item, parent, false);
                    convertView.setTag(new ViewHolder(convertView));
                    holder = (ViewHolder) convertView.getTag();
                    final AccountAsset asset = getAssetWithPosition(position);
                    holder.setupWithAsset(asset);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (assetClickHandler != null) {
                                assetClickHandler.onAssetClick(asset, position);
                            }
                        }
                    });
                    break;
            }
        } else if (rowType == TYPE_ASSET_ITEM) {
            holder = (ViewHolder) convertView.getTag();
            final AccountAsset asset = getAssetWithPosition(position);
            holder.setupWithAsset(asset);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (assetClickHandler != null) {
                        assetClickHandler.onAssetClick(asset, position);
                    }
                }
            });
        }
        return convertView;
    }

    public void setData(List<AccountAsset> assets, String recordName) {
        if (recordName.equals(AssetsListAdapter.SERIALIZED_RECORD_NAME)) {
            this.serializedAssets.clear();
            this.serializedAssets = assets;
        } else if (recordName.equals(AssetsListAdapter.NO_SERIALIZED_RECORD_NAME)) {
            this.noSerializedAssets.clear();
            this.noSerializedAssets = assets;
        }
        this.notifyDataSetChanged();
    }

    private int getPosition(AccountAsset accountAsset) {
        int position = -1;
        if (accountAsset != null) {
            String id = accountAsset.getRealAsset().getId();
            int count = getCount();
            for (int i = 0; i < count; i++) {
                AccountAsset accountAsset1 = getAssetWithPosition(i);
                if (accountAsset1 != null) {
                    if (id != null && id.equals(accountAsset1.getRealAsset().getId())) {
                        return i;
                    }
                }
            }
        }
        return position;
    }

    public AccountAsset getAssetWithPosition(int position) {
        AccountAsset asset = null;
        int serializedAssetsCount = serializedAssets.size();
        int noSerializedAssetsCount = noSerializedAssets.size();
        if (position < serializedAssetsCount) {
            asset = serializedAssets.get(position);
        } else if (position < (serializedAssetsCount + noSerializedAssetsCount)) {
            asset = noSerializedAssets.get(position - serializedAssetsCount);
        }
        return asset;
    }

    public class ViewHolder {
        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }

        @Bind(R.id.asset_code)
        public TextView assetCode;

        @Bind(R.id.asset_name)
        public TextView assetName;

        @Bind(R.id.asset_type)
        public TextView assetType;

        @Bind(R.id.asset_status)
        public TextView assetStatus;
        @Bind(R.id.asset_tracking_time)
        public TextView trackingTime;

        public void setupWithAsset(AccountAsset accountAsset) {
            final Account_Asset__c asset = accountAsset.getAssetNeedToShow();
            this.assetCode.setText(asset.getCode());
            this.assetType.setText(asset.getType());
            this.assetName.setText(asset.getAssetName());
            this.assetStatus.setText(asset.getStatus());
            Observable.fromCallable(
                    () -> {
                        String time = "";
                        AccountAssetTracking__c tracking__c = accountAsset.accountAssetTracking__c;
                        if (tracking__c != null) {
                            time= tracking__c.getTrackingTime();
                            if (time != null) {
                                time = DateUtils.formatDateStringCNLong2(time);
                            }
                        }
                        return time;
                    })
                    .subscribeOn(AppScheduler.background())
                    .observeOn(AppScheduler.main())
                    .subscribe(
                            text -> {
                                if (this.trackingTime != null) {
                                    this.trackingTime.setText(text);
                                }
                            },
                            error -> Log.e(TAG, "Error set trackingTime: ", error)
                    );
            // this.trackingTime.setText(asset);
            String statusKey = AssetsListPresenter.AccountAsset.getTrackingStatus(asset.getStatus());
            if (ASSET_TRACKING_STATUS_QUALIFIED.equals(statusKey)) {
                this.assetStatus.setTextColor(Color.GREEN);
            } else {
                this.assetStatus.setTextColor(Color.RED);
            }
        }

    }

    public void sortSerializedAssetsByOrderNumber(final boolean ascending) {
        sortByOrderNumber(ascending, serializedAssets);
    }

    public void sortNoSerializedAssetsByOrderNumber(final boolean ascending) {
        sortByOrderNumber(ascending, noSerializedAssets);
    }

    public void sortSerializedAssetsByName(final boolean ascending) {
        sortByName(ascending, serializedAssets);
    }

    public void sortNoSerializedAssetsByName(final boolean ascending) {
        sortByName(ascending, noSerializedAssets);
    }

    public void sortSerializedAssetsByBrand(final boolean ascending) {
        sortByBrand(ascending, serializedAssets);
    }

    public void sortNoSerializedAssetsByBrand(final boolean ascending) {
        sortByBrand(ascending, noSerializedAssets);
    }

    public void sortSerializedAssetsByQuantity(final boolean ascending) {
        sortByQuantity(ascending, serializedAssets);
    }

    public void sortNoSerializedAssetsByQuantity(final boolean ascending) {
        sortByQuantity(ascending, noSerializedAssets);
    }

    private void sortByName(final boolean ascending, List<AccountAsset> assets) {
        Collections.sort(assets, new Comparator<AccountAsset>() {
            @Override
            public int compare(AccountAsset lhs, AccountAsset rhs) {
                if (ascending) {
                    return lhs.getAssetNeedToShow().getName().compareTo(rhs.getAssetNeedToShow().getName());
                } else {
                    return rhs.getAssetNeedToShow().getName().compareTo(lhs.getAssetNeedToShow().getName());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    private void sortByOrderNumber(final boolean ascending, List<AccountAsset> assets) {
        Collections.sort(assets, new Comparator<AccountAsset>() {
            @Override
            public int compare(AccountAsset lhs, AccountAsset rhs) {
                if (ascending) {
                    return lhs.getAssetNeedToShow().getDescription().compareTo(rhs.getAssetNeedToShow().getDescription());
                } else {
                    return rhs.getAssetNeedToShow().getDescription().compareTo(lhs.getAssetNeedToShow().getDescription());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    private void sortByBrand(final boolean ascending, List<AccountAsset> assets) {
        Collections.sort(assets, new Comparator<AccountAsset>() {
            @Override
            public int compare(AccountAsset lhs, AccountAsset rhs) {
                if (ascending) {
                    return lhs.getAssetNeedToShow().getBrand().compareTo(rhs.getAssetNeedToShow().getBrand());
                } else {
                    return rhs.getAssetNeedToShow().getBrand().compareTo(lhs.getAssetNeedToShow().getBrand());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    private void sortByQuantity(final boolean ascending, List<AccountAsset> assets) {
        Collections.sort(assets, new Comparator<AccountAsset>() {
            @Override
            public int compare(AccountAsset lhs, AccountAsset rhs) {
                if (ascending) {
                    return lhs.getAssetNeedToShow().getQuantity().compareTo(rhs.getAssetNeedToShow().getQuantity());
                } else {
                    return rhs.getAssetNeedToShow().getQuantity().compareTo(lhs.getAssetNeedToShow().getQuantity());
                }
            }
        });
        this.notifyDataSetChanged();
    }
}