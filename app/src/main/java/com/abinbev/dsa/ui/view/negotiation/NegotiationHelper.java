package com.abinbev.dsa.ui.view.negotiation;

import com.abinbev.dsa.adapter.GiveGetFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wandersonblough on 1/30/16.
 */
public interface NegotiationHelper {

    void updateItems(List<Material__c> items);

    void updateObservations(String observations);

    void viewPromoCodes();

    void viewGiveGetSearch(GiveGetFilter.Type type);

    void addPromoCodes(ArrayList<String> codes);

    void submitNegotiation(boolean placeOrder);

    void saveSubmitNegotiation(boolean lockNegoatiation);

    boolean verifyGivesGetsQuantity();

    void setPesos(String pesos);
}
