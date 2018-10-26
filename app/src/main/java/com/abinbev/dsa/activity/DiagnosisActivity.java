package com.abinbev.dsa.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import com.abinbev.dsa.R;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AppPreferenceUtils;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.smartstore.store.SmartStore;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import java.io.File;
import java.util.List;

public class DiagnosisActivity extends AppBaseActivity {

    private static final String TAG = DiagnosisActivity.class.getSimpleName();

    private DataManager dataManager;
    private EditText dataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.app_name);

        dataView = (EditText) findViewById(R.id.dataView);
    }

    @Override
    public void onResume() {
        super.onResume();
        dataManager = DataManagerFactory.getDataManager();
        updateUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.diagnosis_layout;
    }

    public void updateUI() {
        StringBuilder sb = new StringBuilder();

        try {

            if (dataManager == null) {
                return;
            }

            ClientManager clientManager = new ClientManager(this, SalesforceSDKManager.getInstance()
                    .getAccountType(), SalesforceSDKManager.getInstance().getLoginOptions(), SalesforceSDKManager
                    .getInstance().shouldLogoutWhenTokenRevoked());

            String databaseSize = android.text.format.Formatter.formatShortFileSize(this, DataManagerFactory.getDataManager().getSmartStore().getDatabaseSize());

            RestClient client = clientManager.peekRestClient();

            File downloadedSyncFile = new File(getFilesDir(), ManifestUtils.DOWNLOADED_SYNC_FILE);

            String userName = client.getClientInfo().username;

            SmartStore smartStore = DataManagerFactory.getDataManager().getSmartStore();
            List<String> soupNames = smartStore.getAllSoupNames();

            // Basic info

            sb.append("User: " + userName + "\n");
            sb.append("Profile: " + AppPreferenceUtils.getUserProfile(this) + "\n");
            sb.append("Database size: " + databaseSize + "\n");
            sb.append("Downloaded sync manfiest: " + downloadedSyncFile.exists() + "\n");
            sb.append("Number of Objects: " + soupNames.size() + "\n");


            // Records info

            sb.append("\n===== Records =====\n");
            for (String soupName : soupNames) {
                int count = dataManager.getRecordCount(soupName, "Id");
                sb.append(soupName).append(": ").append(count).append("\n");
            }


            // Files info

            sb.append("\n===== Files =====\n");
            File tempDir = new File(getFilesDir(), "temp");
            if (tempDir.exists()) {
                for (File file: tempDir.listFiles()) {
                    if (file.isDirectory()) {
                        String directoryName = getObjectFromDirName(file.getName());
                        int filesCount = countFilesIn(file);
                        sb.append(directoryName).append(": ").append(filesCount).append('\n');
                    }
                    else {
                        sb.append(file.getName()).append('\n');
                    }
                }
            }

        } catch (Exception e) {
            Log.w(TAG, e);
            sb.append("Got Exception!" + e.getMessage());
        }

        dataView.setText(sb.toString());
    }

    private static String getObjectFromDirName(String dirName) {
        if ("account".equals(dirName)) {
            return AbInBevObjects.ACCOUNT;
        }
        else if ("asset".equals(dirName)) {
            return AbInBevObjects.ACCOUNT_ASSET_C;
        }
        else if ("case".equals(dirName)) {
            return AbInBevObjects.CASE;
        }
        else if ("survey_qr".equals(dirName)) {
            return AbInBevObjects.SURVEY_Question_Response;
        }
        else if ("survey_question".equals(dirName)) {
            return AbInBevObjects.SURVEY_Question;
        }
        else if ("event".equals(dirName)) {
            return AbInBevObjects.EVENT;
        }
        else {
            return dirName;
        }
    }

    private int countFilesIn(File dir) {
        int result = 0;

        for (File file: dir.listFiles()) {
            if (file.isDirectory()) {
                result += countFilesIn(file);
            }
            else {
                result++;
            }
        }

        return result;
    }
}
