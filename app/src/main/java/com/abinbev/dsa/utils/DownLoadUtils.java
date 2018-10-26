package com.abinbev.dsa.utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This class is limited to one class that downloads an Apk file, and it is also possible to use the current file, which is just a working download
 */
public class DownLoadUtils {


    //download fail
    public static final int DOWN_LOAD_FAIL = 0;
    //download progress
    public static final int DOWN_LOAD_PROGRESS = 1;
    //download success
    public static final int DOWN_LOAD_SUCCESS = 2;
    //listener
    private DownLoadListener listener;
    private String TAG = this.getClass().getSimpleName();
    //download File name
    public static final String ABI_OnTap_App = "ABI_OnTap_App";

    //File limit count
    private int FILE_MAX_COUNT = 10;

    private LinkedList<Call> sparseArray = new LinkedList<Call>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {

                case DOWN_LOAD_SUCCESS:
                    if (listener != null) {
                        listener.onDownLoadSuccess((String) msg.obj);
                    }
                    break;
                case DOWN_LOAD_PROGRESS:
                    if (listener != null) {
                        Map<String, Float> floatMap = (Map<String, Float>) msg.obj;
                        listener.onDownLoadProgress(floatMap.get("total"), floatMap.get("progress"), floatMap.get("percent"));
                    }
                    break;
                case DOWN_LOAD_FAIL:
                    if (listener != null) {
                        listener.onDownLoadFail((String) msg.obj);
                    }
                    break;
            }
        }
    };

    private DownLoadUtils() {
        this.okHttpClient = new OkHttpClient();
    }

    //Okhttp
    private OkHttpClient okHttpClient;

    private static DownLoadUtils downLoadUtils;

    public static DownLoadUtils getInstance() {

        if (downLoadUtils == null) {
            synchronized (DownLoadUtils.class) {
                if (downLoadUtils == null) {
                    downLoadUtils = new DownLoadUtils();
                }
            }
        }


        return downLoadUtils;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    /**
     * downLoad file method
     *
     * @param activity
     * @param url      download url
     * @param saveDir  saveDir
     * @param listener listener
     */
    public void downLoadFile(Activity activity, String url, String saveDir, DownLoadListener listener) {

        try {

            clearAllRequest();

            if (TextUtils.isEmpty(url)) {
                throw new IllegalArgumentException("Download Link params Exception");
            }
            if (TextUtils.isEmpty(saveDir)) {
                throw new IllegalArgumentException("Download saveDir params Exception");
            }
            if (listener == null) {
                throw new IllegalArgumentException("Download listener params is null");
            }


            String fileName = getFileNameByUrl(url);
            File file = new File(saveDir, fileName);
            Log.e(TAG, "file:" + file.getAbsolutePath() + " - fileName:" + fileName);
            clearUpFile(saveDir);
            //create file
            if (file.exists()) {
                /**
                 * Decide if you can open it before it exists
                 * Is it lossless APK
                 */

                file.delete();
                File filess = new File(saveDir, fileName);
                file = filess;
            }
            file.createNewFile();
            this.listener = listener;

            Request request = new Request.Builder().get().tag(activity).url(url).build();
            File finalFile = file;
            Call call = okHttpClient.newCall(request);
            sparseArray.add(call);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Message message = Message.obtain();
                    message.what = DOWN_LOAD_FAIL;
                    message.obj = "" + e.getMessage();
                    handler.sendMessage(message);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    InputStream inputStream = null;
                    byte[] buffer = new byte[2048];
                    int len = 0;
                    FileOutputStream outputStream = null;

                    try {

                        inputStream = response.body().byteStream();
                        outputStream = new FileOutputStream(finalFile);
                        float total = response.body().contentLength();

                        float sum = 0;

                        while ((len = inputStream.read(buffer)) != -1) {

                            Map<String, Float> floatMap = new LinkedHashMap<>();
                            outputStream.write(buffer, 0, len);
                            sum += len;
                            float progress = (sum * 1.0f / total * 100);
                            floatMap.put("total", total);
                            floatMap.put("progress", sum);
                            floatMap.put("percent", progress);
                            Message message = Message.obtain();
                            message.what = DOWN_LOAD_PROGRESS;
                            message.obj = floatMap;
                            handler.sendMessage(message);
                        }

                        outputStream.flush();

                        //Download complete
                        Message message = Message.obtain();
                        message.what = DOWN_LOAD_SUCCESS;
                        message.obj = finalFile.getAbsolutePath();
                        handler.sendMessage(message);
                    } catch (Exception e) {
                        Log.e(TAG, "" + e.getMessage());
                        Message message = Message.obtain();
                        message.what = DOWN_LOAD_FAIL;
                        message.obj = "" + e.getMessage();
                        handler.sendMessage(message);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
            Message message = Message.obtain();
            message.what = DOWN_LOAD_FAIL;
            message.obj = "" + e.getMessage();
            handler.sendMessage(message);
        }
    }

    public DownLoadListener getListener() {
        return listener;
    }

    /**
     *Clear all requests
     */
    public void clearAllRequest() {

        try {

            if (sparseArray == null)
                new LinkedHashMap<>();
            else {

                Iterator<Call> iterator = sparseArray.iterator();
                while (iterator.hasNext()) {

                    Call call = iterator.next();
                    if (call != null && !call.isCanceled()) {
                        call.cancel();
                    }
                    iterator.remove();
                }
                this.listener = null;
            }

        }catch (Exception e) {
            Log.e(TAG, ""+e.getMessage());
        }
    }

    /**
     * Expand the name
     *
     * @param url
     * @return
     */
    private String getExpandTheNameByUrl(String url) {
        return url.substring(url.lastIndexOf("."));
    }

    private String getFileNameByUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public interface DownLoadListener {

        /**
         * download success
         *
         * @param downFilePath download File path
         */
        public void onDownLoadSuccess(String downFilePath);

        /**
         * downLoad progress
         *
         * @param total    file lenght
         * @param progress current downLoad progress
         */
        public void onDownLoadProgress(float total, float progress, float percent);

        /**
         * download fail
         */
        public void onDownLoadFail(String error);
    }

    /**
     * clear up the file , Keep the last five files by date
     *
     * @param saveDir
     */
    private void clearUpFile(String saveDir) {

        try {

            File file = new File(saveDir);
            if (file.isDirectory()) {

                List<File> files = Arrays.asList(file.listFiles());

                if (files != null && files.size() > 0) {

                    Collections.sort(files, new CompratorByLastModified());

                    Iterator<File> iterator = files.iterator();
                    int count = 0;
                    while (iterator.hasNext()) {
                        if (count <= FILE_MAX_COUNT) {
                            File f = iterator.next();
                            if (f.isFile()) {
                                f.delete();
                                count++;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "clearUpFile:" + e.getMessage());
        }
    }

    static class CompratorByLastModified implements Comparator<File> {

        @Override
        public int compare(File f1, File f2) {
            long diff = f1.lastModified() - f2.lastModified();
            if (diff > 0)
                return -1;
            else if (diff == 0)
                return 0;
            else
                return 1;
        }
    }
}
