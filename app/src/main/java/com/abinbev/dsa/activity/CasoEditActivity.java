package com.abinbev.dsa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.bus.event.AttachmentEvent;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.Case;
import com.abinbev.dsa.model.CaseStatusList__c;
import com.abinbev.dsa.model.Comentario_caso_force__c;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.service.AttachmentUploadService;
import com.abinbev.dsa.ui.presenter.CasoEditPresenter;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.CaseFields;
import com.abinbev.dsa.utils.AbInBevConstants.CasosFields;
import com.abinbev.dsa.utils.AbInBevConstants.RecordTypeName;
import com.abinbev.dsa.utils.DateUtils;
import com.salesforce.androidsyncengine.data.layouts.Details;
import com.salesforce.androidsyncengine.data.layouts.LayoutItem;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class CasoEditActivity extends DynamicEditActivity implements CasoEditPresenter.ViewModel {

    public static final String CASO_ID_EXTRA = "caso_id";
    public static final String CASO_RECORD_TYPE = "caso_record_type";
    public static final String ACCOUNT_ID = "account_id";
    public static final String ASSET_ID = "asset_id";
    public static final int SELECT_FILE_REQUEST_CODE = 536;
    public static final int CASO_EDIT_REQUEST_CODE = 226;

    private static final String TAG = "CasoEditActivity";

    @Inject
    Bus messageBus;

    @Bind(R.id.scrollview)
    ScrollView scrollView;

    @Bind(R.id.bottom_layout)
    LinearLayout bottomLayout;

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

    @Bind(R.id.attachment_header_container)
    RelativeLayout attachmentHeaderContainer;

    @Bind(R.id.attachment_container)
    LinearLayout attachmentContainer;

    @Bind(R.id.empty_attachments)
    TextView emptyAttachments;

    protected String accountId;
    protected String assetId;
    protected String casoId;
    protected String recordTypeName;
    private Case caso;
    protected CasoEditPresenter casoEditPresenter;
    private int attachmentsCount;

    private JSONObject updatedObject;

    private boolean initialLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getAppComponent().inject(this);

        casoId = getIntent().getStringExtra(CASO_ID_EXTRA);
        recordTypeName = getIntent().getStringExtra(CASO_RECORD_TYPE);
        accountId = getIntent().getStringExtra(ACCOUNT_ID);
        assetId = getIntent().getStringExtra(ASSET_ID);
        getSupportActionBar().setTitle(getString(R.string.title_activity_caso));
        getSupportActionBar().setSubtitle(recordTypeName);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.dynamic_caso_edit;
    }

    @Override
    protected void onResume() {
        super.onResume();
        messageBus.register(this);
        if (casoEditPresenter == null) {
            casoEditPresenter = new CasoEditPresenter(casoId);
        }
        casoEditPresenter.setViewModel(this);
        casoEditPresenter.start();

        if (initialLoad) {
            initialLoad = false;
            prepareData();
        }
    }

    @Override
    protected boolean isUpdateable(LayoutItem layoutItem, Details details, String fieldName, String section) {
        if (CasosFields.ACCOUNT_ID.equals(fieldName) || CasosFields.ASSET_C.equals(fieldName)) {
            return false;
        }
        else {
            if (RecordTypeName.ACCOUNT_CHANGE_REQUEST.equals(recordTypeName) &&
                    (CaseFields.ACCOUNT_ID.equals(fieldName) ||
                        CaseFields.STATUS.equals(fieldName) ||
                        CaseFields.POC_REJECT_REASON.equals(fieldName))) {
                return false;
            }
            else {
                return super.isUpdateable(layoutItem, details, fieldName, section);
            }
        }
    }

    private void prepareData() {
        if (casoId != null) {
            JSONObject caseJsonObject = DataManagerFactory.getDataManager().exactQuery(AbInBevConstants.AbInBevObjects.CASE, "Id", casoId);
            if (caseJsonObject == null) {
                showSnackbar(R.string.no_case_found);
            }
            else {
                caso = new Case(caseJsonObject);
                getSupportActionBar().setSubtitle(caso.getId());
                buildLayout("Case_Force__c", caso);
            }

            if (SyncUtils.isTemporaryIdForObject(casoId, AbInBevConstants.AbInBevObjects.CASE)) {
                hideAttachmentSection();
            }
        }
        else {
            hideAttachmentSection();
            casoEditPresenter.createNewCase(accountId, recordTypeName, assetId);
        }
    }

    private void hideAttachmentSection() {
        attachmentHeaderContainer.setVisibility(View.GONE);
        emptyAttachments.setVisibility(View.GONE);
        attachmentContainer.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        messageBus.unregister(this);
        casoEditPresenter.stop();
    }

    @OnClick(R.id.caso_save)
    void onCasoSaveClicked(View view) {

        if (!isModified()) {
            showSnackbarShort(R.string.nothing_was_changed);
        }
        else if (containsValidValues()) {

            JSONObject originalObject = caso.toJson();

            Log.i(TAG, "original Values: " + originalObject.toString());
            updatedObject = getUpdatedJSONObject();
            Log.i(TAG, "updated Values: " + updatedObject.toString());

            JSONObject jsonObject = caso.toJson();
            if (isNewCase()) {
                try {
                    Iterator it = updatedObject.keys();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        jsonObject.put(key, updatedObject.get(key));
                    }
                    casoEditPresenter.saveNewCase(new Case(jsonObject));
                } catch (JSONException e) {
                    showSnackbar(R.string.failed_to_save_case);
                }
            }
            else {
                casoEditPresenter.saveUpdatedCase(caso, updatedObject);
            }
        }

    }

    @Override
    public void onCaseSaved(boolean success) {
        if (success) {
            onPostSaveCase();
        } else {
            // stay on the same screen
            showSnackbar(R.string.failed_to_save_case);
        }
    }

    @OnClick(R.id.caso_cancel)
    void onCasoCancelClicked(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    void onPostSaveCase() {
        setResult(RESULT_OK);
        finish();
    }

    boolean containsValidValues() {
        if (caso == null) {
            showSnackbar(R.string.no_case_found);
            return false;
        }

        JSONObject originalObject = caso.toJson();

        Log.i(TAG, "original Values: " + originalObject.toString());
        updatedObject = getUpdatedJSONObject();
        Log.i(TAG, "updated Values: " + updatedObject.toString());

        Log.i(TAG, "required fields: " + requiredFields);

        if (requiredFields == null) {
            // the buildLayout has failed or it is still in progress
            return false;
        }

        for (String fieldName : requiredFields) {
            String currentValue = getCurrentValueForFieldName(fieldName);
            if (currentValue == null) {
                Log.i(TAG, "Missing required field: " + fieldName);
                showSnackbar(getString(R.string.missing_required_field) + " : " + fieldName);
                return false;
            } else {
                currentValue = currentValue.trim();
                if (currentValue.isEmpty()) {
                    Log.i(TAG, "Missing required field: " + fieldName);
                    showSnackbar(getString(R.string.missing_required_field) + " : " + fieldName);
                    return false;
                }
            }
        }

        // At least one attachment is required for Draft_Beer_Machine_Installation
        String status = getCurrentValueForFieldName(CaseFields.STATUS);

        if (Case.CasosStates.RESOLVED.equals(status) && attachmentsCount == 0) {
            RecordType recordType = RecordType.getById(caso.getRecordTypeId());
            if (recordType != null && "Draft_Beer_Machine_Survey".equals(recordType.getDeveloperName())) {
                showSnackbar(getString(R.string.sin_adjunto_archivos));
                return false;
            }
        }

//        // run validation checks and on success save
//
//        if (!containsValidEquipmentRequiredFields()) {
//            showSnackbar(R.string.equip_message);
//            Log.i(TAG, "checkEquipment failed");
//            return false;
//        } else {
//            Log.i(TAG, "checkEquipment passed");
//        }
//
//        if (!containsAssignentRequiredFields()) {
//            showSnackbar(R.string.bad_assignment_message);
//            Log.i(TAG, "checkAssignment failed");
//            return false;
//        } else {
//            Log.i(TAG, "checkAssignment passed");
//        }
//
//        if (!isValidSolvedAreaStatus()) {
//            showSnackbar(R.string.solved_by_area_status);
//            Log.i(TAG, "isValidSolvedAreaStatus failed");
//            return false;
//        } else {
//            Log.i(TAG, "isValidSolvedAreaStatus passed");
//        }
//
//        if (!isValidAllocationAndStatus()) {
//            showSnackbar(R.string.needs_allocation);
//            Log.i(TAG, "isValidAllocationAndStatus failed");
//            return false;
//        } else {
//            Log.i(TAG, "isValidAllocationAndStatus passed");
//        }
//
//        if (!isValidNewCaseStatus()) {
//            showSnackbar(R.string.invalid_new_case_status);
//            Log.i(TAG, "isValidAllocationAndStatus failed");
//            return false;
//        } else {
//            Log.i(TAG, "isValidAllocationAndStatus passed");
//        }
//
//        if (!isValidReassignedCase()) {
//            showSnackbar(R.string.invalid_reassignment);
//            Log.i(TAG, "isValidReassignedCase failed");
//            return false;
//        } else {
//            Log.i(TAG, "isValidReassignedCase passed");
//        }
//
//        if (!isValidVRCF038()) {
//            showSnackbar(R.string.vrcf038);
//            Log.i(TAG, "isValidVRCF038 failed");
//            return false;
//        } else {
//            Log.i(TAG, "isValidVRCF038 passed");
//        }

        return true;

    }

    @Override
    protected void buildLayout(String objectType, TranslatableSFBaseObject baseObject, boolean useDetailsLayout) {
        super.buildLayout(objectType, baseObject, useDetailsLayout);
        commentInput.setVisibility(View.VISIBLE);
        if (isNewCase()) {
            saveComment.setVisibility(View.GONE);
        } else {
            saveComment.setVisibility(View.VISIBLE);
        }
        containerLayout.setVisibility(View.VISIBLE);
    }

    protected String getCurrentValueForFieldName(String fieldName) {
        String currentValue;
        try {
            currentValue = updatedObject.getString(fieldName);
            if ("null".equals(currentValue)) return null;
        } catch (JSONException je) {
            if (baseObject.isNullValue(fieldName)) {
                return null;
            } else {
                currentValue = baseObject.getStringValueForKey(fieldName);
            }
        }
        return currentValue;
    }

//    private boolean isValidReassignedCase() {
//        String estado = getCurrentValueForFieldName(AbInBevConstants.CasosFields.STATE);
//        if (estado != null && estado.equalsIgnoreCase(Caso.CasosStates.REASIGNED)) {
//            String previousArea = getCurrentValueForFieldName(AbInBevConstants.CasosFields.PREVIOUS_AREA);
//            if (ContentUtils.isNull_OR_Blank(previousArea)) {
//                return false;
//            } else {
//                return true;
//            }
//        } else {
//            return true;
//        }
//    }

//    private boolean containsValidEquipmentRequiredFields() {
//        String equipo = getCurrentValueForFieldName(AbInBevConstants.CasosFields.EQUIP_DES);
//        if (equipo != null && equipo.equalsIgnoreCase("Si")) {
//            String numero = getCurrentValueForFieldName(AbInBevConstants.CasosFields.NUMERO);
//            String fabricante = getCurrentValueForFieldName(AbInBevConstants.CasosFields.FABRICANTE);
//            String hora_de_visita = getCurrentValueForFieldName(AbInBevConstants.CasosFields.HORA_DE_VISITA);
//            if (ContentUtils.isNull_OR_Blank(numero, fabricante, hora_de_visita)) return false;
//            else return true;
//        }
//        return true;
//    }

//    private boolean containsAssignentRequiredFields()  {
//        String estado = getCurrentValueForFieldName(AbInBevConstants.CasosFields.STATE);
//
//        if (estado != null && estado.equalsIgnoreCase(Caso.CasosStates.MISSALLOCATED)) {
//            String numbre = getCurrentValueForFieldName(AbInBevConstants.CasosFields.NOMBRE_DE_LA_CUENTA);
//            Account account = Account.getById(numbre);
//            if (account != null) {
//                String country = account.getStringValueForKey(AbInBevConstants.AccountFields.COUNTRY__C);
//
//                if (country.equalsIgnoreCase(AbInBevConstants.ECUADOR)) {
//                    String rejection = getCurrentValueForFieldName(AbInBevConstants.CasosFields.REJECTIONMOTIVE);
//                    if (ContentUtils.isNull_OR_Blank(rejection)) return false;
//                } else if (country.equalsIgnoreCase(AbInBevConstants.COLOMBIA)) {
//                    String motivos = getCurrentValueForFieldName(AbInBevConstants.CasosFields.MOTIVOS_DE_RECHAZO);
//                    if (ContentUtils.isNull_OR_Blank(motivos)) return false;
//                }
//            }
//        }
//        return true;
//
//    }

//    private boolean isValidVRCF038() {
//        String estado = getCurrentValueForFieldName(AbInBevConstants.CasosFields.STATE);
//        String closureType = getCurrentValueForFieldName(AbInBevConstants.CasosFields.CLOSURETYPE);
//        String responseMotive = getCurrentValueForFieldName(AbInBevConstants.CasosFields.RESPONSEMOTIVE);
//
//        boolean closureFieldExists = fieldExists(AbInBevConstants.CasosFields.CLOSURETYPE);
//        boolean responseMotiveExists = fieldExists(AbInBevConstants.CasosFields.RESPONSEMOTIVE);
//
//        if (estado != null && estado.equalsIgnoreCase(Case.CasosStates.RESOLVED)) {
//            // we must have valid values for few fields
//            if (closureFieldExists) {
//                if (ContentUtils.isNull_OR_Blank(closureType)) return false;
//            }
//
//            if (responseMotiveExists) {
//                if (ContentUtils.isNull_OR_Blank(responseMotive)) return false;
//            }
//
//            return true;
//        }
//
//        if (estado != null && !estado.equalsIgnoreCase(Caso.CasosStates.RESOLVED)) {
//            // we must not have any values for these fields
//            // we must have valid values for few fields
//            if (closureFieldExists) {
//                if (!ContentUtils.isNull_OR_Blank(closureType)) return false;
//            }
//
//            if (responseMotiveExists) {
//                if (!ContentUtils.isNull_OR_Blank(responseMotive)) return false;
//            }
//
//            return true;
//        }
//
//        return true;
//    }
//
//    private boolean isValidSolvedAreaStatus() {
//
//        String caseSolved = getCurrentValueForFieldName(AbInBevConstants.CasosFields.CASE_SOLVED_BY_AREA);
//
//        if (caseSolved != null && caseSolved.equalsIgnoreCase("true")) {
//            String[] estadoArray = {"Resuelto","Cerrado","Cerrado por el sistema","Cerrado por mala asignación","Cerrado primer contacto"};
//            List estadoList = Arrays.asList(estadoArray);
//            String estado = getCurrentValueForFieldName(AbInBevConstants.CasosFields.STATE);
//            if (estado != null) {
//                if (!estadoList.contains(estado)) {
//                    return false;
//                }
//            } else {
//                // no value also indicates an error condition
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    // If the status is one of the values specified then
//    // depending on the region you cannot have values in the Motivos/Rejection fields
//    private boolean isValidAllocationAndStatus()  {
//            String numbre = getCurrentValueForFieldName(AbInBevConstants.CasosFields.NOMBRE_DE_LA_CUENTA);
//            Account account = Account.getById(numbre);
//            if (account != null) {
//                // this is a duplicate of another case validation but ...
//                String[] estadoArray = {"Abierto","Asignado","Cerrado","En proceso","Resuelto","Supervisor Call center"};
//                List estadoList = Arrays.asList(estadoArray);
//                String estado = getCurrentValueForFieldName(AbInBevConstants.CasosFields.STATE);
//                if (estado != null) {
//                    if (estadoList.contains(estado)) {
//                        // the rejection cannot have a value
//                        String rejection = getCurrentValueForFieldName(AbInBevConstants.CasosFields.REJECTIONMOTIVE);
//                        if (!ContentUtils.isNull_OR_Blank(rejection)) return false;
//                        String motivos = getCurrentValueForFieldName(AbInBevConstants.CasosFields.MOTIVOS_DE_RECHAZO);
//                        if (!ContentUtils.isNull_OR_Blank(motivos)) return false;
//
//                    }
//                }
//            }
//        return true;
//
//    }
//
//    private boolean isValidNewCaseStatus() {
//        String id = getCurrentValueForFieldName(AbInBevConstants.ID);
//        if (id == null || SyncUtils.isTemporaryIdForObject(id, AbInBevConstants.AbInBevObjects.CASOS)) {
//            String[] estadoArray = {"Abierto","Asignado","Cerrado primer contacto"};
//            List estadoList = Arrays.asList(estadoArray);
//            String estado = getCurrentValueForFieldName(AbInBevConstants.CasosFields.STATE);
//            if (estado != null) {
//                if (estadoList.contains(estado)) {
//                    return true;
//                } else {
//                    return false;
//                }
//            } else {
//                // no value also indicates an error condition
//                return false;
//            }
//        }
//
//        return true;
//    }

    @Override
    public void setNewCaso(Case caso, boolean useDetailsLayout) {
        this.caso = caso;
        casoEditPresenter.setCasoId(casoId);
        buildLayout("Case_Force__c", caso, useDetailsLayout);
    }

    @Override
    public void newCasoCreate(String tempId) {
        if (!TextUtils.isEmpty(tempId)) {
            if (isNewCase() && commentInput.getText().toString().trim().equals("")) {
                onPostSaveCase();
            } else {
                casoEditPresenter.setCasoId(tempId);
                casoEditPresenter.saveComment(commentInput.getText()
                        .toString());
            }
        } else {
            showSnackbar(R.string.failed_to_save_case);
        }
    }

    @Override
    public void onError(Throwable e) {
        showSnackbar(e.getMessage());
    }


    protected boolean isNewCase() {
        return casoId == null;
    }


    @Override
    public void setCaso(Case caso, String recordTypeName) {
        this.recordTypeName = recordTypeName;
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.caso));
        if(!TextUtils.isEmpty(caso.getName())) {
            sb.append(" ")
              .append(caso.getName());
        }
        getSupportActionBar().setSubtitle(sb.toString());
        buildLayout("Case_Force__c", caso);
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
            if (isNewCase()) {
                onPostSaveCase();
            }
            commentInput.setText("");
            casoEditPresenter.fetchComments();
        } else {
            //TODO: display error message saving?
        }
    }

    private TextView createCommentView(String comment) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int bottomPadding = getResources().getDimensionPixelSize(R.dimen.space2);

        TextView commentView = new TextView(new ContextThemeWrapper(CasoEditActivity.this, R.style.Comment));
        commentView.setText(comment);
        commentView.setLayoutParams(params);
        commentView.setPadding(0, 0, 0, bottomPadding);

        return commentView;
    }

    @OnTextChanged(R.id.comment_input)
    @SuppressWarnings("unused")
    public void onCommentInput(CharSequence text) {
        saveComment.setEnabled(!TextUtils.isEmpty(text));
    }

    @OnClick(R.id.save_comment)
    @SuppressWarnings("unused")
    public void onSaveComment() {
        casoEditPresenter.saveComment(commentInput.getText()
                                                  .toString());
    }

    @Override
    public void setAttachments(List<Attachment> attachments) {
        attachmentsCount = attachments.size();

        Log.d(TAG, "Creating attachment views for " + attachmentsCount + " attachments");
        attachmentContainer.removeAllViews();
        if (attachments.isEmpty()) {
            emptyAttachments.setVisibility(View.VISIBLE);
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

        // hide remove button on attachment
        if (TextUtils.isEmpty(attachment.getId())) {
            ((ImageView) view.findViewById(R.id.remove_attachment)).setVisibility(View.GONE);
        }

        String createdDate = DateUtils.formatDateTimeShort(attachment.getCreatedDate());

        if (TextUtils.isEmpty(createdDate)) {
            ((TextView) view.findViewById(R.id.attachment_date)).setText(getString(R.string.pendiente));
        } else {
            ((TextView) view.findViewById(R.id.attachment_date)).setText(createdDate);
        }


        view.findViewById(R.id.remove_attachment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                casoEditPresenter.deleteAttachment(attachment.getId(), attachment.getName());
            }
        });

        return view;
    }

    @OnClick(R.id.add_attachment)
    @SuppressWarnings("unused")
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
        }
    }

    @Subscribe
    public void onAttachmentSaved(AttachmentEvent.AttachmentSavedEvent attachmentSavedEvent) {
        Log.d(TAG, "onAttachmentSaved CasoEditActivity success: " + attachmentSavedEvent.isSuccess());
        casoEditPresenter.fetchAttachments();
    }

    @Subscribe
    public void onAttachmentUpload(AttachmentEvent.AttachmentUploadEvent attachmentUploadEvent) {
        casoEditPresenter.fetchAttachments();
    }

    @Override
    public boolean picklistValidator(String fieldName, String oldValue, String newValue) {
        if (fieldName.equalsIgnoreCase(CasosFields.ESTADO__C)) {
            if (CaseStatusList__c.isValidStatusChange(oldValue, newValue)) {
                Log.i(TAG, "valid picklist change");
                return true;
            } else {
                Log.i(TAG, "invalid picklist change");
                showSnackbar(R.string.invalid_case_status_change);
                return false;
            }
        }
        return true;
    }

    @Override
    public void setEditable(boolean isEditable) {
        // this should not do anything
    }


}
