package com.abinbev.dsa.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.ui.presenter.ReferencedValuesPresenter.ReferencedValue;

import java.util.Collections;
import java.util.List;

public class ReferencedValuesAdapter extends RecyclerView.Adapter<ReferencedValuesAdapter.ViewHolder> {

    private final OnItemSelectedListener itemSelectedListener;

    private final View.OnClickListener onRowClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ReferencedValue referencedValue = (ReferencedValue) v.getTag();
            itemSelectedListener.onItemSelected(referencedValue);
        }
    };

    private List<ReferencedValue> referencedValues = Collections.emptyList();

    public ReferencedValuesAdapter(OnItemSelectedListener listener) {
        itemSelectedListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.referenced_spinner_dropdown_item, parent, false);
        return new ViewHolder(view);
    }

    public void setReferencedValues(List<ReferencedValue> values) {
        if (values == null) {
            referencedValues = Collections.emptyList();
        }
        else {
            referencedValues = values;
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.item = referencedValues.get(position);
        holder.nameView.setText(referencedValues.get(position).getName());
        holder.view.setTag(holder.item);
        holder.view.setOnClickListener(onRowClickedListener);
    }

    @Override
    public int getItemCount() {
        return referencedValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView nameView;
        public ReferencedValue item;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            this.nameView = (TextView) view.findViewById(android.R.id.text1);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + nameView.getText() + "'";
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(ReferencedValue item);
    }
}
