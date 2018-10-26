package com.abinbev.dsa.ui.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Case;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.CaseFields;
import com.abinbev.dsa.utils.AbInBevConstants.CaseStatus;
import com.abinbev.dsa.utils.AbInBevConstants.RecordTypeName;
import com.abinbev.dsa.utils.AppScheduler;
import com.salesforce.androidsdk.accounts.UserAccountManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;

import org.json.JSONObject;

import rx.Single;
import rx.subscriptions.CompositeSubscription;

public class CasoEditPresenter extends CasoViewPresenter implements Presenter<CasoViewPresenter.ViewModel> {

    private static final String FIELD_ACCOUNT__R = "Account__r";

    public interface ViewModel extends CasoViewPresenter.ViewModel {
        void setNewCaso(Case caso, boolean useDetailsLayout);
        void newCasoCreate(String tempId);
        void onCaseSaved(boolean success);
        void onError(Throwable e);
    }

    private static final String TAG = CasoEditPresenter.class.getSimpleName();
    private ViewModel viewModel;
    private CompositeSubscription compositeSubscription;


    public CasoEditPresenter(String casoId) {
        super(casoId, false /* refresh on start */);
        compositeSubscription = new CompositeSubscription();
    }

    public void setViewModel(ViewModel viewModel) {
        super.setViewModel(viewModel);
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        super.start();
        compositeSubscription.clear();
    }

    @Override
    public void stop() {
        super.stop();
        compositeSubscription.clear();
        viewModel = null;
    }

    public void saveNewCase(final Case caso) {

        if (caso == null) {
            throw new IllegalArgumentException(("A case must be provided"));
        }

        compositeSubscription.add(Single.fromCallable(() -> {
                    caso.remove(FIELD_ACCOUNT__R); // Don't save related account.
                    return Case.createNewCase(caso);
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        tempId -> {
                            if (viewModel != null) {
                                viewModel.newCasoCreate(tempId);
                            }
                        },
                        error -> {
                            Log.e(TAG, "Error creating new case: ", error);
                            if (viewModel != null) {
                                viewModel.onError(error);
                            }
                        }));
    }

    public void saveUpdatedCase(Case caso, JSONObject updatedData) {
        compositeSubscription.add(Single.fromCallable(
                () -> {
                    RecordType accountChangeRecordType = RecordType.getByName(RecordTypeName.ACCOUNT_CHANGE_REQUEST);

                    // Don't save related account.
                    caso.remove(FIELD_ACCOUNT__R);
                    updatedData.remove(FIELD_ACCOUNT__R);

                    if (accountChangeRecordType != null
                            && accountChangeRecordType.getId().equals(caso.getRecordTypeId())) {
                        updatedData.put(CaseFields.STATUS, CaseStatus.SUBMITTED);
                    }

                    return DataManagerFactory.getDataManager()
                            .updateRecord(AbInBevConstants.AbInBevObjects.CASE, casoId, updatedData);
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                    success -> {
                        if (viewModel != null) {
                            viewModel.onCaseSaved(success);
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error updating case: ", error);
                        if (viewModel != null) {
                            viewModel.onError(error);
                        }
                    }
                ));
    }

    public void createNewCase(final String accountId, final String recordName, final String assetId) {
        if (TextUtils.isEmpty(accountId) || TextUtils.isEmpty(recordName)) {
            throw new IllegalArgumentException(("An account id and record type name must be provided"));
        }

        compositeSubscription.add(Single.fromCallable(
                () -> {
                    CreateCaseResult createCaseResult = new CreateCaseResult();

                    Case caso = new Case(accountId, assetId);
                    RecordType recordType = RecordType.getByNameAndObjectType(recordName, AbInBevConstants.AbInBevObjects.CASE);
                    if (recordType == null || TextUtils.isEmpty(recordType.getId())) {
                        throw new Exception(ABInBevApp.getAppContext().getString(R.string.no_record_type));
                    }
                    caso.setRecordTypeId(recordType.getId());

                    String userId = UserAccountManager.getInstance()
                            .getStoredUserId();
                    caso.setOwnerId(userId);

                    if (RecordTypeName.ACCOUNT_CHANGE_REQUEST.equals(recordName)) {
                        Account account = Account.getById(accountId);
                        caso.setValueForKey(FIELD_ACCOUNT__R, account.toJson());
                        caso.setStatus(CaseStatus.SUBMITTED);
                        createCaseResult.useDetailsLayout = true;
                    }

                    createCaseResult.caso = caso;

                    return createCaseResult;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        createCaseResult -> viewModel.setNewCaso(createCaseResult.caso,
                                createCaseResult.useDetailsLayout),
                        error -> {
                            viewModel.onError(error);
                            Log.e(TAG, "Error creating new case: ", error);
                        }
                ));
    }

    private static class CreateCaseResult {
        public Case caso;
        public boolean useDetailsLayout;
    }
}
