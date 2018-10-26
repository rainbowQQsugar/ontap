package com.abinbev.dsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.KPI__c;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Jakub Stefanowski on 13.03.2017.
 */

public class VolumeChildrenAdapter extends BaseAdapter {

    private List<KPI__c> kpisList = new ArrayList<>();

    public void setItems(List<KPI__c> list) {
        kpisList.clear();
        if (list != null) {
            kpisList.addAll(list);
        }
    }

    @Override
    public int getCount() {
        return kpisList.size();
    }

    @Override
    public KPI__c getItem(int position) {
        return kpisList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VolumeDetailsVH holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.volume_details_list_item, parent, false);
            holder = new VolumeDetailsVH(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (VolumeDetailsVH) convertView.getTag();
        }

        KPI__c kpi = kpisList.get(position);
        double actual = kpi.getActual();
        double target = kpi.getTarget();
        int progress = target == 0 ? 0 : Math.min((int) ((actual * 100) / target), 100);

        holder.title.setText(String.format("%s - %.0f%s/%.0f%s",
                kpi.getKpiName(), actual, kpi.getTranslatedUnit(), target, kpi.getTranslatedUnit()));
        holder.progress.setProgress(progress);
        holder.percent.setText(progress + "%");
        return convertView;
    }

    static class VolumeDetailsVH {

        @Bind(R.id.volume_details_item_title)
        TextView title;

        @Bind(R.id.volume_details_item_percent)
        TextView percent;

        @Bind(R.id.volume_details_item_progress)
        ProgressBar progress;

        VolumeDetailsVH(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}