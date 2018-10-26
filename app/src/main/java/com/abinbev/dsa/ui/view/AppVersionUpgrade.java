package com.abinbev.dsa.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.CN_App_Version__c;
import com.abinbev.dsa.utils.DownLoadUtils;
import com.salesforce.androidsyncengine.utils.DeviceNetworkUtils;
import com.salesforce.dsa.app.ui.dialogs.DownDialog;
import java.io.File;

/**
 * version upgrade
 */
public class AppVersionUpgrade {


    private static AppVersionUpgrade appVersionUpgrade = null;

    private String TAG = this.getClass().getSimpleName();
    private DownDialog downDialog;


    public DownDialog getDownDialog() {
        return downDialog;
    }

    public void setDownDialog(DownDialog downDialog) {
        this.downDialog = downDialog;
    }

    private AppVersionUpgrade(){
    }

    public static AppVersionUpgrade getInstance() {

        try {

            if (appVersionUpgrade == null) {

                synchronized (AppVersionUpgrade.class) {

                    if (appVersionUpgrade == null) {
                        appVersionUpgrade = new AppVersionUpgrade();
                    }
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return appVersionUpgrade;
    }


    /**
     * Check for version updates
     */
    public void checkAppVersion(Context context) {
        Log.e(TAG, "checkAppVersion");
        if (downDialog != null && downDialog.isShow())
            return;
        try {
            CN_App_Version__c c = CN_App_Version__c.getchVersionControlerAllData();

            if (c != null) {

                String versionName_L = getVersionName(context);
                String versionName_S = c.getName();
                Log.e(TAG, "versionCode:" + getVersionCode(context) + " - versionName:" + getVersionName(context) + " - Name:" + versionName_S);
                if (!TextUtils.isEmpty(versionName_L) && !TextUtils.isEmpty(versionName_L) && !versionName_L.equalsIgnoreCase(versionName_S)) {

                    String downLoadLinks = c.getDownLoadLink();
                    String msg = String.format(context.getResources().getString(R.string.dialog_version_tip), versionName_S);
                    String confirm = context.getResources().getString(R.string.confirm);


                    if (downDialog == null) {
                        downDialog = new DownDialog(context).builder()
                                .setMessage(msg)
                                .setPoBtn(confirm, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        if (downDialog != null) {

                                            if (!downDialog.isShow())
                                                downDialog.show();

                                            downDialog.setVisibilityProgressBar(true);
                                            downDialog.setVisibilityBtn(false);
                                            downDialog.setProgress(0, 0, 0);

                                        }

//                                        Toast.makeText(context, getResources().getString(R.string.sync_data_success), Toast.LENGTH_SHORT).show();
                                        downLoadApk(downLoadLinks, downDialog, context);
                                    }
                                }).setCancelable(false);
                        // Listen for Dialog return events
                        downDialog.setOnKeyListener((Activity) context);
                    }
                    downDialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "checkAppVersion:" + e.getMessage());
        }
    }

    /**
     * downLoad Apk Url
     *
     * @param url
     */
    private void downLoadApk(String url, DownDialog dialog, Context context) {

        try {

            if (DeviceNetworkUtils.isConnected(context)) {

                DownLoadUtils.getInstance().downLoadFile((Activity) context, url, context.getExternalFilesDir(null).getAbsolutePath(), new DownLoadUtils.DownLoadListener() {
                    @Override
                    public void onDownLoadSuccess(String downFilePath) {

                        if (dialog != null)
                            dialog.dismiss();

                        Toast.makeText(context, context.getResources().getString(R.string.down_load_success), Toast.LENGTH_SHORT).show();
                        install(downFilePath, url, dialog, context);
                    }

                    @Override
                    public void onDownLoadProgress(float total, float progress, float percent) {
//                    Log.e(TAG, "total:" + total + " - progress:" + progress + " - percent:" + percent);
                        if (dialog != null) {

                            if (!dialog.isShow())
                                dialog.show();

                            dialog.setVisibilityProgressBar(true);
                            dialog.setVisibilityBtn(false);
                            dialog.setProgress(total, progress, percent);

                        }
                    }

                    @Override
                    public void onDownLoadFail(String error) {
//                    Log.e(TAG, "onDownLoadFail:"+error);
                        Toast.makeText(context, context.getResources().getString(R.string.down_load_fail), Toast.LENGTH_SHORT).show();
                        if(downDialog != null)
                            downDialog.dismiss();
                        downDialog = null;
                        checkAppVersion(context);

                    }
                });
            } else {

                Toast.makeText(context, context.getResources().getString(R.string.please_net_status), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "downLoadApk Error:" + e.getMessage());
        }

    }

    /**
     * install
     *
     * @param filePath
     */
    private void install(String filePath, String url, DownDialog dialog, Context context) {

        try {

            if (getUninatllApkInfo(context, filePath)) {
                File apkFile = new File(filePath);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri contentUri = FileProvider.getUriForFile(
                            context
                            , "com.abinbev.dsa.fileprovider"
                            , apkFile);
                    intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                } else {
                    intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                }
                context.startActivity(intent);

                if (downDialog != null)
                    downDialog.dismiss();
                downDialog = null;
            } else {
                Toast.makeText(context, R.string.file_damage, Toast.LENGTH_SHORT).show();
                File file = new File(filePath);
                Log.e(TAG, "-----" + file.getAbsolutePath());
                if (!file.exists()) {
                    file.delete();
                }
                checkAppVersion(context);
            }

        } catch (Exception e) {

            Log.e(TAG, "" + e.getMessage());
            /**
             * Files are lossy to delete files
             */
            File file = new File(filePath);
            if (!file.exists())
                file.delete();
        } finally {
            File file = new File(filePath);
            if (!file.exists())
                file.delete();
        }
    }

    private boolean getUninatllApkInfo(Context context, String filePath) {
        boolean result = false;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
            if (info != null) {
                result = true;//
            }
        } catch (Exception e) {
            result = false;//
        }
        return result;
    }

    /**
     * Get application information VersionName
     *
     * @return
     */
    private String getVersionName(Context context) {

        String versionName = "";

        try {

            versionName = context.getApplicationContext().getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return versionName;
    }

    /**
     * Get application information VersionCode
     *
     * @return
     */
    private int getVersionCode(Context context) {

        int versionCode = -1;
        try {
            versionCode = context.getApplicationContext().getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }


}
