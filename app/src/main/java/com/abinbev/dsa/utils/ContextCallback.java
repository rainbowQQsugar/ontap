package com.abinbev.dsa.utils;

import android.content.Context;

/**
 * Created by mewa on 6/28/17.
 */

/**
 * Interface for components being able to register and unregister within a {@link Context}
 */
public interface ContextCallback<T> {
    /**
     * Registers {@link Context} dependent component with a callback
     * @param context {@link Context} to be registered in
     * @param callback callback to which updates should be delivered
     */
    void register(Context context, T callback);

    /**
     * Unregisters {@link Context} dependent component and unsets specified callback
     * @param context {@link Context} to be unregistered from
     * @param callback callback to be removed
     */
    void unregister(Context context, T callback);
}
