package com.abinbev.dsa.usecase.events;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.OnTapSettings__c;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.service.AttachmentUploadService;
import com.abinbev.dsa.utils.LocationUtils;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.dsa.DSAAppState;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;

/**
 * Created by Jakub Stefanowski on 07.08.2017.
 */

public class CheckInUseCase {

    public enum State {
        ANOTHER_ACCOUNT_IS_CHECKED_IN,
        REQUIRES_LOCATION,
        REQUIRES_LOCATION_PICTURE_USER_TOO_FAR,
        REQUIRES_LOCATION_PICTURE_NO_ACCOUNT_LOCATION,
        REQUIRES_LOCATION_PICTURE_NO_USER_LOCATION,
        COMPLETED
    }

    public static class Request implements Parcelable {
        public Account account;
        public User user;
        public Event event;
        public LatLng location;
        public boolean skipLocation;
        public boolean overrideAccountLocation;

        public Uri locationPicture;
        public String locationDescription;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(this.account);
            dest.writeSerializable(this.user);
            dest.writeSerializable(this.event);
            dest.writeParcelable(this.location, flags);
            dest.writeByte(this.skipLocation ? (byte) 1 : (byte) 0);
            dest.writeByte(this.overrideAccountLocation ? (byte) 1 : (byte) 0);
            dest.writeParcelable(this.locationPicture, flags);
            dest.writeString(this.locationDescription);
        }

        public Request() {
        }

        protected Request(Parcel in) {
            this.account = (Account) in.readSerializable();
            this.user = (User) in.readSerializable();
            this.event = (Event) in.readSerializable();
            this.location = in.readParcelable(LatLng.class.getClassLoader());
            this.skipLocation = in.readByte() != 0;
            this.overrideAccountLocation = in.readByte() != 0;
            this.locationPicture = in.readParcelable(Uri.class.getClassLoader());
            this.locationDescription = in.readString();
        }

        public static final Parcelable.Creator<Request> CREATOR = new Parcelable.Creator<Request>() {
            @Override
            public Request createFromParcel(Parcel source) {
                return new Request(source);
            }

            @Override
            public Request[] newArray(int size) {
                return new Request[size];
            }
        };
    }

    public static class Response {
        public Request originalRequest;
        public State state;
        public Event newEvent;

        public Account anotherCheckedInAccount;
    }

    private PublishSubject<Response> subject = PublishSubject.create();

    private Subscription subscription;

    public void execute(Request request) {
        unsubscribe();

        subscription = Observable.fromCallable(
                () -> {
                    Response response = new Response();
                    response.originalRequest = request;

                    OnTapSettings__c settings = OnTapSettings__c.getCurrentSettings();
                    int requiredDistance = settings.getCheckInDistanceTreshold();

                    List<Event> otherAccountCheckIns = Event.getAllCheckedInVisits();
                    if (!otherAccountCheckIns.isEmpty()) {
                        Event newestEvent = otherAccountCheckIns.get(0);
                        Account checkedInAccount = Account.getById(newestEvent.getWhatId());
                        response.state = State.ANOTHER_ACCOUNT_IS_CHECKED_IN;
                        response.anotherCheckedInAccount = checkedInAccount;
                    }
                    else {

                        if (request.locationPicture != null) {
                            response.newEvent = doCheckIn(request, requiredDistance);
                            response.state = State.COMPLETED;
                        }
                        else if (request.account.getLocation() == null) {
                            response.state = State.REQUIRES_LOCATION_PICTURE_NO_ACCOUNT_LOCATION;
                        }
                        else {
                            if (request.skipLocation) {
                                response.state = State.REQUIRES_LOCATION_PICTURE_NO_USER_LOCATION;
                            }
                            else if (request.location == null) {
                                response.state = State.REQUIRES_LOCATION;
                            }
                            else if (isFarFromTarget(request.account, request.location, requiredDistance)) {
                                response.state = State.REQUIRES_LOCATION_PICTURE_USER_TOO_FAR;
                            }
                            else {
                                response.newEvent = doCheckIn(request, requiredDistance);
                                response.state = State.COMPLETED;
                            }
                        }
                    }

                    return response;
                })
                .subscribe(subject::onNext, subject::onError);
    }

    public Observable<Response> getResponse() {
        return subject;
    }

    public void finish() {
        unsubscribe();
    }

    private boolean isFarFromTarget(Account account, LatLng currentLocation, int requiredDistance) {
        LatLng accountLocation = account.getLocation();
        return currentLocation == null || accountLocation == null ||
                LocationUtils.calculateDistance(currentLocation, accountLocation) > requiredDistance;
    }

    private double calculateDistance(Account account, LatLng currentLocation) {
        LatLng accountLocation = account.getLocation();
        return currentLocation == null || accountLocation == null ? -1f :
                LocationUtils.calculateDistance(currentLocation, accountLocation);
    }

    private Date getCurrentDate() {
        return Calendar.getInstance().getTime();
    }

    private Event doCheckIn(Request request, int requiredDistance) {
        Context context = ABInBevApp.getAppContext();
        Event currentEvent = request.event;

        if (currentEvent == null) {
            currentEvent = Event.createEvent(request.account, request.location);
        }

        Date currentDate = getCurrentDate();

        // Check out all other events.
        Event.setAllCheckedInEventsAsCheckedOut(currentDate);

        // Update account.
        request.account.setLastVisit(currentDate);
        if (request.account.isProspect()) {
            request.account.changeProspectStatusCheckedIn();
        }

        // Set users current location as new location for account.
        if (request.overrideAccountLocation && request.location != null) {
            request.account.setLocation(request.location);
        }

        request.account.updateRecord();

        // Update event.
        currentEvent.checkIn(currentDate, request.location);
        if (request.locationDescription != null) {
            currentEvent.setCheckInDescription(request.locationDescription);
        }

        if (request.locationPicture != null) {
            // Upload picture.
            Intent intent = AttachmentUploadService.uploadCheckInPhoto(context, request.locationPicture, currentEvent.getId());
            context.startService(intent);
        }

        double distance = calculateDistance(request.account, request.location);
        boolean locationCompliance = distance >= 0 && distance <= requiredDistance;

        currentEvent.setVisitCheckInDistance(distance);
        currentEvent.setVisitLocationCompliance(locationCompliance);

        Event.updateEvent(currentEvent);


        DSAAppState.getInstance().setTrackingType(DSAAppState.DocumentTrackingType.DocumentTracking_AlwaysOn);

        return currentEvent;
    }

    private void unsubscribe() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
