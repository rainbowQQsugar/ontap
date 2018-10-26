package com.abinbev.dsa.model.checkoutRules;

import android.content.Context;
import android.util.Log;

import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.MandatoryTaskDetail;
import com.abinbev.dsa.model.MandatoryTaskGroup;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.utils.DateUtils;

import java.text.ParseException;
import java.util.List;

import rx.Observable;

/**
 * Created by Adam Chodera on 29.06.2017.
 */
public abstract class CheckoutRule {

    private static final String TAG = "CheckoutRule";

    public enum CheckoutRuleType {
        NOTES,
        TASKS,
        SURVEYS,
        NEGOTIATIONS,
        UNDEFINED,
        VISIT_NOTE,
        PRICE_COLLECTION,
        ASSET_TRACKING
    }

    private String infoForUser;
    private CheckoutRuleType checkoutRuleType;
    private boolean isCheckoutStep;

    public static CheckoutRule createFrom(Account account, Event event, MandatoryTaskDetail detail) {
        CheckoutRule checkoutRule;

        if ("Survey".equals(detail.getTaskType())) {
            checkoutRule = new SurveyCheckoutRule(account, event, detail);
            checkoutRule.setCheckoutRuleType(CheckoutRuleType.SURVEYS);
        }
        else if ("Visit Note".equals(detail.getTaskType())) {
            checkoutRule = new VisitNoteCheckoutRule(account, event, detail);
            checkoutRule.setCheckoutRuleType(CheckoutRuleType.VISIT_NOTE);
        }
        else if ("Price Collection".equals(detail.getTaskType())) {
            checkoutRule = new PriceCollectionCheckoutRule(account, event, detail);
            checkoutRule.setCheckoutRuleType(CheckoutRuleType.PRICE_COLLECTION);
        }
        else if ("Asset Tracking".equals(detail.getTaskType())) {
            checkoutRule = new AssetsTrackingCheckoutRule(account, event, detail);
            checkoutRule.setCheckoutRuleType(CheckoutRuleType.ASSET_TRACKING);
        }
        else {
            checkoutRule = new UndefinedCheckoutRule();
            checkoutRule.setCheckoutRuleType(CheckoutRuleType.UNDEFINED);
        }

        checkoutRule.setInfoForUser(detail.getErrorMessage());
        return checkoutRule;
    }

    CheckoutRule() { }

    final void setInfoForUser(String infoForUser) {
        this.infoForUser = infoForUser;
    }

    final void setCheckoutRuleType(CheckoutRuleType checkoutRuleType) {
        this.checkoutRuleType = checkoutRuleType;
    }

    public final String getInfoForUser() {
        return infoForUser;
    }

    public final CheckoutRuleType getCheckoutRuleType() {
        return checkoutRuleType;
    }

    public abstract boolean isFulfilled();

    /** Checkout step has to be done during Checkout, not before it. */
    public final boolean isCheckoutStep() {
        return isCheckoutStep;
    }

    protected void setIsCheckoutStep(boolean checkoutStep) {
        isCheckoutStep = checkoutStep;
    }

    public abstract void openScreen(Context context);

    private static Observable<CheckoutRule> getLocalRules(Account account, User user) {
        return Observable.just(new AccountPhotoCheckoutRule(account));
    }

    private static Observable<CheckoutRule> getNotFulfilledRules(Account account, User user, Event event) {
        return Observable.fromCallable(() -> Event.isFirstVisitInMonth(account.getId()))
                // Get mandatory task group.
                .flatMap(isFirstInMonth -> MandatoryTaskGroup.getBy(account, user, isFirstInMonth))
                // Get mandatory task detail.
                .flatMap(MandatoryTaskDetail::getByGroup)
                // Convert list to stream.
                .flatMap(Observable::from)
                // Convert to CheckoutRules.
                .map(mandatoryTaskDetail -> CheckoutRule.createFrom(account, event, mandatoryTaskDetail))
                // Include local CheckoutRules.
                .mergeWith(getLocalRules(account, user))
                // Take not fulfilled rules.
                .filter(CheckoutRule::isNotFulfilled);
    }

    public static Observable<List<CheckoutRule>> getNotFulfilledBeforeCheckOut(Account account, User user, Event event) {
        return getNotFulfilledRules(account, user, event)
                // Get rules that has to be done before check-out.
                .filter(CheckoutRule::isNotCheckoutStep)
                // Convert to list.
                .toList();
    }

    public static Observable<CheckoutRule> getNotFulfilledVisitNoteRule(Account account, User user, Event event) {
        return getNotFulfilledRules(account, user, event)
                // Get visit note rule.
                .filter(CheckoutRule::isVisitNoteRule)
                // Limit it to the first item only.
                .takeFirst(checkoutRule -> true);
    }

    public static Observable<CheckoutRule> getNotFulfilledAccountPhotoRule(Account account, User user, Event event) {
        return getNotFulfilledRules(account, user, event)
                // Get account photo rule.
                .filter(CheckoutRule::isAccountPhotoRule)
                // Limit it to the first item only.
                .takeFirst(checkoutRule -> true);
    }

    private static boolean isNotFulfilled(CheckoutRule checkoutRule) {
        return !isFulfilled(checkoutRule);
    }

    private static boolean isNotCheckoutStep(CheckoutRule checkoutRule) {
        return !checkoutRule.isCheckoutStep();
    }

    private static boolean isFulfilled(CheckoutRule checkoutRule) {
        boolean isFulfilled = checkoutRule.isFulfilled();
        Log.i(TAG, "Checkout rule: " + checkoutRule.getClass().getSimpleName() + " fulfilled: " + isFulfilled);
        return isFulfilled;
    }

    private static boolean isVisitNoteRule(CheckoutRule checkoutRule) {
        return checkoutRule instanceof VisitNoteCheckoutRule;
    }

    private static boolean isAccountPhotoRule(CheckoutRule checkoutRule) {
        return checkoutRule instanceof AccountPhotoCheckoutRule;
    }

    protected static long parseDate(String date) {
        try {
            return DateUtils.SERVER_DATE_TIME_FORMAT.parse(date).getTime();
        } catch (ParseException e) {
            Log.w(TAG, e);
            return -1;
        }
    }
}
