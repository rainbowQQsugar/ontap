package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;

import com.abinbev.dsa.R;
import com.abinbev.dsa.dynamiclist.AbstractDynamicListActivity;
import com.abinbev.dsa.dynamiclist.SimpleDataProvider;
import com.abinbev.dsa.dynamiclist.SimpleLabelProvider;
import com.abinbev.dsa.model.Case;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.CaseFields;
import com.abinbev.dsa.utils.AbInBevConstants.CaseStatus;
import com.abinbev.dsa.utils.AbInBevConstants.RecordTypeName;
import com.abinbev.dsa.utils.datamanager.FormatValues;
import com.salesforce.dsa.data.model.SFBaseObject;

import static com.abinbev.dsa.utils.datamanager.DataManagerUtils.format;

public class RejectedAccountChangesActivity extends AbstractDynamicListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageTitle(R.string.account_case_rejected_cases);

        setObjectName(AbInBevObjects.CASE);
        setFieldNames(
                CaseFields.POC_CURRENT_NAME,
                CaseFields.POC_REJECT_REASON
        );

        setDataProvider(createDataProvider());

        setFieldLabelProvider(new SimpleLabelProvider()
            .addLabel(CaseFields.POC_CURRENT_NAME, R.string.account)
            .addLabel(CaseFields.POC_REJECT_REASON, R.string.account_case_reject_reason));
    }

    @Override
    protected void onItemClicked(SFBaseObject item) {
        super.onItemClicked(item);
        startActivity(new Intent(this, CasoViewActivity.class)
            .putExtra(CasoViewActivity.CASO_ID_EXTRA, item.getId()));
    }

    private SimpleDataProvider createDataProvider() {
        FormatValues fv = new FormatValues()
                .addAll(Case.OBJECT_FORMAT_VALUES)
                .putValue("accountChangeRequest", RecordTypeName.ACCOUNT_CHANGE_REQUEST)
                .putValue("rejected", CaseStatus.REJECTED);

        String queryFilter =
                "{Case:RecordType.Name} = '{accountChangeRequest}'" +
                    " AND {Case:Status} = '{rejected}'";

        return new SimpleDataProvider<>(Case.class)
                .setQueryFilter(format(queryFilter, fv));
    }
}