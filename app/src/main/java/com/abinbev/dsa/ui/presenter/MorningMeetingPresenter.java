package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.Meeting_Attendee__c;
import com.abinbev.dsa.model.Morning_Meeting__c;
import com.abinbev.dsa.model.Office_Location__c;
import com.abinbev.dsa.model.OnTapSettings__c;
import com.abinbev.dsa.service.AttachmentUploadService;
import com.abinbev.dsa.sync.DynamicFetchBroadcastListener;
import com.abinbev.dsa.sync.DynamicFetchBroadcastReceiver;
import com.abinbev.dsa.utils.AbInBevConstants.DynamicFetch;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.LocationUtils;
import com.abinbev.dsa.utils.PermissionManager;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;
import com.salesforce.androidsyncengine.dynamicfetch.DynamicFetchEngine;
import com.salesforce.dsa.location.LocationHandler;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;

public class MorningMeetingPresenter extends AbstractLocationAwarePresenter<MorningMeetingPresenter.ViewModel> {

    private static final String TAG = MorningMeetingPresenter.class.getSimpleName();

    private static final String STATE_CURRENT_VIEW_STATE = "current_view_state";

    public interface ViewModel extends AbstractLocationAwarePresenter.LocationViewModel {

        void promptForPicture(int messageId, boolean isCommentRequired);
        void doDeltaSync();
        void setSyncStatus(SyncStatus syncStatus);
        void showDynamicFetchError(String errorMessage);

        void setViewState(ViewState viewState);
    }

    public static abstract class ViewState implements Parcelable {

        public static class Basic extends ViewState {

            public Basic() { }

            protected Basic(Parcel in) { }

            public void setValues(ViewState other) {}

            public Basic duplicate() {
                return new Basic();
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) { }

            @Override
            public String toString() {
                return "Basic{}";
            }

            public static final Parcelable.Creator<Basic> CREATOR =
                    new Parcelable.Creator<Basic>() {

                @Override
                public Basic createFromParcel(Parcel source) {
                    return new Basic(source);
                }

                @Override
                public Basic[] newArray(int size) {
                    return new Basic[size];
                }
            };
        }

        public abstract static class MorningMeeting extends Basic {
            public boolean showLocationProgress;
            public Event morningMeetingEvent;
            public Morning_Meeting__c morningMeeting;
            public Office_Location__c officeLocation;
            public LatLng currentLocation;
            public int requiredCheckInDistance;
            public boolean isPictureCommentRequired;

            protected MorningMeeting() { }

            protected MorningMeeting(Parcel in) {
                super(in);
                this.showLocationProgress = in.readByte() != 0;
                this.morningMeetingEvent = (Event) in.readSerializable();
                this.morningMeeting = (Morning_Meeting__c) in.readSerializable();
                this.officeLocation = (Office_Location__c) in.readSerializable();
                this.currentLocation = in.readParcelable(LatLng.class.getClassLoader());
                this.requiredCheckInDistance = in.readInt();
                this.isPictureCommentRequired = in.readByte() != 0;
            }

            @Override
            public void setValues(ViewState other) {
                super.setValues(other);
                if (other instanceof MorningMeeting) {
                    MorningMeeting otherMorningMeeting = (MorningMeeting) other;
                    showLocationProgress = otherMorningMeeting.showLocationProgress;
                    morningMeetingEvent = otherMorningMeeting.morningMeetingEvent;
                    morningMeeting = otherMorningMeeting.morningMeeting;
                    officeLocation = otherMorningMeeting.officeLocation;
                    currentLocation = otherMorningMeeting.currentLocation;
                    requiredCheckInDistance = otherMorningMeeting.requiredCheckInDistance;
                    isPictureCommentRequired = otherMorningMeeting.isPictureCommentRequired;
                }
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                super.writeToParcel(dest, flags);
                dest.writeByte(this.showLocationProgress ? (byte) 1 : (byte) 0);
                dest.writeSerializable(this.morningMeetingEvent);
                dest.writeSerializable(this.morningMeeting);
                dest.writeSerializable(this.officeLocation);
                dest.writeParcelable(this.currentLocation, flags);
                dest.writeInt(this.requiredCheckInDistance);
                dest.writeByte(this.isPictureCommentRequired ? (byte) 1 : (byte) 0);
            }

