package com.salesforce.dsa.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.CN_DSA_Azure_File__c;
import com.salesforce.dsa.app.utils.ContentUtils;
import com.salesforce.dsa.data.model.CN_DSA_Folder__c;

import java.io.File;
import java.util.List;

public class FilesListAdapter extends RecyclerView.Adapter<FilesListAdapter.Viewholer> {

    private final Context context;
    private List dataList;
    private OnItemClickListener onItemClickListener;

    public FilesListAdapter(List dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
    }

    @Override
    public Viewholer onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Viewholer(LayoutInflater.from(context).inflate(R.layout.category_item, parent, false));
    }

    public void setDataList(List dataList) {
        this.dataList = dataList;
    }

    @Override
    public void onBindViewHolder(FilesListAdapter.Viewholer holder, int position) {
        Object data = dataList.get(position);
        if (data instanceof CN_DSA_Azure_File__c) {
            CN_DSA_Azure_File__c cn_DSA_azure_filec = (CN_DSA_Azure_File__c) data;
            String fileName = cn_DSA_azure_filec.getId() + "_" + cn_DSA_azure_filec.getName();
            String completeFileName = fileName + ContentUtils.fileNameSuffix(cn_DSA_azure_filec.getDsaFileTpye());
            holder.fileIcon.setImageResource(ContentUtils.getDrawableResource(cn_DSA_azure_filec.getDsaFileTpye()));
            holder.name.setText(cn_DSA_azure_filec.getDsaFileName());
            File file = new File(context.getExternalFilesDir(null), completeFileName);
            holder.decription.setText(file.exists()?context.getString(R.string.downloaded_file):context.getString(R.string.undownloaded_file));
        } else if (data instanceof CN_DSA_Folder__c) {
            CN_DSA_Folder__c category = (CN_DSA_Folder__c) data;
            holder.fileIcon.setImageResource(R.drawable.folder_icon);
            holder.name.setText(category.getName());
        }
        holder.goNext.setVisibility(data instanceof CN_DSA_Folder__c ? View.VISIBLE : View.GONE);
        holder.decription.setVisibility(data instanceof CN_DSA_Folder__c ? View.GONE : View.VISIBLE);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null)
                    onItemClickListener.onItemClickListener(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (dataList == null)
            return 0;
        return dataList.size();
    }

    public Object getItem(int position) {
        if (dataList != null)
            return dataList.get(position);
        return null;
    }

    public class Viewholer extends RecyclerView.ViewHolder {
        ImageView goNext, fileIcon;
        TextView name, decription;
        View view;

        public Viewholer(View itemView) {
            super(itemView);
            fileIcon = (ImageView) itemView.findViewById(R.id.file_icon);
            name = (TextView) itemView.findViewById(R.id.file_name);
            decription = (TextView) itemView.findViewById(R.id.description);
            goNext = (ImageView) itemView.findViewById(R.id.go_next);
            view = itemView;
        }
    }

    public interface OnItemClickListener {
        void onItemClickListener(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
