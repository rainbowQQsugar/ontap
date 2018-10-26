/**
 * WebView activity to handle HTML5 Content
 *
 * @author bduggirala
 */


package com.salesforce.dsa.app.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

import com.abinbev.dsa.activity.AppBaseActivity;
import com.salesforce.dsa.app.R;
import com.salesforce.dsa.app.utils.ContentUtils;

import org.apache.cordova.Config;
import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.engine.SystemWebChromeClient;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class WebViewActivity extends AppBaseActivity implements org.apache.cordova.CordovaInterface {
    private static String TAG = "WebViewActivity";

    private static final int FILECHOOSER_RESULTCODE = 5173;

    private String title;
    private String description;
    private String completeFileName;

    private ProgressDialog progressDialog;
    private SystemWebView systemWebView;
    private CordovaWebView cordovaWebView;

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;

    private String initCallbackClass;

    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    protected CordovaPlugin activityResultCallback = null;
    protected boolean activityResultKeepRunning;
    protected boolean keepRunning = true;

    protected int loadUrlTimeoutValue = 20000;


    @SuppressLint("SetJavaScriptEnabled")
    public void onCreate(Bundle savedInstanceState) {
    //   Config.init(this);

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
                                WebViewActivity.this.startActivityForResult( Intent.createChooser( i, "File Chooser" ), WebViewActivity.FILECHOOSER_RESULTCODE );
                            }
        });
                systemWebView.getSettings().setJavaScriptEnabled(true);
        
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        systemWebView.clearCache(true);
        //webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //webView.getRootView().setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        //webView.setWebViewClient(new WebViewClient());
        //webView.setWebChromeClient(new WebChromeClient());

        // webView.loadUrl("file://localhost/storage/emulated/0/html5zip/index.html");
        processIntent(getIntent());

        //webView.loadUrl("http://google.com");

    }

    private int getPreferredOrientation() {
        return getResources().getBoolean(com.abinbev.dsa.R.bool.is10InchTablet) ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.webview;
    }

    @Override
    public void onBackPressed() {
        if (systemWebView.canGoBack())
            systemWebView.goBack();
        else
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

            String fileExtension = extras.getString("FileExtension");

            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);


            if (fileExtension.equals("ZIP")) {
                title = extras.getString("Title");
                description = extras.getString("Description");
                completeFileName = extras.getString("CompleteFileName");


                actionBar.setTitle((ContentUtils.getDisplayableString(title, "HTML5 Bundle")));

                String subtitle = ContentUtils.getDisplayableString(description, null);
                actionBar.setSubtitle(subtitle);

                progressDialog.show();
                new UnzipHTMLContentTask().execute(completeFileName);
            } else {
                String url = extras.getString("Url");

                // TODO: If it makes sense to add the http header then do it.
                // The below approach works only for a very rudimentary links - a pdf link might work but not others that require multiple images to be downloaded from server
//				Map<String,String> extraHeaders = new HashMap<String, String>();
//				
//				ClientManager clientManager = new ClientManager(this, SalesforceSDKManager.getInstance().getAccountType(),
//						SalesforceSDKManager.getInstance().getLoginOptions(),
//						SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());
//
//				RestClient client = clientManager.peekRestClient();
//				
//			    extraHeaders.put("Authorization", "Bearer " + client.getAuthToken());
//			    webView.loadUrl(url, extraHeaders);
//			    
//			    Log.e("Babu", "url:" + url);
//			    Log.e("Babu", "client:" + client.getClientInfo().loginUrl);
//			    
//			    webView.loadUrl(url, extraHeaders);

                actionBar.setTitle("Web Link");
                cordovaWebView.loadUrl(url);
            }

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class UnzipHTMLContentTask extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... urls) {
            String webUrl = "file://localhost" + ContentUtils.unzip(urls[0]) + "index.html";
            return webUrl;
        }

        protected void onPostExecute(String result) {
            Log.d("qqq", "unzip result " + result);
            cordovaWebView.loadUrl(result);
            progressDialog.dismiss();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "Incoming Result");
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d(TAG, "Request code = " + requestCode);
        if (systemWebView != null && requestCode == FILECHOOSER_RESULTCODE) {
        //    ValueCallback<Uri> mUploadMessage = this.webView.getWebChromeClient().getValueCallback();
            // For 5.x devices
        //    ValueCallback<Uri[]> mFilePathCallback = this.webView.getWebChromeClient().getfilePathsCallback();
            Log.d(TAG, "did we get here?");
            if ((null == mUploadMessage) && (null == mFilePathCallback))
                return;
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
            Log.d(TAG, "result = " + result);
//            Uri filepath = Uri.parse("file://" + FileUtils.getRealPathFromURI(result, this));
//            Log.d(TAG, "result = " + filepath);

            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            } else if (mFilePathCallback != null) {
                Uri[] result2 = WebChromeClient.FileChooserParams.parseResult(resultCode, intent);
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
            callback.onActivityResult(requestCode, resultCode, intent);
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

    @Override
    public ExecutorService getThreadPool() {
        return threadPool;
    }

}
