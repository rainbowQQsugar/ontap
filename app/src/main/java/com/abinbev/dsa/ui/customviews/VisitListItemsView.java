package com.abinbev.dsa.ui.customviews;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.events.CompositeEventFilter;
import com.abinbev.dsa.adapter.events.EventAdapter;
import com.abinbev.dsa.fragments.EventListFragment;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.ui.view.DividerItemDecoration;
import com.abinbev.dsa.ui.view.ScrollToTopLinearLayoutManager;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.dsa.BuildConfig;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Jakub Stefanowski on 08.08.2017.
 */

public class VisitListItemsView extends FrameLayout {

    private static final String TAG = "VisitListItemsView";

    public interface OnEventClickedListener {
        void onEventClicked(Event event);
    }

    public interface OnAccountDetailsClickedListener {
        void onAccountDetailsClicked(String accountId);
    }

    Filter.FilterListener filterListener = (count) -> {
//                mCallbacks.itemsLoaded(pedidoListAdapter.getItems()); //TODO
    };

    @Bind(R.id.view_visit_list_items_recycler)
    RecyclerView recyclerView;

    @Bind(R.id.view_visit_list_items_filter)
    VisitListEventFilter visitFilter;

    EventAdapter adapter;

    LinearLayoutManager layoutManager;

    OnEventClickedListener onEventClickedListener;

    OnAccountDetailsClickedListener onAccountDetailsClickedListener;

    LatLng currentLocation;

    public VisitListItemsView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VisitListItemsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VisitListItemsView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_visit_list_items, this);
        ButterKnife.bind(this);

        ////////////////////////////////////////////////////////////////////////////////////////////
        layoutManager = new ScrollToTopLinearLayoutManager(context);

        ////////////////////////////////////////////////////////////////////////////////////////////

        String[] statusSortingOrder = getResources().getStringArray(R.array.visit_statuses);
        adapter = new EventAdapter(statusSortingOrder);
        adapter.setIsTablet(getResources().getBoolean(R.bool.isTablet));
        adapter.setListener(new EventAdapter.EventListener() {
            @Override
            public void onCloseClick() { }

            @Override
            public void onCreateEventClick(Event event, int position) { }

            @Override
            public void onEventClick(Event event, int position) {
                if (onEventClickedListener != null) {
                    onEventClickedListener.onEventClicked(event);
                }
            }

            @Override
            public void onAccountDetailsClick(String accountId) {
                if (onAccountDetailsClickedListener != null) {
                    onAccountDetailsClickedListener.onAccountDetailsClicked(accountId);
                }
            }

            @Override
            public void onDirectionsClick(Event event) {
                startNavigation(event);
            }

            @Override
            public void onCallClick(Event event) {
                startCall(event);
            }
        });

        ////////////////////////////////////////////////////////////////////////////////////////////
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int height = bottom - top;
            // only set the height of full-size, (more info) items to the largest we have measured
            if (height > adapter.getFullSizeHeight()) {
                adapter.setFullSizeHeight(height);
            }
        });

        ////////////////////////////////////////////////////////////////////////////////////////////
        visitFilter.setVisibility(GONE);

        visitFilter.setOnFilterEventsListener((visitState, channel, pocName, ascending) -> {
            adapter.getCompositeEventFilter()
                    .clearFilters()
                    .addFilter(new CompositeEventFilter.VisitFilter(visitState))
                    .addFilter(new CompositeEventFilter.ChannelFilter(channel))
                    .addFilter(new CompositeEventFilter.POCNameFilter(pocName));

            adapter.setSortAscending(ascending);

            adapter.getFilter().filter(null, filterListener);
            showFiltersView(false);
        });
    }
    public void setAdapterFilterChangeCallBack(EventListFragment.Callbacks callbacks){
        if(adapter!=null){
          adapter.setCallbacks(callbacks);
        }
    }
    public void showFilters(boolean show) {
        showFiltersView(show);
        if (!show) {
            adapter.clearSearches();
            CompositeEventFilter compositeEventFilter = adapter.getCompositeEventFilter();
            compositeEventFilter.clearFilters();
            compositeEventFilter.filter(null, filterListener);
            visitFilter.resetSelection();
        }
    }

    public void setOnFiltersCloseClickedListener(VisitListEventFilter.OnCloseListener l) {
        visitFilter.setOnCloseListener(l);
    }

    public void showFiltersView(boolean show) {
        if (show) {
            visitFilter.setVisibility(VISIBLE);
            recyclerView.setVisibility(GONE);
        }
        else {
            visitFilter.setVisibility(GONE);
            recyclerView.setVisibility(VISIBLE);
        }
    }

    public boolean isShowingFilters() {
        return visitFilter.getVisibility() == VISIBLE;
    }

    public List<Event> getVisits() {
        adapter.notifyDataSetChanged();
        return adapter.getEvents();
    }

    public void setVisits(@NonNull List<Event> visits) {
        adapter.setEvents(visits, true);
        visitFilter.setupFilters(visits);
    }

    public void setLocation(LatLng location) {
        currentLocation = location;
        adapter.setLocation(location);
    }

    public void setOnEventClickedListener(OnEventClickedListener l) {
        this.onEventClickedListener = l;
    }

    public void setOnAccountDetailsClickedListener(OnAccountDetailsClickedListener l) {
        this.onAccountDetailsClickedListener = l;
    }

    private void startNavigation(Event event) {
        Resources res = getResources();
        double targetLat = event.getAccount().getLatitude();
        double targetLong = event.getAccount().getLongitude();

        String url;

        if (BuildConfig.CHINA_BUILD) {
            url = String.format(res.getString(R.string.amap_url), String.valueOf(targetLat), String.valueOf(targetLong));
        } else {
            url = String.format(res.getString(R.string.google_maps_url), String.valueOf(targetLat), String.valueOf(targetLong));
        }

        Log.e(TAG, "Navigation url: " + url);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        getContext().startActivity(intent);
    }

    private void startCall(Event event) {
        String url = "tel:" + event.getAccount().getFirstAvailablePhone();
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));

        try {
            getContext().startActivity(intent);
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), R.string.error_no_phone_app, Toast.LENGTH_LONG).show();
        }
    }
}
