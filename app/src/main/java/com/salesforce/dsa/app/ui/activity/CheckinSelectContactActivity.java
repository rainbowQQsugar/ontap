package com.salesforce.dsa.app.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.salesforce.dsa.DSAAppState;
import com.salesforce.dsa.app.R;
import com.salesforce.dsa.app.ui.adapter.ContactListAdapter;
import com.salesforce.dsa.app.ui.customview.SegmentedGroup;
import com.salesforce.dsa.app.utils.DataUtils;
import com.salesforce.dsa.data.model.Contact;
import java.util.ArrayList;
import java.util.List;

public class CheckinSelectContactActivity extends Activity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private SegmentedGroup segmentedGroup;
    private ListView listView;
    private EditText searchEditText;
    private Button clearButton;
    private Button chooseLaterButton;
    private List<Contact> contacts;
    private ContactListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.checkin_select_contact);

        listView = (ListView) findViewById(R.id.contacts_list);
        listView.setOnItemClickListener(new ContactClickListener());

        segmentedGroup = (SegmentedGroup) findViewById(R.id.segmentedGroup);
        segmentedGroup.setOnCheckedChangeListener(this);

        // select all contacts segment by default
        RadioButton allContactsButton = (RadioButton) segmentedGroup.findViewById(R.id.allContactsButton);
        allContactsButton.setChecked(true);

        onCheckedChanged(segmentedGroup, R.id.allContactsButton);

        searchEditText = (EditText) findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new SearchTextChangeWatcher());

        clearButton = (Button) findViewById(R.id.clearButton);
        chooseLaterButton = (Button) findViewById(R.id.chooseAtCheckout);

        boolean showChooseLaterButton = getIntent().getExtras().getBoolean("showChooseLaterButton");
        chooseLaterButton.setVisibility(showChooseLaterButton ? View.VISIBLE : View.INVISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        List<Contact> recentContacts = DSAAppState.getInstance().getRecentContactsFromPrefs(this);
        if (recentContacts.size() > 0) {
            DSAAppState.getInstance().setRecentContacts(recentContacts);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // save recent contact ids in preferences
        DSAAppState.getInstance().saveRecentContacts(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View arg0) {

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        if (checkedId == R.id.allContactsButton) {
            contacts = DataUtils.fetchContacts();
        } else if (checkedId == R.id.recentContactButton) {
            contacts = DSAAppState.getInstance().getRecentContacts();
        }
        showContacts(contacts);
    }

    private void showContacts(List<Contact> contacts) {
        adapter = new ContactListAdapter(contacts);
        listView.setAdapter(adapter);
    }

    private class ContactClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Contact contact = (Contact) listView.getItemAtPosition(position);
            DSAAppState.DocumentTrackingType trackingType = DSAAppState.getInstance().getTrackingType();

            if (trackingType == DSAAppState.DocumentTrackingType.DocumentTracking_DeferredContact) {
                DSAAppState.getInstance().setTrackingType(DSAAppState.DocumentTrackingType.DocumentTracking_None);
            }
            DSAAppState.getInstance().startDocumentTrackingForContact(contact);
            CheckinSelectContactActivity.this.finish();
        }
    }

    @SuppressLint("DefaultLocale")
    private class SearchTextChangeWatcher implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {
            if (searchEditText.getText().toString().length() > 0)
                clearButton.setVisibility(View.VISIBLE);
            else
                clearButton.setVisibility(View.GONE);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(final CharSequence s, int start, int before, int count) {
            ArrayList<Contact> filteredContacts = new ArrayList<>();
            for (Contact contact : contacts) {
                if (contact.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                    filteredContacts.add(contact);
                }
            }
            adapter.replaceItems(filteredContacts);
        }
    }
}
