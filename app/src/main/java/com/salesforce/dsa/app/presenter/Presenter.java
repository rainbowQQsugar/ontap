package com.salesforce.dsa.app.presenter;

/**
 * Created by zsiegel on 11/2/15.
 */
public interface Presenter<T> {

    void setView(T view);

    void start();

    void stop();
}
