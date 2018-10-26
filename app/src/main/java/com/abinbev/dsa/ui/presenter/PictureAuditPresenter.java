package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.SyncListener;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Account_Asset__c;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.Case;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.Morning_Meeting__c;
import com.abinbev.dsa.model.Picture_Audit_Status__c;
import com.abinbev.dsa.model.SurveyQuestionResponse__c;
import com.abinbev.dsa.model.SurveyTaker__c;
import com.abinbev.dsa.model.Survey_Question__c;
import com.abinbev.dsa.model.Survey__c;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.RegisteredReceiver;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Single;

import static org.apache.commons.lang3.StringUtils.startsWith;

public class PictureAuditPresenter extends AbstractRxPresenter<PictureAuditPresenter.ViewModel> implements SyncListener {

    private static final String TAG = PictureAuditPresenter.class.getSimpleName();

    private final RegisteredReceiver<SyncListener> syncReceiver;

    private final Context context;

    public static class RejectedFile {
        public String caseName;
        public String accountAssetName;
        public String surveyName;
        public String surveyQuestion;
        public String accountName;
        public String eventName;
        public String type;
        public String fileName;
        public String pictureAuditStatusId;
    }

    public interface ViewModel {
        void setAuditStatuses(List<RejectedFile> rejectedFiles);
    }

//    void openAttachment(Attachment attachment) {
//
//        AttachmentUtils.openAttachment(attachment, viewModel().getActivity(), accountId);
//    }

    public PictureAuditPresenter(RegisteredReceiver<SyncListener> syncReceiver) {
        this.syncReceiver = syncReceiver;
        this.context = ABInBevApp.getAppContext();
    }

    @Override
    public void start() {
        super.start();
        syncReceiver.register(context, this);
        loadData();
    }

