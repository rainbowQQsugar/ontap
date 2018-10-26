/**
 * 
 * @author bduggirala
 * Copyright (c) 2014 Salesforce. All rights reserved.
 *
 */

package com.salesforce.androidsyncengine.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsyncengine.R;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.DownloadHelper;
import com.salesforce.androidsyncengine.datamanager.SmartStoreDataManagerImpl;
import com.salesforce.androidsyncengine.datamanager.model.ErrorObject;
import com.salesforce.androidsyncengine.utils.ContentPreferenceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;

public class DownloadIntentService extends IntentService {

	private static String TAG = "DownloadIntentService";

	private static final int MAX_RETRY_COUNT = 1;

//	private static long DEFAULT_MAX_WAIT_TIME = 3000;// 3 seconds
//	private static long DEFAULT_WAIT_POLL_TIME = 1000; // 1 second

	private DownloadManager downloadManager;

	private SharedPreferences retryCountPreferences;

	public DownloadIntentService() {
		super("DownloadIntentService");
	}

	@Override
	public void onCreate() {
		super.onCreate();	
		Log.v(TAG, "onCreate");
		retryCountPreferences = getRetryCountPreferences(this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG, "onHandleIntent");
		long downloadId = intent.getLongExtra(
				DownloadManager.EXTRA_DOWNLOAD_ID, 0);
		
		String appName = getResources().getString(R.string.app_name).replace(" ", "_");

		long currentWaitTime = 0;

		if (downloadId != 0) {
			Log.i(TAG, "downloadId is: " + downloadId);
			if (downloadManager == null)
				downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
			Cursor cursor = downloadManager.query(new DownloadManager.Query()
					.setFilterById(downloadId));
			if (cursor == null) {
				Log.e(TAG, "Download not found. This should not happen");
			} else {

//				try {
//				while (!cursor.moveToFirst()) {
//					Thread.sleep(DEFAULT_WAIT_POLL_TIME);
//					currentWaitTime+=DEFAULT_WAIT_POLL_TIME;
//					if (currentWaitTime > DEFAULT_MAX_WAIT_TIME) {
//						Log.e(TAG, "failed to get cursor movetofirst for downloadId: " + downloadId);
//						return;
//					}
//					cursor = downloadManager.query(new DownloadManager.Query()
//							.setFilterById(downloadId));
//				} } catch (Exception e) {
//					cursor.close();
//					Log.e(TAG, "failed to get cursor movetofirst for downloadId: " + downloadId);
//					return;
//				}

				if (cursor.moveToFirst()) {
					int status = cursor.getInt(cursor
							.getColumnIndex(DownloadManager.COLUMN_STATUS));
					String localURI = cursor.getString(cursor
							.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
					String remoteURI = cursor.getString(cursor.
							getColumnIndex(DownloadManager.COLUMN_URI));
					String fileName = cursor.getString(cursor
							.getColumnIndex(DownloadManager.COLUMN_TITLE));
					
					Log.i(TAG, "title is: " + fileName);
					
					String[] titleSplit = fileName.split("_", 3);
					String Id;
					String objectType;
					
					if (titleSplit.length == 3) {
						Id = titleSplit[1];
						objectType = titleSplit[2];
					} else {
						Id = fileName;
						objectType = fileName;
					}
					
					Log.i(TAG, "objectType is: " + objectType + " Id: " + Id);
					Log.v(TAG, "localURI: " + localURI + ", remoteURI: " + remoteURI);
					
					// TODO: This is a temporary solution while we use DownloadManager
					if (localURI != null && !localURI.contains(appName)) {
						Log.i(TAG, "this is not our file. appName: " + appName + " localURI: " + localURI);
						cursor.close();
						return;
					}

					if (status == DownloadManager.STATUS_SUCCESSFUL) {
						Log.v(TAG, "Status: Successful");
						File src;
						try {
							src = new File(new URI(localURI));
							if (src.exists()) {
								// sr.renameTo(dest) wont work since the mount
								// point are different
//								File dest = new File(getFilesDir(),
//										src.getName());
								File dest = new File(getFilesDir(), fileName);
								try {
									copyFile(src, dest);
									downloadManager.remove(downloadId);
									setRetryCount(fileName, 0);
									ContentPreferenceUtils.removeValue(fileName, this);

									// send a broadcast
									Log.v(TAG,
											"sending file received broadcast");
									Intent broadcastIntent = new Intent(
											DataManager.SYNC_ENGINE_CONTENT_FILES_RECEIVED);
									sendBroadcast(broadcastIntent);
									
//									SyncEngine.updateSyncStatus(this);

									// On Samsung devices, the file seems to stay even after
									// we remove them via DownloadManager
									// so let us delete the file manually
									try {
										Uri fileUri = Uri.parse(localURI);
										File file = new File(fileUri.getPath());
										if (file.exists()) {
											Log.e(TAG, "file still exists, trying to delete");
											file.delete();
										}
									} catch (Exception e) {
										// ignore any exceptions on delete
										Log.i(TAG, "error deleting file: ", e);
									}

								} catch (IOException e) {
									Log.e(TAG, "exception during copy file", e);
								}
							} else {
								Log.e(TAG, "file does not exist ???");
							}
						} catch (URISyntaxException e) {
							Log.w(TAG, e);
						}

					} else if (status == DownloadManager.STATUS_FAILED) {
						Log.v(TAG, "Status: Failed");
						int retryCount = getRetryCount(fileName);

						Log.v(TAG, "Retry count: " + retryCount);

						// remove the file if it exists
						downloadManager.remove(downloadId);

						// Remove value from preference
						ContentPreferenceUtils.removeValue(fileName, this);

						if (retryCount <= MAX_RETRY_COUNT) {
							Log.v(TAG, "... retrying download");
							setRetryCount(fileName, retryCount + 1);
							DownloadHelper.downloadContentFile(downloadManager, createRestClient(), this,
									Uri.parse(remoteURI), fileName);
						}
						else {
							Log.v(TAG, "... delete file without retry");
							// Notify user of error via errorObject

							setRetryCount(fileName, 0);

							SmartStoreDataManagerImpl dataManagerImpl = (SmartStoreDataManagerImpl) DataManagerFactory
									.getDataManager();

							String additionalInfo = String
									.format("Failed to get content file in object: %s with id: %s ",
											objectType, Id);

							// TODO: Get the correct message into the below two objects
							String errorCode = "FAILED CONTENT DOWNLOAD"; // c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
							String errorMessage = "Failed to download content file!"; // c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));

							ErrorObject errorObject = new ErrorObject(Id,
									objectType, additionalInfo, errorCode,
									errorMessage, null, null);

							Log.e(TAG, "errorObject is: " + errorObject.toString());

							// Insert into error store
							dataManagerImpl.insertError(errorObject);

							// TODO: Discuss whether we should retry. Download
							// manager already has retries so not sure
							// whether there is value in retrying

							// TODO: Look into failure reasons. We should retry if
							// it is a auth issue or may be not
							// DownloadManager.COLUMN_REASON will give you the
							// reason for failure

							// send a broadcast
							Log.v(TAG, "sending error broadcast");
							Intent broadcastIntent = new Intent(
									DataManager.SYNC_ENGINE_ERROR);
							sendBroadcast(broadcastIntent);

//							SyncEngine.updateSyncStatus(this);

							// this will add the request back into the Download queue
							// SyncUtils.TriggerRefresh(this);
						}

						// On Samsung devices, the file seems to stay even after
						// we remove them via DownloadManager
						// so let us delete the file manually
						if (localURI != null) {
							try {
								Uri fileUri = Uri.parse(localURI);
								File file = new File(fileUri.getPath());
								if (file.exists()) {
									Log.e(TAG, "file still exists, trying to delete");
									file.delete();
								}
							} catch (Exception e) {
								// ignore any exceptions on delete
								Log.i(TAG, "error deleting file: " + e.getMessage());
								e.printStackTrace();
							}
						}

					} else {
						Log.e(TAG, "in unhandled Download Status. " + status );
					}
					
				} else {
					Log.e(TAG, "failed to get cursor movetofirst for downloadId: " + downloadId);
				}
				cursor.close();
			}
		} else {
			Log.e(TAG, "Investigate how we can get here");
		}
	}
	
	@SuppressWarnings("resource")
	private static void copyFile(File src, File dst) throws IOException {
		FileChannel inChannel = new FileInputStream(src).getChannel();
		FileChannel outChannel = new FileOutputStream(dst).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

	private int getRetryCount(String fileName) {
		return getRetryCount(retryCountPreferences, fileName);
	}

	private void setRetryCount(String fileName, int retryCount) {
		setRetryCount(retryCountPreferences, fileName, retryCount);
	}

	private RestClient createRestClient() {
		ClientManager clientManager = new ClientManager(this,
				SalesforceSDKManager.getInstance().getAccountType(),
				SalesforceSDKManager.getInstance().getLoginOptions(),
				SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());

		return clientManager.peekRestClient();
	}

	private static SharedPreferences getRetryCountPreferences(Context c) {
		return c.getSharedPreferences("DownloadIntentService.retryCount", MODE_PRIVATE);
	}

	private static void setRetryCount(SharedPreferences prefs, String fileName, int retryCount) {
		if (retryCount <= 0) {
			prefs.edit().remove(fileName).commit();
		}
		else {
			prefs.edit().putInt(fileName, retryCount).commit();
		}
	}

	private static int getRetryCount(SharedPreferences prefs, String fileName) {
		return prefs.getInt(fileName, 0);
	}

	public static void clearRetryCounts(Context c) {
		getRetryCountPreferences(c).edit().clear().commit();
	}

	public static void setRetryCount(Context c, String filename, int retryCount) {
		setRetryCount(getRetryCountPreferences(c), filename, retryCount);
	}
}