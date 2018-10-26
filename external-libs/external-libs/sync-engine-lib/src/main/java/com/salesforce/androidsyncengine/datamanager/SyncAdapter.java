/**
 * Basic SyncAdapter
 * @author bduggirala
 */

package com.salesforce.androidsyncengine.datamanager;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsyncengine.R;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;
import com.salesforce.androidsyncengine.dynamicfetch.DynamicFetchEngine;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import java.util.Map;

class SyncAdapter extends AbstractThreadedSyncAdapter {
	private static final String TAG = "SyncAdapter";

	public SyncAdapter(Context context, boolean autoInitialize) {
		this(context, autoInitialize, false);
	}

	public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);

		//TODO: Remove the below block of code after talking to Usha
		int syncFrequency = PreferenceUtils.getSyncFrequency(getContext());
		SyncUtils.updateSyncProperties(getContext(), true, syncFrequency != 0, syncFrequency);
		Log.d(TAG, "Sync Frequency " + syncFrequency);
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {

		Log.i(TAG, "in SyncAdapter.onPerformSync");

		final Context context = getContext();

		if (extras.getBoolean(SyncUtils.SYNC_EXTRA_IS_ACCOUNT_CONTENT_REFRESH, false)) {
			Log.i(TAG, "startAccountContentSync");
			String accountId = extras.getString(SyncUtils.SYNC_EXTRA_ACCOUNT_ID);
			runStartAccountContentSync(context, accountId);
		}
		else if (extras.getBoolean(SyncUtils.SYNC_EXTRA_IS_DYNAMIC_FETCH, false)) {
			Log.i(TAG, "dynamicFetch");
			String fetchName = extras.getString(SyncUtils.SYNC_EXTRA_DYNAMIC_FETCH_NAME);
			String stringParams = extras.getString(SyncUtils.SYNC_EXTRA_DYNAMIC_FETCH_PARAMS);

			Map<String, String> params = SyncUtils.paramsFromString(stringParams);
			runDynamicFetch(context, fetchName, params);
		}
		else {
			Log.i(TAG, "startSync");
			runStartSync(context);
		}

	}

	private void runStartSync(Context context) {
		try {
			ClientManager clientManager = new ClientManager(context,
					SalesforceSDKManager.getInstance().getAccountType(),
					SalesforceSDKManager.getInstance().getLoginOptions(),
					SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());

			RestClient client = clientManager.peekRestClient();
			String apiVersion = context.getString(R.string.api_version);

			SyncEngine syncEngine = new SyncEngine(context, client, apiVersion);
			syncEngine.startSync();
		}
		catch (SyncException e) {
			Log.w(TAG, e);
			String errorMessage = e.getMessage();
			SyncEngine.setSyncStatus(SyncStatus.SyncStatusState.NOT_SYNCING, SyncStatus.SyncStatusStage.NOT_APPLICABLE, errorMessage);
			Intent broadcastIntent = new Intent(DataManager.SYNC_ENGINE_FAILURE)
					.putExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE, errorMessage);
			context.sendBroadcast(broadcastIntent);
		}
		catch (Exception e) {
			Log.w(TAG, e);
			String errorMessage = "Received error during sync! ";
			errorMessage += getExceptionInfo(e);
			SyncEngine.setSyncStatus(SyncStatus.SyncStatusState.NOT_SYNCING, SyncStatus.SyncStatusStage.NOT_APPLICABLE, errorMessage);
			Intent broadcastIntent = new Intent(DataManager.SYNC_ENGINE_FAILURE)
					.putExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE, errorMessage);
			context.sendBroadcast(broadcastIntent);
		}
	}

	private void runStartAccountContentSync(Context context, String accountId) {
		try {
			ClientManager clientManager = new ClientManager(context,
					SalesforceSDKManager.getInstance().getAccountType(),
					SalesforceSDKManager.getInstance().getLoginOptions(),
					SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());

			RestClient client = clientManager.peekRestClient();
			String apiVersion = context.getString(R.string.api_version);

			SyncEngine syncEngine = new SyncEngine(context, client, apiVersion);
			syncEngine.startAccountContentSync(accountId);
		}
		catch (SyncException e) {
			Log.w(TAG, e);
			String errorMessage = e.getMessage();
			broadcastSyncError(context, errorMessage);
		}
		catch (Exception e) {
			Log.w(TAG, e);
			String errorMessage = "Received error during sync! ";
			errorMessage += getExceptionInfo(e);
			broadcastSyncError(context, errorMessage);
		}
	}

	private void broadcastSyncError(Context context, String message) {
		SyncEngine.setSyncStatus(SyncStatus.SyncStatusState.NOT_SYNCING, SyncStatus.SyncStatusStage.NOT_APPLICABLE, message);
		Intent broadcastIntent = new Intent(DataManager.SYNC_ENGINE_FAILURE)
				.putExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE, message);
		context.sendBroadcast(broadcastIntent);
	}

	private void runDynamicFetch(Context context, String fetchName, Map<String, String> params) {
		try {
			SFSyncHelper customSyncHelper = SFSyncHelper.getSFSyncHelperInstance(context);
			DynamicFetchEngine dynamicFetchEngine = new DynamicFetchEngine(context, customSyncHelper);
			dynamicFetchEngine.fetchData(fetchName, params);
		}
		catch (SyncException e) {
			Log.w(TAG, e);
			String errorMessage = e.getMessage();
			broadcastDynamicFetchError(context, fetchName, errorMessage);
		}
		catch (Exception e) {
			Log.w(TAG, e);
			String errorMessage = "Received error during sync! ";
			errorMessage += getExceptionInfo(e);
			broadcastDynamicFetchError(context, fetchName, errorMessage);
		}
	}

	private void broadcastDynamicFetchError(Context context, String fetchName, String message) {
		context.sendBroadcast(new Intent(DataManager.DYNAMIC_FETCH_ERROR)
				.putExtra(DataManager.EXTRAS_DYNAMIC_FETCH_NAME, fetchName)
				.putExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE, message));
	}

	private String getExceptionInfo(Exception e) {
		if (e == null) return null;

		String message = e.getMessage();
		StackTraceElement[] stackTrace = e.getStackTrace();
		if (stackTrace != null && stackTrace.length > 0) {
			message += String.format(" (%s:%s)", stackTrace[0].getFileName(), stackTrace[0].getLineNumber());
		}

		return message;
	}
}
