package com.abinbev.dsa.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Promociones__c;
import com.abinbev.dsa.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wandersonblough on 1/8/16.
 */
public class AddPromotionAdapter extends RecyclerView.Adapter<AddPromotionAdapter.PromotionViewHolder> {

    List<Promociones__c> items;
    List<String> selectedIds;

    public AddPromotionAdapter(List<Promociones__c> items) {
        super();
        this.items = items;
        selectedIds = new ArrayList<>();
    }

    @Override
    public PromotionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.promotion_item, parent, false);
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PromotionViewHolder holder, int position) {
        Promociones__c promociones__c = items.get(position);
        holder.setPromociones__c(promociones__c);
        holder.actionBtn.setActivated(selectedIds.contains(promociones__c.getId()));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public class PromotionViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.action_btn)
        ImageView actionBtn;

        @Bind(R.id.description)
        TextView description;

        @Bind(R.id.name)
        TextView name;

        @Bind(R.id.end_date)
        TextView endDate;

        Promociones__c promociones__c;

        public PromotionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setPromociones__c(Promociones__c promociones__c) {
            this.promociones__c = promociones__c;
            description.setText(promociones__c.getDescription());
            name.setText(promociones__c.getName());
            endDate.setText(DateUtils.formatDateStringShort(promociones__c.getEndDate()));
        }

        @OnClick(R.id.action_btn)
        public void actionClick(View btn) {
            if (btn.isActivated()) {
                selectedIds.remove(promociones__c.getId());
            } else {
                selectedIds.add(promociones__c.getId());
            }
            notifyItemChanged(getAdapterPosition());
        }
    }

    public ArrayList<String> getCodes() {
        ArrayList<String> codes = new ArrayList<>();
        for (String id : selectedIds) {
            for (Promociones__c promociones__c : items) {
                if (promociones__c.getId().equals(id)) {
                    codes.add(promociones__c.getName());
                    break;
                }
            }
        }
        return codes;
    }
}
