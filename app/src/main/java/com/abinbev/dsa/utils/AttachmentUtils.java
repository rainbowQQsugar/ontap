package com.abinbev.dsa.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.utils.crashreport.CrashReportManagerProvider;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsyncengine.datamanager.DownloadHelper;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okio.BufferedSink;
import okio.Okio;

/**
 * Created by lukaszwalukiewicz on 19.01.2016.
 */
public class AttachmentUtils {
    public static final int SELECT_PHOTO_REQUEST_CODE = 234;

    private static final String TAG = "AttachmentUtils";

    public static void openAttachment(Attachment attachment, Context context, String parentId) {
        File file = new File(attachment.getFilePath(context, parentId));
        final String mimeType = attachment.getContentType();
        String provider = context.getString(R.string.provider);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse("content://" + provider + "/" + file.getName());
        intent.setDataAndType(data, mimeType);
        if (isAvailable(context, intent)) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, R.string.no_available_app, Toast.LENGTH_LONG).show();
        }
    }

    public static void openUnsyncedAccountAttachment(Attachment attachment, Context context, String accountId) {
        String provider = context.getString(R.string.abi_provider);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse("content://" + provider + "/" + attachment.getFilePath(context, accountId));
        intent.setDataAndType(data, attachment.getContentType());
        if (isAvailable(context, intent)) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, R.string.no_available_app, Toast.LENGTH_LONG).show();
        }
    }

    public static void openUnsyncedCaseAttachment(Attachment attachment, Context context, String caseId) {
        String provider = context.getString(R.string.abi_provider);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse("content://" + provider + "/" + attachment.getCaseFilePath(context, caseId));
        intent.setDataAndType(data, attachment.getContentType());
        if (isAvailable(context, intent)) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, R.string.no_available_app, Toast.LENGTH_LONG).show();
        }
    }

    public static boolean isAvailable(Context ctx, Intent intent) {
        final PackageManager mgr = ctx.getPackageManager();
        List<ResolveInfo> list = mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static final int MEDIA_TYPE_IMAGE = 1;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "ABinBevApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("Babu", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static Uri fileUri;

    public static AlertDialog createPhotoChooserDialog(final Context context) {
        fileUri = null;
        CharSequence[] items = {context.getString(R.string.take_new_photo), context.getString(R.string.choose_existing)};

        return new AlertDialog.Builder(context)
        .setTitle(context.getString(R.string.choose_image_from))
        .setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Activity activity = (Activity) context;
                Intent intent;
                if (i == 0) {
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("image/*");
                }
                activity.startActivityForResult(intent, SELECT_PHOTO_REQUEST_CODE);
            }
        }).create();
    }

    public static void takePhoto(final Context context) {
        fileUri = null;
        Activity activity = (Activity) context;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        activity.startActivityForResult(intent, SELECT_PHOTO_REQUEST_CODE);
    }

    public static File downloadAttachmentFile(ClientManager clientManager, OkHttpClient client, Context context, Attachment attachment) throws IOException {
        if (TextUtils.isEmpty(attachment.getId())) {
            throw new IOException("Unable to download Attachment which doesn't have id.");
        }

        String newFileName = getAttachmentFileName(context, attachment);
        File newAttachmentFile = new File(context.getFilesDir(), newFileName);
        File tmpFile = new File(getAttachmentTempDir(context), newFileName);

        try {
            // Download attachment file.
            URL remoteImageUrl = getRemoteImageUrl(clientManager, attachment);
            okhttp3.Request httpRequest = new okhttp3.Request.Builder()
                    .url(remoteImageUrl)
                    .get()
                    .build();
            okhttp3.Response response = client.newCall(httpRequest).execute();

            // Throw exception if result was unsuccessful.
            if (response.code() / 100 != 2) {
                throw new IOException("Incorrect response code: " + response.code());
            }

            // Save attachment to tmp file
            BufferedSink sink = Okio.buffer(Okio.sink(tmpFile));
            sink.writeAll(response.body().source());
            sink.close();
            response.close();

            // Copy content to target file.
            FileUtils.copyFile(tmpFile, newAttachmentFile);

            Log.v(TAG, "Downloaded attachment to file: " + newAttachmentFile);
            return newAttachmentFile;
        }
        finally {
            tmpFile.delete();
        }
    }

    /** Get full URL of file from Attachment. */
    private static URL getRemoteImageUrl(ClientManager clientManager, Attachment attachment) throws MalformedURLException {
        RestClient restClient = clientManager.peekRestClient();
        URI completeURI = restClient.getClientInfo().resolveUrl(attachment.getBody());
        return completeURI.toURL();
    }

    public static String getAttachmentFileName(Context context, Attachment attachment) {
        return getAttachmentFileName(context, attachment.getId());
    }

    public static String getAttachmentFileName(Context context, String attachmentId) {
        return DownloadHelper.getFileName(context, AbInBevConstants.AbInBevObjects.ATTACHMENT, attachmentId);
    }

    private static File getAttachmentTempDir(Context context) {
        File file = new File(context.getFilesDir(), "attachmentTemp");
        if (!file.exists()) {
            file.mkdir();
        }

        return file;
    }

    private static final int COMPRESSED_PICTURE_SIZE = 1200;
    private static final int COMPRESSED_PICTURE_QUALITY = 90;

    public static boolean compressImage(Context context, File file){
        return compressImage(context, file, file);
    }

    public static boolean compressImage(Context context, File srcFile, File destFile){
        try {
            Bitmap resizedBitmap = Picasso.with(context)
                    .load(srcFile)
                    .resize(COMPRESSED_PICTURE_SIZE, COMPRESSED_PICTURE_SIZE)
                    .centerInside()
                    .get();

            return storeBitmap(resizedBitmap, destFile, COMPRESSED_PICTURE_QUALITY);
        } catch (Exception e) {
            CrashReportManagerProvider.getInstance().logException(e);
            Log.w(TAG, e);
            return false;
        }
    }

    public static boolean compressImage(Context context, Uri srcUri, File destFile){
        try {
            Bitmap resizedBitmap = Picasso.with(context)
                    .load(srcUri)
                    .resize(COMPRESSED_PICTURE_SIZE, COMPRESSED_PICTURE_SIZE)
                    .centerInside()
                    .get();

            return storeBitmap(resizedBitmap, destFile, COMPRESSED_PICTURE_QUALITY);
        } catch (Exception e) {
            CrashReportManagerProvider.getInstance().logException(e);
            Log.w(TAG, e);
            return false;
        }
    }

    private static boolean storeBitmap(Bitmap bitmap, File destFile, int quality) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(destFile);

        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        outputStream.close();

        if (destFile.length() == 0) {
            Log.e(TAG, "got zero length file after compress!");
            return false;
        } else {
            Log.e(TAG, "non zero length file after compress");
            return true;
        }
    }
}
