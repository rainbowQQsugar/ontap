package com.abinbev.dsa.adapter;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.ui.view.negotiation.Material__c;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wandersonblough on 1/7/16.
 */
public class GiveGetSearchAdapter extends RecyclerView.Adapter<GiveGetSearchAdapter.GiveGetViewHolder> implements Filterable {

    private List<Material__c> completeItemList;
    private List<Material__c> shownItems;
    private GiveGetFilter giveGetFilter;
    private List<String> selectedIds;
    private List<String> selectedGroups;
    private View mainLayout;

    public GiveGetSearchAdapter() {
        super();
        selectedIds = new ArrayList<>();
        selectedGroups = new ArrayList<>();
    }

    @Override
    public GiveGetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mainLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.material_layout, parent, false);
        return new GiveGetViewHolder(mainLayout);
    }

    @Override
    public void onBindViewHolder(GiveGetViewHolder holder, int position) {
        Material__c material__c = shownItems.get(position);
        holder.setMaterial__c(material__c);
        holder.actionBtn.setActivated(selectedIds.contains(material__c.getId()));
    }

    @Override
    public int getItemCount() {
        return shownItems == null ? 0 : shownItems.size();
    }

    @Override
    public Filter getFilter() {
        return giveGetFilter;
    }

    public void setData(List<Material__c> items) {
        this.completeItemList = items;
        giveGetFilter = new GiveGetFilter(this, items);
    }

    public void updateItems(List<Material__c> items) {
        this.shownItems = items;
        notifyDataSetChanged();
    }

    public void updateSelectedIds(List<String> currentIds) {
        this.selectedIds = currentIds;
        notifyDataSetChanged();
    }

    public List<String> getSelectedIds() {
        return selectedIds;
    }

    public List<Material__c> getSelectedItems() {
        List<Material__c> selectedItems = new ArrayList<>();
        // if this is null, the data is not set yet ...
        if (completeItemList == null) return selectedItems;
        for (String id : selectedIds) {
            for (Material__c material__c : completeItemList) {
                if (material__c.getId().equals(id)) {
                    selectedItems.add(material__c);
                    break;
                }
            }
        }
        return selectedItems;
    }

    public class GiveGetViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.action_btn)
        ImageView actionBtn;

        @Bind(R.id.code)
        TextView code;

        @Bind(R.id.name)
        TextView name;

        @Bind(R.id.description)
        TextView description;

//        @Bind(R.id.score)
//        TextView score;

        private Material__c material__c;

        public GiveGetViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setMaterial__c(Material__c material__c) {
            this.material__c = material__c;
            code.setVisibility(TextUtils.isEmpty(material__c.getCode()) ? View.GONE : View.VISIBLE);
            code.setText(material__c.getCode());

            name.setVisibility(TextUtils.isEmpty(material__c.getName()) ? View.GONE : View.VISIBLE);
            name.setText(material__c.getName());

            description.setVisibility(TextUtils.isEmpty(material__c.getDescription()) ? View.GONE : View.VISIBLE);
            description.setText(material__c.getDescription());

//            score.setText(String.format(itemView.getResources().getString(R.string.score), material__c.getScore()));
        }

        @OnClick(R.id.action_btn)
        public void actionClick(View btn) {
            if (btn.isActivated()) {
                selectedIds.remove(material__c.getId());
                selectedGroups.remove(material__c.getGroup());
            } else {
                // validate the item
                if (validateItem(material__c)) {
                    selectedIds.add(material__c.getId());
                    selectedGroups.add(material__c.getGroup());
                } else {
                    showSnackbar(mainLayout.getContext().getResources().getString(R.string.item_validation_error));
                }
            }
            notifyItemChanged(getAdapterPosition());
        }

        private boolean validateItem(Material__c material) {
            if (material.getExclusive().equalsIgnoreCase("yes")) {
                String group = material.getGroup();
                if (selectedGroups.contains(group)){
                    return false;
                }
            }
            return true;
        }

        private void showSnackbar(String errorString) {

            final Snackbar snackbar = Snackbar.make(mainLayout, errorString, Snackbar.LENGTH_INDEFINITE);
            View snackbarView = snackbar.getView();
            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            if (textView != null) textView.setMaxLines(3);  // show multiple line

            snackbar.setAction(R.string.dismiss, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });

            snackbar.show();
        }
    }
}
