package com.abinbev.dsa.utils.okhttp;

import com.salesforce.androidsdk.rest.ClientManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor which sets authorization tokens to each request.
 *
 * Created by Jakub Stefanowski on 22.07.2016.
 */
public class AuthInterceptor implements Interceptor {

    private final ClientManager clientManager;

    public AuthInterceptor(ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = clientManager.peekRestClient().getAuthToken();
        Request newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(newRequest);
    }
}
