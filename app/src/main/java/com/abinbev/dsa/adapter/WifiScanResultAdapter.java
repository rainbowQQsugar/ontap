package com.abinbev.dsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.ui.presenter.WifiScanResultListPresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WifiScanResultAdapter extends BaseAdapter {

    private final List<WifiScanResultListPresenter.WifiScanResultRow> scanResults;

    public WifiScanResultAdapter() {
        super();
        scanResults = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return scanResults.size();
    }

    @Override
    public WifiScanResultListPresenter.WifiScanResultRow getItem(int position) {
        return scanResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.list_item_wifi_scan_result, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }

        WifiScanResultListPresenter.WifiScanResultRow scanResult = getItem(position);

        // First item shows the tag we are searching for.
        int textColor = position == 0 ? R.color.status_no : R.color.abi_blue;

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.name.setText(scanResult.name);
        viewHolder.name.setTextColor(context.getResources().getColor(textColor));
        viewHolder.value.setText(scanResult.value);

        return convertView;
    }

    public void setData(List<WifiScanResultListPresenter.WifiScanResultRow> scanResults) {
        this.scanResults.addAll(scanResults);
        this.notifyDataSetChanged();
    }

    class ViewHolder {

        @Bind(R.id.scan_result_name)
        TextView name;

        @Bind(R.id.scan_result_value)
        TextView value;

        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }
    }
}
