package com.abinbev.dsa.adapter.FlexibleData;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Dato_flexible__c;
import com.abinbev.dsa.model.Parametro__c;
import com.abinbev.dsa.ui.view.FlexibleData.FlexibleDataHeaderView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by lukaszwalukiewicz on 08.01.2016.
 */
public class FlexibleDataListAdapter extends BaseExpandableListAdapter {
    private static final int TYPE_HEADER_ITEM = 0;
    private static final int TYPE_FLEXIBLE_DATA_ITEM = 1;
    private List<String> listDataHeader = new ArrayList<String>();
    private HashMap<String, List<Dato_flexible__c>> flexibleDataChild = new HashMap<>();
    private HashMap<String, SortableHeaderDataHandler> nameSortDataHandlerMap = new HashMap<>();
    private HashMap<String, SortableHeaderDataHandler> valueSortDataHandlerMap = new HashMap<>();

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        if (childPosition == 0) {
            return TYPE_HEADER_ITEM;
        } else {
            return TYPE_FLEXIBLE_DATA_ITEM;
        }
    }

    @Override
    public int getChildTypeCount() {
        return 2;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int itemType = getChildType(groupPosition, childPosition);
        if (convertView == null) {
            switch (itemType) {
                case TYPE_HEADER_ITEM: {
                    FlexibleDataHeaderView headerView = new FlexibleDataHeaderView(parent.getContext(), this);
                    headerView.setTag(headerView);
                    String section = (String) getGroup(groupPosition);
                    headerView.setSection(section);
                    convertView = headerView;
                    SortableHeaderDataHandler nameSortDataHandler = nameSortDataHandlerMap.get(section);
                    headerView.setupNumberHeader(nameSortDataHandler);
                    SortableHeaderDataHandler valueSortDataHandler = valueSortDataHandlerMap.get(section);
                    headerView.setupValuerHeader(valueSortDataHandler);
                }
                break;
                case TYPE_FLEXIBLE_DATA_ITEM: {
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    convertView = inflater.inflate(R.layout.flexible_data_list_item, null);
                    convertView.setTag(new ViewHolder(convertView));
                    holder = (ViewHolder) convertView.getTag();
                    holder.flexibleDataNumber.getTextColors();
                    final Dato_flexible__c datoFlexible = (Dato_flexible__c) getChild(groupPosition, childPosition - 1);
                    holder.setupWithFlexibleData(datoFlexible);
                }
                break;
            }
        } else {
            switch (itemType) {
                case TYPE_HEADER_ITEM:
                    FlexibleDataHeaderView headerView = (FlexibleDataHeaderView) convertView.getTag();
                    String section = (String) getGroup(groupPosition);
                    headerView.setSection(section);
                    SortableHeaderDataHandler nameSortDataHandler = nameSortDataHandlerMap.get(section);
                    headerView.setupNumberHeader(nameSortDataHandler);
                    SortableHeaderDataHandler valueSortDataHandler = valueSortDataHandlerMap.get(section);
                    headerView.setupValuerHeader(valueSortDataHandler);
                    break;
                case TYPE_FLEXIBLE_DATA_ITEM:
                    holder = (ViewHolder) convertView.getTag();
                    final Dato_flexible__c datoFlexible = (Dato_flexible__c) getChild(groupPosition, childPosition - 1);
                    holder.setupWithFlexibleData(datoFlexible);
                    break;
                }
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.flexibleDataChild.get(this.listDataHeader.get(groupPosition)).size() + 1;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.flexibleDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.flexible_data_list_group, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setText(headerTitle);
        return convertView;
    }

    public void setData(List<Dato_flexible__c> flexibleDataList) {
        flexibleDataChild.clear();
        listDataHeader.clear();
        nameSortDataHandlerMap.clear();
        valueSortDataHandlerMap.clear();
        for (Dato_flexible__c flexibleData : flexibleDataList) {
            String type = flexibleData.getType();
            List<Dato_flexible__c> tmpFlexibleData = null;
            if (flexibleDataChild.containsKey(type)) {
                tmpFlexibleData = flexibleDataChild.get(type);
            } else {
                tmpFlexibleData = new ArrayList<Dato_flexible__c>();
                listDataHeader.add(type);
                nameSortDataHandlerMap.put(type, new SortableHeaderDataHandler());
                valueSortDataHandlerMap.put(type, new SortableHeaderDataHandler());
            }
            tmpFlexibleData.add(flexibleData);
            flexibleDataChild.put(type, tmpFlexibleData);
        }
        this.notifyDataSetChanged();
    }

    public void sortByNumber(final boolean ascending, String sectionName) {
        SortableHeaderDataHandler sortDataHandler = nameSortDataHandlerMap.get(sectionName);
        if (ascending) {
            sortDataHandler.setSortedAscending();
        } else {
            sortDataHandler.setSortedDescending();
        }
        SortableHeaderDataHandler valueDataHandler = valueSortDataHandlerMap.get(sectionName);
        valueDataHandler.resetSorting();
        List<Dato_flexible__c> flexibleDataList = flexibleDataChild.get(sectionName);
        if (flexibleDataList != null) {
            Collections.sort(flexibleDataList, new Comparator<Dato_flexible__c>() {
                @Override
                public int compare(Dato_flexible__c lhs, Dato_flexible__c rhs) {
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

    public void sortByValue(final boolean ascending, String sectionName) {
        SortableHeaderDataHandler sortDataHandler = valueSortDataHandlerMap.get(sectionName);
        if (ascending) {
            sortDataHandler.setSortedAscending();
        } else {
            sortDataHandler.setSortedDescending();
        }
        SortableHeaderDataHandler nameDataHandler = nameSortDataHandlerMap.get(sectionName);
        nameDataHandler.resetSorting();
        List<Dato_flexible__c> flexibleDataList = flexibleDataChild.get(sectionName);
        if (flexibleDataList != null) {
            Collections.sort(flexibleDataList, new Comparator<Dato_flexible__c>() {
                @Override
                public int compare(Dato_flexible__c lhs, Dato_flexible__c rhs) {
                    if (ascending) {
                        return lhs.getValue().compareTo(rhs.getValue());
                    } else {
                        return rhs.getValue().compareTo(lhs.getValue());
                    }
                }
            });
            this.notifyDataSetChanged();
        }
    }

     public static class ViewHolder {
         private ColorStateList defaultColor;

         public ViewHolder(View convertView) {
             ButterKnife.bind(this, convertView);
             this.defaultColor = flexibleDataNumber.getTextColors();
         }

         @Bind(R.id.flexibleDataNumber)
         public TextView flexibleDataNumber;

         @Bind(R.id.flexibleDataValue)
         public TextView flexibleDataValue;

         public void setupWithFlexibleData(Dato_flexible__c flexibleData) {
             Parametro__c concepto = Parametro__c.getById(flexibleData.getConceptoId());

             String color;
             if (concepto == null) {
                 color = null;
                 this.flexibleDataNumber.setText("");
             } else {
                 color = concepto.getColor();
                 this.flexibleDataNumber.setText(concepto.getName());
             }

             this.flexibleDataValue.setText(flexibleData.getValue());
             if (color != null && color.length() > 0) {
                 flexibleDataNumber.setTextColor(Color.parseColor(color));
                 flexibleDataValue.setTextColor(Color.parseColor(color));
             } else {
                 flexibleDataNumber.setTextColor(defaultColor);
                 flexibleDataValue.setTextColor(defaultColor);
             }
         }
     }
}