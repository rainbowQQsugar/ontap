package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Contact;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.Morning_Meeting__c;
import com.abinbev.dsa.model.OnTapSettings__c;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.model.checkoutRules.CheckoutRule;
import com.abinbev.dsa.sync.DynamicFetchBroadcastListener;
import com.abinbev.dsa.sync.DynamicFetchBroadcastReceiver;
import com.abinbev.dsa.usecase.events.CheckInUseCase;
import com.abinbev.dsa.usecase.events.CheckOutUseCase;
import com.abinbev.dsa.utils.AbInBevConstants.AccountRecordType;
import com.abinbev.dsa.utils.AbInBevConstants.DynamicFetch;
import com.abinbev.dsa.utils.AbInBevConstants.VisitTypes;
import com.abinbev.dsa.utils.AppPreferenceUtils;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.LocationUtils;
import com.abinbev.dsa.utils.PermissionManager;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.androidsyncengine.datamanager.SFSyncHelper;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;
import com.salesforce.androidsyncengine.dynamicfetch.DynamicFetchEngine;
import com.salesforce.dsa.BuildConfig;
import com.salesforce.dsa.DSAAppState;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rx.Single;
import rx.subscriptions.CompositeSubscription;

public class AccountDetailsPresenter extends AbstractLocationAwarePresenter<AccountDetailsPresenter.ViewModel> implements DynamicFetchControls {

    public static final String TAG = AccountDetailsPresenter.class.getSimpleName();

    private static final String STATE_CHECK_IN_REQUEST = "checkInRequest";
    private static final String STATE_CHECK_OUT_REQUEST = "checkOutRequest";
    private static final String STATE_CURRENT_LOCATION = "currentLocation";
    private static final String STATE_CURRENT_USER = "currentUser";
    private static final String STATE_CURRENT_EVENT = "currentEvent";
    private static final String STATE_CURRENT_ACCOUNT = "currentAccount";
    private static final String STATE_HAS_PENDING_CHECK_IN = "hasPendingCheckIn";
    private static final String STATE_HAS_PENDING_CHECK_OUT = "hasPendingCheckOut";
    private static final String STATE_IS_PICTURE_COMMENT_REQUIRED = "isPictureCommentRequired";
    private static final String STATE_REQUIRED_CHECK_IN_DISTANCE = "requiredCheckInDistance";
    private static final String STATE_ACCOUNT_ID = "accountId";
    private static final String STATE_EVENT_ID = "eventId";
    private static final String STATE_IS_CHECK_OUT_PROMPT = "isCheckoutPrompt";
    private static final String STATE_SHOW_DYNAMIC_FETCH_PROGRESS = "showDynamicFetchProgress";
    private static final String STATE_SHOW_DYNAMIC_FETCH_ERRORS = "showDynamicFetchErrors";

    public interface ViewModel extends AbstractLocationAwarePresenter.LocationViewModel {
        void setAccount(Account Account);
        void setPrimaryContact(Contact contact);
        void setLastVisit(String lastVisitAsString);
        void setOwner(User user);
        void setCheckedInEvent(Event event);
        void setCheckInState(CheckInState state);
        void setCheckInButtonEnabled(boolean enabled);
        void setAutoSyncInProgressHint();
        void setCheckInButtonVisible(boolean visible);
        void showLatestDataNotAvailable(boolean show);
        void setProspectEnable(boolean enable);

        void askToCheckOutFrom(Account account);
        void askForCheckOutDescription(boolean isMandatory);
        void setCanCheckout(final boolean mandatoryTasksFulfilled);
        void setGpsProgressVisible(boolean visible);
        void askForAccountPhoto();
        void informUserCheckOutFirstly();

        void setSyncStatus(SyncStatus syncStatus);
        void showDynamicFetchError(String errorMessage, DynamicFetchControls dynamicFetchControls);
    }

    public interface Navigator {
        void goToMainScreen();
        void goToProspectListScreen();
        void goToMap(Uri destinationUri);
        void goToChatter(String url);
        void goToPhoneApp(String phoneNum);
        void goToNotesList(String accountId);
        void goToAccountDetails(String accountId);
        void goToProspectDetails(String accountId, boolean isCheckOutPrompt);
        void goToAccountOverview(String accountId, boolean isCheckOutPrompt);

