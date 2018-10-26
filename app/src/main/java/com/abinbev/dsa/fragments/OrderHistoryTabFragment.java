package com.abinbev.dsa.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.AddProductActivity;
import com.abinbev.dsa.activity.PedidoDetailActivity;
import com.abinbev.dsa.adapter.CompositeOrderFilter;
import com.abinbev.dsa.adapter.PedidoListAdapter;
import com.abinbev.dsa.model.OrderData;
import com.abinbev.dsa.model.Order__c;
import com.abinbev.dsa.ui.view.DividerItemDecoration;
import com.abinbev.dsa.ui.view.OrderFilterDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Diana Błaszczyk on 27/10/17.
 */

public class OrderHistoryTabFragment extends Fragment implements OrderFilterDialog.OrderFilterDialogListener {

    public PedidoListAdapter pedidoListAdapter;
    private List<Order__c> orders = new ArrayList<>();
    private Date showOrdersSince;
    private OrderData orderData;
    private String accountId;

    @Bind(R.id.recycler_view)
    public RecyclerView recyclerView;

    @Bind(R.id.filter)
    ImageView filter;

    @Bind(R.id.filter_title)
    TextView filterTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.orders_list_view, container, false);
        ButterKnife.bind(this, root);

        pedidoListAdapter = new PedidoListAdapter(getActivity(),false);
        pedidoListAdapter.setData(orders, showOrdersSince);
        recyclerView.setAdapter(pedidoListAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        pedidoListAdapter.setPromotionClickHandler((orderId,accountId) -> {
            Intent intent = new Intent(getActivity(), PedidoDetailActivity.class);
            intent.putExtra(AddProductActivity.EXTRA_ORDER_ID, orderId);
            intent.putExtra(AddProductActivity.EXTRA_ACCOUNT_ID, accountId);
            startActivity(intent);
        });

        if (getResources().getBoolean(R.bool.is10InchTablet)) {
            DividerItemDecoration dividerDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
            recyclerView.addItemDecoration(dividerDecoration);
        }

        return root;
    }


    @Nullable
    @OnClick(R.id.filter)
    public void onFilterClick() {
        OrderFilterDialog orderFilterDialog = new OrderFilterDialog(getActivity(), this);
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
                orderFilterSelection.orderStatus, orderFilterSelection.orderStatusLabel, orderFilterSelection.orderSource,orderFilterSelection.orderBrand,orderFilterSelection.orderSku);
    }

    private void displayFiltersInfo(Date orderStartDate, Date orderEndDate, String orderStatus, String orderStatusLabel, String orderSource, String brand, String sku) {
        String filtersInfo = "";

        if (orderStartDate != null) {
            String from = (String) DateFormat.format("dd/MM/yyyy", orderStartDate);
            filtersInfo = getResources().getString(R.string.from) + " " + from;
        }
        if (orderEndDate != null) {
            String to = (String) DateFormat.format("dd/MM/yyy", orderEndDate);
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
            pedidoListAdapter.setData(this.orders, showOrdersSince);

        } else
            filterTitle.setText(filtersInfo);
    }

    public void setData(OrderData orderData, Date showOrdersSince) {
        if (isAdded())
            filterTitle.setText(getResources().getString(R.string.filter));
        this.orderData = orderData;
        this.orders.clear();
        this.orders.addAll(orderData.getOrdersList());
        this.showOrdersSince = showOrdersSince;
        if (pedidoListAdapter != null) {
            pedidoListAdapter.setData(this.orders, showOrdersSince);
        }

    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (pedidoListAdapter !=null) pedidoListAdapter.onRecycle();
    }
}
