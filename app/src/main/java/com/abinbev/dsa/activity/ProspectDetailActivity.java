package com.abinbev.dsa.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.service.AttachmentUploadService;
import com.abinbev.dsa.ui.presenter.ProspectDetailPresenter;
import com.abinbev.dsa.ui.presenter.ProspectDetailPresenter.State;
import com.abinbev.dsa.ui.view.Account360Header;
import com.abinbev.dsa.ui.view.NotesView;
import com.abinbev.dsa.ui.view.ProspectCreation;
import com.abinbev.dsa.utils.AttachmentUtils;
import com.abinbev.dsa.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import butterknife.Bind;

public class ProspectDetailActivity extends AppBaseDrawerActivity implements ProspectDetailPresenter.ViewModel {

    public static final String ACCOUNT_ID_EXTRA = "account_id";
    public static final String EVENT_ID_EXTRA = "event_id";
    public static final String IS_CHECKOUT_PROMPT_EXTRA = "is_checkout_prompt";
    public static final String PROSPECT_STATUS = "prospectStatus";

    private static final int REQUEST_CODE_OPEN_NEW_NOTE = 1;

    @Bind(R.id.account_header)
    Account360Header account360Header;

    @Bind(R.id.prospect_creation)
    ProspectCreation prospectCreation;

    @Nullable
    @Bind(R.id.notes_view)
    NotesView notesView;

    private String prospectId;
    private String eventId;
    private boolean isCheckoutPrompt;
    private ProspectDetailPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prospectId = getIntent().getStringExtra(ACCOUNT_ID_EXTRA);
        eventId = getIntent().getStringExtra(EVENT_ID_EXTRA);
        isCheckoutPrompt = getIntent().getBooleanExtra(IS_CHECKOUT_PROMPT_EXTRA, false);

        presenter = new ProspectDetailPresenter(prospectId);

        if (notesView != null) {
            notesView.setAccountId(prospectId);
        }

        prospectCreation.setProspectId(prospectId);
        prospectCreation.setConvertClickedListener(presenter::onConvertClicked);
        prospectCreation.setUnqualifiedClickedListener(presenter::onUnqualifiedClicked);

    }

    @Override
    public int getLayoutResId() {
        return R.layout.poc_detail_view;
    }

    @Override
    protected boolean attachToRoot() {
        return true;
    }

    @Override
    public void onRefresh() {
        checkAccount(prospectId);

        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Account360Header.REQUEST_CODE_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                account360Header.checkInWithPictureSuccess(data);
            } else {
                account360Header.checkInWithPictureCancelled();
            }
        } else if (requestCode == AttachmentUtils.SELECT_PHOTO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = AttachmentUtils.fileUri != null ? AttachmentUtils.fileUri : data.getData();

                if (uri == null) {
                    Log.e("Babu", "data is null!");
                    return;
                }

                Intent intent = AttachmentUploadService.uploadAccountPhoto(this, uri, prospectId);
                startService(intent);
            }
        } else if (requestCode == REQUEST_CODE_OPEN_NEW_NOTE && resultCode == RESULT_OK) {
            presenter.updateAccountToUnqualified();
        }
    }

    @Override /* ProspectDetailPresenter.ViewModel */
    public void setState(State state) {
        setupAccounts(state);
        setupNegotiation(state);
        setupAttachments(state);
        setupButtons(state);
    }

    @Override /* ProspectDetailPresenter.ViewModel */
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showUnqualifiedNote() {
        Intent intent = new Intent(this, NewNoteActivity.class);
        intent.putExtra(NewNoteActivity.ARGS_ACCOUNT_ID, prospectId);
        startActivityForResult(intent, REQUEST_CODE_OPEN_NEW_NOTE);
    }

    @Override
    public void close() {
        finish();
    }

    private void setupAccounts(State state) {
        if (state.account != null) {
            //this will overwrite the temporary id with the salesforce id
            prospectId = state.account.getId();
            prospectCreation.setProspectId(prospectId);

            // Don't set data if it didn't change. It would start unnecessary refresh.
            if (!Objects.equals(prospectId, account360Header.getAccountId()) ||
                    !Objects.equals(eventId, account360Header.getEventId())) {
                account360Header.setInitialData(prospectId, eventId, isCheckoutPrompt);
            }

            if (state.hasBasicDataPermission) {
                prospectCreation.showBasicDataCheck(state.account.hasBasicData());
            }

            if (state.hasAdditionalDataPermission) {
                prospectCreation.showAdditionalDataCheck(state.account.hasAdditionalData());
            }
            if (!state.isCheckinButtonEnabled) {
                List<Event> checkedInVisitsFor = Event.getCheckedInVisitsFor(prospectId);
                if (checkedInVisitsFor != null && checkedInVisitsFor.size() != 0) {
                    Date currentDate = Calendar.getInstance().getTime();
                    String date = DateUtils.SERVER_DATE_TIME_FORMAT.format(currentDate);
                    Event.checkoutEvent(checkedInVisitsFor.get(0), date, null);
                }
            }
            account360Header.setCheckInButtonVisible(state.isCheckinButtonEnabled);
            account360Header.setProspectEnable(state.isCheckinButtonEnabled);
        }
    }

    private void setupNegotiation(State state) {
        if (state.hasNegotiationPermission) {
            if (state.negotiation == null || !state.negotiation.isCompleted()) {
                prospectCreation.showNegotiationCheck(false);
            } else {
                prospectCreation.showNegotiationCheck(true);
                prospectCreation.setNegotiationId(state.negotiation.getId());
            }
        }
    }

    private void setupAttachments(State state) {
        prospectCreation.showAttachmentCheck(state.attachments != null && !state.attachments.isEmpty());
    }

    private void setupButtons(State state) {

        // Hide all buttons if morning meeting is not checked out.
        if (!state.isMorningMeetingFinished) {
            prospectCreation.showTilesContainer(false);
        } else {
            boolean hasBasicData = state.hasBasicDataPermission;
            boolean hasAdditionalData = state.hasAdditionalDataPermission;
            boolean hasNegotiations = state.hasNegotiationPermission;
            boolean hasFiles = state.hasAttachmentsPermission;
            boolean isCheckinEnabled = state.isCheckinButtonEnabled;
            if (isCheckinEnabled) {
            }
            if (!hasBasicData && !hasAdditionalData && !hasNegotiations && !hasFiles) {
                prospectCreation.showTilesContainer(false);
            } else {
                prospectCreation.showTilesContainer(true);
                prospectCreation.showBasicDataContainer(hasBasicData);
                prospectCreation.showAdditionalDataContainer(hasAdditionalData);
                prospectCreation.showNegotiationContainer(hasNegotiations);
                prospectCreation.showAttachmentContainer(hasFiles);

                prospectCreation.setBasicDataEnabled(state.isProspectDataEnabled);
                prospectCreation.setAdditionalDataEnabled(state.isProspectDataEnabled);
                prospectCreation.setNegotiationEnabled(state.isNegotiationEnabled);
                prospectCreation.setAttachmentEnabled(state.isProspectDataEnabled || state.isNegotiationEnabled);
                prospectCreation.setConversionButtonEnabled(state.isConversionEnabled);
                prospectCreation.setUnqualifiedButtonEnabled(state.isUnqualifiedEnabled);
            }
        }
    }
}
