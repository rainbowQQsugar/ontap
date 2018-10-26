package com.salesforce.dsa.app.presenter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.salesforce.dsa.data.model.ContentVersion;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

/**
 * Copyright 2015 AKTA a SalesForce Company
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class SpotlightPresenter implements Presenter<SpotlightPresenter.View> {

    private static final String TAG = "SpotlightPresenter";

    public interface View {
        void setFeaturedContent(List<ContentVersion> featuredContent);

        void setRecentlyUpdatedContent(List<ContentVersion> recentContent);

        void filter(int selectedFilterId);
    }

    private class ContentVersionWithType {

        public static final int TYPE_FEATURE = 0;
        public static final int TYPE_RECENTLY_UPDATED = 1;

        private final int type;
        private final List<ContentVersion> contentVersions;

        public ContentVersionWithType(int type, List<ContentVersion> contentVersions) {
            super();
            this.type = type;
            this.contentVersions = contentVersions;
        }
    }

    private View view;
    private Context context;
    private int selectedFilterId;
    private Subscription subscription;

    private ContentVersionWithType featuredItems;
    private ContentVersionWithType recentItems;
    private boolean isLoaded;

    public SpotlightPresenter(int selectedFilterId) {
        super();
        this.selectedFilterId = selectedFilterId;
        subscription = Subscriptions.empty();
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void start() {
        if (isLoaded) {
            view.setFeaturedContent(featuredItems.contentVersions);
            view.setRecentlyUpdatedContent(recentItems.contentVersions);
            setSelectedFilterId(selectedFilterId);
        } else {
            subscription.unsubscribe();
            //TODO we should be propagating the state loading to the view
            //TODO in order to test this properly we need to inject a Scheduler that runs on a single thread
            subscription = Observable.create(new Observable.OnSubscribe<ContentVersionWithType>() {
                @Override
                public void call(Subscriber<? super ContentVersionWithType> subscriber) {
                    subscriber.onNext(new ContentVersionWithType(ContentVersionWithType.TYPE_FEATURE, ContentVersion.fetchFeaturedContent()));
                    subscriber.onNext(new ContentVersionWithType(ContentVersionWithType.TYPE_RECENTLY_UPDATED, ContentVersion.fetchRecentContent(context)));
                    subscriber.onCompleted();
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(getSubscriber());
        }
    }

    @NonNull
    private Subscriber<ContentVersionWithType> getSubscriber() {
        return new Subscriber<ContentVersionWithType>() {
            @Override
            public void onCompleted() {
                isLoaded = true;
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Error loading content", e);
                recentItems = null;
                featuredItems = null;
                isLoaded = false;
                //TODO this should propagate to the view as well
            }

            @Override
            public void onNext(ContentVersionWithType contentVersionWithType) {
                if (contentVersionWithType.type == ContentVersionWithType.TYPE_FEATURE) {
                    featuredItems = contentVersionWithType;
                    view.setFeaturedContent(contentVersionWithType.contentVersions);
                } else if (contentVersionWithType.type == ContentVersionWithType.TYPE_RECENTLY_UPDATED) {
                    recentItems = contentVersionWithType;
                    view.setRecentlyUpdatedContent(contentVersionWithType.contentVersions);
                }
            }
        };
    }

    @Override
    public void stop() {
        subscription.unsubscribe();
        view = null;
        context = null;
    }

    public void setSelectedFilterId(int selectedFilterId) {
        this.selectedFilterId = selectedFilterId;
        view.filter(this.selectedFilterId);
    }

    public int getSelectedFilterId() {
        return selectedFilterId;
    }
}
