package com.abinbev.dsa.ui.presenter;

import com.abinbev.dsa.model.SurveyTaker__c;
import com.abinbev.dsa.model.Survey__c;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukaszwalukiewicz on 23.12.2015.
 */
public class UserSurveysListPresenter extends AbstractSurveysListPresenter {
    public static final String TAG = UserSurveysListPresenter.class.getSimpleName();

    private String userId;

    public UserSurveysListPresenter(String userId) {
        super();
        this.userId = userId;
    }


    protected List<SurveyTaker__c> getSurveysTakers() {
        List<SurveyTaker__c> surveys = SurveyTaker__c.getSurveysByUserId(userId);

        // Preload accounts, they will be needed later.
        for (SurveyTaker__c surveyTaker : surveys) {
            surveyTaker.getAccount();
        }

        return surveys;
    }

    protected List<Survey__c> getSurveyTypes() {
        ArrayList<Survey__c> surveyTypes = new ArrayList<>();
        return surveyTypes;
    }
}
