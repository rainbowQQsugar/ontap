package com.abinbev.dsa.adapter;

import android.view.View;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.SurveyTaker__c;

import java.util.Collections;
import java.util.Comparator;

import butterknife.Bind;

/**
 * Created by Jakub Stefanowski on 20.02.2017.
 */

public class UserSurveysListAdapter extends SurveysListAdapter {

    @Override
    protected int getItemLayoutRes() {
        return R.layout.user_survey_list_entry_view;
    }


    protected void bindViewHolder(ViewHolder vh, SurveyTaker__c survey) {
        super.bindViewHolder(vh, survey);

        UserViewHolder userVH = (UserViewHolder) vh;
        Account account = survey.getAccount();
        userVH.account.setText(account == null ? null : account.getName());
    }

    protected ViewHolder createViewHolder(View view) {
        return new UserViewHolder(view);
    }

    public void sortByAccount(final boolean ascending) {
        Collections.sort(surveys, new Comparator<SurveyTaker__c>() {
            @Override
            public int compare(SurveyTaker__c lhs, SurveyTaker__c rhs) {

                String leftAccountName = lhs.getAccount().getName();
                String rightAccountName = rhs.getAccount().getName();
                if (leftAccountName == null) {
                    leftAccountName = "";
                }
                if (rightAccountName == null) {
                    rightAccountName = "";
                }

                if (ascending) {
                    return leftAccountName.compareTo(rightAccountName);
                } else {
                    return rightAccountName.compareTo(leftAccountName);
                }
            }
        });
        this.notifyDataSetChanged();
    }

    protected class UserViewHolder extends ViewHolder {

        @Bind(R.id.survey_account)
        TextView account;

        public UserViewHolder(View convertView) {
            super(convertView);
        }
    }
}
