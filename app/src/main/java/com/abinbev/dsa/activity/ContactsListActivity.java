package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.ContactsListAdapter;
import com.abinbev.dsa.model.Contact;
import com.abinbev.dsa.ui.presenter.ContactsListPresenter;
import com.abinbev.dsa.ui.view.SortableHeader;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by lukaszwalukiewicz on 15.01.2016.
 */
public class ContactsListActivity extends AppBaseDrawerActivity implements ContactsListPresenter.ViewModel {

    public static final String ACCOUNT_ID_EXTRA = "account_id";
    private String accountId;
    private ContactsListAdapter adapter;
    private ContactsListPresenter presenter;

    @Bind({ R.id.contact_name, R.id.contact_phone, R.id.contact_function, R.id.contact_birthdate})
    List<SortableHeader> sortableHeaders;

    @Bind(R.id.contacts_list)
    ListView listView;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_contacts_list_view;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ContactsListAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Contact contact = (Contact) adapter.getItem(position);
                if (contact != null) {
                    Intent intent = new Intent(ContactsListActivity.this, ContactDetailsActivity.class);
                    intent.putExtra(ContactDetailsActivity.CONTACT_ID_EXTRA, contact.getId());
                    startActivity(intent);
                }
            }
        });


        Intent intent = getIntent();
        if (intent != null) {
            accountId = intent.getStringExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.stop();
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
        if (presenter == null) {
            presenter = new ContactsListPresenter(accountId);
        }
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    public void setData(List<Contact> contacts) {
        adapter.setData(contacts);
    }

    @OnClick(R.id.contact_name)
    public void onContactNameHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByName(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @OnClick(R.id.contact_phone)
    public void onPhoneHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByPhone(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @OnClick(R.id.contact_function)
    public void onFunctionHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByFunction(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @OnClick(R.id.contact_birthdate)
    public void onBirthDateClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByBirthDate(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    /**
     * Clears the sort order on all headers in the group EXCEPT for the id of the view passed in
     * @param sortableHeaderId - the id to NOT clear the sort on
     */
    private void clearSortOnAllOthers(int sortableHeaderId) {
        for (SortableHeader sortableHeader : sortableHeaders) {
            if (sortableHeader.getId() != sortableHeaderId) {
                sortableHeader.clearSortIndicator();
            }
        }
    }
}
