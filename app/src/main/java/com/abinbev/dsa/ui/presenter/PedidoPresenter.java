package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.abinbev.dsa.activity.AddProductActivity;
import com.abinbev.dsa.activity.PedidoDetailActivity;
import com.abinbev.dsa.model.OnTapSettings__c;
import com.abinbev.dsa.model.Order__c;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import rx.Observable;
import rx.Single;

public class PedidoPresenter extends AbstractRxPresenter<PedidoPresenter.ViewModel> implements Presenter<PedidoPresenter.ViewModel> {

    public static final String TAG = PedidoPresenter.class.getSimpleName();

    public interface ViewModel {
        void setData(List<Order__c> pedidos);
        void goToCreateNewOrder(RecordType recordType);
    }

    private ViewModel viewModel;
    private String accountId;
    private int timeFrameMonth = 1;
    private int timeFrameDay = 0;
    private Date showOrdersSince;

    public PedidoPresenter(String accountId) {
        super();
        this.accountId = accountId;
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        super.start();
        getDefaultTimeFrame();
        Calendar now = Calendar.getInstance();
        if (timeFrameDay != 0) {
            now.add(Calendar.DAY_OF_MONTH, -timeFrameDay);
        } else  {
            if (timeFrameMonth == 0)
                timeFrameMonth = 1;
            now.add(Calendar.MONTH, -timeFrameMonth);
        }
        showOrdersSince = now.getTime();
        getOpenOrders();
    }

    private void getDefaultTimeFrame() {
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


    private void getOpenOrders() {
        addSubscription(Observable.fromCallable(
                () -> {
                    List<Order__c> orders = Order__c.getOpenOrdersByAccountIdSince(accountId, showOrdersSince);
                    String objectType = AbInBevConstants.AbInBevObjects.PEDIDO;
                    String[] fields = {AbInBevConstants.PedidoFields.STATUS};
                    TranslatableSFBaseObject.addTranslations(orders, objectType, fields);
                    TranslatableSFBaseObject.addRecordTypeTranslations(orders, objectType, Order__c.FIELD_RECORD_TYPE_NAME);

                    return orders;
                    })
                .subscribe(
                        orders -> {
                            viewModel.setData(orders);
                        },
                        error -> Log.e(TAG, "Error on getting open orders: ", error)
                ));
    }

    @Override
    public void stop() {
        super.stop();
        viewModel = null;
    }

    public Date getShowOrdersSince() {
        return showOrdersSince;
    }

    public void getNewOrderRecordType(final String defaultRecordTypeId) {
        addSubscription(Single.fromCallable(
                () -> RecordType.getById(defaultRecordTypeId))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        rt -> {
                            viewModel.goToCreateNewOrder(rt);
                            },
                        error -> Log.e(TAG, "Error: ", error)
                ));
    }


    public void goToDetailView(Context context, String pedidoId, String accountId) {
        Intent intent = new Intent(context, PedidoDetailActivity.class);
        intent.putExtra(AddProductActivity.EXTRA_ORDER_ID, pedidoId);
        intent.putExtra(AddProductActivity.EXTRA_ACCOUNT_ID, accountId);
        context.startActivity(intent);
    }


}
