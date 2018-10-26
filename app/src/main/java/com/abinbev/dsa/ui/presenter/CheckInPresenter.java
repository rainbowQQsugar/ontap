package com.abinbev.dsa.ui.presenter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.abinbev.dsa.activity.AccountOverviewActivity;
import com.abinbev.dsa.activity.ProspectDetailActivity;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.utils.AbInBevConstants.AccountRecordType;
import com.abinbev.dsa.utils.AppScheduler;
import java.util.List;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by wandersonblough on 2/10/16.
 */
public class CheckInPresenter implements Presenter<CheckInPresenter.ViewModel> {

    private static final String TAG = CheckInPresenter.class.getSimpleName();

    public interface ViewModel {
        void showCheckOutPrompt(Account account);
        void onCheckOutFinished();
    }

    private Activity activity;
    private ViewModel viewModel;
    private CompositeSubscription subscription;
    private String currentAccountId;
    private Event checkedInEvent;
    private Account checkedInAccount;

    public CheckInPresenter(Activity activity, String currentAccountId) {
        super();
        this.subscription = new CompositeSubscription();
        this.activity = activity;
        this.currentAccountId = currentAccountId;
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        subscription.clear();
        subscription.add(Observable.<Event>create(
                subscriber -> {
                    // Find newest event.
                    List<Event> events = Event.getAllCheckedInVisits();
                    if (events.isEmpty()) {
                        subscriber.onNext(null);
                    } else {
                        checkedInEvent = events.get(0);
                        subscriber.onNext(events.get(0));
                    }
                    subscriber.onCompleted();
                })
                .flatMap(event -> Observable.<Account>create(
                        subscriber -> {
                            // Get account from newest event.
                            Account account = null;

                            if (event != null) {
                                account = Account.getById(event.getWhatId());
                            }
                            subscriber.onNext(account);
                            subscriber.onCompleted();
                        }))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        account -> {
                            checkedInAccount = account;
                            if (checkedInAccount != null) {
                                if (!checkedInAccount.getId().equals(currentAccountId)) {
                                    viewModel.showCheckOutPrompt(checkedInAccount);
                                }
                            }
                        },
                        error -> Log.e(TAG, "onError: ", error)
                ));
    }

    public void setCurrentAccountId(String currentAccountId) {
        this.currentAccountId = currentAccountId;
    }

    public void onCheckOutClicked() {
        if (checkedInAccount == null) return;

        final String checkedInAccountId = checkedInAccount.getId();
        final String checkedInAccountRecordType = checkedInAccount.getRecordTypeId();

        subscription.clear();
        subscription.add(Observable.<Intent>create(
                subscriber -> {

                    RecordType recordType = RecordType.getById(checkedInAccountRecordType);
                    final boolean isProspect = recordType != null &&
                            AccountRecordType.PROSPECT.equals(recordType.getName());

                    Intent resultIntent = null;
                    if (isProspect) {
                        resultIntent = new Intent(activity, ProspectDetailActivity.class);
                        resultIntent.putExtra(ProspectDetailActivity.ACCOUNT_ID_EXTRA, checkedInAccountId);
                        resultIntent.putExtra(ProspectDetailActivity.IS_CHECKOUT_PROMPT_EXTRA, true);
                    } else {
                        resultIntent = new Intent(activity, AccountOverviewActivity.class);
                        resultIntent.putExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA, checkedInAccountId);
                        resultIntent.putExtra(AccountOverviewActivity.IS_CHECKOUT_PROMPT_EXTRA, true);
                    }

                    subscriber.onNext(resultIntent);
                    subscriber.onCompleted();
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        resultIntent -> {
                            viewModel.onCheckOutFinished();
                            if (resultIntent != null) {
                                activity.startActivity(resultIntent);
                            }
                        },
                        error -> Log.e(TAG, "onError: ", error)
                ));
    }

    @Override
    public void stop() {
        viewModel = null;
        subscription.clear();
    }
}
