package com.abinbev.dsa.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Survey__c;
import com.abinbev.dsa.ui.presenter.SurveyTestPresenter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by lukaszwalukiewicz on 23.12.2015.
 */
public class SurveyTestActivity extends AppBaseDrawerActivity implements SurveyTestPresenter.ViewModel {
    public static final String ACCOUNT_ID_EXTRA = "account_id";

    @Bind(R.id.new_survey)
    FloatingActionButton newSurveyButton;

    @Bind(R.id.popup_menu_anchor)
    View popupMenuAnchor;

    @Bind(R.id.survey_id)
    EditText surveyIdEditText;

    @Bind(R.id.account_id)
    EditText accountIdEditText;

    @Bind(R.id.bundle_value)
    EditText bundleEditText;

    PopupMenu popupMenu;

    SurveyTestPresenter presenter;

    String accountId;

    Map<String, Survey__c> surveyMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("");

        popupMenu = new PopupMenu(this, popupMenuAnchor);
        popupMenu.setOnMenuItemClickListener(item -> {
            presenter.onSurveyClicked(surveyMap.get(item.getTitle().toString()));
            return true;
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
    public void onRefresh() {
        if (presenter == null) {
            presenter = new SurveyTestPresenter();
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
    public int getLayoutResId() {
        return R.layout.activity_survey_test;
    }

    @Override /* SurveyTestPresenter.ViewModel */
    public void showData(SurveyTestPresenter.Data data) {
        accountIdEditText.setText(data.accountId);
        surveyIdEditText.setText(data.surveyTakerId);
        bundleEditText.setText(data.bundleLocation);
    }

    @Override /* SurveyTestPresenter.ViewModel */
    public SurveyTestPresenter.Data loadData() {
        SurveyTestPresenter.Data data = new SurveyTestPresenter.Data();
        data.surveyTakerId = surveyIdEditText.getText().toString().trim();
        data.accountId = accountIdEditText.getText().toString().trim();
        data.bundleLocation = bundleEditText.getText().toString().trim();
        return data;
    }

    @Override /* SurveyTestPresenter.ViewModel */
    public void showSurveyTypes(List<Survey__c> recordTypes) {
        popupMenu.getMenu().clear();
        surveyMap.clear();

        for (Survey__c recordType : recordTypes) {
            String name = recordType.getName();
            popupMenu.getMenu().add(name);
            surveyMap.put(name, recordType);
        }
        popupMenu.show();
    }

    @Override /* SurveyTestPresenter.ViewModel */
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override /* SurveyTestPresenter.ViewModel */
    public Activity getActivity() {
        return this;
    }

    @OnClick(R.id.new_survey)
    public void onNewSurveyClick() {
        presenter.onNewSurveyClicked();
    }

    @OnClick(R.id.btn_local_html_bundle)
    public void launchLocalHtmlBundle() {
        presenter.onLaunchLocalBundleClicked();
    }
}
