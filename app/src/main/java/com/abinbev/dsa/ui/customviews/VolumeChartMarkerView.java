package com.abinbev.dsa.ui.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.ImageView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.KPI__c;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

/**
 * Created by Jakub Stefanowski on 07.02.2017.
 */

public class VolumeChartMarkerView extends MarkerView {

    private TextView tvContent;

    private ImageView circleImage;

    private MPPointF offsetPoint = new MPPointF();

    public VolumeChartMarkerView(Context context) {
        super(context, R.layout.volume_chart_marker);
        tvContent = (TextView) findViewById(android.R.id.text1);
        circleImage = (ImageView) findViewById(R.id.chart_marker_circle);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        KPI__c kpi = (KPI__c) e.getData();
        double percentage = kpi.getPercentageIncrease() * 100;

        // Set value as percent
        tvContent.setText(String.format("%+.1f%%", percentage));

        // Set marker icon color
        int color = getColor(percentage < 0 ? R.color.status_no : R.color.abi_blue);
        circleImage.setImageTintList(ColorStateList.valueOf(color));

        super.refreshContent(e, highlight);
    }

    private int getColor(int res) {
        return getContext().getResources().getColor(res);
    }

    @Override
    public MPPointF getOffset() {
        offsetPoint.x = - (getWidth() / 2);
        offsetPoint.y = -getHeight() + (circleImage.getHeight() / 2);
        return offsetPoint;
    }
}