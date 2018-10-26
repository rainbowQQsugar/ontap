package com.abinbev.dsa.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.MarketProgramItem;
import com.abinbev.dsa.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MarketProgramItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int LINE_ITEM = 0;
    private static final int ON_LOAN = 1;
    private static final int SALES_ACTUALS = 2;


    boolean isTablet;
    List<MarketProgramItem> marketProgramItems = new ArrayList<>();

    @Override
    public LineItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        switch (viewType) {
            case LINE_ITEM:
                itemView = inflater.inflate(R.layout.market_program_item_view, parent, false);
                return new LineItemViewHolder(itemView);
            case ON_LOAN:
//                itemView = inflater.inflate(R.layout.search_account_item, viewGroup, false);
//                return new AccountSearchViewHolder(itemView);
            case SALES_ACTUALS:
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MarketProgramItem marketProgramItem = marketProgramItems.get(position);
        switch (getItemViewType(position)) {
            case LINE_ITEM:
                LineItemViewHolder viewHolder = (LineItemViewHolder) holder;
                viewHolder.recordType.setText(marketProgramItem.getRecordType());
                viewHolder.type.setText(marketProgramItem.getType());
                viewHolder.period.setText(marketProgramItem.getPeriod());
                viewHolder.date.setText(DateUtils.formatDateStringShort(marketProgramItem.getDate()));
                viewHolder.value.setText(marketProgramItem.getValue());
                break;
            case ON_LOAN:

                break;
            case SALES_ACTUALS:

                break;
            default:
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        // tablet only supports line items
        if (isTablet) {
            return LINE_ITEM;
        }

        MarketProgramItem marketProgramItem = marketProgramItems.get(position);
        String recordType = marketProgramItem.getRecordType();
//        if (MarketProgramItem.LOAN_TYPE.equalsIgnoreCase(recordType)) {
//            return ON_LOAN;
//        } else if (MarketProgramItem.SALES_ACTUALS_TYPE.equalsIgnoreCase(recordType)) {
//            return SALES_ACTUALS;
//        } else {
            return LINE_ITEM;
//        }
    }

    @Override
    public int getItemCount() {
        return marketProgramItems.size();
    }

    public void setMarketProgramItems(List<MarketProgramItem> marketProgramItems){
        this.marketProgramItems.clear();
        this.marketProgramItems.addAll(marketProgramItems);
        notifyDataSetChanged();
    }

    public void setIsTablet(boolean isTablet) {
        this.isTablet = isTablet;
    }

    class LineItemViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.item_record_type)
        TextView recordType;

        @Bind(R.id.item_type)
        TextView type;

        @Bind(R.id.item_period)
        TextView period;

        @Bind(R.id.item_date)
        TextView date;

        @Bind(R.id.item_value)
        TextView value;

        public LineItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}


