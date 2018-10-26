package com.abinbev.dsa.ui.presenter;

import android.support.annotation.CallSuper;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by mewa on 6/26/17.
 */

public abstract class AbstractRxPresenter<T> extends AbstractPresenter<T> {

    final protected CompositeSubscription subscriptions = new CompositeSubscription();

    @CallSuper
    @Override
    public void start() {
    }

    @CallSuper
    @Override
    public void stop() {
        clearSubscriptions();
        super.stop();
    }

    protected void addSubscription(Subscription s) {
        subscriptions.add(s);
    }

    protected void clearSubscriptions() {
        subscriptions.clear();
    }
}
