/**
 * WebView activity to handle HTML5 Content
 *
 * @author bduggirala
 */


package com.abinbev.dsa.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.Toast;

import com.abinbev.dsa.BuildConfig;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.SurveyQuestionResponse__c;
import com.abinbev.dsa.model.SurveyTaker__c;
import com.abinbev.dsa.model.Survey_Question__c;
import com.abinbev.dsa.model.Survey__c;
import com.abinbev.dsa.service.AttachmentUploadService;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.AttachmentUtils;
import com.abinbev.dsa.utils.AzureUtils;
import com.abinbev.dsa.utils.ContentUtils;
import com.abinbev.dsa.utils.LocationUtils;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.androidsdk.accounts.UserAccountManager;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import com.salesforce.dsa.data.model.ContentVersion;

import org.apache.cordova.Config;
import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.engine.SystemWebChromeClient;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;


import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Single;
import rx.subscriptions.CompositeSubscription;


public class WebViewActivity extends AppBaseActivity implements org.apache.cordova.CordovaInterface {

    public static final String SURVEY_TAKER_ID = "SURVEY_TAKER_ID";
    public static final String SURVEY_USER_ID = "SURVEY_USER_ID";
    public static final String SURVEY_ID = "SURVEY_ID";
    public static final String ACCOUNT_ID_EXTRA = "account_id";
    public static final String SSO = "SSO";
    public static final String URL = "Url";
    public static final String ZIP = "ZIP";
    public static final String TITLE = "Title";
    public static final String DESCRIPTION = "Description";
    public static final String COMPLETE_FILE_NAME = "CompleteFileName";
    public static final String FILE_EXTENSION = "FileExtension";
    // indicates that this is the initial webview and not a webview that has the sub-survey
    public static final String PRIMARY = "Primary";
    private static String TAG = "WebViewActivity";

    private static final int FILECHOOSER_RESULTCODE = 5173;

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;

    private String title;
    private String description;
    private String completeFileName;

    private ProgressDialog progressDialog;
    private SystemWebView systemWebView;
    private CordovaWebView cordovaWebView;

    private String initCallbackClass;

    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    protected CordovaPlugin activityResultCallback = null;
    protected boolean activityResultKeepRunning;
    protected boolean keepRunning = true;

    protected int loadUrlTimeoutValue = 20000;

    private String survey;
    private String surveyTakerId;
    private String surveyUserId;
    private String surveyTakerStatus;
    private String accountId;

    private LatLng currentLatLng;

    private boolean isPrimary;
    private boolean isSurveyCreation;
    private boolean isSurveySaved = false;

    private boolean isChatter = false;

    private boolean isSurveyEdited = false;

    private CompositeSubscription subscriptions = new CompositeSubscription();

