package com.salesforce.dsa.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;

import java.util.List;
import java.util.Map;

public class DsaTopIndicatorAdapter extends RecyclerView.Adapter<DsaTopIndicatorAdapter.ViewHolder> {


    private final Context context;
    private Map<Integer, String>  indicators;
    private OnIndicatorClickListener onIndicatorClickListener;

    public DsaTopIndicatorAdapter(Map<Integer, String> indicators, Context context) {
        this.indicators = indicators;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.indicator_item, parent, false));
    }

    @Override
    public void onBindViewHolder(DsaTopIndicatorAdapter.ViewHolder holder, int position) {
        holder.categoryName.setText(indicators.get(position));
        holder.whole_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onIndicatorClickListener != null)
                    onIndicatorClickListener.onIndicatorClickListener(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (indicators != null)
            return indicators.size();
        return 0;
    }

    public void setIndicators(Map<Integer, String> indicators) {
        this.indicators = indicators;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView categoryName;
        LinearLayout whole_item;

        public ViewHolder(View itemView) {
            super(itemView);
            categoryName = (TextView) itemView.findViewById(R.id.category_name);
            whole_item = (LinearLayout) itemView.findViewById(R.id.whole_item);

        }
    }

    public interface OnIndicatorClickListener {
        void onIndicatorClickListener(View view, int position);
    }

    public void setOnIndicatorClickListener(OnIndicatorClickListener onIndicatorClickListener) {
        this.onIndicatorClickListener = onIndicatorClickListener;
    }
}
