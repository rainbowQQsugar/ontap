package com.salesforce.dsa.app.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.salesforce.dsa.app.R;
import com.salesforce.dsa.app.utils.ContentUtils;
import com.salesforce.dsa.app.utils.DataUtils;
import com.salesforce.dsa.data.model.Attachment;
import com.salesforce.dsa.data.model.Category__c;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class CategoryListAdapter extends BaseAdapter {

    private static class ViewHolder {
        public TextView categoryName;
        public ImageView categoryBackground;
    }

    private static final String TAG = CategoryListAdapter.class.getName();

    private List<Category__c> categories;
    private boolean isTopLevel;

    public CategoryListAdapter(List<Category__c> categories, boolean isTopLevel) {
        super();
        this.categories = categories;
        this.isTopLevel = isTopLevel;
    }

    @Override
    public int getCount() {
        if (categories == null) {
            return 0;
        } else {
            return categories.size();
        }
    }

    @Override
    public Object getItem(int arg0) {

        if (categories == null)
            return null;
        else
            return categories.get(arg0);
    }

    public List<Category__c> getItems() {
        return categories;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        ViewHolder holder;
        if (convertView == null) {
            if (isTopLevel) {
                convertView = layoutInflater.inflate(R.layout.category_list_item_main, parent, false);
            } else {
                convertView = layoutInflater.inflate(R.layout.category_list_item, parent, false);
            }
            holder = new ViewHolder();
            holder.categoryName = (TextView) convertView.findViewById(R.id.categoryName);
            holder.categoryBackground = (ImageView) convertView.findViewById(R.id.categoryBackground);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Category__c category = categories.get(position);
        Log.i(TAG, "getJSON: " + category.toString());
        holder.categoryName.setText(category.getName());

        convertView.setContentDescription(category.getName());

        if (isTopLevel) {
            holder.categoryBackground.setVisibility(View.GONE);
        } else {
            holder.categoryBackground.setVisibility(View.VISIBLE);
            List<Attachment> attachments = DataUtils.fetchAttachmentsForCategory(category);
            if (attachments != null && attachments.size() > 0) {

                String filePath = ContentUtils.getImageFilePathFromFileId(context, attachments.get(0).getId());
                File imageFile = new File(context.getFilesDir(), filePath);
                if (imageFile.exists()) {
                    Picasso.with(context).load(Uri.fromFile(imageFile)).into(holder.categoryBackground);
                }
            }
        }

        return convertView;
    }
}