            @Override
            public abstract MorningMeeting duplicate();
        }

        public static class MorningMeetingCheckIn extends MorningMeeting {

            public MorningMeetingCheckIn() { }

            public MorningMeetingCheckIn(Parcel in) {
                super(in);
            }

            @Override
            public MorningMeetingCheckIn duplicate() {
                MorningMeetingCheckIn newState = new MorningMeetingCheckIn();
                newState.setValues(this);
                return newState;
            }

            @Override
            public String toString() {
                return "MorningMeetingCheckIn{" +
                        "showLocationProgress=" + showLocationProgress +
                        ", morningMeetingEvent=" + morningMeetingEvent +
                        ", morningMeeting=" + morningMeeting +
                        ", officeLocation=" + officeLocation +
                        ", currentLocation=" + currentLocation +
                        ", requiredCheckInDistance=" + requiredCheckInDistance +
                        ", isPictureCommentRequired=" + isPictureCommentRequired +
                        '}';
            }

            public static final Creator<MorningMeetingCheckIn> CREATOR = new Creator<MorningMeetingCheckIn>() {
                @Override
                public MorningMeetingCheckIn createFromParcel(Parcel source) {
                    return new MorningMeetingCheckIn(source);
                }

                @Override
                public MorningMeetingCheckIn[] newArray(int size) {
                    return new MorningMeetingCheckIn[size];
                }
            };
        }

        public static class MorningMeetingCheckOut extends MorningMeeting {

            public MorningMeetingCheckOut() { }

            public MorningMeetingCheckOut(Parcel in) {
                super(in);
            }

            @Override
            public MorningMeetingCheckOut duplicate() {
                MorningMeetingCheckOut newState = new MorningMeetingCheckOut();
                newState.setValues(this);
                return newState;
            }

            @Override
            public String toString() {
                return "MorningMeetingCheckOut{" +
                        "showLocationProgress=" + showLocationProgress +
                        ", morningMeetingEvent=" + morningMeetingEvent +
                        ", morningMeeting=" + morningMeeting +
                        ", officeLocation=" + officeLocation +
                        ", currentLocation=" + currentLocation +
                        ", requiredCheckInDistance=" + requiredCheckInDistance +
                        ", isPictureCommentRequired=" + isPictureCommentRequired +
                        '}';
            }

            public static final Creator<MorningMeetingCheckOut> CREATOR = new Creator<MorningMeetingCheckOut>() {
                @Override
                public MorningMeetingCheckOut createFromParcel(Parcel source) {
                    return new MorningMeetingCheckOut(source);
                }

                @Override
                public MorningMeetingCheckOut[] newArray(int size) {
                    return new MorningMeetingCheckOut[size];
                }
            };
        }
    }

    final DynamicFetchBroadcastListener dynamicFetchListener = new DynamicFetchBroadcastListener() {

        @Override
        public void onDynamicFetchStarted(String fetchName, SyncStatus syncStatus, Map<String, String> params) {
            viewModel.setSyncStatus(syncStatus);
        }

        @Override
        public void onDynamicFetchProgress(String fetchName, SyncStatus syncStatus, Map<String, String> params) {
            viewModel.setSyncStatus(syncStatus);
        }

        @Override
        public void onDynamicFetchCompleted(String fetchName, SyncStatus syncStatus, Map<String, String> params) {
            viewModel.setSyncStatus(syncStatus);
        }

        @Override
        public void onDynamicFetchError(String fetchName, String errorMessage, Map<String, String> params) {
            viewModel.setSyncStatus(null);
            viewModel.showDynamicFetchError(errorMessage);
        }
    };

    final CompositeSubscription subscription;

    final DynamicFetchBroadcastReceiver dynamicFetchReceiver;

    final Context context;

    boolean isFirstStart = true;

    ViewModel viewModel;

    ViewState viewState;

