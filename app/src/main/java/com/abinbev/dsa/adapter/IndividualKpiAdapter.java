package com.abinbev.dsa.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.abinbev.dsa.R;
import com.abinbev.dsa.fragments.PerformanceKpiFragment;
import com.abinbev.dsa.fragments.UserKpiDetailsFragment;

public class IndividualKpiAdapter extends FragmentStatePagerAdapter {
    private final Context context;
    private UserKpiDetailsFragment userKpiDetailsFragment;
    private PerformanceKpiFragment performanceKpiFragment;

    public IndividualKpiAdapter(FragmentManager fragmentManager, Context context, String kpiId, String userId, String categoryName) {
        super(fragmentManager);
        this.context = context;
        userKpiDetailsFragment = UserKpiDetailsFragment.newInstance(kpiId, userId, categoryName);
        performanceKpiFragment = PerformanceKpiFragment.newInstance(kpiId, userId);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        return position == 0 ? userKpiDetailsFragment : performanceKpiFragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return position == 0 ? context.getResources().getString(R.string.basic_kpi) :
                context.getResources().getString(R.string.performance_kpi);
    }

    public void onSyncCompleted() {
        if (userKpiDetailsFragment != null && performanceKpiFragment != null) {
            performanceKpiFragment.shouldRefrehDatas = true;
            userKpiDetailsFragment.presentStart();
            performanceKpiFragment.presentStart();
        }

    }
}
