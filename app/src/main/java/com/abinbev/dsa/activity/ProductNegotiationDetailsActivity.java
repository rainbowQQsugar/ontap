package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.NotesListAdapter;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.model.Note;
import com.abinbev.dsa.model.Product;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.ui.customviews.ExpandedListView;
import com.abinbev.dsa.ui.presenter.NewPromotionEditPresenter;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.DistributionFields;
import com.abinbev.dsa.utils.AbInBevConstants.ProductFields;
import com.abinbev.dsa.utils.AbInBevConstants.ProductNegotiationFields;
import com.abinbev.dsa.utils.DateUtils;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.salesforce.androidsyncengine.data.layouts.Details;
import com.salesforce.androidsyncengine.data.layouts.LayoutItem;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Diana Błaszczyk on 19/10/17.
 */
public class ProductNegotiationDetailsActivity extends DynamicEditActivity implements NewPromotionEditPresenter.ViewModel,
        NotesListAdapter.NoteClickHandler {

    private static final String TAG = "NewPromotionEditActivit";

    public static final String ARGS_NEGOTIATION_ID = "negotiation_id";
    public static final String ARGS_ACCOUNT_ID = "account_id";
    public static final String ARGS_RECORD_TYPE_ID = "record_type_id";
    public static final String NEW_NEGOTIATION = "new_negotiation";

    NewPromotionEditPresenter presenter;
    private NotesListAdapter adapter;

    String accountId;
    String negotiationId;
    String negotiationName = "";
    String recordType;
    boolean isProspect;

    @Bind(R.id.notes_list)
    ExpandedListView notesList;

    @Bind(R.id.new_note)
    TextView newNoteButton;

    @Bind(R.id.time_layout)
    LinearLayout timeLayout;

    @Bind(R.id.tv_start_time)
    TextView startTime;

    @Bind(R.id.tv_end_time)
    TextView endTime;

    @Bind(R.id.negotiation_history_section)
    LinearLayout negotiationHistorySection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.negociaciones);

        Intent intent = getIntent();
        negotiationId = intent.getStringExtra(ARGS_NEGOTIATION_ID);
        recordType = intent.getStringExtra(ARGS_RECORD_TYPE_ID);
        accountId = intent.getStringExtra(ARGS_ACCOUNT_ID);


        if (NEW_NEGOTIATION.equals(negotiationId)) {
            negotiationId = null;
            negotiationHistorySection.setVisibility(View.GONE);
        } else {
            timeLayout.setVisibility(View.VISIBLE);
        }

        presenter = new NewPromotionEditPresenter(negotiationId, accountId, recordType);
        adapter = new NotesListAdapter(this);
        notesList.setExpanded(true);
        notesList.setAdapter(adapter);
        notesList.setHeaderDividersEnabled(false);
        notesList.setFooterDividersEnabled(false);
    }

    @Override
    protected boolean isUpdateable(LayoutItem layoutItem, Details details, String fieldName, String section) {
        if (ProductNegotiationFields.ACCOUNT.equals(fieldName)) {
            return false;
        } else {
            return super.isUpdateable(layoutItem, details, fieldName, section);
        }
    }

    @Override
    protected boolean isRequired(LayoutItem layoutItem, Details details, String fieldName) {
        return ProductNegotiationFields.PROMOTION_TYPE.equals(fieldName) || ProductNegotiationFields.PRODUCT.equals(fieldName);
    }

    @Override
    protected String getLookupFilter(String fieldName, String referredObjectType) {
        if (ProductNegotiationFields.PRODUCT.equals(fieldName)) {
            if (recordType.equals(RecordType.getByName(AbInBevConstants.NegotiationType.SELL_IN).getId()) || isProspect) {
                String smartSqlFilter = String.format("{%1$s:%2$s} = '%3$s'",
                        AbInBevConstants.AbInBevObjects.ACCOUNT, AbInBevConstants.ID, accountId);
                String smartSql = String
                        .format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.ACCOUNT, smartSqlFilter);
                Account account = DataManagerUtils.fetchObject(DataManagerFactory.getDataManager(), smartSql, Account.class);
                return String.format("{%1$s:%2$s} IN (SELECT {%3$s:%4$s} FROM {%3$s} WHERE {%3$s:%5$s} = '%6$s' AND {%3$s:%7$s} = '%8$s' )",
                        AbInBevObjects.PRODUCT, ProductFields.ID,
                        AbInBevObjects.CN_SKU_TC_Relationship__c, AbInBevConstants.SkuTcRelationshipFields.PRODUCT,
                        AbInBevConstants.SkuTcRelationshipFields.CHANNEL, account.getChannel(),
                        AbInBevConstants.SkuTcRelationshipFields.TERRITORY, account.getCityRegion());
            } else {
                return String.format("{%1$s:%2$s} IN (SELECT {%3$s:%4$s} FROM {%3$s} WHERE {%3$s:%5$s} = '%7$s' AND {%3$s:%6$s} = 'true')",
                        AbInBevObjects.PRODUCT, ProductFields.ID,
                        AbInBevObjects.CN_DISTRIBUTION, DistributionFields.CN_PRODUCT, DistributionFields.POC_ID, DistributionFields.IS_ACTIVE,
                        accountId);
            }
        } else {
            return super.getLookupFilter(fieldName, referredObjectType);
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_product_negotiation_details;
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.stop();
    }

    @Override
    protected void setLayoutComponentData(View contentView, String viewType, String fieldName, CharSequence label, String value) {
        super.setLayoutComponentData(contentView, viewType, fieldName, label, value);
        if (ProductNegotiationFields.PRODUCT.equals(fieldName)) {
            try {
                String productId = updatedJSONObject.getString(fieldName);
                Product product = Product.getById(productId);
                CN_Product_Negotiation__c negotiation = (CN_Product_Negotiation__c) baseObject;
                setNegotiation(negotiation, product);
            } catch (JSONException e) {
            }
        }
    }

    public void setNegotiation(CN_Product_Negotiation__c negotiation, Product product) {
        negotiation.setProductId(product.getId());
        negotiation.setPackage(product.getPackage());
        negotiation.setBrand(product.getChBrand());
        negotiation.setUnit(product.getProductUnit());
        setNegotiation(negotiation);
    }

    @Override
    public void setNotes(List<Note> notes) {
        adapter.setData(notes);
    }

    @Override
    public void setNegotiation(CN_Product_Negotiation__c negotiation) {
        if (TextUtils.isEmpty(negotiation.getId())) {
            getSupportActionBar().setSubtitle(R.string.new_negotiation);
        } else {
            getSupportActionBar().setSubtitle(negotiation.getName());
            startTime.setText(DateUtils.fromServerDateTimeToDate(negotiation.getStartTime()));
            endTime.setText(DateUtils.fromServerDateTimeToDate(negotiation.getEndTime()));
            negotiationName = negotiation.getName();
            recordType = negotiation.getRecordTypeId();
        }
        buildLayout(AbInBevObjects.PRODUCT_NEGOTIATIONS, negotiation);
    }

    @Override
    public void showNegotiationDoesNotExist(String negotiationId) {
        Toast.makeText(this, getString(R.string.error_no_negotiation_with_id, negotiationId), Toast.LENGTH_LONG).show();
    }

    @Override
    public void setAccount(Account account) {
        if (account != null) {
            isProspect = account.isProspect();
        }
    }

    @Override
    public void closeOnSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    @OnClick(R.id.new_note)
    public void onNewNoteClick() {
        Intent intent = new Intent(this, NewNoteActivity.class);
        intent.putExtra(NewNoteActivity.ARGS_ACCOUNT_ID, negotiationId);
        intent.putExtra(NewNoteActivity.ARGS_DISPLAY_TITLE, false);
        String title = new SimpleDateFormat("yyyy/MM/dd").format(new Date()) + " " + negotiationName;
        intent.putExtra(NewNoteActivity.ARGS_CUSTOM_TITLE, title);
        startActivity(intent);
    }


    @OnClick(R.id.save_button)
    void onSaveClicked() {
        CN_Product_Negotiation__c negotiation = (CN_Product_Negotiation__c) baseObject;
        Log.i(TAG, "original Values: " + negotiation);
        JSONObject updatedObject = getUpdatedJSONObject();
        Log.i(TAG, "updated Values: " + updatedObject.toString());
        try {
            if (negotiationId == null) {
                updatedObject.put(ProductNegotiationFields.START_TIME, DateUtils.fromDateTimeToServerDateTime(new Date()));
            }
            if (updatedObject.has("Status__c") && ((updatedObject.getString("Status__c").equals("Confirmed"))||
                    (updatedObject.getString("Status__c").equals("Cancelled"))||
                    (updatedObject.getString("Status__c").equals("Rejected")))) {
                updatedObject.put(ProductNegotiationFields.END_TIME, DateUtils.fromDateTimeToServerDateTime(new Date()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (testRequiredFields(updatedObject)) {
            addValues(negotiation, updatedObject);
            Log.i(TAG, "result: " + negotiation);
            presenter.saveNegotiation(negotiation);
        }
    }

    private void addValues(SFBaseObject originalObject, JSONObject updatedObject) {
        Iterator it = updatedObject.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            originalObject.setValueForKey(key, updatedObject.opt(key));
        }
    }

    @Override
    public void onNoteClick(String noteId) {

    }
}
