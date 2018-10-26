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

import android.util.Log;

import com.salesforce.androidsdk.phonegap.plugin.ForcePlugin;
import com.salesforce.androidsdk.phonegap.plugin.JavaScriptPluginVersion;
import com.salesforce.dsa.DSAAppState;
import com.salesforce.dsa.data.model.Contact;
import com.salesforce.dsa.utils.DataUtils;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * PhoneGap plugin
 */
public class DSAContactPlugin extends ForcePlugin {


    private static final String TAG="DSAContactPlugin";
    
    /**
     * Supported plugin actions that the client can take.
     */
    enum Action {
        checkedInContact,
        searchContact
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
                synchronized (DSAContactPlugin.class) {
                    try {
                        switch (action) {
                            case checkedInContact:
                                getCheckedInContact(args, callbackContext);
                                break;
                            case searchContact:
                                searchContact(args, callbackContext);
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
    protected void getCheckedInContact(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "getCheckInContact");
        try {
            Contact checkedInContact = DSAAppState.getInstance().getCurrentTrackingContact();

            String contactName = "-";
            if (checkedInContact != null) {
                contactName = checkedInContact.getFirstName() + " " + checkedInContact.getLastName();
                Log.i(TAG, contactName);
            }

            JSONObject jo = new JSONObject();

            JSONArray ja = new JSONArray();

            if (checkedInContact != null) {
                ja.put(checkedInContact.toJson());
            }

            jo.put("ContactList", ja);

            callbackContext.success(jo);
        }
        catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    /**
     * @param callbackContext Used when calling back into Javascript.
     * @throws JSONException
     */
    protected void searchContact(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "searchContact with args: " + args.toString());
        try {

            String searchString = ((JSONObject) (args.get(0))).getString("searchString");

            if (searchString == null || searchString.trim().equals("") || searchString.trim().equals("null")) {
                callbackContext.error("Cannot search for null or empty string");
            }

            Log.i(TAG, "searchContact searchString: " + searchString);

            List<Contact> searchResultContacts = DataUtils.fetchContacts(searchString.trim());

            Log.i(TAG, "searchContact list size: " + searchResultContacts.size());

            JSONObject jo = new JSONObject();

            JSONArray ja = new JSONArray();

            for (Contact contact : searchResultContacts) {
                ja.put(contact.toJson());
            }

            jo.put("ContactList", ja);

            callbackContext.success(jo);
        } catch (Exception e) {
            Log.e(TAG, "exception : " + e.getMessage());
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }

}}
