package com.abinbev.dsa.ui.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AccountRecordType;
import com.abinbev.dsa.utils.AppScheduler;

import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

public class BasicDataDynamicLayoutPresenter extends AbstractRxPresenter<BasicDataDynamicLayoutPresenter.ViewModel> {

    public static final String TAG = BasicDataDynamicLayoutPresenter.class.getSimpleName();

    private String accountId;

    private CompositeSubscription subscription;

    public interface ViewModel {
        void createUpdateAccountSuccess(String accountId);

        void updateAccountInfo(Account account);

        void setAccountPhoto(Attachment attachment);

        void setAccountLicensePhoto(Attachment attachment);
    }

    private boolean isFirstStart = true;

    public BasicDataDynamicLayoutPresenter() {
        super();
        subscription = new CompositeSubscription();
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public void start() {
        super.start();
        subscription.clear();
        if (isFirstStart) {
            getAccount();
            if(!TextUtils.isEmpty(accountId)) {
                getAccountPhoto();
                getAccountLicensePhoto();
            }
            isFirstStart = false;
        }
    }

    @Override
    public void stop() {
        super.stop();
        subscription.clear();
    }

    public void createAccount(final Account account) {
        clearSubscriptions();
        addSubscription(Single.fromCallable(account::createRecord)
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        accountId -> viewModel().createUpdateAccountSuccess(accountId),
                        error -> Log.e(TAG, "Error creating account: ", error)
                ));
    }

    public void updateAccount(final Account account) {
        clearSubscriptions();
        addSubscription(Single.fromCallable(
                () -> {
                    boolean success = account.updateRecord();
                    if (!success) {
                        Log.e(TAG, "Account update failed!!");
                        return null;
                    } else {
                        return account.getId();
                    }
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        accountId -> viewModel().createUpdateAccountSuccess(accountId),
                        error -> Log.e(TAG, "Error updating account: ", error)
                ));
    }

    public void getAccount() {
        addSubscription(Single.fromCallable(
                () -> {
                    Account account = Account.getById(accountId);

                    // Create new account if it doesn't exist.
                    if (account == null) {
                        account = new Account();
                        RecordType recordType = RecordType.getByName(AccountRecordType.PROSPECT);
                        account.setRecordTypeId(recordType.getId());
                        account.setProspectStatus(AbInBevConstants.ProspectStatus.OPEN);
                        account.setLeadSource(AbInBevConstants.ProspectLeadSource.BDR_SOURCE);
                    }

                    String businessUnit = account.getBusinessUnit();
                    if (TextUtils.isEmpty(businessUnit)) {
                        User user = User.getCurrentUser();
                        businessUnit = user.getBusinessUnit();
                        account.setBusinessUnit(businessUnit);
                    }

                    if (!TextUtils.isEmpty(account.getId())) {
                        getAccountPhoto();
                        getAccountLicensePhoto();
                    }

                    String[] fields = {
                            AbInBevConstants.AccountFields.CN_LEAD_SOURCE__C,
                            AbInBevConstants.AccountFields.PROSPECT_STATUS,
                    };
                    TranslatableSFBaseObject.addTranslations(account, "Account",fields);
                    return account;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        account -> viewModel().updateAccountInfo(account),
                        error -> Log.e(TAG, "Error fetching account: ", error)
                ));
    }

    public void getAccountPhoto() {
        this.subscription.add(Observable.create((Observable.OnSubscribe<Attachment>) subscriber -> {
            Attachment attachment = Attachment.getAccountPhotoAttachment(accountId);
            subscriber.onNext(attachment);
            subscriber.onCompleted();
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Attachment>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error retrieving account photo: ", e);
                    }

                    @Override
                    public void onNext(Attachment attachment) {
                        viewModel().setAccountPhoto(attachment);
                    }
                }));
    }

    public void getAccountLicensePhoto() {
        this.subscription.add(Observable.create((Observable.OnSubscribe<Attachment>) subscriber -> {
            Attachment attachment = Attachment.getAccountLicensePhotoAttachment(accountId);
            subscriber.onNext(attachment);
            subscriber.onCompleted();
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Attachment>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error retrieving account photo: ", e);
                    }

                    @Override
                    public void onNext(Attachment attachment) {
                        viewModel().setAccountLicensePhoto(attachment);
                    }
                }));
    }

}