        void goToContactsList(String accountId);
        void goToCheckInWithPicture(int messageId, boolean isCommentRequired, Integer checkBoxTextId, boolean checkUsersLocation);
        void goToCheckOutWithPicture(int messageId, boolean isCommentRequired);

        void close();

        void showCheckoutRules(List<CheckoutRule> checkoutRules);
    }

    public enum CheckInState {
        CHECK_IN, CHECK_OUT, HIDDEN
    }

    private final CompositeSubscription subscription;
    private final String accountId;
    private final String eventId;
    private final Context appContext;
    private final boolean isCheckoutPrompt;

    private User currentUser;
    private Event currentEvent;
    private Account currentAccount;
    private LatLng currentLocation;

    private Navigator navigator;
    private boolean hasPendingCheckIn;
    private boolean hasPendingCheckOut;

    private boolean isFirstStart = true;

    private boolean isPictureCommentRequired;

    private CheckInUseCase checkInUseCase;
    private CheckInUseCase.Request checkInRequest;

    private CheckOutUseCase checkOutUseCase;
    private CheckOutUseCase.Request checkOutRequest;

    private int requiredCheckInDistance;

    private DynamicFetchBroadcastReceiver dynamicFetchReceiver;

    private boolean showDynamicFetchProgress;

    private boolean showDynamicFetchErrors;

    final DynamicFetchBroadcastListener dynamicFetchListener = new DynamicFetchBroadcastListener() {

        @Override
        public void onDynamicFetchStarted(String fetchName, SyncStatus syncStatus, Map<String, String> params) {
            // This method gets called also if dynamic fetch is not required (because data is still
            // valid). The result is that progress screen will blink for a second. Showing progress
            // from onDynamicFetchProgress should be enough.
        }

        @Override
        public void onDynamicFetchProgress(String fetchName, SyncStatus syncStatus, Map<String, String> params) {
            if (showDynamicFetchProgress && accountId != null && accountId.equals(params.get("accountId"))) {
                viewModel().setSyncStatus(syncStatus);
            }
        }

        @Override
        public void onDynamicFetchCompleted(String fetchName, SyncStatus syncStatus, Map<String, String> params) {
            if (accountId != null && accountId.equals(params.get("accountId"))) {
                boolean checkAccountPhotoRule = showDynamicFetchProgress;                           // This was not shown earlier due to dynamic fetch progress.

                showDynamicFetchProgress = false;
                showDynamicFetchErrors = false;
                viewModel().setSyncStatus(syncStatus);
                checkDynamicFetchRequired();

                if (checkAccountPhotoRule) {
                    checkAccountPhotoRule();
                }
            }
        }

        @Override
        public void onDynamicFetchError(String fetchName, String errorMessage, Map<String, String> params) {
            if (accountId != null && accountId.equals(params.get("accountId"))) {
                showDynamicFetchProgress = false;
                viewModel().setSyncStatus(null);
                checkDynamicFetchRequired();

                if (showDynamicFetchErrors) {
                    showDynamicFetchErrors = false;
                    viewModel().showDynamicFetchError(errorMessage, AccountDetailsPresenter.this);
                }
            }
        }
    };

    public AccountDetailsPresenter(String accountId, String eventId, boolean isCheckoutPrompt,
                                   DynamicFetchBroadcastReceiver dynamicFetchBroadcastReceiver) {
        super();
        this.accountId = accountId;
        this.eventId = eventId;
        this.subscription = new CompositeSubscription();
        this.appContext = ABInBevApp.getAppContext();
        this.isCheckoutPrompt = isCheckoutPrompt;
        this.dynamicFetchReceiver = dynamicFetchBroadcastReceiver;
        dynamicFetchReceiver.addDynamicFetchName(DynamicFetch.ACCOUNT_CHECKED_IN);
    }

    public AccountDetailsPresenter(Bundle bundle, DynamicFetchBroadcastReceiver dynamicFetchBroadcastReceiver) {
        super();
        this.subscription = new CompositeSubscription();
        this.appContext = ABInBevApp.getAppContext();
        this.accountId = bundle.getString(STATE_ACCOUNT_ID);
        this.eventId = bundle.getString(STATE_EVENT_ID);
        this.isCheckoutPrompt = bundle.getBoolean(STATE_IS_CHECK_OUT_PROMPT);
        this.dynamicFetchReceiver = dynamicFetchBroadcastReceiver;
        dynamicFetchReceiver.addDynamicFetchName(DynamicFetch.ACCOUNT_CHECKED_IN);
        loadInstanceState(bundle);
    }

