package com.abinbev.dsa.ui.presenter;

import android.support.annotation.CallSuper;

/**
 * Created by mewa on 6/26/17.
 */

public abstract class AbstractPresenter<T> implements Presenter<T> {
    private T viewModel;

    @CallSuper
    @Override
    public void setViewModel(T viewModel) {
        this.viewModel = viewModel;
    }

    protected final T viewModel() {
        return viewModel;
    }

    @CallSuper
    @Override
    public void stop() {
        viewModel = null;
    }
}
