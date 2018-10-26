package com.abinbev.dsa.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.AttachmentsListActivity;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.service.AttachmentUploadService;
import com.abinbev.dsa.ui.presenter.Account360DetailsPresenter;
import com.abinbev.dsa.ui.view.AttachmentsView;
import com.abinbev.dsa.ui.view.CoverageView;
import com.abinbev.dsa.ui.view.NotesView;
import com.abinbev.dsa.ui.view.PedidosView;
import com.abinbev.dsa.ui.view.PerformanceIndicatorsView;
import com.abinbev.dsa.ui.view.VolumeView;

import butterknife.Bind;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Jakub Stefanowski on 08.03.2017.
 */

public class Account360DetailsFragment extends AppBaseFragment implements AttachmentsView.Callback,
        Account360DetailsPresenter.ViewModel, CoverageView.OnCoverageClickedListener,
        VolumeView.OnVolumeClickedListener {

    public interface OnCoverageClickedListener {
        void onCoverageClicked(KPI__c kpi);
    }

    public interface OnVolumeClickedListener {
        void onVolumeClicked(KPI__c kpi);
    }

    private static final int SELECT_ATTACHMENT_REQUEST_CODE = 333;

    private static final String ARG_ACCOUNT_ID = "account_id";

    @Nullable
    @Bind(R.id.notes_view)
    NotesView notesView;

    @Bind(R.id.performance_indicators_view)
    PerformanceIndicatorsView performanceIndicatorsView;

    @Bind(R.id.orders_view)
    PedidosView pedidosView;

    @Nullable
    @Bind(R.id.coverage_view)
    CoverageView coverageView;

    @Nullable
    @Bind(R.id.kpi_volume_divider)
    View kpiVolumeDivider;

    @Nullable
    @Bind(R.id.kpi_coverage_divider)
    View kpiCoverageDivider;

    @Nullable
    @Bind(R.id.volume_view)
    VolumeView volumeView;

    @Bind(R.id.attachments)
    AttachmentsView attachments;

    OnCoverageClickedListener onCoverageClickedListener;

    OnVolumeClickedListener onVolumeClickedListener;

    Account360DetailsPresenter presenter;

    String accountId;

    public static Account360DetailsFragment newInstance(String accountId) {
        Account360DetailsFragment fragment = new Account360DetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountId = getArguments().getString(ARG_ACCOUNT_ID);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter = new Account360DetailsPresenter(accountId);
        coverageView.setOnCoverageClickedListener(this);
        volumeView.setOnVolumeClickedListener(this);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_account360_details;
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    public void onStop() {
        presenter.stop();
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (data == null) {
                Log.e("Babu", "data is null!");
                return;
            }

            if (SELECT_ATTACHMENT_REQUEST_CODE == requestCode) {
                Intent intent = AttachmentUploadService.uploadAccountAttachment(getActivity(), data.getData(), accountId);
                getActivity().startService(intent);
                return;
            }
        }
    }

    @Override // Account360DetailsPresenter.ViewModel
    public void setKpiVisible(boolean visible) {
        if (visible) {
            if (volumeView != null) volumeView.setVisibility(View.VISIBLE);
            if (coverageView != null) coverageView.setVisibility(View.VISIBLE);
            if (kpiVolumeDivider != null) kpiVolumeDivider.setVisibility(View.VISIBLE);
            if (kpiCoverageDivider != null) kpiCoverageDivider.setVisibility(View.VISIBLE);
        }
        else {
            if (volumeView != null) volumeView.setVisibility(View.GONE);
            if (coverageView != null) coverageView.setVisibility(View.GONE);
            if (kpiVolumeDivider != null) kpiVolumeDivider.setVisibility(View.GONE);
            if (kpiCoverageDivider != null) kpiCoverageDivider.setVisibility(View.GONE);
        }
    }

    @Override // Account360DetailsPresenter.ViewModel
    public void setPedidoVisible(boolean visible) {
        pedidosView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override // Account360DetailsPresenter.ViewModel
    public void refreshKpis() {
        if (coverageView != null) coverageView.setAccountId(accountId);
        if (volumeView != null) volumeView.setAccountId(accountId);
    }

    @Override // Account360DetailsPresenter.ViewModel
    public void refreshNotes() {
        if (notesView != null) notesView.setAccountId(accountId);
    }

    @Override // Account360DetailsPresenter.ViewModel
    public void refreshPerformanceIndicators() {
        performanceIndicatorsView.setAccountId(accountId);
    }

    @Override // Account360DetailsPresenter.ViewModel
    public void refreshAttachments() {
        attachments.setAccountId(accountId, this);
    }

    @Override // Account360DetailsPresenter.ViewModel
    public void refreshPedidos() {
        pedidosView.setAccountId(accountId);
    }

    @Override // Account360DetailsPresenter.ViewModel
    public void showAttachmentDownloadFailed() {
        showToast(R.string.toast_attachment_download_failed, Toast.LENGTH_SHORT);
    }

    @Override // Account360DetailsPresenter.ViewModel
    public void showDownloadStarted() {
        showToast(R.string.toast_downloading_attachment, Toast.LENGTH_SHORT);
    }

    @Override // Account360DetailsPresenter.ViewModel
    public void hideAllViews() {
        setKpiVisible(false);
        setPedidoVisible(false);
        hideView(performanceIndicatorsView);
        hideView(attachments);
        hideView(notesView);
    }

    @Override // AttachmentsView.Callback
    public void onChooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, SELECT_ATTACHMENT_REQUEST_CODE);
    }

    @Override // AttachmentsView.Callback
    public String getAttachmentParentId() {
        return accountId;
    }

    @Override // AttachmentsView.Callback
    public void onShowMoreAttachment() {
        Intent intent = new Intent(getActivity(), AttachmentsListActivity.class);
        intent.putExtra(AttachmentsListActivity.ARGS_ACCOUNT_ID, accountId);
        startActivity(intent);
    }

    @Override // AttachmentsView.Callback
    public void onViewAttachment(final Attachment attachment) {
        presenter.viewAttachment(attachment);
    }

    @Override // CoverageView.OnCoverageClickedListener
    public void onCoverageClicked(KPI__c kpi) {
        if (onCoverageClickedListener != null) {
            onCoverageClickedListener.onCoverageClicked(kpi);
        }
    }

    @Override // VolumeView.OnVolumeClickedListener
    public void onVolumeClicked(KPI__c kpi) {
        if (onVolumeClickedListener != null) {
            onVolumeClickedListener.onVolumeClicked(kpi);
        }
    }

    private void hideView(View v) {
        if (v != null) v.setVisibility(View.GONE);
    }

    private void showToast(int stringRes, int length) {
        Toast.makeText(getActivity(), stringRes, length).show();
    }

    public void onRefresh() {
        presenter.start();
    }

    public void setOnCoverageClickedListener(OnCoverageClickedListener l) {
        this.onCoverageClickedListener = l;
    }

    public void setOnVolumeClickedListener(OnVolumeClickedListener onVolumeClickedListener) {
        this.onVolumeClickedListener = onVolumeClickedListener;
    }
}
