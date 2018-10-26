package com.abinbev.dsa.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.QuizAdapter;
import com.abinbev.dsa.adapter.SearchableAdapter;
import com.abinbev.dsa.model.SurveyTaker__c;
import com.abinbev.dsa.model.Survey__c;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.ui.presenter.QuizzesPresenter;
import com.abinbev.dsa.ui.view.SortableHeader;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import com.salesforce.dsa.data.model.ContentVersion;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class QuizListActivity extends AppBaseDrawerActivity implements QuizzesPresenter.ViewModel {

    @Bind(R.id.quiz_list)
    ListView listView;

    @Nullable
    @Bind({ R.id.quiz_name, R.id.quiz_state, R.id.quiz_creation_date, R.id.quiz_total_score})
    List<SortableHeader> sortableHeaders;

    @Bind(R.id.new_quiz)
    FloatingActionButton newSurveyButton;

    @Bind(R.id.popup_menu_anchor)
    View popupMenuAnchor;

    private QuizAdapter adapter;
    private QuizzesPresenter presenter;
    private PopupMenu popupMenu;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("");

        adapter = new QuizAdapter();
        adapter.setIsSupervisor(User.isSupervisor(this));
        listView.setAdapter(adapter);

        Intent intent = getIntent();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (syncInProgress) {
                    Toast.makeText(QuizListActivity.this, R.string.quiz_sync_in_progress, Toast.LENGTH_LONG).show();
                    return;
                }
                SurveyTaker__c surveyTaker = (SurveyTaker__c)adapter.getItem(position);
                launchSurveyWebViewIntent(surveyTaker);
            }
        });

        popupMenu = new PopupMenu(this, popupMenuAnchor);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (syncInProgress) {
                    Toast.makeText(QuizListActivity.this, R.string.quiz_sync_in_progress, Toast.LENGTH_LONG).show();
                    return true;
                }

                if (item != null) {
                    if (User.isSupervisor(QuizListActivity.this)) {
                        final String quizTitle = item.getTitle().toString();
                        // show dialog
                        new AlertDialog.Builder(QuizListActivity.this)
                                .setTitle(quizTitle)
                                .setMessage(getString(R.string.supervisor_prompt))
                                .setPositiveButton(getString(R.string.select_user), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        selectUserForQuiz(quizTitle);
                                    }
                                }).setNegativeButton(getString(R.string.take_quiz), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        goToAddSurvey(quizTitle, null);
                                    }
                        }).create().show();

                        // based on input either assign or take survey

                    } else {
                        goToAddSurvey(item.getTitle().toString(), null);
                    }
                }
                return true;
            }
        });
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
    protected void onPause() {
        super.onPause();
        presenter.stop();
    }

    @Override
    public void onRefresh() {
        if (presenter == null) {
            presenter = new QuizzesPresenter();
        }
        presenter.setViewModel(this);
        presenter.start();
    }


    @Override
    public int getLayoutResId() {
        return R.layout.activity_quiz_list;
    }

    @Override
    public void setData(List<SurveyTaker__c> surveys){
        adapter.setData(surveys);
    }

    @Nullable
    @OnClick(R.id.quiz_name)
    public void onSurveyNumberHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByOrderNumber(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @Nullable
    @OnClick(R.id.quiz_state)
    public void onSurveyStateHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByState(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @Nullable
    @OnClick(R.id.quiz_creation_date)
    public void onSurveyCreateDateHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByCreateDate(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @Nullable
    @OnClick(R.id.quiz_total_score)
    public void onSurveyDueDateHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByTotalScore(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
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

    @Override
    public void setSurveyTypes(List<Survey__c> recordTypes) {

        if (!recordTypes.isEmpty()) {
            newSurveyButton.setEnabled(true);
        }

        popupMenu.getMenu().clear();
        for (Survey__c recordType : recordTypes) {
            popupMenu.getMenu().add(recordType.getName());
        }
    }

    @OnClick(R.id.new_quiz)
    @SuppressWarnings("unused")
    public void onNewQuizClick() {
        popupMenu.show();
    }

    private void goToAddSurvey(String name, String userId) {

        final Survey__c surveyType = Survey__c.getByName(name);
        ContentVersion contentVersion = surveyType.getAssociatedContent();
        if (contentVersion == null) {
            Toast.makeText(this, R.string.survey_error_message, Toast.LENGTH_LONG).show();
            return;
        }
        final Intent webViewIntent = new Intent(this, WebViewActivity.class);
        //New Survey uses SURVEY_ID
        webViewIntent.putExtra(WebViewActivity.SURVEY_ID, surveyType.getId());
        webViewIntent.putExtra(WebViewActivity.TITLE, getString(R.string.quizzes));
        webViewIntent.putExtra(WebViewActivity.DESCRIPTION, surveyType.getName());
        webViewIntent.putExtra(WebViewActivity.COMPLETE_FILE_NAME, contentVersion.getCompleteFileName(this));
        webViewIntent.putExtra(WebViewActivity.FILE_EXTENSION, WebViewActivity.ZIP);
        webViewIntent.putExtra(WebViewActivity.SURVEY_USER_ID, userId);
        startActivity(webViewIntent);
    }

    private void launchSurveyWebViewIntent(SurveyTaker__c surveyTaker) {

        ContentVersion contentVersion = surveyTaker.getAssociatedContent();
        if (contentVersion == null) {
            Toast.makeText(this, R.string.survey_error_message, Toast.LENGTH_LONG).show();
            return;
        }

        Survey__c survey = surveyTaker.getSurvey__c();
        final Intent webViewIntent = new Intent(this, WebViewActivity.class);
        webViewIntent.putExtra(WebViewActivity.SURVEY_TAKER_ID, surveyTaker.getId());
        webViewIntent.putExtra(WebViewActivity.TITLE, getString(R.string.quizzes));
        webViewIntent.putExtra(WebViewActivity.DESCRIPTION, survey.getName());
        webViewIntent.putExtra(WebViewActivity.COMPLETE_FILE_NAME, contentVersion.getCompleteFileName(this));
        webViewIntent.putExtra(WebViewActivity.FILE_EXTENSION, WebViewActivity.ZIP);
        startActivity(webViewIntent);
    }


    private void selectUserForQuiz(final String quizTitle) {
        if (!User.isSupervisor(this)) return;
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_search, null, false);
        final EditText input = (EditText) view.findViewById(R.id.editSearch);
        final ListView list = (ListView) view.findViewById(R.id.list);
        final SearchableAdapter adapter = new SearchableAdapter();
        list.setAdapter(adapter);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    adapter.getFilter().filter(s);
                } else {
                    adapter.clearUsers(); //pedidoListAdapter.users.clear();
                    adapter.resetSearch(); //pedidoListAdapter.search = null;
                    adapter.notifyDataSetChanged();
                }
                list.requestLayout();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_user))
                .setView(view)
                .setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final User user = (User) adapter.getItem(position);
                final Survey__c surveyType = Survey__c.getByName(quizTitle);

                // show dialog
                new AlertDialog.Builder(QuizListActivity.this)
                        .setTitle(quizTitle)
                        .setMessage(getString(R.string.supervisor_prompt))
                        .setPositiveButton(getString(R.string.assign_quiz), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String tempId = SurveyTaker__c.createNewQuizTakerRecord(surveyType.getId(), user.getId());
                                if (tempId != null) {
                                    String toastMessage = getString(R.string.quiz_assigned, quizTitle, user.getName());
                                    Toast.makeText(QuizListActivity.this, toastMessage, Toast.LENGTH_LONG).show();
                                    onRefresh();
                                    SyncUtils.TriggerRefresh(QuizListActivity.this);
                                } else {

                                }
                                dialog.dismiss();

                            }
                        }).setNegativeButton(getString(R.string.take_quiz_user), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                goToAddSurvey(quizTitle, user.getId());
                                dialog.dismiss();

                            }
                }).create().show();

                dialog.dismiss();
            }

        });
        dialog.show();
    }
}
