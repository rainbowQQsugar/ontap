package com.abinbev.dsa.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.OrderHistoryTabsPagerAdapter;
import com.abinbev.dsa.adapter.PedidoListAdapter;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Auth_Keys__c;
import com.abinbev.dsa.model.OrderData;
import com.abinbev.dsa.model.Order__c;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.model.SalesVolumeData;
import com.abinbev.dsa.ui.presenter.B2BOrderPresenter;
import com.abinbev.dsa.ui.presenter.PedidoListPresenter;
import com.abinbev.dsa.ui.view.SortableHeader;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public class PedidoListActivity extends AppBaseDrawerActivity implements PedidoListPresenter.ViewModel, B2BOrderPresenter.ViewModel {

    private static final String TAG = PedidoListActivity.class.getSimpleName();
    private static final String SHOW_ORDERS_SINCE = "showOrdersSince";

    private PedidoListPresenter pedidoListPresenter;
    private Subscription subscription;
    private PedidoListAdapter pedidoListAdapter;

    private Date showOrdersSince;
    OrderHistoryTabsPagerAdapter tabsPagerAdapter;

    String accountId;

    @Bind(R.id.tabs_layout)
    TabLayout tabLayout;

    @Bind(R.id.viewpager)
    ViewPager viewPager;

    @Bind(R.id.new_order)
    FloatingActionButton newOrderButton;

    @Nullable
    @Bind({ R.id.order_number, R.id.order_type, R.id.order_status, R.id.order_created_date, R.id.total_header})
    List<SortableHeader> sortableHeaders;
    private Account account;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle("");

        tabsPagerAdapter = new OrderHistoryTabsPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(tabsPagerAdapter);
//        tabLayout.setupWithViewPager(viewPager);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0)
                    newOrderButton.show();
                else newOrderButton.hide();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        setDefaultDateSince();

        Intent intent = getIntent();
        if (intent != null) {
            accountId = intent.getStringExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA);
            showOrdersSince.setTime(intent.getLongExtra(SHOW_ORDERS_SINCE, -1));
            tabsPagerAdapter.setAccountId(accountId);
        }

    }

    private void setDefaultDateSince() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH, -1);
        showOrdersSince = now.getTime();
    }


    private void goToAddProduct(final String defaultRecordTypeId) {
        subscription.unsubscribe();
        subscription = Observable.just(RecordType.getById(defaultRecordTypeId))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<RecordType>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting record type: ", e);
                    }

                    @Override
                    public void onNext(RecordType recordType) {
                        Intent intent = new Intent(PedidoListActivity.this, AddProductActivity.class);
                        intent.putExtra(AddProductActivity.EXTRA_ORDER_TYPE_NAME, recordType.getName());
                        intent.putExtra(AddProductActivity.EXTRA_ORDER_TYPE_ID, recordType.getId());
                        intent.putExtra(AddProductActivity.EXTRA_ORDER_ID, AddProductActivity.NEW_ORDER);
                        intent.putExtra(AddProductActivity.EXTRA_ACCOUNT_ID, accountId);
                        startActivity(intent);
                    }
                });

    }

    @Nullable
    @OnClick(R.id.new_order)
    public void onNewOrderClick() {
        account = Account.getAccountForId(accountId);
        if (account == null) return;
        String b2BRegistered = account.isB2BRegistered();
        if (!"true".equalsIgnoreCase(b2BRegistered)) {
            String defaultRecordType = RecordType.getDefaultRecordTypeId(this, AbInBevConstants.AbInBevObjects.PEDIDO);
            if (defaultRecordType != null)
                goToAddProduct(defaultRecordType);
        } else {
            B2BOrderPresenter b2BOrderPresenter = new B2BOrderPresenter();
            b2BOrderPresenter.start();
            b2BOrderPresenter.setViewModel(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pedidoListPresenter.stop();
        subscription.unsubscribe();
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
        subscription = Subscriptions.empty();

        newOrderButton.setEnabled(true);

        if (pedidoListPresenter == null) {
            pedidoListPresenter = new PedidoListPresenter(accountId);
        }
        pedidoListPresenter.setViewModel(this);
        pedidoListPresenter.start();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.order_history_list_view;
    }

    @Override
    public void setOrdersData(OrderData ordersData) {
        tabsPagerAdapter.setOrdersData(ordersData, showOrdersSince);

    }

    @Override
    public void setSalesVolumeData(SalesVolumeData salesVolumeData) {
        tabsPagerAdapter.setSalesVolumeData(salesVolumeData, showOrdersSince);
    }

    @Override
    public void getAccountResult(B2BOrderPresenter.B2BParams params) {
        handler.obtainMessage(0, params).sendToTarget();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            B2BOrderPresenter.B2BParams params = (B2BOrderPresenter.B2BParams) msg.obj;
            if (!params.rsp.equals("succ")) {
                Toast.makeText(PedidoListActivity.this, getResources().getString(R.string.net_error), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(params.data.access_token)) {
                Toast.makeText(PedidoListActivity.this, params.data.message, Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(PedidoListActivity.this)
                    .setMessage(getResources().getString(R.string.skip_tips))
                    .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(PedidoListActivity.this, B2BOrderWebViewActivity.class);
                            //params.data.access_token
                            intent.putExtra(B2BOrderWebViewActivity.URL, Auth_Keys__c.getB2BHtmlUrl(params.data.access_token, account.getB2BCode()));
                            PedidoListActivity.this.startActivity(intent);
                        }
                    }).show();
        }
    };
}
