package com.abinbev.dsa.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.DistributionActivity;
import com.abinbev.dsa.model.Distribution;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DistributionAdapter extends RecyclerView.Adapter<DistributionAdapter.DataObjectHolder> {

    public class DataObjectHolder extends RecyclerView.ViewHolder {

        public Distribution distribution;

        @Bind(R.id.distribution_list_category)
        public TextView category;

        @Bind(R.id.distribution_list_brand)
        public TextView brand;

        @Bind(R.id.distribution_list_sku_name)
        public TextView sku;

        @Bind(R.id.distribution_list_package)
        public TextView packageName;

        @Bind(R.id.distribution_list_collected_ptr)
        public TextView ptr;

        @Bind(R.id.distribution_list_collected_ptc)
        public TextView ptc;

        @Bind(R.id.distribution_list_unit)
        public TextView unit;

        public DataObjectHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setDistribution(Distribution distribution) {
            this.distribution = distribution;
        }

        @OnClick(R.id.distribution_card)
        public void onItemClicked() {
            Context context = itemView.getContext();
            context.startActivity(new Intent(context, DistributionActivity.class)
                    .putExtra(DistributionActivity.ARGS_DISTRIBUTION_ID, distribution.getId())
                    .putExtra(DistributionActivity.ARGS_DISTRIBUTION_NAME, distribution.getSKUName()));
        }
    }

    List<Distribution> distributions = new ArrayList<>();

    public void setData(List<Distribution> contracts) {
        this.distributions.clear();
        this.distributions.addAll(contracts);
        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        Distribution distribution = distributions.get(position);

        holder.setDistribution(distribution);
        holder.category.setText(distribution.getCategory());
        holder.brand.setText(distribution.getBrand());
        holder.sku.setText(distribution.getSKUName());
        holder.packageName.setText(distribution.getTranslatedPackage());
        holder.ptr.setText(distribution.getLastCollectedPtr());
        holder.ptc.setText(distribution.getLastCollectedPtc());
        holder.unit.setText(distribution.getTranslatedUnit());
    }

    @Override
    public int getItemCount() {
        return distributions.size();
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.distribution_list_item, parent, false);

        return new DataObjectHolder(view);
    }
}
