package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.SurveysListAdapter;
import com.abinbev.dsa.adapter.UserSurveysListAdapter;
import com.abinbev.dsa.model.SurveyTaker__c;
import com.abinbev.dsa.model.Survey__c;
import com.abinbev.dsa.ui.presenter.AccountSurveysListPresenter;
import com.abinbev.dsa.ui.presenter.UserSurveysListPresenter;
import com.abinbev.dsa.ui.view.SortableHeader;
import com.abinbev.dsa.utils.PermissionManager;
import com.salesforce.androidsyncengine.datamanager.SyncEngine;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class UserSurveysListActivity extends AppBaseDrawerActivity implements UserSurveysListPresenter.ViewModel{
    public static final String USER_ID_EXTRA = "user_id";

    @Bind(R.id.survey_list)
    ListView surveyListView;

    @Nullable
    @Bind({ R.id.survey_number, R.id.survey_account, R.id.survey_state, R.id.survey_creation_date, R.id.survey_due_date})
    List<SortableHeader> sortableHeaders;

    private UserSurveysListAdapter adapter;
    private UserSurveysListPresenter presenter;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("");

        adapter = new UserSurveysListAdapter();
        surveyListView.setAdapter(adapter);

        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra(USER_ID_EXTRA);
        }

        surveyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (syncInProgress) {
                    Toast.makeText(UserSurveysListActivity.this, R.string.survey_sync_in_progress, Toast.LENGTH_LONG).show();
                    return;
                }
                SurveyTaker__c surveyTaker = (SurveyTaker__c)adapter.getItem(position);
                presenter.launchSurveyWebViewIntent(UserSurveysListActivity.this, surveyTaker);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.stop();
    }

    @Override
    public void onRefresh() {
        if (presenter == null) {
            presenter = new UserSurveysListPresenter(userId);
        }

        if (SyncEngine.getSyncStatus().getStatus() == SyncStatus.SyncStatusState.INPROGRESS) {
            syncInProgress = true;
        } else {
            syncInProgress = false;
        }

        presenter.setViewModel(this);
        presenter.start();
    }


    @Override
    public int getLayoutResId() {
        return R.layout.activity_user_survey_list;
    }

    @Override
    public void setData(List<SurveyTaker__c> surveys){
        adapter.setData(surveys);
    }

    @OnClick(R.id.survey_number)
    @Nullable
    public void onSurveyNumberHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByOrderNumber(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @OnClick(R.id.survey_state)
    @Nullable
    public void onSurveyStateHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByState(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @OnClick(R.id.survey_creation_date)
    @Nullable
    public void onSurveyCreateDateHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByCreateDate(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @OnClick(R.id.survey_due_date)
    @Nullable
    public void onSurveyDueDateHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByDueDate(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @OnClick(R.id.survey_account)
    @Nullable
    public void onSurveyAccountHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByAccount(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    /**
     * Clears the sort order on all headers in the group EXCEPT for the id of the view passed in
     * @param sortableHeaderId - the id to NOT clear the sort on
     */
    private void clearSortOnAllOthers(int sortableHeaderId) {
        for (SortableHeader sortableHeader : sortableHeaders) {
            if (sortableHeader.getId() != sortableHeaderId) {
                sortableHeader.clearSortIndicator();
            }
        }
    }

    @Override
    public void setSurveyTypes(List<Survey__c> recordTypes) { }
}
