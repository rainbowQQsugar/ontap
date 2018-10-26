package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.NotifyMsgAdapter;
import com.abinbev.dsa.model.CN_NotificationUserRead__c;
import com.abinbev.dsa.model.CN_Notification_Message__c;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.receiver.TriggerReceiver;
import com.abinbev.dsa.ui.presenter.UserDetailsPresenter;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.dynamicfetch.DynamicFetchEngine;
import com.salesforce.dsa.app.utils.DeviceNetworkUtils;

import java.util.LinkedList;
import java.util.List;
import butterknife.Bind;

public class NotifyMessageActivity extends AppBaseDrawerActivity implements AdapterView.OnItemClickListener, UserDetailsPresenter.ViewModel {

    public static final String ARGS_ACCOUNT_ID = "account_id";
    @Bind(R.id.account_list)
    ListView accountList;
    @Bind(R.id.no_such_message)
    TextView noSuchMessage;
    private List<CN_Notification_Message__c> message__cs = new LinkedList<>();
    private NotifyMsgAdapter adapter;
    private String TAG = this.getClass().getSimpleName();
    private boolean isFirstStart;
    private UserDetailsPresenter userDetailsPresenter;
    private User user;
    private List<SyncTask> syncTasks = new LinkedList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ImageView imageView = (ImageView) toolbar.getChildAt(0);
            imageView.setImageResource(R.drawable.ic_abi_message_logo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        adapter = new NotifyMsgAdapter(this, message__cs);
        accountList.setAdapter(adapter);
        accountList.setOnItemClickListener(this);

        userDetailsPresenter = new UserDetailsPresenter(this, isFirstStart);
        userDetailsPresenter.setViewModel(this);

        CN_NotificationUserRead__c.getAll();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_notify_message;
    }

    @Override
    public void onRefresh() {

    }

    @Override
    protected void onSyncCompleted() {
        super.onSyncCompleted();
        userDetailsPresenter.start();
        initData();
    }

