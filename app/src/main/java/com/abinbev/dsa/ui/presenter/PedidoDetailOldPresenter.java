package com.abinbev.dsa.ui.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.model.Order__c;
import com.abinbev.dsa.utils.AppScheduler;

import org.json.JSONObject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

/**
 * @author Jason Harris (jason@akta.com)
 */
public class PedidoDetailOldPresenter implements Presenter<PedidoDetailOldPresenter.ViewModel> {

    public static final String TAG = PedidoDetailOldPresenter.class.getSimpleName();

    public interface ViewModel {
        void setOrder(Order__c order);
        void updateSuccess(boolean success);
    }

    private ViewModel viewModel;
    private CompositeSubscription composite;
    private Subscription getOrderSubscription;
    private Subscription updateOrderSubscription;
    private String orderId;

    public PedidoDetailOldPresenter() {
        super();
        getOrderSubscription = Subscriptions.empty();
        updateOrderSubscription = Subscriptions.empty();
        composite = new CompositeSubscription();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public void start() {
        getOrder();
    }

    public void getOrder() {
        if (TextUtils.isEmpty(orderId)) {
            return;
        }
        composite.remove(getOrderSubscription);
        getOrderSubscription = Observable.create(new Observable.OnSubscribe<Order__c>() {
            @Override
            public void call(Subscriber<? super Order__c> subscriber) {
                Order__c order = Order__c.getOrderById(orderId);
                subscriber.onNext(order);
                subscriber.onCompleted();
            }
        })
                                         .subscribeOn(AppScheduler.background())
                                         .observeOn(AppScheduler.main())
                                         .subscribe(new Subscriber<Order__c>() {
                                             @Override
                                             public void onCompleted() {

                                             }

                                             @Override
                                             public void onError(Throwable e) {
                                                 Log.e(TAG, "Error getting order: ", e);
                                             }

                                             @Override
                                             public void onNext(Order__c order) {
                                                 viewModel.setOrder(order);
                                             }
                                         });
        composite.add(getOrderSubscription);
    }

    public void updateOrder(final JSONObject json) {
        if (TextUtils.isEmpty(orderId)) {
            return;
        }
        composite.remove(updateOrderSubscription);
        updateOrderSubscription = Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                boolean success = Order__c.updateOrder(orderId, json);
                subscriber.onNext(success);
                subscriber.onCompleted();
            }
        })
                                         .subscribeOn(AppScheduler.background())
                                         .observeOn(AppScheduler.main())
                                         .subscribe(new Subscriber<Boolean>() {
                                             @Override
                                             public void onCompleted() {

                                             }

                                             @Override
                                             public void onError(Throwable e) {
                                                 Log.e(TAG, "Error updating order: ", e);
                                             }

                                             @Override
                                             public void onNext(Boolean success) {
                                                 viewModel.updateSuccess(success);
                                             }
                                         });
        composite.add(updateOrderSubscription);
    }

    @Override
    public void stop() {
        composite.clear();
        viewModel = null;
    }
}
