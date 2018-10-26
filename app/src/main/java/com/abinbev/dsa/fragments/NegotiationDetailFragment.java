package com.abinbev.dsa.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Negotiation_Item__c;
import com.abinbev.dsa.model.Material_Get__c;
import com.abinbev.dsa.model.Negotiation_Limit__c;
import com.abinbev.dsa.model.Negotiation__c;
import com.abinbev.dsa.ui.view.negotiation.NegotiationDateHeaderView;
import com.abinbev.dsa.ui.view.negotiation.NegotiationDetailView;
import com.abinbev.dsa.ui.view.negotiation.NegotiationHelper;
import com.abinbev.dsa.ui.view.negotiation.NegotiationItemsView;
import com.abinbev.dsa.ui.view.negotiation.ObservationsView;
import com.abinbev.dsa.ui.view.negotiation.PointScoreView;
import com.abinbev.dsa.utils.PermissionManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wandersonblough on 2/1/16.
 */
public class NegotiationDetailFragment extends Fragment implements NegotiationDateHeaderView.SubmitListener {

    private static final String TAG = NegotiationDetailFragment.class.getSimpleName();
    @Bind(R.id.toolbar)
    Toolbar toolbar;

//    @Bind(R.id.packages_view)
//    PackagesView packagesView;

    @Bind(R.id.observation_view)
    ObservationsView observationsView;

    @Bind(R.id.header_view)
    NegotiationDateHeaderView headerView;

    @Bind(R.id.save)
    FloatingActionButton submitButton;

    @Bind(R.id.submit_buttons)
    ViewGroup submitButtons;

//    @Bind(R.id.submit_and_order)
//    TextView submitAndOrderBtn;

    @Bind(R.id.negotiation_item_views)
    NegotiationItemsView negotiationItemsView;

    @Bind(R.id.points_score_view)
    PointScoreView pointScoreView;

    @Bind(R.id.scroll_view)
    ScrollView scrollView;

    @Bind(R.id.negotiation_detail_view)
    NegotiationDetailView negotiationDetailView;

    @Bind(R.id.approval_title)
    TextView approvalTitle;

    private View mainLayout;

    private NegotiationHelper negotiationHelper;

    public NegotiationDetailFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainLayout = inflater.inflate(R.layout.fragment_negotiation_details, container, false);
        ButterKnife.bind(this, mainLayout);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.negociaciones);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        boolean canViewGauge = PermissionManager.getInstance().hasPermission(PermissionManager.NEGOTIATION_GAUGE);
        pointScoreView.setVisibility(canViewGauge ? View.VISIBLE : View.GONE);

        return mainLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        negotiationItemsView.setNegotiationHelper(negotiationHelper);
        observationsView.setNegotiationHelper(negotiationHelper);
        headerView.setSubmitListener(this);
        headerView.setupClassificationAdapter();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            negotiationHelper = (NegotiationHelper) getActivity();
        } catch (Exception e) {
            Log.e(TAG, "onAttach: ", e);
        }
    }

    public void setAccountId(String accountId) {
        pointScoreView.setProspectId(accountId);
    }

    public void setNew() {
        getSupportActionBar().setSubtitle(R.string.new_negotiation);
        updateUI(true);
//        packagesView.setProspectId(accountId);
        updateApprovalTitle();
    }

    public void setReadOnly(Negotiation__c negotiation) {
        getSupportActionBar().setSubtitle(negotiation.getName() == null ? getString(R.string.pending_approval) : negotiation.getName());
        updateUI(Negotiation__c.STATUS_SUBMITTED.equals(negotiation.getStatus()) ? true : false);
        observationsView.setObservation(negotiation.getObservations());
        negotiationDetailView.setPromotion(negotiation);
        headerView.setStartDate(negotiation.getStartDate());
        headerView.setEndDate(negotiation.getDeliveryDate());
        updateApprovalTitle();
    }

    private void updateApprovalTitle() {
        Account account = Account.getById(pointScoreView.prospectId);
        Negotiation_Limit__c limit = Negotiation_Limit__c.fetchNegotiationLimitForAccount(account);
        if (limit != null) {
            int lowLimit = limit.getLimitLow();
            int highLimit = limit.getLimitHigh();

            if (pointScoreView.pesosVal < lowLimit) { // in the green
                approvalTitle.setText(getString(R.string.green_approval));
            } else if (pointScoreView.pesosVal > highLimit) { //in the red
                approvalTitle.setText(getString(R.string.red_approval));
            } else { //must be yellow
                approvalTitle.setText(getString(R.string.yellow_approval));
            }
        }
    }

    private void updateUI(boolean editable) {
//        packagesView.setVisibility(editable ? View.VISIBLE : View.GONE);
        observationsView.setEditable(editable);
        headerView.setVisibility(editable ? View.VISIBLE : View.GONE);
        submitButtons.setVisibility(editable ? View.VISIBLE : View.GONE);
        negotiationItemsView.setEditable(editable);
        negotiationDetailView.setVisibility(editable ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.save)
    public void submit() {

        String[] buttonTitles = new String[]{getResources().getString(R.string.submit), getResources().getString(R.string.save_and_send)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("")
                .setItems(buttonTitles, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            negotiationHelper.saveSubmitNegotiation(false);
                        } else {
                            if (negotiationHelper.verifyGivesGetsQuantity()) {
                                negotiationHelper.saveSubmitNegotiation(true);
                            } else {
                                showSnackbar(getResources().getString(R.string.give_get_quantity_error));
                            }
                        }
                    }
                });
        builder.create().show();
    }

//    @OnClick(R.id.submit_and_order)
    public void submitAndOrder() {
//        negotiationHelper.submitNegotiation(true);
    }

    private void showSnackbar(String errorString) {

        final Snackbar snackbar = Snackbar.make(mainLayout, errorString, Snackbar.LENGTH_INDEFINITE);
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


    public ObservationsView getObservationsView() {
        return observationsView;
    }

    public void hideOrderButtons(boolean hideOrderButton) {
//        submitAndOrderBtn.setVisibility(hideOrderButton ? View.GONE : View.VISIBLE);
//        headerView.getSubmitAndOrderBtn().setVisibility(hideOrderButton ? View.GONE : View.VISIBLE);
    }

    public void hideSubmitButtons(boolean hideSubmitButton) {
        submitButton.setVisibility(hideSubmitButton ? View.GONE : View.VISIBLE);
//        headerView.getSubmitButton().setVisibility(hideSubmitButton ? View.GONE : View.VISIBLE);
    }

    public void setItems(List<Negotiation_Item__c> items) {
        List<Negotiation_Item__c> gives = new ArrayList<>();
        List<Negotiation_Item__c> gets = new ArrayList<>();
        for (Negotiation_Item__c item : items) {
            if (item.material__c instanceof Material_Get__c) {
                gets.add(item);
            } else {
                gives.add(item);
            }
        }
        negotiationItemsView.setNegotiationGets(gets);
        negotiationItemsView.setNegotiationGives(gives);
        pointScoreView.setGivesGets(gives, gets);

        negotiationHelper.setPesos(pointScoreView.pesosString);
        updateApprovalTitle();
    }

    public void setPointScore(List<Negotiation_Item__c> items) {
        List<Negotiation_Item__c> gives = new ArrayList<>();
        List<Negotiation_Item__c> gets = new ArrayList<>();
        for (Negotiation_Item__c item : items) {
            if (item.material__c instanceof Material_Get__c) {
                gets.add(item);
            } else {
                gives.add(item);
            }
        }
        pointScoreView.setGivesGets(gives, gets);
        updateApprovalTitle();
    }

    private ActionBar getSupportActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }
}