    public void saveInstanceState(Bundle bundle) {
        bundle.putParcelable(STATE_CHECK_IN_REQUEST, checkInRequest);
        bundle.putParcelable(STATE_CHECK_OUT_REQUEST, checkOutRequest);
        bundle.putParcelable(STATE_CURRENT_LOCATION, currentLocation);
        bundle.putSerializable(STATE_CURRENT_USER, currentUser);
        bundle.putSerializable(STATE_CURRENT_EVENT, currentEvent);
        bundle.putSerializable(STATE_CURRENT_ACCOUNT, currentAccount);
        bundle.putBoolean(STATE_HAS_PENDING_CHECK_IN, hasPendingCheckIn);
        bundle.putBoolean(STATE_HAS_PENDING_CHECK_OUT, hasPendingCheckOut);
        bundle.putBoolean(STATE_IS_PICTURE_COMMENT_REQUIRED, isPictureCommentRequired);
        bundle.putBoolean(STATE_IS_CHECK_OUT_PROMPT, isCheckoutPrompt);
        bundle.putInt(STATE_REQUIRED_CHECK_IN_DISTANCE, requiredCheckInDistance);
        bundle.putString(STATE_ACCOUNT_ID, accountId);
        bundle.putString(STATE_EVENT_ID, eventId);
        bundle.putBoolean(STATE_SHOW_DYNAMIC_FETCH_PROGRESS, showDynamicFetchProgress);
        bundle.putBoolean(STATE_SHOW_DYNAMIC_FETCH_ERRORS, showDynamicFetchErrors);
    }

    public void loadInstanceState(Bundle bundle) {
        checkInRequest = bundle.getParcelable(STATE_CHECK_IN_REQUEST);
        checkOutRequest = bundle.getParcelable(STATE_CHECK_OUT_REQUEST);
        currentLocation = bundle.getParcelable(STATE_CURRENT_LOCATION);
        currentUser = (User) bundle.getSerializable(STATE_CURRENT_USER);
        currentEvent = (Event) bundle.getSerializable(STATE_CURRENT_EVENT);
        currentAccount = (Account) bundle.getSerializable(STATE_CURRENT_ACCOUNT);
        hasPendingCheckIn = bundle.getBoolean(STATE_HAS_PENDING_CHECK_IN);
        hasPendingCheckOut = bundle.getBoolean(STATE_HAS_PENDING_CHECK_OUT);
        isPictureCommentRequired = bundle.getBoolean(STATE_IS_PICTURE_COMMENT_REQUIRED);
        requiredCheckInDistance = bundle.getInt(STATE_REQUIRED_CHECK_IN_DISTANCE);
        showDynamicFetchProgress = bundle.getBoolean(STATE_SHOW_DYNAMIC_FETCH_PROGRESS);
        showDynamicFetchErrors = bundle.getBoolean(STATE_SHOW_DYNAMIC_FETCH_ERRORS);
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public void start() {
        super.start();

        loadInitialData();

        if (isFirstStart) {
            startDynamicFetch();
            isFirstStart = false;
        }

        setupCheckout();
        setupCheckIn();

        viewModel().setCheckInButtonVisible(Morning_Meeting__c.isMorningMeetingFinished());

        if (hasPendingCheckIn || hasPendingCheckOut) {
            startLocationUpdates();
        }

        dynamicFetchReceiver.register(appContext, dynamicFetchListener);
    }

    @Override
    public void stop() {
        super.stop();
        subscription.clear();
        checkInUseCase.finish();
        checkOutUseCase.finish();
        navigator = null;
        checkInUseCase = null;
        checkOutUseCase = null;
        dynamicFetchReceiver.unregister(appContext, dynamicFetchListener);
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "LocationHandler connected.");
    }

    @Override
    protected void startLocationUpdates() {
        viewModel().setGpsProgressVisible(true);
        super.startLocationUpdates();
    }

    @Override
    protected void stopLocationUpdates() {
        viewModel().setGpsProgressVisible(false);
        super.stopLocationUpdates();
    }

