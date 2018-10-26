package com.abinbev.dsa.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.abinbev.dsa.service.LogoutService;

/**
 * Receives broadcast telling that token is revoked and starts LogoutService.
 *
 * Created by Jakub Stefanowski on 09.11.2016.
 */
public class TokenRevokedReceiver extends BroadcastReceiver {

    private static final String TAG = "TokenRevokedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Received access token revoked intent.");
        context.startService(new Intent(context, LogoutService.class));
    }
}