    @Override
    protected void dynamicFetchCompleted(Intent intent) {
        super.dynamicFetchCompleted(intent);
        if (intent != null && (intent.getStringExtra(DataManager.EXTRAS_DYNAMIC_FETCH_NAME).
                equalsIgnoreCase(AbInBevConstants.DynamicFetch.NOTIFICATION_MESSAGE_LATEST)
                ||
                intent.getStringExtra(DataManager.EXTRAS_DYNAMIC_FETCH_NAME).
                        equalsIgnoreCase(AbInBevConstants.DynamicFetch.NOTIFICATION_USER_READ))) {
            initData();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DynamicFetchEngine.fetchInBackground(ABInBevApp.getAppContext(), AbInBevConstants.DynamicFetch.NOTIFICATION_MESSAGE_LATEST, null);

        if (!DeviceNetworkUtils.isConnected(this)) {

            initData();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        userDetailsPresenter.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        userDetailsPresenter.start();
    }

    @Override
    protected void onDestroy() {
        try {

            if (syncTasks != null && syncTasks.size() > 0) {

                for (SyncTask syncTask : syncTasks) {
                    if (syncTasks != null) {
                        syncTask.cancel(true);
                    }
                    Log.e(TAG, "cancel");
                    syncTask = null;
                }
                syncTasks.clear();
                syncTasks = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
        userDetailsPresenter.onDestroy();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (adapter != null) {
            CN_Notification_Message__c c = adapter.getItem(position);
            if (c != null && c.getItemId() == NotifyMsgAdapter.TYPE_CONTENT) {

                if (c.getTypeId() == NotifyMsgAdapter.TYPE_CONTRACT_RENEWAL) {

                    executeContractRenewal(c);

                } else if (c.getTypeId() == NotifyMsgAdapter.TYPE_SYSTEM_NOTIFICATION) {

                    executeSystemNotification(c);

                } else if (c.getTypeId() == NotifyMsgAdapter.TYPE_NEW_TASK) {
                    executeNewTask(c);
                }
            }
        }
    }

    public void initData() {

        SyncTask syncTask = new SyncTask();
        syncTask.execute();
        syncTasks.add(syncTask);
    }

    @Override
    public void setState(UserDetailsPresenter.State state) {
        if (state != null)
            this.user = state.user;
    }


    class SyncTask extends AsyncTask<Void, Void, List<CN_Notification_Message__c>> {

        public SyncTask() {}

        @Override
        protected List<CN_Notification_Message__c> doInBackground(Void... voids) {
            return CN_Notification_Message__c.getCNNotitycationMessageAllData();
        }

        @Override
        protected void onPostExecute(List<CN_Notification_Message__c> message) {
            super.onPostExecute(message);
            try {

                if (message != null) {

                    if (message__cs != null) {
                        message__cs.clear();
                        message__cs.addAll(message);

                        invalidateOptionsMenu();

                        if (message__cs.size() == 0) {
                            accountList.setVisibility(View.GONE);
                            noSuchMessage.setVisibility(View.VISIBLE);
                        } else {
                            accountList.setVisibility(View.VISIBLE);
                            noSuchMessage.setVisibility(View.GONE);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * New task function
     */
    private void executeNewTask(CN_Notification_Message__c c) {

        String RelatedId = c.getRelatedId();
        String messageID = c.getId();
        String userID = this.user.getId();
        if (TextUtils.isEmpty(RelatedId) || TextUtils.isEmpty(messageID) || TextUtils.isEmpty(userID)) {
            Toast.makeText(this, getResources().getString(R.string.related_id), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            boolean isSuccess = CN_NotificationUserRead__c.saveUserMessage(c.getId(), this.user.getId());
            if (isSuccess) {
                initData();
            }
            if (DeviceNetworkUtils.isConnected(this)) {
                Intent intentReceiver = new Intent();
                intentReceiver.setAction(TriggerReceiver.TAG);
                sendBroadcast(intentReceiver);
            }
            Intent intent = new Intent(this, TaskDetailActivity.class);
            intent.putExtra(TaskDetailActivity.TASK_ID_EXTRA, RelatedId);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * System Notification function
     */
    private void executeSystemNotification(CN_Notification_Message__c c) {

        try {
            String messageID = c.getId();
            String userID = this.user.getId();
            if ( TextUtils.isEmpty(messageID) || TextUtils.isEmpty(userID)) {
                Toast.makeText(this, getResources().getString(R.string.related_id), Toast.LENGTH_SHORT).show();
                return;
            }


            boolean isSuccess = CN_NotificationUserRead__c.saveUserMessage(messageID, userID);

            if (isSuccess) {

                initData();
            }

            if (DeviceNetworkUtils.isConnected(this)) {
                Intent intentReceiver = new Intent();
                intentReceiver.setAction(TriggerReceiver.TAG);
                sendBroadcast(intentReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Contract Renewal function
     */
    private void executeContractRenewal(CN_Notification_Message__c c) {

        String RelatedId = c.getRelatedId();
        String messageID = c.getId();
        String userID = this.user.getId();

        if (TextUtils.isEmpty(RelatedId) || TextUtils.isEmpty(messageID) || TextUtils.isEmpty(userID)) {
            Toast.makeText(this, getResources().getString(R.string.related_id), Toast.LENGTH_SHORT).show();
            return;
        }


        try {

            boolean isSuccess = CN_NotificationUserRead__c.saveUserMessage(messageID, userID);

            if (isSuccess) {

                initData();
            }
            if (DeviceNetworkUtils.isConnected(this)) {
                Intent intentReceiver = new Intent();
                intentReceiver.setAction(TriggerReceiver.TAG);
                sendBroadcast(intentReceiver);
            }
            Intent intent = new Intent(this, ContractsActivity.class);
            intent.putExtra(ContractsActivity.ARGS_ACCOUNT_ID, RelatedId);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
