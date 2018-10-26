package com.abinbev.dsa.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.SalesVolume;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Diana Błaszczyk on 27/10/17.
 */

public class SalesVolumeListAdapter extends RecyclerView.Adapter<SalesVolumeListAdapter.DataObjectHolder> implements Filterable {

    List<SalesVolume> salesVolumes;
    PedidoListAdapter.OrderClickHandler pedidoClickHandler;
    CompositeSalesVolumeFilter compositeSalesVolumeFilter;

    public SalesVolumeListAdapter() {
        super();
        salesVolumes = new ArrayList<>();
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder {
        @Nullable
        @Bind(R.id.card_view)
        CardView cardView;

        @Bind(R.id.sales_volume_brand)
        TextView brand;

        @Bind(R.id.sales_volume_sku)
        TextView sku;

        @Bind(R.id.sales_volume_volume_case)
        TextView volumeCase;


        public DataObjectHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setPromotionClickHandler(PedidoListAdapter.OrderClickHandler promotionClickHandler) {
        this.pedidoClickHandler = promotionClickHandler;
    }

    @Override
    public SalesVolumeListAdapter.DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sales_volume_list_entry_view, parent, false);
        return new SalesVolumeListAdapter.DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(SalesVolumeListAdapter.DataObjectHolder holder, int position) {
        SalesVolume sv = salesVolumes.get(position);
        holder.brand.setText(sv.getProductBrand());
        holder.sku.setText(sv.getProductSKU());
        holder.volumeCase.setText(sv.getVolumeCase());
    }


    @Override
    public int getItemCount() {
        return this.salesVolumes.size();
    }


    public void setData(List<SalesVolume> salesVolumes, Date showDateSince) {

        if (salesVolumes != null) {
            this.salesVolumes.clear();
            List<SalesVolume> tmpSvs = new ArrayList<>();
            for (SalesVolume s : salesVolumes)
                tmpSvs.add(new SalesVolume(s));

            HashMap<String, SalesVolume> svs = getAggregatedVolumes(tmpSvs);

            for (SalesVolume sv : svs.values()) {
                if (sv.getStartDate() == null || sv.getStartDate().after(showDateSince))
                    this.salesVolumes.add(sv);
            }
            this.notifyDataSetChanged();

            compositeSalesVolumeFilter = new CompositeSalesVolumeFilter(this, salesVolumes);
        }
    }

    protected void filterData(List<SalesVolume> salesVolumes) {

        if (salesVolumes != null) {
            this.salesVolumes.clear();
            List<SalesVolume> tmpSvs = new ArrayList<>();
            for (SalesVolume s : salesVolumes)
                tmpSvs.add(new SalesVolume(s));

            HashMap<String, SalesVolume> svs = getAggregatedVolumes(tmpSvs);

            for (SalesVolume sv : svs.values())
                this.salesVolumes.add(sv);

            this.notifyDataSetChanged();
        }
    }

    private HashMap<String, SalesVolume> getAggregatedVolumes(List<SalesVolume> salesVolumes) {
        HashMap<String, SalesVolume> svs = new HashMap<>();
        for (SalesVolume singleSv : salesVolumes) {
            String productSKU = singleSv.getProductSKU();

            SalesVolume oldSv = svs.get(productSKU);
            if (oldSv == null)
                svs.put(productSKU, singleSv);
            else {
                int volCase = Integer.valueOf(singleSv.getVolumeCase()) + Integer.valueOf(oldSv.getVolumeCase());
                oldSv.setVolumeCase(String.valueOf(volCase));
                svs.put(productSKU, oldSv);
            }
        }
        return svs;
    }


    @Override
    public Filter getFilter() {
        return compositeSalesVolumeFilter;
    }

}
