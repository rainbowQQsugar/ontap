package com.abinbev.dsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Attachment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PocAttachmentsAdapter extends BaseAdapter {
    private final List<Attachment> attachments;

    private String accountId;

    public interface AttachmentClickHandler {
        void onAttachmentRemove(Attachment attachment);
    }


    private AttachmentClickHandler attachmentClickHandler;

    public PocAttachmentsAdapter(String accountId) {
        super();
        this.attachments = new ArrayList<>();
        this.accountId = accountId;
    }

    public void setAttachmentClickHandler(AttachmentClickHandler attachmentClickHandler) {
        this.attachmentClickHandler = attachmentClickHandler;
    }

    @Override
    public int getCount() {
        return attachments.size();
    }

    @Override
    public Object getItem(int position) {
        return attachments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.poc_attachment_item, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }

        final Attachment attachment = attachments.get(position);
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.attachmentName.setText(attachment.getName());
        setAttachment(viewHolder.image, attachment);
        viewHolder.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachmentClickHandler.onAttachmentRemove(attachment);
            }
        });
        return convertView;
    }

    public void setData(List<Attachment> attachments) {
        this.attachments.clear();
        this.attachments.addAll(attachments);
        this.notifyDataSetChanged();
    }

    public void setAttachment(ImageView imagePreview, Attachment attachment) {

        String contentType = attachment.getContentType();
        if (contentType != null) {
            int start = contentType.indexOf("/");
            String type = contentType.substring(start + 1, contentType.length());
            if (type.contains(".")) {
                int lastIndex = type.lastIndexOf('.');
                type = type.substring(lastIndex + 1, type.length());
            }
            File file = new File(attachment.getFilePath(imagePreview.getContext(), accountId));
            if (attachment.getContentType().contains("image")) {
                Picasso.with(imagePreview.getContext()).load(file).fit().centerCrop().into(imagePreview);
            } else {
                imagePreview.setImageResource(R.drawable.ic_help_black); //TODO: change this to a better image
            }
        }
    }

    class ViewHolder {

        @Bind(R.id.attachment_name)
        TextView attachmentName;

        @Bind(R.id.image_preview)
        ImageView image;

        @Bind(R.id.close)
        ImageView close;

        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }

    }
}
