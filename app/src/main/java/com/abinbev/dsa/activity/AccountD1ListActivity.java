package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.AccountD1ListAdapter;
import com.abinbev.dsa.model.CN_Technicians__c;
import com.abinbev.dsa.ui.presenter.AccountD1ListPresenter;

import java.util.List;

import butterknife.Bind;

public class AccountD1ListActivity extends AppBaseDrawerActivity implements AccountD1ListPresenter.ViewModel {

    public static final String TAG = AccountD1ListActivity.class.getSimpleName();

    public static final String ARGS_ACCOUNT_ID = "account_id";
    public static final String ARGS_IS_CHECKED_IN = "checked_in";

    private AccountD1ListAdapter adapter;
    private AccountD1ListPresenter accountD1ListPresenter;
    private String accountId;

    @Bind(R.id.d1_list)
    ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new AccountD1ListAdapter(this);
        listView.setAdapter(adapter);
        listView.setEmptyView(getEmptyView());
        Intent intent = getIntent();
        if (intent != null) {
            accountId = intent.getStringExtra(ARGS_ACCOUNT_ID);
        }
        getSupportActionBar().setTitle("");
    }

    private View getEmptyView() {
        TextView textView = new TextView(this);
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        textView.setLayoutParams(params);
        params.topMargin = 50;
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setText(getResources().getString(R.string.list_empty));
        return textView;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_d1_list_view;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        accountD1ListPresenter.stop();
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
        Intent intent = getIntent();
        if (intent != null) {
            accountId = intent.getStringExtra(ARGS_ACCOUNT_ID);
            if (accountD1ListPresenter == null) {
                accountD1ListPresenter = new AccountD1ListPresenter(accountId);
            }
            accountD1ListPresenter.setViewModel(this);
            accountD1ListPresenter.start();
        }
    }

    @Override
    public void setD1List(List<CN_Technicians__c> technicians) {
        adapter.setData(technicians);
    }
}
