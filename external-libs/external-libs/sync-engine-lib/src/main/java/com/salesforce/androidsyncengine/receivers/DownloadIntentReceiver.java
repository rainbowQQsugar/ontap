/**
 * 
 * @author bduggirala
 * Copyright (c) 2014 Salesforce. All rights reserved.
 *
 */

package com.salesforce.androidsyncengine.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.salesforce.androidsyncengine.services.DownloadIntentService;

/**
 * This receiver class is designed to listen for downloads
 * 
 */
public class DownloadIntentReceiver extends BroadcastReceiver {

	private static final String TAG = "DownloadIntentReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.v(TAG, "received intent");
		
		long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
		
		if (downloadId != 0) {
			Intent newIntent = new Intent(context, DownloadIntentService.class);
			newIntent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId);
			context.startService(newIntent);
		}	
	}

}