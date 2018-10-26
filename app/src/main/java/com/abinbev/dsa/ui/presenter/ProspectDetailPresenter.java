package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.model.Morning_Meeting__c;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.AccountRecordType;
import com.abinbev.dsa.utils.AbInBevConstants.ProspectStatus;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.DateUtils;
import com.abinbev.dsa.utils.PermissionManager;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;

import java.util.Date;
import java.util.List;

import rx.Single;

public class ProspectDetailPresenter extends AbstractRxPresenter<ProspectDetailPresenter.ViewModel> {

    private static final String TAG = ProspectDetailPresenter.class.getSimpleName();

    public interface ViewModel {
        void setState(State state);

        void showError(String message);

        void showUnqualifiedNote();

        void close();
    }

    public static class State {
        public List<Attachment> attachments;
        public Account account;
        public CN_Product_Negotiation__c negotiation;

        public boolean hasBasicDataPermission;
        public boolean hasAdditionalDataPermission;
        public boolean hasNegotiationPermission;
        public boolean hasAttachmentsPermission;

        public boolean isMorningMeetingFinished;

        public boolean isProspectDataEnabled;
        public boolean isNegotiationEnabled;
        public boolean isConversionEnabled;
        public boolean isUnqualifiedEnabled;
        public boolean isCheckinButtonEnabled;
    }

    private String accountId;

    private State state;

    public ProspectDetailPresenter(String accountId) {
        super();
        this.accountId = accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public void start() {
        super.start();
        loadInitialData();
    }

    private void loadInitialData() {
        addSubscription(Single.fromCallable(
                () -> {
                    PermissionManager pm = PermissionManager.getInstance();

                    State state = new State();
                    state.account = Account.getById(accountId);

                    state.hasBasicDataPermission = pm.hasPermission(PermissionManager.PROSPECT_BASIC_DATA);
                    state.hasAdditionalDataPermission = pm.hasPermission(PermissionManager.PROSPECT_ADDITIONAL_DATA);
                    state.hasNegotiationPermission = pm.hasPermission(PermissionManager.PROSPECT_NEGOTIATIONS);
                    state.hasAttachmentsPermission = pm.hasPermission(PermissionManager.PROSPECT_FILES);

                    if (state.hasAttachmentsPermission) {
                        state.attachments = Attachment.getAttachmentsForAccount(accountId);
                    }

                    if (state.hasNegotiationPermission) {
                        List<CN_Product_Negotiation__c> negotiations = CN_Product_Negotiation__c.getNegotiationsByAccountId(accountId);
                        if (!negotiations.isEmpty()) {
                            state.negotiation = negotiations.get(0);
                        }
                    }

                    state.isMorningMeetingFinished = Morning_Meeting__c.isMorningMeetingFinished();
                    state.isProspectDataEnabled = isProspectDataEnabled(state.account);
                    state.isNegotiationEnabled = isNegotiationEnabled(state.account);
                    state.isConversionEnabled = isConversionEnabled(state);
                    state.isUnqualifiedEnabled = isUnqualifiedEnabled(state.account);
                    state.isCheckinButtonEnabled = isCheckinButtonEnabled(state.account);

                    return state;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        state -> {
                            this.state = state;
                            viewModel().setState(state);
                        },
                        error -> Log.e(TAG, "Error loading initial data", error)
                ));
    }

    private boolean isProspectDataEnabled(Account account) {
        String status = account.getProspectStatus();
        return ProspectStatus.CONTACTED.equals(status);
    }

    private boolean isNegotiationEnabled(Account account) {
        String status = account.getProspectStatus();
        return ProspectStatus.CONTACTED.equals(status);
    }

    private boolean isCheckinButtonEnabled(Account account) {
        String status = account.getProspectStatus();
        return !(ProspectStatus.SUBMITTED.equals(status) ||
                ProspectStatus.CONVERTED.equals(status) ||
                ProspectStatus.REJECTED.equals(status) ||
                ProspectStatus.UNQUALIFIED.equals(status));
    }


    private boolean isConversionEnabled(State state) {
        String status = state.account.getProspectStatus();
        return state.account.hasBasicData() &&
                state.account.hasAdditionalData() &&
                (state.hasNegotiationPermission &&
                        null != state.negotiation &&
                        state.negotiation.isCompleted())
                && ProspectStatus.CONTACTED.equals(status);
    }

    private boolean isUnqualifiedEnabled(Account account) {
        String status = account.getProspectStatus();
        return ProspectStatus.CONTACTED.equals(status);
    }

    public void onConvertClicked() {
        doConversion(state);
    }

    public void onUnqualifiedClicked() {
        viewModel().showUnqualifiedNote();
    }

    private void doConversion(State currentState) {
        if (currentState == null) return;

        addSubscription(Single.fromCallable(
                () -> {
                    Account account = currentState.account;

                    User user = User.getCurrentUser();
                    assertHasAssignedManager(user);
                    boolean needsUpdate = false;

                    if (!account.hasOwnerAssignedDate()) {
                        String ownerAssignedDate = account.getLastModifiedDate();
                        if (TextUtils.isEmpty(ownerAssignedDate)) {
                            long date = account.getSoupLastModifiedDate();
                            ownerAssignedDate = DateUtils.SERVER_DATE_TIME_FORMAT.format(new Date(date));
                        }

                        account.setOwnerAssignedDate(ownerAssignedDate);
                        needsUpdate = true;
                    }

                    if (!account.hasPocType()) {
                        account.setPocType("unknown");
                        needsUpdate = true;
                    }

                    if (needsUpdate) {
                        account.updateRecord();
                    }

                    account.changeProspectStatusConversionSent();
                    account.updateRecord();

                    return currentState;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        state -> {
                            this.state = state;
                            SyncUtils.TriggerRefresh(ABInBevApp.getAppContext());
                            viewModel().close();
                        },
                        error -> {
                            Log.e(TAG, "Error while converting prospect to account", error);
                            viewModel().showError(error.getMessage());
                        }
                ));
    }

    public void updateAccountToUnqualified() {
        addSubscription(Single.fromCallable(
                () -> {
                    Account account = state.account;
                    account.changeProspectStatusUnqualified();
                    account.updateRecord();
                    return state;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        state -> {
                            this.state = state;
                            SyncUtils.TriggerRefresh(ABInBevApp.getAppContext());
                            viewModel().setState(state);
                        },
                        error -> {
                            Log.e(TAG, "Error while converting prospect to account", error);
                            viewModel().showError(error.getMessage());
                        }
                ));
    }

    private String getAccountRecordType() {
        RecordType accountRecordType = RecordType.getByNameAndObjectType(AccountRecordType.ACCOUNT,
                AbInBevObjects.ACCOUNT);
        return accountRecordType == null ? null : accountRecordType.getId();
    }

    private void assertHasAssignedManager(User user) {
        if (!user.hasManager()) {
            Context context = ABInBevApp.getAppContext();
            String message = context.getString(R.string.error_your_user_has_to_have_manager_assigned);
            throw new IllegalStateException(message);
        }
    }
}