    public MorningMeetingPresenter(final LocationHandler locationHandler, DynamicFetchBroadcastReceiver dynamicFetchReceiver) {
        super(locationHandler);
        this.subscription = new CompositeSubscription();
        this.dynamicFetchReceiver = dynamicFetchReceiver;
        this.context = ABInBevApp.getAppContext();
        this.viewState = new ViewState.Basic();
        dynamicFetchReceiver.addDynamicFetchName(DynamicFetch.MORNING_MEETING_CHECKED_IN);
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        super.setViewModel(viewModel);
        this.viewModel = viewModel;
        if (viewModel != null) {
            viewModel.setViewState(viewState);
        }
    }

    @Override
    public void start() {
        getCurrentViewState();
        if (isFirstStart) {
            dynamicFetchReceiver.register(context, dynamicFetchListener);
            isFirstStart = false;
        }
    }

    @Override
    public void stop() {
        super.stop();
        subscription.clear();
    }

    private void setViewState(ViewState viewState) {
        Log.i(TAG, "New state: " + viewState);
        this.viewState = viewState;
        if (viewModel != null) {
            viewModel.setViewState(viewState);
        }
    }

    public void onSaveInstanceState(Bundle state) {
        state.putParcelable(STATE_CURRENT_VIEW_STATE, viewState);
    }

    public void onLoadInstanceState(Bundle state) {
        if (state == null) return;
        setViewState(state.getParcelable(STATE_CURRENT_VIEW_STATE));
    }

    public void onDestroy() {
        dynamicFetchReceiver.unregister(context, dynamicFetchListener);
        viewModel = null;
    }

    @Override
    public void onNewLocationReceived(Location location) {
        if (viewState instanceof ViewState.MorningMeeting) {
            ViewState.MorningMeeting morningMeetingVs = (ViewState.MorningMeeting) viewState;

            Office_Location__c officeLocation = morningMeetingVs.officeLocation;
            LatLng officeLatLng = officeLocation == null ? null : officeLocation.getLocation();
            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            float distance = LocationUtils.calculateDistance(officeLatLng, userLatLng);
            float requiredCheckInDistance = morningMeetingVs.requiredCheckInDistance;

            if (distance + location.getAccuracy() <= requiredCheckInDistance) {
                // User is in range.
                stopLocationUpdates();
                ViewState.MorningMeeting newState = morningMeetingVs.duplicate();
                newState.currentLocation = userLatLng;
                newState.showLocationProgress = false;
                setViewState(newState);
            } else if (distance - location.getAccuracy() > requiredCheckInDistance) {
                // User is too far.
                stopLocationUpdates();
                ViewState.MorningMeeting newState = morningMeetingVs.duplicate();
                newState.currentLocation = userLatLng;
                newState.showLocationProgress = false;
                setViewState(newState);
            }

            // In other case it means that location is not precise enough.
        }
        else {
            // We want location only for morning meeting states.
            stopLocationUpdates();
        }
    }

    @Override
    public void onConnected() { }

