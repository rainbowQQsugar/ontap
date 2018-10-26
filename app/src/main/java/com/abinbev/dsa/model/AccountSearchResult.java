package com.abinbev.dsa.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Jason Harris (jason@akta.com)
 */
public class AccountSearchResult extends Event {

    public static final String TAG = AccountSearchResult.class.getName();

    private boolean hasEvent;

    public AccountSearchResult() {
        // no args constructor for gson
    }

    public AccountSearchResult(JSONObject json) {
        super(json);
    }

    public boolean hasEvent() {
        return hasEvent;
    }

    public void setHasEvent(boolean hasEvent) {
        this.hasEvent = hasEvent;
    }

    public static List<AccountSearchResult> searchAccountsByNameOrId(String search) {
        List<AccountSearchResult> accountSearchResults = new LinkedList<>();
        try {
            List<Account> accountList = Account.getActiveAccountsForSearchText(search);
            for (Account account : accountList) {
                AccountSearchResult result = createAccountSearchResult(account);
                accountSearchResults.add(result);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in searching accounts", e);
        }
        return accountSearchResults;
    }

    private static AccountSearchResult createAccountSearchResult(Account account) throws JSONException {
        AccountSearchResult result = new AccountSearchResult(new JSONObject());
        result.setAccount(account);
        result.setVisitState(VisitState.open);
        return result;
    }
}
