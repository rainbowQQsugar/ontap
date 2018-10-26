package com.abinbev.dsa.ui.view.negotiation;

/**
 * Created by wandersonblough on 12/14/15.
 */
public interface Material__c {

    String getId();

    String getCode();

    String getName();

    String getDescription();

    String getScore();

    String getCalculation();

    String getPoints();

    String getExclusive();

    String getGroup();

    String getComment();

    Material__c copy();
}
