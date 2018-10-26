package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.model.Task;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.model.UserProfile;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.ContentUtils;
import com.abinbev.dsa.utils.MetadataPicklistHelper;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by lukaszwalukiewicz on 13.01.2016.
 */
public class TaskDetailActivity extends AppBaseActivity {
    public static final String TAG = TaskDetailActivity.class.getSimpleName();
    public static final String TASK_ID_EXTRA = "task_id";
    public static final String ACCOUNT_ID_EXTRA = "account_id";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.assignValue)
    TextView assign;

    @Bind(R.id.commentValue)
    TextView comment;

    @Bind(R.id.activityDateValue)
    TextView activityDate;

    @Bind(R.id.resultValue)
    EditText result;

    @Bind(R.id.subject)
    TextView subject;

    @Bind(R.id.stateSpinner)
    Spinner statusSpinner;

    @Bind(R.id.priority)
    TextView priority;

    @Bind(R.id.related_to)
    TextView relatedTo;

    @Bind(R.id.task_save)
    Button saveButton;

    @Bind(R.id.task_cancel)
    Button cancelButton;

    private Task task;
    private MetadataPicklistHelper metadataPicklistHelper;
    private String taskId;
    private Map<String, String> statusMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            String tasks = getString(R.string.task);
            getSupportActionBar().setTitle(tasks.toUpperCase());
        }
        Intent intent = getIntent();
        if (intent != null) {
            String subtitle = getString(R.string.new_task);
            taskId = intent.getStringExtra(TASK_ID_EXTRA);
            if (taskId != null) {
                task = Task.getById(taskId);
                if (task != null) {
                    subtitle = task.getSubject();
                }
            } else {
                String accountId = intent.getStringExtra(ACCOUNT_ID_EXTRA);
                String recordTypeId = RecordType.getDefaultRecordTypeId(this, AbInBevConstants.AbInBevObjects.TASK);
                task = Task.createDefaultTask(accountId, recordTypeId);
            }

            if (task == null) {
                Log.e(TAG, "got null task");
                finish();
                return;
            }
            getSupportActionBar().setSubtitle(subtitle);
        }
        //This spinner is controlled by status spinner, this is whay it is set as disabled by default
        setupViewWithTaskValues();
        if (Task.STATE_COMPLETED.equals(task.getState())) {
            setupViewMode();
        }
    }


    @Override
    public int getLayoutResId() {
        return R.layout.activity_task_detail_view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewWithTaskValues() {
        // we may not have the task owner info in the local store due to filtering
        if (task.getOwnerName() != null) {
            assign.setText(task.getOwnerName());
        } else {
            assign.setText("");
        }

        comment.setText(task.getComment());
        relatedTo.setText(task.getAccount().getName());
        if (Task.PRIORITY_NORMAL.equals(task.getPriority())) {
            priority.setText(R.string.priority_normal);
        } else if (Task.PRIORITY_HIGH.equals(task.getPriority())) {
            priority.setText(R.string.priority_high);
        } else if (Task.PRIORITY_LOW.equals(task.getPriority())) {
            priority.setText(R.string.priority_low);
        } else {
            priority.setText(task.getPriority());
        }
//        scheduledTask.setEnabled(task.getScheduled());
        activityDate.setText(task.getActivityDate());
        result.setText(task.getTaskResult());
        this.metadataPicklistHelper = new MetadataPicklistHelper(AbInBevConstants.AbInBevObjects.TASK, task, this);
        metadataPicklistHelper.setupSpinnerWithoutNoneLabelValues(statusSpinner, AbInBevConstants.TaskFields.STATUS, null);
        statusMap = metadataPicklistHelper.getPickListMapValues(AbInBevConstants.TaskFields.STATUS);
        subject.setText(task.getSubject());
    }

    private void setupViewMode() {
        saveButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        setupUIEnabled(false);
    }

    private void setupUIEnabled(boolean enabled) {
        result.setEnabled(enabled);
        statusSpinner.setEnabled(enabled);
    }

    private boolean areTaskValuesCorrect() {

        String status = statusSpinner.getSelectedItem().toString();
        String statusKey = statusMap.get(status);
        if (isCancelStatus(statusKey)) {
            if (!canUserCancelTask()) {
                Toast.makeText(this, R.string.no_permitted_task_status, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (Task.STATE_COMPLETED.equals(statusKey) ||
                Task.STATE_WAIT_SOMEONE_ELSE.equals(statusKey) ||
                Task.STATE_DEFERRED.equals(statusKey)) {
            if (StringUtils.isEmpty(result.getText().toString())) {
                Toast.makeText(this, R.string.status_tip, Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private boolean canUserCancelTask() {
        String userId = task.getOwnerId();
        User user = User.getUserByUserId(userId);
        if (user == null) return false;
        String profile = user.getProfile();
        return (!profile.equalsIgnoreCase(UserProfile.brandDeveloper.getProfileName()) &&
                !profile.equalsIgnoreCase(UserProfile.pe_agent.getProfileName()) &&
                !profile.equalsIgnoreCase(UserProfile.pe_supervisor.getProfileName()) &&
                !profile.equalsIgnoreCase(UserProfile.EC_BRAND_DEVELOPER_FORCE.getProfileName()) &&
                !profile.equalsIgnoreCase(UserProfile.EC_SUPERVISOR_FORCE.getProfileName()));
    }

    private boolean isCancelStatus(String state) {
        return (Task.STATE_CANCELADO.equalsIgnoreCase(state) || Task.STATE_CANCELADAS.equalsIgnoreCase(state));
    }

    @OnClick(R.id.task_save)
    public void saveButtonClicked() {
        if (areTaskValuesCorrect()) {
            setupViewMode();
            setupUIEnabled(false);
            task.setComment(comment.getText().toString());
            String status = statusSpinner.getSelectedItem().toString();
            if (ContentUtils.isPicklistStringValid(status, this)) {
                task.setStatus(statusMap.get(status));
            }
            task.setActivityDate(activityDate.getText().toString());
            task.setTaskResult(result.getText().toString());
            if (taskId != null) {
                boolean success = task.updateTask();
                if (!success) {
                    showSnackbar(R.string.failed_to_save_task);
                } else {
                    SyncUtils.TriggerRefresh(this);
                    finish();
                }
            } else {
                task = Task.createTask(task.toJson());
                taskId = task.getId();
                SyncUtils.TriggerRefresh(this);
                finish();
            }
        }
    }

    @OnClick(R.id.task_cancel)
    public void cancelButtonClicked() {
        finish();
    }

    private void showSnackbar(int errorStringResourceId) {

        final Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), errorStringResourceId, Snackbar.LENGTH_INDEFINITE);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        if (textView != null) textView.setMaxLines(3);  // show multiple line

        snackbar.setAction(R.string.dismiss, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }
}
