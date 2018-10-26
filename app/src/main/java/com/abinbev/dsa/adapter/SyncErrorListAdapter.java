package com.abinbev.dsa.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.model.ErrorObject;
import com.salesforce.androidsyncengine.datamanager.model.QueueObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SyncErrorListAdapter extends BaseAdapter {

    List<ErrorObject> syncErrors;

    public SyncErrorListAdapter() {
        super();
        syncErrors = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return syncErrors.size();
    }

    @Override
    public Object getItem(int position) {
        return syncErrors.get(getCount() - position - 1);
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
            convertView = inflater.inflate(R.layout.sync_errors_item_view, parent, false);

            convertView.setTag(new ViewHolder(convertView));
        }

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        // show the most recent errors at top
        final ErrorObject errorObject = syncErrors.get(getCount() - position - 1);

        viewHolder.objectType.setText(errorObject.getObjectType());
        viewHolder.objectId.setText(errorObject.getId());
        viewHolder.errorCode.setText(errorObject.getErrorCode());
        viewHolder.errorMessage.setText(errorObject.getErrorMessage());

        viewHolder.jsonData.setText(R.string.dash);

        if (errorObject.getFieldsJson() != null) {
            viewHolder.jsonData.setText(errorObject.getFieldsJson());
        }

        return convertView;
    }

    public void setData(List<ErrorObject> syncErrors) {
        this.syncErrors.clear();
        this.syncErrors.addAll(syncErrors);
        this.notifyDataSetChanged();
    }

    class ViewHolder {

        @Bind(R.id.object_type)
        TextView objectType;

        @Bind(R.id.object_id)
        TextView objectId;

        @Bind(R.id.error_code)
        TextView errorCode;

        @Bind(R.id.error_message)
        TextView errorMessage;

        @Bind(R.id.json_data)
        TextView jsonData;

        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }


    }
}
