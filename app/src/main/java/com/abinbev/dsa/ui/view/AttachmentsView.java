package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.ui.customviews.ExpandedListView;
import com.abinbev.dsa.ui.presenter.AttachmentPresenter;
import com.abinbev.dsa.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wandersonblough on 12/2/15.
 */
public class AttachmentsView extends RelativeLayout implements AttachmentPresenter.ViewModel, RefreshListener {

    @Bind(R.id.no_content)
    TextView noContent;

    @Bind(R.id.previews_list)
    RecyclerView previewsList;

    @Bind(R.id.files_list)
    ExpandedListView filesList;

    @Bind(R.id.lists_spacing)
    View listsSpacing;

    PreviewAdapter previewAdapter;

    FilesAdapter filesAdapter;

    AttachmentPresenter attachmentPresenter;
    Callback callback;

    String attachmentParentId;

    PreviewAdapterCallback previewAdapterCallback = new PreviewAdapterCallback() {
        @Override
        public void onPreviewClicked(Attachment attachment) {
            if (callback != null) callback.onViewAttachment(attachment);
        }
    };

    AdapterView.OnItemClickListener onFileClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (callback != null) {
                Attachment attachment = (Attachment) parent.getItemAtPosition(position);
                callback.onViewAttachment(attachment);
            }
        }
    };

    @Override
    public void onRefresh() {
        if (attachmentPresenter != null) {
            attachmentPresenter.start();
        }
    }

    public interface Callback {
        void onViewAttachment(Attachment attachment);
        void onChooseFile();
        String getAttachmentParentId();
        void onShowMoreAttachment();
    }

    public AttachmentsView(Context context) {
        this(context, null);
    }

    public AttachmentsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AttachmentsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.merge_attachments, this);
        ButterKnife.bind(this);

        previewAdapter = new PreviewAdapter(context, previewAdapterCallback);
        previewsList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        previewsList.setAdapter(previewAdapter);

        filesAdapter = new FilesAdapter(context);
        filesList.setAdapter(filesAdapter);
        filesList.setOnItemClickListener(onFileClickListener);
        filesList.setExpanded(true);
    }

    public void setAccountId(String accountId, Callback callback) {
        this.callback = callback;
        if (attachmentPresenter == null) {
            attachmentPresenter = new AttachmentPresenter(accountId);
        } else {
            attachmentPresenter.setAccountId(accountId);
        }
        attachmentPresenter.setViewModel(this);
        attachmentPresenter.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (attachmentPresenter != null)
            attachmentPresenter.stop();
        callback = null;
    }

    @Override
    public void setAttachments(final List<Attachment> attachments) {
        List<Attachment> imageAttachments = new ArrayList<>();
        List<Attachment> otherAttachments = new ArrayList<>();

        for (Attachment attachment : attachments) {
            if (isImage(attachment)) {
                imageAttachments.add(attachment);
            }
            else {
                otherAttachments.add(attachment);
            }
        }

        attachmentParentId = callback.getAttachmentParentId();

        if (imageAttachments.isEmpty()) {
            previewsList.setVisibility(GONE);
        }
        else {
            previewsList.setVisibility(VISIBLE);
            previewAdapter.setData(attachmentParentId, imageAttachments);
            previewAdapter.notifyDataSetChanged();
        }

        if (otherAttachments.isEmpty()) {
            filesList.setVisibility(GONE);
        }
        else {
            filesList.setVisibility(VISIBLE);
            filesAdapter.setData(attachmentParentId, otherAttachments);
            filesAdapter.notifyDataSetChanged();
        }

        if (otherAttachments.isEmpty() && imageAttachments.isEmpty()) {
            noContent.setVisibility(VISIBLE);
        }
        else {
            noContent.setVisibility(GONE);
        }

        if (!otherAttachments.isEmpty() && !imageAttachments.isEmpty()) {
            listsSpacing.setVisibility(VISIBLE);
        }
        else {
            listsSpacing.setVisibility(GONE);
        }
    }

    private static boolean isImage(Attachment attachment) {
        if (attachment == null) return false;

        String contentType = attachment.getContentType();
        return contentType != null && contentType.contains("image");
    }

    private static boolean isPdf(Attachment attachment) {
        if (attachment == null) return false;

        String contentType = attachment.getContentType();
        return contentType != null && "application/pdf".equalsIgnoreCase(contentType);
    }

    @OnClick(R.id.add)
    public void add() {
        if (callback != null) {
            callback.onChooseFile();
        }
    }

    @OnClick(R.id.more)
    public void more() {
        if (callback != null) {
            callback.onShowMoreAttachment();
        }
    }

    private static class FilesAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        private List<Attachment> attachments = Collections.emptyList();

        private String attachmentParentId;

        FilesAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return attachments.size();
        }

        @Override
        public Attachment getItem(int position) {
            return attachments.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FilesAdapterViewHolder vh;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.file_item, parent, false);
                vh = new FilesAdapterViewHolder();
                ButterKnife.bind(vh, convertView);
                convertView.setTag(vh);
            }
            else {
                vh = (FilesAdapterViewHolder) convertView.getTag();
            }

            Attachment attachment = attachments.get(position);
            Context context = parent.getContext();

            vh.attachmentIcon.setImageResource(isPdf(attachment) ? R.drawable.ic_attachment_pdf :
                R.drawable.ic_attachment);
            vh.fileName.setText(attachment.getName());

            String prefix = convertView.getResources().getString(R.string.modified).toUpperCase() + " ";
            vh.modificationDate.setText(prefix + DateUtils.formatDateStringShort(attachment.getLastModifiedDate()));

            boolean isDownloaded = attachment.isFileDownloaded(context, attachmentParentId);
            vh.downloadIcon.setVisibility(isDownloaded ? INVISIBLE : VISIBLE);

            return convertView;
        }

        public void setData(String attachmentParentId, List<Attachment> attachments) {
            this.attachments = attachments;
            this.attachmentParentId = attachmentParentId;
        }
    }

    static class FilesAdapterViewHolder {
        @Bind(R.id.attachment_icon)
        ImageView attachmentIcon;

        @Bind(R.id.download_icon)
        ImageView downloadIcon;

        @Bind(R.id.file_name)
        TextView fileName;

        @Bind(R.id.modification_date)
        TextView modificationDate;
    }

    private static class PreviewAdapter extends RecyclerView.Adapter<PreviewViewHolder> {

        private LayoutInflater inflater;

        private List<Attachment> attachments = Collections.emptyList();

        private String attachmentParentId;

        private PreviewAdapterCallback callback;

        private int smallMargin;

        private int bigMargin;

        PreviewAdapter(Context context, PreviewAdapterCallback callback) {
            this.inflater = LayoutInflater.from(context);
            this.callback = callback;

            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            this.smallMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, dm);
            this.bigMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, dm);
        }

        @Override
        public PreviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View previewView = inflater.inflate(R.layout.file_preview_item, parent, false);
            return new PreviewViewHolder(previewView);
        }

        @Override
        public void onBindViewHolder(PreviewViewHolder holder, int position) {
            Attachment attachment = attachments.get(position);
            holder.filePreview.setAttachment(attachment, attachmentParentId);

            MarginLayoutParams layoutParams = (MarginLayoutParams) holder.filePreview.getLayoutParams();
            layoutParams.setMarginStart(position == 0 ? bigMargin : smallMargin);
            layoutParams.setMarginEnd(position == (getItemCount() - 1) ? bigMargin : smallMargin);
            holder.filePreview.setLayoutParams(layoutParams);
            holder.attachment = attachment;
            holder.callback = callback;
        }

        @Override
        public int getItemCount() {
            return attachments.size();
        }

        public void setData(String attachmentParentId, List<Attachment> attachments) {
            this.attachmentParentId = attachmentParentId;
            this.attachments = attachments;
        }
    }

    interface PreviewAdapterCallback {
        void onPreviewClicked(Attachment attachment);
    }

    static class PreviewViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.file_preview)
        FilePreview filePreview;

        Attachment attachment;

        PreviewAdapterCallback callback;

        PreviewViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.file_preview)
        void onPreviewClicked() {
            callback.onPreviewClicked(attachment);
        }
    }
}
