package com.abinbev.dsa.ui.view;

import static com.abinbev.dsa.utils.AzureUtils.getAzurePhotoFileForAccount;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.AccountDetailsActivity;
import com.abinbev.dsa.activity.AccountOverviewActivity;
import com.abinbev.dsa.activity.AppBaseDrawerActivity;
import com.abinbev.dsa.activity.ChatterWebViewActivity;
import com.abinbev.dsa.activity.CheckInWithPictureActivity;
import com.abinbev.dsa.activity.ContactsListActivity;
import com.abinbev.dsa.activity.NewNoteActivity;
import com.abinbev.dsa.activity.NotesListActivity;
import com.abinbev.dsa.activity.ProspectDetailActivity;
import com.abinbev.dsa.activity.ProspectListActivity;
import com.abinbev.dsa.activity.SyncListener;
import com.abinbev.dsa.activity.TimelineActivity;
import com.abinbev.dsa.activity.UserDetailsActivity;
import com.abinbev.dsa.activity.VisitPlanActivity;
import com.abinbev.dsa.bus.event.AttachmentEvent;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.Contact;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.model.checkoutRules.AccountPhotoCheckoutRule;
import com.abinbev.dsa.model.checkoutRules.CheckoutRule;
import com.abinbev.dsa.sync.DynamicFetchBroadcastReceiver;
import com.abinbev.dsa.ui.presenter.AccountDetailsPresenter;
import com.abinbev.dsa.ui.presenter.AccountHeaderPresenter;
import com.abinbev.dsa.ui.presenter.DynamicFetchControls;
import com.abinbev.dsa.utils.AbInBevConstants.ProspectStatus;
import com.abinbev.dsa.utils.AttachmentUtils;
import com.abinbev.dsa.utils.AzureUtils;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;
import com.salesforce.androidsyncengine.utils.DeviceNetworkUtils;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by wandersonblough on 2/18/16.
 */
