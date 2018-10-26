package com.abinbev.dsa.di.module;

import android.app.Application;

import com.abinbev.dsa.bus.MainThreadBus;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by wandersonblough on 12/15/15.
 */
@Module
public class AppModule {

    Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Bus eventBus() {
        return new MainThreadBus("SabMiller");
    }
}
