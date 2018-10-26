package com.abinbev.dsa.activity;

import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.abinbev.dsa.R;
import com.abinbev.dsa.fragments.TimelineFragment;
import com.abinbev.dsa.model.Account;

public class TimelineActivity extends AppCompatActivity {

    public static final String ACCOUNT_ID_EXTRA = "accountId";

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(getPreferredOrientation());
        setContentView(R.layout.activity_timeline);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        String accountId = getIntent().getStringExtra(ACCOUNT_ID_EXTRA);
        actionBar.setTitle(Account.getById(accountId).getName());
        actionBar.setSubtitle(R.string.timeline);

        setupViewPager();
    }

    private int getPreferredOrientation() {
        return getResources().getBoolean(R.bool.is10InchTablet) ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager() {
        // Create the pedidoListAdapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections pedidoListAdapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_timeline_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_marker_visited);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_notes_24dp);

        final int colorActive;
        final int colorInactive;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            colorActive = getResources().getColor(R.color.white, getTheme());
            colorInactive = getResources().getColor(R.color.white80, getTheme());
        } else {
            colorActive = getResources().getColor(R.color.white);
            colorInactive = getResources().getColor(R.color.white80);
        }

        final int selected = tabLayout.getSelectedTabPosition();
        for (int i = 0; i < tabLayout.getTabCount(); ++i) {
            if (i == selected)
                tabLayout.getTabAt(i).getIcon().setColorFilter(colorActive, PorterDuff.Mode.SRC_ATOP);
            else
                tabLayout.getTabAt(i).getIcon().setColorFilter(colorInactive, PorterDuff.Mode.SRC_ATOP);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(colorActive, PorterDuff.Mode.SRC_ATOP);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(colorInactive, PorterDuff.Mode.SRC_ATOP);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            String accountId = getIntent().getStringExtra(ACCOUNT_ID_EXTRA);
            switch (position) {
                case 0:
                    return TimelineFragment.newInstance(accountId, true, true);
                case 1:
                    return TimelineFragment.newInstance(accountId, true, false);
                case 2:
                    return TimelineFragment.newInstance(accountId, false, true);
            }
            throw new IllegalArgumentException("ViewPager supports only 3 children but item " + position + " was requested");
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.all);
                case 1:
                    return getString(R.string.visits);
                case 2:
                    return getString(R.string.notes);
            }
            return super.getPageTitle(position);
        }
    }
}
