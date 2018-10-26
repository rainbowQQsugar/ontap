package com.abinbev.dsa.ui.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.model.Note;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.crashreport.CrashReportManagerProvider;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;

import org.json.JSONObject;

import java.util.List;

import rx.Single;

public class NewPromotionEditPresenter extends AbstractRxPresenter<NewPromotionEditPresenter.ViewModel> {

    private static final String TAG = "NewPromotionEditPresent";

    public interface ViewModel {
        void setNegotiation(CN_Product_Negotiation__c negotiation);
        void setAccount(Account account);
        void setNotes(List<Note> notes);
        void showNegotiationDoesNotExist(String negotiationId);
        void closeOnSuccess();
    }

    private final String negotiationId;
    private String recordType;
    private final String accountId;

    private boolean isFirstStart = true;

    public NewPromotionEditPresenter(String negotiationId, String accountId, String recordType) {
        super();
        this.negotiationId = negotiationId;
        this.accountId = accountId;
        this.recordType = recordType;
    }

    @Override
    public void start() {
        super.start();
        getNotes();
        loadAccount();

        if (isFirstStart) {
            isFirstStart = false;

            if (TextUtils.isEmpty(negotiationId)) {
                createNegotiation();
            }
            else {
                loadNegotiation();
            }
        }
    }

    public void getNotes() {
        addSubscription(Single.fromCallable(
                () -> Note.getLatestNotesByParentId(negotiationId))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        notes -> viewModel().setNotes(notes),
                        error -> Log.e(TAG, "Error getting all notes: ", error)
                ));

    }

    private void loadNegotiation() {
        addSubscription(Single.fromCallable(
                () -> CN_Product_Negotiation__c.getById(negotiationId))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        negotiation -> {
                            if (negotiation == null) {
                                logException("Couldn't load CN_Product_Negotiation__c with id: " + negotiationId);
                                viewModel().showNegotiationDoesNotExist(negotiationId);
                            }
                            else {
                                viewModel().setNegotiation(negotiation);
                            }
                        },
                        error -> Log.e(TAG, "Error loading negotiation: ", error)
                ));
    }

    private void loadAccount() {
        addSubscription(Single.fromCallable(
                () -> Account.getById(accountId))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        account -> viewModel().setAccount(account),
                        error -> Log.e(TAG, "Error loading account: ", error)
                ));
    }

    private void createNegotiation() {
        addSubscription(Single.fromCallable(
                () -> {
                    CN_Product_Negotiation__c negotiation = new CN_Product_Negotiation__c(new JSONObject());
                    negotiation.setAccount(accountId);
                    if (negotiation.getStatus().isEmpty())
                        negotiation.setStatus(CN_Product_Negotiation__c.STATUS_IN_NEGOTIATION);
                    negotiation.setRecordTypeId(recordType);
                    return negotiation;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        negotiation -> viewModel().setNegotiation(negotiation),
                        error -> Log.e(TAG, "Error loading negotiation: ", error)
                ));
    }

    public void saveNegotiation(CN_Product_Negotiation__c negotiation) {
        addSubscription(Single.fromCallable(
                () -> {
                    negotiation.upsertRecord();

                    Account account = Account.getById(accountId);
//                    account.changeProspectStatusNegotiationUpdated();
                    account.updateRecord();

                    SyncUtils.TriggerRefresh(ABInBevApp.getAppContext());

                    return negotiation.getId();
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        newId -> viewModel().closeOnSuccess(),
                        error -> Log.e(TAG, "Error saving negotiation: ", error)
                ));
    }

    private static void logException(String message) {
        CrashReportManagerProvider.getInstance().logException(new Exception(message));
    }
}
