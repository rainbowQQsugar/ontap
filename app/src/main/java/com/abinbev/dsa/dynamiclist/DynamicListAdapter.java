package com.abinbev.dsa.dynamiclist;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.salesforce.dsa.data.model.SFBaseObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicListAdapter extends RecyclerView.Adapter<DynamicListAdapter.DataObjectHolder> {

    public interface OnItemClickListener {
        void onItemClicked(SFBaseObject item);
    }


    private final List<String> fieldNames;

    private final List<SFBaseObject> items;

    @LayoutRes
    private final int listItemResId;

    private final Map<String, String> fieldLabels = new HashMap<>();

    private DynamicListItemRowBuilder itemRowBuilder;

    private DynamicListFieldValueBinder fieldValueBinder;

    private LayoutInflater layoutInflater;

    private OnItemClickListener onItemClickListener;

    public static class DataObjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private OnItemClickListener onItemClickListener;

        public final TableLayout tableLayout;

        public SFBaseObject currentItem;

        public DataObjectHolder(View itemView, TableLayout tableLayout, OnItemClickListener onItemClickListener) {
            super(itemView);
            this.tableLayout = tableLayout;
            this.onItemClickListener = onItemClickListener;

            View clickContainer = itemView.findViewById(R.id.dynamic_list_item_click_container);
            if (clickContainer == null) {
                clickContainer = tableLayout;
            }

            clickContainer.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClicked(currentItem);
            }
        }
    }

    public DynamicListAdapter(List<String> fieldNames, @LayoutRes int listItemResId) {
        this.fieldNames = fieldNames;
        this.listItemResId = listItemResId;
        this.items = new ArrayList<>();
    }

    public void setData(List<SFBaseObject> rejectedFiles) {
        this.items.clear();
        this.items.addAll(rejectedFiles);
        this.notifyDataSetChanged();
    }

    public void addData(List<SFBaseObject> rejectedFiles) {
        this.items.addAll(rejectedFiles);
        this.notifyDataSetChanged();
    }

    public void setItemRowBuilder(DynamicListItemRowBuilder itemRowBuilder) {
        this.itemRowBuilder = itemRowBuilder;
    }

    public void setFieldValueBinder(DynamicListFieldValueBinder fieldValueBinder) {
        this.fieldValueBinder = fieldValueBinder;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setFieldLabels(Map<String, String> fieldLabels) {
        this.fieldLabels.clear();
        this.fieldLabels.putAll(fieldLabels);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        SFBaseObject item = items.get(position);
        holder.currentItem = item;

        TableLayout tableLayout = holder.tableLayout;

        for (String fieldName: fieldNames) {
            TableRow tableRow = (TableRow) tableLayout.findViewWithTag(fieldName);
            View labelView = tableRow.findViewById(R.id.dynamic_list_item_row_label);
            if (labelView != null && labelView instanceof TextView) {
                TextView labelTextView = (TextView) labelView;
                labelTextView.setText(fieldLabels.get(fieldName));
            }

            View valueView = tableRow.findViewById(R.id.dynamic_list_item_row_value);
            assertNotNull(valueView, "Each list item row should contain view with id" +
                    " R.id.dynamic_list_item_row_value");

            fieldValueBinder.bindValue(fieldName, valueView, item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = getLayoutInflater(parent.getContext());

        View view = inflater.inflate(listItemResId, parent, false);
        TableLayout tableLayout = (TableLayout) view.findViewById(R.id.dynamic_list_item_table);

        assertNotNull(tableLayout, "List item layout for DynamicListAdapter must contain" +
                " TableLayout with id set to R.id.dynamic_list_item_table.");

        addTableRows(tableLayout, inflater, fieldNames);

        return new DataObjectHolder(view, tableLayout, onItemClickListener);
    }

    protected void addTableRows(TableLayout tableLayout, LayoutInflater inflater, List<String> fieldNames) {
        assertNotNull(itemRowBuilder, "DynamicListItemRowBuilder is missing.");

        for (String fieldName : fieldNames) {
            TableRow tableRow = itemRowBuilder.createListItemRow(inflater, fieldName, tableLayout);
            tableRow.setTag(fieldName);
            tableLayout.addView(tableRow);
        }
    }

    private LayoutInflater getLayoutInflater(Context context) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(context);
        }
        return layoutInflater;
    }

    private void assertNotNull(Object object, String msg) {
        if (object == null) {
            throw new IllegalStateException(msg);
        }
    }

    // Tworzenie layoutu (osobno card i osobno wiersze?)
    // Bindowanie layoutu z danymi.
    // TODO wywal table i zrób jakoś generycznie
}
