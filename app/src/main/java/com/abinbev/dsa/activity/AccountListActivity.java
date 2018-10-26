package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.AccountListAdapter;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.ui.presenter.AccountListPresenter;
import com.abinbev.dsa.ui.view.SortableHeader;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AccountRecordType;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class AccountListActivity extends AppBaseDrawerActivity implements AccountListPresenter.ViewModel,
        AccountListAdapter.AccountClickHandler, AdapterView.OnItemSelectedListener, TextWatcher {

    private static final int REQUEST_CODE_OPEN_ACCOUNT = 1;

    private AccountListPresenter presenter;
    private AccountListAdapter adapter;

    @Bind(R.id.account_list)
    ListView accountList;

    @Bind(R.id.title)
    TextView title;

    @Bind(R.id.add_poc_button)
    FloatingActionButton addPOCButton;

    @Nullable
    @Bind({R.id.account_name, R.id.code, R.id.address, R.id.barrio})
    List<SortableHeader> sortableHeaders;

    @Bind(R.id.searchEditText)
    EditText searchEditText;

    List<String> recordtypeIds;
    int totalPOCCount = -1;
    int pageSize = 50;
    ProgressBar footer;

    boolean showCheckOutWarning = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("");

        footer = new ProgressBar(this);

        processIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        processIntent(intent);
    }


    public void processIntent(Intent intent) {

        title.setText(getResources().getString(R.string.cuentas));
        addPOCButton.setVisibility(View.GONE);
        String recordName = AccountRecordType.ACCOUNT;

        recordtypeIds = RecordType.getRecordIdsListByNameAndObjectType(recordName, AbInBevConstants.AbInBevObjects.ACCOUNT);

        //RecordType recordType = RecordType.getByName(recordName);
        //recordtypeId = recordType.getId();

        accountList.addFooterView(footer);

        adapter = new AccountListAdapter(this);
        accountList.setAdapter(adapter);

        accountList.setOnScrollListener(new AccountsLoader() {
            @Override
            public void loadMore(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (presenter != null) {
                    int index = adapter.getCount() / pageSize;
                    presenter.getAccountsForSearchString(searchEditText.getText().toString(), recordtypeIds, index, pageSize);

                }
            }
        });

        searchEditText.addTextChangedListener(this);
        searchEditText.setOnClickListener(v -> {
            if (v.getId() == searchEditText.getId()) {
                searchEditText.setCursorVisible(true);
            }
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> actionId == EditorInfo.IME_ACTION_SEARCH);


        if (presenter == null) {
            presenter = new AccountListPresenter();
            presenter.setViewModel(this);
            presenter.recordTypeIds = recordtypeIds;

            // checkAccount(null);

            clearSortOnAllOthers(-1); //clear all
//            spinner.setSelection(0);
            presenter.start();

        } else {
            presenter.setViewModel(this);
            presenter.recordTypeIds = recordtypeIds;
            presenter.start();
        }

        searchEditText.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (presenter != null)
            presenter.setViewModel(this);

        if (showCheckOutWarning) {
            showCheckOutWarning = false;
            checkAccount(null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (presenter != null)
            presenter.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mFilterTask);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_ACCOUNT && data != null) {
            showCheckOutWarning = data.getBooleanExtra(AccountOverviewActivity.RESULT_IS_CHECKED_IN, false);
        }
    }

    @Override
    public void onRefresh() {

        if (presenter != null) {
            presenter.start();
        }

        mHandler.removeCallbacks(mFilterTask);
        mHandler.post(mFilterTask);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_account_list;
    }

    @Override
    public void setData(List<Account> accounts) {
        loading = false;
        adapter.setData(accounts);
    }

    @Override
    public void setPOCCount(int size) {

        totalPOCCount = size;
        title.setText(getResources().getString(R.string.cuentas) + " (" + totalPOCCount + ")");
        adapter.clearData();
        footer.setVisibility(View.VISIBLE);
        presenter.getAccountsForSearchString(searchEditText.getText().toString(), recordtypeIds, 0, pageSize);
    }

//    TODO: Commenting out for now, in case sorting comes back
//    @OnClick({R.id.account_name, R.id.code, R.id.address, R.id.barrio, R.id.phone})
//    @SuppressWarnings("unused")
//    public void onHeaderClicked(View view) {
//        SortableHeader sortableHeader = (SortableHeader) view;
//
//        switch (sortableHeader.getId()) {
//            case R.id.account_name :
//                pedidoListAdapter.sortByName(sortableHeader.toggleSortDirection());
//                break;
//            case R.id.code :
//                pedidoListAdapter.sortByCode(sortableHeader.toggleSortDirection());
//                break;
//            case R.id.address :
//                pedidoListAdapter.sortByAddress(sortableHeader.toggleSortDirection());
//                break;
//            case R.id.barrio :
//                pedidoListAdapter.sortByNeighborhood(sortableHeader.toggleSortDirection());
//                break;
//            case R.id.phone :
//                pedidoListAdapter.sortByPhone(sortableHeader.toggleSortDirection());
//                break;
//            default:
//                break;
//        }
//        clearSortOnAllOthers(sortableHeader.getId());
//    }

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
    public void onAccountClick(String accountId) {
        Intent intent = new Intent(this, AccountOverviewActivity.class);
        intent.putExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA, accountId);
        startActivityForResult(intent, REQUEST_CODE_OPEN_ACCOUNT);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        clearSortOnAllOthers(-1);
//        searchEditText.setText("");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // no action
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // perform search
        // searchEditText.setCursorVisible(false);
        if (presenter == null) return;

        adapter.clearData();
        footer.setVisibility(View.VISIBLE);

        mHandler.removeCallbacks(mFilterTask);
        mHandler.postDelayed(mFilterTask, DELAY_BEFORE_SEARCH);

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @OnClick(R.id.add_poc_button)
    public void addPocButtonClicked() {
        Intent intent = new Intent(this, BasicDataDynamicLayoutActivity.class);
        startActivity(intent);
    }

    private Handler mHandler = new Handler();
    private int DELAY_BEFORE_SEARCH = 1000; // 1 second

    Runnable mFilterTask = new Runnable() {
        @Override
        public void run() {
            presenter.getAccountsCount(searchEditText.getText().toString(), recordtypeIds);
        }
    };

    private boolean loading = true;

    abstract class AccountsLoader implements AbsListView.OnScrollListener {

        public AccountsLoader() {
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            if (totalPOCCount >= 0 && totalItemCount >= totalPOCCount) {
                footer.setVisibility(View.GONE);
            } else if (totalItemCount < totalPOCCount && !loading && totalItemCount > 1 && ((firstVisibleItem + visibleItemCount) >= (totalItemCount - 1))) {
                loading = true;
                loadMore(view, firstVisibleItem, visibleItemCount, totalItemCount);
                footer.setVisibility(View.VISIBLE);
            }
        }

        public abstract void loadMore(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount);
    }

}
