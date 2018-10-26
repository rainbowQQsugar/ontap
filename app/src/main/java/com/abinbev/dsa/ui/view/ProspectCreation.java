package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.AdditionalDataDynamicLayoutActivity;
import com.abinbev.dsa.activity.BasicDataDynamicLayoutActivity;
import com.abinbev.dsa.activity.ProductNegotiationDetailsActivity;
import com.abinbev.dsa.activity.PocAttachmentsActivity;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.utils.AbInBevConstants;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by mlangreder on 4/4/16.
 */
public class ProspectCreation extends LinearLayout{

    public interface OnConvertClickedListener {
        void onConvertClicked();

    }

    public interface OnUnqualifiedClickedListener {
        void onUnqualifiedClicked();
    }

    private static final float ALPHA_ENABLED = 1.0f;

    private static final float ALPHA_DISABLED = 0.66f;

    @Bind(R.id.tiles_container)
    View tilesContainer;

    @Bind(R.id.basic_data_container)
    View basicDataContainer;

    @Bind(R.id.basic_data_check)
    ImageView basicDataCheck;

    @Bind(R.id.additional_data_container)
    View additionalDataContainer;

    @Bind(R.id.additional_data_check)
    ImageView additionalDataCheck;

    @Bind(R.id.negotiation_container)
    View negotiationContainer;

    @Bind(R.id.negotiation_check)
    ImageView negotiationCheck;

    @Bind(R.id.attachment_container)
    View attachmentContainer;

    @Bind(R.id.attachment_check)
    ImageView attachmentCheck;

    @Bind(R.id.prospect_conversion_button)
    View prospectConversionButton;

    OnConvertClickedListener convertClickedListener;

    @Bind(R.id.prospect_unqualified_button)
    View prospectUnqualifiedButton;

    OnUnqualifiedClickedListener unqualifiedClickedListener;


    String prospectId;

    String negotiationId;

    Context context;

    public ProspectCreation(Context context) {
        this(context, null);
        this.context = context;
    }

    public ProspectCreation(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.context = context;
    }

    public ProspectCreation(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp(context);
        this.context = context;
    }

    private void setUp(Context context) {
        inflate(context, R.layout.merge_prospect_creation, this);
        setOrientation(LinearLayout.VERTICAL);
        ButterKnife.bind(this);
    }

    public String getProspectId() {
        return prospectId;
    }

    public void setProspectId(String prospectId) {
        this.prospectId = prospectId;
    }

    public String getNegotiationId() {
        return prospectId;
    }

    public void setNegotiationId(String negotiationId) {
        this.negotiationId = negotiationId;
    }


    @OnClick(R.id.basic_data_container)
    public void basicDataClicked() {
        Intent intent = new Intent(getContext(), BasicDataDynamicLayoutActivity.class);
        intent.putExtra(BasicDataDynamicLayoutActivity.ACCOUNT_ID_EXTRA, prospectId);
        getContext().startActivity(intent);
    }

    @OnClick(R.id.additional_data_container)
    public void additionalDataClicked() {
        // TODO: intent to additional info entry
        Intent intent = new Intent(getContext(), AdditionalDataDynamicLayoutActivity.class);
        intent.putExtra(AdditionalDataDynamicLayoutActivity.PROSPECT_ID_EXTRA, prospectId);
        getContext().startActivity(intent);
    }


    @OnClick(R.id.negotiation_container)
    public void negotiationClicked() {
        // TODO db calls on main thread
        Intent intent = new Intent(getContext(), ProductNegotiationDetailsActivity.class);
        intent.putExtra(ProductNegotiationDetailsActivity.ARGS_ACCOUNT_ID, prospectId);

        RecordType recordType = RecordType.getByName(AbInBevConstants.NegotiationType.SELL_IN);
        if (recordType != null)
            intent.putExtra(ProductNegotiationDetailsActivity.ARGS_RECORD_TYPE_ID, recordType.getId());

        List<CN_Product_Negotiation__c> negotiations = CN_Product_Negotiation__c.getNegotiationsByAccountId(prospectId);
        if (negotiations.size() > 0) negotiationId = negotiations.get(0).getId();

        if (negotiationId != null) {
            intent.putExtra(ProductNegotiationDetailsActivity.ARGS_NEGOTIATION_ID, negotiationId);
        } else {
            intent.putExtra(ProductNegotiationDetailsActivity.ARGS_NEGOTIATION_ID, ProductNegotiationDetailsActivity.NEW_NEGOTIATION);
        }
        getContext().startActivity(intent);
    }


