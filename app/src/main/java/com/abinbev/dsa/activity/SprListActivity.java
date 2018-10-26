package com.abinbev.dsa.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.abinbev.dsa.R;
import com.abinbev.dsa.fragments.SprListFragment;
import com.abinbev.dsa.model.Account;

/**
 * Created by Adam Chodera on 7.07.2017.
 */

public class SprListActivity extends AppCompatActivity {

    public static final String ACCOUNT_ID_EXTRA = "accountId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(getPreferredOrientation());
        setContentView(R.layout.activity_spr_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        String accountId = getIntent().getStringExtra(ACCOUNT_ID_EXTRA);
        actionBar.setTitle(Account.getById(accountId).getName());
        actionBar.setSubtitle(R.string.spr_list);

//        accountId = "001N000001As2ODIAZ"; // this account has a few SPR items

        final SprListFragment sprListFragment = SprListFragment.newInstance(accountId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, sprListFragment)
                .commit();
    }

    private int getPreferredOrientation() {
        return getResources().getBoolean(R.bool.is10InchTablet) ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}