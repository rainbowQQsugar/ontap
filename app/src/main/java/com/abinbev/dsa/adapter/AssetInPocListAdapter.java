package com.abinbev.dsa.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TableRow;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account_Asset__c;
import com.abinbev.dsa.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AssetInPocListAdapter extends BaseAdapter {

    private static final String CN_ASSET_CATEGORY_REFRIGERATOR = "冰柜";
    private static final String CN_ASSET_CATEGORY_BARREL_BEER = "桶啤机";

    public static final String SERIALIZED_RECORD_NAME = "Serialized Asset";
    public static final String NO_SERIALIZED_RECORD_NAME = "Non-Serialized";
    private static final int TYPE_ASSET_ITEM = 0;
    private static final int TYPE_HEADER = 1;
    private List<Account_Asset__c> serializedAssets;
    private List<Account_Asset__c> noSerializedAssets;
    Context context;

    public AssetInPocListAdapter(Context applicationContext) {
        serializedAssets = new ArrayList<>();
        noSerializedAssets = new ArrayList<>();
        this.context = applicationContext;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ASSET_ITEM;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        int headerCount = 0;
        if (!serializedAssets.isEmpty()) {
            headerCount = 1;
        }

        if (!noSerializedAssets.isEmpty()) {
            headerCount = 1;
        }

        return serializedAssets.size() + noSerializedAssets.size() + headerCount;
    }

    @Override
    public Object getItem(int position) {
        return getAssetWithPosition(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return (getAssetWithPosition(position) != null);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int rowType = getItemViewType(position);
        if (convertView == null) {
            switch (rowType) {
                case TYPE_ASSET_ITEM:
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    convertView = inflater.inflate(R.layout.asset_in_poc_list_item, parent, false);
                    convertView.setTag(new ViewHolder(convertView));
                    holder = (ViewHolder) convertView.getTag();
                    final Account_Asset__c asset = getAssetWithPosition(position);
                    holder.setupWithAsset(asset);
                    break;
                case TYPE_HEADER:
                    convertView = getHeadView(parent.getContext());
                    break;
            }
        } else if (rowType == TYPE_ASSET_ITEM) {
            holder = (ViewHolder) convertView.getTag();
            final Account_Asset__c asset = getAssetWithPosition(position);
            holder.setupWithAsset(asset);
        }

        return convertView;
    }

    private View getHeadView(Context context) {
        return new TextView(context);
    }

    public void setData(List<Account_Asset__c> assets, String recordName) {
        if (recordName.equals(AssetInPocListAdapter.SERIALIZED_RECORD_NAME)) {
            this.serializedAssets.clear();
            this.serializedAssets = assets;
        } else if (recordName.equals(AssetInPocListAdapter.NO_SERIALIZED_RECORD_NAME)) {
            this.noSerializedAssets.clear();
            this.noSerializedAssets = assets;
        }
        this.notifyDataSetChanged();
    }

    private Account_Asset__c getAssetWithPosition(int position) {
        if (position == 0) {
            return null;
        }
        Account_Asset__c asset = null;
        final int serSize = serializedAssets.size();
        if (position <= serSize) {
            asset = serializedAssets.get(position - 1);
        } else {
            asset = noSerializedAssets.get(position - 1 - serSize);
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
        @Bind(R.id.asset_clean_status_layout)
        public TableRow assetCleanStatusLayout;

        @Bind(R.id.asset_clean_status_label)
        public TextView assetCleanStatusLabel;
        @Bind(R.id.asset_clean_status)
        public TextView assetCleanStatus;
        @Bind(R.id.asset_last_check_layout)
        public TableRow assetLastCheckLayout;
        @Bind(R.id.asset_last_check)
        public TextView assetLastCheck;

        public void setupWithAsset(Account_Asset__c asset) {

            this.assetCode.setText(asset.getCode());

            if (Account_Asset__c.ASSET_STATUS_INSTALLED.equals(asset.getStatus())) {
                this.assetStatus.setText(R.string.asset_installed);
            } else if (Account_Asset__c.ASSET_STATUS_IN_STOCK.equals(asset.getStatus())) {
                this.assetStatus.setText(R.string.asset_in_stock);
            }
            this.assetName.setText(asset.getAssetName());
            if (CN_ASSET_CATEGORY_REFRIGERATOR.equals(asset.getCN_AssetCategory())) {
                assetCleanStatusLayout.setVisibility(View.GONE);
                assetLastCheckLayout.setVisibility(View.GONE);
                this.assetType.setText(R.string.refridgerator);
            } else if (CN_ASSET_CATEGORY_BARREL_BEER.equals(asset.getCN_AssetCategory())) {
                assetCleanStatusLayout.setVisibility(View.VISIBLE);
                assetLastCheckLayout.setVisibility(View.VISIBLE);
                this.assetType.setText(R.string.draught_machine);
                this.assetLastCheck.setText(DateUtils.formatDateStringCNLong(asset.getCN_LatestCleanTime()));
                if (Account_Asset__c.CN_CLEAN_STATUS_CLEANED.equals(asset.getCN_CleanStatus())) {
                    this.assetCleanStatus.setTextColor(Color.GREEN);
                    this.assetCleanStatus.setText(R.string.asset_beer_cleaned);
                } else if (Account_Asset__c.CN_CLEAN_STATUS_NOT_CLEANED.equals(asset.getCN_CleanStatus())) {
                    this.assetCleanStatus.setTextColor(Color.RED);
                    this.assetCleanStatus.setText(R.string.asset_beer_not_cleaned);
                }
            } else {
                assetCleanStatusLayout.setVisibility(View.GONE);
                assetLastCheckLayout.setVisibility(View.GONE);
            }
        }
    }
}