    @Override
    public void onNewLocationReceived(Location location) {
        Log.d(TAG, "new location received " + location);

        LatLng accountLatLng = currentAccount.getLocation();
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        float distance = LocationUtils.calculateDistance(accountLatLng, currentLatLng);

        if (distance + location.getAccuracy() <= requiredCheckInDistance) {
            // User is in required range.
            currentLocation = currentLatLng;
            stopLocationUpdates();
            onPreciseLocationReceived();
        }
        else if (distance - location.getAccuracy() > requiredCheckInDistance) {
            // User is outside of required range.
            currentLocation = currentLatLng;
            stopLocationUpdates();
            onPreciseLocationReceived();
        }

        // In other case location is not precise enough.
    }

    private void onPreciseLocationReceived() {
        if (hasPendingCheckIn) {
            hasPendingCheckIn = false;
            checkInRequest.location = currentLocation;
            checkInUseCase.execute(checkInRequest);
        }
        else if (hasPendingCheckOut) {
            hasPendingCheckOut = false;
            checkOutRequest.location = currentLocation;
            checkOutUseCase.execute(checkOutRequest);
        }
    }

    private void loadInitialData() {
        subscription.add(Single.fromCallable(
                () -> {
                    InitialData data = new InitialData();
                    data.account = Account.getById(accountId);
                    data.accountOwner = User.getUserByUserId(data.account.getOwnerId());
                    data.primaryContact = Account.getPrimaryContactForAccountId(accountId);
                    data.lastVisit = Account.getLastVisitDateForAccountId(accountId);
                    data.currentUser = User.getCurrentUser();
                    data.event = getCurrentEvent(eventId, accountId);

                    if (data.event != null) {
                        data.isCheckoutRulesFulfilled = areRulesBeforeCheckOutFulfilled(data.account, data.currentUser, data.event);
                    }
                    else {
                        data.isCheckoutRulesFulfilled = true;
                    }

                    PermissionManager pm = PermissionManager.getInstance();
                    data.isPictureCommentRequired = pm.isCheckInPictureCommentRequired();

                    OnTapSettings__c settings = OnTapSettings__c.getCurrentSettings();
                    data.requiredCheckInDistance = settings.getCheckInDistanceTreshold();

                    return data;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        initialData -> {
                            currentAccount = initialData.account;
                            currentUser = initialData.currentUser;
                            currentEvent = initialData.event;
                            isPictureCommentRequired = initialData.isPictureCommentRequired;
                            requiredCheckInDistance = initialData.requiredCheckInDistance;

                            setCurrentTrackingContact(initialData.primaryContact);
                            checkDynamicFetchRequired();

                            if (viewModel() == null) {
                                return;
                            }

                            viewModel().setAccount(initialData.account);
                            viewModel().setOwner(initialData.accountOwner);
                            viewModel().setPrimaryContact(initialData.primaryContact);
                            viewModel().setLastVisit(initialData.lastVisit);
                            viewModel().setCheckInState(
                                    initialData.event == null || !initialData.event.isCheckedIn() ?
                                            CheckInState.CHECK_IN : CheckInState.CHECK_OUT);
                            viewModel().setCheckedInEvent(initialData.event);
                            viewModel().setCanCheckout(initialData.isCheckoutRulesFulfilled);

                            String accountIdOfCuriosity = AppPreferenceUtils.getAccountIdOfCuriosity(getReceivingActivity());
                            if(TextUtils.isEmpty(accountIdOfCuriosity)){
                                return;
                            }

                            if(accountIdOfCuriosity.equals(currentAccount.getId()) && (currentEvent != null && currentEvent.isCheckedIn())){
                                viewModel().informUserCheckOutFirstly();
                            }

                            AppPreferenceUtils.removeAccountIdOfCuriosity(getReceivingActivity());
                        },
                        error -> Log.e(TAG, "Error: ", error)
                ));
    }

    private static boolean areRulesBeforeCheckOutFulfilled(Account account, User user, Event event) {
        if (account == null || user == null || event == null) return true;

        return CheckoutRule.getNotFulfilledBeforeCheckOut(account, user, event)
                .map(rules -> rules == null || rules.isEmpty())
                .toBlocking()
                .firstOrDefault(Boolean.TRUE);
    }

    private static void setCurrentTrackingContact(Contact contact) {
        if (contact != null) {
            DSAAppState.getInstance().setCurrentTrackingContact(
                    new com.salesforce.dsa.data.model.Contact(contact.toJson()));
        }
    }

    private static Event getCurrentEvent(String eventId, String accountId) {
        Event event = null;

        if (!TextUtils.isEmpty(eventId)) {
            event = Event.getById(eventId);
        }

        /**
         *  0. The event that is in the middle progress has the highest priority, he should handle it firstly.
         */
        List<Event>  unCheckingOutEvents = Event.getCheckedInVisitsFor(accountId);
        if(!unCheckingOutEvents.isEmpty()){
           return unCheckingOutEvents.get(0);
        }


        /**
         *
         *  1.Check whether the event exist.  if it does, return it  when this event is checked in.
         *  Otherwise, go to query all the event that are suitable to put in today's schedule of this account.
         *  But the  event that has been checked in has a higher priority.
        **/

        if (event == null) {
            event = Event.getCurrentVisitFor(accountId);
        }else {

            if(event.isCheckedIn()){
                return event;
            }

            event = Event.getCurrentVisitFor(accountId);
        }

        if(event == null){
            return null;
        }

        if(event.isCheckedIn()){
            return event;
        }

        /**
         * 2. Check whether bdr have already done the visit event today.
         * If he does, then all the other visit to this account would be considered as out of plan.
         * So we return null.
         */

        Event finishedEventToday = Event.getFinishedVisitToday(accountId);
        if(finishedEventToday != null){
            return null;
        }

        return event;
    }

    private void setupCheckIn() {
        checkInUseCase = new CheckInUseCase();
        subscription.add(checkInUseCase.getResponse()
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        response -> {
                            if (response.state == CheckInUseCase.State.ANOTHER_ACCOUNT_IS_CHECKED_IN) {
                                viewModel().setCheckInButtonEnabled(true);
                                viewModel().askToCheckOutFrom(response.anotherCheckedInAccount);
                            }
                            else if (response.state == CheckInUseCase.State.REQUIRES_LOCATION) {
                                viewModel().setCheckInButtonEnabled(true);
                                hasPendingCheckIn = true;
                                startLocationUpdates();
                            }
                            else if (response.state == CheckInUseCase.State.REQUIRES_LOCATION_PICTURE_NO_ACCOUNT_LOCATION) {
                                navigator.goToCheckInWithPicture(R.string.missing_account_location, isPictureCommentRequired, R.string.set_current_location_as_poc_location, true);
                            }
                            else if (response.state == CheckInUseCase.State.REQUIRES_LOCATION_PICTURE_NO_USER_LOCATION) {
                                navigator.goToCheckInWithPicture(R.string.cannot_detect_your_location, isPictureCommentRequired, null, false);
                            }
                            else if (response.state == CheckInUseCase.State.REQUIRES_LOCATION_PICTURE_USER_TOO_FAR) {
                                navigator.goToCheckInWithPicture(R.string.too_far_from_target, isPictureCommentRequired, null, false);
                            }
                            else if (response.state == CheckInUseCase.State.COMPLETED) {
                                viewModel().setCheckInState(CheckInState.CHECK_OUT);

                                /**
                                 * we disable the button in order to wait for the completion of auto sync. So BDR can not leave this store right now.
                                 *
                                 * If the sync process is completed or it passes 30 seconds, we enable it again.
                                 */
                                viewModel().setAutoSyncInProgressHint();

                                refreshCheckoutRules();
                                currentEvent = response.newEvent;
                                checkDynamicFetchRequired();

                                if (currentEvent != null && !VisitTypes.IN_PLAN.equals(currentEvent.getVisitType())) {
                                    showDynamicFetchProgress = true;
                                }
                                else {
                                    checkAccountPhotoRule();
                                }

                                showDynamicFetchErrors = true;

                                startDynamicFetch();
                                SyncUtils.TriggerRefresh(appContext);
                            }
                        },
                        error -> {
                            viewModel().setCheckInButtonEnabled(true);
                            Log.e(TAG, "Error: ", error);
                        }
                ));
    }

