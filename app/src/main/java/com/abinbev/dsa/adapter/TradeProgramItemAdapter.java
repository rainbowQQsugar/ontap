package com.abinbev.dsa.adapter;

import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.TradeProgramItem;

import java.util.List;

import io.mewa.adapterodactil.annotations.Adapt;
import io.mewa.adapterodactil.annotations.Data;
import io.mewa.adapterodactil.annotations.Row;
import io.mewa.adapterodactil.annotations.ViewType;

@Adapt(layout = R.layout.list_item_trade_program_item, viewGroup = R.id.trade_program_item_container, type = TradeProgramItem.class)
public abstract class TradeProgramItemAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

    public static final int TRADE_PROGRAM_ITEM_TYPE = 0;

    @ViewType(TRADE_PROGRAM_ITEM_TYPE)
    public static class TradeProgramItemViewAdapter {

        @Row(num = 0, dataId = R.id.trade_program_item_program_item_id)
        public static String programItemId(TextView view, TradeProgramItem item) {
            return item.getProgramItemId();
        }

        @Row(num = 1, dataId = R.id.trade_program_item_contract_id)
            public static String itemId(TextView view, TradeProgramItem item) {
            return item.getItemId();
        }

        @Row(num = 2, dataId = R.id.trade_program_item_description)
        public static String descriptionOrder(TextView view, TradeProgramItem item) {
            return item.getDescription();
        }

        @Row(num = 3, dataId = R.id.trade_program_item_name)
        public static String name(TextView view, TradeProgramItem item) {
            return item.getName();
        }

        @Row(num = 4, dataId = R.id.trade_program_item_description_order)
        public static String description(TextView view, TradeProgramItem item) {
            return item.getDescriptionOrder();
        }
    }

    @Data
    public abstract void setData(List<TradeProgramItem> data);
}
