package com.abinbev.dsa.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.NewNoteActivity;
import com.abinbev.dsa.activity.NotesListActivity;
import com.abinbev.dsa.model.Note;
import com.abinbev.dsa.ui.presenter.NotePresenter;
import com.abinbev.dsa.utils.ContentUtils;
import com.abinbev.dsa.utils.DateUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by nchangnon on 11/24/15.
 */
public class NotesView extends RelativeLayout implements NotePresenter.ViewModel, RefreshListener {

    @Bind(R.id.note_title)
    TextView noteTitle;

    @Bind(R.id.note_created_date)
    TextView noteCreatedDate;

    @Bind(R.id.note_body)
    TextView noteBody;

    @Bind(R.id.no_notes)
    TextView noNotes;

    private String accountId;
    private NotePresenter notePresenter;

    public NotesView(Context context) {
        this(context, null);
    }

    public NotesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp(context);
    }

    @TargetApi(21)
    public NotesView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setUp(context);
    }

    private void setUp(Context context) {
        inflate(context, R.layout.notes_view, this);
        ButterKnife.bind(this);
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
        if (notePresenter == null) {
            notePresenter = new NotePresenter(accountId);
        }
        notePresenter.setViewModel(this);
        notePresenter.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (notePresenter != null) {
            notePresenter.stop();
        }
    }

    @Override
    public void setData(Note note) {
        if (note == null) {
            noNotes.setVisibility(VISIBLE);
            noteCreatedDate.setVisibility(GONE);
            noteTitle.setVisibility(GONE);
            noteBody.setVisibility(GONE);
        } else {
            noNotes.setVisibility(GONE);
            noteCreatedDate.setVisibility(VISIBLE);
            noteTitle.setVisibility(VISIBLE);
            noteBody.setVisibility(VISIBLE);
            if (ContentUtils.isNull_OR_Blank(note.getCreatedDate())) {
                noteCreatedDate.setText("");
            } else {
                noteCreatedDate.setText(DateUtils.formatDateTimeShort(note.getCreatedDate()));
            }
            noteTitle.setText(note.getTitle());
            noteBody.setText(note.getBody());
        }
    }

    @OnClick(R.id.add_note)
    public void onAddNoteClicked() {
        Intent intent = new Intent(getContext(), NewNoteActivity.class);
        intent.putExtra(NewNoteActivity.ARGS_ACCOUNT_ID, accountId);
        getContext().startActivity(intent);
    }

    @Override
    public void onRefresh() {
        if (notePresenter != null) {
            notePresenter.start();
        }
    }

    @OnClick(R.id.view_all_notes)
    @SuppressWarnings("unused")
    public void onViewAllClick() {
        Intent intent = new Intent(getContext(), NotesListActivity.class);
        intent.putExtra(NewNoteActivity.ARGS_ACCOUNT_ID, accountId);
        getContext().startActivity(intent);
    }
}
