package com.abinbev.dsa.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.AssetsListAdapter;
import com.abinbev.dsa.model.Account_Asset__c;
import com.abinbev.dsa.model.Caso;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.ui.presenter.RelatedCasesPresenter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Jason Harris (jason@akta.com)
 */
public class RelatedCaseView extends LinearLayout implements RelatedCasesPresenter.ViewModel {

    @Bind(R.id.serialized_container)
    LinearLayout serializedContainer;

    @Bind(R.id.unserialized_container)
    LinearLayout unserializedContainer;

    @Bind(R.id.title_serialized)
    LinearLayout titleSerialized;

    @Bind(R.id.title_unserialized)
    LinearLayout titleUnserialized;

    @Bind(R.id.header_serialized)
    TextView headerSerialized;

    @Bind(R.id.header_unserialized)
    TextView headerUnserialized;

    protected String casoId;
    protected RelatedCasesPresenter presenter;
    private List<Caso> relatedCases;
    private Map<String, Account_Asset__c> assets;
    private Map<String, RecordType> recordTypes;

    protected Map<String, String> assetCaseMap = new HashMap<>();

    public RelatedCaseView(Context context) {
        super(context);
        setUp(context);
    }

    public RelatedCaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp(context);
    }

    public RelatedCaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RelatedCaseView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setUp(context);
    }

    private void setUp(Context context) {
        inflate(context, R.layout.related_case_view, this);
        ButterKnife.bind(this);

        setOrientation(VERTICAL);
        if (isInEditMode()) {
            return;
        }


    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        presenter.stop();
    }

    public void setCasoId(String casoId) {
        this.casoId = casoId;
        if (presenter == null) {
            presenter = new RelatedCasesPresenter();
        }
        presenter.setCaseId(casoId);
        presenter.setViewModel(this);
        presenter.start();
    }

    private void setData() {
        assetCaseMap.clear();
        serializedContainer.removeAllViews();
        unserializedContainer.removeAllViews();
        for (Caso caso : relatedCases) {
            Account_Asset__c asset = assets.get(caso.getActivoPorClient());
            if (asset != null && asset.getRecordTypeId() != null) {
                RecordType recordType = recordTypes.get(asset.getRecordTypeId());
                if (recordType != null) {
                    if (AssetsListAdapter.SERIALIZED_RECORD_NAME.equals(recordType.getName())) {
                        createRow(asset, caso, serializedContainer, asset.getSerialNumber());
                    } else if (AssetsListAdapter.NO_SERIALIZED_RECORD_NAME.equals(recordType.getName())) {
                        createRow(asset, caso, unserializedContainer, asset.getCode());
                    }
                }
            }

        }

        hideEmptyViews(serializedContainer, titleSerialized, headerSerialized);
        hideEmptyViews(unserializedContainer, titleUnserialized, headerUnserialized);
    }

    private static void hideEmptyViews(LinearLayout container, View... views) {
        if (container.getChildCount() < 1) {
            for(View view : views) {
                view.setVisibility(View.GONE);
            }
        } else {
            for(View view : views) {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    protected View createRow(final Account_Asset__c asset, Caso caso, LinearLayout container, String firstField) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.related_asset_item, container, false);
        view.setBackgroundColor(getResources().getColor(R.color.sab_white));
        TextView series = (TextView) view.findViewById(R.id.series);
        series.setText(firstField);
        TextView quantity = (TextView) view.findViewById(R.id.quantity);
        quantity.setText(asset.getQuantity());
        TextView reqd = (TextView) view.findViewById(R.id.quantity_reqd);
        reqd.setText(caso.getQuantityRequired());
        reqd.setBackground(null);
        TextView brand = (TextView) view.findViewById(R.id.brand);
        brand.setText(asset.getBrand());
        TextView state = (TextView) view.findViewById(R.id.state);
        state.setText(asset.getStatus());
        container.addView(view);
        container.addView(createSeparator());
        assetCaseMap.put(asset.getId(), caso.getId());
        return view;
    }

    private View createSeparator() {
        View view = new View(getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.separator)));
        view.setBackgroundColor(getResources().getColor(R.color.sab_lightest_gray));
        return view;
    }


    @Override
    public void setRelatedCases(List<Caso> relatedCases) {
        this.relatedCases = relatedCases;
        presenter.getAssets(relatedCases);
    }

    @Override
    public void setAssets(Map<String, Account_Asset__c> assets) {
        this.assets = assets;
    }

    @Override
    public void setAssetRecordTypes(Map<String, RecordType> recordTypes) {
        this.recordTypes = recordTypes;
        setData();
    }
}