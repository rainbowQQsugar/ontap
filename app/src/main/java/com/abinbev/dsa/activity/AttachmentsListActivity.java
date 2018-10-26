package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.AttachmentsListAdapter;
import com.abinbev.dsa.bus.event.AttachmentEvent;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.service.AttachmentUploadService;
import com.abinbev.dsa.ui.presenter.AttachmentsListPresenter;
import com.abinbev.dsa.ui.view.SortableHeader;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by lukaszwalukiewicz on 18.01.2016.
 */
public class AttachmentsListActivity extends AppBaseDrawerActivity implements AttachmentsListPresenter.ViewModel {

    private static final int SELECT_ATTACHMENT_REQUEST_CODE = 333;

    public static final String ARGS_ACCOUNT_ID = "account_id";
    private AttachmentsListPresenter presenter;
    private AttachmentsListAdapter adapter;
    private String accountId;

    @Inject
    Bus messageBus;

    @Bind(R.id.attachment_list)
    ListView listView;

    @Bind({ R.id.attachment_name, R.id.attachment_last_modified_date})
    List<SortableHeader> sortableHeaders;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getAppComponent().inject(this);
        adapter = new AttachmentsListAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Attachment attachment = (Attachment) adapter.getItem(position);
                presenter.onAttachmentClicked(attachment);
            }
        });
        Intent intent = getIntent();
        if (intent != null) {
            String currentAccountId = intent.getStringExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA);
            this.accountId = currentAccountId;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        messageBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.stop();
        messageBus.unregister(this);
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }

            if (SELECT_ATTACHMENT_REQUEST_CODE == requestCode) {
                Intent intent = AttachmentUploadService.uploadAccountAttachment(this, data.getData(), accountId);
                startService(intent);
            }
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_attachments_list_view;
    }

    @Override
    public void onRefresh() {
        if (presenter == null) {
            presenter = new AttachmentsListPresenter(this, accountId);
        }
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    public void setData(List<Attachment> attachments){
        adapter.setData(attachments);
    }

    @OnClick(R.id.attachment_name)
    public void onAttachmentNameHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByName(sortableHeader.toggleSortDirection());
        clearSortOnAllOthers(sortableHeader.getId());
    }

    @OnClick(R.id.attachment_last_modified_date)
    public void onAttachmentLastModifiedDateHeaderClicked(View view) {
        SortableHeader sortableHeader = (SortableHeader) view;
        adapter.sortByLastModifiedDate(sortableHeader.toggleSortDirection());
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

    @OnClick(R.id.add_attachment)
    public void onAddAttachmentClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, SELECT_ATTACHMENT_REQUEST_CODE);
    }

    @Subscribe
    public void onAttachmentSaved(AttachmentEvent.AttachmentSavedEvent attachmentSavedEvent) {
        presenter.start();
    }
}
