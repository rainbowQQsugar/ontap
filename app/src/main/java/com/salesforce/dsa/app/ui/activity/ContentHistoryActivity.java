package com.salesforce.dsa.app.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.salesforce.dsa.DSAAppState;
import com.salesforce.dsa.app.R;
import com.salesforce.dsa.app.ui.clickListeners.ContentVersionClickListener;
import com.salesforce.dsa.data.model.ContentVersion;

import java.util.List;

public class ContentHistoryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.history_list);

        List<ContentVersion> historyItems = DSAAppState.getInstance().getHistoryItemsFromPrefs(this);
        if (historyItems.size() > 0) {
            DSAAppState.getInstance().setHistoryItems(historyItems);
        }

        final ListView listView = (ListView) findViewById(R.id.history_list);
        listView.setAdapter(new HistoryListAdapter(this, historyItems));

        listView.setOnItemClickListener(new ContentVersionClickListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class HistoryListAdapter extends BaseAdapter {

        private class ViewHolder {
            TextView title;
        }

        private List<ContentVersion> listData;
        private LayoutInflater layoutInflator;

        public HistoryListAdapter(Context context, List<ContentVersion> listData) {
            super();
            this.listData = listData;
            layoutInflator = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            if (listData == null) {
                return 0;
            } else {
                return listData.size();
            }
        }

        @Override
        public Object getItem(int arg0) {
            if (listData == null)
                return null;
            else
                return listData.get(arg0);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null) {
                convertView = layoutInflator.inflate(android.R.layout.simple_list_item_1, parent, false);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(android.R.id.text1);
                holder.title.setTextColor(getResources().getColor(android.R.color.black));
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Object object = getItem(position);

            ContentVersion cv = (ContentVersion) object;
            holder.title.setText(cv.getTitle());

            return convertView;
        }
    }

}
