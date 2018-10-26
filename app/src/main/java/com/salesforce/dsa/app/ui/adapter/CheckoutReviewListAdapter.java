package com.salesforce.dsa.app.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.salesforce.dsa.app.R;
import com.salesforce.dsa.app.utils.ContentUtils;
import com.salesforce.dsa.data.model.TrackedDocument;

import java.util.List;

public class CheckoutReviewListAdapter extends BaseAdapter {

    private static class ViewHolder {
        TextView titleTextView;
        ImageView thumbnailIcon;
        ImageView docTypeIcon;
        Switch mailToContactSwitch;
        RatingBar ratingBar;
    }

    private List<TrackedDocument> listData;

    public CheckoutReviewListAdapter(List<TrackedDocument> listData) {
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
    public TrackedDocument getItem(int position) {
        if (listData == null)
            return null;
        else
            return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.checkout_review_list_item, parent, false);
            holder = new ViewHolder();
            holder.titleTextView = (TextView) convertView.findViewById(R.id.docTitle);
            holder.thumbnailIcon = (ImageView) convertView.findViewById(R.id.thumbnailIcon);
            holder.docTypeIcon = (ImageView) convertView.findViewById(R.id.docTypeIcon);
            holder.mailToContactSwitch = (Switch) convertView.findViewById(R.id.mailToContactSwitch);
            holder.ratingBar = (RatingBar) convertView.findViewById(R.id.ratingBar);

            // set Listeners
            addListenerOnRatingBar(holder.ratingBar, position);
            addListenerOnSwitch(holder.mailToContactSwitch, position);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Object object = getItem(position);

        TrackedDocument trackedDoc = (TrackedDocument) object;
        String title = trackedDoc.getContentVersion().getTitle();
        // holder.moreIcon.setVisibility(View.GONE);
        int fileTypeResourceId = ContentUtils.getDrawableResourceId(trackedDoc.getContentVersion().getFileType());
        holder.docTypeIcon.setImageDrawable(holder.docTypeIcon.getContext().getResources().getDrawable(fileTypeResourceId));
        holder.titleTextView.setText(title);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), ContentUtils.getDrawableResourceId(trackedDoc.getContentVersion().getFileType()));

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 80, 80, true);
        holder.thumbnailIcon.setImageBitmap(resized);

        return convertView;
    }

    public void addListenerOnRatingBar(RatingBar ratingBar, final int position) {
        ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                TrackedDocument trackedDoc = getItem(position);
                trackedDoc.setRating(rating);
            }
        });
    }

    public void addListenerOnSwitch(Switch mailToSwitch, final int position) {
        mailToSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TrackedDocument trackedDoc = getItem(position);
                trackedDoc.setMarkedToEmail(isChecked);
            }
        });
    }
}