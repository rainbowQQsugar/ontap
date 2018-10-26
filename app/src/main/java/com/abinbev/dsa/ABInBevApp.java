package com.abinbev.dsa;

import android.app.Application;
import android.content.Context;

import com.abinbev.dsa.activity.UserDetailsActivity;
import com.abinbev.dsa.di.component.AppComponent;
import com.abinbev.dsa.di.component.DaggerAppComponent;
import com.abinbev.dsa.di.module.AppModule;
import com.abinbev.dsa.syncmanifest.JexlManifestProcessorFactory;
import com.abinbev.dsa.utils.crashreport.CrashReportManager;
import com.abinbev.dsa.utils.crashreport.CrashReportManagerProvider;
import com.abinbev.dsa.utils.okhttp.AuthInterceptor;
import com.abinbev.dsa.utils.picasso.AttachmentRequestHandler;
import com.abinbev.dsa.utils.picasso.PicassoUtils;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.smartstore.CrashReportLogger;
import com.salesforce.androidsdk.smartstore.app.SmartStoreSDKManager;
import com.salesforce.androidsyncengine.syncmanifest.ManifestProcessorFactory;
import com.salesforce.dsa.KeyImpl;
import com.salesforce.dsa.utils.DisplayUtils;
import com.squareup.picasso.Picasso;

import java.io.File;

import okhttp3.OkHttpClient;

/**
 * Copyright 2015 AKTA a SalesForce Company
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ABInBevApp extends Application {

    private static ABInBevApp app;
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        // LeakCanary.install(this);
        setupCrashReportTools();

        app = (ABInBevApp) getApplicationContext();
        SmartStoreSDKManager.initNative(app, new KeyImpl(), UserDetailsActivity.class);
        DisplayUtils.updateDeviceDisplayValues(getAppContext());

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        setupPicasso();

        // Set custom manifest processor.
        ManifestProcessorFactory.setInstance(new JexlManifestProcessorFactory());
    }

    private void setupCrashReportTools() {
        CrashReportManager crManager = CrashReportManagerProvider.getInstance();
        crManager.init(this);

        // Separate class created in Salesforce SDK just to log logout errors.
        CrashReportLogger.setInstance(new CrashReportLogger() {
            @Override
            public void log(String msg) {
                CrashReportManager crManager = CrashReportManagerProvider.getInstance();
                crManager.log(msg);
            }
        });

    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    public ClientManager createClientManager() {
        return new ClientManager(this,
                SalesforceSDKManager.getInstance().getAccountType(),
                SalesforceSDKManager.getInstance().getLoginOptions(),
                SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());
    }

    public OkHttpClient createOkHttpClient(ClientManager clientManager) {
        return  new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(clientManager))
                .build();
    }

    public static Context getAppContext() {
        return app;
    }

    /** Setup default picasso implementation. */
    private void setupPicasso() {
        final ClientManager clientManager = createClientManager();

        File cacheDir = new File(getFilesDir(), "picasso_cache");
        long cacheSize = PicassoUtils.calculateDiskCacheSize(cacheDir);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(clientManager))
                .cache(new okhttp3.Cache(cacheDir, cacheSize))
                .build();

        Picasso.setSingletonInstance(
                new Picasso.Builder(this)
                        .addRequestHandler(new AttachmentRequestHandler(okHttpClient, clientManager))
                        .build());
    }
}
