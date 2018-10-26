package com.abinbev.dsa.adapter;

import android.widget.Filter;

import com.abinbev.dsa.model.Material_Get__c;
import com.abinbev.dsa.model.Material_Give__c;
import com.abinbev.dsa.ui.view.negotiation.Material__c;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wandersonblough on 1/7/16.
 */
public class GiveGetFilter extends Filter {

    Type type;
    List<Material__c> materialItems;
    GiveGetSearchAdapter adapter;

    public enum Type {
        GIVE,
        GET
    }

    public GiveGetFilter(GiveGetSearchAdapter adapter, List<Material__c> materialItems) {
        super();
        this.adapter = adapter;
        this.materialItems = materialItems;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        List<Material__c> items = new ArrayList<>();
        String searchTerm = constraint.toString();

        boolean addItem;

        for (Material__c material__c : materialItems) {
            if (type == Type.GIVE) {
                addItem = material__c instanceof Material_Give__c;
            } else {
                addItem = material__c instanceof Material_Get__c;
            }
            if (addItem) {
                items.add(material__c);
            }
        }
        FilterResults filterResults = new FilterResults();
        filterResults.values = items;
        filterResults.count = items.size();

        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.updateItems((List<Material__c>) results.values);
    }
}
