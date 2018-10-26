package com.abinbev.dsa.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.CN_PBO_Contract_Item__c;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Jakub Stefanowski
 */
public class ContractItemsAdapter extends RecyclerView.Adapter<ContractItemsAdapter.DataObjectHolder>{

    List<CN_PBO_Contract_Item__c> contractItems = new ArrayList<>();

    public static class DataObjectHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.contract_item_name)
        public TextView name;

        @Bind(R.id.contract_item_sku_id)
        public TextView skuId;

        @Bind(R.id.contract_item_year_month)
        public TextView yearMonth;

        @Bind(R.id.contract_item_volume)
        public TextView volume;

        @Bind(R.id.contract_item_achieved)
        public TextView achevied;

        public DataObjectHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setData(List<CN_PBO_Contract_Item__c> contractItems) {
        this.contractItems.clear();
        this.contractItems.addAll(contractItems);
        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        CN_PBO_Contract_Item__c contractItem = contractItems.get(position);
        int actual = contractItem.getActual();
        int target = contractItem.getTarget();

        holder.name.setText(contractItem.getProductName());
        holder.skuId.setText(contractItem.getSkuId());
        holder.yearMonth.setText(contractItem.getYearMonth());
        holder.volume.setText(actual + "/" + target);

        int achievedPercent = target == 0 ? 0 : (int) ((actual / (float) target) * 100);
        holder.achevied.setText(achievedPercent + "%");
    }

    @Override
    public int getItemCount() {
        return contractItems.size();
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_contract_item, parent, false);

        return new DataObjectHolder(view);
    }
}
