package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.OnClick;
import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.NotesListAdapter;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Note;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.ui.presenter.NotesListPresenter;
import com.abinbev.dsa.ui.view.SortableHeader;
import java.util.List;
import java.util.Map;

public class NotesListActivity extends AppBaseActivity implements NotesListPresenter.ViewModel, NotesListAdapter.NoteClickHandler, SyncListener {

    public static final String ACCOUNT_ID = "account_id";

    private NotesListPresenter presenter;
    private NotesListAdapter adapter;

    @Bind(R.id.notes_list)
    ListView notesList;

    @Bind(R.id.new_note)
    FloatingActionButton newNoteButton;

    @Nullable
    @Bind({ R.id.title, R.id.comment })
    List<SortableHeader> sortableHeaders;

    public String accountId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setSubtitle(getString(R.string.notas));

        accountId = getIntent().getStringExtra(ACCOUNT_ID);
        adapter = new NotesListAdapter(this);
        notesList.setAdapter(adapter);

        setSyncListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        clearSortOnAllOthers(-1); //clear all
        if (presenter == null) {
            presenter = new NotesListPresenter(accountId);
        }
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.stop();
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
    public int getLayoutResId() {
        return R.layout.activity_notes_list;
    }

    @Override
    public void setData(List<Note> notes, Map<String, User> notesCreators) {
        adapter.setData(notes, notesCreators);
    }

    @Override
    public void setAccount(Account account) {
        getSupportActionBar().setTitle(account.getName());
    }

    @Nullable
    @OnClick({R.id.title, R.id.comment})
    @SuppressWarnings("unused")
    public void onHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;

        switch (sortableHeader.getId()) {
            case R.id.title :
                adapter.sortByTitle(sortableHeader.toggleSortDirection());
                break;
            case R.id.comment :
                adapter.sortByComment(sortableHeader.toggleSortDirection());
                break;
            default:
                break;
        }
        clearSortOnAllOthers(sortableHeader.getId());
    }

    /**
     * Clears the sort order on all headers in the group EXCEPT for the id of the view passed in
     * @param sortableHeaderId - the id to NOT clear the sort
     */
    private void clearSortOnAllOthers(int sortableHeaderId) {
        for (SortableHeader sortableHeader : sortableHeaders) {
            if (sortableHeader.getId() != sortableHeaderId) {
                sortableHeader.clearSortIndicator();
            }
        }
    }

    @Override
    public void onNoteClick(String noteId) {
        Intent intent = new Intent(this, NewNoteActivity.class);
        intent.putExtra(NewNoteActivity.ARGS_NOTE_ID, noteId);
        startActivity(intent);
    }

    @OnClick(R.id.new_note)
    @SuppressWarnings("unused")
    public void onNewNoteClick() {
        Intent intent = new Intent(this, NewNoteActivity.class);
        intent.putExtra(NewNoteActivity.ARGS_ACCOUNT_ID, accountId);
        startActivity(intent);
    }

    @Override
    public void onSyncCompleted() {
        presenter.start();
    }

    @Override
    public void onSyncError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSyncFailure(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
