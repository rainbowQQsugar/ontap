package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.widget.ImageView;
import android.widget.TextView;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.CompositeOrderFilter;
import com.abinbev.dsa.adapter.PedidoListAdapter;
import com.abinbev.dsa.model.OrderData;
import com.abinbev.dsa.model.SalesVolumeData;
import com.abinbev.dsa.ui.presenter.PedidoListPresenter;
import com.abinbev.dsa.ui.view.OrderFilterDialog;
import com.abinbev.dsa.utils.DateUtils;

import java.util.Date;

import butterknife.Bind;
import butterknife.OnClick;

public class MyordersActivity extends AppBaseDrawerActivity implements OrderFilterDialog.OrderFilterDialogListener, PedidoListPresenter.ViewModel {

    @Bind(R.id.filter_title)
    TextView filterTitle;
    @Bind(R.id.filter)
    ImageView filter;
    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    private PedidoListPresenter pedidoListPresenter;
    private PedidoListAdapter pedidoListAdapter;
    private OrderData orderData;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_my_orders;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pedidoListPresenter = new PedidoListPresenter(true);
        pedidoListPresenter.setViewModel(this);
        pedidoListPresenter.start();
    }

    @Override
    public void onRefresh() {
        pedidoListPresenter.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        filterTitle.setText(getResources().getString(R.string.filter));
    }

    @OnClick(R.id.filter)
    public void onViewClicked() {
        OrderFilterDialog orderFilterDialog = new OrderFilterDialog(this, this);
        orderFilterDialog.setOrderData(orderData);
        CompositeOrderFilter filter = ((CompositeOrderFilter) pedidoListAdapter.getFilter());
        orderFilterDialog.setOrderStatusSelection(filter.getSelectedOrderStatus());
        orderFilterDialog.setOrderSourceSelection(filter.getSelectedOrderSource());
        orderFilterDialog.show();
    }

    @Override
    public void onDialogPositiveClick(OrderFilterDialog.OrderFilterSelection orderFilterSelection) {
        CompositeOrderFilter filter = ((CompositeOrderFilter) pedidoListAdapter.getFilter());
        filter.setOrderStatus(orderFilterSelection.orderStatus);
        filter.setOrderSource(orderFilterSelection.orderSource);
        filter.setOrderStartDate(orderFilterSelection.orderStartDate);
        filter.setOrderEndDate(orderFilterSelection.orderEndDate);
        filter.setSelectedBrand(orderFilterSelection.orderBrand);
        filter.setSelectedSku(orderFilterSelection.orderSku);
        filter.filter("");
        displayFiltersInfo(orderFilterSelection.orderStartDate, orderFilterSelection.orderEndDate,
                orderFilterSelection.orderStatus, orderFilterSelection.orderStatusLabel, orderFilterSelection.orderSource, orderFilterSelection.orderBrand, orderFilterSelection.orderSku);
    }

    private void displayFiltersInfo(Date orderStartDate, Date orderEndDate, String orderStatus, String orderStatusLabel, String orderSource, String brand, String sku) {
        String filtersInfo = "";

        if (orderStartDate != null) {
            String from = DateUtils.DATE_STRING_FORMAT2.format(orderStartDate);
            filtersInfo = getResources().getString(R.string.from) + " " + from;
        }
        if (orderEndDate != null) {
            String to = DateUtils.DATE_STRING_FORMAT2.format(orderEndDate);
            filtersInfo += " " + getResources().getString(R.string.to) + " " + to;
        }

        String allValues = ABInBevApp.getAppContext().getString(R.string.all_values);
        if (!orderStatus.equals(allValues))
            filtersInfo += (filtersInfo.isEmpty() ? "" : "\n") + getResources().getString(R.string.status) + ": " + orderStatusLabel;
        if (!orderSource.equals(allValues))
            filtersInfo += (filtersInfo.isEmpty() ? "" : "\n") + getResources().getString(R.string.order_source) + ": " + orderSource;
        if (!brand.equals(allValues))
            filtersInfo += (filtersInfo.isEmpty() ? "" : "\n") + getResources().getString(R.string.brand) + ": " + brand;
        if (!sku.equals(allValues))
            filtersInfo += (filtersInfo.isEmpty() ? "" : "\n") + getResources().getString(R.string.sku_label) + ": " + sku;


        if (filtersInfo.isEmpty()) {
            filterTitle.setText(getResources().getString(R.string.filter));
            pedidoListAdapter.setData(this.orderData.getOrdersList(), pedidoListPresenter.getShowOrdersSince());

        } else
            filterTitle.setText(filtersInfo);
    }

    @Override
    public void setOrdersData(OrderData ordersData) {
        this.orderData = ordersData;

        pedidoListAdapter = new PedidoListAdapter(this, true);
        recyclerView.setAdapter(pedidoListAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        pedidoListAdapter.setData(ordersData.getOrdersList(), pedidoListPresenter.getShowOrdersSince());
        pedidoListAdapter.setPromotionClickHandler(new PedidoListAdapter.OrderClickHandler() {
            @Override
            public void onOrderClick(String orderId, String accountId) {
                Intent intent = new Intent(MyordersActivity.this, PedidoDetailActivity.class);
                intent.putExtra(AddProductActivity.EXTRA_ORDER_ID, orderId);
                intent.putExtra(AddProductActivity.EXTRA_ACCOUNT_ID, accountId);
                startActivity(intent);
            }
        });
    }

    @Override
    public void setSalesVolumeData(SalesVolumeData salesVolumeData) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pedidoListAdapter != null) pedidoListAdapter.onRecycle();
    }
}
