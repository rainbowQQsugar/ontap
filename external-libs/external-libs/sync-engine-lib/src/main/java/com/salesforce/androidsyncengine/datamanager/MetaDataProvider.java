package com.salesforce.androidsyncengine.datamanager;

import android.content.Context;

import com.google.gson.Gson;
import com.salesforce.androidsyncengine.data.layouts.IndividualCompactLayout;
import com.salesforce.androidsyncengine.data.layouts.IndividualLayouts;
import com.salesforce.androidsyncengine.data.layouts.ObjectCompactLayouts;
import com.salesforce.androidsyncengine.data.layouts.ObjectLayouts;
import com.salesforce.androidsyncengine.data.model.DescribeSObjectResult;
import com.salesforce.androidsyncengine.data.model.PicklistDependencyHolder;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;

/**
 * Created by bduggirala on 11/16/15.
 */
public class MetaDataProvider {

    private static final String META_EXTENSION = ".meta";
    private static final String PICKLIST_EXTENSION = ".picklist";
    private static final String LAYOUT_EXTENSION = ".layout";

    private MetaDataProvider() {}

    public static PicklistDependencyHolder getPicklistDependency(Context context, String objectType) {

        PicklistDependencyHolder picklistDependencyHolder;
        try {
            ObjectInputStream in = new ObjectInputStream(context.openFileInput(objectType + PICKLIST_EXTENSION));
            picklistDependencyHolder = (PicklistDependencyHolder) in.readObject();
        } catch (FileNotFoundException e) {
            picklistDependencyHolder = new PicklistDependencyHolder();
        } catch (Exception e) {
            picklistDependencyHolder = new PicklistDependencyHolder();
        }
        return picklistDependencyHolder;
    }

    public static void savePicklistDependency(Context context, String objectType, PicklistDependencyHolder picklistDependencyHolder) {
        saveMetaDataObject(context, objectType, PICKLIST_EXTENSION, picklistDependencyHolder);
    }

    public static DescribeSObjectResult getMetaData(Context context, Gson gson, String objectType) {
        return getMetaDataObjectFromString(context, gson, objectType, META_EXTENSION, DescribeSObjectResult.class);
    }

    public static void saveMetaData(Context context, String objectType, String describeSObjectResult) {
        saveMetaDataObject(context, objectType, META_EXTENSION, describeSObjectResult);
    }

    public static ObjectLayouts getMetaDataForLayouts(Context context, Gson gson, String objectType) {
        objectType = ManifestUtils.getNamespaceSupportedObjectName(objectType, context);
        return getMetaDataObjectFromString(context, gson, objectType, LAYOUT_EXTENSION, ObjectLayouts.class);
    }

    public static void saveMetadataForLayouts(Context context, String objectType, String objectLayoutsResult) {
        saveMetaDataObject(context, objectType, LAYOUT_EXTENSION, objectLayoutsResult);
    }

    public static IndividualLayouts getMetaDataForIndividualLayout(Context context, Gson gson, String objectType, String recordId) {
        objectType = ManifestUtils.getNamespaceSupportedObjectName(objectType, context);
        return getMetaDataObjectFromString(context, gson, objectType + "_" + recordId, LAYOUT_EXTENSION,
                IndividualLayouts.class);
    }

    public static void saveMetadataForIndividualLayout(Context context, String objectType, String recordId, String individualLayoutsResult) {
        saveMetaDataObject(context, objectType + "_" + recordId, LAYOUT_EXTENSION, individualLayoutsResult);
    }

    public static void saveMetadataForCompactLayouts(Context context, String objectType, String objectLayoutsResult) {
        saveMetaDataObject(context, objectType + "_compact", LAYOUT_EXTENSION, objectLayoutsResult);
    }

    public static void saveMetadataForIndividualCompactLayout(Context context, String objectType, String recordId, String individualLayoutsResult) {
        saveMetaDataObject(context, objectType + "_compact_" + recordId, LAYOUT_EXTENSION, individualLayoutsResult);
    }

    public static ObjectCompactLayouts getMetaDataForCompactLayouts(Context context, Gson gson, String objectType) {
        objectType = ManifestUtils.getNamespaceSupportedObjectName(objectType, context);
        return getMetaDataObjectFromString(context, gson, objectType + "_compact", LAYOUT_EXTENSION, ObjectCompactLayouts.class);
    }

    public static IndividualCompactLayout getMetaDataForIndividualCompactLayout(Context context, Gson gson, String recordId, String objectType) {
        objectType = ManifestUtils.getNamespaceSupportedObjectName(objectType, context);
        return getMetaDataObjectFromString(context, gson, objectType + "_compact_" + recordId, LAYOUT_EXTENSION, IndividualCompactLayout.class);
    }

    public static <T> T getMetaDataObjectFromString(Context context, Gson gson, String objectType,
                                                    String extension, Class<T> objectClass) {
        T object;
        try {
            InputStream myInputStream = context.openFileInput(objectType + extension);
            Reader reader = new InputStreamReader(myInputStream);
            object = gson.fromJson(reader, objectClass);
        } catch (Exception e) {
            object = null;
        }
        return object;
    }

    private static <T> void saveMetaDataObject(Context context, String objectType, String extension, T object) {
        try {
            // write
            ObjectOutputStream out = new ObjectOutputStream(
                    context.openFileOutput(objectType + extension, Context.MODE_PRIVATE));
            out.writeObject(object);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveMetaDataObject(Context context, String objectType, String extension, String string) {
        saveMetaDataString(context, objectType, extension, string);
    }

    private static void saveMetaDataString(Context context, String objectType, String extension, String metaDataString) {
        try {
            // write
            FileOutputStream out = context.openFileOutput(objectType + extension,
                    Context.MODE_PRIVATE);
            out.write(metaDataString.getBytes());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
