package com.abinbev.dsa.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.CN_PBO_Contract__c;
import com.abinbev.dsa.ui.presenter.ContractsPresenter.ContractData;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Jakub Stefanowski
 */
public class ContractsAdapter extends RecyclerView.Adapter<ContractsAdapter.DataObjectHolder> {

    public class DataObjectHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.contract_name)
        public TextView name;

        @Bind(R.id.contract_manager)
        public TextView manager;

        @Bind(R.id.contract_start)
        public TextView start;

        @Bind(R.id.contract_end)
        public TextView end;

        @Bind(R.id.contract_id)
        public TextView id;

        @Bind(R.id.contract_status)
        public TextView status;

        public DataObjectHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.contract_card)
        public void onItemClicked() {
            listener.onItemClicked(contracts.get(getAdapterPosition()).contract);
        }
    }

    public interface OnItemClickedListener {
        void onItemClicked(CN_PBO_Contract__c contract);
    }

    List<ContractData> contracts = new ArrayList<>();

    OnItemClickedListener listener;

    public void setData(List<ContractData> contracts) {
        this.contracts.clear();
        this.contracts.addAll(contracts);
        this.notifyDataSetChanged();
    }

    public void setOnItemClickedListener(OnItemClickedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        ContractData contractData = contracts.get(position);
        CN_PBO_Contract__c contract = contractData.contract;

        holder.id.setText(contract.getContractId());
        holder.name.setText(contract.getName());
        holder.manager.setText(contract.getSalesManager());
        holder.start.setText(contract.getStartDateString());
        holder.end.setText(contract.getEndDateString());
        holder.status.setText(contract.getTranslatedStatus());
    }

    @Override
    public int getItemCount() {
        return contracts.size();
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_contract, parent, false);

        return new DataObjectHolder(view);
    }
}
