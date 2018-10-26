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
import com.abinbev.dsa.model.checkoutRules.CheckoutRule;
import com.abinbev.dsa.service.AttachmentUploadService;
import com.abinbev.dsa.utils.LocationUtils;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import com.salesforce.dsa.DSAAppState;
import com.salesforce.dsa.data.model.TrackedDocument;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;

/**
 * Created by Jakub Stefanowski on 07.08.2017.
 */

public class CheckOutUseCase {

    public enum State {
        CHECKOUT_RULES_NOT_FULFILLED,
        REQUIRES_LOCATION,
        REQUIRES_LOCATION_PICTURE_USER_TOO_FAR,
        REQUIRES_LOCATION_PICTURE_NO_ACCOUNT_LOCATION,
        REQUIRES_LOCATION_PICTURE_NO_USER_LOCATION,
        TRACKED_DOCUMENTS_IS_NOT_EMPTY,
        CHECK_OUT_NOTE_REQUIRED,
        COMPLETED
    }

    public static class Request implements Parcelable {
        public Account account;
        public User user;
        public Event event;
        public LatLng location;

        public Uri locationPicture;
        public String locationDescription;

        public String checkOutNote;

        public boolean skipLocation;

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
            dest.writeParcelable(this.locationPicture, flags);
            dest.writeString(this.locationDescription);
            dest.writeString(this.checkOutNote);
            dest.writeByte(this.skipLocation ? (byte) 1 : (byte) 0);
        }

        public Request() {
        }

        protected Request(Parcel in) {
            this.account = (Account) in.readSerializable();
            this.user = (User) in.readSerializable();
            this.event = (Event) in.readSerializable();
            this.location = in.readParcelable(LatLng.class.getClassLoader());
            this.locationPicture = in.readParcelable(Uri.class.getClassLoader());
            this.locationDescription = in.readString();
            this.checkOutNote = in.readString();
            this.skipLocation = in.readByte() != 0;
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
        public List<CheckoutRule> checkoutRules;

        public boolean isCheckOutNoteMandatory;
    }

    private PublishSubject<Response> subject = PublishSubject.create();

    private Subscription subscription;

    public void execute(Request request) {
        unsubscribe();

        subscription = CheckoutRule.getNotFulfilledBeforeCheckOut(request.account, request.user, request.event)
                .map(checkoutRules -> {
                    Response response = new Response();
                    response.originalRequest = request;

                    if (checkoutRules.isEmpty()) {
                        OnTapSettings__c settings = OnTapSettings__c.getCurrentSettings();
                        int requiredDistance = settings.getCheckInDistanceTreshold();

                        if (request.locationPicture != null) {
                            List<TrackedDocument> tds = DSAAppState.getInstance().getTrackedDocuments();
                            if (tds.isEmpty()) {
                                if (request.checkOutNote == null) {
                                    response.state = State.CHECK_OUT_NOTE_REQUIRED;
                                    response.isCheckOutNoteMandatory =
                                            !CheckoutRule.getNotFulfilledVisitNoteRule(request.account, request.user, request.event)
                                                    .isEmpty()
                                                    .toBlocking()
                                                    .firstOrDefault(Boolean.TRUE);
                                }
                                else {
                                    doCheckOut(request);
                                    response.state = State.COMPLETED;
                                }
                            }
                            else {
                                response.state = State.TRACKED_DOCUMENTS_IS_NOT_EMPTY;
                            }
                        }
                        else if (request.account.getLocation() == null) {
                            response.state = State.REQUIRES_LOCATION_PICTURE_NO_ACCOUNT_LOCATION;
                        }
                        else if (request.skipLocation) {
                            response.state = State.REQUIRES_LOCATION_PICTURE_NO_USER_LOCATION;
                        }
                        else if (request.location == null) {
                            response.state = State.REQUIRES_LOCATION;
                        }
                        else if (isFarFromTarget(request.account, request.location, requiredDistance)) {
                            response.state = State.REQUIRES_LOCATION_PICTURE_USER_TOO_FAR;
                        }
                        else {
                            List<TrackedDocument> tds = DSAAppState.getInstance().getTrackedDocuments();
                            if (tds.isEmpty()) {
                                if (request.checkOutNote == null) {
                                    response.state = State.CHECK_OUT_NOTE_REQUIRED;
                                    response.isCheckOutNoteMandatory =
                                            !CheckoutRule.getNotFulfilledVisitNoteRule(request.account, request.user, request.event)
                                                    .isEmpty()
                                                    .toBlocking()
                                                    .firstOrDefault(Boolean.TRUE);
                                }
                                else {
                                    doCheckOut(request);
                                    response.state = State.COMPLETED;
                                }
                            }
                            else {
                                response.state = State.TRACKED_DOCUMENTS_IS_NOT_EMPTY;
                            }
                        }
                    }
                    else {
                        response.state = State.CHECKOUT_RULES_NOT_FULFILLED;
                        response.checkoutRules = checkoutRules;
                    }

                    return response;
                })
        .subscribe(subject::onNext, subject::onError);
    }

    public void finish() {
        unsubscribe();
    }

    public Observable<Response> getResponse() {
        return subject;
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

    private void doCheckOut(Request request) {
        Context context = ABInBevApp.getAppContext();
        Event event = request.event;

        if (request.locationPicture != null) {
            // Upload picture.
            Intent intent = AttachmentUploadService.uploadCheckOutPhoto(
                    context, request.locationPicture, event.getId());
            context.startService(intent);
        }

        event.doVisitCheckOut(request.location);
        event.setCheckOutDescription(request.locationDescription);
        event.setCheckOutComment(request.checkOutNote);
        event.setVisitCheckOutDistance(calculateDistance(request.account, request.location));
        event.updateRecord();

        SyncUtils.TriggerRefresh(context);
    }

    private void unsubscribe() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
