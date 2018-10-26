package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.Case;
import com.abinbev.dsa.model.Comentario_caso_force__c;
import com.abinbev.dsa.service.AttachmentUploadService;
import com.abinbev.dsa.ui.presenter.CasoViewPresenter;
import com.abinbev.dsa.utils.DateUtils;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class CasoViewActivity extends DynamicViewActivity implements CasoViewPresenter.ViewModel {


    public static final String CASO_ID_EXTRA = "caso_id";
    public static final String ACCOUNT_ID_EXTRA = "account_id";

    private static final String TAG = CasoViewActivity.class.getSimpleName();
    public static final int SELECT_FILE_REQUEST_CODE = 536;

    protected String casoId;
    protected String accountId;
    protected CasoViewPresenter casoViewPresenter;

    @Bind(R.id.comment_container)
    LinearLayout containerLayout;

    @Bind(R.id.empty_comments)
    TextView emptyComments;

    @Bind(R.id.comment_input)
    EditText commentInput;

    @Bind(R.id.save_comment)
    TextView saveComment;

    @Bind(R.id.related_cases_container)
    LinearLayout relatedContainer;

    @Bind(R.id.attachment_container)
    LinearLayout attachmentContainer;

    @Bind(R.id.empty_attachments)
    TextView emptyAttachments;

    @Bind(R.id.caso_edit)
    Button casoEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            casoId = getIntent().getStringExtra(CASO_ID_EXTRA);
            accountId = getIntent().getStringExtra(ACCOUNT_ID_EXTRA);
        }

        getSupportActionBar().setTitle(getString(R.string.title_activity_caso));

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        commentInput.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        casoViewPresenter = new CasoViewPresenter(casoId);
        casoViewPresenter.setViewModel(this);
        casoViewPresenter.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        casoViewPresenter.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.dynamic_caso;
    }

    @OnClick(R.id.caso_edit)
    void onCasoEditClicked(View view) {
        Intent intent = new Intent(this, CasoEditActivity.class);
        intent.putExtra(CasoViewActivity.CASO_ID_EXTRA, casoId);
        intent.putExtra(CasoViewActivity.ACCOUNT_ID_EXTRA, accountId);
        startActivityForResult(intent, CasoEditActivity.CASO_EDIT_REQUEST_CODE);
    }

    @Override
    public void setCaso(Case caso, String recordTypeName) {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.caso));
        if (!TextUtils.isEmpty(caso.getName())) {
            sb.append(" ")
              .append(caso.getName());
        }
        getSupportActionBar().setSubtitle(sb.toString());
        buildLayout("Case_Force__c", caso);
    }

    @Override
    public void setEditable(boolean isEditable) {
       if (isEditable) casoEdit.setVisibility(View.VISIBLE);
       else casoEdit.setVisibility(View.GONE);
    }

    @Override
    public void setComments(List<Comentario_caso_force__c> comments) {
        if (comments.isEmpty()) {
            emptyComments.setVisibility(View.VISIBLE);
        } else {
            emptyComments.setVisibility(View.GONE);
            containerLayout.removeAllViews();
            for (Comentario_caso_force__c comment : comments) {
                TextView commentView = createCommentView(comment.getComment());
                containerLayout.addView(commentView, containerLayout.getChildCount()); //add to the end
            }
        }
    }

    @Override
    public void commentSaved(boolean success) {
        if (success) {
            commentInput.setText("");
            casoViewPresenter.fetchComments();
        } else {
            //TODO: display error message saving?
        }
    }

    private TextView createCommentView(String comment) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int bottomPadding = getResources().getDimensionPixelSize(R.dimen.space2);

        TextView commentView = new TextView(new ContextThemeWrapper(CasoViewActivity.this, R.style.Comment));
        commentView.setText(comment);
        commentView.setLayoutParams(params);
        commentView.setPadding(0, 0, 0, bottomPadding);

        return commentView;
    }

    @OnTextChanged(R.id.comment_input)
    public void onCommentInput(CharSequence text) {
        saveComment.setEnabled(!TextUtils.isEmpty(text));
    }

    @OnClick(R.id.save_comment)
    public void onSaveComment() {
        casoViewPresenter.saveComment(commentInput.getText().toString());
    }

    @Override
    public void setAttachments(List<Attachment> attachments) {
        attachmentContainer.removeAllViews();
        if (attachments.isEmpty()) {
            emptyAttachments.setVisibility(View.VISIBLE);attachmentContainer.removeAllViews();
        } else {
            emptyAttachments.setVisibility(View.GONE);
            for (Attachment attachment : attachments) {
                View attachmentView = createAttachmentView(attachment);
                attachmentContainer.addView(attachmentView, attachmentContainer.getChildCount());
            }
        }
    }

    private View createAttachmentView(final Attachment attachment) {
        View view = getLayoutInflater().inflate(R.layout.caso_attachment, attachmentContainer, false);
        ((LinearLayout.LayoutParams) view.getLayoutParams()).setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.separator));

        ((TextView) view.findViewById(R.id.attachment_title)).setText(attachment.getName());

        // hide remove button since this screen is view only
        ((ImageView) view.findViewById(R.id.remove_attachment)).setVisibility(View.GONE);

        String createdDate = DateUtils.formatDateTimeShort(attachment.getCreatedDate());

        if (TextUtils.isEmpty(createdDate)) {
            ((TextView) view.findViewById(R.id.attachment_date)).setText(getString(R.string.pendiente));
        } else {
            ((TextView) view.findViewById(R.id.attachment_date)).setText(createdDate);
        }


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                casoViewPresenter.onAttachmentClicked(CasoViewActivity.this, attachment);
            }
        });

        return view;
    }

    @OnClick(R.id.add_attachment)
    public void addAttachmentClicked() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, SELECT_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            Intent uploadIntent = AttachmentUploadService.uploadCaseAttachment(this, data.getData(), casoId);
            startService(uploadIntent);
        } else if (requestCode == CasoEditActivity.CASO_EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, R.string.case_save_success, Toast.LENGTH_SHORT).show();
            SyncUtils.TriggerRefresh(this);
            return;
        }
    }


}
