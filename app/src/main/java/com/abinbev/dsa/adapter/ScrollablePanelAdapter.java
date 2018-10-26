package com.abinbev.dsa.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.CN_KPI_Dict__c;
import com.abinbev.dsa.ui.customviews.PanelAdapter;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kelin on 16-11-18.
 */

public class ScrollablePanelAdapter extends PanelAdapter {
    private static final int TITLE_TYPE = 4;
    private static final int KPI_NAME_TYPE = 0;
    private static final int VALUE_TYPE = 1;
    private static final int CONTENT_TYPE = 2;
    private String[] firstRowValues;
    private List<String> firstColumeValues;
    private List<List<String>> contentValues = new ArrayList<>();
    private String TAG = getClass().getSimpleName();
    private String[] desciptionValue = new String[]{AbInBevConstants.KPIStatisticFields.CN_TTL_Volume_Rate__c, AbInBevConstants.KPIStatisticFields.CN_Key_SKU_Rate__c
            , AbInBevConstants.KPIStatisticFields.CN_POCE_Compliance_Rate__c, AbInBevConstants.KPIStatisticFields.CN_Distribution_Rate__c, AbInBevConstants.KPIStatisticFields.CN_Visit_Compliance_Rate__c
            , AbInBevConstants.KPIStatisticFields.CN_B2B_Volume_Rate__c};
    private AlertDialog alertDialog;
    private int firstColumItemWidth;

    @Override
    public int getRowCount() {
        return firstColumeValues.size() + 1;
    }

    @Override
    public int getColumnCount() {
        return firstRowValues.length + 1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int row, int column) {
        int viewType = getItemViewType(row, column);
        switch (viewType) {
            case VALUE_TYPE:
                TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
                setValueView(column, titleViewHolder);
                break;
            case KPI_NAME_TYPE:
                KPIViewHolder holder1 = (KPIViewHolder) holder;
                setKPINameView(row, holder1);
                setClickListener(holder1, row);
                break;
            case CONTENT_TYPE:
                setContentView(row, column, (TitleViewHolder) holder);
                break;
            case TITLE_TYPE:
                setTitleView((TitleViewHolder) holder);
                break;
            default:
                setContentView(row, column, (TitleViewHolder) holder);
        }

    }

    private void setClickListener(KPIViewHolder holder, int row) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String kpiDescription = CN_KPI_Dict__c.getDesc1(firstColumeValues.get(row - 1));
                if (TextUtils.isEmpty(kpiDescription))
                    kpiDescription = holder.context.getString(R.string.no_instructions);
                alertDialog = new AlertDialog.Builder(holder.context)
                        .setMessage(kpiDescription)
                        .setPositiveButton(holder.context.getString(R.string.ok), null)
                        .show();
            }
        });
    }


    public int getItemViewType(int row, int column) {
        if (column == 0 && row == 0) {
            return TITLE_TYPE;
        }
        if (column == 0) {
            return KPI_NAME_TYPE;
        }
        if (row == 0) {
            return VALUE_TYPE;
        }
        return CONTENT_TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case KPI_NAME_TYPE:
                return new KPIViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_kpi_name, parent, false), parent.getContext());
            case CONTENT_TYPE:
            case VALUE_TYPE:
                return new TitleViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_title, parent, false), parent.getContext());
            case TITLE_TYPE:
                return new TitleViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_title1, parent, false), parent.getContext());
            default:
                break;
        }
        return new TitleViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_title, parent, false), parent.getContext());
    }


    private void setValueView(int pos, TitleViewHolder viewHolder) {
        viewHolder.titleTextView.setTextColor(Color.WHITE);
        viewHolder.titleTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        viewHolder.titleTextView.setText(firstRowValues[pos - 1]);
        ViewGroup.LayoutParams lp = viewHolder.titleTextView.getLayoutParams();
        lp.width = (ViewUtils.getScreenWidth(viewHolder.context) - firstColumItemWidth) / firstRowValues.length;
        viewHolder.titleTextView.setLayoutParams(lp);
    }

    private void setKPINameView(int pos, KPIViewHolder viewHolder) {
        if (pos % 2 == 1) viewHolder.ll_kpi_name.setBackgroundColor(Color.WHITE);
        else
            viewHolder.ll_kpi_name.setBackgroundColor(viewHolder.context.getResources().getColor(R.color.abi_gray));
        viewHolder.kpi_name.setText(firstColumeValues.get(pos - 1));
    }

    private void setContentView(int row, int column, TitleViewHolder viewHolder) {
        if (row % 2 == 1) viewHolder.titleTextView.setBackgroundColor(Color.WHITE);
        else
            viewHolder.titleTextView.setBackgroundColor(viewHolder.context.getResources().getColor(R.color.abi_gray));
        column -= 1;
        row -= 1;
        String value = contentValues.get(column).get(row);
        value = TextUtils.isEmpty(value) ? "0" : (Math.round(Double.valueOf(value)) + "");
        viewHolder.titleTextView.setText(column == 2 ? value + "%" : value);
        ViewGroup.LayoutParams lp = viewHolder.titleTextView.getLayoutParams();
        lp.width = (ViewUtils.getScreenWidth(viewHolder.context) - firstColumItemWidth) / firstRowValues.length;
        viewHolder.titleTextView.setLayoutParams(lp);
    }

    private void setTitleView(TitleViewHolder holder) {
        holder.titleTextView.setTextColor(Color.WHITE);
        holder.titleTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        holder.ll_item.measure(0, 0);
        setFirstColumItemWidth(holder.ll_item.getMeasuredWidth());
    }

    public void setValues(String[] values) {
        this.firstRowValues = values;
    }

    public void setFirstColume(List<String> firstColumeValues) {
        this.firstColumeValues = firstColumeValues;
    }

    public void setContentValues(List<List<String>> contentValues) {
        this.contentValues.clear();
        this.contentValues.addAll(contentValues);
    }

    private static class KPIViewHolder extends RecyclerView.ViewHolder {
        public TextView kpi_name, question_mark;
        public LinearLayout ll_kpi_name;
        public Context context;

        public KPIViewHolder(View itemView, Context context) {
            super(itemView);
            this.kpi_name = (TextView) itemView.findViewById(R.id.kpi_name);
            this.ll_kpi_name = (LinearLayout) itemView.findViewById(R.id.ll_kpi_name);
            this.context = context;
        }

    }

    public void setFirstColumItemWidth(int firstColumItemWidth) {
        this.firstColumItemWidth = firstColumItemWidth;
    }

    private class TitleViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public LinearLayout ll_item;
        public Context context;

        public TitleViewHolder(View view, Context context) {
            super(view);
            this.titleTextView = (TextView) view.findViewById(R.id.title);
            this.ll_item = (LinearLayout) view.findViewById(R.id.ll_item);
            this.context = context;
        }
    }

}