    private void getCurrentViewState() {
        this.subscription.add(Observable.fromCallable(
                () -> {
                    // Load morning meeting.
                    Morning_Meeting__c morningMeeting = Morning_Meeting__c.getCurrentMeeting();
                    Log.v(TAG, "Morning meeting: " + morningMeeting);

                    Office_Location__c officeLocation = null;
                    Event morningMeetingEvent = null;
                    ViewState viewState = new ViewState.Basic();

                    if (morningMeeting != null) {

                        // Load office location.
                        String officeId = morningMeeting.getOfficeId();
                        officeLocation = Office_Location__c.getById(officeId);
                        Log.v(TAG, "Office location: " + officeLocation);

                        // Load event.
                        morningMeetingEvent = Event.getEventForMorningMeeting();
                        Log.v(TAG, "Morning meeting event: " + morningMeetingEvent);


                        // Check if check-in and check-out are required.
                        if (morningMeetingEvent == null) {
                            // User is not checked into any event. Check if there is a morning meeting for today.
                            Meeting_Attendee__c attendee = Meeting_Attendee__c.getByMorningMeetingId(morningMeeting.getId());

                            if (attendee != null && attendee.isMandatory()) {
                                viewState = new ViewState.MorningMeetingCheckIn();
                            }
                        } else {
                            if (morningMeetingEvent.isCheckedIn()) {
                                viewState = new ViewState.MorningMeetingCheckOut();
                            }
                        }

                        if (viewState instanceof ViewState.MorningMeeting) {
                            ViewState.MorningMeeting morningMeetingVS = (ViewState.MorningMeeting) viewState;

                            morningMeetingVS.morningMeeting = morningMeeting;
                            morningMeetingVS.morningMeetingEvent = morningMeetingEvent;
                            morningMeetingVS.officeLocation = officeLocation;
                            if (officeLocation != null) {
                                morningMeetingVS.showLocationProgress = true;
                            }

                            PermissionManager permissionManager = PermissionManager.getInstance();
                            morningMeetingVS.isPictureCommentRequired = permissionManager.isMorningMeetingPictureCommentRequired();

                            OnTapSettings__c settings = OnTapSettings__c.getCurrentSettings();
                            morningMeetingVS.requiredCheckInDistance = settings.getCheckInDistanceTreshold();
                        }
                    }

                    return viewState;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        viewState -> {
                            setViewState(viewState);
                            if (viewState instanceof ViewState.MorningMeeting) {
                                ViewState.MorningMeeting morningMeetingVS = (ViewState.MorningMeeting) viewState;
                                if (morningMeetingVS.showLocationProgress) {
                                    startLocationUpdates();
                                }
                            }
                        },
                        error -> {
                            Log.e(TAG, "Error in note: ", error);
                        }
                ));
    }

    public void onCheckInClicked() {
        Log.v(TAG, "onCheckInClicked");

        if (viewState instanceof ViewState.MorningMeetingCheckIn) {
            ViewState.MorningMeetingCheckIn checkInViewState = (ViewState.MorningMeetingCheckIn) viewState;

            Office_Location__c officeLocation = checkInViewState.officeLocation;
            LatLng officeLatLng = officeLocation == null ? null : officeLocation.getLocation();
            LatLng userLocation = checkInViewState.currentLocation;
            float requiredCheckInDistance = checkInViewState.requiredCheckInDistance;

            if (officeLatLng == null) {
                viewModel.promptForPicture(R.string.morning_meeting_doesnt_have_location,
                        checkInViewState.isPictureCommentRequired);
            } else if (userLocation == null) {
                viewModel.promptForPicture(R.string.cannot_detect_your_location,
                        checkInViewState.isPictureCommentRequired);
            } else if (isNear(userLocation, officeLatLng, requiredCheckInDistance)) {
                double distance = calculateDistance(officeLocation, userLocation);
                String morningMeetingId = checkInViewState.morningMeeting.getId();

                Event event = new Event();
                event.doMorningMeetingCheckIn(morningMeetingId, userLocation, null);
                event.setMorningMeetingCheckInDistance(distance);
                event.createRecord();

                ViewState.MorningMeetingCheckOut newViewState = new ViewState.MorningMeetingCheckOut();
                newViewState.setValues(checkInViewState);
                newViewState.morningMeetingEvent = event;
                newViewState.showLocationProgress = false;
                setViewState(newViewState);

                startDynamicFetch();
            } else {
                viewModel.promptForPicture(R.string.too_far_from_target,
                        checkInViewState.isPictureCommentRequired);
            }
        }
        else {
            Log.w(TAG, "onCheckInClicked called in " + viewState);
        }
    }

    public void onCheckOutClicked() {
        Log.v(TAG, "onCheckOutClicked");

        if (viewState instanceof ViewState.MorningMeetingCheckOut) {
            ViewState.MorningMeetingCheckOut checkOutViewState = (ViewState.MorningMeetingCheckOut) viewState;

            Office_Location__c officeLocation = checkOutViewState.officeLocation;
            LatLng officeLatLng = officeLocation == null ? null : officeLocation.getLocation();
            LatLng userLocation = checkOutViewState.currentLocation;
            float requiredCheckInDistance = checkOutViewState.requiredCheckInDistance;

            if (officeLatLng == null) {
                viewModel.promptForPicture(R.string.morning_meeting_doesnt_have_location,
                        checkOutViewState.isPictureCommentRequired);
            } else if (userLocation == null) {
                viewModel.promptForPicture(R.string.cannot_detect_your_location,
                        checkOutViewState.isPictureCommentRequired);
            } else if (isNear(userLocation, officeLatLng, requiredCheckInDistance)) {
                double distance = calculateDistance(officeLocation, userLocation);

                checkOutViewState.morningMeetingEvent.doMorningMeetingCheckOut(userLocation, null);
                checkOutViewState.morningMeetingEvent.setMorningMeetingCheckOutDistance(distance);
                checkOutViewState.morningMeetingEvent.updateRecord();

                setViewState(new ViewState.Basic());

                viewModel.doDeltaSync();
            } else {
                viewModel.promptForPicture(R.string.too_far_from_target,
                        checkOutViewState.isPictureCommentRequired);
            }
        }
        else {
            Log.w(TAG, "onCheckOutClicked called in " + viewState);
        }
    }

    public void onPictureTaken(Uri pictureUri, String description) {
        if (viewState instanceof ViewState.MorningMeetingCheckIn) {
            ViewState.MorningMeetingCheckIn checkInViewState = (ViewState.MorningMeetingCheckIn) viewState;

            Office_Location__c officeLocation = checkInViewState.officeLocation;
            LatLng currentLocation = checkInViewState.currentLocation;
            Morning_Meeting__c morningMeeting = checkInViewState.morningMeeting;
            double distance = calculateDistance(officeLocation, currentLocation);

            Event event = new Event();
            event.doMorningMeetingCheckIn(morningMeeting.getId(), currentLocation, description);
            event.setMorningMeetingCheckInDistance(distance);
            event.createRecord();

            ViewState.MorningMeetingCheckOut newViewState = new ViewState.MorningMeetingCheckOut();
            newViewState.setValues(viewState);
            newViewState.morningMeetingEvent = event;
            newViewState.showLocationProgress = false;
            setViewState(newViewState);

            Context context = viewModel.getActivity();
            Intent intent = AttachmentUploadService.uploadCheckInPhoto(context, pictureUri, event.getId());
            context.startService(intent);

            startDynamicFetch();
        }
        else if (viewState instanceof ViewState.MorningMeetingCheckOut) {
            ViewState.MorningMeetingCheckOut checkOutViewState = (ViewState.MorningMeetingCheckOut) viewState;

            Office_Location__c officeLocation = checkOutViewState.officeLocation;
            LatLng currentLocation = checkOutViewState.currentLocation;
            double distance = calculateDistance(officeLocation, currentLocation);

            Event event = checkOutViewState.morningMeetingEvent;
            event.doMorningMeetingCheckOut(currentLocation, description);
            event.setMorningMeetingCheckOutDistance(distance);
            event.updateRecord();

            setViewState(new ViewState.Basic());

            Context context = viewModel.getActivity();
            Intent intent = AttachmentUploadService.uploadCheckOutPhoto(context, pictureUri, event.getId());
            context.startService(intent);

            viewModel.doDeltaSync();
        }
        else {
            Log.w(TAG,"onPictureTaken called in " + viewState);
        }
    }

    public void retryDynamicFetch() {
        startDynamicFetch();
    }

    private boolean isNear(LatLng locA, LatLng locB, float requiredCheckInDistance) {
        return LocationUtils.calculateDistance(locA, locB) <= requiredCheckInDistance;
    }

    private void startDynamicFetch() {
        Context context = ABInBevApp.getAppContext();
        Map<String, String> params = new HashMap<>();

        DynamicFetchEngine.fetchInBackground(context, DynamicFetch.MORNING_MEETING_CHECKED_IN, params);
    }

    private static double calculateDistance(Office_Location__c officeLocation, LatLng currentLatLng) {
        LatLng officeLatLng = officeLocation == null ? null : officeLocation.getLocation();
        return currentLatLng == null || officeLatLng == null ? -1f :
                LocationUtils.calculateDistance(currentLatLng, officeLatLng);
    }
}
