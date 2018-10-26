package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.AccountTasksListAdapter;
import com.abinbev.dsa.model.Task;
import com.abinbev.dsa.ui.presenter.AccountTasksListPresenter;
import com.abinbev.dsa.ui.view.RefreshListener;
import com.abinbev.dsa.ui.view.SortableHeader;
import com.abinbev.dsa.utils.PermissionManager;
import com.salesforce.androidsyncengine.data.model.PicklistValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by lukaszwalukiewicz on 12.01.2016.
 */
public class TasksListActivity extends AppBaseDrawerActivity implements AccountTasksListPresenter.ViewModel, RefreshListener {
    public static final String ARGS_ACCOUNT_ID = "account_id";

    @Nullable
    @Bind({ R.id.subject, R.id.dueDate, R.id.state})
    List<SortableHeader> sortableHeaders;

    AccountTasksListPresenter presenter;
    AccountTasksListAdapter adapter;

    @Bind(R.id.tasksListView)
    ListView listView;

    @Bind(R.id.subjectSpinner)
    Spinner subjectSpinner;

    @Bind(R.id.stateSpinner)
    Spinner stateSpinner;

    @Bind(R.id.newTask)
    FloatingActionButton newTaskButton;

    private String accountId;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_tasks_list_view;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new AccountTasksListAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Task task = adapter.getItem(position);
                launchDynamicTaskViewIntent(task);
            }
        });
        Intent intent = getIntent();
        accountId = intent.getStringExtra(ARGS_ACCOUNT_ID);

        if (!PermissionManager.getInstance().hasPermission(PermissionManager.CREATE_TASKS)) {
            newTaskButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.stop();
    }

    @Override
    public void setTasks(List<Task> tasks) {
        adapter.setData(tasks);
        setupSubjectSpinnerWithTasks(tasks);
        setupStateSpinnerWithTasks(tasks);
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
    public void onRefresh() {
        String accountId = getIntent().getStringExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA);
        if (accountId != null){
            if (presenter == null) {
                presenter = new AccountTasksListPresenter(accountId);
            }
            presenter.setViewModel(this);
            presenter.start();
        }
        getSupportActionBar().setDisplayShowTitleEnabled(false);
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

    @OnClick(R.id.newTask)
    public void onNewTaskClicked(View view) {
        launchDynamicTaskViewIntent(null);
    }

    @Nullable
    @OnClick(R.id.subject)
    public void onSubjectHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortBySubject(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @Nullable
    @OnClick(R.id.dueDate)
    public void onDueDateHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByDueDate(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @Nullable
    @OnClick(R.id.state)
    public void onStateHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByState(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    private void setupSubjectSpinnerWithTasks(List<Task> tasks) {
        PicklistValue allValues = new PicklistValue();
        allValues.setLabel(getString(R.string.allIssues));
        allValues.setValue(null);
        HashMap<String, Task> statesValues = new HashMap<>();
        List<PicklistValue> picklistValues = new ArrayList<PicklistValue>();
        picklistValues.add(0, allValues);
        for (Task task : tasks){
            String subject = task.getSubject();
            if (!statesValues.containsKey(subject)){
                statesValues.put(subject, task);
                PicklistValue pickListValue = new PicklistValue();
                pickListValue.setLabel(task.getTranslatedSubject());
                pickListValue.setValue(subject);
                picklistValues.add(pickListValue);
            }
        }
        final ArrayAdapter<PicklistValue> spinnerAdapter = new ArrayAdapter<PicklistValue>(this,
                R.layout.twoline_spinner_item, android.R.id.text1, picklistValues) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                TextView textView = (TextView) inflater.inflate(R.layout.dropdown_text_item, parent, false);
                PicklistValue picklistValue = getItem(position);
                textView.setText(picklistValue.getLabel());
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    view = inflater.inflate(R.layout.twoline_spinner_item, parent, false);
                }
                PicklistValue picklistValue = getItem(position);
                ((TextView) view.findViewById(android.R.id.text1)).setText(picklistValue.getLabel());
                return view;
            }
        };
        subjectSpinner.setAdapter(spinnerAdapter);
        subjectSpinner.setSelection(Adapter.NO_SELECTION, false);
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PicklistValue item = (PicklistValue) parent.getAdapter().getItem(position);
                adapter.filterBySubject(item.getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupStateSpinnerWithTasks(List<Task> tasks) {
        PicklistValue allValues = new PicklistValue();
        allValues.setLabel(getString(R.string.allStates));
        allValues.setValue(null);
        HashMap<String, Task> statesValues = new HashMap<>();
        List<PicklistValue> picklistValues = new ArrayList<PicklistValue>();
        picklistValues.add(0, allValues);
        for (Task task : tasks){
            String state = task.getState();
            if (!statesValues.containsKey(state)){
                statesValues.put(state, task);
                PicklistValue pickListValue = new PicklistValue();
                pickListValue.setLabel(task.getTranslatedState());
                pickListValue.setValue(state);
                picklistValues.add(pickListValue);
            }
        }
        final ArrayAdapter<PicklistValue> spinnerAdapter = new ArrayAdapter<PicklistValue>(this,
                R.layout.twoline_spinner_item, android.R.id.text1, picklistValues) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                TextView textView = (TextView) inflater.inflate(R.layout.dropdown_text_item, parent, false);
                PicklistValue picklistValue = getItem(position);
                textView.setText(picklistValue.getLabel());
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    view = inflater.inflate(R.layout.twoline_spinner_item, parent, false);
                }
                PicklistValue picklistValue = getItem(position);
                ((TextView) view.findViewById(android.R.id.text1)).setText(picklistValue.getLabel());
                return view;
            }
        };
        stateSpinner.setAdapter(spinnerAdapter);
        stateSpinner.setSelection(Adapter.NO_SELECTION, false);
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PicklistValue item = (PicklistValue) parent.getAdapter().getItem(position);
                adapter.filterByState(item.getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void launchDynamicTaskViewIntent(Task task) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        if (task != null){
            intent.putExtra(TaskDetailActivity.TASK_ID_EXTRA, task.getId());
        }
        intent.putExtra(TaskDetailActivity.ACCOUNT_ID_EXTRA, accountId);
        startActivity(intent);
    }
}
