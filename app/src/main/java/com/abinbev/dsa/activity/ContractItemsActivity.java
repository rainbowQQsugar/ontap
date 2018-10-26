package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.ContractItemsAdapter;
import com.abinbev.dsa.model.CN_PBO_Contract_Item__c;
import com.abinbev.dsa.ui.presenter.ContractItemsPresenter;

import java.util.List;

import butterknife.Bind;

/**
 * Created by Jakub Stefanowski
 */
public class ContractItemsActivity extends AppBaseDrawerActivity implements ContractItemsPresenter.ViewModel {

    public static final String ARGS_CONTRACT_ID = "contract_id";

    public static final String ARGS_CONTRACT_NAME = "contract_name";

    ContractItemsAdapter adapter;

    ContractItemsPresenter presenter;

    String contractId;

    String title;

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.title)
    TextView titleTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("");

        Intent intent = getIntent();
        if (intent != null) {
            contractId = intent.getStringExtra(ARGS_CONTRACT_ID);
            title = intent.getStringExtra(ARGS_CONTRACT_NAME);
        }

        titleTextView.setText(title);

        adapter = new ContractItemsAdapter();
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_contract_items;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.stop();
    }

    @Override
    public void onRefresh() {
        if (presenter == null) {
            presenter = new ContractItemsPresenter(contractId);
        }
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override /* ContractItemsPresenter.ViewModel */
    public void setData(List<CN_PBO_Contract_Item__c> contractItems) {
        adapter.setData(contractItems);
    }
}
