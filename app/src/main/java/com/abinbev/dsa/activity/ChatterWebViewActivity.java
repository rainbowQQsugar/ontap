package com.abinbev.dsa.activity;

/**
 * Created by bduggirala on 11/2/16.
 */
/**
 * WebView activity to handle HTML5 Content
 *
 * @author bduggirala
 */


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.abinbev.dsa.BuildConfig;
import com.abinbev.dsa.R;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;

import java.net.URLEncoder;


public class ChatterWebViewActivity extends AppBaseActivity {

    public static final String URL = "Url";
    private static final int FILECHOOSER_RESULTCODE = 5173;

    private static String TAG = "ChatterWebViewActivity";

    private String title;

    private ProgressDialog progressDialog;
    private WebView webView;
    private String initialUrl;
    private boolean cleanHistoryOnPageLoaded = false;

    @SuppressLint("SetJavaScriptEnabled")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configures options for the progress indicator.
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading HTML5 Content ...");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);

        webView = (WebView) findViewById(R.id.webView);

        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            private static final String PARAM_ERROR_CODE = "ec";
            private static final String PARAM_START_URL = "startURL";

            private static final String STATUS_CODE_REDIRECT = "302";

            private static final String SALESFORCE_HOST = "salesforce.com";

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (isDestroyed()) return;

                if (requiresSessionUpdate(url)) {
                    Log.i(TAG, "Session update is required (currentUrl=" + url + ")");
                    webView.stopLoading();

                    progressDialog.setTitle(getString(R.string.loading_chatter));
                    progressDialog.show();
                    new authenticateWebViewTask().execute(initialUrl);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (isDestroyed()) return;

                // Required after session update.
                if (cleanHistoryOnPageLoaded) {
                    webView.clearHistory();
                    cleanHistoryOnPageLoaded = false;
                }
            }

            private boolean requiresSessionUpdate(String pageUrl) {
                Uri uri = Uri.parse(pageUrl);
                String host = uri.getHost();

                // Check if we are on salesforce website.
                if (!TextUtils.isEmpty(host) && host.toLowerCase().contains(SALESFORCE_HOST)) {
                    String errorCode = uri.getQueryParameter(PARAM_ERROR_CODE);

                    return STATUS_CODE_REDIRECT.equals(errorCode);
                }

                return false;
            }
        });
        webView.setWebChromeClient(new ABIWebChromeClient());

        processIntent(getIntent());
        setupToolbar();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            if (webView != null) {
                webView.setWebViewClient(new WebViewClient());
                webView.destroy();
            }
        } catch (Exception e) {
            // Ignore any exceptions on handleDestroy
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.chatter_webview;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            //if Back key pressed and webview can navigate to previous page
            webView.goBack();
            // go back to previous page
            return true;
        }
        else
        {
            finish();
            // finish the activity
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
        Bundle extras = newIntent.getExtras();
        if (extras != null) {
            initialUrl = extras.getString(URL);

            ClientManager clientManager = new ClientManager(this, SalesforceSDKManager.getInstance()
                    .getAccountType(), SalesforceSDKManager.getInstance().getLoginOptions(), SalesforceSDKManager
                    .getInstance().shouldLogoutWhenTokenRevoked());

            RestClient client = clientManager.peekRestClient();

            String instanceUrl = client.getClientInfo().getInstanceUrl().toString();
            Uri uriToOpen = Uri.parse(instanceUrl).buildUpon().encodedPath(initialUrl).build();
            uriToOpen = uriToOpen.buildUpon().encodedPath(initialUrl).build();

            webView.loadUrl(uriToOpen.toString());
            title = getString(R.string.chatter);
        }
    }

    private void setupToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(title);
        }
    }

    private void authenticateIntoWebView(String url) {
        try {
            ClientManager clientManager = new ClientManager(this,
                    SalesforceSDKManager.getInstance().getAccountType(),
                    SalesforceSDKManager.getInstance().getLoginOptions(),
                    SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());

            RestClient client = clientManager.peekRestClient();

            String sessionId = URLEncoder.encode(client.getAuthToken(), "utf-8");
            String encodedURL = URLEncoder.encode(url, "utf-8");

            String frontDoorUrl = client.getClientInfo().getInstanceUrlAsString() + "/secur/frontdoor.jsp?retURL=" + encodedURL + "&display=touch&sid=";
            String newUrl = frontDoorUrl + sessionId;

            webView.loadUrl(newUrl);
            Log.e(TAG, "newUrl is: " + newUrl);
            cleanHistoryOnPageLoaded = true;

        } catch (Exception e) {
            Log.e(TAG, "Error in authenticateIntoWebView : " + e.getMessage());
        }

    }

    private class authenticateWebViewTask extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... urls) {

            try {
                // the below is to ensure that we get a new auth token if the current auth token has expired
                ClientManager clientManager = new ClientManager(ChatterWebViewActivity.this, SalesforceSDKManager.getInstance()
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
            if (ChatterWebViewActivity.this.isDestroyed()) {
                return;
            }

            if (result != null) {
                authenticateIntoWebView(result);
            }

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
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

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "Incoming Result");
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d(TAG, "Request code = " + requestCode);
        if (webView != null && requestCode == FILECHOOSER_RESULTCODE) {
            // For 5.x devices
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Uri[] result2 = WebChromeClient.FileChooserParams.parseResult(resultCode, intent);
                ValueCallback<Uri[]> mFilePathCallback = this.mUploadMessage5;
                Log.d(TAG, "did we get here?");
                if (null == mFilePathCallback) {
                    return;
                }
                mFilePathCallback.onReceiveValue(result2);
                mUploadMessage5 = null;
            } else {
                Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
                Log.d(TAG, "result = " + result);
                if (mUploadMessage4 != null) {
                    mUploadMessage4.onReceiveValue(result);
                    mUploadMessage4 = null;
                }

            }
        }
    }

    // File Chooser
    public ValueCallback<Uri> mUploadMessage4;
    private ValueCallback<Uri[]> mUploadMessage5;

    private class ABIWebChromeClient extends WebChromeClient {

        // For Lollipop 5.0+ Devices
        @Override
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            if (mUploadMessage5 != null) {
                mUploadMessage5.onReceiveValue(null);
                mUploadMessage5 = null;
            }

            mUploadMessage5 = filePathCallback;
            Intent intent = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent = fileChooserParams.createIntent();
            }
            try {
                startActivityForResult(intent, FILECHOOSER_RESULTCODE);
            } catch (ActivityNotFoundException e) {
                mUploadMessage5 = null;
                Toast.makeText(getApplicationContext(), "No se puede abrir selector de archivos", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }


        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
            mUploadMessage4 = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            ChatterWebViewActivity.this.startActivityForResult( Intent.createChooser( i, "File Chooser" ), FILECHOOSER_RESULTCODE );

        }
    }
}


