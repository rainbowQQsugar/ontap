package com.abinbev.dsa.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.WebViewActivity;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.ContentVersion;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
            return "application/vnd.ms-excel_icon";
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

    public static Intent getContentIntent(Context context, ContentVersion contentVersion) {

        String fileExtension = contentVersion.getFileType();
        Log.e(TAG, "fileExtension is: " + fileExtension);

        if ("ZIP".equalsIgnoreCase(fileExtension)) {
            String completeFileName = contentVersion.getCompleteFileName(context);
            // final Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
            final Intent webViewIntent = new Intent(context, WebViewActivity.class);
            webViewIntent.putExtra("Title", contentVersion.getTitle());
            webViewIntent.putExtra("Description", contentVersion.getDescription());
            webViewIntent.putExtra("CompleteFileName", completeFileName);
            webViewIntent.putExtra("FileExtension", "ZIP");
            return webViewIntent;
        } else if ("LINK".equalsIgnoreCase(fileExtension)) {
            // String linkUrl = contentVersion.getContentUrl();
            // final Intent linkIntent = new Intent(Intent.ACTION_VIEW,
            // Uri.parse(linkUrl));
            // return linkIntent;
            //
            // // final Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));

            final Intent webViewIntent = new Intent(context, WebViewActivity.class);
            webViewIntent.putExtra("Url", contentVersion.getContentUrl());
            webViewIntent.putExtra("FileExtension", "LINK");
            return webViewIntent;

        } else {

            final Intent contentIntent = new Intent(Intent.ACTION_VIEW);
            String provider = context.getString(R.string.provider);
            String mimetype = getMimeType(fileExtension);
            contentIntent.setDataAndType(
                    Uri.parse("content://" + provider + "/" + contentVersion.getFilePath(context)), mimetype);

            return contentIntent;
        }
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
        if (contentVersion.getFilePath(context) != null) {
            Intent intent = ContentUtils.getContentIntent(context, contentVersion);
            if (ContentUtils.isAvailable(context, intent)) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context,
                        "Application to handle this file type is not available on the device!!",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "Selected content: " + contentVersion.getName() + "is still being downloaded.",
                    Toast.LENGTH_LONG).show();
        }
    }

    public static boolean isNull_OR_Blank(String... values) {
        for (String value : values) {
            if (!ContentUtils.isStringValid(value)){
                return true;
            }
        }
        return false;
    }

    public static Boolean isPicklistStringValid(String stringValue, Context context){
        return (ContentUtils.isStringValid(stringValue) && !context.getResources().getString(R.string.picklist_none).equalsIgnoreCase(stringValue));
    }

    public static Boolean isStringValid(String stringValue){
        String emptyString = "";
        String nullString = "null";
        return !nullString.equals(stringValue) && stringValue != null && !emptyString.equals(stringValue.trim()) ;
    }

    public static long copyContent(Context context, Uri fileUri, File destination) throws IOException {
        if (fileUri == null || destination == null) return -1;

        InputStream inputStream = null;
        OutputStream outputStream = null;
        ParcelFileDescriptor fileDescriptor = null;

        try {
            ContentResolver resolver = context.getContentResolver();
            fileDescriptor = resolver.openFileDescriptor(fileUri, "r");
            if (fileDescriptor == null) {
                throw new FileNotFoundException("No file descriptor for Uri " + fileUri);
            }

            //convert file into array of bytes
            inputStream = new BufferedInputStream(
                    new FileInputStream(fileDescriptor.getFileDescriptor()));
            outputStream = new BufferedOutputStream(new FileOutputStream(destination));

            long bytes = StreamUtils.copy(inputStream, outputStream);
            outputStream.flush();
            return bytes;

        } finally {
            CloseableUtils.closeSilently(outputStream);
            CloseableUtils.closeSilently(inputStream);
            CloseableUtils.closeSilently(fileDescriptor);
        }
    }

    public static boolean deleteContent(Context context, Uri fileUri) {
        if (fileUri == null) return true;
        File file = new File(fileUri.getPath());
        return file.exists() ? file.delete() : true; // TODO handle items from ContentResolver
    }
}
