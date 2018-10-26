package com.abinbev.dsa.ui.view.negotiation;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Negotiation_Item__c;
import com.abinbev.dsa.model.Negotiation_Limit__c;
import com.abinbev.dsa.ui.view.PointMeter;

import java.text.NumberFormat;
import java.util.List;


import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wandersonblough on 1/11/16.
 */
public class PointScoreView extends RelativeLayout {

    @Bind(R.id.points_meter)
    PointMeter pointMeter;
//
//    @Bind(R.id.give_points)
//    TextView givePoints;
//
//    @Bind(R.id.get_points)
//    TextView getPoints;

//    @Bind(R.id.num_of_gets)
//    TextView numOfGets;
//
//    @Bind(R.id.num_of_gives)
//    TextView numOfGives;

    @Bind(R.id.inversion_total)
    TextView inversionTotal;

    @Bind(R.id.upper_limit)
    TextView upperLimit;

    @Bind(R.id.lower_limit)
    TextView lowerLimit;

    @Bind(R.id.pesos)
    TextView pesos;

    int gives, gets, givesValue;
    int givePts, getPts;

    public String prospectId;

    public String pesosString;
    public int pesosVal;

    public PointScoreView(Context context) {
        this(context, null);
    }

    public PointScoreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PointScoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.point_score_view, this);
        ButterKnife.bind(this);
        init();
    }

    public void setGivesGets(List<Negotiation_Item__c> gives, List<Negotiation_Item__c> gets) {
        this.gives = gives.size();
        this.gets = gets.size();
        givePts = getPoints(gives);
        getPts = getPoints(gets);
//        int total = 0;
//        for (Negotiation_Item__c give:gives) {
//            total += Integer.parseInt(give.getQuantity());
//        }
//        givesQuantity = total;
        givesValue = getSumOfAllGiveItems(gives);
        init();

        int sumOfGetItems = getSumOfGetItems(gets);
        if (sumOfGetItems != 0) {
            pesosVal = getSumOfGiveItems(gives) / sumOfGetItems;
        } else {
            // not sure whether this is the right way or not but ...
            pesosVal = getSumOfGiveItems(gives);
        }

        pesosString = Integer.toString(pesosVal);

        pointMeter.setPesos(pesosVal);
        pesos.setText(String.format(getResources().getString(R.string.pesos_caja), pesosString));
    }

    public void setProspectId(String prospectId) {
        this.prospectId = prospectId;
        Account prospect;
        if (prospectId != null) {
            prospect = Account.getById(prospectId);
            // get upper and lower limits
            Negotiation_Limit__c limit = Negotiation_Limit__c.fetchNegotiationLimitForAccount(prospect);
            if (limit != null) {
                int lowLimit = limit.getLimitLow();
                int highLimit = limit.getLimitHigh();
                pointMeter.setLimits(lowLimit, highLimit);

                upperLimit.setText(Integer.toString(highLimit));
                lowerLimit.setText(Integer.toString(lowLimit));
            }
        }
    }

    private void init() {
        pointMeter.setGivesGets(givePts, getPts);
//        givePoints.setText(getResources().getQuantityString(R.plurals.points, givePts, givePts));
//        getPoints.setText(getResources().getQuantityString(R.plurals.points, getPts, getPts));

//        numOfGets.setText(getResources().getQuantityString(R.plurals.gets, gets, gets));
//        numOfGives.setText(getResources().getQuantityString(R.plurals.gives, gives, gives));
        String inversionTotalString = "$" + NumberFormat.getInstance().format(Long.parseLong(String.valueOf(givesValue)));
        inversionTotal.setText(inversionTotalString);

    }

    private int getPoints(List<? extends Negotiation_Item__c> negotionItems) {
        int points = 0;
        for (Negotiation_Item__c negotiationItem : negotionItems) {
            Material__c material__c = negotiationItem.material__c;
            int materialPoints;
            try {
                materialPoints = Integer.valueOf(material__c.getScore());
            } catch(NumberFormatException nfe) {
                materialPoints = 0;
            }
            int score = TextUtils.isEmpty(material__c.getScore()) ? 0 : materialPoints;
            points += score;
        }
        return points;
    }

    private int getSumOfGetItems (List<Negotiation_Item__c> items) {
        int quantity = 1;
        for (Negotiation_Item__c item : items) {
            Material__c material__c = item.material__c;
            if (material__c.getCalculation().equalsIgnoreCase("yes")) {
                try {
                    quantity = quantity * (TextUtils.isEmpty(item.getQuantity()) ? 1 : Integer.valueOf(item.getQuantity()));
                } catch (NumberFormatException nfe) {
                // Ignore the values if they are not valid integer values
                }
            }
        }
        return quantity;
    }

    private int getSumOfGiveItems (List<Negotiation_Item__c> items) {
        int quantity = 0;
        for (Negotiation_Item__c item : items) {
            Material__c material__c = item.material__c;
            if (material__c.getCalculation().equalsIgnoreCase("yes")) {
                try {
                    int quantityPoints = (TextUtils.isEmpty(item.getQuantity()) ? 0 : Integer.valueOf(item.getQuantity())) * Integer.valueOf(material__c.getPoints());
                    quantity = quantity + quantityPoints;
                } catch (NumberFormatException nfe) {
                    // Ignore the quantity and points if they are not valid integer values
                }
            }
        }
        return quantity;
    }

    private int getSumOfAllGiveItems (List<Negotiation_Item__c> items) {
        int quantity = 0;
        for (Negotiation_Item__c item : items) {
            Material__c material__c = item.material__c;
            try {
                int quantityPoints = (TextUtils.isEmpty(item.getQuantity()) ? 0 : Integer.valueOf(item.getQuantity())) * Integer.valueOf(material__c.getPoints());
                quantity = quantity + quantityPoints;
            } catch (NumberFormatException nfe) {
                // Ignore the quantity and points if they are not valid integer values
            }
        }
        return quantity;
    }
}
