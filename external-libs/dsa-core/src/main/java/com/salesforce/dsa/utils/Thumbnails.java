package com.salesforce.dsa.utils;

import android.content.Context;
import android.net.Uri;
import com.salesforce.androidsyncengine.utils.Guava.GuavaUtils;
import com.salesforce.dsa.data.model.ContentVersion;

import java.io.File;

/**
 * Copyright 2015 AKTA a SalesForce Company
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class Thumbnails {

    enum Type {
        None,
        Image
    }

    private static Type getPreviewType(String fileType) {
        if (GuavaUtils.isNullOrEmpty(fileType)) {
            return Type.None;
        }

        switch (fileType.toUpperCase()) {
            case "IMAGE":
            case "PNG":
            case "JPG":
            case "JPEG":
            case "BMP":
                return Type.Image;
            default:
                return Type.None;
        }
    }

    /**
     * Attempts to return a URI to an image thumbnail for the item
     * <p>
     * Note this does not to any scaling what so ever - Images returned from here may be
     * very large and crash the app if not dealt with properly
     *
     * @param context
     * @param contentVersion
     * @return a uri to a preview image if available otherwise null
     */
    public static Uri getThumbnail(Context context, ContentVersion contentVersion) {
        Uri uri = null;
        if (getPreviewType(contentVersion.getFileType()) == Type.Image) {

            String imagePath = contentVersion.getFilePath(context);
            File imageFile = new File(context.getFilesDir(), imagePath);
            if (imageFile.exists()) {
                uri = Uri.fromFile(imageFile);
            }
        }
        return uri;
    }

    private Thumbnails() {
        super();
    }
}
