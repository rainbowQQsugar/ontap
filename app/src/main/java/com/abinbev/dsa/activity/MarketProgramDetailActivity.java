package com.abinbev.dsa.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.MarketProgramItemAdapter;
import com.abinbev.dsa.model.MarketProgram;
import com.abinbev.dsa.model.MarketProgramItem;
import com.abinbev.dsa.ui.presenter.MarketProgramDetailPresenter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MarketProgramDetailActivity extends AppBaseActivity implements MarketProgramDetailPresenter.ViewModel {

    public static final String MARKET_PROGRAM_ID_EXTRA = "market_program_id";
    private String marketProgramId = "";
    private MarketProgramDetailPresenter marketProgramDetailPresenter;
    private MarketProgramItemAdapter adapter;

    @Bind(R.id.line_items)
    @Nullable
    RecyclerView recyclerView;

    @Bind(R.id.loan_items)
    @Nullable
    TableLayout loanItemTable;

    @Bind(R.id.sales_items)
    @Nullable
    TableLayout salesActualsTable;

    @Bind(R.id.scroll_container)
    @Nullable
    ScrollView scrollContainer;

    @Bind(R.id.empty_view)
    TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (getIntent() != null) {
            marketProgramId = getIntent().getStringExtra(MarketProgramDetailActivity.MARKET_PROGRAM_ID_EXTRA);
        }
        getSupportActionBar().setTitle(getString(R.string.programa_de_mercado));

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet) {
            adapter = new MarketProgramItemAdapter();
            adapter.setIsTablet(isTablet);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        }

        marketProgramDetailPresenter = new MarketProgramDetailPresenter(marketProgramId, isTablet);
        marketProgramDetailPresenter.setViewModel(this);
        marketProgramDetailPresenter.start();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_market_program_detail;
    }

    @Override
    public void setMarketProgram(MarketProgram marketProgram) {
        getSupportActionBar().setSubtitle(marketProgram.getMarketProgram());
    }

    @Override
    public void setLineItems(List<MarketProgramItem> marketProgramItems) {
        adapter.setMarketProgramItems(marketProgramItems);

        if (marketProgramItems.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility((View.GONE));
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility((View.VISIBLE));
        }
    }

    @Override
    public void setLineItemWrapper(MarketProgramDetailPresenter.MarketProgramItemWrapper marketProgramItemWrapper) {
        for (MarketProgramItem marketProgramItem : marketProgramItemWrapper.onLoanMaterials ) {
            TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.loan_item_table_row, loanItemTable, false);
            TextView itemType = (TextView) tableRow.findViewById(R.id.item_type);
            TextView itemValue = (TextView) tableRow.findViewById(R.id.item_value);

            itemType.setText(marketProgramItem.getType() + ": ");
            itemValue.setText(marketProgramItem.getValue());

            String description = marketProgramItem.getDescription();
            if (!TextUtils.isEmpty(description)) {
                itemValue.append(" (" + description + ")");
            }

            loanItemTable.addView(tableRow);
        }

        for (MarketProgramDetailPresenter.MarketProgramSalesActuals marketProgramSalesActuals : marketProgramItemWrapper.salesActuals.values()) {
            TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.sales_actuals_item_table_row, loanItemTable, false);
            ((TextView) tableRow.findViewById(R.id.item_type)).setText(marketProgramSalesActuals.itemType);

            TextView monthValueTv = (TextView) tableRow.findViewById(R.id.item_month_value);
            monthValueTv.setText(marketProgramSalesActuals.monthValue);
            monthValueTv.setTextColor(ContextCompat.getColor(this, contains(marketProgramSalesActuals.monthValue, "-") ? R.color.red : R.color.meter_goal));

            TextView quarterValueTv = ((TextView) tableRow.findViewById(R.id.item_quarter_value));
            quarterValueTv.setText(marketProgramSalesActuals.quarterValue);
            quarterValueTv.setTextColor(ContextCompat.getColor(this, contains(marketProgramSalesActuals.quarterValue, "-") ? R.color.red : R.color.meter_goal));

            salesActualsTable.addView(tableRow);
        }

        if (marketProgramItemWrapper.onLoanMaterials.isEmpty() && marketProgramItemWrapper.salesActuals.isEmpty()) {
            scrollContainer.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private boolean contains(String text, String textFragment) {
        return text != null && text.contains(textFragment);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        marketProgramDetailPresenter.stop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

