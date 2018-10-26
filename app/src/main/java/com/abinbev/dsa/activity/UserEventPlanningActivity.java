package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.EventPlanningListAdapter;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.Event_Catalog__c;
import com.abinbev.dsa.model.Task;
import com.abinbev.dsa.ui.presenter.EventPlanningUserPresenter;
import com.abinbev.dsa.ui.presenter.UserTasksListPresenter;
import com.abinbev.dsa.utils.AppPreferenceUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;

import static com.abinbev.dsa.activity.AccountOverviewActivity.ACCOUNT_ID_EXTRA;

public class UserEventPlanningActivity extends AppBaseActivity implements EventPlanningUserPresenter.ViewModel, EventPlanningListAdapter.CalendarEventListener {

    public static final String USER_ID_EXTRA = "user_id";
    public static final String ACCOUNT_ID_EXTRA = "account_id";
    public static final String EVENT_ID_EXTRA = "event_id";

    @Bind(R.id.event_list)
    RecyclerView recyclerView;

    private EventPlanningListAdapter adapter;
    private EventPlanningUserPresenter presenter;
    private String userId;
    private Long selectedDate;

    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.event_planning_header));

        adapter = new EventPlanningListAdapter();
        adapter.setListener(this);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra(USER_ID_EXTRA);
        }

        selectedDate = new Date().getTime();
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
    public void onResume() {
        super.onResume();
        if (presenter == null) {
            presenter = new EventPlanningUserPresenter(userId);
        }
        presenter.setViewModel(this);
        String dateString = android.text.format.DateFormat.format("yyyy-MM-dd", selectedDate).toString();
        presenter.setSelectedDate(dateString);
        presenter.setUserId(userId);
        presenter.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.stop();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.user_event_planning_account;
    }

    @Override
    public void setData(List<Event> events) {
        adapter.setEventData(events);
    }

    @Override
    public void setTaskData(List<Task> tasks) {
        adapter.setTaskData(tasks);
    }

    @Override
    public void setEventTypes(List<Event_Catalog__c> recordTypes) {
    }

    private void goToEventDetails(Event event) {
        Intent intent = new Intent(this, AccountOverviewActivity.class);
        intent.putExtra(EVENT_ID_EXTRA, event.getId());
        intent.putExtra(ACCOUNT_ID_EXTRA, event.getAccountId());
        startActivity(intent);
    }

    private void goToTaskDetails(Task task) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.TASK_ID_EXTRA, task.getId());
        startActivity(intent);
    }

    @Override
    public void onEventClick(Event event) {
        Account account = event.getAccount();
        AppPreferenceUtils.putAccountIdOfCuriosity(getApplicationContext(), account.getId());

        goToEventDetails(event);
    }

    @Override
    public void onTaskClick(Task task) {
        goToTaskDetails(task);
    }

    @Override
    public void onDateSelected(Date newDate) {
        if (newDate.getTime() != selectedDate) {

            selectedDate = newDate.getTime();
            String dateString = android.text.format.DateFormat.format("yyyy-MM-dd", selectedDate).toString();
            if (presenter == null) {
                presenter = new EventPlanningUserPresenter(userId);
            }

            int scrollPos = getPositionToScroll();
            if (scrollPos >= 0)
                layoutManager.smoothScrollToPosition(recyclerView, null, scrollPos);

            presenter.setSelectedDate(dateString);
            presenter.start();
        }
    }

    public int getPositionToScroll() {
        int i = 0;
        Calendar selectedDateCalendar = Calendar.getInstance();
        selectedDateCalendar.setTime(new Date(selectedDate));
        Calendar eventTime = Calendar.getInstance();
        for (Event event : adapter.getEventList()) {
            eventTime.setTime(event.getStartDateTime());
            if (selectedDateCalendar.get(Calendar.YEAR) == eventTime.get(Calendar.YEAR) &&
                    selectedDateCalendar.get(Calendar.DAY_OF_YEAR) == eventTime.get(Calendar.DAY_OF_YEAR))
                return i + 1;

            i++;
        }
        return -1;
    }
}
