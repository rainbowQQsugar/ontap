package com.salesforce.dsa.app.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.model.ErrorObject;
import com.salesforce.androidsyncengine.datamanager.model.QueueObject;
import com.salesforce.androidsyncengine.datamanager.model.QueueOperation;
import com.salesforce.dsa.app.R;

import org.json.JSONException;

import java.util.List;

/**
 * @author Jason Harris (jason@akta.com)
 */
public class ErrorHandler {

    private static final String TAG = "ErrorHandler";

    public static void showError(Context context, List<ErrorObject> errorList) {
        for (ErrorObject errorObject : errorList) {

            if (errorObject.getQueueSoupEntryId() != null) {
                DataManager dataManager = DataManagerFactory.getDataManager();
                QueueObject queueObject = dataManager.getQueueRecordFromClient(errorObject.getQueueSoupEntryId());
                try {
                    if (queueObject != null) {
                        QueueOperation queueOperation = queueObject.getOperation();

                        String errorMessage;
                        switch (queueOperation) {
                            case CREATE:
                                errorMessage = context.getString(R.string.error_create);
                                break;
                            case DELETE:
                                errorMessage = context.getString(R.string.error_delete);
                                break;
                            case UPDATE:
                                errorMessage = context.getString(R.string.error_update);
                                break;
                            default:
                                errorMessage = context.getString(R.string.error_unknown);
                                break;
                        }

                        Toast.makeText(context, errorMessage + queueObject.getObjectType() + " on server."
                                        + "\nError: " + errorObject.getErrorMessage()
                                        + "\nRetry count: " + queueObject.getRetryCount(),
                                Toast.LENGTH_LONG).show();

                        // Try twice and then delete
                        if (queueObject.getRetryCount() > 1) {
                            Log.i(TAG, "Deleting the following object in Queue: "
                                    + queueObject.toJson());
                            dataManager.deleteQueueRecordFromClient(
                                    errorObject.getQueueSoupEntryId(), true);
                        }
                    }
                } catch (JSONException e) {
                    Toast.makeText(context, R.string.error_unknown, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, R.string.error_unknown, Toast.LENGTH_LONG).show();
            }


        }
    }
}