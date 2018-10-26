package com.salesforce.androidsyncengine.utils.Guava;

import java.util.Iterator;

/*
 * Copyright (C) 2012 The Guava Authors
 */
abstract class TransformedIterator<F, T> implements Iterator<T> {
    final Iterator<? extends F> backingIterator;

    TransformedIterator(Iterator<? extends F> backingIterator) {
        this.backingIterator = GuavaUtils.checkNotNull(backingIterator);
    }

    abstract T transform(F from);

    @Override
    public final boolean hasNext() {
        return backingIterator.hasNext();
    }

    @Override
    public final T next() {
        return transform(backingIterator.next());
    }

    @Override
    public final void remove() {
        backingIterator.remove();
    }
}