    private void loadData() {
        clearSubscriptions();
        addSubscription(Single.fromCallable(Picture_Audit_Status__c::getRejected)
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        auditStatuses -> viewModel().setAuditStatuses(convert(auditStatuses)),
                        error -> Log.w(TAG, "Error while loading Picture Audit Statuses", error)
                ));
    }

    private List<RejectedFile> convert(List<Picture_Audit_Status__c> auditStatuses) {
        List<RejectedFile> rejectedFiles = new ArrayList<>();

        for (Picture_Audit_Status__c status : auditStatuses) {
            rejectedFiles.add(createFrom(status));
        }

        return rejectedFiles;
    }

    private RejectedFile createFrom(Picture_Audit_Status__c pas) {
        RejectedFile rejectedPicture = new RejectedFile();
        rejectedPicture.pictureAuditStatusId = pas.getName();

        String parentObject = ManifestUtils.removeNamespaceFromObject(pas.getParentObjectName(), context);

        if (AbInBevObjects.CASE.equals(parentObject)) {
            addCaseData(rejectedPicture, pas);
        }
        else if (AbInBevObjects.ACCOUNT_ASSET_C.equals(parentObject)) {
            addAccountAssetData(rejectedPicture, pas);
        }
        else if (AbInBevObjects.SURVEY_Question_Response.equals(parentObject)) {
            addSurveyResponseData(rejectedPicture, pas);
        }
        else if (AbInBevObjects.EVENT.equals(parentObject)) {
            addEventData(rejectedPicture, pas);
        }
        else if (AbInBevObjects.ACCOUNT.equals(parentObject)) {
            addAccountData(rejectedPicture, pas);
        }
        else {
            rejectedPicture.fileName = pas.getPictureReference();
        }

        return rejectedPicture;
    }

    private void addSurveyResponseData(RejectedFile rejectedPicture, Picture_Audit_Status__c auditStatus) {
        String responseId = auditStatus.getParentId();
        rejectedPicture.type = getString(R.string.rejected_file_type_survey_response);

        SurveyQuestionResponse__c surveyResponse = SurveyQuestionResponse__c.getById(responseId);

        if (surveyResponse != null) {
            String surveyQuestionId = surveyResponse.getSurveyQuestionId();
            Survey_Question__c surveyQuestion = Survey_Question__c.getById(surveyQuestionId);
            if (surveyQuestion != null) {
                rejectedPicture.surveyQuestion = surveyQuestion.getQuestion();

                String surveyId = surveyQuestion.getSurveyId();
                Survey__c survey = Survey__c.getById(surveyId);
                if (survey != null) {
                    rejectedPicture.surveyName = survey.getName();
                }
                else {
                    rejectedPicture.surveyName = surveyId;
                }

                String surveyTakerId = surveyResponse.getSurveyTakerId();
                SurveyTaker__c surveyTaker = SurveyTaker__c.getById(surveyTakerId);
                if (surveyTaker != null) {
                    String accountId = surveyTaker.getAccountId();
                    Account account = Account.getById(accountId);

                    if (account != null) {
                        rejectedPicture.accountName = account.getName();
                    }
                    else {
                        rejectedPicture.accountName = accountId;
                    }
                }
            }
            else {
                rejectedPicture.surveyQuestion = surveyQuestionId;
            }
        }
    }

    private void addCaseData(RejectedFile rejectedPicture, Picture_Audit_Status__c auditStatus) {
        String caseId = auditStatus.getParentId();

        rejectedPicture.type = getString(R.string.rejected_file_type_case_attachment);
        rejectedPicture.fileName = auditStatus.getPictureReference();

        Case caso = Case.getById(caseId);
        if (caso != null) {
            rejectedPicture.caseName = caso.getName();
            Account account = caso.getAccount();
            if (account != null) {
                rejectedPicture.accountName = account.getName();
            }
            else {
                rejectedPicture.accountName = caso.getAccountId();
            }
        }
        else {
            rejectedPicture.caseName = caseId;
        }
    }

    private void addAccountAssetData(RejectedFile rejectedPicture, Picture_Audit_Status__c auditStatus) {
        String assetId = auditStatus.getParentId();

        rejectedPicture.type = getString(R.string.rejected_file_type_asset_attachment);
        rejectedPicture.fileName = auditStatus.getPictureReference();

        Account_Asset__c asset = Account_Asset__c.getById(assetId);
        if (asset != null) {
            rejectedPicture.accountAssetName = asset.getName();

            String accountId = asset.getAccountId();
            Account account = Account.getById(accountId);
            if (account != null) {
                rejectedPicture.accountName = account.getName();
            }
            else {
                rejectedPicture.accountName = accountId;
            }
        }
        else {
            rejectedPicture.accountAssetName = assetId;
        }
    }

    private void addEventData(RejectedFile rejectedPicture, Picture_Audit_Status__c auditStatus) {
        String fileName = auditStatus.getPictureReference();

        boolean hasAccount = false;
        boolean hasMorningMeeting = false;
        boolean isCheckInPhoto = startsWith(fileName, Attachment.getCheckInPhotoFileName());
        boolean isCheckOutPhoto = startsWith(fileName, Attachment.getCheckOutPhotoFileName());

        Event event = Event.getById(auditStatus.getParentId());
        if (event != null) {
            Account account = Account.getById(event.getWhatId());

            if (account != null) {
                hasAccount = true;
                rejectedPicture.accountName = account.getName();
            }
            else {
                Morning_Meeting__c morningMeeting = Morning_Meeting__c.getById(event.getWhatId());
                hasMorningMeeting = morningMeeting != null;
            }
        }


        if (isCheckInPhoto) {
            if (hasAccount) {
                rejectedPicture.type = getString(R.string.rejected_file_type_account_check_in_photo);
            }
            else if (hasMorningMeeting) {
                rejectedPicture.type = getString(R.string.rejected_file_type_morning_meeting_check_in_photo);
            }
            else {
                rejectedPicture.type = getString(R.string.rejected_file_type_check_in_photo);
            }
        }
        else if (isCheckOutPhoto) {
            if (hasAccount) {
                rejectedPicture.type = getString(R.string.rejected_file_type_account_check_out_photo);
            }
            else if (hasMorningMeeting) {
                rejectedPicture.type = getString(R.string.rejected_file_type_morning_meeting_check_out_photo);
            }
            else {
                rejectedPicture.type = getString(R.string.rejected_file_type_check_out_photo);
            }
        }
        else {
            rejectedPicture.type = getString(R.string.rejected_file_type_event_attachment);
        }
    }

    private void addAccountData(RejectedFile rejectedPicture, Picture_Audit_Status__c auditStatus) {
        String accountId = auditStatus.getParentId();
        rejectedPicture.type = getString(R.string.rejected_file_type_account_attachment);

        Account account = Account.getById(accountId);
        if (account != null) {
            rejectedPicture.accountName = account.getName();

            String fileName = auditStatus.getPictureReference();
            if (startsWith(fileName, Attachment.getAccountPhotoFileName())) {
                rejectedPicture.type = getString(R.string.rejected_file_type_account_photo);
            }
            else {
                rejectedPicture.fileName = fileName;
            }
        }
        else {
            rejectedPicture.accountName = accountId;
        }
    }

    private String getString(int resId) {
        return context.getString(resId);
    }

    @Override
    public void stop() {
        syncReceiver.unregister(context, this);
        super.stop();
    }


    @Override
    public void onSyncCompleted() {
        loadData();
    }

    @Override
    public void onSyncError(String message) {
        Log.e(TAG, "Sync error: " + message);
    }

    @Override
    public void onSyncFailure(String message) {
        Log.e(TAG, "Sync failure: " + message);
    }
}
