package com.abinbev.dsa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.utils.ContentUtils;
import com.abinbev.dsa.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by lukaszwalukiewicz on 18.01.2016.
 */
public class AttachmentsListAdapter extends BaseAdapter {
    private final List<Attachment> attachments;
    private Context context;

    public AttachmentsListAdapter(Context context) {
        super();
        this.context = context;
        this.attachments = new ArrayList<>();
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
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.attachment_list_entry_view, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }

        final Attachment attachment = attachments.get(position);
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.attachmentName.setText(attachment.getName());
        viewHolder.attachmentLastModifiedDate.setText(DateUtils.formatDateTimeAMPM(attachment.getLastModifiedDate()));
        return convertView;
    }

    public void setData(List<Attachment> attachments) {
        this.attachments.clear();
        this.attachments.addAll(attachments);
        this.notifyDataSetChanged();
    }

    public void sortByName(final boolean ascending) {
        Collections.sort(attachments, new Comparator<Attachment>() {
            @Override
            public int compare(Attachment lhs, Attachment rhs) {
                if (ascending) {
                    return lhs.getName().compareTo(rhs.getName());
                } else {
                    return rhs.getName().compareTo(lhs.getName());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByLastModifiedDate(final boolean ascending) {
        Collections.sort(attachments, new Comparator<Attachment>() {
            @Override
            public int compare(Attachment lhs, Attachment rhs) {
                return compareDates(lhs.getLastModifiedDate(), rhs.getLastModifiedDate(), ascending);
            }
        });
        this.notifyDataSetChanged();
    }

    private static int compareDates(String lhs, String rhs, boolean ascending) {
        int result;

        if (!ContentUtils.isStringValid(lhs) && !ContentUtils.isStringValid(rhs)) {
            result = 0;
        } else if (!ContentUtils.isStringValid(lhs)) {
            result = -1;
        } else if (!ContentUtils.isStringValid(rhs)) {
            result = 1;
        } else {
            result = DateUtils.dateFromDateTimeString(lhs).compareTo(DateUtils.dateFromDateTimeString(rhs));
        }

        return ascending ? result : result * -1;
    }

    class ViewHolder {

        @Bind(R.id.attachment_name)
        TextView attachmentName;

        @Bind(R.id.attachment_last_modified_date)
        TextView attachmentLastModifiedDate;


        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }

    }
}
