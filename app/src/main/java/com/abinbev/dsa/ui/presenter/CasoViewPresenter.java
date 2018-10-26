package com.abinbev.dsa.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.bus.event.AttachmentEvent;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.Case;
import com.abinbev.dsa.model.Caso;
import com.abinbev.dsa.model.Comentario_caso_force__c;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.utils.AbInBevConstants.CaseStatus;
import com.abinbev.dsa.utils.AbInBevConstants.RecordTypeName;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.AttachmentUtils;
import com.abinbev.dsa.utils.ContentUtils;
import com.salesforce.androidsdk.accounts.UserAccountManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class CasoViewPresenter implements Presenter<CasoViewPresenter.ViewModel> {

    private static final String TAG = CasoViewPresenter.class.getSimpleName();

    public interface ViewModel {
        void setCaso(Case caso, String recordTypeName);
        void setComments(List<Comentario_caso_force__c> comments);
        void commentSaved(boolean success);
        void setAttachments(List<Attachment> attachments);
        void setEditable(boolean isEditable);
    }

    @Inject
    Bus eventBus;

    private ViewModel viewModel;
    protected String casoId;
    private CompositeSubscription compositeSubscription;
    private boolean refreshOnStart;
    private boolean isFirstStart = true;

    private ClientManager clientManager;

    private OkHttpClient okHttpClient;

    public CasoViewPresenter(String casoId, boolean refreshOnStart) {
        super();
        this.casoId = casoId;
        this.compositeSubscription = new CompositeSubscription();

        ABInBevApp appContext = (ABInBevApp) ABInBevApp.getAppContext();
        this.clientManager = appContext.createClientManager();
        this.okHttpClient = appContext.createOkHttpClient(clientManager);

        appContext.getAppComponent().inject(this);
        this.eventBus.register(this);
        this.refreshOnStart = refreshOnStart;
    }

    public CasoViewPresenter(String casoId) {
        this(casoId, true);
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        if (casoId != null) {
            compositeSubscription.clear();
            if (refreshOnStart || isFirstStart) {
                fetchCase();
                fetchComments();
                fetchAttachments();
                isFirstStart = false;
            }
        }
    }

    private void fetchCase() {
        compositeSubscription.add(Single.fromCallable(
                () -> {
                    Case caso = Case.getById(casoId);
                    RecordType recordType = RecordType.getById(caso.getRecordTypeId());
                    String recordTypeName = recordType == null ? null : recordType.getName();

                    return new Pair<>(caso, recordTypeName);
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        result -> {
                            Case caso = result.first;
                            String recordTypeName = result.second;
                            viewModel.setCaso(caso, recordTypeName);

                            if (Case.CasosStates.IN_APPROVAL.equalsIgnoreCase(caso.getStatus())) {
                                viewModel.setEditable(false);
                            } else {
                                String userId = UserAccountManager.getInstance().getStoredUserId();
                                if (caso.getOwnerId() != null && caso.getOwnerId().equals(userId)) {
                                    if (RecordTypeName.ACCOUNT_CHANGE_REQUEST.equals(recordTypeName)
                                            && CaseStatus.SUBMITTED.equals(caso.getStatus())) {

                                        viewModel.setEditable(false);
                                    }
                                    else {
                                        viewModel.setEditable(true);
                                    }
                                } else {
                                    viewModel.setEditable(false);
                                }
                            }
                        },
                        error -> Log.e(TAG, "Error fetching Caso: " + casoId, error)
                ));
    }

    public void fetchComments() {
        compositeSubscription.add(Observable.just(Comentario_caso_force__c.getTwoNewestCommentsForCase(casoId))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Comentario_caso_force__c>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error fetching comments for caso: " + casoId, e);
                    }

                    @Override
                    public void onNext(List<Comentario_caso_force__c> comentario_caso_force__cs) {
                        viewModel.setComments(comentario_caso_force__cs);
                    }
                }));
    }

    public void saveComment(final String comment) {
        final String userId = UserAccountManager.getInstance().getStoredUserId();
        compositeSubscription.add(Observable.just(Comentario_caso_force__c.createComment(comment, casoId, userId))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Comentario_caso_force__c>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error saving comment.", e);
                    }

                    @Override
                    public void onNext(Comentario_caso_force__c comentario_caso_force__c) {
                        viewModel.commentSaved(true);
                    }
                }));
    }

    public void fetchAttachments() {
        compositeSubscription.add(Observable.create(new Observable.OnSubscribe<List<Attachment>>() {
            @Override
            public void call(Subscriber<? super List<Attachment>> subscriber) {
                Caso caso = Caso.getCasoById(casoId);
                List<Attachment> attachments = new ArrayList<>();
                if (caso != null && caso.isAssetCase()) {
                    Log.d(TAG, "Fetching attachments for parent caso: " + caso.getParentId());
                    attachments.addAll(Attachment.getAttachmentsForCase(caso.getParentId()));
                } else if (caso != null) {
                    Log.e(TAG, "Fetching attachments for caso: " + casoId);
                    attachments.addAll(Attachment.getAttachmentsForCase(casoId));
                }
                subscriber.onNext(attachments);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Attachment>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error fetching attachments for caso: " + casoId, e);
                    }

                    @Override
                    public void onNext(List<Attachment> attachments) {
                        viewModel.setAttachments(attachments);
                    }
                }));
    }

    public void deleteAttachment(final String attachmentId, final String attachmentName) {

        //TODO: if no network connection, need to queue up with Tape, or perhaps remove the existing sync job...
        compositeSubscription.add(Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                if (TextUtils.isEmpty(attachmentId)) { //won't have id if attachment has not been synced
                    Attachment.deleteUnsyncedCaseAttachment(attachmentName, casoId);
                } else {
                    Attachment.deleteAttachment(attachmentId);
                }
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error deleting attachment: ", e);
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        fetchAttachments();
                    }
                }));
    }

    public void onAttachmentClicked(Activity activity, Attachment attachment) {
        if (attachment.isCaseFileDownloaded(activity, casoId)) {
            openAttachment(activity, attachment);
        }
        else {
            downloadAttachment(activity, attachment);
        }
    }

    private void openAttachment(Activity activity, Attachment attachment) {
        if (ContentUtils.isNull_OR_Blank(attachment.getId())) {
            AttachmentUtils.openUnsyncedCaseAttachment(attachment, activity, casoId);
        } else {
            AttachmentUtils.openAttachment(attachment, activity, casoId);
        }
    }

    private void downloadAttachment(final Activity activity, final Attachment attachment) {
        showToast(activity, R.string.toast_downloading_attachment, Toast.LENGTH_SHORT);
        attachment.invalidateFilePath();

        final Context appContext = activity.getApplicationContext();

        compositeSubscription.add(Observable.create(
                new Observable.OnSubscribe<Attachment>() {
                    @Override
                    public void call(Subscriber<? super Attachment> subscriber) {
                        try {
                            AttachmentUtils.downloadAttachmentFile(clientManager, okHttpClient,
                                    appContext, attachment);
                            subscriber.onNext(attachment);
                            subscriber.onCompleted();
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Attachment>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        showToast(activity, R.string.toast_attachment_download_failed, Toast.LENGTH_SHORT);
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onNext(Attachment result) {
                        openAttachment(activity, result);
                    }
                }));
    }

    private void showToast(Context context, int stringRes, int length) {
        Toast.makeText(context, stringRes, length).show();
    }

    @Override
    public void stop() {
        compositeSubscription.clear();
        viewModel = null;
        eventBus.unregister(this);
    }

    @Subscribe
    public void onAttachmentSaved(final AttachmentEvent.AttachmentSavedEvent attachmentSavedEvent) {
        Log.d(TAG, "onAttachmentSaved CasoViewPresenter success: " + attachmentSavedEvent.isSuccess());
        if (attachmentSavedEvent.isSuccess()) {
            fetchAttachments();
        }
    }

    public String getCasoId() {
        return casoId;
    }

    public void setCasoId(String casoId) {
        this.casoId = casoId;
    }
}
