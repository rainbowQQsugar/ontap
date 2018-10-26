package com.abinbev.dsa.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.AccountOverviewActivity;
import com.abinbev.dsa.activity.AppBaseDrawerActivity;
import com.abinbev.dsa.activity.ProspectDetailActivity;
import com.abinbev.dsa.activity.VisitPlanActivity;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.VisitState;
import com.abinbev.dsa.ui.customviews.VisitListAddVisit;
import com.abinbev.dsa.ui.customviews.VisitListItemsView;
import com.abinbev.dsa.ui.customviews.VisitListTabBar;
import com.abinbev.dsa.ui.presenter.EventsPresenter;
import com.abinbev.dsa.utils.AppPreferenceUtils;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A list fragment representing a list of Events. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' city upon selection. This helps indicate which item is
 * currently being viewed in a {@link com.google.android.gms.maps.MapFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class EventListFragment extends Fragment implements EventsPresenter.ViewModel {

    /**
     * Represents an invalid position. All valid positions are in the range 0 to 1 less than the
     * number of items in the current adapter.
     */
    public static final int INVALID_POSITION = -1;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        void onItemSelected(Event event);

        void itemsLoaded(List<Event> events);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(Event event) {
        }

        @Override
        public void itemsLoaded(List<Event> events) {

        }
    };

    @Bind(R.id.visit_tab_bar)
    VisitListTabBar visitListTabBar;

    @Bind(R.id.in_plan_visits)
    VisitListItemsView inPlanVisitsView;

    @Bind(R.id.out_of_plan_visits)
    VisitListItemsView outOfPlanVisitsView;

    @Bind(R.id.visit_add)
    VisitListAddVisit visitAddView;

    private EventsPresenter presenter;

    private int lastVisitListTab = VisitListTabBar.TAB_IN_PLAN_VISITS;

    public EventListFragment() {
    }

    public static EventListFragment newInstance() {
        return new EventListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.visit_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        presenter = new EventsPresenter();
        presenter.setViewModel(this);

        visitListTabBar.setSelectedTab(VisitListTabBar.TAB_IN_PLAN_VISITS);
        visitListTabBar.setOnFilterClickedListener(() -> {
            if (inPlanVisitsView.getVisibility() == View.VISIBLE) {
                inPlanVisitsView.showFilters(true);
            } else if (outOfPlanVisitsView.getVisibility() == View.VISIBLE) {
                outOfPlanVisitsView.showFilters(true);
            }
            showMap(true);
        });
        visitListTabBar.setOnTabSelectedListener(tab -> {
            if (tab == VisitListTabBar.TAB_IN_PLAN_VISITS) {
                lastVisitListTab = VisitListTabBar.TAB_IN_PLAN_VISITS;
                inPlanVisitsView.setVisibility(View.VISIBLE);
                outOfPlanVisitsView.setVisibility(View.GONE);
                visitAddView.setVisibility(View.GONE);
                mCallbacks.itemsLoaded(inPlanVisitsView.getVisits());
            } else if (tab == VisitListTabBar.TAB_OUT_OF_PLAN_VISITS) {
                lastVisitListTab = VisitListTabBar.TAB_OUT_OF_PLAN_VISITS;
                inPlanVisitsView.setVisibility(View.GONE);
                outOfPlanVisitsView.setVisibility(View.VISIBLE);
                visitAddView.setVisibility(View.GONE);
                mCallbacks.itemsLoaded(outOfPlanVisitsView.getVisits());
            } else if (tab == VisitListTabBar.TAB_ADD) {
                inPlanVisitsView.setVisibility(View.GONE);
                outOfPlanVisitsView.setVisibility(View.GONE);
                visitAddView.setVisibility(View.VISIBLE);
                showMap(true);
            } else if (tab == VisitListTabBar.TAB_FILTERS) {
                if (lastVisitListTab == VisitListTabBar.TAB_IN_PLAN_VISITS) {
                    inPlanVisitsView.setVisibility(View.VISIBLE);
                    outOfPlanVisitsView.setVisibility(View.GONE);
                } else if (lastVisitListTab == VisitListTabBar.TAB_OUT_OF_PLAN_VISITS) {
                    inPlanVisitsView.setVisibility(View.GONE);
                    outOfPlanVisitsView.setVisibility(View.VISIBLE);
                }
                visitAddView.setVisibility(View.GONE);
                showMap(true);
            }
        });
        inPlanVisitsView.setAdapterFilterChangeCallBack(mCallbacks);
        inPlanVisitsView.setOnEventClickedListener(this::onEventClicked);
        inPlanVisitsView.setOnAccountDetailsClickedListener(this::onAccountDetailsClicked);
        outOfPlanVisitsView.setAdapterFilterChangeCallBack(mCallbacks);
        outOfPlanVisitsView.setOnEventClickedListener(this::onEventClicked);
        outOfPlanVisitsView.setOnAccountDetailsClickedListener(this::onAccountDetailsClicked);

        visitAddView.setOnVisitCreateClickedListener(this::onCreateVisitClicked);
        visitAddView.setOnAddVisitCloseClickedListener(() -> {
            visitListTabBar.changeSelectedTab(lastVisitListTab);
            outOfPlanVisitsView.showFiltersView(false);
            inPlanVisitsView.showFiltersView(false);
        });

        outOfPlanVisitsView.setOnFiltersCloseClickedListener(() -> {
            visitListTabBar.changeSelectedTab(lastVisitListTab);
            outOfPlanVisitsView.showFiltersView(false);
        });
        inPlanVisitsView.setOnFiltersCloseClickedListener(() -> {
            visitListTabBar.changeSelectedTab(lastVisitListTab);
            inPlanVisitsView.showFiltersView(false);
        });

        inPlanVisitsView.setVisibility(View.VISIBLE);
        outOfPlanVisitsView.setVisibility(View.GONE);
        visitAddView.setVisibility(View.GONE);
    }

    @Override
    public void setInPlanVisits(List<Event> visits) {
        int total = visits.size();
        int completed = 0;

        for (Event e : visits) {
            if (e.getVisitState() == VisitState.completed) {
                completed++;
            }
        }

        visitListTabBar.setInPlanVisitsCount(completed, total);
        inPlanVisitsView.setVisits(visits);
        visitAddView.setInPlanVisits(visits);
        if (visitListTabBar.getSelectedTab() == VisitListTabBar.TAB_IN_PLAN_VISITS) {
            mCallbacks.itemsLoaded(visits);
        }
    }

    @Override
    public void setOutOfPlanVisits(List<Event> visits) {
        int total = visits.size();
        int completed = 0;

        for (Event e : visits) {
            if (e.getVisitState() == VisitState.completed) {
                completed++;
            }
        }

        visitListTabBar.setOutOfPlanVisitsCount(completed, total);
        outOfPlanVisitsView.setVisits(visits);
        visitAddView.setOutOfPlanVisits(visits);
        if (visitListTabBar.getSelectedTab() == VisitListTabBar.TAB_OUT_OF_PLAN_VISITS
                ||visitListTabBar.getSelectedTab() == VisitListTabBar.TAB_ADD) {
            mCallbacks.itemsLoaded(visits);
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.stop();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stopLocationUpdates();
    }


    public boolean handleBack() {
        if (inPlanVisitsView.getVisibility() == View.VISIBLE) {
            if (inPlanVisitsView.isShowingFilters()) {
                inPlanVisitsView.showFilters(false);
                return true;
            }
        } else if (outOfPlanVisitsView.getVisibility() == View.VISIBLE) {
            if (outOfPlanVisitsView.isShowingFilters()) {
                outOfPlanVisitsView.showFilters(false);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.startLocationUpdates();
        if (!((AppBaseDrawerActivity) getActivity()).getSyncProgressView().isInProgress()) {
            ((VisitPlanActivity) getActivity()).showHideMap(false);
        }
    }

    private void showMap(boolean show) {
        ((VisitPlanActivity) getActivity()).showHideMap(show);
    }

    private void onEventClicked(Event event) {
        Account account = event.getAccount();
        AppPreferenceUtils.putAccountIdOfCuriosity(getContext(), account.getId());

        Intent intent;
        if (account.isProspect()) {
            intent = new Intent(getContext(), ProspectDetailActivity.class);
            intent.putExtra(ProspectDetailActivity.ACCOUNT_ID_EXTRA, event.getAccount().getId());
            intent.putExtra(ProspectDetailActivity.EVENT_ID_EXTRA, event.getId());
            getActivity().startActivity(intent);
        } else {
            intent = new Intent(getContext(), AccountOverviewActivity.class);
            intent.putExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA, event.getAccount().getId());
            intent.putExtra(AccountOverviewActivity.EVENT_ID_EXTRA, event.getId());
            getActivity().startActivityForResult(intent, VisitPlanActivity.REQUEST_CODE_OPEN_ACCOUNT);
        }
    }

    private void onCreateVisitClicked(Event event) {
        presenter.createEvent(event.getAccount());
    }

    private void onAccountDetailsClicked(String accountId) {
        Intent intent = new Intent(getActivity(), AccountOverviewActivity.class);
        intent.putExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA, accountId);
        getActivity().startActivityForResult(intent, VisitPlanActivity.REQUEST_CODE_OPEN_ACCOUNT);
    }

    @Override
    public void eventCreated(Event event) {
        SyncUtils.TriggerRefresh(getActivity());
    }

    @Override
    public void setLocation(LatLng location) {
        outOfPlanVisitsView.setLocation(location);
        inPlanVisitsView.setLocation(location);
    }

    public EventsPresenter getPresenter() {
        return presenter;
    }
}
