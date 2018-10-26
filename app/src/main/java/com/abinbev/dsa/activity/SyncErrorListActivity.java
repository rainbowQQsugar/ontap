package com.abinbev.dsa.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.SyncErrorListAdapter;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.model.ErrorObject;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by bduggirala on 12/17/15.
 */
public class SyncErrorListActivity extends AppBaseDrawerActivity {


    private SyncErrorListAdapter syncErrorListAdapter;

    @Bind(R.id.sync_header)
    TextView syncHeader;

    @Bind(R.id.sync_error_list)
    ListView syncErrorList;

    int resetCounter;

    int resetAtCount = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle("");

        syncErrorListAdapter = new SyncErrorListAdapter();
        syncErrorList.setAdapter(syncErrorListAdapter);

        List<ErrorObject> syncErrors = DataManagerFactory.getDataManager().getErrors();
        syncErrorListAdapter.setData(syncErrors);
        syncHeader.setText(String.format(getString(R.string.sync_error_header), syncErrors.size()));
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
    public void onRefresh() {
        List<ErrorObject> syncErrors = DataManagerFactory.getDataManager().getErrors();
        syncErrorListAdapter.setData(syncErrors);
        syncHeader.setText(String.format(getString(R.string.sync_error_header), syncErrors.size()));
    }

    @Override
    public int getLayoutResId() {
        return R.layout.sync_error_list_view;
    }

    @OnClick(R.id.sync_header)
    public void syncHeaderClick() {
        resetCounter++;
        if (resetCounter >= resetAtCount) {
            resetCounter = 0;
            DataManagerFactory.getDataManager().clearErrors();
            onRefresh();
        }
    }

    @OnClick(R.id.contact_admin)
    public void contactAdminClick() {
        final List<ErrorObject> syncErrors = DataManagerFactory.getDataManager().getErrors();
        final StringBuilder stringBuilder = new StringBuilder();
        for (ErrorObject errorObject : syncErrors) {
            try {
                stringBuilder.append("\n\n");
                stringBuilder.append(errorObject.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.contact_admin);
        Button sendButton = (Button) dialog.findViewById(R.id.sendButton);
        final EditText editText = (EditText) dialog.findViewById(R.id.problemDescEditText);
        Button closeButton = (Button) dialog.findViewById(R.id.cancelButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String body = editText.getText().toString();
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.admin_email)});
                i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.contact_admin_email_subject));
                i.putExtra(Intent.EXTRA_TEXT, body + "\n" + stringBuilder.toString());
                try {
                    startActivity(i);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(SyncErrorListActivity.this, getResources().getString(R.string.no_email_clients), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}
