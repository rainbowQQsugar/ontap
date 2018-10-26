package com.salesforce.androidsyncengine.datamanager.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Pojo to abstract away the queue records in smartstore.
 * Has method to convert to/from json
 * @author usanaga, bduggirala
 */
public class ErrorObject {
	
	private static final String SOUP_ENTRY_ID_FIELD = "_soupEntryId";
	private static final String QUEUE_SOUP_ENTRY_ID_FIELD = "queueSoupEntryId";
	private static final String OBJECT_TYPE_FIELD = "objectType";
	private static final String ID_FIELD = "id";
	
	private static final String ADDITIONAL_INFO = "additionalInfo";
	// server returned values
	private static final String ERROR_CODE = "errorCode";
	private static final String ERROR_MESSAGE = "errorMessage";

	private static final String FIELDS_JSON_FIELD = "fieldsJson";
	
	private String id;
	private String objectType;
	private Long soupEntryId;
	private String additionalInfo;
	private String errorCode;
	private String errorMessage;
	
	private Long queueSoupEntryId;

	private String fieldsJsonFromQueue;
	
	public ErrorObject(String id, String objectType, String additionalInfo, String errorCode, String errorMessage,
					   Long queueSoupEntryId, String fieldsJsonFromQueue) {
		this.id = id;
		this.objectType = objectType;
		this.additionalInfo = additionalInfo;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.queueSoupEntryId = queueSoupEntryId;
		this.fieldsJsonFromQueue = fieldsJsonFromQueue;
	}
	
	public ErrorObject(JSONObject jsonObj) throws JSONException {
		this.id = jsonObj.getString(ID_FIELD);
		this.objectType = jsonObj.getString(OBJECT_TYPE_FIELD);
		this.soupEntryId = jsonObj.optLong(SOUP_ENTRY_ID_FIELD);
		this.additionalInfo = jsonObj.optString(ADDITIONAL_INFO);
		this.errorCode = jsonObj.optString(ERROR_CODE);
		this.errorMessage = jsonObj.optString(ERROR_MESSAGE);
		this.queueSoupEntryId = jsonObj.optLong(QUEUE_SOUP_ENTRY_ID_FIELD);
		this.fieldsJsonFromQueue = jsonObj.optString(FIELDS_JSON_FIELD);
	}

	public String getId() {
		return id;
	}

	public String getObjectType() {
		return objectType;
	}

	public Long getSoupEntryId() {
		return soupEntryId;
	}
	
	public Long getQueueSoupEntryId() {
		return queueSoupEntryId;
	}

	public String getFieldsJson() {
		return fieldsJsonFromQueue;
	}
	
	public String getAdditionalInfo() {
		return additionalInfo;
	}
	
	public String getErrorCode() {
		return errorCode;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public JSONObject toJson() throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(ID_FIELD, id);
		jsonObj.put(OBJECT_TYPE_FIELD, objectType);
		jsonObj.putOpt(SOUP_ENTRY_ID_FIELD, soupEntryId);
		jsonObj.putOpt(ERROR_CODE, errorCode);
		jsonObj.putOpt(ERROR_MESSAGE, errorMessage);
		jsonObj.put(QUEUE_SOUP_ENTRY_ID_FIELD, queueSoupEntryId);
		jsonObj.put(FIELDS_JSON_FIELD, fieldsJsonFromQueue);
		return jsonObj;
	}
	
	public String toString() {
		return ("Object: " + objectType +
				"\nId: " + id +
				"\nadditionalInfo: " + additionalInfo +
				"\nerrorCode: " + errorCode +
				"\nerrorMessage: " + errorMessage +
				"\nqueueSoupEntryId: " + queueSoupEntryId +
				"\nfieldsJson: " + fieldsJsonFromQueue);
	}
}
