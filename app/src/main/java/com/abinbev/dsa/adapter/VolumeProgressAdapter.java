package com.abinbev.dsa.adapter;

import static org.apache.commons.lang3.StringUtils.stripToEmpty;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.KPI__c;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jakub Stefanowski on 17.02.2017.
 */

public class VolumeProgressAdapter extends BaseAdapter {

    List<KPI__c> kpis = new ArrayList<>();

    public void setData(List<KPI__c> kpis) {
        this.kpis.clear();
        if(kpis != null) {
            this.kpis.addAll(kpis);
        }
    }

    @Override
    public int getCount() {
        return kpis.size();
    }

    @Override
    public KPI__c getItem(int position) {
        return kpis.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VolumeVH holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.volume_list_item, parent, false);
            holder = new VolumeVH(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (VolumeVH) convertView.getTag();
        }

        KPI__c kpi = getItem(position);
        double actual = kpi.getActual();
        double target = kpi.getTarget();
        int percentProgress = target == 0 ? 0 : (int) Math.round((actual / target) * 100);
        holder.title.setText(kpi.getTranslatedCategory());

        holder.progress.setProgress(percentProgress);
        holder.progress.setMax(100);

        holder.percent.setText(percentProgress + "%");
        holder.values.setText(String.format("%.0f%s / %.0f%s", actual, kpi.getTranslatedUnit(), target,
                kpi.getTranslatedUnit()));

        String startDate = stripToEmpty(kpi.getStartDate());
        String endDate = stripToEmpty(kpi.getEndDate());
        holder.dates.setText(String.format("%s/\n%s", startDate, endDate));

        return convertView;
    }

    static class VolumeVH {

        @Bind(R.id.volume_item_title)
        TextView title;

        @Bind(R.id.volume_item_percent)
        TextView percent;

        @Bind(R.id.volume_item_values)
        TextView values;

        @Bind(R.id.volume_item_date)
        TextView dates;

        @Bind(R.id.volume_item_progress)
        ProgressBar progress;

        public VolumeVH(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
