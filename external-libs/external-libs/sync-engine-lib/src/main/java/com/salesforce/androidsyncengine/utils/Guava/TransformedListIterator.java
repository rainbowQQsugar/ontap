package com.salesforce.androidsyncengine.utils.Guava;

import java.util.ListIterator;

/**
 * Copyright (C) 2012 The Guava Authors
 */
abstract class TransformedListIterator<F, T> extends TransformedIterator<F, T>
        implements ListIterator<T> {
    TransformedListIterator(ListIterator<? extends F> backingIterator) {
        super(backingIterator);
    }

    private ListIterator<? extends F> backingIterator() {
        return (ListIterator<? extends F>) backingIterator;
    }

    @Override
    public final boolean hasPrevious() {
        return backingIterator().hasPrevious();
    }

    @Override
    public final T previous() {
        return transform(backingIterator().previous());
    }

    @Override
    public final int nextIndex() {
        return backingIterator().nextIndex();
    }

    @Override
    public final int previousIndex() {
        return backingIterator().previousIndex();
    }

    @Override
    public void set(T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(T element) {
        throw new UnsupportedOperationException();
    }
}
