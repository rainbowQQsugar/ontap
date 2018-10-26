package com.abinbev.dsa.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.adapter.PocAttachmentsAdapter;
import com.abinbev.dsa.bus.event.AttachmentEvent;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.Resource__c;
import com.abinbev.dsa.service.AttachmentUploadService;
import com.abinbev.dsa.ui.presenter.PocAttachmentsPresenter;
import com.abinbev.dsa.utils.AppPreferenceUtils;
import com.abinbev.dsa.utils.AttachmentUtils;
import com.abinbev.dsa.utils.ContentUtils;
import com.abinbev.dsa.utils.UITagHandler;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;

public class PocAttachmentsActivity extends AppBaseActivity implements PocAttachmentsPresenter.ViewModel, PocAttachmentsAdapter.AttachmentClickHandler {
    private static final String TAG = PocAttachmentsActivity.class.getSimpleName();
    public static final int SELECT_ATTACHMENT_REQUEST_CODE = 314;

    public static final String ARGS_ACCOUNT_ID = "account_id";

    private static final String PROSPECT_ATTACHMENT_TEXT = "PROSPECT ATTACHMENT";

    @Bind(R.id.add_attachment)
    ImageView add;
    private PocAttachmentsPresenter presenter;
    private PocAttachmentsAdapter adapter;
    private String accountId;

    @Inject
    Bus messageBus;

    @Bind(R.id.attachment_list)
    ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getAppComponent().inject(this);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.archivos));

        Intent intent = getIntent();
        if (intent != null) {
            String currentAccountId = intent.getStringExtra(AccountOverviewActivity.ACCOUNT_ID_EXTRA);
            this.accountId = currentAccountId;
        }

        adapter = new PocAttachmentsAdapter(accountId);
        adapter.setAttachmentClickHandler(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= adapter.getCount()) {
                    return;
                }
                Attachment attachment = (Attachment) adapter.getItem(position);
                openAttachment(attachment);
            }
        });
        View footerView =  ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_poc_attachment_list, null, false);
        String footerText = Resource__c.getFieldText(AppPreferenceUtils.getUserProfile(this),PROSPECT_ATTACHMENT_TEXT);
        if (footerText != null) {
            Log.e("Babu", "footerText: " + footerText);
            Spanned formattedText;
            try {
                formattedText = Html.fromHtml(footerText, null, new UITagHandler());
                ((TextView)footerView).setText(formattedText);
            } catch (Exception e) {
                // formattedText = Html.fromHtml("Got Exception: " + e.getMessage());
                Log.e(TAG, e.getMessage());
            }

        }
        listView.addFooterView(footerView);
        setupAdd();



    }

    private void setupAdd() {
        Drawable drawable = add.getDrawable();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable.mutate(), getResources().getColor(R.color.primary));
    }

    private void openAttachment(Attachment attachment){
        if (ContentUtils.isNull_OR_Blank(attachment.getId())) {
            AttachmentUtils.openUnsyncedAccountAttachment(attachment, this, this.accountId);
        } else {
            AttachmentUtils.openAttachment(attachment, this, this.accountId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        messageBus.register(this);

        if (presenter == null) {
            presenter = new PocAttachmentsPresenter(accountId);
        }
        presenter.setViewModel(this);
        presenter.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.stop();
        messageBus.unregister(this);
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
        return R.layout.activity_poc_attachments;
    }

    @Override
    public void setData(List<Attachment> attachments){
        adapter.setData(attachments);
    }

    @OnClick(R.id.add_attachment)
    public void onAddAttachmentClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, SELECT_ATTACHMENT_REQUEST_CODE);
    }

    @Subscribe
    public void onAttachmentSaved(AttachmentEvent.AttachmentSavedEvent attachmentSavedEvent) {
        Log.d(TAG, "onAttachmentSaved " + attachmentSavedEvent.isSuccess());
        presenter.fetchAttachments();
    }

    @Subscribe
    public void onAttachmentUpload(AttachmentEvent.AttachmentUploadEvent attachmentUploadEvent) {
        Log.d(TAG, "onAttachmentUpload " + attachmentUploadEvent.isSuccess());
    }

    @Override
    public void onAttachmentRemove(Attachment attachment) {
        presenter.deleteAttachment(attachment.getId(), attachment.getName());
    }

    @Override
    public void setAccount(Account account) {
        getSupportActionBar().setSubtitle(account.getName());
    }
}
