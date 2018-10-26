package com.salesforce.dsa.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.abinbev.dsa.model.CN_DSA_Azure_File__c;
import com.salesforce.dsa.app.R;
import com.salesforce.dsa.app.utils.ContentUtils;
import com.salesforce.dsa.data.model.Category__c;

import java.util.List;

public class MenuBrowserListAdapter extends BaseAdapter {

    private static class ViewHolder {
        TextView title;
        ImageView fileTypeIcon;
        // ImageView moreIcon;
    }

    private List<? extends Object> listData;

    public MenuBrowserListAdapter(List<? extends Object> listData) {
        super();
        this.listData = listData;
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

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.menu_browser_list_item, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.fileTypeIcon = (ImageView) convertView.findViewById(R.id.fileTypeIcon);
            // holder.moreIcon = (ImageView) convertView.findViewById(R.id.moreIcon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Object object = getItem(position);
        String title = null;
        if (object instanceof Category__c) {
            Category__c cat = (Category__c) object;
            title = cat.getName();
            // holder.moreIcon.setVisibility(View.VISIBLE);
            holder.fileTypeIcon.setVisibility(View.GONE);
        } else if (object instanceof CN_DSA_Azure_File__c) {
            CN_DSA_Azure_File__c cv = (CN_DSA_Azure_File__c) object;
            title = cv.getDsaFileName();
            // holder.moreIcon.setVisibility(View.GONE);
            int fileTypeResourceId = ContentUtils.getDrawableResourceId(cv.getDsaFileTpye());
            holder.fileTypeIcon.setImageDrawable(holder.fileTypeIcon.getContext().getResources().getDrawable(fileTypeResourceId));
            holder.fileTypeIcon.setVisibility(View.VISIBLE);
        }
        holder.title.setText(title);

        return convertView;
    }

}
