package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.VolumeProgressAdapter;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.ui.presenter.VolumePresenter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

public class VolumeView extends RelativeLayout implements VolumePresenter.ViewModel, RefreshListener {

    public interface OnVolumeClickedListener {
        void onVolumeClicked(KPI__c kpi);
    }

    @Bind(R.id.volume_list)
    GridView volumeList;

    @Bind(R.id.volume_secondary_label)
    TextView daysTextView;

    private VolumePresenter volumePresenter;

    private VolumeProgressAdapter volumeAdapter;

    private OnVolumeClickedListener onVolumeClickedListener;

    public VolumeView(Context context) {
        this(context, null);
    }

    public VolumeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VolumeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    private void setup(Context context) {
        inflate(context, R.layout.merge_volume_view, this);
        ButterKnife.bind(this);
    }

    public void setAccountId(String accountId) {
        if (volumePresenter == null) {
            volumePresenter = VolumePresenter.createAccountPresenter(accountId);
        }
        volumePresenter.setViewModel(this);
        volumePresenter.start();
    }

    public void setOnVolumeClickedListener(OnVolumeClickedListener onVolumeClickedListener) {
        this.onVolumeClickedListener = onVolumeClickedListener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (volumePresenter != null) volumePresenter.stop();
    }

    @Override
    public void setCurrentKpis(List<KPI__c> kpis) {
        if (volumeAdapter == null) {
            volumeAdapter = new VolumeProgressAdapter();
            volumeList.setAdapter(volumeAdapter);
        }
        volumeAdapter.setData(kpis);
        volumeAdapter.notifyDataSetChanged();
    }

    @Override
    public void setSaleDays(int currentDay, int maxDays) {
        daysTextView.setText(getContext().getString(R.string.days_of_sale, currentDay, maxDays));
    }

    @Override
    public void onRefresh() {
        if (volumePresenter != null) {
            volumePresenter.start();
        }
    }

    @OnItemClick(R.id.volume_list)
    public void onVolumeClicked(int position) {
        if (onVolumeClickedListener != null) {
            onVolumeClickedListener.onVolumeClicked(volumeAdapter.getItem(position));
        }
    }
}
