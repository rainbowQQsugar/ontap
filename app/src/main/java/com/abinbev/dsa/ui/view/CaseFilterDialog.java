package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Case;
import com.salesforce.androidsyncengine.data.model.PicklistValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A custom dialog to handle the filter criteria for {@link com.abinbev.dsa.model.Case}
 */
public class CaseFilterDialog extends AppCompatDialog {

    @Bind(R.id.cases_States)
    Spinner caseStatusSpinner;
    ArrayAdapter<PicklistValue> caseStatusAdapter;

    @Bind(R.id.cases_recordType)
    Spinner caseTypeSpinner;
    ArrayAdapter<PicklistValue> caseTypeAdapter;

    private CaseFilterDialogListener listener;

    public interface CaseFilterDialogListener {
        void onDialogPositiveClick(CaseFilterSelection caseFilterSelection);
    }

    public class CaseFilterSelection {
        public String caseStatus = "";
        public String caseType = "";
    }

    public CaseFilterDialog(Context context, CaseFilterDialogListener listener, List<Case> cases) {
        super(context, R.style.AppCompatAlertDialogStyle);
        setContentView(R.layout.case_filter);
        ButterKnife.bind(this);

        this.listener = listener;

        setupCaseStatesSpinnerWithCases(cases);
        setupCaseRecordNameSpinnerWithCases(cases);
    }

    private void setupCaseStatesSpinnerWithCases(List<Case> cases) {
        PicklistValue allValues = new PicklistValue();
        allValues.setLabel(getContext().getString(R.string.allStates));
        allValues.setValue(null);
        HashMap<String, Case> statesValues = new HashMap<>();
        List<PicklistValue> picklistValues = new ArrayList<PicklistValue>();
        picklistValues.add(0, allValues);
        for (Case tmpCase : cases){
            String state = tmpCase.getStatus();
            if (!statesValues.containsKey(state)){
                statesValues.put(state, tmpCase);
                PicklistValue pickListValue = new PicklistValue();
                pickListValue.setLabel(tmpCase.getTranslatedStatus());
                pickListValue.setValue(state);
                picklistValues.add(pickListValue);
            }
        }
        caseStatusAdapter = createAdapter(picklistValues);
        caseStatusSpinner.setAdapter(caseStatusAdapter);
        caseStatusSpinner.setSelection(Adapter.NO_SELECTION, false);
    }

    private void setupCaseRecordNameSpinnerWithCases(List<Case> cases) {
        PicklistValue allValues = new PicklistValue();
        allValues.setLabel(getContext().getString(R.string.allRecordTypes));
        allValues.setValue(null);
        HashMap<String, Case> recordNameValues = new HashMap<>();
        List<PicklistValue> picklistValues = new ArrayList<PicklistValue>();
        picklistValues.add(0, allValues);
        for (Case tmpCase : cases){
            String recordName = tmpCase.getTranslatedRecordName();
            if (!recordNameValues.containsKey(recordName)){
                recordNameValues.put(recordName, tmpCase);
                PicklistValue pickListValue = new PicklistValue();
                pickListValue.setLabel(recordName);
                pickListValue.setValue(tmpCase.getRecordTypeId());
                picklistValues.add(pickListValue);
            }
        }
        caseTypeAdapter = createAdapter(picklistValues);
        caseTypeSpinner.setAdapter(caseTypeAdapter);
        caseTypeSpinner.setSelection(Adapter.NO_SELECTION, false);
    }

    private ArrayAdapter<PicklistValue> createAdapter(List<PicklistValue> picklistValues) {
        return new ArrayAdapter<PicklistValue>(getContext(),
                R.layout.twoline_spinner_item, android.R.id.text1, picklistValues) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                TextView textView = (TextView) inflater.inflate(R.layout.dropdown_text_item, parent, false);
                PicklistValue picklistValue = getItem(position);
                textView.setText(picklistValue.getLabel());
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    view = inflater.inflate(R.layout.twoline_spinner_item, parent, false);
                }
                PicklistValue picklistValue = getItem(position);
                ((TextView) view.findViewById(android.R.id.text1)).setText(picklistValue.getLabel());
                return view;
            }
        };
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void setCaseStatusSelection(String selectedOrderStatus) {
//        caseStatusSpinner.setSelection(caseStatusAdapter.getPosition(selectedOrderStatus));
    }

    public void setCaseTypeSelection(String selectedOrderType) {
//        caseTypeSpinner.setSelection(caseTypeAdapter.getPosition(selectedOrderType));
    }

    @OnClick(R.id.cancel)
    @SuppressWarnings("unused")
    void onCancelClicked() {
        dismiss();
    }

    @OnClick(R.id.apply)
    @SuppressWarnings("unused")
    void onApplyClicked() {
        CaseFilterSelection caseFilterSelection = new CaseFilterSelection();
        caseFilterSelection.caseType = ((PicklistValue) caseTypeSpinner.getSelectedItem()).getValue();
        caseFilterSelection.caseStatus = ((PicklistValue) caseStatusSpinner.getSelectedItem()).getValue();

        listener.onDialogPositiveClick(caseFilterSelection);
        dismiss();
    }


}