    @SuppressLint("SetJavaScriptEnabled")
    public void onCreate(Bundle savedInstanceState) {
      //  Config.init(this);

        super.onCreate(savedInstanceState);

        setRequestedOrientation(getPreferredOrientation());

        if (savedInstanceState != null) {
            initCallbackClass = savedInstanceState.getString("callbackClass");
        }

        // Configures options for the progress indicator.
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading HTML5 Content ...");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);

        systemWebView = (SystemWebView) findViewById(R.id.webView);

        ConfigXmlParser parser = new ConfigXmlParser();
                parser.parse(this);
                SystemWebViewEngine systemWebViewEngine = new SystemWebViewEngine(systemWebView);
                cordovaWebView = new CordovaWebViewImpl(systemWebViewEngine);
                cordovaWebView.init(this, parser.getPluginEntries(), parser.getPreferences());
                systemWebView.setWebChromeClient(new SystemWebChromeClient(systemWebViewEngine) {
            //The undocumented magic method override
                    //Eclipse will swear at you if you try to put @Override here
                    // For Android 3.0+
                    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                                mUploadMessage = uploadMsg;
                                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                                i.addCategory(Intent.CATEGORY_OPENABLE);
                                i.setType("image/*");
                                WebViewActivity.this.startActivityForResult(Intent.createChooser(i,"File Chooser"), FILECHOOSER_RESULTCODE);
                            }

                    // For Android 3.0+
                    public void openFileChooser( ValueCallback uploadMsg, String acceptType ) {
                                mUploadMessage = uploadMsg;
                                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                                i.addCategory(Intent.CATEGORY_OPENABLE);
                                i.setType("*/*");
                                WebViewActivity.this.startActivityForResult(
                                                Intent.createChooser(i, "File Browser"),
                                                FILECHOOSER_RESULTCODE);
                            }

                    //For Android 4.1
                    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
                                mUploadMessage = uploadMsg;
                                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                                i.addCategory(Intent.CATEGORY_OPENABLE);
                                i.setType("image/*");
                                WebViewActivity.this.startActivityForResult(Intent.createChooser( i, "File Chooser" ), WebViewActivity.FILECHOOSER_RESULTCODE );
                            }
        });

        if (BuildConfig.DEBUG) {
            systemWebView.setWebContentsDebuggingEnabled(true);
        }

        systemWebView.clearCache(true);
        systemWebView.addJavascriptInterface(new SurveyWebAppInterface(), "Surveys");

        processIntent(getIntent());

        setupToolbar();

        surveysDirectory = getFilesDir().getAbsolutePath() + File.separator + "SurveyData" + File.separator;
        if (isPrimary) deleteSurveyFolder();

    }

    private int getPreferredOrientation() {
        return getResources().getBoolean(R.bool.is10InchTablet) ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    @Override
    public void onDestroy() {
        try {
            if (cordovaWebView != null) cordovaWebView.handleDestroy();
        } catch (Exception e) {
            // Ignore any exceptions on handleDestroy
        }
        subscriptions.clear();
        super.onDestroy();
    }


    @Override
    public int getLayoutResId() {
        return R.layout.webview;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (systemWebView.canGoBack()) {
                //if Back key pressed and webview can navigate to previous page
                systemWebView.goBack();
                // go back to previous page
                return true;
            } else {
                if (isChatter) {
                    finish();
                } else {
                    promptToExitActivity();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Called when new intent is delivered.
     * This is where we check the incoming intent for an action.
     *
     * @param newIntent The intent used to restart this activity
     */
    @Override
    public void onNewIntent(final Intent newIntent) {
        super.onNewIntent(newIntent);
        // get and process new intent
        processIntent(newIntent);
    }

    private void processIntent(Intent newIntent) {

        //String summary = "<html><body>Hello World.</body></html>";
        //webView.loadData(summary, "text/html", null);


        Bundle extras = newIntent.getExtras();
        if (extras != null) {

            String fileExtension = extras.getString(FILE_EXTENSION);

            if (ZIP.equals(fileExtension)) {
                title = extras.getString(TITLE);
                description = extras.getString(DESCRIPTION);
                completeFileName = extras.getString(COMPLETE_FILE_NAME);
                surveyTakerId = extras.getString(SURVEY_TAKER_ID);
                surveyUserId = extras.getString(SURVEY_USER_ID);
                accountId = extras.getString(ACCOUNT_ID_EXTRA);
                survey = extras.getString(SURVEY_ID);
                isSurveyCreation = !TextUtils.isEmpty(survey); // New survey requires to have SURVEY_ID

                surveyTakerId = extras.getString(SURVEY_TAKER_ID);

                isPrimary = extras.getBoolean(PRIMARY, true);

                if (surveyTakerId != null) {
                    SurveyTaker__c surveyTaker = SurveyTaker__c.getById(surveyTakerId);
                    if (surveyTaker != null) {
                        survey = surveyTaker.getStringValueForKey(AbInBevConstants.SurveyTakerFields.SURVEY__C);
                        surveyTakerStatus = surveyTaker.getState();
                    }
                }


                // If we want more accurate results we should ask and get the current location
                // rather than use the last known location which is what the below method does
                currentLatLng = LocationUtils.getCurrentLocation(this);
                progressDialog.show();
                new UnzipHTMLContentTask().execute(completeFileName);
            } else {

                String url = extras.getString(URL);
                boolean sso = extras.getBoolean(SSO, false);

                if (sso) {
                    progressDialog.setTitle(getString(R.string.loading_chatter));
                    progressDialog.show();
                    new authenticateWebViewTask().execute(url);
                    //authenticateIntoWebView(url);
                } else {
                    android.webkit.CookieManager.getInstance().removeAllCookie();
                    systemWebView.loadUrl(url);
                }
                title = getString(R.string.chatter);
                isChatter = true;
            }

        }

    }

    private void setupToolbar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setSubtitle(description);
    }

    public void authenticateIntoWebView(String url) {

        try {


            // on auth we want to clear the webview cookies

            // android.webkit.CookieManager.getInstance().removeAllCookie();
            // Log.i(TAG, "in authenticateIntoWebView removed all cookies");

            ClientManager clientManager = new ClientManager(this, SalesforceSDKManager.getInstance()
                    .getAccountType(), SalesforceSDKManager.getInstance().getLoginOptions(), SalesforceSDKManager
                    .getInstance().shouldLogoutWhenTokenRevoked());

            RestClient client = clientManager.peekRestClient();

            String sessionId = URLEncoder.encode(client.getAuthToken(), "utf-8");
            String encodedURL = URLEncoder.encode(url, "utf-8");

            String frontDoorUrl = client.getClientInfo().getInstanceUrlAsString() + "/secur/frontdoor.jsp?retURL=" + encodedURL + "&display=touch&sid=";
            //String frontDoorUrl = client.getClientInfo().getInstanceUrlAsString() + "/secur/frontdoor.jsp?display=touch&sid=";

            String newUrl = frontDoorUrl + sessionId;

            Log.e(TAG, "newUrl is: " + newUrl);
            systemWebView.loadUrl(newUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error in authenticateIntoWebView : " + e.getMessage());
        }

    }

    private class authenticateWebViewTask extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... urls) {

            try {

                // the below is to ensure that we get a new auth token if the current auth token has expired
                ClientManager clientManager = new ClientManager(WebViewActivity.this, SalesforceSDKManager.getInstance()
                        .getAccountType(), SalesforceSDKManager.getInstance().getLoginOptions(), SalesforceSDKManager
                        .getInstance().shouldLogoutWhenTokenRevoked());

                RestClient client = clientManager.peekRestClient();

                String apiVersion = getString(R.string.api_version);
                RestResponse result = null;
                String query = "SELECT User_Profile__c FROM User where Id = '" + client.getClientInfo().userId + "'";


                final RestRequest restRequest = RestRequest.getRequestForQuery(apiVersion, query);
                result = client.sendSync(restRequest);
                if (result.isSuccess()) {
                }

                return urls[0];

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "in WebViewActivity :" + e.getMessage());

            }

            return null;
        }

        protected void onPostExecute(String result) {
            if (result != null) {
                authenticateIntoWebView(result);
            }
            progressDialog.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (systemWebView.canGoBack()) {
                    //if Back key pressed and webview can navigate to previous page
                    systemWebView.goBack();
                    // go back to previous page
                    return true;
                } else {
                    if (isChatter) {
                        finish();
                    } else {
                        promptToExitActivity();
                    }
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private class UnzipHTMLContentTask extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... urls) {
            String unzipLocation = ContentUtils.unzip(urls[0]);
            if (unzipLocation == null) return null;
            String webUrl = "file://localhost" + unzipLocation + "index.html";
            return webUrl;
        }

        protected void onPostExecute(String result) {
            if (result == null) {
                progressDialog.dismiss();
                Toast.makeText(WebViewActivity.this, "Could not get html bundle!", Toast.LENGTH_LONG).show();
                return;
            }
            Log.d(TAG, "unzip result " + result);
            progressDialog.dismiss();
            systemWebView.loadUrl(result);

        }
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public Activity getActivity() {
        return this;
    }


    @Override
    public Object onMessage(String arg0, Object arg1) {
        return null;
    }

    @Override
    public void setActivityResultCallback(CordovaPlugin plugin) {
        this.activityResultCallback = plugin;
    }

    /**
     * Launch an activity for which you would like a result when it finished. When this activity exits,
     * your onActivityResult() method is called.
     *
     * @param command     The command object
     * @param intent      The intent to start
     * @param requestCode The request code that is passed to callback to identify the activity
     */
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
        this.activityResultCallback = command;
        this.activityResultKeepRunning = this.keepRunning;


        // If multitasking turned on, then disable it for activities that return results
        if (command != null) {
            this.keepRunning = false;
        }


        // Start activity
        super.startActivityForResult(intent, requestCode);

    }

    @Override
    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode       The request code originally supplied to startActivityForResult(),
     *                          allowing you to identify who this result came from.
     * @param resultCode        The integer result code returned by the child activity through its setResult().
     * @param data              An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Request code = " + requestCode);

        if (requestCode == AttachmentUtils.SELECT_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri pictureUri = (data != null && data.getData() != null) ? data.getData() : AttachmentUtils.fileUri;
            Log.e("Babu", "pictureUri: " + pictureUri);

            if (pictureUri != null) {
                handleQuestionResponsePicture(pictureUri);
            } else {
                Log.e(TAG, "pictureUri is null. Error in getting image!");
            }
        }
        else {
            if (systemWebView != null && requestCode == FILECHOOSER_RESULTCODE) {
            //    ValueCallback<Uri> mUploadMessage = this.webView.getWebChromeClient().getValueCallback();
                // For 5.x devices
            //    ValueCallback<Uri[]> mFilePathCallback = this.webView.getWebChromeClient().getfilePathsCallback();
                Log.d(TAG, "did we get here?");
                if ((null == mUploadMessage) && (null == mFilePathCallback))
                    return;
                Uri result = data == null || resultCode != Activity.RESULT_OK ? null : data.getData();
                Log.d(TAG, "result = " + result);
//            Uri filepath = Uri.parse("file://" + FileUtils.getRealPathFromURI(result, this));
//            Log.d(TAG, "result = " + filepath);

                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(result);
                    mUploadMessage = null;
                } else if (mFilePathCallback != null) {
                    Uri[] result2 = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                    mFilePathCallback.onReceiveValue(result2);
                    mFilePathCallback = null;
                }
            }
            CordovaPlugin callback = this.activityResultCallback;
            if (callback == null && initCallbackClass != null) {
                // The application was restarted, but had defined an initial callback
                // before being shut down.
                this.activityResultCallback = cordovaWebView.getPluginManager().getPlugin(initCallbackClass);
                callback = this.activityResultCallback;
            }
            if (callback != null) {
                Log.d(TAG, "We have a callback to send this result to");
                callback.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void requestPermission(CordovaPlugin plugin, int requestCode, String permission) {

    }

    @Override
    public void requestPermissions(CordovaPlugin plugin, int requestCode, String[] permissions) {

    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }

    private void handleQuestionResponsePicture(Uri pictureUri) {
        if (pictureUri == null) return;

        String questionResponsePath = surveyTakerId == null ?
                surveysDirectory + NEW_SURVEY_DIR + File.separator + takePictureParentId
                    : surveysDirectory + surveyTakerId + File.separator + takePictureParentId;

        File questionResponseDirectory = new File(questionResponsePath);
        if (!questionResponseDirectory.exists())
            questionResponseDirectory.mkdirs();

        File destPictureFile = new File(questionResponseDirectory + File.separator + pictureUri.getLastPathSegment());

        subscriptions.add(Single.fromCallable(
                () -> {
                    boolean success = AttachmentUtils.compressImage(getApplicationContext(), pictureUri, destPictureFile);

                    Log.e(TAG, "newSurveyPhoto path :" + destPictureFile.getAbsolutePath());
                    Log.e(TAG, "newSurveyPhoto size: " + destPictureFile.length());

                    if (success) {
                        ContentUtils.deleteContent(getApplicationContext(), pictureUri);
                    }

                    return success ? destPictureFile : null;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        resultFile -> {
                            if (resultFile != null) {
                                // this seems like the most straight forward mechanism to pass back data to the webview
                                cordovaWebView.loadUrl("javascript:Surveys.photoCallback('" + mPassedId + "','" + resultFile.getAbsolutePath()
                                        + "','" + resultFile.getName() + "')");
                            }
                        },
                        error -> Log.e(TAG, "Error saving new photo: ", error)
                ));
    }


    @Override
    public ExecutorService getThreadPool() {
        return threadPool;
    }

    private class SurveyWebAppInterface {

        SurveyWebAppInterface() {
        }

        @JavascriptInterface
        public String getSurvey() {
            Log.i(TAG, "in getSurvey");
            return survey;
        }

        @JavascriptInterface
        public String getSurveyName() {
            Log.i(TAG, "in getSurveyName");
            return Survey__c.getById(survey).getName();
        }

        @JavascriptInterface
        public String getSurveyTakerId() {
            Log.i(TAG, "in getSurveyTakerId");
            // If it is a new record then save it and then pass the value
            if (surveyTakerId == null) {
                if (surveyUserId == null) {
                    surveyTakerId = SurveyTaker__c.createNewSurveyTakerRecord(accountId, survey);
                    processSurveyPhotos(surveyTakerId);
                } else {
                    surveyTakerId = SurveyTaker__c.createNewQuizTakerRecord(survey, surveyUserId);
                }
            } else {
                String updatedSurveyTakerId = DataManagerFactory.getDataManager().getSalesforceIdFromTemporaryId(surveyTakerId);
                if (updatedSurveyTakerId != null) {
                    Log.i(TAG, "*** using updated value ***");
                    surveyTakerId = updatedSurveyTakerId;
                }
            }
            return surveyTakerId;
        }

        @JavascriptInterface
        public String getSurveyTakerStatus() {
            Log.i(TAG, "in getSurveyTakerStatus");
            return surveyTakerStatus;
        }

        @JavascriptInterface
        public String getCurrentAccountId() {
            Log.i(TAG, "in getCurrentAccountId");
            return accountId;
        }

        @JavascriptInterface
        public String getCurrentUserId() {
            Log.i(TAG, "in getCurrentUserId");
            return  UserAccountManager.getInstance().getStoredUserId();
        }

        @JavascriptInterface
        public String getCurrentLatitude() {
            Log.i(TAG, "in getCurrentLatitude");
            if (currentLatLng != null) return String.valueOf(currentLatLng.latitude);
            return null;
        }

        @JavascriptInterface
        public String getCurrentLongitude() {
            Log.i(TAG, "in getCurrentLongitude");
            if (currentLatLng != null) return String.valueOf(currentLatLng.longitude);
            return null;
        }

        @JavascriptInterface
        public boolean launchNewSurvey(String surveyId) {
            Log.i(TAG, "in launchNewSurvey");
            createAndLaunchNewSurvey(surveyId);
            return true;
        }

        @JavascriptInterface
        public void finish() {
            Log.i(TAG, "in finish");
            if (isPrimary) {
                publishSurveyResults();
            }
            WebViewActivity.this.finish();
        }

        @JavascriptInterface
        public void publishSurveyResults() {
            Log.i(TAG, "in publishSurveyResults");
            isSurveySaved = true;
            deletePhotoFiles();
            mapQuestionToResponse(surveyTakerId);
            SyncUtils.TriggerRefresh(WebViewActivity.this);
        }

        @JavascriptInterface
        public String getPhotosForSurveyQuestionResponseId(String Id) {
            Log.i(TAG, "in getPhotosForSurveyQuestionResponseId: " + Id);
            String attachments = Attachment.getFilesForSurveyQuestionResponse(Id).toString();
            Log.i(TAG, attachments);
            return attachments;
        }

        @JavascriptInterface
        public String getPhotosForSurveyQuestionId(String Id) {
            Log.i(TAG, "in getPhotosForSurveyQuestionId: " + Id);
            String attachments = Attachment.getAttachmentsForSurveyQuestion(Id).toString();
            Log.i(TAG, attachments);
            return attachments;
        }

        @JavascriptInterface
        public void takePhotoForQuestionWithId(String Id, String successCallback, String errorCallback) {
            // for a new survey we only have question id
            mPassedId = Id;
            Log.i(TAG, "in takePhotoForQuestionWithId with :" + Id);
            launchGetPictureIntent(Id);
        }

        @JavascriptInterface
        public void takePhotoForSurveyQuestionId(String Id) {
            // for a new survey we only have question id
            Log.i(TAG, "in takePhotoForQuestionWithId with :" + Id);
            mPassedId = Id;
            launchGetPictureIntent(Id);
        }

        @JavascriptInterface
        public void takePhotoForSurveyQuestionResponseId(String Id, boolean allowLibrary) {
            // for a survey that is already started we may or may not have the SurveyQuestionResponseId
        }

        @JavascriptInterface
        public void removePhotoAtPath(String path) {
            // for a survey that is already started we may or may not have the SurveyQuestionResponseId

            Log.v(TAG, "path: " + path);

            // if it new survey then just delete the file
            if (surveyTakerId == null) {
                deleteFileAtPath(path);
            } else {
                // the reason we are not deleting right away is if the user decides not to save his changes
                // then we want to leave the photos in place
                filesToDelete.add(path);
            }

            Log.v(TAG, "filesToDelete: " + filesToDelete);
        }

        @JavascriptInterface
        public void markAsEdited() {
            isSurveyEdited = true;
        }

    }

    private void deleteFileAtPath(String path) {
        File file = new File(path);
        if (file.exists()) file.delete();
    }

    private void createAndLaunchNewSurvey(String newSurveyId) {

        final Survey__c surveyType = Survey__c.getById(newSurveyId);

        ContentVersion contentVersion = surveyType.getAssociatedContent();
        if (contentVersion == null) {
            Toast.makeText(this, R.string.survey_error_message, Toast.LENGTH_LONG).show();
            return;
        }
        final Intent webViewIntent = new Intent(this, WebViewActivity.class);
        //New Survey uses SURVEY_ID
        webViewIntent.putExtra(WebViewActivity.SURVEY_ID, surveyType.getId());
        webViewIntent.putExtra(WebViewActivity.ACCOUNT_ID_EXTRA, accountId);
        webViewIntent.putExtra(WebViewActivity.TITLE, getString(R.string.survey_header_title));
        webViewIntent.putExtra(WebViewActivity.DESCRIPTION, surveyType.getName());
        webViewIntent.putExtra(WebViewActivity.COMPLETE_FILE_NAME, contentVersion.getCompleteFileName(this));
        webViewIntent.putExtra(WebViewActivity.FILE_EXTENSION, WebViewActivity.ZIP);
        webViewIntent.putExtra(WebViewActivity.PRIMARY, false);
        startActivity(webViewIntent);
        
    }

    // Photo capture will work only with Primary Surveys. Not with Sub Surveys.
    static final String NEW_SURVEY_DIR = "NewSurvey";
    private String takePictureParentId;

    private void launchGetPictureIntent(String id) {
        takePictureParentId = id;
        Survey_Question__c questionData = Survey_Question__c.getById(id);
        if (questionData != null) {
            if (questionData.shouldAllowPickingFromLibrary()) {
                AttachmentUtils.createPhotoChooserDialog(this).show();
            } else {
                AttachmentUtils.takePhoto(this);
            }
        } else {
            // show error message about missing question data
            // this should not happen unless we are mucking with data in sql
            Log.e(TAG, "No question available for id: " + id);
        }
    }

    String mPassedId;

    List<String> filesToDelete = new ArrayList<String>();

    String surveysDirectory; // = getFilesDir().getAbsolutePath() + File.separator + "Surveys" + File.separator;

    private void deleteSurveyFolder() {
        String dirPath = surveysDirectory + NEW_SURVEY_DIR;
        File surveyDir = new File(dirPath);
        if (surveyDir.exists())
            delete(surveyDir);
    }

    private void delete(File f){
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete()) {
            Log.e("Babu", "Failed to delete file: " + f);
        }
    }

    private void processSurveyPhotos(String newSurveyTakerId) {
        String dirPath = surveysDirectory + NEW_SURVEY_DIR;
        File newPath = new File(surveysDirectory + newSurveyTakerId);
        File surveyDir = new File(dirPath);
        if (surveyDir.exists()) {
            if (surveyDir.list().length > 0) {
                if (surveyDir.renameTo(newPath)) {
                    Log.e("Babu", "successfully renamed survey directory");
                    // mapQuestionToResponse(newSurveyTakerId);
                } else {
                    Log.e("Babu", "failed to rename survey directory");
                }
            } else {
                Log.e("Babu", "survey directory is empty. Let us just delete it!");
                surveyDir.delete();
            }
        } else {
            Log.e("Babu", "new survey directory does not exist!");
        }
    }

    private void mapQuestionToResponse(String newSurveyTakerId) {
        File newPath = new File(surveysDirectory + newSurveyTakerId);
        File[] questionIdFiles = newPath.listFiles();
        // null means it is not a directory or does not exist
        if (questionIdFiles == null) return;
        for (File file : questionIdFiles) {
            if (file.list().length > 0) {
                Log.e("Babu", "file name: " + file.getName());
                String surveyQuestionId = file.getName();
                String questionResponseId = SurveyQuestionResponse__c.getSurveyQuestionResponseId(newSurveyTakerId, surveyQuestionId);
                if (questionResponseId == null) {
                    Log.e("Babu", "got null value for: " + surveyQuestionId);
                    continue;
                }
                String currentQuestionResponseDir = surveysDirectory + "QR" + File.separator + questionResponseId;
                File fileCurrentQuestionResponse = new File(currentQuestionResponseDir);
                if (!fileCurrentQuestionResponse.exists())
                    fileCurrentQuestionResponse.mkdirs();
                File[] imageFiles = file.listFiles();

                for (File imageFile : imageFiles) {
                    Log.e("Babu", "renamed: " + imageFile.getAbsolutePath());
                    File newImageFile = new File(currentQuestionResponseDir + File.separator + imageFile.getName());
                    if (imageFile.renameTo(newImageFile)) {
                        Log.e("Babu", "to: " + newImageFile.getAbsolutePath());
                        //Log.e("Babu", "renamed: " + imageFile.getAbsolutePath());
                        Intent intent = AttachmentUploadService.uploadSurveyPhoto(this, Uri.fromFile(newImageFile), questionResponseId);
                        startService(intent);
                    } else {
                        Log.e("Babu", "failed renamed: " + imageFile.getAbsolutePath());
                    }
                }
                file.delete();
            } else {
                file.delete();
            }
        }
        newPath.delete();
    }

    private void deletePhotoFiles() {
        for (String path : filesToDelete) {
            if (path.endsWith("Attachment")) {
                // get id from path
                // hacky way to get id
                String filePath = path;
                filePath = filePath.replace("_Attachment", "");
                int lastIndex = filePath.lastIndexOf("_");
                String attachmentId = filePath.substring(lastIndex + 1, filePath.length());
                Log.e("Babu", "attachment Id: " + attachmentId + "length: " + attachmentId.length());
                Attachment.deleteAttachment(attachmentId);
            } else if (path.contains(AbInBevConstants.AbInBevObjects.PICTURE_AUDIT_STATUS)) {
                AzureUtils.deleteBlobFromBackend(path);
            } else {
                Attachment.deleteLocalSurveyPhotos(path);
            }
        }
        filesToDelete.clear();
    }

    public void promptToExitActivity() {
        if (isSurveyCreation && isSurveyEdited && !isSurveySaved) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            alertDialog.setTitle("");
            alertDialog.setMessage(R.string.survey_exit_prompt);

            alertDialog.setPositiveButton(R.string.si,
                    (dialog, which) -> finish());

            alertDialog.setNegativeButton(R.string.no,
                    (dialog, which) -> dialog.cancel());

            alertDialog.show();
        }
        else {
            finish();
        }
    }
}
