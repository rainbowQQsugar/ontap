package com.abinbev.dsa.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import butterknife.Bind;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Caso;
import com.abinbev.dsa.ui.presenter.AssetCaseEditPresenter;
import java.util.ArrayList;
import java.util.List;

public class AssetCaseEditActivity extends CasoEditActivity implements AssetCaseEditPresenter.ViewModel {

    public static final String TAG = AssetCaseEditActivity.class.getSimpleName();
    public static final String CASO_ASSETS = "caso_assets";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    //EditRelatedCaseView relatedCaseView;
    AssetCaseEditPresenter presenter;
    protected List<String> assets;
    protected List<Caso> cases;

    private String tempId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.title_activity_asset_case_detail));


        if (getIntent() != null) {
            assets = (List<String>) getIntent().getSerializableExtra(CASO_ASSETS);
            if (assets != null && assets.size() > 0) {
                cases = new ArrayList<>();
                for (String assetId : assets) {
                    Caso caso = new Caso(accountId, assetId);
                    cases.add(caso);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter = new AssetCaseEditPresenter(accountId);
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.stop();
    }

    @Override
    void onCasoSaveClicked(View view) {
        super.onCasoSaveClicked(view);
    }

    @Override
    boolean containsValidValues() {
        if (super.containsValidValues()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    void onPostSaveCase() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void newCasoCreate(String tempId) {
        this.tempId = tempId;
        super.newCasoCreate(tempId);
    }


    @Override
    public void onChildCasesCreated() {
        setResult(RESULT_OK);
        finish();
    }
}
