package com.abinbev.dsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.KPI__c;

import java.util.List;

public class PieChartAdapter extends BaseAdapter {


    private List<KPI__c> pieChartList;

    public PieChartAdapter(List<KPI__c> pieChartList) {
        this.pieChartList = pieChartList;
    }


    @Override
    public int getCount() {
        return pieChartList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.kpi_progress_view, null);
            holder.lable = convertView.findViewById(R.id.kpi_progress_view_label);
            holder.percent = convertView.findViewById(R.id.kpi_progress_view_percent);
            holder.progressBar = convertView.findViewById(R.id.kpi_progress_view_progress);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        KPI__c kpi__c = pieChartList.get(position);
        holder.lable.setText(kpi__c.getKpiName());
        holder.percent.setText(Math.round(Double.valueOf(kpi__c.getPercentCompleted())) + "%");
        holder.progressBar.setProgress((int) Math.round(Double.valueOf(kpi__c.getPercentCompleted())));
        return convertView;
    }

    public void setData(List<KPI__c> pieChartList) {
        this.pieChartList.clear();
        this.pieChartList.addAll(pieChartList);
        notifyDataSetChanged();
    }

    class ViewHolder {
        TextView lable, percent;
        ProgressBar progressBar;
    }
}
