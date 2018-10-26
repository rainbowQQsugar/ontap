package com.abinbev.dsa.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.ui.customviews.ExpandedListView;
import com.abinbev.dsa.ui.presenter.CoverageDetailsPresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Jakub Stefanowski on 08.03.2017.
 */

public class CoverageDetailsFragment extends AppBaseFragment implements CoverageDetailsPresenter.ViewModel {

    private static final String ARG_COVERAGE_ID = "coverage_id";

    private static final String ARG_ACCOUNT_ID = "account_id";

    private static final String STATE_IS_LIST_EXPANDED = "is_list_expanded";

    private static final String STATE_SHOW_CLOSE_BUTTON = "show_close_button";

    public interface OnCoverageCloseClickedListener {
        void onCoverageCloseClicked();
    }

    public interface ViewModel {
        void setHeaderTitle(String title);
        void setHeaderSubtitle(String subtitle);
    }


    @Bind(R.id.coverage_details_title)
    TextView coverageTitle;

    @Bind(R.id.coverage_list)
    ExpandedListView coverageList;

    @Nullable
    @Bind(R.id.close_button)
    ImageButton closeButton;

    OnCoverageCloseClickedListener onCloseClickedListener;

    CoverageDetailsPresenter presenter;

    CoverageAdapter coverageAdapter;

    ViewModel fragmentViewModel;

    String accountId;

    String coverageId;

    boolean isCoverageListExpanded = false;

    boolean showCloseButton = false;

    public static CoverageDetailsFragment newInstance(String coverageId, String accountId) {
        CoverageDetailsFragment fragment = new CoverageDetailsFragment();

        Bundle args = new Bundle();
        args.putString(ARG_ACCOUNT_ID, accountId);
        args.putString(ARG_COVERAGE_ID, coverageId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        accountId = getArguments().getString(ARG_ACCOUNT_ID);
        coverageId = getArguments().getString(ARG_COVERAGE_ID);

        if (savedInstanceState != null) {
            isCoverageListExpanded = savedInstanceState.getBoolean(STATE_IS_LIST_EXPANDED, false);
            showCloseButton = savedInstanceState.getBoolean(STATE_SHOW_CLOSE_BUTTON, false);
        }

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        coverageList.setExpanded(isCoverageListExpanded);
        if (closeButton != null) {
            closeButton.setVisibility(showCloseButton ? View.VISIBLE : View.GONE);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        coverageAdapter = new CoverageAdapter();
        coverageList.setAdapter(coverageAdapter);

        presenter = new CoverageDetailsPresenter(coverageId, accountId);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_IS_LIST_EXPANDED, isCoverageListExpanded);
        outState.putBoolean(STATE_SHOW_CLOSE_BUTTON, showCloseButton);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_coverage_details;
    }

    @Override
    public void setKpi(KPI__c kpi) {
        String title = getString(R.string.coverage) + " – " + kpi.getKpiName();
        coverageTitle.setText(title);

        if (fragmentViewModel != null) {
            fragmentViewModel.setHeaderTitle(title);
        }
    }

    @Override
    public void setDetails(List<KPI__c> detailKpis) {
        coverageAdapter.setData(detailKpis);
        coverageAdapter.notifyDataSetChanged();
    }

    @Override
    public void setAccount(Account account) {
        if (fragmentViewModel != null) {
            fragmentViewModel.setHeaderSubtitle(account.getName());
        }
    }

    @Nullable
    @OnClick(R.id.close_button)
    public void onCloseClicked() {
        if (onCloseClickedListener != null) {
            onCloseClickedListener.onCoverageCloseClicked();
        }
    }

    public void setFragmentViewModel(ViewModel fragmentViewModel) {
        this.fragmentViewModel = fragmentViewModel;
    }

    public void setCoverageListExpanded(boolean coverageListExpanded) {
        isCoverageListExpanded = coverageListExpanded;
        if (coverageList != null) {
            coverageList.setExpanded(isCoverageListExpanded);
        }
    }

    public void setShowCloseButton(boolean showCloseButton) {
        this.showCloseButton = showCloseButton;
        if (closeButton != null) {
            closeButton.setVisibility(showCloseButton ? View.VISIBLE : View.GONE);
        }
    }

    public void setOnCloseClickedListener(OnCoverageCloseClickedListener l) {
        this.onCloseClickedListener = l;
    }

    static class CoverageAdapter extends BaseAdapter {

        List<KPI__c> coverageList = new ArrayList<>();

        public void setData(List<KPI__c> coverages) {
            coverageList.clear();
            if (coverages != null) {
                coverageList.addAll(coverages);
            }
        }

        @Override
        public int getCount() {
            return coverageList.size();
        }

        @Override
        public KPI__c getItem(int position) {
            return coverageList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CoverageVH vh;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.coverage_details_list_item, parent, false);
                vh = new CoverageVH(convertView);
                convertView.setTag(vh);
            }
            else {
                vh = (CoverageVH) convertView.getTag();
            }

            Resources resources = parent.getResources();
            KPI__c kpi = getItem(position);
            boolean isDone = kpi.getActual() > 0.9;

            vh.title.setText(kpi.getKpiName());
            vh.status.setText(isDone ? R.string.si : R.string.no);
            vh.status.setTextColor(resources
                    .getColor(isDone ? R.color.status_yes : R.color.status_no));

            return convertView;
        }
    }

    static class CoverageVH {

        @Bind(R.id.coverage_item_title)
        TextView title;

        @Bind(R.id.coverage_item_status)
        TextView status;

        public CoverageVH(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
