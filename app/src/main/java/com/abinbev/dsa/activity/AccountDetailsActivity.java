package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.RecordTypeName;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import com.salesforce.dsa.utils.DSAConstants;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class AccountDetailsActivity extends DynamicViewActivity {

    public static final String ACCOUNT_ID_EXTRA = "account_id";

    private String accountId;

    @Bind(R.id.top_button2)
    TextView sendPOCClosureRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_activity_account);
        accountId = getIntent().getStringExtra(ACCOUNT_ID_EXTRA);

        refresh();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.dynamic_main;
    }

    @Override
    protected List<String> filterFields() {
        List<String> filterFields = new ArrayList<>();
        filterFields.add(AbInBevConstants.AccountFields.PARENT_ID);
        return filterFields;
    }

    private void refresh() {
        Account account = new Account(DataManagerFactory.getDataManager().exactQuery(DSAConstants.DSAObjects.ACCOUNT, "Id", accountId));
        getSupportActionBar().setSubtitle(account.getName());

        buildLayout(AbInBevObjects.ACCOUNT, account);

        if(account.isProspect()) {
            sendPOCClosureRequest.setVisibility(View.GONE);
        }
        else {
            sendPOCClosureRequest.setText(R.string.request_account_info_change);
            sendPOCClosureRequest.setEnabled(true);
            sendPOCClosureRequest.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.top_button2)
    public void onRequestChangeClicked() {
        startActivityForResult(
                new Intent(this, CasoEditActivity.class)
                    .putExtra(CasoEditActivity.ACCOUNT_ID, accountId)
                    .putExtra(CasoEditActivity.CASO_RECORD_TYPE, RecordTypeName.ACCOUNT_CHANGE_REQUEST),
                CasoEditActivity.CASO_EDIT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CasoEditActivity.CASO_EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            SyncUtils.TriggerRefresh(this);
        }
    }
}
