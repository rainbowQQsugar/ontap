/*
 * Copyright (c) 2012, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.dsa.phonegap;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.salesforce.androidsdk.phonegap.plugin.ForcePlugin;
import com.salesforce.androidsdk.phonegap.plugin.JavaScriptPluginVersion;
import com.salesforce.dsa.utils.PlugInUtils;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * PhoneGap plugin
 */
public class DSANavigationPlugin extends ForcePlugin {

    private static final String TAG="DSANavigationPlugin";
    
    /**
     * Supported plugin actions that the client can take.
     */
    enum Action {
        openURL
    }

    @Override
    public boolean execute(String actionStr, JavaScriptPluginVersion jsVersion,
                           final JSONArray args, final CallbackContext callbackContext) throws JSONException {

        final long start = System.currentTimeMillis();

        // Figure out action
        final Action action;
        try {
            action = Action.valueOf(actionStr);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unknown action " + actionStr);
            return false;
        }

        // Not running smartstore action on the main thread
        cordova.getThreadPool().execute(new Runnable() {

            @Override
            public void run() {
                // All actions need to be serialized
                // Overkill right now ... remove after testing
                synchronized (DSANavigationPlugin.class) {
                    try {
                        switch (action) {
                            case openURL:
                                openURL(args, callbackContext);
                                break;
                            default:
                                callbackContext.error(String.format("No handler for action %s", action));
                        }
                    } catch (Exception e) {
                            Log.w(TAG, e.getMessage(), e);
                            callbackContext.error(e.getMessage());
                    }
                        Log.d(TAG, "Total time for " + action + "->" + (System.currentTimeMillis() - start));
                    }
                }
            });

        Log.d(TAG, "Main thread time for " + action + "->" + (System.currentTimeMillis() - start));
        return true;
    }


    /**
     * @param callbackContext Used when calling back into Javascript.
     * @throws JSONException
     */
    protected void openURL(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "openURL : " + args);
        try {

            JSONObject jsonObject = ((JSONObject) (args.get(0)));
            String url = jsonObject.getString("url");
            boolean sso = jsonObject.getBoolean("sso");
            boolean externalBrowser = jsonObject.getBoolean("externalBrowser");

            Context context = cordova.getActivity();

            //TODO: Test this. We need to pass the relevant WebViewActivity as the last parameter
            Intent intent = PlugInUtils.getNavigationIntent(context, url, sso, externalBrowser, cordova.getActivity().getClass());


            context.startActivity(intent);

            JSONObject data = new JSONObject();
            data.put("success", "true");
            callbackContext.success(data);

            // cordova.getActivity()
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }

}}