    @OnClick(R.id.attachment_container)
    public void attachmentClicked() {
        Intent intent = new Intent(getContext(), PocAttachmentsActivity.class);
        intent.putExtra(PocAttachmentsActivity.ARGS_ACCOUNT_ID, prospectId);

        getContext().startActivity(intent);
    }

    public void showBasicDataCheck(boolean show) {
        basicDataCheck.setVisibility(show ? VISIBLE : GONE);
    }

    public void showAdditionalDataCheck(boolean show) {
        additionalDataCheck.setVisibility(show ? VISIBLE : GONE);
    }

    public void showNegotiationCheck(boolean show) {
        negotiationCheck.setVisibility(show ? VISIBLE : GONE);
    }

    public void showAttachmentCheck(boolean show) {
        attachmentCheck.setVisibility(show ? VISIBLE : GONE);
    }

    public void showBasicDataContainer(boolean show) {
        basicDataContainer.setVisibility(show ? VISIBLE : GONE);
    }

    public void showAdditionalDataContainer(boolean show) {
        additionalDataContainer.setVisibility(show ? VISIBLE : GONE);
    }

    public void showNegotiationContainer(boolean show) {
        negotiationContainer.setVisibility(show ? VISIBLE : GONE);
    }

    public void showAttachmentContainer(boolean show) {
        attachmentContainer.setVisibility(show ? VISIBLE : GONE);
    }

    public void showTilesContainer(boolean show) {
        tilesContainer.setVisibility(show ? VISIBLE : GONE);
    }

    public void setBasicDataEnabled(boolean enable) {
        basicDataContainer.setEnabled(enable);
        basicDataContainer.setAlpha(enable ? ALPHA_ENABLED : ALPHA_DISABLED);
        basicDataCheck.setAlpha(enable ? ALPHA_ENABLED : ALPHA_DISABLED);
    }

    public void setAdditionalDataEnabled(boolean enable) {
        additionalDataContainer.setEnabled(enable);
        additionalDataContainer.setAlpha(enable ? ALPHA_ENABLED : ALPHA_DISABLED);
        additionalDataCheck.setAlpha(enable ? ALPHA_ENABLED : ALPHA_DISABLED);
    }

    public void setNegotiationEnabled(boolean enable) {
        negotiationContainer.setEnabled(enable);
        negotiationContainer.setAlpha(enable ? ALPHA_ENABLED : ALPHA_DISABLED);
        negotiationCheck.setAlpha(enable ? ALPHA_ENABLED : ALPHA_DISABLED);
    }

    public void setAttachmentEnabled(boolean enable) {
        attachmentContainer.setEnabled(enable);
        attachmentContainer.setAlpha(enable ? ALPHA_ENABLED : ALPHA_DISABLED);
        attachmentCheck.setAlpha(enable ? ALPHA_ENABLED : ALPHA_DISABLED);
    }

    public void setConversionButtonEnabled(boolean enable) {
        prospectConversionButton.setEnabled(enable);
        prospectConversionButton.setAlpha(enable ? ALPHA_ENABLED : ALPHA_DISABLED);
    }

    public void setUnqualifiedButtonEnabled(boolean enable) {
        prospectUnqualifiedButton.setEnabled(enable);
        prospectUnqualifiedButton.setAlpha(enable ? ALPHA_ENABLED : ALPHA_DISABLED);
    }

    @OnClick(R.id.prospect_conversion_button)
    void onConversionButtonClicked() {
        if (convertClickedListener != null) {
            convertClickedListener.onConvertClicked();
        }
    }

    @OnClick(R.id.prospect_unqualified_button)
    void onUnqualifiedButtonClicked() {
        if (unqualifiedClickedListener != null) {
            unqualifiedClickedListener.onUnqualifiedClicked();
        }
    }

    public void setConvertClickedListener(OnConvertClickedListener l) {
        this.convertClickedListener = l;
    }

    public void setUnqualifiedClickedListener(OnUnqualifiedClickedListener l) {
        this.unqualifiedClickedListener = l;
    }
}
