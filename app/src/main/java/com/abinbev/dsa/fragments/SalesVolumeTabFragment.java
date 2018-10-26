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
import com.abinbev.dsa.adapter.CompositeSalesVolumeFilter;
import com.abinbev.dsa.adapter.SalesVolumeListAdapter;
import com.abinbev.dsa.model.SalesVolume;
import com.abinbev.dsa.model.SalesVolumeData;
import com.abinbev.dsa.ui.view.DividerItemDecoration;
import com.abinbev.dsa.ui.view.OrderFilterDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Diana Błaszczyk on 28/10/17.
 */

public class SalesVolumeTabFragment extends Fragment implements OrderFilterDialog.OrderFilterDialogListener {
    private SalesVolumeData salesVolumeData;

    public SalesVolumeListAdapter salesVolumeListAdapter;

    private List<SalesVolume> salesVolumes = new ArrayList<>();
    private Date showOrdersSince;

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

        salesVolumeListAdapter = new SalesVolumeListAdapter();
        salesVolumeListAdapter.setData(salesVolumes, showOrdersSince);
        recyclerView.setAdapter(salesVolumeListAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        salesVolumeListAdapter.setPromotionClickHandler((orderId,accountId) -> {
            Intent intent = new Intent(getActivity(), PedidoDetailActivity.class);
            intent.putExtra(AddProductActivity.EXTRA_ORDER_ID, orderId);
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
        orderFilterDialog.setSalesVolumeData(salesVolumeData);
        CompositeSalesVolumeFilter filter = ((CompositeSalesVolumeFilter) salesVolumeListAdapter.getFilter());
        orderFilterDialog.setOrderStatusSelection(filter.getSelectedOrderStatus());
        orderFilterDialog.setOrderSourceSelection(filter.getSelectedOrderSource());
        orderFilterDialog.setBrandSelection(filter.getSelectedBrand());
        orderFilterDialog.setSkuSelection(filter.getSelectedSku());
        orderFilterDialog.show();

    }

    @Override
    public void onDialogPositiveClick(OrderFilterDialog.OrderFilterSelection orderFilterSelection) {
        CompositeSalesVolumeFilter filter = ((CompositeSalesVolumeFilter) salesVolumeListAdapter.getFilter());
        filter.setOrderStatus(orderFilterSelection.orderStatus);
        filter.setOrderSource(orderFilterSelection.orderSource);
        filter.setStartDate(orderFilterSelection.orderStartDate);
        filter.setEndDate(orderFilterSelection.orderEndDate);
        filter.setSelectedBrand(orderFilterSelection.orderBrand);
        filter.setSelectedSku(orderFilterSelection.orderSku);
        filter.filter("");
        displayFiltersInfo(orderFilterSelection.orderStartDate, orderFilterSelection.orderEndDate,
                orderFilterSelection.orderStatus, orderFilterSelection.orderStatusLabel, orderFilterSelection.orderSource, orderFilterSelection.orderBrand, orderFilterSelection.orderSku);


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
            salesVolumeListAdapter.setData(this.salesVolumes, showOrdersSince);

        } else
            filterTitle.setText(filtersInfo);
    }

    public void setData(SalesVolumeData salesVolumeData, Date showOrdersSince) {
        if (isAdded())
            filterTitle.setText(getResources().getString(R.string.filter));
        this.salesVolumeData = salesVolumeData;
        this.salesVolumes.clear();
        this.salesVolumes.addAll(salesVolumeData.getSalesVolume());
        this.showOrdersSince = showOrdersSince;
        if (salesVolumeListAdapter != null) {
            salesVolumeListAdapter.setData(this.salesVolumes, showOrdersSince);
        }
    }


}
