package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.MarketProgramsListAdapter;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.MarketProgram;
import com.abinbev.dsa.ui.presenter.MarketProgamsListPresenter;
import com.abinbev.dsa.ui.view.SortableHeader;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class MarketProgramsListActivity extends AppBaseDrawerActivity implements MarketProgamsListPresenter.ViewModel{
    public static final String ACCOUNT_ID_EXTRA = "account_id";

    @Bind(R.id.market_program_list)
    ListView marketProgramListView;

    @Nullable
    @Bind({ R.id.market_program, R.id.market_program_state, R.id.market_program_start_date, R.id.market_program_end_date})
    List<SortableHeader> sortableHeaders;

    private MarketProgramsListAdapter adapter;
    private MarketProgamsListPresenter presenter;
    private String accountId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new MarketProgramsListAdapter();
        marketProgramListView.setAdapter(adapter);


        getSupportActionBar().setTitle(getString(R.string.programa_de_mercado));

        Intent intent = getIntent();
        if (intent != null) {
            accountId = intent.getStringExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA);
            Account account  = Account.getById(accountId);

            getSupportActionBar().setSubtitle(account.getName());

            if (presenter == null) {
                presenter = new MarketProgamsListPresenter(accountId);
            }
            presenter.setViewModel(this);
            presenter.start();
        }

        marketProgramListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                MarketProgram marketProgram = (MarketProgram)adapter.getItem(position);
                goToDetailView(marketProgram);
            }
        });

    }

    @Override
    public void onRefresh() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.stop();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_market_program_list;
    }

    @Override
    public void setData(List<MarketProgram> marketPrograms){
        adapter.setData(marketPrograms);
    }

    @OnClick(R.id.market_program)
    @Nullable
    public void onMarketProgramHeaderClick(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByMarketProgram(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @OnClick(R.id.market_program_state)
    @Nullable
    public void onMarketProgramStatusHeaderClick(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByState(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @OnClick(R.id.market_program_start_date)
    @Nullable
    public void onMarketProgramStartDateHeaderClick(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByStartDate(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @OnClick(R.id.market_program_end_date)
    @Nullable
    public void onMarketProgramEndDateHeaderClick(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByEndDate(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    /**
     * Clears the sort order on all headers in the group EXCEPT for the id of the view passed in
     * @param sortableHeaderId - the id to NOT clear the sort on
     */
    private void clearSortOnAllOthers(int sortableHeaderId) {
        for (SortableHeader sortableHeader : sortableHeaders) {
            if (sortableHeader.getId() != sortableHeaderId) {
                sortableHeader.clearSortIndicator();
            }
        }
    }

    private void goToDetailView(MarketProgram marketProgram) {
        Intent intent = new Intent(this, MarketProgramDetailActivity.class);
        intent.putExtra(MarketProgramDetailActivity.MARKET_PROGRAM_ID_EXTRA, marketProgram.getId());
        startActivity(intent);
    }
}
