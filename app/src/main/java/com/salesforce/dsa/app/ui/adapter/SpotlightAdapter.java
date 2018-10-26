package com.salesforce.dsa.app.ui.adapter;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.salesforce.androidsyncengine.utils.Guava.GuavaUtils;
import com.salesforce.dsa.app.R;
import com.salesforce.dsa.app.utils.FileIconUtil;
import com.salesforce.dsa.data.model.ContentVersion;
import com.salesforce.dsa.utils.Thumbnails;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author nickc (nick.c@akta.com).
 */
public class SpotlightAdapter extends BaseAdapter {

    private static final String LOG_TAG = SpotlightAdapter.class.getSimpleName();

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;

    private List<ContentVersion> featuredContent;
    private List<ContentVersion> recentlyUpdatedContent;
    private SpotlightFilter spotlightFilter = SpotlightFilter.ALL;

    public SpotlightAdapter() {
    }

    public void setFeaturedContent(List<ContentVersion> featuredContent) {
        if (this.featuredContent == null) {
            this.featuredContent = new ArrayList<>();
        } else {
            this.featuredContent.clear();
        }
        this.featuredContent.addAll(featuredContent);
        notifyDataSetChanged();
    }

    public void setRecentlyUpdatedContent(List<ContentVersion> recentlyUpdatedContent) {
        if (this.recentlyUpdatedContent == null) {
            this.recentlyUpdatedContent = new ArrayList<>();
        } else {
            this.recentlyUpdatedContent.clear();
        }
        this.recentlyUpdatedContent.addAll(recentlyUpdatedContent);
        notifyDataSetChanged();
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return getFeaturedContentCount() + getRecentlyUpdatedContentCount();
    }

    private int getFeaturedContentCount() {
        return spotlightFilter != SpotlightFilter.NEW_AND_UPDATED ? sizeOf(featuredContent) + 1 : 0; //add one for section header
    }

    private int getRecentlyUpdatedContentCount() {
        return spotlightFilter != SpotlightFilter.FEATURED ? sizeOf(recentlyUpdatedContent) + 1 : 0; //add one for section header
    }

    private int sizeOf(List list) {
        return list == null ? 0 : list.size();
    }

    @Override
    public ContentVersion getItem(int position) {

        if (getItemViewType(position) == TYPE_HEADER) {
            return null;
        }

        if (spotlightFilter == SpotlightFilter.ALL) {
            if (position < getFeaturedContentCount()) {
                int calculatedPosition = position - 1; //subtract one for header
                return featuredContent.get(calculatedPosition);
            } else {
                int calculatedPosition = position - getFeaturedContentCount() - 1; //subtract one for header
                return recentlyUpdatedContent.get(calculatedPosition);
            }
        } else if (spotlightFilter == SpotlightFilter.FEATURED) {
            int calculatedPosition = position - 1; //subtract one for header
            return featuredContent.get(calculatedPosition);
        } else {
            int calculatedPosition = position - 1; //subtract one for header
            return recentlyUpdatedContent.get(calculatedPosition);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private String getHeaderText(int position) {
        String headerText = "Featured";

        if (spotlightFilter == SpotlightFilter.NEW_AND_UPDATED || position > 0) {
            headerText = "New & Updated Content";
        }

        return headerText;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (getItemViewType(position) == TYPE_HEADER) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.spotlight_section_header, parent, false);
            ((TextView) convertView.findViewById(R.id.spotlight_header)).setText(getHeaderText(position));
            return convertView;
        }

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.spotlight_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.type = (ImageView) convertView.findViewById(R.id.item_type);
            viewHolder.title = (TextView) convertView.findViewById(R.id.item_title);
            viewHolder.description = (TextView) convertView.findViewById(R.id.item_description);
            viewHolder.location = (TextView) convertView.findViewById(R.id.item_location);
            viewHolder.dateAdded = (TextView) convertView.findViewById(R.id.item_date_added);
            convertView.setTag(viewHolder);
        }

        ContentVersion contentVersion = getItem(position);

        viewHolder = (ViewHolder) convertView.getTag();

        //Set the default image as an icon
        viewHolder.type.setImageDrawable(viewHolder.type.getContext().getResources().getDrawable(FileIconUtil.getFileIconThumbnailForFileType(contentVersion.getFileType())));
        viewHolder.type.setBackgroundColor(viewHolder.type.getContext().getResources().getColor(android.R.color.transparent));

        //Override the icon with an image preview if possible
        Uri thumbnail = Thumbnails.getThumbnail(viewHolder.type.getContext(), contentVersion);
        if (thumbnail != null) {

            Picasso.with(viewHolder.type.getContext())
                    .load(thumbnail)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .into(viewHolder.type);

            viewHolder.type.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            viewHolder.type.setBackgroundColor(viewHolder.type.getContext().getResources().getColor(android.R.color.black));
        }
        viewHolder.title.setText(contentVersion.getTitle());
        viewHolder.description.setText(contentVersion.getDescription());
        viewHolder.location.setText(contentVersion.getCategoryLocation());
        viewHolder.dateAdded.setText(convertDateFormat(contentVersion.getCreatedDate()));

        return convertView;
    }

    private String convertDateFormat(String dateToBeParsed) {
        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        DateFormat outputFormat = SimpleDateFormat.getDateTimeInstance();

        if (!GuavaUtils.isNullOrEmpty(dateToBeParsed)) {
            try {
                Date parsedDate = parseFormat.parse(dateToBeParsed);
                return outputFormat.format(parsedDate);
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error parsing date: " + dateToBeParsed);
            }
        }

        return "N/A";
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || (spotlightFilter != SpotlightFilter.NEW_AND_UPDATED ? position == getFeaturedContentCount() : position == getRecentlyUpdatedContentCount())) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_ITEM;
    }

    public void filter(SpotlightFilter spotlightFilter) {
        this.spotlightFilter = spotlightFilter;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        ImageView type;
        TextView title;
        TextView description;
        TextView location;
        TextView dateAdded;
    }

    public enum SpotlightFilter {
        ALL, FEATURED, NEW_AND_UPDATED
    }

}
