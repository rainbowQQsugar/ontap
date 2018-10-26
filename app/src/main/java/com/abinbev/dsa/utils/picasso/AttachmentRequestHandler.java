package com.abinbev.dsa.utils.picasso;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.utils.AttachmentUtils;
import com.salesforce.androidsdk.rest.ClientManager;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;

import static com.squareup.picasso.Picasso.LoadedFrom.DISK;
import static com.squareup.picasso.Picasso.LoadedFrom.NETWORK;

/**
 * Picasso RequestHandler that is able to handle uris with 'attachment' scheme.
 *
 * Created by Jakub Stefanowski on 21.07.2016.
 */
public class AttachmentRequestHandler extends RequestHandler {

    private static final String TAG = AttachmentRequestHandler.class.getSimpleName();

    private static final String SCHEME_ATTACHMENT = "attachment";

    private static final int SCHEME_PREFIX_LENGTH = (SCHEME_ATTACHMENT + "://").length();

    private final ClientManager clientManager;

    private final OkHttpClient client;

    public AttachmentRequestHandler(OkHttpClient client, ClientManager clientManager) {
        this.clientManager = clientManager;
        this.client = client;
    }

    @Override
    public boolean canHandleRequest(Request data) {
        String scheme = data.uri.getScheme();
        return SCHEME_ATTACHMENT.equals(scheme);
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        String[] uriParts = getUriParts(request);
        String accountId = uriParts[0];
        String attachmentId = uriParts[1];
        Log.v(TAG, "Loading image for attachment id: " + attachmentId + " account id: " + accountId);
        Attachment attachment = Attachment.getById(attachmentId);

        if (attachment == null) {
            Log.v(TAG, "Attachment with id: " + attachmentId + " does not exist.");
            return null;
        }

        String fileName = attachment.getFilePath(getContext(), accountId);
        if (!TextUtils.isEmpty(fileName)) {
            File attachmentFile = new File(fileName);
            if (attachmentFile.exists()) {
                Log.v(TAG, "Attachment file " + attachmentFile + " already exists.");
                return new Result(getFileInputStream(Uri.fromFile(attachmentFile)), DISK);
            }
        }

        File file = AttachmentUtils.downloadAttachmentFile(clientManager, client, getContext(), attachment);
        Log.v(TAG, "Downloaded file location: " + file);

        return new Result(getFileInputStream(Uri.fromFile(file)), NETWORK);
    }

    /** Read uri path parts from request. */
    private String[] getUriParts(Request request) {
        String path = request.uri.toString().substring(SCHEME_PREFIX_LENGTH);
        return path.split("/");
    }

    /** Returns InputStream to local file. */
    private InputStream getFileInputStream(Uri uri) throws FileNotFoundException {
        ContentResolver contentResolver = getContext().getContentResolver();
        return contentResolver.openInputStream(uri);
    }

    private Context getContext() {
        return ABInBevApp.getAppContext();
    }
}
