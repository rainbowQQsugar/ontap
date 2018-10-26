package com.abinbev.dsa.ui.presenter;

import android.location.Location;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.VisitState;
import com.abinbev.dsa.utils.AbInBevConstants.DynamicFetch;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.DateUtils;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import com.salesforce.androidsyncengine.dynamicfetch.DynamicFetchEngine;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Single;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Jason Harris (jason@akta.com)
 */
public class EventsPresenter extends AbstractLocationAwarePresenter<EventsPresenter.ViewModel> {

    public static final String TAG = EventsPresenter.class.getSimpleName();

    public interface ViewModel extends AbstractLocationAwarePresenter.LocationViewModel {
        void setInPlanVisits(List<Event> visits);

        void setOutOfPlanVisits(List<Event> visits);

        void eventCreated(Event event);

        void setLocation(LatLng location);
    }

    private CompositeSubscription subscription;
    private LatLng currentLocation;

    public EventsPresenter() {
        super();
        this.subscription = new CompositeSubscription();
    }


    @Override
    public void start() {
        super.start();
        super.startLocationUpdates();
        getInPlanVisits();
        getOutOfPlanVisits();
    }

    @Override
    public void onNewLocationReceived(Location location) {
        currentLocation = getLocationHandler().getCurrentLatLng();
        viewModel().setLocation(currentLocation);
    }

    @Override
    public void onConnected() {
    }

    private List<Event> filterDailyVisits(List<Event> events) {
        List<Event> filterEvents = new ArrayList<>();
        for (Event event : events) {
            String startDateString = event.getStartDate();
            String endDateString = event.getEndDate();
            Date currentDate = DateUtils.dateFromString(DateUtils.currentDateString());
            Date startDate = DateUtils.dateFromString(DateUtils.fromServerDateTimeToDate(startDateString));
            Date endDate = DateUtils.dateFromString(DateUtils.fromServerDateTimeToDate(endDateString));
            if (((currentDate.getTime() >= startDate.getTime()) && (currentDate.getTime() <= endDate.getTime()))) {
                filterEvents.add(event);
            }
        }
        return filterEvents;

    }

    private void getInPlanVisits() {
        subscription.add(Single.fromCallable(Event::getCurrentInPlanVisits)
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        events -> viewModel().setInPlanVisits(filterDailyVisits(events)),
                        error -> Log.e(TAG, "Error getting events: ", error)
                ));
    }

    private void getOutOfPlanVisits() {
        subscription.add(Single.fromCallable(Event::getCurrentOutOfPlanVisits)
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        events -> viewModel().setOutOfPlanVisits(events),
                        error -> Log.e(TAG, "Error getting events: ", error)
                ));
    }

    public void createEvent(final Account account) {
        if (account == null) {
            Log.e(TAG, "Unable to create an event without an account.");
            return;
        }

        subscription.add(Single.fromCallable(
                () -> {
                    Event event = Event.createEvent(account);

                    Map<String, String> params = new HashMap<>();
                    params.put("accountId", account.getId());
                    DynamicFetchEngine.fetchInBackground(ABInBevApp.getAppContext(), DynamicFetch.ACCOUNT_CHECKED_IN, params);
                    SyncUtils.TriggerAccountContentRefresh(ABInBevApp.getAppContext(), account.getId());

                    return event;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        event -> {
                            viewModel().eventCreated(event);
                            getOutOfPlanVisits();
                        },
                        error -> Log.e(TAG, "Error creating event: ", error)
                ));
    }

    public void stopLocationUpdates() {
        super.stopLocationUpdates();
    }

    public void startLocationUpdates() {
        super.startLocationUpdates();
    }

    @Override
    public void stop() {
        super.stop();
        subscription.clear();
    }
}
