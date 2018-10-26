package com.abinbev.dsa.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.abinbev.dsa.R;
import com.abinbev.dsa.fragments.AMapFragment;
import com.abinbev.dsa.fragments.EventListFragment;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.ui.view.CollapsedAccountInfo;
import com.abinbev.dsa.ui.view.LegendView;
import com.abinbev.dsa.ui.view.SlidingPane;
import com.salesforce.dsa.BuildConfig;

import java.util.List;

import butterknife.Bind;

/**
 * Created by wandersonblough on 11/10/15.
 */
public class VisitPlanActivity extends AppBaseDrawerActivity implements EventListFragment.Callbacks, AMapFragment.MapListener, CollapsedAccountInfo.AccountInfoCallback {

    public static final String TAG = VisitPlanActivity.class.getSimpleName();

    public static final int REQUEST_CODE_OPEN_ACCOUNT = 1;

    @Nullable
    @Bind(R.id.visit_list)
    SlidingPane slidingPane;

    @Bind(R.id.legend)
    LegendView legend;

    AMapFragment mapFragment;
    EventListFragment eventFragment;
    boolean multiPane;
    boolean showCheckOutWarning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapFragment = (AMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);

        if (mapFragment != null) {
            /*
            if  (!LocationUtils.isGooglePlayServicesDevice(this)) {
                mapFragment = null;
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) slidingPane.getLayoutParams();
                layoutParams.topMargin = 0;
                if (slidingPane != null) slidingPane.setLayoutParams(layoutParams);
            } */
        }
        if (slidingPane != null) {
            multiPane = true;

            eventFragment = new EventListFragment();
//            eventFragment.setActivateOnItemClick(true);
            getSupportFragmentManager().beginTransaction()
                    .add(slidingPane.getContentId(), eventFragment)
                    .commit();
        } else {
            eventFragment = (EventListFragment) getSupportFragmentManager().findFragmentById(R.id.event_list);
//            eventFragment.setActivateOnItemClick(true);
        }
        slidingPane.setDrawerCallback(new SlidingPane.DrawerCallback() {
            @Override
            public void onDrawerUpdate(int offset) {
                if (getResources().getBoolean(R.bool.isTablet)) {
                    legend.setTranslationX(offset);
                    if (offset != 0) {
                        mapFragment.slidePaneOffset = offset / 2;
                    } else {
                        mapFragment.slidePaneOffset = 0;
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (showCheckOutWarning) {
            showCheckOutWarning = false;
            checkAccount(null);
        }
    }

    /**
     * Callback method from {@link EventListFragment.Callbacks}
     * indicating that the item with the given Event was selected.
     */
    @Override
    public void onItemSelected(Event event) {
        if (multiPane) {
            if (mapFragment != null) {
//                mapFragment.setSelectedEvent(event);
            }
            if (event != null) {
                CollapsedAccountInfo collapsedAccountInfo = new CollapsedAccountInfo(this);
                collapsedAccountInfo.setCallback(this);
                collapsedAccountInfo.setEvent(event);
                slidingPane.setCollapsedView(collapsedAccountInfo);
            } else {
                slidingPane.setCollapsedView(null);
            }
        } else {
            //TODO: phone layout behaviour - only showing list no map do we want to go
        }
    }

    @Override
    public void itemsLoaded(List<Event> events) {
        if (mapFragment != null) {
            mapFragment.setVisibleEvents(events);
        }
    }

    @Override
    public void onMarkerSelected(Event event) {
//        if (eventFragment != null) {
//            eventFragment.setActivated(event);
//        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_visit_plan;
    }

    @Override
    public void onBackPressed() {
        if (getSyncProgressView().isInProgress()) {
            super.onBackPressed();
        } else if (eventFragment != null && eventFragment.handleBack()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onRefresh() {
        if (eventFragment != null && eventFragment.getPresenter() != null) {
            eventFragment.getPresenter().start();
        }
    }

    @Override
    public void onDetailsClick(Event event) {
        Intent intent = new Intent(this, AccountOverviewActivity.class);
        String accountId = event.getAccountId();
        intent.putExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA, accountId);
        startActivityForResult(intent, REQUEST_CODE_OPEN_ACCOUNT);
    }

    @Override
    public void onGetDirections(Event event) {
        double targetLat = event.getAccount().getLatitude();
        double targetLong = event.getAccount().getLongitude();

        String url;

        if (BuildConfig.CHINA_BUILD) {
            url = String.format(getString(R.string.amap_url), String.valueOf(targetLat), String.valueOf(targetLong));
        } else {
            url = String.format(getString(R.string.google_maps_url), String.valueOf(targetLat), String.valueOf(targetLong));
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_ACCOUNT && data != null) {
            showCheckOutWarning = data.getBooleanExtra(AccountOverviewActivity.RESULT_IS_CHECKED_IN, false);
        }
    }

    @Override
    public void onCheckOutFinished() {
        super.onCheckOutFinished();

        if (eventFragment != null && eventFragment.getPresenter() != null) {
            eventFragment.getPresenter().start();
        }
    }

    public void showHideMap(boolean shouldShow) {
        if (!getResources().getBoolean(R.bool.isTablet)) {
            if (shouldShow) {
                slidingPane.expandVertically();
            }
        }
    }

}
