package com.salesforce.androidsyncengine.datamanager.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Pojo to abstract away the queue records in smartstore.
 * Has method to convert to/from json
 * @author usanaga, bduggirala
 */
public class QueueObject {

	public static final String ID_FIELD = "Id";
	public static final String OBJECT_TYPE_FIELD = "objectType";
	public static final String OPERATION_FIELD = "operation";
	public static final String FIELDS_FIELD = "fields";
	public static final String SOUP_ENTRY_ID_FIELD = "_soupEntryId";
	public static final String QUERY_FIELD = "query";
	public static final String RETRY_COUNT_FIELD = "retryCount";
	
	private String id;
	private String objectType;
	private QueueOperation operation;
	private JSONObject fieldsJson;
	private Long soupEntryId;
	private String query;
	private int retryCount;
	
	public QueueObject(String id, String objectType, QueueOperation operation) {
		this.id = id;
		this.objectType = objectType;
		this.operation = operation;
		this.retryCount = 0;
	}

	public QueueObject(JSONObject jsonObj) throws JSONException {
		this.id = jsonObj.getString(ID_FIELD);
		this.objectType = jsonObj.getString(OBJECT_TYPE_FIELD);
		this.operation = QueueOperation.valueOf(jsonObj.getString(OPERATION_FIELD));
		this.soupEntryId = jsonObj.optLong(SOUP_ENTRY_ID_FIELD);
		if (!"".equals(jsonObj.optString(FIELDS_FIELD))) {
			this.fieldsJson = new JSONObject(jsonObj.getString(FIELDS_FIELD));
		}
		this.query = jsonObj.optString(QUERY_FIELD);
		this.retryCount = jsonObj.getInt(RETRY_COUNT_FIELD);
	}

	public QueueObject(QueueObject other) throws JSONException {
		this.id = other.id;
		this.objectType = other.objectType;
		this.operation = other.operation;
		this.soupEntryId = other.soupEntryId;
		this.query = other.query;
		this.retryCount = other.retryCount;
		if (other.fieldsJson != null) {
			this.fieldsJson = new JSONObject(other.getFieldsJson().toString());
		}
	}

	public Object getField(String fieldName) {
		return fieldsJson == null ? null : fieldsJson.opt(fieldName);
	}

	public boolean hasField(String fieldName) {
		return fieldsJson != null && fieldName.contains(fieldName);
	}

	public Object removeField(String fieldName) {
		return fieldsJson == null ? null : fieldsJson.remove(fieldName);
	}

	public JSONObject getFieldsJson() {
		return fieldsJson;
	}

	public void setFieldsJson(JSONObject fieldsJson) {
		this.fieldsJson = fieldsJson;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getObjectType() {
		return objectType;
	}

	public QueueOperation getOperation() {
		return operation;
	}
	
	public int getRetryCount() {
		return retryCount;
	}
	
	public void incrementRetryCount() {
		if (retryCount < Integer.MAX_VALUE) {
			retryCount++;
		}
	}

	public Long getSoupEntryId() {
		return soupEntryId;
	}
	
	public void setSoupEntryId(long soupEntryId) {
		this.soupEntryId = soupEntryId;
	}
	
	public JSONObject toJson() throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(ID_FIELD, getId());
		jsonObj.put(OBJECT_TYPE_FIELD, getObjectType());
		jsonObj.put(OPERATION_FIELD, getOperation().name());
		jsonObj.putOpt(SOUP_ENTRY_ID_FIELD, getSoupEntryId());
		jsonObj.putOpt(FIELDS_FIELD, getFieldsJson());
		jsonObj.putOpt(QUERY_FIELD, getQuery());
		jsonObj.put(RETRY_COUNT_FIELD, getRetryCount());
		
		return jsonObj;
	}
}
