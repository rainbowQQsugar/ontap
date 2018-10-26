package com.abinbev.dsa.ui.presenter;

import rx.subscriptions.CompositeSubscription;

/**
 * @author Jason Harris (jason@akta.com)
 */
public class AssetCaseEditPresenter implements Presenter<AssetCaseEditPresenter.ViewModel>{
    public interface ViewModel extends CasoViewPresenter.ViewModel {
        void onChildCasesCreated();
    }

    private static final String TAG = AssetCaseEditPresenter.class.getSimpleName();
    private ViewModel viewModel;
    protected String accountId;
    private CompositeSubscription compositeSubscription;


    public AssetCaseEditPresenter(String accountId) {
        super();
        this.accountId = accountId;
        compositeSubscription = new CompositeSubscription();
    }


    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        compositeSubscription.clear();
    }

    @Override
    public void stop() {
        compositeSubscription.clear();
        viewModel = null;
    }
}
