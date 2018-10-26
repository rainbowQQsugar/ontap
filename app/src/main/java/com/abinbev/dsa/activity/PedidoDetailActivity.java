package com.abinbev.dsa.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.OrderItemAdapter;
import com.abinbev.dsa.model.Order_Item__c;
import com.abinbev.dsa.model.Order__c;
import com.abinbev.dsa.ui.presenter.PedidoDetailPresenter;
import com.abinbev.dsa.ui.view.ShoppingCart;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppPreferenceUtils;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import java.util.List;
import java.util.Locale;

public class PedidoDetailActivity extends AppBaseActivity implements PedidoDetailPresenter.ViewModel, OrderItemAdapter.OrderItemRemovedListener {

    private String orderId = "";
    private String accountId;
    private PedidoDetailPresenter pedidoDetailPresenter;
    private OrderItemAdapter adapter;
    private PopupMenu popupMenu;
    private Order__c order;

    private boolean syncOnDestroy = false;

    @Bind(R.id.shopping_cart)
    ShoppingCart shoppingCart;

    @Bind(R.id.pedido_status)
    TextView pedidoStatus;

    @Bind(R.id.line_items)
    RecyclerView recyclerView;

    @Bind(R.id.empty_view)
    TextView emptyView;

    @Bind(R.id.popup_menu_anchor)
    View popupMenuAnchor;

    @Bind(R.id.order_actions)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (getIntent() != null) {
            orderId = getIntent().getStringExtra(AddProductActivity.EXTRA_ORDER_ID);
            accountId = getIntent().getStringExtra(AddProductActivity.EXTRA_ACCOUNT_ID);
            if (AppPreferenceUtils.getPedidoSyncRequired(this))
                syncOnDestroy = true;
        }
        getSupportActionBar().setTitle(getString(R.string.title_activity_pedido_detail));

        adapter = new OrderItemAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        popupMenu = new PopupMenu(this, popupMenuAnchor);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item != null) {
                    handleMenuClick(item.getItemId());
                }
                return true;
            }
        });

        popupMenu.inflate(R.menu.order_actions);

        pedidoDetailPresenter = new PedidoDetailPresenter(orderId);
        pedidoDetailPresenter.setViewModel(this);
        pedidoDetailPresenter.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pedidoDetailPresenter.start();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_pedido_detail;
    }

    @Override
    public void setOrder(Order__c order) {
        getSupportActionBar().setSubtitle(order.getName());
        String status = order.getTranslatedStatus();
        pedidoStatus.setText(getResources().getString(R.string.order_status_label) + ": " + status);
        this.order = order;

        String source = order.getSource();

        if ((source.isEmpty() || isSFDCOrder(source)) && AbInBevConstants.PedidoStatus.STATUS_OPEN.equals(order.getStatus())) {
            shoppingCart.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
            adapter.setItemsCanBeDeleted(true);

        }  else {
            shoppingCart.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.GONE);
            adapter.setItemsCanBeDeleted(false);
        }
    }

    private boolean isSFDCOrder(String source) {
        Configuration chinaConf = new Configuration(getResources().getConfiguration());
        chinaConf.setLocale(new Locale("zh"));

        Configuration enConf = new Configuration(getResources().getConfiguration());
        enConf.setLocale(new Locale("en"));

        return source.equals(createConfigurationContext(enConf).getResources().getString(R.string.order_source_sfdc))
                || source.equals(createConfigurationContext(chinaConf).getResources().getString(R.string.order_source_sfdc))
                || source.equals(createConfigurationContext(chinaConf).getResources().getString(R.string.order_source_sfdc_no_spaces));
    }


    private void setOrderSubmittedMessage() {
        pedidoStatus.setText(getOrderDetails());
    }

    private SpannableString getOrderDetails() {
        if (order == null)
            return null;
        String status = order.getTranslatedStatus();
        String orderSource = order.getSource();
        String header = getResources().getString(R.string.order_detail_submitted_info);

        String details = "\n\n" + getResources().getString(R.string.order_status_label) + ":" + (!TextUtils.isEmpty(status) ? (" " + status) : "")
                + "\n" + getResources().getString(R.string.order_source_label) + ":" + (!TextUtils.isEmpty(orderSource) ? (" " + orderSource) : "") + "\n";

        SpannableString sps = new SpannableString(header + details);
        sps.setSpan(new StyleSpan(Typeface.BOLD), 0, header.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return sps;
    }

    @Override
    public void setLineItems(List<Order_Item__c> orderItemProducts) {
        adapter.setOrderItems(orderItemProducts);
        shoppingCart.setCount(adapter.getItemCount());

        if (orderItemProducts.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility((View.GONE));
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility((View.VISIBLE));
        }
    }

    void handleMenuClick(int itemId) {
        syncOnDestroy = true;
        switch (itemId) {
            case R.id.order_action_add_products: {
                Intent intent = new Intent(this, AddProductActivity.class);
                intent.putExtra(AddProductActivity.EXTRA_ORDER_ID, orderId);
                intent.putExtra(AddProductActivity.EXTRA_ACCOUNT_ID, accountId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            }

            case R.id.order_action_cancel: {
                pedidoDetailPresenter.updateStatus(AbInBevConstants.PedidoStatus.STATUS_CANCELLED);
                break;
            }

            case R.id.order_action_submit: {
                pedidoDetailPresenter.updateStatus(AbInBevConstants.PedidoStatus.STATUS_SUBMITTED);
                setOrderSubmittedMessage();
                break;
            }
        }
    }

    @Override
    public void orderItemRemoved(Order_Item__c orderItem) {
        pedidoDetailPresenter.removeOrderItem(orderItem);
        shoppingCart.setCount(adapter.getItemCount());
        syncOnDestroy = true;
    }

    @OnClick(R.id.order_actions)
    @SuppressWarnings("unused")
    public void onOrderActionsClick() {
        popupMenu.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pedidoDetailPresenter.stop();

        if (syncOnDestroy) {
            SyncUtils.TriggerRefresh(this);
        }
        finish();
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

