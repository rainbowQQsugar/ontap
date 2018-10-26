package com.abinbev.dsa.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.PictureAuditAdapter;
import com.abinbev.dsa.sync.DefaultSyncBroadcastReceiver;
import com.abinbev.dsa.ui.presenter.PictureAuditPresenter;
import com.abinbev.dsa.ui.presenter.PictureAuditPresenter.RejectedFile;

import java.util.List;

import butterknife.Bind;

/**
 * Created by Jakub Stefanowski on 19.10.2017.
 */
public class PictureAuditActivity extends AppBaseDrawerActivity implements PictureAuditPresenter.ViewModel {

    private PictureAuditAdapter adapter;

    private PictureAuditPresenter presenter;

    @Bind(R.id.picture_recycler)
    RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("");

        adapter = new PictureAuditAdapter();
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        presenter = new PictureAuditPresenter(new DefaultSyncBroadcastReceiver());
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_picture_audit;
    }

    @Override
    public void onRefresh() { }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    protected void onStop() {
        presenter.stop();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override /* PictureAuditPresenter.ViewModel */
    public void setAuditStatuses(List<RejectedFile> rejectedFiles) {
        adapter.setData(rejectedFiles);
    }
}
