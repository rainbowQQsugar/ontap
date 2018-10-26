package com.abinbev.dsa.model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.service.AttachmentUploadService;
import com.abinbev.dsa.utils.ContentUtils;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.SmartStoreDataManagerImpl;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * A Map containing un-synced (from server) {@link Attachment} and possibly their ids if
 * attachment has been posted.
 *
 * Stored as a key:value pair of filePath:attachmentId .
 *
 */
public class PendingSyncAttachments extends HashMap<String, String> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SerializedFileName = "pendingAttachments.ser";
    private static final String TAG = PendingSyncAttachments.class.getSimpleName();


    private static PendingSyncAttachments getPendingAttachments() {
        PendingSyncAttachments pendingSyncAttachments;
        try {
            ObjectInputStream in = new ObjectInputStream(ABInBevApp.getAppContext().openFileInput(SerializedFileName));
            pendingSyncAttachments = (PendingSyncAttachments) in.readObject();
        } catch (Exception e) {
            pendingSyncAttachments = new PendingSyncAttachments();
        }
        return pendingSyncAttachments;
    }

    public static void cleanAttachments() {
        Log.d(TAG, "cleanAttachments: REMOVING OLD ATTACHMENTS");
        for (Entry<String, String> entry : getPendingAttachments().entrySet()) {
            String key = entry.getKey();
            String attachmentId = entry.getValue();

            int slash = key.lastIndexOf("/");
            String dir = key.substring(0, slash + 1);
            String filename = key.substring(slash, key.length());

            Log.d(TAG, "cleanAttachments: " + key);
            File file = new File(dir, filename);
            Log.d(TAG, "file path: " + file.getPath());

            if (!file.exists()) {
                Log.d(TAG, "file does not exist");
                // Remove key if file does not exist.
                removeAttachment(key);
            }
            else if (!TextUtils.isEmpty(attachmentId)) {
                // Remove file if already has an attachmentId.
                Log.d(TAG, "file is already uploaded");
//                boolean deleted = file.delete();
//                Log.d(TAG, "deletedAttachment: " + deleted);
                removeAttachment(key);
            }
        }
    }

    public synchronized static void flushAttachments(Context context) {
        Log.d(TAG, "flushing attachments");
        updateTempIds();

        List<String> keys = new ArrayList<>();
        for (Entry<String, String> entry : getPendingAttachments().entrySet()) {
            if (ContentUtils.isNull_OR_Blank(entry.getValue())) {
                keys.add(entry.getKey());
            }
        }

        for (String key : keys) {
            int lastSlash = key.lastIndexOf("/");
            String parentId = extractParentId(key);

            SmartStoreDataManagerImpl dataManager = (SmartStoreDataManagerImpl) DataManagerFactory.getDataManager();
            if (dataManager.isClientId(parentId)) {
                //no need to attempt upload
                continue;
            }

            String dir = key.substring(0, lastSlash + 1);
            String filename = key.substring(lastSlash, key.length());

            Log.d(TAG, "flushing attachment: " + key);
            File file = new File(dir, filename);
            Log.d(TAG, "flush file path: " + file.getPath());
            Log.d(TAG, "flush file absolute path: " + file.getAbsolutePath());
            if (file.exists()) {
                Log.d(TAG, "sending for upload: ");
                Intent flushIntent = null;
                if (dir.contains("temp/account")) {
                    flushIntent = AttachmentUploadService.flushAccount(context, Uri.fromFile(file), parentId);
                } else if (dir.contains("temp/asset")) {
                    flushIntent = AttachmentUploadService.flushAsset(context, Uri.fromFile(file), parentId);
                } else if (dir.contains("temp/case")) {
                    flushIntent = AttachmentUploadService.flushCase(context, Uri.fromFile(file), parentId);
                } else if (dir.contains("temp/survey_qr")) {
                    flushIntent = AttachmentUploadService.flushSurvey(context, Uri.fromFile(file), parentId);
                } else if (dir.contains("temp/event")) {
                    flushIntent = AttachmentUploadService.flushEvent(context, Uri.fromFile(file), parentId);
                }

                if (flushIntent != null) {
                    context.startService(flushIntent);
                    Log.d(TAG, "flush sent: ");
                }

            }
        }
    }

    /**
     * Extracts the parent id from the path
     *
     * @return - the parent id
     */
    private static String extractParentId(String path) {
        int lastSlash = path.lastIndexOf("/");
        int secondToLastSlash = path.substring(0, lastSlash).lastIndexOf("/");
        return path.substring(secondToLastSlash + 1, lastSlash);
    }

    /**
     * Work through the pending map to checking for tempIds in file path (key).
     *
     * If one is found, it needs to be updated with it's true id so that uploads
     * can succeed. We will also rename the underlying file location so that it
     * is properly linked to the maps updated key.
     */
    private synchronized static void updateTempIds() {
        //update hash key to use new ids
        List<String> keysToBeUpdated = new ArrayList<>();
        Set<String> keysToBeChecked = getPendingAttachments().keySet();

        SmartStoreDataManagerImpl dataManager = (SmartStoreDataManagerImpl) DataManagerFactory.getDataManager();

        //gather keys that need to be updated with proper ids.
        for (String keyToBeChecked : keysToBeChecked) {

            String parentId = extractParentId(keyToBeChecked);

            //ensure that this is a temp id and that there is an updated id to be used
            if (dataManager.isClientId(parentId) && !ContentUtils.isNull_OR_Blank(dataManager.getSalesforceIdFromTemporaryId(parentId))) {
                keysToBeUpdated.add(keyToBeChecked);
            }
        }

        for (String keyToBeUpdated : keysToBeUpdated) {
            String tempId = extractParentId(keyToBeUpdated);
            String newId = dataManager.getSalesforceIdFromTemporaryId(tempId);

            //rename directory
            int lastSlash = keyToBeUpdated.lastIndexOf("/");
            String dir = keyToBeUpdated.substring(0, lastSlash + 1);

            File oldDirectory = new File(dir);

            if (oldDirectory.exists()) { //old directory is still around, need to rename it with new Id
                File newDirectory = new File(dir.replace(tempId, newId));
                Log.d(TAG, "renaming directory from: " + oldDirectory + " to: " + newDirectory);
                oldDirectory.renameTo(newDirectory);
            }

            //update hashmap with new path
            removeAttachment(keyToBeUpdated);
            updateAttachment(keyToBeUpdated.replace(tempId, newId), null);
        }
    }

    private static void savePendingSyncAttachments(PendingSyncAttachments pendingSyncAttachments) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    ABInBevApp.getAppContext().openFileOutput(SerializedFileName, Context.MODE_PRIVATE));
            out.writeObject(pendingSyncAttachments);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAttachemntId(String path) {
        return getPendingAttachments().get(path);
    }

    public static void updateAttachment(String path, String attachmentId) {
        PendingSyncAttachments pendingSyncAttachments = getPendingAttachments();
        pendingSyncAttachments.put(path, attachmentId);
        savePendingSyncAttachments(pendingSyncAttachments);
    }

    public static void removeAttachment(String path) {
        PendingSyncAttachments pendingSyncAttachments = getPendingAttachments();
        pendingSyncAttachments.remove(path);
        savePendingSyncAttachments(pendingSyncAttachments);
    }

}
