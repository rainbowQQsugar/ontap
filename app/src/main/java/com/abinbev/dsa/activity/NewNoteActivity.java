package com.abinbev.dsa.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Note;
import com.abinbev.dsa.ui.presenter.NoteDetailPresenter;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;


import butterknife.Bind;
import butterknife.OnClick;

public class NewNoteActivity extends AppBaseActivity implements NoteDetailPresenter.ViewModel {
    public static final String ARGS_ACCOUNT_ID = "account_id";
    public static final String ARGS_NOTE_ID = "note_id";
    public static final String ARGS_DISPLAY_TITLE = "show_title";
    public static final String ARGS_CUSTOM_TITLE = "custom_title";

    public static final String TAG = NewNoteActivity.class.getSimpleName();

    @Bind(R.id.note_save)
    Button saveButton;

    @Bind(R.id.note_cancel)
    Button cancelButton;

    @Bind(R.id.note_edit)
    Button editButton;

    @Bind(R.id.title)
    TextView noteTitleLabel;

    @Bind(R.id.title_value)
    EditText noteTitle;

    @Bind(R.id.body_value)
    EditText noteBody;

//    @Bind(R.id.create_date)
//    TextView createDate;

    private Note currentNote;
    private String noteId;
    private String accountId;
    private NoteDetailPresenter presenter;
    private String customTitle = "";
    private boolean displayTitle = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        String noteTitle = getString(R.string.notas);
        getSupportActionBar().setTitle(noteTitle.toUpperCase());
        Intent intent = getIntent();
        if (intent != null) {
            noteId = intent.getStringExtra(ARGS_NOTE_ID);
            accountId = intent.getStringExtra(ARGS_ACCOUNT_ID);
            customTitle = intent.getStringExtra(ARGS_CUSTOM_TITLE);
            displayTitle = intent.getBooleanExtra(ARGS_DISPLAY_TITLE, true);
        }

        if (customTitle == null)
            customTitle = "";

        presenter = new NoteDetailPresenter(noteId, accountId);
        presenter.setViewModel(this);
        presenter.start();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isModified()) {
                    askIfClose();
                    return true;
                }
                else {
                    finish();
                    return true;
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.stop();
    }

    @Override
    public void onBackPressed() {
        if (isModified()) {
            askIfClose();
        }
        else {
            super.onBackPressed();
        }
    }

    private boolean isModified() {
        return currentNote == null ||
                !noteTitle.getText().toString().equals(currentNote.getTitle()) ||
                !noteBody.getText().toString().equals(currentNote.getBody());
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_new_note_view;
    }

    private void setupViewWithNoteValues(){

        if (!displayTitle) {
            this.noteTitle.setText(customTitle);
            this.noteTitle.setVisibility(View.GONE);
            noteTitleLabel.setVisibility(View.GONE);
        } else
            noteTitle.setText(currentNote == null ? getString(R.string.notas) : currentNote.getBody());

        noteBody.setText(currentNote == null ? "" : currentNote.getBody());

    }

    private void setupEditMode(){
        editButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
        setupUIEnabled(true);
    }

    private void setupViewMode(){
        editButton.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        setupUIEnabled(false);
    }

    private void setupUIEnabled(boolean enabled){
        if (!displayTitle) {
            this.noteTitle.setText(customTitle);
            this.noteTitle.setVisibility(View.GONE);
            noteTitleLabel.setVisibility(View.GONE);
        } else
            noteTitle.setEnabled(enabled);
        noteBody.setEnabled(enabled);
    }

    private boolean isNoteBodyValid(){
        if (currentNote.getBody() != null){
            return (currentNote.getBody().length() > 0);
        }
        return false;
    }

    private boolean areNoteValuesCorrect(){
        return (noteBody.getText().length() > 0 && noteTitle.getText().length() > 0 && noteTitle.getText().length() <= 80 &&  noteBody.getText().length() <= 32000);
    }

    private void showExitPageDialog() {
        if(isModified()){
            askIfClose();
        }else{
            finish();
        }
    }

    @OnClick(R.id.note_cancel)
    public void cancelButtonClicked(){
//        setupViewMode();
//        setupUIEnabled(false);
//        setupViewWithNoteValues();
        showExitPageDialog();
    }

    @OnClick(R.id.note_edit)
    public void editButtonClicked(){
        setupEditMode();
        setupUIEnabled(true);
    }

    @OnClick(R.id.note_save)
    public void saveButtonClicked(){
        if (areNoteValuesCorrect()){
            setupViewMode();
            setupUIEnabled(false);
            currentNote.setTitle(noteTitle.getText().toString());
            currentNote.setBody(noteBody.getText().toString());
            boolean success = false;
            if (noteId != null){
                success = currentNote.updateNote();
                if (!success) {
                    showSnackbar(R.string.failed_to_save_note);
                }
            } else {
                currentNote = Note.createNote(currentNote.toJson());
                noteId = currentNote.getId();
                success = true;
            }

            if (success) {
                SyncUtils.TriggerRefresh(this);
                setResult(RESULT_OK);
                finish();
            }
            getSupportActionBar().setSubtitle(currentNote.getTitle());
        } else {
            showSnackbar(R.string.save_note_criteria);
        }
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

    @Override
    public void setData(Note note){

        // if the note is coming back as null we are going to run into other issues when we try to save or do any other action
        // better to take the user back to the list view so the user can continue his work
        if (note == null) {
            Log.e(TAG, "Note not found. Id might have been refreshed after sync");
            finish();
            return;
        }

        currentNote = note;

        String title = (currentNote == null) ? null: currentNote.getTitle();
        if (!TextUtils.isEmpty(title)){
            getSupportActionBar().setSubtitle(title);
        }
        else {
            getSupportActionBar().setSubtitle(getString(R.string.new_note));
        }
        if (isNoteBodyValid()){
            setupViewMode();
        } else {
            setupEditMode();
        }
        if (currentNote != null){
            setupViewWithNoteValues();
        }
    }

    @Override
    public void askIfClose() {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setMessage(R.string.are_you_sure_to_discard)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}
