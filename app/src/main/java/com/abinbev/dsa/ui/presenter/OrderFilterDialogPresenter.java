package com.abinbev.dsa.ui.presenter;

import android.content.Context;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.PedidoFields;
import com.abinbev.dsa.utils.AbInBevConstants.PedidoStatus;
import com.abinbev.dsa.utils.PicklistUtils;
import com.salesforce.androidsyncengine.data.model.PicklistValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderFilterDialogPresenter extends AbstractRxPresenter <OrderFilterDialogPresenter.ViewModel> implements Presenter<OrderFilterDialogPresenter.ViewModel> {
    public interface ViewModel {
        void setOrderStatuses(List<PicklistValue> orderStatuses);
    }

    private ViewModel viewModel;
    private Context context;

    public OrderFilterDialogPresenter() {
        super();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        super.setViewModel(viewModel);
        this.viewModel = viewModel;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void start() {
        super.start();
        fetchOrderStatuses();
    }


    private void fetchOrderStatuses() {
        Set<String> requiredStatuses = new HashSet<>();
        requiredStatuses.add(PedidoStatus.STATUS_OPEN);
        requiredStatuses.add(PedidoStatus.STATUS_SUBMITTED);
        requiredStatuses.add(PedidoStatus.STATUS_CANCELLED);
//        requiredStatuses.add(PedidoStatus.STATUS_CONFIRM);
//        requiredStatuses.add(PedidoStatus.STATUS_IN_DELIVERY);
//        requiredStatuses.add(PedidoStatus.STATUS_DELIVERY);
        requiredStatuses.add(PedidoStatus.STATUS_COMPLETED);
        requiredStatuses.add(PedidoStatus.STATUS_REJECT);
        requiredStatuses.add(PedidoStatus.STATUS_PROCESSING);
        requiredStatuses.add(PedidoStatus.STATUS_SHIPPED);
        requiredStatuses.add(PedidoStatus.STATUS_RECEIVED);

        List<PicklistValue> orderStatuses = getPicklistValuesFor(requiredStatuses);

        String allValues = ABInBevApp.getAppContext().getString(R.string.all_values);
        PicklistValue todosPicklistValue = new PicklistValue();
        todosPicklistValue.setLabel(allValues);
        todosPicklistValue.setValue(allValues);

        orderStatuses.add(0, todosPicklistValue);

        viewModel.setOrderStatuses(orderStatuses);
    }

    private List<PicklistValue> getPicklistValuesFor(Set<String> statusNames) {
        List<PicklistValue> result = new ArrayList<>();

        HashMap<String, List<PicklistValue>> picklistValuesMap = PicklistUtils.getMetadataPicklistValues(
                AbInBevObjects.PEDIDO, PedidoFields.STATUS);
        List<PicklistValue> statusPicklistValues = picklistValuesMap.get(PedidoFields.STATUS);

        if (statusPicklistValues != null && !statusPicklistValues.isEmpty()) {

            for (PicklistValue pv: statusPicklistValues) {
                if (statusNames.contains(pv.getValue())) {
                    result.add(pv);
                }
            }
        }

        return result;
    }

    @Override
    public void stop() {
        super.stop();
        viewModel = null;
        context = null;
    }
}
