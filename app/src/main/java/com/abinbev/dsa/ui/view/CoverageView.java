package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.ui.customviews.ExpandedGridView;
import com.abinbev.dsa.ui.presenter.CoveragePresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CoverageView extends RelativeLayout implements CoveragePresenter.ViewModel, RefreshListener, AdapterView.OnItemClickListener {

    public interface OnCoverageClickedListener {
        void onCoverageClicked(KPI__c kpi);
    }

    @Bind(R.id.coverage_list)
    ExpandedGridView coverageList;

    private CoveragePresenter coveragePresenter;

    private CoverageAdapter coverageAdapter;

    private OnCoverageClickedListener listener;

    public CoverageView(Context context) {
        this(context, null);
    }

    public CoverageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    private void setup(Context context) {
        inflate(context, R.layout.merge_coverage_view, this);
        ButterKnife.bind(this);

        coverageList.setExpanded(true);
        coverageList.setOnItemClickListener(this);
    }

    public void setAccountId(String accountId) {
        if (coveragePresenter == null) {
            coveragePresenter = new CoveragePresenter(getContext(), accountId);
        }
        coveragePresenter.setViewModel(this);
        coveragePresenter.start();
    }

    public void setOnCoverageClickedListener(OnCoverageClickedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (coveragePresenter != null) coveragePresenter.stop();
    }

    @Override
    public void setData(List<CoveragePresenter.ParentKpiDetails> kpis) {
        if (coverageAdapter == null) {
            coverageAdapter = new CoverageAdapter();
            coverageList.setAdapter(coverageAdapter);
        }
        coverageAdapter.setItems(kpis);
        coverageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        if (coveragePresenter != null) {
            coveragePresenter.start();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (listener != null) {
            KPI__c kpi = coverageAdapter.getKpi(position);
            listener.onCoverageClicked(kpi);
        }
    }

    static class CoverageAdapter extends BaseAdapter {

        List<CoveragePresenter.ParentKpiDetails> kpisList = new ArrayList<>();

        public void setItems(List<CoveragePresenter.ParentKpiDetails> list) {
            kpisList.clear();
            if (list != null) {
                kpisList.addAll(list);
            }
        }

        public KPI__c getKpi(int position) {
            return kpisList.get(position).getKpi();
        }

        @Override
        public int getCount() {
            return kpisList.size();
        }

        @Override
        public CoveragePresenter.ParentKpiDetails getItem(int position) {
            return kpisList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CoverageVH holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                convertView = inflater.inflate(R.layout.coverage_list_item, parent, false);
                holder = new CoverageVH(convertView);
                convertView.setTag(holder);
            }
            else {
                holder = (CoverageVH) convertView.getTag();
            }

            CoveragePresenter.ParentKpiDetails kpiDetails = kpisList.get(position);
            double actual = kpiDetails.getChildrenActualSum();
            double target = kpiDetails.getChildrenTargetSum();
            int progress = target == 0 ? 0 : Math.min((int) ((actual * 100) / target), 100);
            String kpiName = kpiDetails.getKpi().getKpiName();

            holder.title.setText(parent.getContext().getString(R.string.coverage_progress_title,
                    kpiName, Math.round(actual), Math.round(target)));
            holder.percent.setText(progress + "%");
            holder.progress.setProgress(progress);

            return convertView;
        }
    }

    static class CoverageVH {

        @Bind(R.id.coverage_item_title)
        TextView title;

        @Bind(R.id.coverage_item_percent)
        TextView percent;

        @Bind(R.id.coverage_item_progress)
        ProgressBar progress;

        CoverageVH(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
