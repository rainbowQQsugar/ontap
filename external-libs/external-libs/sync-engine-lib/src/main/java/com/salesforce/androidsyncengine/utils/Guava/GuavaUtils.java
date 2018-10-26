package com.salesforce.androidsyncengine.utils.Guava;

import java.io.Serializable;
import java.util.*;

/**
 * Utility class to give "Guava-like" functionality without the
 * large dependency on Guava.
 * <p/>
 * implementation from: https://github.com/google/guava
 */
public class GuavaUtils {

    public interface Function<F, T> {
        T apply(F input);

        @Override
        boolean equals(Object object);
    }

    public interface Predicate<T> {
        boolean apply(T input);

        @Override
        boolean equals(Object object);
    }

    public static <F, T> List<T> transform(
            List<F> fromList, Function<? super F, ? extends T> function) {
        return (fromList instanceof RandomAccess)
                ? new TransformingRandomAccessList<F, T>(fromList, function)
                : new TransformingSequentialList<F, T>(fromList, function);
    }

    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    public static <T> T checkNotNull(T reference, Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Implementation of a sequential transforming list.
     */
    private static class TransformingSequentialList<F, T> extends AbstractSequentialList<T>
            implements Serializable {
        final List<F> fromList;
        final Function<? super F, ? extends T> function;

        TransformingSequentialList(List<F> fromList, Function<? super F, ? extends T> function) {
            this.fromList = checkNotNull(fromList);
            this.function = checkNotNull(function);
        }

        /**
         * The default implementation inherited is based on iteration and removal of
         * each element which can be overkill. That's why we forward this call
         * directly to the backing list.
         */
        @Override
        public void clear() {
            fromList.clear();
        }

        @Override
        public int size() {
            return fromList.size();
        }

        @Override
        public ListIterator<T> listIterator(final int index) {
            return new TransformedListIterator<F, T>(fromList.listIterator(index)) {
                @Override
                T transform(F from) {
                    return function.apply(from);
                }
            };
        }

        private static final long serialVersionUID = 0;
    }

    /**
     * Implementation of a transforming random access list. We try to make as many
     * of these methods pass-through to the source list as possible so that the
     * performance characteristics of the source list and transformed list are
     * similar.
     */
    private static class TransformingRandomAccessList<F, T> extends AbstractList<T>
            implements RandomAccess, Serializable {
        final List<F> fromList;
        final Function<? super F, ? extends T> function;

        TransformingRandomAccessList(List<F> fromList, Function<? super F, ? extends T> function) {
            this.fromList = checkNotNull(fromList);
            this.function = checkNotNull(function);
        }

        @Override
        public void clear() {
            fromList.clear();
        }

        @Override
        public T get(int index) {
            return function.apply(fromList.get(index));
        }

        @Override
        public Iterator<T> iterator() {
            return listIterator();
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            return new TransformedListIterator<F, T>(fromList.listIterator(index)) {
                @Override
                T transform(F from) {
                    return function.apply(from);
                }
            };
        }

        @Override
        public boolean isEmpty() {
            return fromList.isEmpty();
        }

        @Override
        public T remove(int index) {
            return function.apply(fromList.remove(index));
        }

        @Override
        public int size() {
            return fromList.size();
        }

        private static final long serialVersionUID = 0;
    }


}
