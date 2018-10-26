package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.OnClick;
import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.AccountCasesListAdapter;
import com.abinbev.dsa.model.Case;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.ui.presenter.AccountCasesListPresenter;
import com.abinbev.dsa.ui.view.CaseFilterDialog;
import com.abinbev.dsa.ui.view.SortableHeader;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.PermissionManager;
import com.salesforce.androidsyncengine.data.model.PicklistValue;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AccountCasesListActivity extends AppBaseDrawerActivity implements AccountCasesListPresenter.ViewModel, CaseFilterDialog.CaseFilterDialogListener {
    public static final String ARGS_ACCOUNT_ID = "account_id";
    public static final String DISPLAY_USER_CASES = "display_user_cases";

    private AccountCasesListPresenter casesListPresenter;
    private AccountCasesListAdapter casesListAdapter;
    private String accountId;
    private Boolean shouldDisplayUserCases;
    private PopupMenu popupMenu;
    private List<String> actions;

    @Bind(R.id.add_caso)
    FloatingActionButton fab;

    @Bind(R.id.popup_menu_anchor)
    View popupMenuAnchor;

    @Bind(R.id.cases_list)
    ListView listView;

    @Nullable
    @Bind(R.id.cases_States)
    Spinner caseStatesSpinner;

    @Nullable
    @Bind(R.id.cases_recordType)
    Spinner caseRecordNameSpinner;

    @Nullable
    @Bind({ R.id.case_Name, R.id.case_recordName, R.id.case_sla1_header, R.id.case_State})
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
            accountId = intent.getStringExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA);
//            shouldDisplayUserCases = intent.getBooleanExtra(CasesListActivity.DISPLAY_USER_CASES, false);
//            if (shouldDisplayUserCases){
//                titleTextView.setText(R.string.title_activity_my_caso);
//                fab.setVisibility(View.GONE);
//            }
        }
        casesListAdapter = new AccountCasesListAdapter();
        listView.setAdapter(casesListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Case caso = casesListAdapter.getItem(position);
                launchDynamicCasoViewIntent(caso);
            }
        });
        popupMenu = new PopupMenu(this, popupMenuAnchor);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item != null) {
                    launchNewCase(item.getItemId());
                }
                return true;
            }
        });

        if (!PermissionManager.getInstance().hasPermission(PermissionManager.CREATE_CASES)) {
            fab.setVisibility(View.GONE);
        }

    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_case_list_view;
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
        String userId = null;
//        if (shouldDisplayUserCases){
//            userId = UserAccountManager.getInstance().getStoredUserId();
//        }
        checkAccount(accountId);
        if (casesListPresenter == null) {
            casesListPresenter = new AccountCasesListPresenter(accountId);
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
            intent.putExtra(CasoViewActivity.ACCOUNT_ID_EXTRA, accountId);
            startActivity(intent);
//        }
    }

    @Override
    public void setNewCaseRecordTypes(List<String> recordTypes) {
        popupMenu.getMenu().clear();
        actions = recordTypes;
        for (int i= 0; i < recordTypes.size(); i++) {
            popupMenu.getMenu().add(Menu.NONE, i, i, recordTypes.get(i));
        }
    }

    @OnClick(R.id.add_caso)
    @SuppressWarnings("unused")
    public void onNewCasoClick() {
        popupMenu.show();
    }

    private void launchNewCase(int position) {
        String selectedActionLabel = actions.get(position);
        Intent intent;
        //TODO: Fix this incorrect logic
        if (selectedActionLabel != null && AbInBevConstants.AssetActionFields.ACTION_ASSET_CASE.equals(selectedActionLabel)) {
            intent = new Intent(this, AssetCaseEditActivity.class);
        } else {
            intent = new Intent(this, CasoEditActivity.class);
        }
        intent.putExtra(CasoEditActivity.CASO_RECORD_TYPE, selectedActionLabel);
        intent.putExtra(CasoEditActivity.ACCOUNT_ID, accountId);
        startActivityForResult(intent, CasoEditActivity.CASO_EDIT_REQUEST_CODE);
    }

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
