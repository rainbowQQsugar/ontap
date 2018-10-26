package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Attachment;
import com.squareup.picasso.Picasso;
import java.io.File;

/**
 * Created by wandersonblough on 12/3/15.
 */
public class FilePreview extends FrameLayout {

    private static final String TAG = FilePreview.class.getSimpleName();

    @Bind(R.id.file_type)
    TextView fileType;

    @Bind(R.id.image_preview)
    ImageView imagePreview;

    @Bind(R.id.download_icon)
    ImageView downloadIcon;

    public FilePreview(Context context) {
        this(context, null);
    }

    public FilePreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FilePreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.merge_file_preview, this);
        ButterKnife.bind(this);
        setBackgroundColor(getResources().getColor(R.color.sab_blue));
    }

    public void setAttachment(Attachment attachment, String parentId) {
        boolean isDownloaded = attachment.isFileDownloaded(getContext(), parentId);
        downloadIcon.setVisibility(isDownloaded ? GONE : VISIBLE);

        String contentType = attachment.getContentType();
        if (contentType != null) {
            int start = contentType.indexOf("/");
            String type = contentType.substring(start + 1, contentType.length());
            if (type.contains(".")) {
                //get the last part of content type
                int lastIndex = type.lastIndexOf('.');
                type = type.substring(lastIndex + 1, type.length());
            }
            fileType.setText(type);
            File file = new File(attachment.getFilePath(getContext(), parentId));
            if (attachment.getContentType().contains("image")) {
                imagePreview.setVisibility(VISIBLE);
                Picasso.with(getContext()).load(file).fit().centerCrop().into(imagePreview);
            } else {
                imagePreview.setVisibility(GONE);
            }
        } else {
            Log.e(TAG, "setAttachment: Null Content Type");
        }
    }

    public ImageView getImagePreview() {
        return imagePreview;
    }
}
