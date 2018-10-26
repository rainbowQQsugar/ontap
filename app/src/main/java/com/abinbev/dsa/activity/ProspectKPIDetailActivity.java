package com.abinbev.dsa.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.ui.presenter.ProspectKPIDetailPresenter;
import com.abinbev.dsa.ui.view.KPIDetailChartView;
import com.abinbev.dsa.ui.view.ProspectKPIDetailFilterDialog;
import com.abinbev.dsa.utils.DateUtils;

import butterknife.Bind;

public class ProspectKPIDetailActivity extends AppBaseActivity implements ProspectKPIDetailFilterDialog.ProspectsFilterDialogListener, ProspectKPIDetailPresenter.ViewModel {

    private static final String TAG = ProspectKPIDetailActivity.class.getSimpleName();

    private ProspectKPIDetailFilterDialog prospectKPIDetailFilterDialog;

    private int[] colors;

    @Bind(R.id.filter_status_text)
    public TextView filterText;

    @Bind(R.id.chart_view)
    public KPIDetailChartView chartView;

    @Bind(R.id.kpi_items_layout)
    public LinearLayout kpiItemsLayout;

    ProspectKPIDetailPresenter presenter;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_prospect_kpi_detail;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");
        prospectKPIDetailFilterDialog = new ProspectKPIDetailFilterDialog(this, this);
        colors = new int[]{
                ActivityCompat.getColor(this, R.color.chart_kpi_color_1),
                ActivityCompat.getColor(this, R.color.chart_kpi_color_2),
                ActivityCompat.getColor(this, R.color.chart_kpi_color_3),
                ActivityCompat.getColor(this, R.color.chart_kpi_color_4)
        };
        presenter = new ProspectKPIDetailPresenter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.setViewModel(this);
            presenter.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (presenter != null)
            presenter.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.filter_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.menu_filter: {
                prospectKPIDetailFilterDialog.show();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDialogFilterClick(ProspectKPIDetailFilterDialog.ProspectsKPIDetailFilterSelection prospectsKPIDetailFilterSelection) {

        presenter.filterByDate(prospectsKPIDetailFilterSelection.startDate, prospectsKPIDetailFilterSelection.endDate);
        filterText.setTextColor(ActivityCompat.getColor(this, R.color.sab_gray));
        if (prospectsKPIDetailFilterSelection.startDate == null && prospectsKPIDetailFilterSelection.endDate == null) {
            filterText.setText(R.string.prospect_kpi_all);
        } else {
            if (prospectsKPIDetailFilterSelection.endDate != null &&
                    prospectsKPIDetailFilterSelection.startDate != null &&
                    prospectsKPIDetailFilterSelection.endDate.before(prospectsKPIDetailFilterSelection.startDate)) {
                filterText.setTextColor(ActivityCompat.getColor(this, R.color.red));
            }

            String filterStatus = "(";
            if (prospectsKPIDetailFilterSelection.startDate != null) {
                filterStatus += DateUtils.dateToDateString(prospectsKPIDetailFilterSelection.startDate);
            }
            filterStatus += "~";
            if (prospectsKPIDetailFilterSelection.endDate != null) {
                filterStatus += DateUtils.dateToDateString(prospectsKPIDetailFilterSelection.endDate);
            }
            filterStatus += ")";
            filterText.setText(filterStatus);

        }
    }

    private int getColorByIndex(int index) {
        return colors[index % colors.length];
    }

    @Override
    public void clearData() {
        chartView.clear();
        kpiItemsLayout.removeAllViews();
    }

    @Override
    public void addChartData(int index, String label, Integer value) {
        chartView.addItem(label, value, getColorByIndex(index));
    }

    @Override
    public void addItemData(int index, String label) {
        View view = LayoutInflater.from(this).inflate(R.layout.prospect_kpi_detail_item, null);
        View colorView = view.findViewById(R.id.item_color);
        TextView textView = (TextView) view.findViewById(R.id.item_text);
        colorView.setBackgroundColor(getColorByIndex(index));
        textView.setText(label);
        kpiItemsLayout.addView(view);
    }

    @Override
    public void presenterData() {
        chartView.prepare();
    }
}
