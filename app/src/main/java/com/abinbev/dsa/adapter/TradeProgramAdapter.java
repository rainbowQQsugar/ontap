package com.abinbev.dsa.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.TradeProgram;

import java.util.List;

import io.mewa.adapterodactil.annotations.Adapt;
import io.mewa.adapterodactil.annotations.Data;
import io.mewa.adapterodactil.annotations.Row;
import io.mewa.adapterodactil.annotations.ViewType;

@Adapt(layout = R.layout.list_item_trade_program, viewGroup = R.id.trade_program_item_container, type = TradeProgram.class)
public abstract class TradeProgramAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

    public static final int TRADE_PROGRAM_TYPE = 0;

    @ViewType(TRADE_PROGRAM_TYPE)
    public static class TradeProgramViewAdapter {

        @Row(num = 0, dataId = R.id.trade_program_item_contract_id)
        public static String contractId(TextView view, TradeProgram s) {
            return s.getContractId();
        }

        @Row(num = 1, dataId = R.id.trade_program_item_name)
        public static String name(TextView view, TradeProgram s) {
            return s.getName();
        }

        @Row(num = 2, dataId = R.id.trade_program_item_validity)
        public static String validRange(TextView view, TradeProgram s) {
            String validFrom = s.getValidFrom();
            String validTo = s.getValidTo();

            if (TextUtils.isEmpty(validFrom) && TextUtils.isEmpty(validTo)) {
                return null;
            }
            else {
                return validFrom + " - " + validTo;
            }
        }

        @Row(num = 3, dataId = R.id.trade_program_item_status)
        public static String status(TextView view, TradeProgram s) {
            return s.getStatus();
        }
    }

    @Data
    public abstract void setData(List<TradeProgram> data);
}
