package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.PicklistValueAdapter;
import com.abinbev.dsa.model.OrderData;
import com.abinbev.dsa.model.Order__c;
import com.abinbev.dsa.model.SalesVolumeData;
import com.abinbev.dsa.ui.presenter.OrderFilterDialogPresenter;
import com.salesforce.androidsyncengine.data.model.PicklistValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A custom dialog to handle the filter criteria for {@link Order__c}
 */
public class OrderFilterDialog extends AppCompatDialog implements OrderFilterDialogPresenter.ViewModel {

    @Bind(R.id.order_statuses)
    Spinner orderStatusSpinner;
    @Bind(R.id.header_view)
    OrderDateHeaderView headerView;
    @Bind(R.id.order_source)
    Spinner orderSourceSpinner;
    @Bind(R.id.order_brand)
    AutoCompleteTextView orderBrandAC;
    @Bind(R.id.order_sku)
    AutoCompleteTextView orderSkuAC;

    private PicklistValueAdapter orderStatusAdapter;
    private PicklistValueAdapter orderSourceAdapter;

    private OrderFilterDialogPresenter presenter;
    private OrderFilterDialogListener listener;

    public OrderFilterDialog(Context context, OrderFilterDialogListener listener) {
        super(context, R.style.AppCompatAlertDialogStyle);
        setContentView(R.layout.pedido_filter);
        ButterKnife.bind(this);

        this.listener = listener;
        orderStatusAdapter = new PicklistValueAdapter(getContext(), R.layout.dropdown_text_item);
        orderStatusAdapter.setDropDownViewResource(R.layout.twoline_spinner_item);
        orderStatusSpinner.setAdapter(orderStatusAdapter);

        orderSourceAdapter = new PicklistValueAdapter(getContext(), R.layout.dropdown_text_item);
        orderSourceAdapter.setDropDownViewResource(R.layout.twoline_spinner_item);
        orderSourceSpinner.setAdapter(orderSourceAdapter);

        presenter = new OrderFilterDialogPresenter();
        presenter.setViewModel(this);
        presenter.setContext(getContext());
        presenter.start();

    }

    public void setBrandSelection(String brandSelection) {
        orderBrandAC.setText(brandSelection);
    }

    public void setSkuSelection(String skuSelection) {
        orderSkuAC.setText(skuSelection);
    }

    public void setOrderData(OrderData orderData) {
        initOrderBrandListener(new ArrayList<>(orderData.getUniqueOrderBrand()));
        initOrderSKUListener(new ArrayList<>(orderData.getUniqueOrderSku()));
        setOrderSourceAdapter(orderData.getOrderSourceList());

    }

    public void setSalesVolumeData(SalesVolumeData salesVolumeData) {
        initOrderBrandListener(new ArrayList<>(salesVolumeData.getUniqueOrderBrand()));
        initOrderSKUListener(new ArrayList<>(salesVolumeData.getUniqueOrderSku()));
        setOrderSourceAdapter(salesVolumeData.getOrderSourceList());

    }

    public interface OrderFilterDialogListener {
        void onDialogPositiveClick(OrderFilterSelection orderFilterSelection);
    }

    public class OrderFilterSelection {
        public String orderStatusLabel = "";
        public String orderStatus = "";
        public String orderSource = "";
        public Date orderStartDate;
        public Date orderEndDate;
        public String orderBrand;
        public String orderSku;
    }


    private void initOrderBrandListener(List<String> orderBrand) {
        LimitArrayAdapter<String> adapter = new LimitArrayAdapter<>(getContext(), R.layout.dropdown_text_item, orderBrand);
        orderBrandAC.setThreshold(0);
        orderBrandAC.setAdapter(adapter);

        orderBrandAC.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                orderBrandAC.showDropDown();
                return false;
            }
        });

    }

    private void initOrderSKUListener(List<String> orderSku) {
        LimitArrayAdapter<String> adapter = new LimitArrayAdapter<>(getContext(), R.layout.dropdown_text_item, orderSku);
        orderSkuAC.setThreshold(0);
        orderSkuAC.setAdapter(adapter);

        orderSkuAC.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                orderSkuAC.showDropDown();
                return false;
            }
        });
    }


    public void setOrderStatusSelection(String selectedOrderStatus) {
        orderStatusSpinner.setSelection(orderStatusAdapter.getPositionByValue(selectedOrderStatus));
    }

    public void setOrderSourceSelection(String selectedOrderSource) {
        orderSourceSpinner.setSelection(orderSourceAdapter.getPositionByValue(selectedOrderSource));

    }

    @Override
    public void setOrderStatuses(List<PicklistValue> orderStatuses) {
        orderStatusAdapter.addAll(orderStatuses);
    }

    private void setOrderSourceAdapter(SortedSet<String> values) {
        List<PicklistValue> picklistValues = new ArrayList<>();
        for (String item : values) {
            PicklistValue picklistValue = new PicklistValue();
            picklistValue.setLabel(item);
            picklistValue.setValue(item);
            picklistValues.add(picklistValue);
        }
        orderSourceAdapter.addAll(picklistValues);
    }
    @OnClick(R.id.cancel)
    void onCancelClicked() {
        dismiss();
    }

    @OnClick(R.id.apply)
    void onApplyClicked() {
        PicklistValue selectedPicklistValue = (PicklistValue) orderStatusSpinner.getSelectedItem();
        String allValues = ABInBevApp.getAppContext().getString(R.string.all_values);

        OrderFilterSelection orderFilterSelection = new OrderFilterSelection();
        String orderSource = orderSourceSpinner.getSelectedItem().toString();
        orderFilterSelection.orderSource = orderSource;
        String orderBrand = orderBrandAC.getText().toString();
        orderFilterSelection.orderBrand = TextUtils.isEmpty(orderBrand) ? allValues : orderBrand;
        String orderSku = orderSkuAC.getText().toString();
        orderFilterSelection.orderSku = TextUtils.isEmpty(orderSku) ? allValues : orderSku;
        orderFilterSelection.orderStatusLabel = selectedPicklistValue == null ? null : selectedPicklistValue.getLabel();
        orderFilterSelection.orderStatus = selectedPicklistValue == null ? null : selectedPicklistValue.getValue();
        orderFilterSelection.orderStartDate = headerView.startDate;
        orderFilterSelection.orderEndDate = headerView.endDate;

        listener.onDialogPositiveClick(orderFilterSelection);
        dismiss();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.stop();
        presenter = null;
    }

    private class LimitArrayAdapter<T> extends ArrayAdapter<T> {
        private final int LIMIT = 10;
        public LimitArrayAdapter(Context context, int textViewResourceId, List<T> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public int getCount() {
            return Math.min(LIMIT, super.getCount());
        }

    }
}
