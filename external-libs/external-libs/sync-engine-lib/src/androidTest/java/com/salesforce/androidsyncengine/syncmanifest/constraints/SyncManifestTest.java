package com.salesforce.androidsyncengine.syncmanifest.constraints;

import java.io.InputStream;

import android.test.ActivityInstrumentationTestCase2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.androidsyncengine.SyncEngine;
import com.salesforce.androidsyncengine.R;
import com.salesforce.androidsyncengine.syncmanifest.SyncManifest;
import com.salesforce.androidsyncengine.utils.ObjectMapperFactory;

public class SyncManifestTest extends ActivityInstrumentationTestCase2<SyncEngine> {

	public SyncManifestTest() {
		super(SyncEngine.class);
	}

	// all good and no violations
	public void testAllGoodCase() {
		int fileId = R.raw.sync_manifest;
		String message = validateFile(fileId);
		assertNull(message);
	}
	
	public void testInvalidFields() {
		int fileId = R.raw.fieldsto_fetch_and_ignore;
		String message = validateFile(fileId);
		assertNotNull(message);
		assertTrue(message.contains("must have one and only one of fieldsToIgnore, fieldsToFetch, fetchAllFields"));
	}

	public void testInvalidFieldsToIndex() {
		int fileId = R.raw.invalid_fields_to_index;
		String message = validateFile(fileId);
		assertNotNull(message);
		assertTrue(message.contains("cannot be null") || message.contains("cannot be empty"));
	}
	
	public void testInvalidObjectId() {
		int fileId = R.raw.invalid_object_id;
		String message = validateFile(fileId);
		assertNotNull(message);
		assertTrue(message.contains("must be 15 or 18 characters long"));
	}
	
	public void testInvalidOrderByField() {
		int fileId = R.raw.invalid_orderby_field;
		String message = validateFile(fileId);
		assertNotNull(message);
		assertTrue(message.contains("should be either ASC or DESC"));
	}
	
	public void testNoConfiguration() {
		int fileId = R.raw.no_configuration;
		String message = validateFile(fileId);
		assertNotNull(message);
		assertTrue(message.contains("cannot be null"));
	}
	
	public void testNoObjectName() {
		int fileId = R.raw.no_objectname;
		String message = validateFile(fileId);
		assertNotNull(message);
		assertTrue(message.contains("cannot be null"));
	}
	
	public void testNoSfObject() {
		int fileId = R.raw.no_sfobject;
		String message = validateFile(fileId);
		assertNotNull(message);
		assertTrue(message.contains("cannot be null"));
	}
	
	public void testZeroSyncObjects() {
		int fileId = R.raw.zero_sync_objects;
		String message = validateFile(fileId);
		assertNotNull(message);
		assertTrue(message.contains("does not have between 1 and 100 elements"));
	}
	
	private SyncManifest getSyncManifest(InputStream inputStream) {
		SyncManifest syncManifest = null;

		ObjectMapper mapper = ObjectMapperFactory.createMapper();
		try {
			syncManifest = mapper.readValue(inputStream, SyncManifest.class);
		} catch (Exception e) {
		}
		return syncManifest;
	}
	
	private String validateFile(int fileId) {
		InputStream is = getActivity().getResources().openRawResource(fileId);
		SyncManifest syncManifest = getSyncManifest(is);
		
		String message = ValidationUtil.validate(syncManifest);
		return message;
	}
}
