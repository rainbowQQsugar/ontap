package com.abinbev.dsa.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.ui.presenter.PictureAuditPresenter.RejectedFile;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PictureAuditAdapter extends RecyclerView.Adapter<PictureAuditAdapter.DataObjectHolder> {

    public interface OnRejectedFileClickedListener {
        void onRejectedFileClicked(RejectedFile rejectedFile);
    }

    private List<RejectedFile> rejectedFiles = new ArrayList<>();

    private OnRejectedFileClickedListener onClickListener;

    public static class DataObjectHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.picture_audit_case_name)
        TextView caseName;

        @Bind(R.id.picture_audit_case_name_container)
        View caseNameContainer;

        @Bind(R.id.picture_audit_account_asset_name)
        TextView assetName;

        @Bind(R.id.picture_audit_account_asset_name_container)
        View assetNameContainer;

        @Bind(R.id.picture_audit_survey_name)
        TextView surveyName;

        @Bind(R.id.picture_audit_survey_name_container)
        View surveyNameContainer;

        @Bind(R.id.picture_audit_survey_question)
        TextView surveyQuestion;

        @Bind(R.id.picture_audit_survey_question_container)
        View surveyQuestionContainer;

        @Bind(R.id.picture_audit_account_name)
        TextView accountName;

        @Bind(R.id.picture_audit_account_name_container)
        View accountNameContainer;

        @Bind(R.id.picture_audit_event_name)
        TextView eventName;

        @Bind(R.id.picture_audit_event_name_container)
        View eventNameContainer;

        @Bind(R.id.picture_audit_type)
        TextView typeName;

        @Bind(R.id.picture_audit_type_container)
        View typeNameContainer;

        @Bind(R.id.picture_audit_file_name)
        TextView fileName;

        @Bind(R.id.picture_audit_file_name_container)
        View fileNameContainer;

        @Bind(R.id.picture_audit_pic_audit_status_id)
        TextView auditStatusId;

        @Bind(R.id.picture_audit_pic_audit_status_id_container)
        View auditStatusIdContainer;

        final OnRejectedFileClickedListener onClickListener;

        RejectedFile currentRejectedFile;

        public DataObjectHolder(View itemView, OnRejectedFileClickedListener l) {
            super(itemView);
            onClickListener = l;
            ButterKnife.bind(this, itemView);
        }

        public void setRejectedFile(RejectedFile rejectedFile) {
            currentRejectedFile = rejectedFile;
            setText(caseNameContainer, caseName, rejectedFile.caseName);
            setText(assetNameContainer, assetName, rejectedFile.accountAssetName);
            setText(surveyNameContainer, surveyName, rejectedFile.surveyName);
            setText(surveyQuestionContainer, surveyQuestion, rejectedFile.surveyQuestion);
            setText(accountNameContainer, accountName, rejectedFile.accountName);
            setText(eventNameContainer, eventName, rejectedFile.eventName);
            setText(typeNameContainer, typeName, rejectedFile.type);
            setText(fileNameContainer, fileName, rejectedFile.fileName);
            setText(auditStatusIdContainer, auditStatusId, rejectedFile.pictureAuditStatusId);
        }

        private void setText(View container, TextView textView, String text) {
            if (TextUtils.isEmpty(text)) {
                container.setVisibility(View.GONE);
            }
            else {
                container.setVisibility(View.VISIBLE);
                textView.setText(text);
            }
        }

        @OnClick(R.id.picture_audit_card)
        public void onItemClicked() {
            if (onClickListener != null) {
                onClickListener.onRejectedFileClicked(currentRejectedFile);
            }
        }
    }

    public void setOnClickListener(OnRejectedFileClickedListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setData(List<RejectedFile> rejectedFiles) {
        this.rejectedFiles.clear();
        this.rejectedFiles.addAll(rejectedFiles);
        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        holder.setRejectedFile(rejectedFiles.get(position));
    }

    @Override
    public int getItemCount() {
        return rejectedFiles.size();
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_picture_audit, parent, false);

        return new DataObjectHolder(view, onClickListener);
    }
}
