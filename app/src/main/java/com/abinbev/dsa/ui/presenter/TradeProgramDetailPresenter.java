package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.TradeProgramItem;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Single;

public class TradeProgramDetailPresenter extends AbstractRxPresenter<TradeProgramDetailPresenter.ViewModel> {

    public static final String TAG = TradeProgramDetailPresenter.class.getSimpleName();

    private String marketProgramId;

    public TradeProgramDetailPresenter(String marketProgramId) {
        this.marketProgramId = marketProgramId;
    }

    @Override
    public void start() {
        super.start();
        getTradeProgramItems();
    }

    private void getTradeProgramItems() {
        addSubscription(
                Single.fromCallable(() -> TradeProgramItem.getByMarketProgram(marketProgramId))
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                marketProgramItems -> viewModel().updateAdapter(marketProgramItems),
                                error -> Log.e(TAG, "Error getting market program line items: ", error)
                        ));
    }

    public interface ViewModel {
        void updateAdapter(List<TradeProgramItem> tradeProgramItems);
    }
}
