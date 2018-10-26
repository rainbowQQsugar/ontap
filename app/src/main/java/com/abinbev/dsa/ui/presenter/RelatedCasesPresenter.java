package com.abinbev.dsa.ui.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.model.Account_Asset__c;
import com.abinbev.dsa.model.Caso;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * @author Jason Harris (jason@akta.com)
 */
public class RelatedCasesPresenter implements Presenter<RelatedCasesPresenter.ViewModel>  {

    public static final String TAG = RelatedCasesPresenter.class.getSimpleName();

    public interface ViewModel {
        void setRelatedCases(List<Caso> relatedCases);
        void setAssets(Map<String, Account_Asset__c> assets);
        void setAssetRecordTypes(Map<String, RecordType> recordTypes);
    }
    private ViewModel viewModel;
    private Subscription getRelatedSubscription;
    private String caseId;

    public RelatedCasesPresenter() {
        getRelatedSubscription = Subscriptions.empty();
    }

    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    @Override
    public void start() {
        if(caseId != null) {
            getRelatedCases();
        }
    }

    public void getRelatedCases() {
        if (TextUtils.isEmpty(caseId)) {
            return;
        }

        getRelatedSubscription.unsubscribe();
        getRelatedSubscription = Observable.just(Caso.getRelatedCases(caseId))
                                        .subscribeOn(AppScheduler.background())
                                        .observeOn(AppScheduler.main())
                                        .subscribe(new Subscriber<List<Caso>>() {
                                            @Override
                                            public void onCompleted() {

                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Log.e(TAG, "Error fetching case: ", e);
                                            }

                                            @Override
                                            public void onNext(List<Caso> casos) {
                                                viewModel.setRelatedCases(casos);
                                            }
                                        });
    }

    @Override
    public void stop() {
        getRelatedSubscription.unsubscribe();
        viewModel = null;
    }

    public void getAssets(final List<Caso> cases) {
        if (cases == null) {
            return;
        }
        getRelatedSubscription.unsubscribe();
        getRelatedSubscription = Observable.create(new Observable.OnSubscribe<Map<String, Account_Asset__c>>() {
            @Override
            public void call(Subscriber<? super Map<String, Account_Asset__c>> subscriber) {
                LinkedHashMap<String, Account_Asset__c> map = new LinkedHashMap<>();
                for (Caso caso : cases) {
                    Account_Asset__c asset = Account_Asset__c.getById(caso.getActivoPorClient());
                    map.put(caso.getActivoPorClient(), asset);
                }
                subscriber.onNext(map);
                subscriber.onCompleted();
            }
        })
           .subscribeOn(AppScheduler.background())
           .observeOn(AppScheduler.main())
           .subscribe(new Subscriber<Map<String, Account_Asset__c>>() {
               @Override
               public void onCompleted() {

               }

               @Override
               public void onError(Throwable e) {
                   Log.e(TAG, "Error fetching assets ", e);
               }

               @Override
               public void onNext(Map<String, Account_Asset__c> map) {
                   viewModel.setAssets(map);
                   ArrayList<Account_Asset__c> list = new ArrayList<Account_Asset__c>();
                   list.addAll(map.values());
                   getAssetRecordTypes(list);
               }
           });
    }

    public void getAssetRecordTypes(final List<Account_Asset__c> assets) {

        getRelatedSubscription.unsubscribe();
        getRelatedSubscription = Observable.create(new Observable.OnSubscribe<Map<String, RecordType>>() {
            @Override
            public void call(Subscriber<? super Map<String, RecordType>> subscriber) {
                LinkedHashMap<String, RecordType> map = new LinkedHashMap<>();
                for (Account_Asset__c asset : assets) {
                    RecordType recordType = RecordType.getById(asset.getRecordTypeId());
                    map.put(asset.getRecordTypeId(), recordType);
                }
                subscriber.onNext(map);
                subscriber.onCompleted();
            }
        })
           .subscribeOn(AppScheduler.background())
           .observeOn(AppScheduler.main())
           .subscribe(new Subscriber<Map<String, RecordType>>() {
               @Override
               public void onCompleted() {

               }

               @Override
               public void onError(Throwable e) {
                   Log.e(TAG, "Error fetching asset record types ", e);
               }

               @Override
               public void onNext(Map<String, RecordType> map) {
                   viewModel.setAssetRecordTypes(map);
               }
           });
    }

    public void saveQuantities(final Map<String, String> quantities) {

        getRelatedSubscription.unsubscribe();
        getRelatedSubscription = Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                for (Map.Entry<String, String> entry : quantities.entrySet()) {
                    Caso.updateQuantityRequired(entry.getKey(), entry.getValue());
                }
                subscriber.onNext(true);
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
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {

                    }
                });
    }
}
