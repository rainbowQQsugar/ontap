package com.abinbev.dsa.utils;

import com.android.internal.util.Predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lukaszwalukiewicz on 12.01.2016.
 */
public class CollectionUtils {
    public static <T> List<T> filter(Collection<T> col, Predicate<T> predicate) {
        List<T> result = new ArrayList<T>();
        for (T element: col) {
            if (predicate.apply(element)) {
                result.add(element);
            }
        }
        return result;
    }
}
