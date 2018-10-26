package com.abinbev.dsa.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by lukaszwalukiewicz on 07.01.2016.
 */
public class PromotionsListAdapter extends RecyclerView.Adapter<PromotionsListAdapter.DataObjectHolder>{


    public interface PromotionClickHandler {
        void onPromotionClick(CN_Product_Negotiation__c promotion, int position);
    }

    List<CN_Product_Negotiation__c> promotions = new ArrayList<CN_Product_Negotiation__c>();
    PromotionClickHandler promotionClickHandler;


    public static class DataObjectHolder extends ViewHolder {
        @Bind(R.id.promotionNumberValue)
        public TextView promotionName;

        @Bind(R.id.promotionTypeValue)
        public TextView promotionType;

        @Bind(R.id.promotionDescriptionValue)
        public TextView promotionDescription;

        public DataObjectHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setData(List<CN_Product_Negotiation__c> promotions) {
        this.promotions.clear();
        this.promotions.addAll(promotions);
        this.notifyDataSetChanged();
    }

    public void setPromotionClickHandler(PromotionClickHandler promotionClickHandler) {
        this.promotionClickHandler = promotionClickHandler;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        CN_Product_Negotiation__c promotion = promotions.get(position);
        holder.promotionName.setText(promotion.getProductName());
        holder.promotionType.setText(promotion.getTranslatedType());
        holder.promotionDescription.setText(promotion.getDescription());

        holder.itemView.setOnClickListener(v -> {
            if (promotionClickHandler != null)
                if (position >= 0)
                    promotionClickHandler.onPromotionClick(promotions.get(position), position);
        });

    }

    @Override
    public int getItemCount() {
        return promotions.size();
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.promotion_card_view, parent, false);

        return new DataObjectHolder(view);
    }

}