    private void refreshCheckoutRules() {
        subscription.add(Single.fromCallable(
                () -> {
                    boolean isCheckOut = currentEvent != null && currentEvent.isCheckedIn();
                    return !isCheckOut || areRulesBeforeCheckOutFulfilled(currentAccount, currentUser, currentEvent);
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        mandatoryTasksFulfilled -> viewModel().setCanCheckout(mandatoryTasksFulfilled),
                        error -> Log.e(TAG, "Error: ", error)
                ));
    }

    private void checkAccountPhotoRule() {
        subscription.add(CheckoutRule.getNotFulfilledAccountPhotoRule(currentAccount, currentUser, currentEvent)
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        checkoutRule -> viewModel().askForAccountPhoto(),
                        error -> Log.e(TAG, "Error: ", error)
                ));
    }

    private void checkDynamicFetchRequired() {
        subscription.add(Single.fromCallable(
                () -> {
                    if (currentEvent == null || !VisitTypes.IN_PLAN.equals(currentEvent.getVisitType())) {
                        Map<String, String> params = new HashMap<>();
                        params.put("accountId", accountId);

                        SFSyncHelper customSyncHelper = SFSyncHelper.getSFSyncHelperInstance(appContext);
                        DynamicFetchEngine dynamicFetchEngine = new DynamicFetchEngine(appContext, customSyncHelper);
                        return dynamicFetchEngine.requiresFetch(DynamicFetch.ACCOUNT_CHECKED_IN, params);
                    }
                    else {
                        return false; // Not needed for in plan visits.
                    }
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        isFetchRequired -> {
                            if (!hasPendingCheckIn && !hasPendingCheckOut) {
                                viewModel().showLatestDataNotAvailable(isFetchRequired);
                            }
                        },
                        error -> Log.e(TAG, "Error: ", error)
                ));
    }

    public void onCheckInClicked() {
        Log.d(TAG, "onCheckInClicked()");
        if (hasPendingCheckIn) {
            stopLocationUpdates();
            hasPendingCheckIn = false;
            checkInRequest.skipLocation = true;
            checkInUseCase.execute(checkInRequest);
        }
        else {
            viewModel().setCheckInButtonEnabled(false);
            checkInRequest = new CheckInUseCase.Request();
            checkInRequest.account = currentAccount;
            checkInRequest.user = currentUser;
            checkInRequest.location = currentLocation;
            checkInRequest.event = currentEvent;
            checkInUseCase.execute(checkInRequest);
        }
    }

    public void doCheckInWithPicture(Uri pictureUri, String description, boolean isCheckBoxChecked, LatLng newLocation) {
        hasPendingCheckIn = false;
        checkInRequest.locationPicture = pictureUri;
        checkInRequest.locationDescription = description;
        checkInRequest.overrideAccountLocation = isCheckBoxChecked;
        if (isCheckBoxChecked && newLocation != null) {
            checkInRequest.location = newLocation;
        }
        checkInUseCase.execute(checkInRequest);
    }

    public void onCheckInWithPictureCancelled() {
        viewModel().setCheckInButtonEnabled(true);
    }

    public void onCheckOutNoteCancelled() {
        viewModel().setCheckInButtonEnabled(true);
    }

    private void setupCheckout() {
        checkOutUseCase = new CheckOutUseCase();
        subscription.add(checkOutUseCase.getResponse()
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        response -> {
                            if (response.state == CheckOutUseCase.State.COMPLETED) {
                                viewModel().setCheckInButtonEnabled(true);
                                if (isCheckoutPrompt) {
                                    navigator.close();
                                }
                                else {
                                    if(currentAccount.isProspect()) {
                                        navigator.goToProspectListScreen();
                                    }else {
                                        navigator.goToMainScreen();
                                    }
                                }
                            }
                            else if (response.state == CheckOutUseCase.State.CHECK_OUT_NOTE_REQUIRED) {
                                viewModel().askForCheckOutDescription(response.isCheckOutNoteMandatory);
                            }
                            else if (response.state == CheckOutUseCase.State.TRACKED_DOCUMENTS_IS_NOT_EMPTY) {
                                viewModel().setCheckInButtonEnabled(true);
                                viewModel().setCanCheckout(true);
                                viewModel().setCheckInState(CheckInState.CHECK_IN);
                            }
                            else if (response.state == CheckOutUseCase.State.REQUIRES_LOCATION_PICTURE_NO_ACCOUNT_LOCATION) {
                                navigator.goToCheckOutWithPicture(R.string.missing_account_location, isPictureCommentRequired);
                            }
                            else if (response.state == CheckOutUseCase.State.REQUIRES_LOCATION_PICTURE_NO_USER_LOCATION) {
                                navigator.goToCheckOutWithPicture(R.string.cannot_detect_your_location, isPictureCommentRequired);
                            }
                            else if (response.state == CheckOutUseCase.State.REQUIRES_LOCATION_PICTURE_USER_TOO_FAR) {
                                navigator.goToCheckOutWithPicture(R.string.too_far_from_target, isPictureCommentRequired);
                            }
                            else if (response.state == CheckOutUseCase.State.CHECKOUT_RULES_NOT_FULFILLED) {
                                viewModel().setCheckInButtonEnabled(true);
                                navigator.showCheckoutRules(response.checkoutRules);
                            }
                            else if (response.state == CheckOutUseCase.State.REQUIRES_LOCATION) {
                                viewModel().setCheckInButtonEnabled(true);
                                hasPendingCheckOut = true;
                                startLocationUpdates();
                            }
                        },
                        error -> {
                            viewModel().setCheckInButtonEnabled(true);
                            Log.e(TAG, "Error: ", error);
                        }
                ));
    }

