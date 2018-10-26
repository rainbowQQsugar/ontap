package com.abinbev.dsa.ui.presenter;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Order_Item__c;
import com.abinbev.dsa.model.Order__c;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants;

import java.util.List;

import rx.subscriptions.CompositeSubscription;

public class PedidoDetailPresenter implements Presenter<PedidoDetailPresenter.ViewModel> {

    String orderId;
    ViewModel viewModel;
    CompositeSubscription compositeSubscription;

    public interface ViewModel {
        void setOrder(Order__c order);
        void setLineItems(List<Order_Item__c> orderItemProducts);
    }

    public PedidoDetailPresenter(String orderId) {
        this.orderId = orderId;
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void updateStatus(String status) {
        Order__c order = Order__c.getOrderById(orderId);
        order.setStatus(status);
        order.setSource(ABInBevApp.getAppContext().getResources().getString(R.string.order_source_sfdc));
        Order__c.updateOrder(order.getId(), order.toJson());
        String objectType = AbInBevConstants.AbInBevObjects.PEDIDO;
        String[] fields = {AbInBevConstants.PedidoFields.STATUS};
        TranslatableSFBaseObject.addTranslations(order, objectType, fields);
        viewModel.setOrder(order);
    }

    public void removeOrderItem(Order_Item__c orderItem) {
        Order_Item__c.deleteOrderItem(orderItem);
//        fetchOrderItems();
    }

    @Override
    public void start() {
        fetchOrder();
        fetchOrderItems();
    }

    @Override
    public void stop() {

    }

    private void fetchOrder() {
        Order__c order = Order__c.getOrderById(orderId);
        String objectType = AbInBevConstants.AbInBevObjects.PEDIDO;
        String[] fields = {AbInBevConstants.PedidoFields.STATUS};
        TranslatableSFBaseObject.addTranslations(order, objectType, fields);
        viewModel.setOrder(order);
    }

    private void fetchOrderItems() {
        viewModel.setLineItems(Order_Item__c.getAllOrderLineItemsByOrderId(orderId));
    }
}
