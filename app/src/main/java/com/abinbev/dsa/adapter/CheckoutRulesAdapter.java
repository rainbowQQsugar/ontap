package com.abinbev.dsa.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.checkoutRules.CheckoutRule;

import java.util.List;

/**
 * Created by Adam Chodera on 20.06.2017.
 */
public class CheckoutRulesAdapter extends RecyclerView.Adapter<CheckoutRulesAdapter.ViewHolder> {

    public interface OnItemClickedListener {
        void onItemClicked();
    }

    private List<CheckoutRule> rules;

    private OnItemClickedListener listener;

    @Override
    public CheckoutRulesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.tasks_before_checkout_list_item, parent, false);

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(CheckoutRulesAdapter.ViewHolder viewHolder, int position) {
        final CheckoutRule checkoutRule = rules.get(position);
        viewHolder.setCheckoutRule(checkoutRule);
        viewHolder.setListener(listener);
    }

    @Override
    public int getItemCount() {
        return rules == null ? 0 : rules.size();
    }

    public void setData(List<CheckoutRule> checkoutRules) {
        this.rules = checkoutRules;
    }

    public void setListener(OnItemClickedListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final Context context;
        final ViewGroup rootView;
        final TextView taskTitleView;

        CheckoutRule checkoutRule;

        OnItemClickedListener listener;

        public ViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            rootView = (ViewGroup) itemView.findViewById(R.id.item_task_root_view);
            taskTitleView = (TextView) itemView.findViewById(R.id.task_title);
        }

        public void setCheckoutRule(CheckoutRule checkoutRule) {
            this.checkoutRule = checkoutRule;
            this.taskTitleView.setText(checkoutRule.getInfoForUser());
            this.rootView.setOnClickListener(this);
        }

        public void setListener(OnItemClickedListener listener) {
            this.listener = listener;
        }

        @Override
        public void onClick(View view) {
            listener.onItemClicked();
            checkoutRule.openScreen(context);
        }
    }
}
