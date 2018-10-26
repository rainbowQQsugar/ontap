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

public class QuizAdapter extends BaseAdapter{
    List<SurveyTaker__c> quizzes;
    private boolean isSupervisor;
    public QuizAdapter() {
        super();
        quizzes = new ArrayList<>();
        isSupervisor = false;
    }

    public void setIsSupervisor(boolean isSupervisor) {
        this.isSupervisor = isSupervisor;
    }

    @Override
    public int getCount() {
        return quizzes.size();
    }

    @Override
    public Object getItem(int position) {
        return quizzes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.quiz_item, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }

        final SurveyTaker__c quiz = quizzes.get(position);
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        String quizNameString = quiz.getName() + "\n" + quiz.getSurveyName();
        if (isSupervisor) quizNameString = quizNameString + "\n" + quiz.getUserName();
        viewHolder.quizName.setText(quizNameString);
        viewHolder.quizState.setText(quiz.getState());

        String quizScoreString = "-";
        if (quiz.getTotalScore() != -1) {
            quizScoreString = String.valueOf(quiz.getTotalScore());
        }
        viewHolder.quizTotalScore.setText(quizScoreString);
        viewHolder.quizCreationDate.setText(DateUtils.formatDateTimeShort(quiz.getCreatedDate()));

        return convertView;
    }

    public void setData(List<SurveyTaker__c> surveys) {
        this.quizzes.clear();
        this.quizzes.addAll(surveys);
        this.notifyDataSetChanged();
    }

    public void sortByOrderNumber(final boolean ascending) {
        Collections.sort(quizzes, new Comparator<SurveyTaker__c>() {
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
        Collections.sort(quizzes, new Comparator<SurveyTaker__c>() {
            @Override
            public int compare(SurveyTaker__c lhs, SurveyTaker__c rhs) {
                if (ascending) {
                    return lhs.getState().compareTo(rhs.getState());
                } else {
                    return rhs.getState().compareTo(lhs.getState());
                }
            }
        });
        this.notifyDataSetChanged();
    }

    public void sortByCreateDate(final boolean ascending) {
        Collections.sort(quizzes, new Comparator<SurveyTaker__c>() {
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

    public void sortByTotalScore(final boolean ascending) {
        Collections.sort(quizzes, new Comparator<SurveyTaker__c>() {
            @Override
            public int compare(SurveyTaker__c lhs, SurveyTaker__c rhs) {
                if (ascending) {
                    return rhs.getTotalScore() - lhs.getTotalScore();
                } else {
                    return lhs.getTotalScore() - rhs.getTotalScore();
                }
            }
        });
        this.notifyDataSetChanged();
    }

    class ViewHolder {

        @Bind(R.id.quiz_name)
        TextView quizName;

        @Bind(R.id.quiz_state)
        TextView quizState;

        @Bind(R.id.quiz_total_score)
        TextView quizTotalScore;

        @Bind(R.id.quiz_creation_date)
        TextView quizCreationDate;

        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }
    }
}
