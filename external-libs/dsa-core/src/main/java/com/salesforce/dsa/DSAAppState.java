package com.salesforce.dsa;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.salesforce.dsa.utils.DataUtils;
import com.salesforce.androidsyncengine.utils.Guava.GuavaUtils;
import com.salesforce.dsa.data.model.Contact;
import com.salesforce.dsa.data.model.ContentVersion;
import com.salesforce.dsa.data.model.TrackedDocument;

import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DSAAppState {

    public enum DocumentTrackingType {
        DocumentTracking_None,
        DocumentTracking_SelectedContact,
        DocumentTracking_DeferredContact,
        DocumentTracking_AlwaysOn
    }

    private static DSAAppState INSTANCE = null;
    private List<TrackedDocument> trackedDocuments = new ArrayList<>();
    private Contact currentTrackingContact;
    private TrackedDocument currentTrackingDocument;
    private Deque<Contact> recentContacts;
    private Date checkInStart;
    private Date checkInEnd;
    private Deque<ContentVersion> historyItems;
    private DocumentTrackingType trackingType;
    private boolean enableTrackingWithoutCheckin;

    private DSAAppState() {
    }

    public static DSAAppState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DSAAppState();
        }
        return INSTANCE;
    }

    public List<TrackedDocument> getTrackedDocuments() {
        return trackedDocuments;
    }

    public Contact getCurrentTrackingContact() {
        return currentTrackingContact;
    }

    public void setCurrentTrackingContact(Contact currentTrackingContact) {
        this.currentTrackingContact = currentTrackingContact;
    }

    public TrackedDocument getCurrentTrackingDocument() {
        return currentTrackingDocument;
    }

    public void setCurrentTrackingDocument(TrackedDocument currentTrackingDocument) {
        this.currentTrackingDocument = currentTrackingDocument;
    }

    public Date getCheckInStart() {
        return checkInStart;
    }

    public void setCheckInStart(Date checkInStart) {
        this.checkInStart = checkInStart;
    }

    public Date getCheckInEnd() {
        return checkInEnd;
    }

    public void setCheckInEnd(Date checkInEnd) {
        this.checkInEnd = checkInEnd;
    }

    @SuppressWarnings("unchecked")
    public List<Contact> getRecentContacts() {
        if (recentContacts == null) {
            recentContacts = new LinkedList<>();
        }
        return (List<Contact>) recentContacts;
    }

    @SuppressWarnings("unchecked")
    public void setRecentContacts(List<Contact> contacts) {
        this.recentContacts = (Deque<Contact>) contacts;
    }

    @SuppressWarnings("unchecked")
    public List<ContentVersion> getHistoryItems() {
        if (historyItems == null) {
            historyItems = new LinkedList<>();
        }
        return (List<ContentVersion>) historyItems;
    }

    @SuppressWarnings("unchecked")
    public void setHistoryItems(List<ContentVersion> historyItems) {
        this.historyItems = (Deque<ContentVersion>) historyItems;
    }

    public DocumentTrackingType getTrackingType() {
        return trackingType;
    }

    public void setTrackingType(DocumentTrackingType trackingType) {
        this.trackingType = trackingType;
    }

    public boolean isEnableTrackingWithoutCheckin() {
        return enableTrackingWithoutCheckin;
    }

    public void setEnableTrackingWithoutCheckin(boolean enableTrackingWithoutCheckin) {
        if (enableTrackingWithoutCheckin) {
            if (this.trackingType == DocumentTrackingType.DocumentTracking_None)
                this.trackingType = DocumentTrackingType.DocumentTracking_AlwaysOn;
        } else {
            if (this.trackingType == DocumentTrackingType.DocumentTracking_AlwaysOn) {
                this.trackingType = DocumentTrackingType.DocumentTracking_None;
            }
        }
    }

    public void addTrackedDocument(ContentVersion contentVersion) {
        TrackedDocument td = new TrackedDocument(contentVersion);
        this.currentTrackingDocument = td;
        List<String> ids = new ArrayList<String>();
        for (TrackedDocument trackedDoc : trackedDocuments) {
            ids.add(trackedDoc.getContentVersion().getId());
        }
        if (!ids.contains(td.getContentVersion().getId()))
            this.trackedDocuments.add(td);
    }

    public void addRecentContact(Contact contact) {
        // check for duplicates
        // if exists remove and add to queue
        if (recentContacts == null) {
            recentContacts = new LinkedList<>();
        }
        if (recentContacts.contains(contact)) {
            recentContacts.remove(contact);
        }
        if (recentContacts.size() >= 25) {
            recentContacts.remove();
        }
        recentContacts.add(contact);
    }

    public void addContentItemToHistory(ContentVersion contentVersion, Context context) {
        // check for duplicates
        // if exists remove and add to queue
        if (historyItems == null) {
            historyItems = new LinkedList<>();
        }
        if (historyItems.contains(contentVersion)) {
            historyItems.remove(contentVersion);
        }
        if (historyItems.size() >= 30) {
            historyItems.remove();
        }
        historyItems.add(contentVersion);
        saveHistoryItems(context);
    }

    public void saveRecentContacts(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = prefs.edit();
        List<String> ids = GuavaUtils.transform(getRecentContacts(), new GuavaUtils.Function<Contact, String>() {
            public String apply(Contact obj) {
                return obj.getId();
            }
        });
        Set<String> recentContactIds = new HashSet<String>(ids);
        editor.putStringSet("RecentContacts", recentContactIds);
        editor.commit();
    }

    public List<Contact> getRecentContactsFromPrefs(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> contactIds = prefs.getStringSet("RecentContacts", null);
        List<Contact> contacts = new LinkedList<>();
        if (contactIds != null) {
            contacts = DataUtils.fetchContactsForIds(new ArrayList<String>(contactIds));
        }
        return contacts;
    }

    public void saveHistoryItems(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = prefs.edit();
        List<String> ids = GuavaUtils.transform(getHistoryItems(), new GuavaUtils.Function<ContentVersion, String>() {
            public String apply(ContentVersion obj) {
                return obj.getId();
            }
        });
        Set<String> historyIds = new HashSet<String>(ids);
        editor.putStringSet("HistoryItems", historyIds);
        editor.commit();
    }

    public List<ContentVersion> getHistoryItemsFromPrefs(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> historyIds = prefs.getStringSet("HistoryItems", null);
        List<ContentVersion> historyItems = new LinkedList<>();
        if (historyIds != null) {
            historyItems = DataUtils.fetchContentForIds(new ArrayList<String>(historyIds));
        }
        return historyItems;
    }

    public void startDocumentTrackingForContact(Contact contact) {
        setTrackingType(DocumentTrackingType.DocumentTracking_SelectedContact);
        setCurrentTrackingContact(contact);
        addRecentContact(contact);
    }

    public void stopDocumentTracking() {
        if (this.isEnableTrackingWithoutCheckin()) {
            setTrackingType(DocumentTrackingType.DocumentTracking_AlwaysOn);
        } else {
            setTrackingType(DocumentTrackingType.DocumentTracking_None);
        }
        this.trackedDocuments.removeAll(getTrackedDocuments());
        setCurrentTrackingContact(null);
    }

    public boolean isCheckout() {
        boolean isCheckout = this.trackingType == DocumentTrackingType.DocumentTracking_SelectedContact ||
                this.trackingType == DocumentTrackingType.DocumentTracking_DeferredContact ||
                this.trackingType == DocumentTrackingType.DocumentTracking_AlwaysOn; // added alwaysOn check for SAB Miller and please test it in base dsa
        // and make sure it doesnt break default checkin/checkout flow
        return isCheckout;
    }

    public void startViewingDocument(ContentVersion contentVersion, Context ctx) {
        // add item to history list
        addContentItemToHistory(contentVersion, ctx);

        // if isCheckout add opened docs to tracked docs
        if (isCheckout()) {
            addTrackedDocument(contentVersion);
        }
    }

    public void stopViewingDocument(Context ctx) {
        // if the current document time is tracking
        // set end time and reset tracking state
        TrackedDocument td = this.currentTrackingDocument;
        if (td != null && td.isTracking()) {
            td.setEndTimeMillis(System.currentTimeMillis());
            td.setTracking(false);
        }
        // if the tracking is always on
        // create review and Event objects
        // and push changes to server
        if (this.trackingType == DocumentTrackingType.DocumentTracking_AlwaysOn) {
            Intent intent = new Intent("com.salesforce.dsa.DocumentStoppedViewing");
            ctx.sendBroadcast(intent);
        }
    }
}
