package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.UserCasesListAdapter;
import com.abinbev.dsa.model.Case;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.ui.presenter.UserCasesListPresenter;
import com.abinbev.dsa.ui.view.CaseFilterDialog;
import com.abinbev.dsa.ui.view.SortableHeader;
import com.salesforce.androidsyncengine.data.model.PicklistValue;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class UserCasesListActivity extends AppBaseDrawerActivity implements UserCasesListPresenter.ViewModel, CaseFilterDialog.CaseFilterDialogListener {
    public static final String ARGS_USER_ID = "user_id";

    private UserCasesListPresenter casesListPresenter;
    private UserCasesListAdapter casesListAdapter;
    private String userId;

    @Bind(R.id.cases_list)
    ListView listView;

    @Nullable
    @Bind(R.id.cases_States)
    Spinner caseStatesSpinner;

    @Nullable
    @Bind(R.id.cases_recordType)
    Spinner caseRecordNameSpinner;

    @Nullable
    @Bind({ R.id.case_Name, R.id.case_recordName, R.id.case_sla1_header, R.id.case_State, R.id.case_account})
    List<SortableHeader> sortableHeaders;

    @Nullable
    @Bind(R.id.filter)
    ImageView filter;

    CaseFilterDialog caseFilterDialog;
    CaseFilterDialog.CaseFilterSelection caseFilterSelection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("");
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra(ARGS_USER_ID);
        }
        casesListAdapter = new UserCasesListAdapter();
        listView.setAdapter(casesListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Case caso = casesListAdapter.getItem(position);
                launchDynamicCasoViewIntent(caso);
            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_user_case_list_view;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    protected void onPause() {
        super.onPause();
        casesListPresenter.stop();
    }

    @Override
    public void onRefresh() {
        if (casesListPresenter == null) {
            casesListPresenter = new UserCasesListPresenter(userId);
        }
        casesListPresenter.setViewModel(this);
        casesListPresenter.start();
    }

    @Override
    public void setData(List<Case> cases) {
        casesListAdapter.setData(cases);
        if (caseStatesSpinner != null) {
            setupCaseStatesSpinnerWithCases(cases);
        }

        if (caseRecordNameSpinner != null) {
            setupCaseRecordNameSpinnerWithCases(cases);
        }

        if (filter != null) {
            caseFilterDialog = new CaseFilterDialog(this, this, cases);
        }
    }

    public void onCaseNameHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        casesListAdapter.sortByName(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @Nullable
    @OnClick(R.id.case_recordName)
    public void onCaseRecordNameHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        casesListAdapter.sortByRecordName(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @Nullable
    @OnClick(R.id.case_sla1_header)
    public void onCaseScheduledDateHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        casesListAdapter.sortBySLA1(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @Nullable
    @OnClick(R.id.case_State)
    public void onCaseStateHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        casesListAdapter.sortByState(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @Nullable
    @OnClick(R.id.case_account)
    public void onCaseAccountHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        casesListAdapter.sortByAccount(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    /**
     * Clears the sort order on all headers in the group EXCEPT for the id of the view passed in
     * @param sortableHeaderId - the id to NOT clear the sort on
     */
    private void clearSortOnAllOthers(int sortableHeaderId) {
        for (SortableHeader sortableHeader : sortableHeaders) {
            if (sortableHeader.getId() != sortableHeaderId) {
                sortableHeader.clearSortIndicator();
               }
        }
    }

    private void setupCaseStatesSpinnerWithCases(List<Case> cases) {
        PicklistValue allValues = new PicklistValue();
        allValues.setLabel(getString(R.string.allStates));
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
        ArrayAdapter<PicklistValue> adapter = new ArrayAdapter<PicklistValue>(this,
                R.layout.twoline_spinner_item, android.R.id.text1, picklistValues) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                TextView textView = (TextView) inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
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
        caseStatesSpinner.setAdapter(adapter);
        caseStatesSpinner.setSelection(Adapter.NO_SELECTION, false);
        caseStatesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PicklistValue item = (PicklistValue) parent.getAdapter().getItem(position);
                casesListAdapter.filterByState(item.getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupCaseRecordNameSpinnerWithCases(List<Case> cases) {
        PicklistValue allValues = new PicklistValue();
        allValues.setLabel(getString(R.string.allRecordTypes));
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

        ArrayAdapter<PicklistValue> adapter = new ArrayAdapter<PicklistValue>(this,
                R.layout.twoline_spinner_item, android.R.id.text1, picklistValues) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                TextView textView = (TextView) inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
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
        caseRecordNameSpinner.setAdapter(adapter);
        caseRecordNameSpinner.setSelection(Adapter.NO_SELECTION, false);
        caseRecordNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PicklistValue item = (PicklistValue) parent.getAdapter().getItem(position);
                casesListAdapter.filterByRecordTypeId(item.getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Nullable
    @OnClick(R.id.filter)
    @SuppressWarnings("unused")
    public void onFilterClicked() {
        if (caseFilterSelection != null) {
            caseFilterDialog.setCaseStatusSelection(caseFilterSelection.caseStatus);
            caseFilterDialog.setCaseTypeSelection(caseFilterSelection.caseType);
        }
        caseFilterDialog.show();
    }

    private void launchDynamicCasoViewIntent(Case Case) {
        onCaseClick(Case);
    }

    @Override
    public void setRecordType(Case caso, RecordType recordType) {
        viewCaseByRecordType(caso, recordType.getName());
    }

    public void onCaseClick(Case caso) {
        String recordName = caso.getRecordName();
        if (recordName == null && caso.getRecordTypeId() != null) {
            // un-synced case: need to lookup the record name since names will not be filled until
            // after a succesful sync.
            casesListPresenter.getRecordType(caso);
            return;
        }
        viewCaseByRecordType(caso, recordName);
    }

    private void viewCaseByRecordType(Case caso, String recordName) {
//        if (Case.isAssetCaseRecordType(recordName)) {
//            Intent intent = new Intent(this, AssetCaseDetailActivity.class);
//            if (CasosFields.CASO_DE_ACTIVOS.equals(recordName)) {
//                intent.putExtra(AssetCaseDetailActivity.CASO_ID_EXTRA, caso.getParentId());
//            } else {
//                intent.putExtra(AssetCaseDetailActivity.CASO_ID_EXTRA, caso.getId());
//            }
//            startActivity(intent);
//        } else {
            Intent intent = new Intent(this, CasoViewActivity.class);
            intent.putExtra(CasoViewActivity.CASO_ID_EXTRA, caso.getId());
            startActivity(intent);
//        }
    }

    @Override
    public void setNewCaseRecordTypes(List<String> recordTypes) { }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CasoEditActivity.CASO_EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, R.string.case_save_success, Toast.LENGTH_SHORT).show();
            SyncUtils.TriggerRefresh(this);
        }
    }

    @Override
    public void onDialogPositiveClick(CaseFilterDialog.CaseFilterSelection caseFilterSelection) {
        casesListAdapter.filterByRecordTypeId(caseFilterSelection.caseType);
        casesListAdapter.filterByState(caseFilterSelection.caseStatus);
    }
}
