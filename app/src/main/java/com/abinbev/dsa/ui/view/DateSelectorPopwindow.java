package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.CN_KPI_Statistic__c;
import com.abinbev.dsa.model.KPI__c;

import java.util.ArrayList;
import java.util.List;

public class DateSelectorPopwindow extends PopupWindow implements AdapterView.OnItemClickListener {

    private String currentDate;
    private Context context;
    private OnDateItemSelected onDateItemSelected;
    private List<List<KPI__c>> kpis = new ArrayList<>();

    public void setOnDateItemSelected(OnDateItemSelected onDateItemSelected) {
        this.onDateItemSelected = onDateItemSelected;
    }

    public void buildPopwindow(Context context) {
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.popwindow_date_selector, null);
        setOutsideTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(0));
        setContentView(view);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setViewBehavior(view);
    }

    private void setViewBehavior(View view) {
        GridView dateSelector = (GridView) view.findViewById(R.id.date_selector);
        DateSelectorAdapter dateSelectorAdapter = new DateSelectorAdapter();
        dateSelector.setOnItemClickListener(this);
        dateSelector.setAdapter(dateSelectorAdapter);
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public void setKpis(List<List<KPI__c>> kpis) {
        this.kpis = kpis;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (onDateItemSelected != null) {
            List<KPI__c> item = (List<KPI__c>) parent.getAdapter().getItem(position);
            onDateItemSelected.onDateItemSelected(item, position);
        }
    }


    private class DateSelectorAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return kpis.size();
        }

        @Override
        public Object getItem(int position) {
            return kpis.get(position);
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
                convertView = LayoutInflater.from(context).inflate(R.layout.date_item, null);
                holder.dateString = (TextView) convertView.findViewById(R.id.tv_date);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String[] split = kpis.get(position).get(0).getEndDate().split("-");
            holder.dateString.setText(split.length > 2 ? split[0] + "-" + split[1] : "");
            return convertView;
        }

        class ViewHolder {
            TextView dateString;
        }
    }

    public interface OnDateItemSelected {
        void onDateItemSelected(List<KPI__c> c, int pos);
    }
}
