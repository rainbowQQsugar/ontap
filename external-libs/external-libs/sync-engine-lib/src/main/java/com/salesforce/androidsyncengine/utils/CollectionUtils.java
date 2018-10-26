package com.salesforce.androidsyncengine.utils;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Jakub Stefanowski on 22.09.2017.
 */

public final class CollectionUtils {
    private CollectionUtils() {}

    public static <T> void fill(Collection<T> collection, Iterator<T> iterator) {
        if (collection == null || iterator == null) return;

        while (iterator.hasNext()) {
            collection.add(iterator.next());
        }
    }
}
