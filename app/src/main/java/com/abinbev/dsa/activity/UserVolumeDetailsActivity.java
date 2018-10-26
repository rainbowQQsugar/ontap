package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.VolumeProgressAdapter;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.ui.customviews.ExpandedGridView;
import com.abinbev.dsa.ui.presenter.UserVolumeDetailsPresenter;

import java.util.List;

import butterknife.Bind;

public class UserVolumeDetailsActivity extends AppBaseActivity implements UserVolumeDetailsPresenter.ViewModel, AdapterView.OnItemClickListener {

    public static final String ARGS_VOLUME_ID = "volume_id";
    public static final String ARGS_USER_ID = "user_id";

    public static final String TAG = UserVolumeDetailsActivity.class.getSimpleName();

    @Bind(R.id.volume_label)
    TextView titleTextView;

    @Bind(R.id.volume_secondary_label)
    TextView subtitleTextView;

    @Bind(R.id.volume_list)
    ExpandedGridView gridView;

    private UserVolumeDetailsPresenter volumePresenter;

    private VolumeProgressAdapter volumeAdapter;

    private String volumeId;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.volume));
        getSupportActionBar().setSubtitle(getString(R.string.my_360));

        Intent intent = getIntent();
        if (intent != null) {
            volumeId = intent.getStringExtra(ARGS_VOLUME_ID);
            userId = intent.getStringExtra(ARGS_USER_ID);
        }

        gridView.setExpanded(true);
        gridView.setOnItemClickListener(this);
        volumePresenter = new UserVolumeDetailsPresenter(this, userId, volumeId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        volumePresenter.setViewModel(this);
        volumePresenter.start();
    }

    @Override
    protected void onStop() {
        volumePresenter.stop();
        super.onStop();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_user_volume_details;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setData(List<KPI__c> kpis) {
        if (volumeAdapter == null) {
            volumeAdapter = new VolumeProgressAdapter();
            gridView.setAdapter(volumeAdapter);
        }
        volumeAdapter.setData(kpis);
        volumeAdapter.notifyDataSetChanged();
    }

    @Override
    public void setDays(int currentDay, int maxDays) {
        subtitleTextView.setText(getString(R.string.days_of_sale, currentDay, maxDays));
    }

    @Override
    public void setUser(User user) {
        String noteTitle = getString(R.string.volume) + " " + user.getName();
        getSupportActionBar().setTitle(noteTitle);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        volumePresenter.onVolumeClicked(volumeAdapter.getItem(position));
    }
}
