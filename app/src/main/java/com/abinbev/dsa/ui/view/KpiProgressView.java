package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.CN_KPI_Statistic__c;
import com.abinbev.dsa.model.KPI__c;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Adam Chodera on 18.07.2017.
 */
public class KpiProgressView extends FrameLayout {

    @Bind(R.id.kpi_progress_view_label)
    TextView label;

    @Bind(R.id.kpi_progress_view_progress)
    ProgressBar userVolumeProgress;

    @Bind(R.id.kpi_progress_view_percent)
    TextView userVolumePercent;

    @Bind(R.id.kpi_progress_view_actual_value)
    TextView userVolumeActualValue;

    @Bind(R.id.kpi_progress_view_actual_units)
    TextView userVolumeActualUnits;

    @Bind(R.id.kpi_progress_view_target_value)
    TextView userVolumeTargetValue;

    @Bind(R.id.kpi_progress_view_target_units)
    TextView userVolumeTargetUnits;

    @Bind(R.id.volume_item_date)
    TextView volume_item_date;

    @Bind(R.id.ll_volume_info)
    LinearLayout ll_volume_info;


    private KPI__c firstKpi;
    private String userId;

    public KpiProgressView(@NonNull final Context context) {
        super(context);

        init();
    }

    public KpiProgressView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public KpiProgressView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        inflate(getContext(), R.layout.kpi_progress_view, this);
        ButterKnife.bind(this);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                1.0f
        );
        setLayoutParams(param);
    }

    public void setupKpiProgress(final List<KPI__c> kpis, String userId) {
        firstKpi = kpis.get(0);
        this.userId = userId;

        final String category = firstKpi.getTranslatedCategory();
        label.setText(category);

        int percent;
        if (firstKpi.getTarget() == 0) {
            percent = 0;
        } else {
            percent = (int) ((firstKpi.getActual() / firstKpi.getTarget()) * 100);
        }
        userVolumeProgress.setProgress(percent);
        userVolumePercent.setText(percent + "%");
        userVolumeActualValue.setText(String.format("%.0f", firstKpi.getActual()));
        userVolumeActualUnits.setText(firstKpi.getTranslatedUnit());
        userVolumeTargetValue.setText(String.format("%.0f", firstKpi.getTarget()));
        userVolumeTargetUnits.setText(firstKpi.getTranslatedUnit());
    }

    public void setUpPerformanceTTLKpiProgress(CN_KPI_Statistic__c c) {
        label.setText(getContext().getString(R.string.ttl_kpi_rate));
        int totalRate = (int) Math.round(Double.valueOf(TextUtils.isEmpty(c.getTotalRate()) ? "0" : c.getTotalRate()));
        userVolumeProgress.setProgress(totalRate);
        userVolumePercent.setText(totalRate + "%");
        ll_volume_info.setVisibility(GONE);
    }

    public void setUpWeeklyVisitProgress(CN_KPI_Statistic__c c) {
        label.setText(getContext().getString(R.string.visit_rate_for_week));
        int weeklyVisitRate = (int) Math.round(Double.valueOf(TextUtils.isEmpty(c.getWeeklyVisitRate()) ? "0" : c.getWeeklyVisitRate()));
        userVolumeProgress.setProgress(weeklyVisitRate);
        userVolumePercent.setText(weeklyVisitRate + "%");
        userVolumeActualValue.setText(Math.round(Float.valueOf(c.getActualWeeklyVisit())) + "");
        userVolumeTargetValue.setText(Math.round(Float.valueOf(c.getTargetWeeklyVisit())) + "");
        volume_item_date.setVisibility(VISIBLE);
        volume_item_date.setText(String.format("%s/\n%s", c.getThisWeekFirstDay(), c.getThisWeekEndtDay()));
    }

    public void setUpPerformanceKpiBonusProportionProgress(CN_KPI_Statistic__c c) {
        label.setText(getContext().getString(R.string.bonus_rate));
        int bonusRate = (int) Math.round(Double.valueOf(TextUtils.isEmpty(c.getBonusRate()) ? "0" : c.getBonusRate()));
        userVolumeProgress.setProgress(bonusRate);
        userVolumePercent.setText(bonusRate + "%");
        ll_volume_info.setVisibility(GONE);
    }

    public void setUpMonthlyVisitProgress(CN_KPI_Statistic__c c) {
        label.setText(getContext().getString(R.string.visit_rate_for_month));
        long monthlyVisitRate = Math.round(c.getCompletedVisits() / c.getInPlannedVisits() * 100);
        userVolumeProgress.setProgress((int) monthlyVisitRate);
        userVolumePercent.setText(monthlyVisitRate + "%");
        userVolumeActualValue.setText(Math.round(c.getCompletedVisits()) + "");
        userVolumeTargetValue.setText(Math.round(c.getInPlannedVisits()) + "");
    }

    public void setUpMonthlyVisitProgress2(CN_KPI_Statistic__c c) {
        label.setText(getContext().getString(R.string.visit_rate_for_month));
        long monthlyVisitRate = Math.round(Double.valueOf(c.getVisitCompletedRate()));
        userVolumeProgress.setProgress((int) monthlyVisitRate);
        userVolumePercent.setText(c.getVisitCompletedRate() + "%");
        userVolumeActualValue.setText(Math.round(c.getCompletedVisits2()) + "");
        userVolumeTargetValue.setText(Math.round(c.getInPlannedVisits()) + "");
        volume_item_date.setVisibility(VISIBLE);
        volume_item_date.setText(String.format("%s/\n%s", c.getThisMonthFirstDay(), c.getThisMonthEndDay()));
    }


    @OnClick(R.id.user_volume_progress_container)
    public void onClick() {
//        getContext().startActivity(new Intent(getContext(), KpiDetailsActivity.class)
//                .putExtra(KpiDetailsActivity.ARGS_PARENT_CATEGORY_NAME, firstKpi.getTranslatedCategory())
//                .putExtra(UserVolumeDetailsActivity.ARGS_USER_ID, userId)
//                .putExtra(KpiDetailsActivity.ARGS_PARENT_KPI_ID, firstKpi.getKpiNum()));
    }
}
