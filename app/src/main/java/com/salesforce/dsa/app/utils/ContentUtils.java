package com.salesforce.dsa.app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;

import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.app.R;
import com.salesforce.dsa.app.ui.activity.WebViewActivity;
import com.salesforce.dsa.data.model.CategoryMobileConfig__c;
import com.salesforce.dsa.data.model.Category__c;
import com.salesforce.dsa.data.model.ContentVersion;
import com.salesforce.dsa.data.model.MobileAppConfig__c;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Util class to assist with content operations
 *
 * @author bduggirala
 */

public class ContentUtils {


    private static String TAG = "ContentUtils";
    private static final int BUFFER_SIZE = 8192;

    private ContentUtils() {
    }

    ;

    public static String getMimeType(String fileExtension) {
//		String mimeType = FileUtils.getMimeType(fileExtension);
        if ("PDF".equalsIgnoreCase(fileExtension)) {
            return "application/pdf";
        } else if ("ZIP".equalsIgnoreCase(fileExtension)) {
            return "application/zip";
        } else if ("WORD".equalsIgnoreCase(fileExtension)) {
            return "application/msword";
        } else if ("POWER_POINT".equalsIgnoreCase(fileExtension)) {
            return "application/vnd.ms-powerpoint";
        } else if ("EXCEL".equalsIgnoreCase(fileExtension)) {
            return "application/vnd.ms-excel";
        } else if ("WORD_X".equalsIgnoreCase(fileExtension)) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if ("EXCEL_X".equalsIgnoreCase(fileExtension)) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if ("POWER_POINT_X".equalsIgnoreCase(fileExtension)) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        } else if ("JPEG".equalsIgnoreCase(fileExtension) || "JPG".equalsIgnoreCase(fileExtension) || "PNG".equalsIgnoreCase(fileExtension) ||
                "BMP".equalsIgnoreCase(fileExtension)) {
            return ("image/*");
        } else if ("LINK".equalsIgnoreCase(fileExtension)) {
            return ("application/xhtml+xml");
        } else if ("MP4".equalsIgnoreCase(fileExtension)) {
            return ("video/mp4");
        } else {
            return "*/*";
        }
    }

    public static int getDrawableResourceId(String fileExtension) {
        if ("PDF".equalsIgnoreCase(fileExtension)) {
            return R.drawable.pdf;
        } else if ("ZIP".equalsIgnoreCase(fileExtension)) {
            return R.drawable.zip;
        } else if ("WORD".equalsIgnoreCase(fileExtension)) {
            return R.drawable.doc;
        } else if ("POWER_POINT".equalsIgnoreCase(fileExtension)) {
            return R.drawable.ppt;
        } else if ("EXCEL".equalsIgnoreCase(fileExtension)) {
            return R.drawable.xls;
        } else if ("WORD_X".equalsIgnoreCase(fileExtension)) {
            return R.drawable.docx;
        } else if ("EXCEL_X".equalsIgnoreCase(fileExtension)) {
            return R.drawable.xlsx;
        } else if ("POWER_POINT_X".equalsIgnoreCase(fileExtension)) {
            return R.drawable.pptx;
        } else if ("JPEG".equalsIgnoreCase(fileExtension) || "JPG".equalsIgnoreCase(fileExtension) || "PNG".equalsIgnoreCase(fileExtension) ||
                "BMP".equalsIgnoreCase(fileExtension)) {
            return R.drawable.png;
        } else if ("LINK".equalsIgnoreCase(fileExtension)) {
            return R.drawable.link;
        } else if ("MP4".equalsIgnoreCase(fileExtension)) {
            return R.drawable.mov;
        } else {
            Log.e(TAG, "No resource for extension: " + fileExtension);
            return R.drawable.pdf;
        }
    }

    public static int getDrawableResource(String fileExtension) {
        if ("PDF".equalsIgnoreCase(fileExtension)) {
            return R.drawable.pdf_icon;
        } else if ("ZIP".equalsIgnoreCase(fileExtension)) {
            return R.drawable.zip;
        } else if ("WORD".equalsIgnoreCase(fileExtension)) {
            return R.drawable.word;
        } else if ("POWER_POINT".equalsIgnoreCase(fileExtension)) {
            return R.drawable.ppt_icon;
        } else if ("EXCEL".equalsIgnoreCase(fileExtension)) {
            return R.drawable.excel_icon;
        } else if ("WORD_X".equalsIgnoreCase(fileExtension)) {
            return R.drawable.word;
        } else if ("EXCEL_X".equalsIgnoreCase(fileExtension)) {
            return R.drawable.excel_icon;
        } else if ("POWER_POINT_X".equalsIgnoreCase(fileExtension)) {
            return R.drawable.ppt_icon;
        } else if ("JPEG".equalsIgnoreCase(fileExtension) || "JPG".equalsIgnoreCase(fileExtension) || "PNG".equalsIgnoreCase(fileExtension) ||
                "BMP".equalsIgnoreCase(fileExtension)) {
            return R.drawable.pictrues;
        } else if ("LINK".equalsIgnoreCase(fileExtension)) {
            return R.drawable.link;
        } else if ("MP4".equalsIgnoreCase(fileExtension)) {
            return R.drawable.video;
        } else {
            Log.e(TAG, "No resource for extension: " + fileExtension);
            return R.drawable.interrogation_mark;
        }
    }


    public static String fileNameSuffix(String fileExtension) {
        if ("PDF".equalsIgnoreCase(fileExtension)) {
            return ".pdf";
        } else if ("ZIP".equalsIgnoreCase(fileExtension)) {
            return ".zip";
        } else if ("WORD".equalsIgnoreCase(fileExtension)) {
            return ".doc";
        } else if ("POWER_POINT".equalsIgnoreCase(fileExtension)) {
            return ".pptx";
        } else if ("EXCEL".equalsIgnoreCase(fileExtension)) {
            return ".xlsx";
        } else if ("WORD_X".equalsIgnoreCase(fileExtension)) {
            return "docx";
        } else if ("EXCEL_X".equalsIgnoreCase(fileExtension)) {
            return ".xlsx";
        } else if ("POWER_POINT_X".equalsIgnoreCase(fileExtension)) {
            return ".pptx";
        } else if ("JPEG".equalsIgnoreCase(fileExtension)) {
            return ".jpeg";
        } else if ("PNG".equalsIgnoreCase(fileExtension)) {
            return ".png";
        } else if ("JPG".equalsIgnoreCase(fileExtension)) {
            return ".jpg";
        } else if ("BMP".equalsIgnoreCase(fileExtension)) {
            return ".bmp";
        } else if ("MP4".equalsIgnoreCase(fileExtension)){
            return ".mp4";
        }else {
            return "";
        }
    }

    public static Intent getContentIntent(Context context, File file, String type) {
        String mimeType = getMimeType(type);
        if ("JPEG".equalsIgnoreCase(type) || "JPG".equalsIgnoreCase(type) || "PNG".equalsIgnoreCase(type) ||
                "BMP".equalsIgnoreCase(type) || "word".equalsIgnoreCase(type) ||
                "power_point".equalsIgnoreCase(type) || "Excel".equalsIgnoreCase(type) || "pdf".equalsIgnoreCase(type)
                || "WORD_X".equalsIgnoreCase(type) || "EXCEL_X".equalsIgnoreCase(type) || "POWER_POINT_X".equalsIgnoreCase(type)) {
            return generateCommonIntent(context, file, mimeType);
        } else if ("html".equalsIgnoreCase(type)) {
            Intent intent = new Intent(context, WebViewActivity.class);
            intent.putExtra("FileExtension", "html");
            intent.putExtra("Url", file.getAbsolutePath());
            return intent;
        } else if ("MP4".equalsIgnoreCase(type)) {
            return generateVideoAudioIntent(context, file, mimeType);
        }
        return null;
    }

    private static Intent generateVideoAudioIntent(Context context, File file, String dataType) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        intent.setDataAndType(getUri(context, intent, file), dataType);
        return intent;
    }

    public static Intent generateHtmlFileIntent(String filePath) {
        Uri uri = Uri.parse(filePath)
                .buildUpon()
                .encodedAuthority("com.android.htmlfileprovider")
                .scheme("content")
                .encodedPath(filePath)
                .build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "text/html");
        return intent;
    }

    private static Intent generateCommonIntent(Context context, File file, String dataType) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = getUri(context, intent, file);
        intent.setDataAndType(uri, dataType);
        return intent;
    }

    private static Uri getUri(Context context, Intent intent, File file) {
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider",
                    file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    public static boolean isAvailable(Context ctx, Intent intent) {
        final PackageManager mgr = ctx.getPackageManager();
        List<ResolveInfo> list = mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static int getColorFromString(String colorString, int defaultColor) {
        int color;
        try {
            color = Color.parseColor("#" + colorString);
        } catch (Exception e) {
            Log.i(TAG, "Cannot convert string to color: " + colorString);
            color = defaultColor;
        }
        return color;
    }

    public static float getAlphaFromString(String alphaString, float defaultAlpha) {
        float alpha;
        try {
            alpha = Float.parseFloat(alphaString) / 100;
        } catch (Exception e) {
            Log.i(TAG, "Cannot convert string to alpha: " + alphaString);
            alpha = defaultAlpha / 100;
        }
        return alpha;
    }

    public static int getGravityFromString(String gravityString, int defaultGravity) {
        int gravity;
        if ("Left".equalsIgnoreCase(gravityString)) {
            gravity = Gravity.LEFT;
        } else if ("Right".equalsIgnoreCase(gravityString)) {
            gravity = Gravity.RIGHT;
        } else if ("Center".equalsIgnoreCase(gravityString)) {
            gravity = Gravity.CENTER;
        } else {
            gravity = defaultGravity;
        }
        return gravity;
    }

    public static StateListDrawable getStateListDrawable(Drawable defaultDrawable, Drawable highlightedDrawable) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{-android.R.attr.state_enabled}, defaultDrawable);
        states.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled}, highlightedDrawable);
        states.addState(new int[]{android.R.attr.state_pressed}, highlightedDrawable);
        return states;
    }

    public static ColorStateList getColorStateList(int defaultColor, int highlightedColor) {
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled, -android.R.attr.state_pressed}, //1
                        new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled}, //2
                        new int[]{android.R.attr.state_pressed}, //3
                        new int[]{android.R.attr.state_enabled} //4
                },
                new int[]{
                        defaultColor, //1
                        highlightedColor, //2
                        highlightedColor, //3
                        defaultColor //4
                }
        );

        return colorStateList;
    }

    public static String getDisplayableString(String value, String defaultValue) {
        if (value == null || "null".equals(value)) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public static Drawable getDrawableFromFileId(Context context, String fileId) {
        if (fileId == null) {
            return null;
        }
        String filePath = DataManagerFactory.getDataManager().getFilePath(context, "Attachment", fileId);
        if (filePath == null) {
            return null;
        }
        Drawable drawable = Drawable.createFromPath(filePath);
        return drawable;
    }

    public static String getImageFilePathFromFileId(Context context, String fileId) {
        if (fileId == null) {
            return null;
        }
        String filePath = DataManagerFactory.getDataManager().getFilePath(context, "Attachment", fileId);
        return filePath;
    }

    /**
     * Unzip a zip file.  Will overwrite existing files.
     *
     * @param zipFileName Full path of the zip file you'd like to unzip.
     */
    public static String unzip(String zipFileName) {
        int size;
        byte[] buffer = new byte[BUFFER_SIZE];


        String unzipLocation = zipFileName + "-zip/";
        try {
            if (!unzipLocation.endsWith("/")) {
                unzipLocation += "/";
            }
            File unzipFileDirectory = new File(unzipLocation);

            if (unzipFileDirectory.exists()) {
                File zipFile = new File(zipFileName);
                if (unzipFileDirectory.lastModified() > zipFile.lastModified()) {
                    Log.i(TAG, "Already unzipped latest version. No need to unzip.");
                    return unzipLocation;
                } else {
                    deleteDirectory(unzipFileDirectory);
                }
            }

            if (!unzipFileDirectory.isDirectory()) {
                unzipFileDirectory.mkdirs();
                unzipFileDirectory.setReadable(true, false);
            }
            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFileName),
                    BUFFER_SIZE));
            try {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = unzipLocation + ze.getName();
                    File unzipFile = new File(path);

                    if (ze.isDirectory()) {
                        if (!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {
                        // check for and create parent directories if they don't
                        // exist
                        File parentDir = unzipFile.getParentFile();
                        if (null != parentDir) {
                            if (!parentDir.isDirectory()) {
                                parentDir.mkdirs();
                                parentDir.setReadable(true, false);
                            }
                        }

                        // unzip the file
                        FileOutputStream out = new FileOutputStream(unzipFile, false);
                        BufferedOutputStream fout = new BufferedOutputStream(out, BUFFER_SIZE);
                        try {
                            while ((size = zin.read(buffer, 0, BUFFER_SIZE)) != -1) {
                                fout.write(buffer, 0, size);
                            }

                            zin.closeEntry();
                        } finally {
                            fout.flush();
                            fout.close();
                        }
                    }
                    unzipFile.setReadable(true, false);
                }
            } finally {
                zin.close();
            }
        } catch (Exception e) {
            unzipLocation = null;
            Log.e(TAG, "Unzip exception", e);
        }

        return unzipLocation;
    }


    private static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            for (File child : dir.listFiles()) {
                deleteDirectory(child);
            }
        }
        dir.delete();
    }

    @SuppressLint("DefaultLocale")
    public static String getExtension(String fileType) {
        if (fileType == null) return "";

        fileType = fileType.toLowerCase();
        if (FILE_TYPE_TO_EXTN.containsKey(fileType)) {
            return FILE_TYPE_TO_EXTN.get(fileType);
        }

        return fileType;
    }

    private static final Map<String, String> FILE_TYPE_TO_EXTN = new HashMap<String, String>() {
        private static final long serialVersionUID = -8662551162210966241L;

        {
            put("power_point", "ppt");
            put("power_point_x", "pptx");
            put("word", "doc");
            put("wordx", "docx");
            put("link", "html");
        }
    };

    public static void openContentVersion(Context context, ContentVersion contentVersion) {
//        if (contentVersion.getFilePath(context) != null) {
//            Intent intent = ContentUtils.getContentIntent(context, contentVersion);
//            if (ContentUtils.isAvailable(context, intent)) {
//                context.startActivity(intent);
//            } else {
//                Toast.makeText(context,
//                        "Application to handle this file type is not available on the device!!",
//                        Toast.LENGTH_LONG).show();
//            }
//        } else {
//            Toast.makeText(context, "Selected content: " + contentVersion.getName() + "is still being downloaded.",
//                    Toast.LENGTH_LONG).show();
//        }
    }

    public static Drawable getCategoryBackgroundDrawableForOrientation(CategoryMobileConfig__c config, int orientation, Context context) {

        String backgroundImageId;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            backgroundImageId = config.getLandscapeAttachmentId__c();
        } else {
            backgroundImageId = config.getPortraitAttachmentId__c();
        }

        try {
            Drawable backgroundDrawable = ContentUtils.getDrawableFromFileId(context, backgroundImageId);
            if (backgroundDrawable != null)
                return backgroundDrawable;

            Category__c category = Category__c.getCategoryForId(config.getCategoryId__c());

            if (category != null) {
                Category__c parent = Category__c.getCategoryForId(category.getParent_Category__c());

                if (parent != null) {
                    String filter = String.format("{CategoryMobileConfig__c:%s} = '%s' AND {CategoryMobileConfig__c:%s} = '%s'",
                            DSAConstants.CategoryMobileConfigFields.MOBILE_APP_CONFIGURATION_ID,
                            config.getMobileAppConfigurationId__c(), DSAConstants.CategoryMobileConfigFields.CATEGORY_ID, parent.getId());
                    String subCatSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.DSAObjects.CATEGORY_MOBILE_CONFIG, filter);
                    JSONArray records = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(subCatSql);
                    try {
                        if (records.length() > 0) {
                            JSONObject jsonCat = records.getJSONArray(0).getJSONObject(0);
                            CategoryMobileConfig__c catMobileConfig = new CategoryMobileConfig__c(jsonCat);
                            return getCategoryBackgroundDrawableForOrientation(catMobileConfig, orientation, context);
                        }
                    } catch (Exception e) {
                        return null;
                    }
                } else {
                    MobileAppConfig__c mobileAppConfig = MobileAppConfig__c.getMobileAppConfigForCatMobileConfig(config);
                    if (mobileAppConfig != null) {
                        String attachmentId;
                        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            attachmentId = mobileAppConfig.getLandscapeAttachmentId__c();
                        } else {
                            attachmentId = mobileAppConfig.getPortraitAttachmentId__c();
                        }

                        Drawable drawable = ContentUtils.getDrawableFromFileId(context, attachmentId);
                        return drawable;
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long getExistFileSize(String filePath) {
        FileChannel fc = null;
        try {
            File f = new File(filePath);
            if (f.exists() && f.isFile()) {
                FileInputStream fis = new FileInputStream(f);
                fc = fis.getChannel();
                return fc.size();
            } else {
                return 0;
            }
        } catch (FileNotFoundException e) {
            return 0;
        } catch (IOException e) {
            Log.e(TAG, "got Exist File Size with an IO exception");
        } finally {
            if (null != fc) {
                try {
                    fc.close();
                } catch (IOException e) {
                    Log.e(TAG, "got Exist File Size with an IO exception");
                }
            }
        }
        return 0;
    }
}
