package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.MarketProgram;
import com.abinbev.dsa.model.MarketProgramItem;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

public class MarketProgramDetailPresenter implements Presenter<MarketProgramDetailPresenter.ViewModel> {

    public static final String TAG = MarketProgramDetailPresenter.class.getSimpleName();

    String marketProgramId;
    boolean isTablet;
    ViewModel viewModel;
    CompositeSubscription compositeSubscription;

    public interface ViewModel {
        void setMarketProgram(MarketProgram marketProgram);

        void setLineItems(List<MarketProgramItem> marketProgramItems);

        void setLineItemWrapper(MarketProgramItemWrapper marketProgramItemWrapper);
    }

    public MarketProgramDetailPresenter(String marketProgramId, boolean isTablet) {
        this.marketProgramId = marketProgramId;
        this.isTablet = isTablet;
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        fetchMarketProgram();
        if (isTablet) {
            fetchMarketProgramItems();
        } else {
            fetchMarketProgramItemWrapper();
        }
    }

    @Override
    public void stop() {

    }

    private void fetchMarketProgram() {
        compositeSubscription.add(Observable.create(new Observable.OnSubscribe<MarketProgram>() {
            @Override
            public void call(Subscriber<? super MarketProgram> subscriber) {
                subscriber.onNext(MarketProgram.getById(marketProgramId));
                subscriber.onCompleted();
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<MarketProgram>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting market program: ", e);
                    }

                    @Override
                    public void onNext(MarketProgram marketProgram) {

                        if (viewModel != null) {
                            viewModel.setMarketProgram(marketProgram);
                        }
                    }
                }));
    }

    private void fetchMarketProgramItems() {
        compositeSubscription.add(Observable.create(new Observable.OnSubscribe<List<MarketProgramItem>>() {
            @Override
            public void call(Subscriber<? super List<MarketProgramItem>> subscriber) {
                subscriber.onNext(MarketProgramItem.getAllMarketProgramItemsByMarketProgramId(marketProgramId));
                subscriber.onCompleted();
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<MarketProgramItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting market program line items: ", e);
                    }

                    @Override
                    public void onNext(List<MarketProgramItem> marketProgramItems) {
                        if (viewModel != null) {
                            viewModel.setLineItems(marketProgramItems);
                        }
                    }
                }));
    }

    private void fetchMarketProgramItemWrapper() {
        compositeSubscription.add(Observable.create(new Observable.OnSubscribe<List<MarketProgramItem>>() {
            @Override
            public void call(Subscriber<? super List<MarketProgramItem>> subscriber) {
                subscriber.onNext(MarketProgramItem.getAllMarketProgramItemsByMarketProgramId(marketProgramId));
                subscriber.onCompleted();
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<MarketProgramItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting market program line items: ", e);
                    }

                    @Override
                    public void onNext(List<MarketProgramItem> marketProgramItems) {
                        MarketProgramItemWrapper marketProgramItemWrapper = new MarketProgramItemWrapper();
                        for(MarketProgramItem marketProgramItem : marketProgramItems) {
                            String recordType = marketProgramItem.getRecordType();
                            if (MarketProgramItem.LOAN_TYPE.equalsIgnoreCase(recordType)) {
                                //loan types get added as is
                                marketProgramItemWrapper.onLoanMaterials.add(marketProgramItem);
                            } else if (MarketProgramItem.SALES_ACTUALS_TYPE.equalsIgnoreCase(recordType)) {
                                //we need to group sales actual items together based on item type
                                //if one already exists, just need to add either missing month / quarter value
                                if (marketProgramItemWrapper.salesActuals.containsKey(marketProgramItem.getType())) {
                                    MarketProgramSalesActuals marketProgramSalesActuals = marketProgramItemWrapper.salesActuals.get(marketProgramItem.getType());
                                    if (MarketProgramItem.PERIOD_MONTH.equalsIgnoreCase(marketProgramItem.getPeriod())) {
                                        marketProgramSalesActuals.monthValue = marketProgramItem.getValue();
                                    } else if (MarketProgramItem.PERIOD_QUARTER.equalsIgnoreCase(marketProgramItem.getPeriod())) {
                                        marketProgramSalesActuals.quarterValue = marketProgramItem.getValue();
                                    }
                                } else { //does not exist, need to add new entry
                                    MarketProgramSalesActuals marketProgramSalesActuals = new MarketProgramSalesActuals();
                                    marketProgramSalesActuals.itemType = marketProgramItem.getType();
                                    if (MarketProgramItem.PERIOD_MONTH.equalsIgnoreCase(marketProgramItem.getPeriod())) {
                                        marketProgramSalesActuals.monthValue = marketProgramItem.getValue();
                                    } else if (MarketProgramItem.PERIOD_QUARTER.equalsIgnoreCase(marketProgramItem.getPeriod())) {
                                        marketProgramSalesActuals.quarterValue = marketProgramItem.getValue();
                                    }
                                    marketProgramItemWrapper.salesActuals.put(marketProgramSalesActuals.itemType, marketProgramSalesActuals);
                                }
                            }
                        }

                        //may need to make another pass through itemWrapper to find 'edge case' records.

                        if (viewModel != null) {
                            viewModel.setLineItemWrapper(marketProgramItemWrapper);
                        }
                    }
                }));

    }


    public class MarketProgramItemWrapper {
        public List<MarketProgramItem> onLoanMaterials = new ArrayList<>();
        public Map<String, MarketProgramSalesActuals> salesActuals = new HashMap<>();
    }

    public class MarketProgramSalesActuals {
        public String itemType;
        public String monthValue;
        public String quarterValue;
    }

}
