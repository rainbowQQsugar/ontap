package com.abinbev.dsa.model;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.NoteFields;
import com.abinbev.dsa.utils.DateUtils;
import com.salesforce.androidsdk.accounts.UserAccountManager;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

/**
 * Created by nchangnon on 11/24/15.
 */
public class Note extends SFBaseObject {
    public static final String TAG = Note.class.getSimpleName();

    public Note(JSONObject json) {
        super(AbInBevObjects.NOTE, json);
    }

    public Note() {
        super(AbInBevObjects.NOTE);
    }

    public String getBody() {
        return getStringValueForKey(NoteFields.BODY);
    }

    public String getTitle() {
        return getStringValueForKey(NoteFields.TITLE);
    }

    //to handle sorting be created date when note synced, this no created date.
    public String getSoupLastModified() {
        return getStringValueForKey("_soupLastModifiedDate");
    }

    public void setId(String id) {
        try {
            toJson().put(SyncEngineConstants.StdFields.ID, id);
        } catch (JSONException e) {
            Log.e(TAG, "Exception trying to set id", e);
        }
    }

    public void setTitle(String title) {
        try {
            toJson().put(NoteFields.TITLE, title);
        } catch (JSONException e) {
            Log.e(TAG, "Exception trying to set title", e);
        }
    }

    public void setBody(String body) {
        try {
            toJson().put(NoteFields.BODY, body);
        } catch (JSONException e) {
            Log.e(TAG, "Exception trying to set body", e);
        }
    }

    public static Note getLatestNoteByParentId(String parentId) {
        List<Note> notes = getLatestNotesByParentId(parentId);
        return notes == null || notes.isEmpty() ? null : notes.get(0);
    }

    public static List<Note> getLatestNotesByParentId(String parentId) {

        //need to get all then sort to handle nulls properly.
        String smartSqlFilter = String.format("{%s:%s} = '%s'",
                AbInBevObjects.NOTE, NoteFields.PARENT_ID, parentId);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.NOTE, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

        List<Note> results = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                results.add(new Note(jsonObject));
            }

            Collections.sort(results, new Comparator<Note>() {

                @Override
                //createdDate, nulls first, then soupLastModifiedDate descending order
                public int compare(Note lhs, Note rhs) {

                    Date lhsDate = getDate(lhs);
                    Date rhsDate = getDate(rhs);

                    if (lhsDate == rhsDate) {
                        return 0;
                    }

                    if (lhsDate == null) {
                        return -1;
                    }

                    if (rhsDate == null) {
                        return 1;
                    }

                    return rhsDate.compareTo(lhsDate);
                }

                private Date getDate(Note note) {
                    Date result = null;


                    // Try to get created date.
                    String createdDate = note.getCreatedDate();
                    if (!TextUtils.isEmpty(createdDate)) {
                        try {
                            result = DateUtils.SERVER_DATE_TIME_FORMAT.parse(createdDate);
                        } catch (ParseException e) {
                            Log.w(TAG, e);
                        }
                    }

                    // If there is no "created date" take soup last modified date.
                    if (result == null) {
                        result = new Date(Long.valueOf(note.getSoupLastModified()));
                    }

                    return result;
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Exception in getting newest Note for parent: " + parentId, e);
        }

        return results;
    }

    public static JSONObject createJSONNote(String accountId) {
        if (accountId == null || TextUtils.isEmpty(accountId)) {
            Log.e(TAG, "Unable to create a note without an account.");
            return null;
        }
        String id = UserAccountManager.getInstance().getStoredUserId();
        if (TextUtils.isEmpty(id)) {
            Log.e(TAG, "Unable to create a note without a user id.");
            return null;
        }
        JSONObject json = new JSONObject();
        try {
            json.put(NoteFields.PARENT_ID, accountId);
            json.put(NoteFields.OWNER_ID, id);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating new note", e);
            return null;
        }
        return json;
    }

    public static Note getById(String id) {
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(AbInBevObjects.NOTE, SyncEngineConstants.StdFields.ID, id);
        if (jsonObject != null) {
            return new Note(jsonObject);
        }
        return null;
    }

    public static Note createNote(String accountId) {
        JSONObject jsonNote = Note.createJSONNote(accountId);
        return new Note(jsonNote);
    }

    public static Note createNote(JSONObject noteJSON) {
        String tempId = DataManagerFactory.getDataManager().createRecord(AbInBevObjects.NOTE, noteJSON);
        Note note = new Note(noteJSON);
        note.setId(tempId);
        return note;
    }

    public boolean updateNote() {
        JSONObject updatedObject = this.toJson();
        DataManager dataManager = DataManagerFactory.getDataManager();
        return dataManager.updateRecord(AbInBevObjects.NOTE, this.getId(), updatedObject);
    }

    public static Observable<NotesDetails> notesByAccountId(String accountId) {
        return Observable.fromCallable(() -> {
            List<Note> notes = Note.getLatestNotesByParentId(accountId);
            Map<String, User> users = new HashMap<>();

            for (Note note : notes) {
                if (!users.containsKey(note.getCreatedById())) {
                    User user = User.getUserByUserId(note.getCreatedById());
                    users.put(note.getCreatedById(), user);
                }
            }

            return new NotesDetails(notes, users);
        });
    }

    public static class NotesDetails {
        public final List<Note> notes;
        public final Map<String, User> users;

        public NotesDetails(List<Note> notes, Map<String, User> users) {
            this.notes = notes;
            this.users = users;
        }
    }
}
