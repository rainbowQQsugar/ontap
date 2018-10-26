package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.OnTapSettings__c;
import com.abinbev.dsa.model.OrderData;
import com.abinbev.dsa.model.Order_Item__c;
import com.abinbev.dsa.model.Order__c;
import com.abinbev.dsa.model.SalesVolume;
import com.abinbev.dsa.model.SalesVolumeData;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PedidoListPresenter extends AbstractRxPresenter<PedidoListPresenter.ViewModel> implements Presenter<PedidoListPresenter.ViewModel> {

    public static final String TAG = PedidoListPresenter.class.getSimpleName();
    private boolean isFetchAllPocOrders;
    private int timeFrameDay;
    private int timeFrameMonth =1;
    private Date showOrdersSince;

    public interface ViewModel {
        void setOrdersData(OrderData ordersData);

        void setSalesVolumeData(SalesVolumeData salesVolumeData);
    }

    private String accountId;
    private List<Order__c> allOrdersByAccount = new ArrayList<Order__c>();

    public PedidoListPresenter(String accountId) {
        super();
        this.accountId = accountId;
    }

    public PedidoListPresenter(boolean isFetchAllPocOrders) {
        this.isFetchAllPocOrders = isFetchAllPocOrders;
    }

    @Override
    public void start() {
        super.start();
        getDefaultTimeFrame();
        this.allOrdersByAccount.clear();
        loadAllOrders();
        loadAllSalesVolumes();

    }


    public void getDefaultTimeFrame() {
        addSubscription(Observable.fromCallable(
                () -> {
                    OnTapSettings__c settings = OnTapSettings__c.getCurrentSettings();
                    return settings;
                })
                .subscribe(
                        settings -> {
                            if (settings.getOrderTimeFrameDays()) {
                                this.timeFrameDay = settings.getOrderTimeFrameValue();
                            } else {
                                this.timeFrameMonth = settings.getOrderTimeFrameValue();
                            }
                        },
                        error -> Log.e(TAG, "Error on getting open orders: ", error)
                ));
    }

    public Date getShowOrdersSince() {
        Calendar now = Calendar.getInstance();
        if (timeFrameDay != 0) {
            now.add(Calendar.DAY_OF_MONTH, -timeFrameDay);
        } else  {
            if (timeFrameMonth == 0)
                timeFrameMonth = 1;
            now.add(Calendar.MONTH, -timeFrameMonth);
        }
        return now.getTime();
    }


    private void loadAllSalesVolumes() {

        addSubscription(Observable.fromCallable(
                () -> {
                    SortedSet<String> uniqueOrderBrand = new TreeSet<>();
                    SortedSet<String> uniqueOrderSku = new TreeSet<>();
                    SortedSet<String> uniqueOrderSource = new TreeSet<>();
                    String allValues = ABInBevApp.getAppContext().getString(R.string.all_values);
                    uniqueOrderBrand.add(allValues);
                    uniqueOrderSku.add(allValues);
                    uniqueOrderSource.add(allValues);

                    List<SalesVolume> svs = new ArrayList<SalesVolume>();
                    for (Order__c o : this.allOrdersByAccount) {
                        List<Order_Item__c> items = Order_Item__c.getAllOrderLineItemsByOrderId(o.getId());
                        for (Order_Item__c i : items) {
                            String ds = i.getOrderStartDate();
                            String de = i.getOrderEndDate();
                            SalesVolume s = new SalesVolume(i.getProduct().getChBrand(),
                                    i.getProduct().getProductName(),
                                    Integer.toString(i.getQuantity()),
                                    ds, de);
                            s.setOrderSource(o.getSource());
                            s.setOrderStatus(o.getStatus());

                            svs.add(s);
                            if (!i.getProduct().getChBrand().isEmpty()) {
                                uniqueOrderBrand.add(i.getProduct().getChBrand());

                            }
                            if (!i.getProduct().getProductName().isEmpty()) {
                                uniqueOrderSku.add(i.getProductDesc());
                            }

                        }
                        if (!o.getSource().isEmpty()) {
                            uniqueOrderSource.add(o.getSource());
                        }
                    }
                    SalesVolumeData salesVolumeData = new SalesVolumeData(svs, uniqueOrderBrand, uniqueOrderSku, uniqueOrderSource);

                    return salesVolumeData;
                })
                .subscribe(
                        salesVolumeData -> {
                            viewModel().setSalesVolumeData(salesVolumeData);
                        },

                        error -> Log.e(TAG, "Error getting all orders: ", error)
                ));
    }

    private void loadAllOrders() {

        addSubscription(Observable.fromCallable(
                () -> {
                    List<Order__c> orders;
                    if (isFetchAllPocOrders) orders = Order__c.getAllAccountOrders();
                    else orders = Order__c.getAllOrdersByAccountId(accountId);
                    String objectType = AbInBevConstants.AbInBevObjects.PEDIDO;
                    String[] fields = {AbInBevConstants.PedidoFields.STATUS};
                    TranslatableSFBaseObject.addTranslations(orders, objectType, fields);
                    TranslatableSFBaseObject.addRecordTypeTranslations(orders, objectType, Order__c.FIELD_RECORD_TYPE_NAME);

                    SortedSet<String> uniqueOrderBrand = new TreeSet<>();
                    SortedSet<String> uniqueOrderSku = new TreeSet<>();
                    SortedSet<String> uniqueOrderSource = new TreeSet<>();
                    String allValues = ABInBevApp.getAppContext().getString(R.string.all_values);
                    uniqueOrderBrand.add(allValues);
                    uniqueOrderSku.add(allValues);
                    uniqueOrderSource.add(allValues);


                    for (Order__c o : orders) {
                        List<Order_Item__c> items = Order_Item__c.getAllOrderLineItemsByOrderId(o.getId());
                        for (Order_Item__c i : items) {

                            if (!i.getProduct().getChBrand().isEmpty()) {
                                uniqueOrderBrand.add(i.getBrand());

                            }
                            if (!i.getProduct().getProductName().isEmpty()) {
                                uniqueOrderSku.add(i.getProductDesc());
                            }

                        }
                        if (!o.getSource().isEmpty()) {
                            uniqueOrderSource.add(o.getSource());
                        }
                    }
                    OrderData ordersData = new OrderData(orders, uniqueOrderBrand, uniqueOrderSku, uniqueOrderSource);

                    return ordersData;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ordersData -> {
                            this.allOrdersByAccount.addAll(ordersData.getOrdersList());
                            viewModel().setOrdersData(ordersData);
                        },
                        error -> Log.e(TAG, "Error getting all orders: ", error)
                ));
    }


    @Override
    public void stop() {
        super.stop();
    }

}
