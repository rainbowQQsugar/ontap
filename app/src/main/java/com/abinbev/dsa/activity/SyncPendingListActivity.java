package com.abinbev.dsa.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.SyncPendingListAdapter;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.model.QueueObject;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by bduggirala on 12/17/15.
 */
public class SyncPendingListActivity extends AppBaseDrawerActivity {

    private SyncPendingListAdapter syncPendingListAdapter;

    @Bind(R.id.pending_header)
    TextView pendingHeader;

    @Bind(R.id.pending_list)
    ListView pendingList;

    int resetCounter;
    int resetAtCount = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("");

        syncPendingListAdapter = new SyncPendingListAdapter();
        pendingList.setAdapter(syncPendingListAdapter);

        List<QueueObject> queueObjectList = DataManagerFactory.getDataManager().getQueueRecords();
        syncPendingListAdapter.setData(queueObjectList);
        pendingHeader.setText(String.format(getString(R.string.pending_header), queueObjectList.size()));

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
        List<QueueObject> queueObjectList = DataManagerFactory.getDataManager().getQueueRecords();
        syncPendingListAdapter.setData(queueObjectList);
        pendingHeader.setText(String.format(getString(R.string.pending_header), queueObjectList.size()));
    }

    @Override
    public int getLayoutResId() {
        return R.layout.pending_list_view;
    }

    @OnClick(R.id.pending_header)
    public void pendingHeaderClick() {
        resetCounter++;
        if (resetCounter >= resetAtCount) {
            // TODO: Remove this after testing ...
            resetCounter = 0;
            DataManager dataManager = DataManagerFactory.getDataManager();
            List<QueueObject> queueObjectList = DataManagerFactory.getDataManager().getQueueRecords();
            for (QueueObject queueObject : queueObjectList) {
                dataManager.deleteQueueRecordFromClient(queueObject.getSoupEntryId(), true);
            }
            onRefresh();
        }
    }
}
