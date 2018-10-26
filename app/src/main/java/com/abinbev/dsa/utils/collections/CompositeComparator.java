package com.abinbev.dsa.utils.collections;

import java.util.Comparator;

public class CompositeComparator<T> implements Comparator<T> {

    private final Comparator<T>[] comparators;

    @SafeVarargs
    public CompositeComparator(Comparator<T>... comparators) {
        if (comparators == null || comparators.length == 0) {
            throw new IllegalArgumentException("Must include at least one child comparator");
        }
        this.comparators = comparators;
    }

    @Override
    public int compare(final T first, final T second) {
        int result = 0;
        for (Comparator<T> comparator : comparators) {
            result = comparator.compare(first, second);
            if (result != 0) {
                return result;
            }
        }
        return result;
    }

}