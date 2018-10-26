package com.abinbev.dsa.activity;

import android.os.Bundle;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Contact;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.utils.DSAConstants;

public class ContactDetailsActivity extends DynamicViewActivity {

    public static final String CONTACT_ID_EXTRA = "CONTACT_ID";

    private static final String TAG = "ContactDetailsActivity";

    private String contactId;
    private Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contactId = getIntent().getStringExtra(CONTACT_ID_EXTRA);
        contact = new Contact(DataManagerFactory.getDataManager().exactQuery(DSAConstants.DSAObjects.CONTACT, "Id", contactId));

        getSupportActionBar().setTitle(getString(R.string.title_activity_contact));
        getSupportActionBar().setSubtitle(contact.getName());

        buildLayout(DSAConstants.DSAObjects.CONTACT, contact);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.dynamic_main;
    }

}
