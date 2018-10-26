package com.abinbev.dsa.adapter;

import android.content.Context;
import android.support.v4.widget.Space;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.CN_Technicians__c;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AccountD1ListAdapter extends BaseAdapter {

    private static final int TYPE_ASSET_ITEM = 0;
    private static final int TYPE_HEADER = 1;
    private List<CN_Technicians__c> techniciansList;

    public AccountD1ListAdapter(Context applicationContext) {
        techniciansList = new ArrayList<>();
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
        return techniciansList.isEmpty() ? techniciansList.size() : techniciansList.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return getTechniciansWithPosition(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return getTechniciansWithPosition(position) != null;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int rowType = getItemViewType(position);
        if (convertView == null) {
            switch (rowType) {
                case TYPE_ASSET_ITEM:
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    convertView = inflater.inflate(R.layout.account_d1_list_item, parent, false);
                    convertView.setTag(new ViewHolder(convertView));
                    holder = (ViewHolder) convertView.getTag();
                    final CN_Technicians__c technicians = getTechniciansWithPosition(position);
                    holder.setupWithTechnicians(technicians);
                    break;
                case TYPE_HEADER:
                    convertView = getHeadView(parent.getContext());
                    break;


            }
        } else if (rowType == TYPE_ASSET_ITEM) {
            holder = (ViewHolder) convertView.getTag();
            final CN_Technicians__c technicians = getTechniciansWithPosition(position);
            holder.setupWithTechnicians(technicians);
        }

        return convertView;
    }

    private View getHeadView(Context context) {
        return new Space(context);
    }

    public void setData(List<CN_Technicians__c> technicians) {
        this.techniciansList = technicians;
        this.notifyDataSetChanged();
    }

    private CN_Technicians__c getTechniciansWithPosition(int position) {
        if (position == 0) {
            return null;
        }
        return techniciansList.get(position - 1);
    }


    public class ViewHolder {
        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }

        @Bind(R.id.d1_name)
        public TextView d1Name;

        @Bind(R.id.d1_id)
        public TextView d1Id;

        @Bind(R.id.d1_phone)
        public TextView d1Phone;

        void setupWithTechnicians(CN_Technicians__c technicians__c) {
            this.d1Name.setText(technicians__c.getD1Name());
            this.d1Id.setText(technicians__c.getD1Id());
            this.d1Phone.setText(technicians__c.getD1Phone());
        }
    }
}