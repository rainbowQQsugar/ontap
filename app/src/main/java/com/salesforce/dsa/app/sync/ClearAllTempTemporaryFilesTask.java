package com.salesforce.dsa.app.sync;

import android.os.AsyncTask;
import android.util.Log;

import com.salesforce.dsa.app.ui.activity.DigitalSalesAid;

import java.io.File;

public class ClearAllTempTemporaryFilesTask extends AsyncTask<String, Void, Void> {


    private String TAG = getClass().getSimpleName();

    @Override
    protected Void doInBackground(String... paths) {
        File file = new File(paths[0]);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files == null) return null;
            for (int i = 0; i < files.length; i++) {
                String name = files[i].getName();
                if (name.contains(DigitalSalesAid.TEMP_SUFFIX)) {
                    files[i].delete();
                }
            }
        }
        return null;
    }
}
