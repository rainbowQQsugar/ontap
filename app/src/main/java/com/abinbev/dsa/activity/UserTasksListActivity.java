package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.UserTasksListAdapter;
import com.abinbev.dsa.model.Task;
import com.abinbev.dsa.ui.presenter.UserTasksListPresenter;
import com.abinbev.dsa.ui.view.RefreshListener;
import com.abinbev.dsa.ui.view.SortableHeader;
import com.salesforce.androidsyncengine.data.model.PicklistValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class UserTasksListActivity extends AppBaseDrawerActivity implements UserTasksListPresenter.ViewModel, RefreshListener {
    public static final String ARGS_USER_ID = "user_id";

    @Nullable
    @Bind({R.id.subject, R.id.dueDate, R.id.state, R.id.account})
    List<SortableHeader> sortableHeaders;

    UserTasksListPresenter presenter;
    UserTasksListAdapter adapter;


    @Bind(R.id.tasksListView)
    ListView listView;

    @Bind(R.id.subjectSpinner)
    Spinner subjectSpinner;

    @Bind(R.id.stateSpinner)
    Spinner stateSpinner;

    private String userId;
    private String mCurrentStatus;
    private String mCurrentSubject;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_user_tasks_list_view;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new UserTasksListAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Task task = adapter.getItem(position);
                launchDynamicTaskViewIntent(task);
            }
        });
        Intent intent = getIntent();
        userId = intent.getStringExtra(ARGS_USER_ID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.stop();
    }

    @Override
    public void setTasks(List<Task> tasks) {
        adapter.setData(tasks);
        setupSubjectSpinnerWithTasks(tasks, mCurrentSubject);
        setupStateSpinnerWithTasks(tasks, mCurrentStatus);
        if (tasks.size() > 20) {
            adapter.filterExceptCompletedTasks();
        }
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
        if (presenter == null) {
            presenter = new UserTasksListPresenter(userId);
        }
        presenter.setViewModel(this);
        presenter.start();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    /**
     * Clears the sort order on all headers in the group EXCEPT for the id of the view passed in
     *
     * @param sortableHeaderId - the id to NOT clear the sort on
     */
    private void clearSortOnAllOthers(int sortableHeaderId) {
        for (SortableHeader sortableHeader : sortableHeaders) {
            if (sortableHeader.getId() != sortableHeaderId) {
                sortableHeader.clearSortIndicator();
            }
        }
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

    @Nullable
    @OnClick(R.id.account)
    public void onAccountHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByAccount(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    private void setupSubjectSpinnerWithTasks(List<Task> tasks, String currentSubject) {
        int selectPosition = 0;
        PicklistValue allValues = new PicklistValue();
        allValues.setLabel(getString(R.string.allIssues));
        allValues.setValue(null);
        HashMap<String, Task> statesValues = new HashMap<>();
        List<PicklistValue> picklistValues = new ArrayList<PicklistValue>();
        picklistValues.add(0, allValues);
        int i = 0;
        for (Task task : tasks) {
            String subject = task.getSubject();
            if (!statesValues.containsKey(subject)) {
                i++;
                statesValues.put(subject, task);
                PicklistValue pickListValue = new PicklistValue();
                pickListValue.setLabel(task.getTranslatedSubject());
                pickListValue.setValue(subject);
                picklistValues.add(pickListValue);
                if (currentSubject != null && currentSubject.equals(subject)) {
                    selectPosition = i;
                }
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
        subjectSpinner.setSelection(selectPosition, true);
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PicklistValue item = (PicklistValue) parent.getAdapter().getItem(position);
                mCurrentSubject = item.getValue();
                adapter.filterBySubject(item.getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupStateSpinnerWithTasks(List<Task> tasks, String currentStatus) {
        int selectPosition = 0;
        PicklistValue allValues = new PicklistValue();
        allValues.setLabel(getString(R.string.allStates));
        allValues.setValue(null);
        HashMap<String, Task> statesValues = new HashMap<>();
        List<PicklistValue> picklistValues = new ArrayList<PicklistValue>();
        picklistValues.add(0, allValues);
        int i = 0;
        for (Task task : tasks) {
            String state = task.getState();
            if (!statesValues.containsKey(state)) {
                i++;
                statesValues.put(state, task);
                PicklistValue pickListValue = new PicklistValue();
                pickListValue.setLabel(task.getTranslatedState());
                pickListValue.setValue(state);
                picklistValues.add(pickListValue);
                if (currentStatus != null && currentStatus.equals(state)) {
                    selectPosition = i;
                }
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
        stateSpinner.setSelection(selectPosition, true);
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PicklistValue item = (PicklistValue) parent.getAdapter().getItem(position);
                mCurrentStatus = item.getValue();
                adapter.filterByState(item.getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void launchDynamicTaskViewIntent(Task task) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.TASK_ID_EXTRA, task.getId());
        startActivity(intent);
    }
}
