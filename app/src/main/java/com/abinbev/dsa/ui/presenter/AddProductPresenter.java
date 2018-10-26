package com.abinbev.dsa.ui.presenter;

import static com.abinbev.dsa.activity.AddProductActivity.NEW_ORDER;

import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.activity.AddProductActivity;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Order_Item__c;
import com.abinbev.dsa.model.Order__c;
import com.abinbev.dsa.model.Product;
import com.abinbev.dsa.utils.AppScheduler;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.json.JSONObject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

/**
 * Responsible for the {@link AddProductActivity}.
 */
public class AddProductPresenter implements Presenter<AddProductPresenter.ViewModel> {

    private static final String TAG = AddProductPresenter.class.getSimpleName();

    public interface ViewModel {

        void setAllOrderItems(List<Order_Item__c> orderItems);

        void productAdded(int totalCount);

        void orderCreated(String orderId);
    }

    @Inject
    Bus eventBus;

    ViewModel viewModel;
    CompositeSubscription compositeSubscription;
    Subscription productSubscription;
    Subscription orderSubscription;
    String orderTypeId;
    String orderId;
    String accountId;
    Order__c order;
    List<Order_Item__c> orderItems;
    int preExistingOrderItems;
    boolean isFirstStart = true;

    public AddProductPresenter(String orderTypeId, String orderId, String accountId) {
        super();
        this.orderTypeId = orderTypeId;
        this.orderId = orderId;
        this.accountId = accountId;
        orderItems = new ArrayList<>();
        compositeSubscription = new CompositeSubscription();
        productSubscription = Subscriptions.empty();
        orderSubscription = Subscriptions.empty();
        ((ABInBevApp) ABInBevApp.getAppContext()).getAppComponent().inject(this);
    }

    private Subscriber<List<Product>> createProductSubscriber() {
        return new Subscriber<List<Product>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Error fetching products: ", e);
            }

            @Override
            public void onNext(List<Product> products) {
                List<Order_Item__c> orderItems = new ArrayList<>();
                for (Product p : products) {
                    if (!p.competitorFlag()) {
                        Order_Item__c orderItem = new Order_Item__c(new JSONObject());
                        orderItem.setProduct(p.getId());
                        orderItems.add(orderItem);
                    } else {
                        continue;
                    }
                }
                viewModel.setAllOrderItems(orderItems);
            }
        };
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        eventBus.register(this);
        if (isFirstStart) {
            loadData();
            isFirstStart = false;
        }
    }

    private void loadData() {
        compositeSubscription.add(getAvailableProducts());
        compositeSubscription.add(getOrderItems()
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Order_Item__c>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error fetching order Items: ", e);
                    }

                    @Override
                    public void onNext(List<Order_Item__c> orderItems) {
                        preExistingOrderItems = orderItems.size();
                        viewModel.productAdded(preExistingOrderItems);
                    }
                }));
    }

    /**
     * Fetches the products not currently on an order
     *
     * @return
     */
    private Subscription getAvailableProducts() {
        return Observable.zip(getProductsByChannelAndTerritory(), getOrderItems(), new Func2<List<Product>, List<Order_Item__c>, List<Product>>() {
            @Override
            public List<Product> call(List<Product> products, List<Order_Item__c> orderItems) {
                if (!orderItems.isEmpty()) {

                    //get list of product ids to filter out
                    List<String> productIds = new ArrayList<>();
                    for (Order_Item__c orderItem : orderItems) {
                        productIds.add(orderItem.getProductId());
                    }


                    //filter out any products already on orderItems;
                    ArrayList<Product> filteredProducts = new ArrayList<>();
                    for (Product p : products) {
                        if (!productIds.contains(p.getId())) {
                            filteredProducts.add(p);
                        }
                    }
                    products = filteredProducts;
                }
                return products;
            }
        })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(createProductSubscriber());

    }

    private Observable<List<Product>> getProductsByChannel() {
        Account account = Account.getById(accountId);
        String accountChannel = account != null ? account.getChannel() : "";
        return Observable.just(Product.getProductsByChannel(accountChannel));
    }

    private Observable<List<Product>> getProductsByChannelAndTerritory() {
        Account account = Account.getById(accountId);
        String accountChannel = account != null ? account.getChannel() : "";
        String accountRegion = account != null ? account.getCityRegion() : "";
        return Observable.just(Product.getProductsByChannelAndTerritory(accountChannel, accountRegion));
    }

    private Observable<List<Order_Item__c>> getOrderItems() {
        return Observable.just(Order_Item__c.getAllOrderLineItemsByOrderId(this.orderId));
    }

    @Override
    public void stop() {
        viewModel = null;
        compositeSubscription.clear();
        productSubscription.unsubscribe();
        orderSubscription.unsubscribe();
        eventBus.unregister(this);
    }

    public void addOrderItem(Order_Item__c orderItem) {
        orderItems.add(orderItem);
        viewModel.productAdded(preExistingOrderItems + orderItems.size());
    }

    public boolean canCreate() {
        return !NEW_ORDER.equals(orderId) || !orderItems.isEmpty();
    }

    public void createOrder(String source) {
        Observable<Order__c> orderObservable;
        if (NEW_ORDER.equalsIgnoreCase(orderId)) {
            orderObservable = Observable.just(Order__c.createOrder(accountId, orderTypeId, source));
        } else {
            orderObservable = Observable.create(new Observable.OnSubscribe<Order__c>() {
                @Override
                public void call(Subscriber<? super Order__c> subscriber) {
                    Order__c order = Order__c.getOrderById(orderId);
                    subscriber.onNext(order);
                    subscriber.onCompleted();
                }
            });
        }

        orderSubscription = orderObservable
                .flatMap(new Func1<Order__c, Observable<Order__c>>() {
                    @Override
                    public Observable<Order__c> call(Order__c order__c) {
                        if (order__c != null && orderItems.size() > 0) {
                            String orderId = order__c.getId();
                            for (Order_Item__c orderItem : orderItems) {
                                orderItem.setOrder(orderId);
                                Order_Item__c.saveOrderItem(orderItem);
                            }
                        }
                        return Observable.just(order__c);
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
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onNext(Order__c order__c) {
                        viewModel.orderCreated(order__c.getId());
                    }
                });
    }

}
