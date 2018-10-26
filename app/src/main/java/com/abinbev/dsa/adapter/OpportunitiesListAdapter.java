package com.abinbev.dsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Estandar__c;
import com.abinbev.dsa.ui.view.Opportunity.OpportunityHeaderView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by lukaszwalukiewicz on 05.01.2016.
 */
public class OpportunitiesListAdapter extends BaseAdapter {
    private static final int TYPE_HEADER_ITEM = 0;
    private static final int TYPE_OPPORTUNITY_ITEM = 1;
    private int opportunitiesCount;
    private List<Integer> headerPositions = new ArrayList<>();
    private HashMap <String, List<Estandar__c>> opportunitiesMap = new HashMap<>();

    public OpportunitiesListAdapter(Context context) {}

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeaderPosition(position)){
            return TYPE_HEADER_ITEM;
        }
        return TYPE_OPPORTUNITY_ITEM;

    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return opportunitiesMap.keySet().size() + opportunitiesCount;
    }

    @Override
    public Object getItem(int position) {
        return getOpportunity(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int rowType = getItemViewType(position);
        if (convertView == null) {
            switch (rowType) {
                case TYPE_HEADER_ITEM:
                    OpportunityHeaderView headerView = new OpportunityHeaderView(parent.getContext(), this);
                    headerView.setTag(headerView);
                    headerView.sectionName.setText(nameForSection(position));
                    convertView = headerView;
                    break;
                case TYPE_OPPORTUNITY_ITEM:
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    convertView = inflater.inflate(R.layout.opportunities_list_entry_view, parent, false);
                    convertView.setTag(new ViewHolder(convertView));
                    holder = (ViewHolder) convertView.getTag();
                    Estandar__c opportunity = getOpportunity(position);
                    holder.setupWithOpportunity(opportunity);
                    break;
            }
        } else{
            switch (rowType) {
                case TYPE_HEADER_ITEM:
                    OpportunityHeaderView headerView = (OpportunityHeaderView) convertView.getTag();
                    headerView.sectionName.setText(nameForSection(position));
                    break;
                case TYPE_OPPORTUNITY_ITEM:
                    holder = (ViewHolder) convertView.getTag();
                    Estandar__c opportunity = getOpportunity(position);
                    holder.setupWithOpportunity(opportunity);
                    break;
            }
        }

        return convertView;
    }

    public void setData(List<Estandar__c> opportunities) {
        opportunitiesCount = opportunities.size();
        for (Estandar__c opportunity : opportunities){
            String variable = opportunity.getVariable();
            List<Estandar__c> tmpOpportunities = null;
            if (opportunitiesMap.containsKey(variable)){
                tmpOpportunities = opportunitiesMap.get(variable);
            } else {
                tmpOpportunities = new ArrayList<Estandar__c>();
            }
            tmpOpportunities.add(opportunity);
            opportunitiesMap.put(variable, tmpOpportunities);
        }
        Integer index = 0;
        for (String key : opportunitiesMap.keySet()){
            headerPositions.add(index);
            index++;
            List <Estandar__c> tmpOpportunities = opportunitiesMap.get(key);
            index += tmpOpportunities.size();
        }
        this.notifyDataSetChanged();
    }

    private Boolean isPositionHeaderPosition(int position){
        return (headerPositions.contains(position));
    }

    private String nameForSection(int position){
        int index = 0;
        for (Map.Entry<String, List<Estandar__c>> entry : opportunitiesMap.entrySet()){
            index++;
            List<Estandar__c> tmpOpportunities = entry.getValue();
            index += tmpOpportunities.size();
            if (position < index){
                return entry.getKey();
            }
        }

        return null;
    }

    private Estandar__c getOpportunity(int position){
        if (isPositionHeaderPosition(position)){
            return null;
        } else {
            int index = 0;
            for (Map.Entry<String, List<Estandar__c>> entry : opportunitiesMap.entrySet()){
                index++;
                List<Estandar__c> tmpOpportunities = entry.getValue();
                int opportunitiesSize = tmpOpportunities.size();
                index += opportunitiesSize;
                if (position < index){
                    int initialIndex = index - opportunitiesSize;
                    int opportunityIndex = position - initialIndex;
                    Estandar__c tmpOpportunity = tmpOpportunities.get(opportunityIndex);
                    return tmpOpportunity;
                }
            }
        }
        return null;
    }

    public void sortByOpportunityName(final boolean ascending, String sectionName) {
        List<Estandar__c> opportunities = opportunitiesMap.get(sectionName);
        if (opportunities != null){
            Collections.sort(opportunities, new Comparator<Estandar__c>() {
                @Override
                public int compare(Estandar__c lhs, Estandar__c rhs) {
                    if (ascending) {
                        return lhs.getName().compareTo(rhs.getName());
                    } else {
                        return rhs.getName().compareTo(lhs.getName());
                    }
                }
            });
            this.notifyDataSetChanged();
        }
    }

    public void sortByOpportunityReal(final boolean ascending, String sectionName) {
        List<Estandar__c> opportunities = opportunitiesMap.get(sectionName);
        if (opportunities != null) {
            Collections.sort(opportunities, new Comparator<Estandar__c>() {
                @Override
                public int compare(Estandar__c lhs, Estandar__c rhs) {
                    if (ascending) {
                        return lhs.getRealValues().compareTo(rhs.getRealValues());
                    } else {
                        return rhs.getRealValues().compareTo(lhs.getRealValues());
                    }
                }
            });
            this.notifyDataSetChanged();
        }
    }

    public void sortByOpportunityIdeal(final boolean ascending, String sectionName) {
        List<Estandar__c> opportunities = opportunitiesMap.get(sectionName);
        if (opportunities != null) {
            Collections.sort(opportunities, new Comparator<Estandar__c>() {
                @Override
                public int compare(Estandar__c lhs, Estandar__c rhs) {
                    if (ascending) {
                        return lhs.getIdealValues().compareTo(rhs.getIdealValues());
                    } else {
                        return rhs.getIdealValues().compareTo(lhs.getIdealValues());
                    }
                    }
            });
            this.notifyDataSetChanged();
        }
    }

    public void sortByOpportunity(final boolean ascending, String sectionName) {
        List<Estandar__c> opportunities = opportunitiesMap.get(sectionName);
        if (opportunities != null) {
            Collections.sort(opportunities, new Comparator<Estandar__c>() {
                @Override
                public int compare(Estandar__c lhs, Estandar__c rhs) {
                    if (ascending) {
                        return lhs.getOpportunityValues().compareTo(rhs.getOpportunityValues());
                    } else {
                        return rhs.getOpportunityValues().compareTo(lhs.getOpportunityValues());
                    }
                }
            });
            this.notifyDataSetChanged();
        }
    }

    public static class ViewHolder {
        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }

        @Bind(R.id.opportunity_Name)
        public TextView opportunitiesNumber;

        @Bind(R.id.opportunity_Ideal)
        public TextView opportunitiesIdeal;

        @Bind(R.id.opportunity_Real)
        public TextView opportunitiesReal;

        @Bind(R.id.opportunity_Opportunity)
        public TextView opportunitiesOpportunity;

        public void setupWithOpportunity(Estandar__c opportunity){
            this.opportunitiesNumber.setText(opportunity.getName());
            this.opportunitiesIdeal.setText(opportunity.getIdealValues());
            this.opportunitiesReal.setText(opportunity.getRealValues());
            this.opportunitiesOpportunity.setText(opportunity.getOpportunityValues());
        }
    }
}
