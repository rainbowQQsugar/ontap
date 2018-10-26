package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Jakub Stefanowski on 1/15/16.
 */
public class SimpleSyncProgressView extends RelativeLayout {

    @Bind(R.id.simple_sync_progress_data)
    TextView data;

//    @Bind(R.id.simple_sync_progress_object)
//    TextView objectField;

    @Bind(R.id.simple_sync_progress_warning)
    TextView warning;

//    @Bind(R.id.simple_sync_progress_progress)
//    ProgressBar progressBar;

    @Bind(R.id.imageView)
    ImageView imageView;

    public SimpleSyncProgressView(Context context) {
        this(context, null);
    }

    public SimpleSyncProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleSyncProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.simple_sync_progress_view, this);
        ButterKnife.bind(this);
        Glide.with(context)
                .load(R.drawable.beer)
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    public void setSyncStatus(SyncStatus status) {
        if (status.getDescription() != null) {
            String description = status.getDescription();
            int breakPoint = description.indexOf(":");
            if (breakPoint != -1) {
                String state = description.substring(0, breakPoint + 1);
                String object = description.substring(breakPoint + 1, description.length());
                data.setText(state);
//                objectField.setText(object);
//                objectField.setVisibility(VISIBLE);
                imageView.setVisibility(VISIBLE);
            } else {
                data.setText(description);
                imageView.setVisibility(GONE);
//                objectField.setVisibility(GONE);
            }
        }
    }
}
