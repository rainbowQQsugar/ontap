package com.abinbev.dsa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.SurveyTaker__c;
import com.abinbev.dsa.utils.ContentUtils;
import com.abinbev.dsa.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by lukaszwalukiewicz on 23.12.2015.
 */
public class SurveysListAdapter extends BaseAdapter{
    List<SurveyTaker__c> surveys;
    public SurveysListAdapter() {
        super();
        surveys = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return surveys.size();
    }

    @Override
    public Object getItem(int position) {
        return surveys.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(getItemLayoutRes(), parent, false);
            convertView.setTag(createViewHolder(convertView));
        }

        SurveyTaker__c survey = surveys.get(position);
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        bindViewHolder(viewHolder, survey);

        return convertView;
    }

    protected void bindViewHolder(ViewHolder vh, SurveyTaker__c survey) {
        vh.surveyNumber.setText(survey.getSurveyName());
        vh.surveyState.setText(survey.getTranslatedState());
        vh.surveyDueDate.setText(DateUtils.formatDateStringShort(survey.getDueDate()));
        vh.surveyCreationDate.setText(DateUtils.formatDateTimeShort(survey.getCreatedDate()));
    }

    protected ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    protected int getItemLayoutRes() {
        return R.layout.survey_list_entry_view;
    }

    public void setData(List<SurveyTaker__c> surveys) {
        this.surveys.clear();
        this.surveys.addAll(surveys);
        this.notifyDataSetChanged();
    }

    public void sortByOrderNumber(final boolean ascending) {
        Collections.sort(surveys, new Comparator<SurveyTaker__c>() {
            @Override
            public int compare(SurveyTaker__c lhs, SurveyTaker__c rhs) {
                if (ascending) {
                    return lhs.getSurveyName().compareTo(rhs.getSurveyName());
                } else {
                    return rhs.getSurveyName().compareTo(lhs.getSurveyName());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByState(final boolean ascending) {
        Collections.sort(surveys, new Comparator<SurveyTaker__c>() {
            @Override
            public int compare(SurveyTaker__c lhs, SurveyTaker__c rhs) {
                if (ascending) {
                    return lhs.getTranslatedState().compareTo(rhs.getTranslatedState());
                } else {
                    return rhs.getTranslatedState().compareTo(lhs.getTranslatedState());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByCreateDate(final boolean ascending) {
        Collections.sort(surveys, new Comparator<SurveyTaker__c>() {
            @Override
            public int compare(SurveyTaker__c lhs, SurveyTaker__c rhs) {
                if (ascending) {
                    //null check these dates
                    if (!ContentUtils.isStringValid(lhs.getCreatedDate()) && !ContentUtils.isStringValid(rhs.getCreatedDate())) {
                        return 0;
                    } else if (!ContentUtils.isStringValid(lhs.getCreatedDate())) {
                        return -1;
                    } else if (!ContentUtils.isStringValid(rhs.getCreatedDate())) {
                        return 1;
                    }
                    return DateUtils.dateFromString(lhs.getCreatedDate()).compareTo(DateUtils.dateFromString(rhs.getCreatedDate()));
                } else {
                    //null check these dates
                    if (!ContentUtils.isStringValid(lhs.getCreatedDate()) && !ContentUtils.isStringValid(lhs.getCreatedDate())) {
                        return 0;
                    } else if (!ContentUtils.isStringValid(rhs.getCreatedDate())) {
                        return -1;
                    } else if (!ContentUtils.isStringValid(lhs.getCreatedDate())) {
                        return 1;
                    }
                    return DateUtils.dateFromString(rhs.getCreatedDate()).compareTo(DateUtils.dateFromString(lhs.getCreatedDate()));
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByDueDate(final boolean ascending) {
        Collections.sort(surveys, new Comparator<SurveyTaker__c>() {
            @Override
            public int compare(SurveyTaker__c lhs, SurveyTaker__c rhs) {
                if (ascending) {
                    if (!ContentUtils.isStringValid(lhs.getDueDate()) && !ContentUtils.isStringValid(rhs.getDueDate())) {
                        return 0;
                    } else if (!ContentUtils.isStringValid(lhs.getDueDate())) {
                        return -1;
                    } else if (!ContentUtils.isStringValid(rhs.getDueDate())) {
                        return 1;
                    }
                    return DateUtils.dateFromString(lhs.getDueDate()).compareTo(DateUtils.dateFromString(rhs.getDueDate()));
                } else {
                    if (!ContentUtils.isStringValid(rhs.getDueDate()) && !ContentUtils.isStringValid(lhs.getDueDate())) {
                        return 0;
                    } else if (!ContentUtils.isStringValid(rhs.getDueDate())) {
                        return -1;
                    } else if (!ContentUtils.isStringValid(lhs.getDueDate())) {
                        return 1;
                    }
                    return DateUtils.dateFromString(rhs.getDueDate()).compareTo(DateUtils.dateFromString(lhs.getDueDate()));
                }
            }
        });
        this.notifyDataSetChanged();
    }

    protected class ViewHolder {

        @Bind(R.id.survey_number)
        TextView surveyNumber;

        @Bind(R.id.survey_state)
        TextView surveyState;

        @Bind(R.id.survey_due_date)
        TextView surveyDueDate;

        @Bind(R.id.survey_creation_date)
        TextView surveyCreationDate;

        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }
    }
}
