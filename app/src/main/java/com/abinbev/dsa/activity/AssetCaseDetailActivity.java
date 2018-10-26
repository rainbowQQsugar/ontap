package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.abinbev.dsa.R;
import com.abinbev.dsa.ui.view.RelatedCaseView;

import butterknife.Bind;

public class AssetCaseDetailActivity extends CasoViewActivity {

    public static final String TAG = AssetCaseDetailActivity.class.getSimpleName();

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    RelatedCaseView related;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.title_activity_asset_case_detail));

        related = new RelatedCaseView(this);
        relatedContainer.addView(related);

    }

    @Override
    void onCasoEditClicked(View view) {
        Intent intent = new Intent(this, AssetCaseEditActivity.class);
        intent.putExtra(AssetCaseEditActivity.CASO_ID_EXTRA, casoId);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (related != null) {
            related.setCasoId(casoId);
        }
    }


}
