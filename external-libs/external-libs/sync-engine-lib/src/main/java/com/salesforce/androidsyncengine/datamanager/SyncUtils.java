/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.salesforce.androidsyncengine.datamanager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.salesforce.androidsyncengine.R;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Static helper methods for working with the sync framework.
 */
public class SyncUtils {

	public static final String SYNC_EXTRA_IS_ACCOUNT_CONTENT_REFRESH = "is_account_content_refresh";
	public static final String SYNC_EXTRA_IS_DYNAMIC_FETCH = "is_dynamic_fetch";
	public static final String SYNC_EXTRA_ACCOUNT_ID = "account_id";
	public static final String SYNC_EXTRA_DYNAMIC_FETCH_NAME = "dynamic_fetch_name";
	public static final String SYNC_EXTRA_DYNAMIC_FETCH_PARAMS = "dynamic_fetch_params";

	/**
	 * Helper method to trigger an immediate sync ("refresh").
	 *
	 * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
	 * means the user has pressed the "refresh" button.
	 *
	 * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
	 * preserve battery life. If you know new data is available (perhaps via a GCM notification),
	 * but the user is not actively waiting for that data, you should omit this flag; this will give
	 * the OS additional freedom in scheduling your sync request.
	 */
	public static void TriggerRefresh(Context context) {
		String contentAuthority = getContentAuthority(context);
		Bundle b = new Bundle();
		// Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
		b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

		Account account = getAccount(context);
		if (account != null) {
			ContentResolver.requestSync(
					account,      // Sync account
					contentAuthority, // Content authority
					b);                // Extras
		}
	}

	public static void TriggerAccountContentRefresh(Context context, String accountId) {
		String contentAuthority = getContentAuthority(context);
		Bundle b = new Bundle();
		// Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
		b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		b.putBoolean(SYNC_EXTRA_IS_ACCOUNT_CONTENT_REFRESH, true);
		b.putString(SYNC_EXTRA_ACCOUNT_ID, accountId);

		Account account = getAccount(context);
		if (account != null) {
			ContentResolver.requestSync(
					account,      // Sync account
					contentAuthority, // Content authority
					b);                // Extras
		}
	}

	public static void TriggerDynamicFetch(Context context, String fetchName, Map<String, String> params) {
		String contentAuthority = getContentAuthority(context);
		Bundle b = new Bundle();
		// Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
		b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		b.putBoolean(SYNC_EXTRA_IS_DYNAMIC_FETCH, true);
		b.putString(SYNC_EXTRA_DYNAMIC_FETCH_NAME, fetchName);
		b.putString(SYNC_EXTRA_DYNAMIC_FETCH_PARAMS, paramsToString(params));

		Account account = getAccount(context);
		if (account != null) {
			ContentResolver.requestSync(
					account,      // Sync account
					contentAuthority, // Content authority
					b);                // Extras
		}
	}

	public static void TriggerDeltaRefresh(Context context) {
		PreferenceUtils.putTriggerDeltaSync(true, context);
		TriggerRefresh(context);
	}

	public static void cancelDeltaRefresh(Context context) {
		PreferenceUtils.putTriggerDeltaSync(false, context);
		String contentAuthority = getContentAuthority(context);
		Account account = getAccount(context);
		if (account != null) {
			ContentResolver.cancelSync(account, contentAuthority);
		}

	}

	public static Account getAccount(Context context) {
		String accountType = context.getResources().getString(R.string.account_type);
		AccountManager accountManager = AccountManager.get(context);      
		Account[] accounts = accountManager.getAccountsByType(accountType);
		if (accounts.length > 0) {      	
			return accounts[0];
		} else {
			return null;
		}
	}

	public static void updateSyncProperties(Context context, boolean isSyncable, boolean autoSync, long syncFrequencyInSeconds) {
		String contentAuthority = getContentAuthority(context);
		Account account = getAccount(context);
		// Inform the system that this account supports sync
		ContentResolver.setIsSyncable(account, contentAuthority, isSyncable ? 1 : 0);
		// Inform the system that this account is eligible for auto sync when the network is up
		ContentResolver.setSyncAutomatically(account, contentAuthority, autoSync);
		// Recommend a schedule for automatic synchronization. The system may modify this based
		// on other scheduled syncs and network utilization.

		// we should never get a zero value but ...
		if (syncFrequencyInSeconds == 0) {
			syncFrequencyInSeconds = 86400; // try after 24 hours
		}
		ContentResolver.addPeriodicSync(
				account, contentAuthority, new Bundle(), syncFrequencyInSeconds);
	}
	
	public static String getContentAuthority(Context context) {
		String contentAuthority = context.getResources().getString(R.string.content_authority);
		return contentAuthority;
	}

	static String paramsToString(Map<String, String> params) {
		Gson gson = new Gson();
		return gson.toJson(params);
	}

	static Map<String, String> paramsFromString(String params) {
		Gson gson = new Gson();
		return gson.fromJson(params, Map.class);
	}

	public static boolean isTemporaryIdForObject(String id, String obj) {
		String prefix = obj.subSequence(0, Math.max(obj.length(), 3)).toString().toUpperCase();
		Pattern pattern = Pattern.compile("^[" + prefix + "\\-]{3}.*", Pattern.DOTALL);
		return pattern.matcher(id).matches() ;
	}
}