public class Account360Header extends LinearLayout implements AccountHeaderPresenter.ViewModel,
        AccountDetailsPresenter.ViewModel, AccountDetailsPresenter.Navigator, RefreshListener, CheckInButton.OnCheckButtonClickedListener, SyncListener {

    public static final String TAG = Account360Header.class.getSimpleName();

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public static final int REQUEST_CODE_TAKE_PICTURE = 178;

    final static long UNIT_SECOND = 1000L;
    final static long TIMEOUT_FOR_CHECKING_NETWORK_STATE = 10 * UNIT_SECOND;
    final static long TIMEOUT_FOR_SYNCING_AFTER_CHECKING = 30 * UNIT_SECOND;
    final static int POST_ACTION_AFTER_SYNCING = 1;
    final static int INTERVAL_ACTION_CHECKING_NETWORK_STATE = 2;

    @Inject
    Bus eventBus;

    @Bind(R.id.hero_image)
    ImageView heroImage;

    @Bind(R.id.image_container)
    ViewGroup heroImageContainer;

    @Bind(R.id.chatter_button)
    TextView chatterBtn;

    @Nullable
    @Bind(R.id.account_title_layout)
    LinearLayout accountTitleLayout;

    @Bind(R.id.account_name)
    TextView accountName;

    @Bind(R.id.account_number)
    TextView accountNumber;

    @Bind(R.id.account_error_message)
    TextView accountErrorMessage;

    @Bind(R.id.directions)
    TextView directions;

    @Bind(R.id.primary_contact)
    TextView primaryContact;

    @Bind(R.id.account_owner)
    TextView accountOwner;

    @Bind(R.id.call_button)
    TextView callButton;

    @Bind(R.id.timeline_button)
    Button timelineButton;

    @Bind(R.id.notes_button)
    Button noteButton;

    @Bind(R.id.take_picture_button)
    View takePictureButton;

    @Bind(R.id.take_picture_button_big)
    View takePictureBigButton;

    @Bind(R.id.check_in_button)
    CheckInButton checkInButton;

    DynamicFetchBroadcastReceiver dynamicFetchBroadcastReceiver;

    AccountDetailsPresenter accountDetailsPresenter;

    AccountDetailsCallback accountDetailsCallback;

    Handler handler = new Handler();

    BroadcastReceiver takePhotoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onTakePictureClicked();
        }
    };

    private AccountHeaderPresenter accountHeaderPresenter;

    private String accountId;

    private String eventId;

    boolean isTablet;

    Float customImageAspectRatio = null;

    @Override
    public void onSyncCompleted() {
        actionAfterCheckingSync();
    }

    @Override
    public void onSyncError(String message) {
        actionAfterCheckingSync();
    }

    @Override
    public void onSyncFailure(String message) {
        actionAfterCheckingSync();
    }

    public void actionAfterCheckingSync() {
        setCheckInButtonEnabled(true);
        mHandler.removeCallbacksAndMessages(null);
    }

    public interface AccountDetailsCallback {

        void onCheckInChanged(boolean isCheckedIn);

        void setSyncStatus(SyncStatus status);

        void showDynamicFetchError(String errorMessage, DynamicFetchControls dynamicFetchControls);
    }

    public Account360Header(Context context) {
        this(context, null);
    }

    public Account360Header(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Account360Header(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttributes(context, attrs, defStyleAttr);
        inflate(context, R.layout.account_360_header, this);
        ButterKnife.bind(this);

        ((AppBaseDrawerActivity) getContext()).getAppComponent().inject(this);
        eventBus.register(this);
        isTablet = getResources().getBoolean(R.bool.isTablet);
        showBigTakePictureButton();

        dynamicFetchBroadcastReceiver = new DynamicFetchBroadcastReceiver();
//        timelineButton.setEnabled(false);

        // Setup check-in button.
        checkInButton.setCheckInValues(R.string.check_in, R.string.please_check_in);
        checkInButton.setCheckOutValues(R.string.check_out, R.string.please_check_out);
        checkInButton.setOnCheckButtonClickedListener(this);

        // Setup custom aspect ratio of image.
        if (customImageAspectRatio != null) {
            PercentRelativeLayout.LayoutParams layoutParams = (PercentRelativeLayout.LayoutParams) heroImageContainer.getLayoutParams();
            layoutParams.getPercentLayoutInfo().aspectRatio = customImageAspectRatio;
            heroImageContainer.setLayoutParams(layoutParams);
        }
    }

    private void readAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.Account360Header, defStyleAttr, 0);

        try {
            if (a.hasValue(R.styleable.Account360Header_customImageAspectRatio)) {
                customImageAspectRatio = a.getFraction(R.styleable.Account360Header_customImageAspectRatio, 1, 1, 0);
            }
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AccountPhotoCheckoutRule.ACTION_TAKE_PHOTO);
        getContext().registerReceiver(takePhotoReceiver, intentFilter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        eventBus.unregister(this);

        if (accountHeaderPresenter != null)
            accountHeaderPresenter.stop();
        if (accountDetailsPresenter != null)
            accountDetailsPresenter.stop();

        getContext().unregisterReceiver(takePhotoReceiver);
    }

    @Override /* RefreshListener */
    public void onRefresh() {
        if (accountDetailsPresenter != null) {
            accountDetailsPresenter.start();
        }
        if (accountHeaderPresenter != null) {
            accountHeaderPresenter.start();
        }
    }

    public String getAccountId() {
        return accountId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setInitialData(String accountId, String eventId, boolean isCheckoutPrompt) {
        this.accountId = accountId;
        this.eventId = eventId;

        if (accountHeaderPresenter == null) {
            accountHeaderPresenter = new AccountHeaderPresenter(accountId);
        } else {
            accountHeaderPresenter.setAccountId(accountId);
        }
        accountHeaderPresenter.setViewModel(this);
        accountHeaderPresenter.start();

        if (accountDetailsPresenter == null) {
            accountDetailsPresenter = new AccountDetailsPresenter(accountId, eventId, isCheckoutPrompt, dynamicFetchBroadcastReceiver);
        }
        accountDetailsPresenter.setViewModel(this);
        accountDetailsPresenter.setNavigator(this);
        accountDetailsPresenter.start();
    }

    public void setAccountDetailsCallback(AccountDetailsCallback callback) {
        this.accountDetailsCallback = callback;
    }

    public void checkInWithPictureSuccess(Intent data) {
        Uri pictureUri = data.getParcelableExtra(CheckInWithPictureActivity.RESULT_PICTURE_URI);
        String description = data.getStringExtra(CheckInWithPictureActivity.RESULT_DESCRIPTION);
        boolean isCheckBoxChecked = data.getBooleanExtra(CheckInWithPictureActivity.RESULT_IS_CHECKBOX_CHECKED, false);
        LatLng location = data.getParcelableExtra(CheckInWithPictureActivity.RESULT_LOCATION);
        Bundle additionalData = data.getBundleExtra(CheckInWithPictureActivity.RESULT_EXTRAS);

        if (additionalData.getBoolean("isCheckIn")) {
            accountDetailsPresenter.doCheckInWithPicture(pictureUri, description, isCheckBoxChecked, location);
        } else {
            accountDetailsPresenter.doCheckOutWithPicture(pictureUri, description);
        }
    }

    public void informUserCheckOutFirstly() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setMessage(R.string.hint_leave_store_firstly);
        builder.setPositiveButton(R.string.yes, null);

        builder.create().show();
    }

    public void checkInWithPictureCancelled() {
        accountDetailsPresenter.onCheckInWithPictureCancelled();
    }

    private void setupChatterButton() {
        if (DeviceNetworkUtils.isConnected(this.getContext())) {
            chatterBtn.setEnabled(true);
        } else {
            chatterBtn.setEnabled(false);
        }
    }

    private void showBigTakePictureButton() {
        takePictureButton.setVisibility(GONE);
        takePictureBigButton.setVisibility(VISIBLE);
    }

    private void showSmallTakePictureButton() {
        takePictureButton.setVisibility(VISIBLE);
        takePictureBigButton.setVisibility(GONE);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////


    @Override /* AccountHeaderPresenter.ViewModel */
    public void setAccountPhoto(Attachment attachment, String accountId) {
        Log.v(TAG, "setAccountPhoto...");

        if (attachment == null) {
            final File azurePhotoForAccount = getAzurePhotoFileForAccount(accountId);

            if (azurePhotoForAccount.exists()) {
                Log.v(TAG, "... set azure local file: " + azurePhotoForAccount.getPath());
                Picasso.with(getContext())
                        .load(azurePhotoForAccount)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .placeholder(R.drawable.bg_empty_photo)
                        .error(R.drawable.bg_empty_photo)
                        .fit()
                        .centerCrop()
                        .into(heroImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                showSmallTakePictureButton();
                            }

                            @Override
                            public void onError() {
                                showBigTakePictureButton();
                            }
                        });
                return;
            } else {
                String completeUrl = AzureUtils.getPathToAccountPhotoInAzure(accountId);

                Log.v(TAG, "... set azure remote file: " + completeUrl);
                Picasso.with(getContext())
                        .load(completeUrl)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .placeholder(R.drawable.bg_empty_photo)
                        .error(R.drawable.bg_empty_photo)
                        .fit()
                        .centerCrop()
                        .into(heroImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                showSmallTakePictureButton();
                            }

                            @Override
                            public void onError() {
                                showBigTakePictureButton();
                            }
                        });
                if (!completeUrl.equals("")) {
                    return;
                }
            }
        }

        if (TextUtils.isEmpty(accountId)) {
            Log.v(TAG, "... set default background");
            Picasso.with(getContext())
                    .load(R.drawable.bg_empty_photo)
                    .fit()
                    .centerCrop()
                    .into(heroImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            showBigTakePictureButton();
                        }

                        @Override
                        public void onError() {
                            showBigTakePictureButton();
                        }
                    });
        }
        // Attachment is not in database, it is only local file.
        else if (TextUtils.isEmpty(attachment.getId())) {
            String path = attachment.getFilePath(getContext(), accountId);
            Log.v(TAG, "... set local file: " + path);
            Picasso.with(getContext())
                    .load("file://" + path)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .placeholder(R.drawable.bg_empty_photo)
                    .error(R.drawable.bg_empty_photo)
                    .fit()
                    .centerCrop()
                    .into(heroImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            showSmallTakePictureButton();
                        }

                        @Override
                        public void onError() {
                            showBigTakePictureButton();
                        }
                    });
        } else {
            Log.v(TAG, "... set attachment " + accountId + "/" + attachment.getId());
            Picasso.with(getContext())
                    .load("attachment://" + accountId + "/" + attachment.getId())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .placeholder(R.drawable.bg_empty_photo)
                    .error(R.drawable.bg_empty_photo)
                    .fit()
                    .centerCrop()
                    .into(heroImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            showSmallTakePictureButton();
                        }

                        @Override
                        public void onError() {
                            showBigTakePictureButton();
                        }
                    });
        }
    }

    @Override /* AccountDetailsPresenter.ViewModel */
    public void askToCheckOutFrom(Account account) {
        String message = getContext().getString(R.string.banner_message, account.getName());
        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton(R.string.check_out, (dialogInterface, i) -> {
                    accountDetailsPresenter.onCheckOutFromAnotherAccountClicked(account);
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    @Override /* AccountDetailsPresenter.ViewModel */
    public void askForAccountPhoto() {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.prospect_photo_is_missing)
                .setPositiveButton(android.R.string.yes, (di, i) -> onTakePictureClicked())
                .setNegativeButton(android.R.string.no, null)
                .create()
                .show();
    }

    @Override /* AccountDetailsPresenter.ViewModel */
    public void setSyncStatus(SyncStatus syncStatus) {
        if (accountDetailsCallback != null) {
            accountDetailsCallback.setSyncStatus(syncStatus);
        }
    }

    @Override /* AccountDetailsPresenter.ViewModel */
    public void showDynamicFetchError(String errorMessage, DynamicFetchControls dynamicFetchControls) {
        if (accountDetailsCallback != null) {
            accountDetailsCallback.showDynamicFetchError(errorMessage, dynamicFetchControls);
        }
    }

    @Override /* AccountDetailsPresenter.ViewModel */
    public void askForCheckOutDescription(boolean isMandatory) {
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_text, null, false);
        final EditText editText = (EditText) view.findViewById(R.id.edit_text);
        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setMessage(R.string.check_out_comment)
                .setView(view)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> accountDetailsPresenter.onCheckOutNoteCancelled())
                .setOnCancelListener(dialogInterface -> accountDetailsPresenter.onCheckOutNoteCancelled())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button saveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view1 -> {
                String comment = editText.getText().toString();
                if (!isMandatory || !TextUtils.isEmpty(comment)) {
                    accountDetailsPresenter.onCheckOutCommentReceived(comment);
                    dialog.dismiss();
                }
            });
        });
        dialog.show();
    }

    @Override /* AccountHeaderPresenter.ViewModel, AccountDetailsPresenter.ViewModel */
    public void setAccount(Account account) {
        if (ProspectStatus.SUBMITTED.equals(account.getProspectStatus())) {
            accountErrorMessage.setText(R.string.error_waiting_for_prospect_conversion_approval);
            accountErrorMessage.setVisibility(VISIBLE);
        } else {
            accountErrorMessage.setVisibility(GONE);
        }

        accountName.setText(account.getName());
        if (TextUtils.isEmpty(account.getSAPNumber())) {
            accountNumber.setVisibility(GONE);
        } else {
            accountNumber.setText("#" + account.getSAPNumber());
            accountNumber.setVisibility(VISIBLE);
        }

        setupChatterButton();

        String phone = account.getFirstAvailablePhone();

        if (isTablet) {
            if (TextUtils.isEmpty(phone)) {
                callButton.setVisibility(GONE);
            } else {
                callButton.setVisibility(VISIBLE);
                callButton.setText(phone);
            }
        } else {
            callButton.setEnabled(!TextUtils.isEmpty(phone));
        }

        directions.setEnabled(account.hasLatitude() && account.hasLongitude());

        setCheckInState(AccountDetailsPresenter.CheckInState.HIDDEN);
    }

    @Override /* AccountDetailsPresenter.ViewModel */
    public void setCheckedInEvent(Event event) {
    }

    @Override /* AccountDetailsPresenter.ViewModel */
    public void setPrimaryContact(Contact contact) {
        if (contact != null) {
            primaryContact.setText(contact.getName());
        }
    }

    @Override /* AccountDetailsPresenter.ViewModel */
    public void setLastVisit(String lastVisitAsString) {
    }

    @Override /* AccountDetailsPresenter.ViewModel */
    public void setOwner(User user) {
        if (user != null) {
            accountOwner.setText(user.getName());
        }
    }

    @Override /* AccountDetailsPresenter.ViewModel */
    public void setCheckInState(AccountDetailsPresenter.CheckInState state) {
        switch (state) {
            case CHECK_IN:
                checkInButton.setState(CheckInButton.State.CHECK_IN);
                break;

            case CHECK_OUT:
                checkInButton.setState(CheckInButton.State.CHECK_OUT);
                break;
        }


        if (accountDetailsCallback != null) {
            boolean isCheckedIn = AccountDetailsPresenter.CheckInState.CHECK_OUT.equals(state);
            accountDetailsCallback.onCheckInChanged(isCheckedIn);
        }
    }

    @Override /* AccountDetailsPresenter.ViewModel */
    public void setCheckInButtonEnabled(boolean enabled) {
        checkInButton.setEnabled(enabled);
        takePictureButton.setEnabled(enabled);
        takePictureBigButton.setEnabled(enabled);
        noteButton.setEnabled(enabled);
    }

    @Override
    public void setProspectEnable(boolean enabled) {
        takePictureButton.setEnabled(enabled);
        takePictureBigButton.setEnabled(enabled);
        noteButton.setEnabled(enabled);
    }


    @Override /* AccountDetailsPresenter.ViewModel */
    public void setCheckInButtonVisible(boolean visible) {
        checkInButton.setVisibility(visible ? VISIBLE : GONE);
    }


    @Override /* AccountDetailsPresenter.ViewModel */
    public void setCanCheckout(final boolean mandatoryTasksFulfilled) {
        checkInButton.setCanCheckout(mandatoryTasksFulfilled);
    }

    @Override /* AccountDetailsPresenter.ViewModel */
    public void setGpsProgressVisible(boolean visible) {
        if (visible) {
            checkInButton.showValidationError(R.string.please_wait_for_location);
        } else {
            checkInButton.hideValidationError();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case POST_ACTION_AFTER_SYNCING:
                    actionAfterCheckingSync();
                    break;

                case INTERVAL_ACTION_CHECKING_NETWORK_STATE:
                    if (!DeviceNetworkUtils.isConnected(getContext())) {
                        actionAfterCheckingSync();
                        break;
                    }

                    sendEmptyMessageDelayed(INTERVAL_ACTION_CHECKING_NETWORK_STATE, TIMEOUT_FOR_CHECKING_NETWORK_STATE);
                    break;

                default:
                    super.handleMessage(msg);
            }

        }
    };

    public void setAutoSyncInProgressHint() {

        setCheckInButtonEnabled(false);
        mHandler.sendEmptyMessageDelayed(POST_ACTION_AFTER_SYNCING, TIMEOUT_FOR_SYNCING_AFTER_CHECKING);
        mHandler.sendEmptyMessage(INTERVAL_ACTION_CHECKING_NETWORK_STATE);
    }


    @Override
    public void showLatestDataNotAvailable(boolean show) {
        if (show) {
            checkInButton.showValidationError(R.string.error_latest_data_not_available);
        } else {
            checkInButton.hideValidationError();
        }
    }

    @Override /* AccountDetailsPresenter.Navigator */
    public void goToMainScreen() {
        Intent[] intents = {
                new Intent(getContext(), UserDetailsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                new Intent(getContext(), VisitPlanActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        };
        getContext().startActivities(intents);
    }

    @Override /* AccountDetailsPresenter.Navigator */
    public void goToProspectListScreen() {
        Intent[] intents = {
                new Intent(getContext(), UserDetailsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                new Intent(getContext(), ProspectListActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        };
        getContext().startActivities(intents);
    }

    @Override /* AccountDetailsPresenter.Navigator */
    public void goToMap(Uri destinationUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, destinationUri);
        getContext().startActivity(intent);
    }

    @Override /* AccountDetailsPresenter.Navigator */
    public void goToChatter(String url) {
        Intent intent = new Intent(getContext(), ChatterWebViewActivity.class);
        intent.putExtra(ChatterWebViewActivity.URL, url);
        getContext().startActivity(intent);
    }

    @Override /* AccountDetailsPresenter.Navigator */
    public void goToPhoneApp(String phoneNum) {
        String url = "tel:" + phoneNum;
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));

        try {
            getContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), R.string.error_no_phone_app, Toast.LENGTH_LONG).show();
        }
    }

    @Override /* AccountDetailsPresenter.Navigator */
    public void goToNotesList(String accountId) {
        Intent intent = new Intent(getContext(), NotesListActivity.class);
        intent.putExtra(NewNoteActivity.ARGS_ACCOUNT_ID, accountId);
        getContext().startActivity(intent);
    }

    @Override /* AccountDetailsPresenter.Navigator */
    public void goToAccountDetails(String accountId) {
        Intent intent = new Intent(getContext(), AccountDetailsActivity.class);
        intent.putExtra(AccountDetailsActivity.ACCOUNT_ID_EXTRA, accountId);
        getContext().startActivity(intent);
    }

    @Override /* AccountDetailsPresenter.Navigator */
    public void goToContactsList(String accountId) {
        Intent intent = new Intent(getContext(), ContactsListActivity.class);
        intent.putExtra(ContactsListActivity.ACCOUNT_ID_EXTRA, accountId);
        getContext().startActivity(intent);
    }

    @Override /* AccountDetailsPresenter.Navigator */
    public void goToCheckInWithPicture(int messageId, boolean isCommentRequired, Integer checkBoxMessageId, boolean checkUsersLocation) {
        Bundle additionalExtras = new Bundle();
        additionalExtras.putBoolean("isCheckIn", true);

        Activity activity = (Activity) getContext();
        activity.startActivityForResult(
                new Intent(activity, CheckInWithPictureActivity.class)
                        .putExtra(CheckInWithPictureActivity.ARGS_MESSAGE, activity.getString(messageId))
                        .putExtra(CheckInWithPictureActivity.ARGS_IS_COMMENT_REQUIRED, isCommentRequired)
                        .putExtra(CheckInWithPictureActivity.ARGS_CHECK_LOCATION, checkUsersLocation)
                        .putExtra(CheckInWithPictureActivity.ARGS_CHECK_BOX_MESSAGE,
                                checkBoxMessageId == null ? null : activity.getString(checkBoxMessageId))
                        .putExtra(CheckInWithPictureActivity.ARGS_EXTRAS, additionalExtras),
                REQUEST_CODE_TAKE_PICTURE);
    }

    @Override /* AccountDetailsPresenter.Navigator */
    public void goToCheckOutWithPicture(int messageId, boolean isCommentRequired) {
        Bundle additionalExtras = new Bundle();
        additionalExtras.putBoolean("isCheckIn", false);

        Activity activity = (Activity) getContext();
        activity.startActivityForResult(
                new Intent(activity, CheckInWithPictureActivity.class)
                        .putExtra(CheckInWithPictureActivity.ARGS_MESSAGE, activity.getString(messageId))
                        .putExtra(CheckInWithPictureActivity.ARGS_IS_COMMENT_REQUIRED, isCommentRequired)
                        .putExtra(CheckInWithPictureActivity.ARGS_EXTRAS, additionalExtras),
                REQUEST_CODE_TAKE_PICTURE);
    }

    @Override /* AccountDetailsPresenter.Navigator */
    public void goToAccountOverview(String accountId, boolean isCheckOutPrompt) {
        Activity activity = (Activity) getContext();
        Intent intent = new Intent(activity, AccountOverviewActivity.class);
        intent.putExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA, accountId);
        intent.putExtra(AccountOverviewActivity.IS_CHECKOUT_PROMPT_EXTRA, true);
        activity.startActivity(intent);
    }

    @Override /* AccountDetailsPresenter.Navigator */
    public void goToProspectDetails(String accountId, boolean isCheckOutPrompt) {
        Activity activity = (Activity) getContext();
        Intent intent = new Intent(activity, ProspectDetailActivity.class);
        intent.putExtra(ProspectDetailActivity.ACCOUNT_ID_EXTRA, accountId);
        intent.putExtra(ProspectDetailActivity.IS_CHECKOUT_PROMPT_EXTRA, true);
        activity.startActivity(intent);
    }

    @Override /* AccountDetailsPresenter.Navigator */
    public void close() {
        Activity activity = (Activity) getContext();
        activity.finish();
    }

    @Override /* AccountDetailsPresenter.ViewModel */
    public void showCheckoutRules(List<CheckoutRule> checkoutRules) {
        CheckoutRulesDialog alertDialog = new CheckoutRulesDialog(getContext());
        alertDialog.setCheckOutRules(checkoutRules);
        alertDialog.show();
    }

    @Override /* AccountDetailsPresenter.ViewModel */
    public Activity getActivity() {
        return (Activity) getContext();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


    @Override /* CheckInButton.OnCheckButtonClickedListener */
    public void onCheckInClicked() {
        accountDetailsPresenter.onCheckInClicked();
    }

    @Override /* CheckInButton.OnCheckButtonClickedListener */
    public void onCheckOutClicked() {
        accountDetailsPresenter.onCheckOutClicked();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////


    @OnClick({R.id.take_picture_button, R.id.take_picture_button_big})
    public void onTakePictureClicked() {
        AlertDialog alertDialog = AttachmentUtils.createPhotoChooserDialog(getContext());
        alertDialog.show();
    }

    @OnClick(R.id.directions)
    public void onDirectionsClicked() {
        accountDetailsPresenter.onGetDirectionsClicked();
    }

    @OnClick(R.id.chatter_button)
    public void onViewChatterClicked() {
        accountDetailsPresenter.onViewChatterClicked();
    }

    @OnClick(R.id.call_button)
    public void onStartCallClicked() {
        accountDetailsPresenter.onStartCallClicked();
    }

    @OnClick(R.id.notes_button)
    public void onViewNotesClicked() {
        accountDetailsPresenter.onViewNotesClicked();
    }

    @OnClick(R.id.more_info)
    public void onMoreInfoClicked() {
        accountDetailsPresenter.onMoreInfoClicked();
    }

    @OnClick(R.id.view_all)
    @Nullable
    public void onViewContactsClicked() {
        accountDetailsPresenter.onViewContactsClicked();
    }

    @OnClick(R.id.timeline_button)
    public void timelineClicked() {
        Intent intent = new Intent(getContext(), TimelineActivity.class);
        intent.putExtra(TimelineActivity.ACCOUNT_ID_EXTRA, accountId);
        getContext().startActivity(intent);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        if (accountDetailsPresenter != null) {
            Bundle bundle = new Bundle();
            accountDetailsPresenter.saveInstanceState(bundle);
            ss.presenterBundle = bundle;
        }

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        if (accountDetailsPresenter == null) {
            accountDetailsPresenter = new AccountDetailsPresenter(ss.presenterBundle, dynamicFetchBroadcastReceiver);
        } else {
            accountDetailsPresenter.loadInstanceState(ss.presenterBundle);
        }
    }

    static class SavedState extends BaseSavedState {
        Bundle presenterBundle;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            presenterBundle = in.readBundle(getClass().getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeBundle(presenterBundle);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


    @Subscribe
    public void attachmentSaved(AttachmentEvent.AttachmentSavedEvent event) {
        if (accountHeaderPresenter != null) {
            accountHeaderPresenter.getAccountPhoto();
        }
    }
}
