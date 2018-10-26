package com.salesforce.dsa.app.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.utils.AzureUtils;
import com.microsoft.azure.storage.blob.BlockEntry;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.File;
import java.util.ArrayList;

public class DownloadDsaFileTask extends AsyncTask<String, Void, Boolean> {

    private final Context context;
    private DownloadDsaFileCallBack downloadDsaFileCallBack;
    private String TAG = getClass().getSimpleName();

    private final File tempFile;
    private String completeFileName;

    public DownloadDsaFileTask(Context context, File tempFile, String completeFileName) {
        this.context = context;
        this.tempFile = tempFile;
        this.completeFileName = completeFileName;
    }


    @Override
    protected Boolean doInBackground(String... params) {
        try {
            if (params.length != 0 || params.length > 1) {
                return downloadFromAzure(params[0], params[1]);
            } else return false;
        } catch (Exception e) {
            Log.e(TAG, "DownloadDsaFileTask doInBackground error: " + e);
            if (tempFile.exists()) {
                tempFile.delete();
            }
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (downloadDsaFileCallBack != null) downloadDsaFileCallBack.downloadResult(aBoolean);
        Toast.makeText(context,
                context.getResources().getString(aBoolean ? R.string.downlaod_done : R.string.download_failed),
                Toast.LENGTH_SHORT).show();
        if (tempFile.exists()) {
            renameFile(tempFile, completeFileName);
        }
    }

    public interface DownloadDsaFileCallBack {
        void downloadResult(boolean isSucess);
    }

    private boolean downloadFromAzure(String SalesforceId, String path) {
        try {

            CloudBlockBlob fileBlob = AzureUtils.getAzureBlobContainer()
                    .getBlockBlobReference(SalesforceId);
            ArrayList<BlockEntry> blockEntries = fileBlob.downloadBlockList();
            Log.e("BlobDownload3", "downloading: " + fileBlob.getUri() + " to: " + path);
            fileBlob.downloadToFile(path);
        } catch (Exception e) {
            Log.e(TAG, "got exception while downloading from Azure " + e.getMessage());
            if (tempFile.exists()) {
                tempFile.delete();
            }
            return false;
        }
        return true;
    }

    public void setDownloadDsaFileCallBack(DownloadDsaFileCallBack downloadDsaFileCallBack) {
        this.downloadDsaFileCallBack = downloadDsaFileCallBack;
    }

    private void renameFile(File file, String newPath) {
        if (TextUtils.isEmpty(newPath)) {
            return;
        }
        if (!file.exists())
            return;
        file.renameTo(new File(context.getExternalFilesDir(null), newPath));
    }
}