    public void onCheckOutClicked() {
        Log.d(TAG, "onCheckOutClicked()");
        if (hasPendingCheckOut) {
            stopLocationUpdates();
            hasPendingCheckOut = false;
            checkOutRequest.skipLocation = true;
            checkOutUseCase.execute(checkOutRequest);
        }
        else {
            viewModel().setCheckInButtonEnabled(false);
            checkOutRequest = new CheckOutUseCase.Request();
            checkOutRequest.account = currentAccount;
            checkOutRequest.user = currentUser;
            checkOutRequest.event = currentEvent;
            checkOutRequest.location = currentLocation;
            checkOutUseCase.execute(checkOutRequest);
        }
    }

    public void doCheckOutWithPicture(Uri pictureUri, String description) {
        checkOutRequest.locationPicture = pictureUri;
        checkOutRequest.locationDescription = description;
        checkOutUseCase.execute(checkOutRequest);
    }

    public void onCheckOutCommentReceived(String comment) {
        checkOutRequest.checkOutNote = comment;
        checkOutUseCase.execute(checkOutRequest);
    }

    public void onCheckOutFromAnotherAccountClicked(final Account account) {
        subscription.add(Single.fromCallable(
                () -> {
                    RecordType recordType = RecordType.getById(account.getRecordTypeId());
                    return recordType != null && AccountRecordType.PROSPECT.equals(recordType.getName());
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        isProspect -> {
                            if (isProspect) {
                                navigator.goToProspectDetails(account.getId(), true /* isCheckOutPrompt */);
                            }
                            else {
                                navigator.goToAccountOverview(account.getId(), true /* isCheckOutPrompt */);
                            }
                        },
                        error -> Log.e(TAG, "onError: ", error)
                ));
    }

    private void startDynamicFetch() {
        Map<String, String> params = new HashMap<>();
        params.put("accountId", accountId);
        DynamicFetchEngine.fetchInBackground(appContext, DynamicFetch.ACCOUNT_CHECKED_IN, params);
    }

    public void onGetDirectionsClicked() {
        if (currentAccount == null) return;

        String latString = String.valueOf(currentAccount.getLatitude());
        String lonString = String.valueOf(currentAccount.getLongitude());
        String urlFormat = BuildConfig.CHINA_BUILD ? appContext.getString(R.string.amap_url) :
                appContext.getString(R.string.google_maps_url);
        String url = String.format(urlFormat, latString, lonString);

        navigator.goToMap(Uri.parse(url));
    }

    public void onViewChatterClicked() {
        if (currentAccount != null && currentAccount.toJson() != null) {
            String url = appContext.getString(R.string.chatter_feed_url_suffix) + currentAccount.getId();
            navigator.goToChatter(url);
        }
    }

    public void onStartCallClicked() {
        if (currentAccount != null) {
            navigator.goToPhoneApp(currentAccount.getFirstAvailablePhone());
        }
    }

    public void onViewNotesClicked() {
        if (currentAccount != null) {
            navigator.goToNotesList(currentAccount.getId());
        }
    }

    public void onMoreInfoClicked() {
        if (currentAccount != null) {
            navigator.goToAccountDetails(currentAccount.getId());
        }
    }

    public void onViewContactsClicked() {
        if (currentAccount != null) {
            navigator.goToContactsList(currentAccount.getId());
        }
    }

    @Override
    public void retryDynamicFetch() {
        showDynamicFetchProgress = true;
        showDynamicFetchErrors = true;
        startDynamicFetch();
    }

    static class InitialData {
        Account account;
        User accountOwner;
        Contact primaryContact;
        String lastVisit;
        User currentUser;
        Event event;
        boolean isCheckoutRulesFulfilled;
        boolean isPictureCommentRequired;
        int requiredCheckInDistance;
    }
}
