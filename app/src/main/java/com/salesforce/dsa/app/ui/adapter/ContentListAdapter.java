package com.salesforce.dsa.app.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.abinbev.dsa.model.CN_DSA_Azure_File__c;
import com.salesforce.dsa.app.R;
import com.salesforce.dsa.app.utils.ContentUtils;

import java.util.List;

public class ContentListAdapter extends BaseAdapter {


    private static class ViewHolder {
        TextView title;
        ImageView fileTypeIcon;
        ImageView infoIcon;
    }

    private List<CN_DSA_Azure_File__c> listData;
    private boolean showCellBackground;

    public ContentListAdapter(List<CN_DSA_Azure_File__c> listData, boolean showCellBackground) {
        super();
        this.listData = listData;
        this.showCellBackground = showCellBackground;
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

        final Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.content_list_item, parent, false);
            if (showCellBackground) {
                convertView.setBackground(context.getResources().getDrawable(R.drawable.content_row_background));
            }
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.fileTypeIcon = (ImageView) convertView.findViewById(R.id.fileTypeIcon);
            holder.infoIcon = (ImageView) convertView.findViewById(R.id.infoIcon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Object object = getItem(position);
        final CN_DSA_Azure_File__c cv = (CN_DSA_Azure_File__c) object;

        String title = cv.getDsaFileName();
        int fileTypeResourceId = ContentUtils.getDrawableResourceId(cv.getDsaFileTpye());
        holder.fileTypeIcon.setImageDrawable(holder.fileTypeIcon.getContext().getResources().getDrawable(fileTypeResourceId));
        holder.fileTypeIcon.setVisibility(View.VISIBLE);
        holder.title.setText(title);

        convertView.setContentDescription(title);

        holder.infoIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(cv.getDsaFileName());
                String desc = cv.getDsaFileName();
                builder.setMessage(desc == null || "null".equals(desc) || desc.isEmpty() ? "" : desc);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return convertView;
    }

}
