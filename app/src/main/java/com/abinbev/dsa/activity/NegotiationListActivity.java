package com.abinbev.dsa.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.NegotiationListAdapter;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.ui.presenter.NegotiationListPresenter;
import com.abinbev.dsa.ui.view.SortableHeader;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.PermissionManager;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class NegotiationListActivity extends AppBaseDrawerActivity implements NegotiationListPresenter.ViewModel, NegotiationListAdapter.NegotiationClickHandler {

    private NegotiationListPresenter negotiationListPresenter;
    private NegotiationListAdapter negotiationListAdapter;

    @Bind(R.id.negotiation_list)
    ListView negotiationList;

    @Nullable
    @Bind({R.id.negotiation_number, R.id.status, R.id.type})
    List<SortableHeader> sortableHeaders;

    @Bind(R.id.new_negotiation)
    FloatingActionButton newNegotiationButton;

    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("");

        context = this;

        negotiationListAdapter = new NegotiationListAdapter(this);
        negotiationListAdapter.setSortingOrder(this.getResources().getStringArray(R.array.negotiation_statuses));
        negotiationList.setAdapter(negotiationListAdapter);

        if (!PermissionManager.getInstance().hasPermission(PermissionManager.CREATE_NEGOTIATION)) {
            newNegotiationButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        negotiationListPresenter.stop();
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
    public void onRefresh() {
        clearSortOnAllOthers(-1); //clear all
        String accountId = getIntent().getStringExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA);
        if (negotiationListPresenter == null) {
            negotiationListPresenter = new NegotiationListPresenter(accountId);
        }
        negotiationListPresenter.setViewModel(this);
        negotiationListPresenter.start();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_negotiations_list;
    }

    @Override
    public void setData(List<CN_Product_Negotiation__c> negotiations) {
        negotiationListAdapter.setData(negotiations);
    }

    @Nullable
    @OnClick({R.id.negotiation_number, R.id.status, R.id.type})
    @SuppressWarnings("unused")
    public void onHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;

        switch (sortableHeader.getId()) {
            case R.id.negotiation_number:
                negotiationListAdapter.sortByNegotiationNumber(sortableHeader.toggleSortDirection());
                break;
            case R.id.status:
                negotiationListAdapter.sortByStatus();
                break;
            case R.id.type:
                negotiationListAdapter.sortByType(sortableHeader.toggleSortDirection());
                break;
            default:
                break;
        }
        clearSortOnAllOthers(sortableHeader.getId());
    }

    /**
     * Clears the sort order on all headers in the group EXCEPT for the id of the view passed in
     *
     * @param sortableHeaderId - the id to NOT clear the sort on
     */
    private void clearSortOnAllOthers(int sortableHeaderId) {
        for (SortableHeader sortableHeader : sortableHeaders) {
            if (sortableHeader.getId() != sortableHeaderId) {
                sortableHeader.clearSortIndicator();
            }
        }
    }

    @Override
    public void onNegotiationClick(String negotiationId) {
        negotiationListPresenter.goToNegotiationDetail(this, negotiationId);
    }

    @OnClick(R.id.new_negotiation)
    @SuppressWarnings("unused")
    public void onNewNegotiationClick() {
        showDialog();
    }

    void showDialog() {
        final String[] choice = {""};
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        alt_bld.setTitle(R.string.select_negotation_type);
        CharSequence[] types = {
                getResources().getString(R.string.promotion),
                getResources().getString(R.string.product_sellin),
                getResources().getString(R.string.contract_negotiation),
                getResources().getString(R.string.trade_program),
                getResources().getString(R.string.listing),
                getResources().getString(R.string.sales_event)
        };
        alt_bld.setSingleChoiceItems(types, -1, new DialogInterface
                .OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                choice[0] = (String) types[item];

            }
        });
        alt_bld.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (choice[0].equals(getResources().getString(R.string.product_sellin))) {
                    negotiationListPresenter.createNewProductNegotiation(context, RecordType.getByName(AbInBevConstants.NegotiationType.SELL_IN).getId());
                } else if (choice[0].equals(getResources().getString(R.string.promotion))) {
                    negotiationListPresenter.createNewProductNegotiation(context, RecordType.getByName(AbInBevConstants.NegotiationType.PROMOTION).getId());
                } else if (choice[0].equals(getResources().getString(R.string.contract_negotiation))) {
                    negotiationListPresenter.createNewProductNegotiation(context, RecordType.getByName(AbInBevConstants.NegotiationType.CONTRACT).getId());
                } else if (choice[0].equals(getResources().getString(R.string.trade_program))) {
                    negotiationListPresenter.createNewProductNegotiation(context, RecordType.getByName(AbInBevConstants.NegotiationType.TRADE_PROGRAM).getId());
                } else if (choice[0].equals(getResources().getString(R.string.listing))) {
                    negotiationListPresenter.createNewProductNegotiation(context, RecordType.getByName(AbInBevConstants.NegotiationType.LISTING).getId());
                } else if (choice[0].equals(getResources().getString(R.string.sales_event))){
                    negotiationListPresenter.createNewProductNegotiation(context, RecordType.getByName(AbInBevConstants.NegotiationType.SALES_EVENT).getId());
                }
            }
        });
        AlertDialog alert = alt_bld.create();
        alert.show();
    }

}
