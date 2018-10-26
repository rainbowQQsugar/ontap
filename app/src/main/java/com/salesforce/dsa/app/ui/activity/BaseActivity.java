package com.salesforce.dsa.app.ui.activity;

import android.view.Menu;
import android.view.MenuItem;

import com.abinbev.dsa.activity.AppBaseDrawerActivity;

/**
 * Serves as a base activity for DSA specific activities.
 * This activity will globally handle drawer navigation and toolbar access.
 */
public abstract class BaseActivity extends AppBaseDrawerActivity {

    @Override
    public void onRefresh() {
        //TODO: Refresh the DSA views ...
    }

    @Override
    public int getLayoutResId() {
        return getChildLayoutId();
    }

    abstract int getChildLayoutId();

    abstract int getSelfNavDrawerItem();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }
}
