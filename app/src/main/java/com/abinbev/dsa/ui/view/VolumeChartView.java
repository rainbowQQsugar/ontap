package com.abinbev.dsa.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.ui.customviews.VolumeChartMarkerView;
import com.abinbev.dsa.utils.DateUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VolumeChartView extends FrameLayout {

    @Bind(R.id.chart)
    LineChart historyChart;


    public VolumeChartView(Context context) {
        this(context, null);
    }

    public VolumeChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VolumeChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    @TargetApi(21)
    public VolumeChartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(context);
    }

    private void setup(Context context) {
        inflate(context, R.layout.merge_volume_chart_view, this);
        ButterKnife.bind(this);

        setupChart();
    }

    public void setKpis(List<KPI__c> kpis) {
        // TODO: handle only one element
        setChartData(convertToEntries(kpis), convertToHistoricalEntries(kpis));
    }

    private void setupChart() {
        int sabDarkGrayColor = getColor(R.color.sab_dark_gray);
        float textSizeH7 = getDimension(R.dimen.text_h7);

        // Setup x axis.
        historyChart.getXAxis().setDrawGridLines(false);
        historyChart.getXAxis().setGranularity(1f); // only intervals of 1 month
        historyChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        historyChart.getXAxis().setTextColor(sabDarkGrayColor);
        historyChart.getXAxis().setTextSize(convertPixelsToDp(getResources(), textSizeH7));
        historyChart.getXAxis().setAvoidFirstLastClipping(true);
        historyChart.getXAxis().setValueFormatter(new MonthValueFormatter());

        // Setup right y axis
        historyChart.getAxisRight().setEnabled(false);

        // Setup description
        historyChart.getDescription().setEnabled(false);

        // Add custom highlight marker
        VolumeChartMarkerView markerView = new VolumeChartMarkerView(getContext());
        markerView.setChartView(historyChart);
        historyChart.setMarker(markerView);

        // Setup legend
        historyChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        historyChart.getLegend().setTextSize(convertPixelsToDp(getResources(), textSizeH7));
        historyChart.getLegend().setTextColor(sabDarkGrayColor);
        historyChart.getLegend().setFormSize(13);
        historyChart.getLegend().setXEntrySpace(8);
        historyChart.getLegend().setFormToTextSpace(7);

        // Setup chart
        historyChart.setTouchEnabled(true);
        historyChart.setScaleEnabled(false);
        historyChart.setPinchZoom(false);
        historyChart.setHighlightPerTapEnabled(true);
        historyChart.setMinOffset(0);
        historyChart.setExtraTopOffset(30);
        historyChart.setExtraLeftOffset(4);
        historyChart.setExtraRightOffset(26);

        historyChart.setNoDataText(getString(R.string.loading));

        historyChart.invalidate();
    }

    private void setChartData(List<Entry> currentData, List<Entry> historicalData) {

        int chartPrimaryColor = getColor(R.color.chart_primary);
        int chartSecondaryColor = getColor(R.color.chart_secondary);

        LineDataSet currentDataSet = new LineDataSet(currentData, getString(R.string.current_chart));
        currentDataSet.setDrawValues(false);
        currentDataSet.setHighlightEnabled(true);
        currentDataSet.setDrawHighlightIndicators(false);
        currentDataSet.setDrawFilled(true);
        currentDataSet.setColor(chartPrimaryColor, (int) (255 * 0.9));
        currentDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        currentDataSet.setFillDrawable(getDrawable(R.drawable.chart_fade_blue));
        currentDataSet.setDrawCircles(currentData.size() < 2);                                      // We need that if there is only single value.
        currentDataSet.setCircleColor(chartPrimaryColor);
        currentDataSet.setCircleColorHole(chartPrimaryColor);

        LineDataSet historicalDataSet = new LineDataSet(historicalData, getString(R.string.last_year_chart));
        historicalDataSet.setDrawValues(false);
        historicalDataSet.setHighlightEnabled(false);
        historicalDataSet.setDrawHighlightIndicators(false);
        historicalDataSet.setDrawFilled(true);
        historicalDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        historicalDataSet.setFillAlpha((int) (255 * 0.8));
        historicalDataSet.setColor(chartSecondaryColor, (int) (255 * 0.8));
        historicalDataSet.setFillColor(chartSecondaryColor);
        historicalDataSet.setDrawCircles(historicalData.size() < 2);                                // We need that if there is only single value.
        historicalDataSet.setCircleColor(chartSecondaryColor);
        historicalDataSet.setCircleColorHole(chartSecondaryColor);

        LineData lineData = new LineData(historicalDataSet, currentDataSet);

        historyChart.setData(lineData);
        historyChart.invalidate();
    }

    public static float convertPixelsToDp(Resources resources, float px){
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    private String getString(int res) {
        return getResources().getString(res);
    }

    private int getColor(int res) {
        return getResources().getColor(res);
    }

    private Drawable getDrawable(int res) {
        return getResources().getDrawable(res);
    }

    private float getDimension(int res) {
        return getResources().getDimension(res);
    }

    private List<Entry> convertToEntries(List<KPI__c> kpis) {
        List<Entry> entries = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        for (KPI__c kpi : kpis) {
            Date startDate = DateUtils.dateFromString(kpi.getStartDate());
            calendar.setTime(startDate);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);

            // I.e. if January = 0 then the previous month should be -1 to show it properly on chart.
            month -= (currentYear - year) * 12;
            float value = (float) kpi.getActual();

            entries.add(new Entry(month, value, kpi));
        }

        return entries;
    }

    private List<Entry> convertToHistoricalEntries(List<KPI__c> kpis) {
        List<Entry> entries = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        for (KPI__c kpi : kpis) {
            Date startDate = DateUtils.dateFromString(kpi.getStartDate());
            calendar.setTime(startDate);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);

            // I.e. if January = 0 then the previous month should be -1 to show it properly on chart.
            month -= (currentYear - year) * 12;

            float percentageIncrease = (float) (1 + kpi.getPercentageIncrease());
            float value = percentageIncrease == 0 ? 0 : (float) (kpi.getActual() / percentageIncrease);

            entries.add(new Entry(month, value, kpi));
        }

        return entries;
    }

    private static class MonthValueFormatter implements IAxisValueFormatter {
        String[] months = new DateFormatSymbols().getShortMonths();

        @Override
        public String getFormattedValue(float value, AxisBase axis) {

            // Since the values are drawn on chart and January is 0 then the month before equals -11.
            int month = ((int) value) % 12;
            if (value < 0) month += 12;

            return months[month].toUpperCase();
        }
    }
}
