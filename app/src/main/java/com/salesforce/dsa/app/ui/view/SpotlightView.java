package com.salesforce.dsa.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.salesforce.dsa.app.R;
import com.salesforce.dsa.app.presenter.SpotlightPresenter;
import com.salesforce.dsa.app.ui.adapter.SpotlightAdapter;
import com.salesforce.dsa.app.ui.clickListeners.ContentVersionClickListener;
import com.salesforce.dsa.data.model.ContentVersion;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

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
public class SpotlightView extends LinearLayout implements SpotlightPresenter.View {

    @Bind(R.id.spotlight_list)
    ListView spotlightListView;

    private SpotlightAdapter spotlightAdapter;

    public SpotlightView(Context context) {
        this(context, null);
    }

    public SpotlightView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpotlightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SpotlightView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        spotlightAdapter = new SpotlightAdapter();
        spotlightListView.setAdapter(spotlightAdapter);
        spotlightListView.setOnItemClickListener(new ContentVersionClickListener());
    }

    @Override
    public void setFeaturedContent(List<ContentVersion> featuredContent) {
        spotlightAdapter.setFeaturedContent(featuredContent);
    }

    @Override
    public void setRecentlyUpdatedContent(List<ContentVersion> recentContent) {
        spotlightAdapter.setRecentlyUpdatedContent(recentContent);
    }

    @Override
    public void filter(int selectedFilterId) {
        switch (selectedFilterId) {
            case R.id.spotlight_filter_all:
                spotlightAdapter.filter(SpotlightAdapter.SpotlightFilter.ALL);
                break;
            case R.id.spotlight_filter_featured:
                spotlightAdapter.filter(SpotlightAdapter.SpotlightFilter.FEATURED);
                break;
            case R.id.spotlight_filter_new_and_updated:
                spotlightAdapter.filter(SpotlightAdapter.SpotlightFilter.NEW_AND_UPDATED);
                break;
        }
    }
}
