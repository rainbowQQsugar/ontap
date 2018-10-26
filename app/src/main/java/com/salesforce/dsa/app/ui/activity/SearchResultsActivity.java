package com.salesforce.dsa.app.ui.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.salesforce.dsa.DSAAppState;
import com.salesforce.dsa.app.R;
import com.salesforce.dsa.app.ui.adapter.ContentListAdapter;
import com.salesforce.dsa.app.utils.ContentUtils;
import com.salesforce.dsa.app.utils.DataUtils;
import com.salesforce.dsa.data.model.ContentVersion;

import java.util.List;

public class SearchResultsActivity extends Activity {

    private ContentListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.search_results);
        ListView list = (ListView) findViewById(R.id.content_list);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY).trim();
            List<ContentVersion> results = DataUtils.searchContentForText(query, this);

//            adapter = new ContentListAdapter(results, false);

            list.setAdapter(adapter);
            list.setOnItemClickListener(new ListItemClickListener());
        }
    }

    private class ListItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object object = parent.getAdapter().getItem(position);
            if (object instanceof ContentVersion) {
                ContentVersion cv = (ContentVersion) object;

                Context context = SearchResultsActivity.this;
                DSAAppState.getInstance().startViewingDocument(cv, context);

//                if (cv.getFilePath(context) != null) {
////                    Intent intent = ContentUtils.getContentIntent(context, cv);
//                    if (ContentUtils.isAvailable(context, intent)) {
//                        startActivity(intent);
//                    } else {
//                        Toast.makeText(context,
//                                "Application to handle this file type is not available on the device!!",
//                                Toast.LENGTH_LONG).show();
//                    }
//                } else {
//                    Toast.makeText(context, "Selected content: " + cv.getName() + "is still being downloaded.",
//                            Toast.LENGTH_LONG).show();
//                }

            }
        }
    }

}
