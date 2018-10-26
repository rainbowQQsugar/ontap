package com.abinbev.dsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.salesforce.androidsyncengine.datamanager.model.QueueObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SyncPendingListAdapter extends BaseAdapter {

    List<QueueObject> queueObjectList;

    public SyncPendingListAdapter() {
        super();
        queueObjectList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return queueObjectList.size();
    }

    @Override
    public Object getItem(int position) {
        return queueObjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.pending_list_item_view, parent, false);

            convertView.setTag(new ViewHolder(convertView));
        }

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        final QueueObject queueObject = queueObjectList.get(position);

        if (queueObject.getRetryCount() > 0) {
            viewHolder.pending.setText(R.string.pending_error);
            int redColor = convertView.getContext().getResources().getColor(R.color.red);
            viewHolder.pending.setTextColor(redColor);
        } else {
            viewHolder.pending.setText(R.string.pending);
            int greenColor = convertView.getContext().getResources().getColor(R.color.green);
            viewHolder.pending.setTextColor(greenColor);
        }

        viewHolder.objectType.setText(queueObject.getObjectType());
        viewHolder.objectId.setText(queueObject.getId());
        viewHolder.pendingDescripton.setText(String.format(parent.getContext().getString(R.string.pending_description), queueObject.getOperation(), queueObject.getObjectType(), queueObject.getId()));

        if (queueObject.getFieldsJson() != null) {
                viewHolder.jsonData.setText(queueObject.getFieldsJson().toString());
        } else {
            viewHolder.jsonData.setText(R.string.no_data);
        }

        return convertView;
    }

    public void setData(List<QueueObject> queueObjectList) {
        this.queueObjectList.clear();
        this.queueObjectList.addAll(queueObjectList);
        this.notifyDataSetChanged();
    }

    class ViewHolder {

        @Bind(R.id.pending)
        TextView pending;

        @Bind(R.id.object_type)
        TextView objectType;

        @Bind(R.id.object_id)
        TextView objectId;

        @Bind(R.id.pending_description)
        TextView pendingDescripton;

        @Bind(R.id.json_data)
        TextView jsonData;

        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }


    }
}
