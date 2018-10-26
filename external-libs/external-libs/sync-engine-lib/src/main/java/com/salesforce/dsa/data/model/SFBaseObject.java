package com.salesforce.dsa.data.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.salesforce.androidsdk.smartstore.store.SmartStore;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.syncmanifest.FilterObject;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import com.salesforce.androidsyncengine.utils.CharUtils;
import com.salesforce.androidsyncengine.utils.Guava.GuavaUtils;
import com.salesforce.androidsyncengine.utils.JSONUtils;
import com.salesforce.androidsyncengine.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.salesforce.androidsyncengine.utils.SyncEngineConstants.StdFields;

public abstract class SFBaseObject implements Serializable {
	private static final long serialVersionUID = -6889091119717602537L;

	private static final String TAG = SFBaseObject.class.getSimpleName();

	private static final String NULL_STRING = "null";

	private String objectName;

	/** Copy of json which was used to initialize the object. Useful when checking for modified fields. */
    private JSONObject originalJson;

	private JSONObject delegate;

	public SFBaseObject(String objectName, JSONObject json) {
		this.delegate = json;
		this.objectName = objectName;
		updateOriginalJson();
	}

	protected SFBaseObject(String objectName) {
		this(objectName, new JSONObject());
	}

	public String getId() {
		return getStringValueForKey(StdFields.ID);
	}

	public void setId(String id) {
		setStringValueForKey(StdFields.ID, id);
	}

	public String getOwnerId() {
		return getStringValueForKey(StdFields.OWNERID);
	}

	public boolean getIsDeleted() {
		return getBooleanValueForKey(StdFields.ISDELETED);
	}

	public String getName() {
		return getStringValueForKey(StdFields.NAME);
	}

	public void setName(String name) {
		setStringValueForKey(StdFields.NAME, name);
	}

	public String getCreatedDate() {
		return getStringValueForKey(StdFields.CREATED_DATE);
	}

	public long getSoupLastModifiedDate() {
		return getLongValueForKey(SmartStore.SOUP_LAST_MODIFIED_DATE);
	}

	public String getCreatedById() {
		return getStringValueForKey(StdFields.CREATED_BY_ID);
	}

	public String getLastModifiedDate() {
		return getStringValueForKey(StdFields.LAST_MODIFIED_DATE);
	}

	public String getLastModifiedById() {
		return getStringValueForKey(StdFields.LAST_MODIFIED_BY_ID);
	}

	public String getSystemModstamp() {
		return getStringValueForKey(StdFields.SYSTEM_MOD_STAMP);
	}

	public String getObjectName() {
		return objectName;
	}

	public JSONObject toJson() {
		return delegate;
	}

	/** Returns only fields that were modified. */
	public JSONObject getModifiedData() {
        JSONObject result = new JSONObject();
        Set<String> fieldNames = new HashSet<>();
        Iterator<String> iterator = delegate.keys();
        while (iterator.hasNext()) {
            fieldNames.add(iterator.next());
        }

        iterator = originalJson.keys();
        while (iterator.hasNext()) {
            fieldNames.add(iterator.next());
        }

        for (String fieldName : fieldNames) {

            Object originalValue = originalJson.opt(fieldName);
			if (isEmpty(originalValue)) {
				originalValue = JSONObject.NULL;
			}

            Object currentValue = delegate.opt(fieldName);
			if (isEmpty(currentValue)) {
				currentValue = JSONObject.NULL;
			}

            if (!JSONUtils.deepEquals(originalValue, currentValue)) {
                JSONUtils.silentPut(result, fieldName, currentValue);
            }
        }

        return result;
	}

	private void updateOriginalJson() {
		originalJson = JSONUtils.deepCopy(delegate);
	}

	public void remove(String key) {
		delegate.remove(key);
	}

	public final String createRecord() {
		DataManager dataManager = DataManagerFactory.getDataManager();
		String id = dataManager.createRecord(objectName, getModifiedData());
		setId(id);

		if (id != null) {
			updateOriginalJson();
		}

		return id;
	}

	public final boolean updateRecord() {
		DataManager dataManager = DataManagerFactory.getDataManager();
		boolean success = dataManager.updateRecord(objectName, getId(), getModifiedData());
		if (success) {
			updateOriginalJson();
		}

		return success;
	}

	public final boolean upsertRecord() {
		if (TextUtils.isEmpty(getId())) {
			return createRecord() != null;
		}
		else {
			return updateRecord();
		}
	}

	private boolean isEmpty(Object o) {
		return o == null || (o instanceof CharSequence && TextUtils.isEmpty((CharSequence) o));
	}

	public boolean isNullValue(String key) {
		String result = delegate.optString(key, null);
		return result == null || NULL_STRING.equals(result);
	}

	public boolean isNullOrEmpty(String key) {
		String result = delegate.optString(key, null);
		return TextUtils.isEmpty(result) || NULL_STRING.equals(result);
	}

	public Object getValueForKey(String key) {
		Object obj = delegate.opt(key);

		if (obj == JSONObject.NULL) {
			obj = null;
		}
		else if (obj instanceof String) {
			String result = (String) obj;
			if (NULL_STRING.equals(result)) {
				obj = StringUtils.EMPTY;
			}
		}

		return obj;
	}

	public void setValueForKey(String key, Object value) {
		try {
			delegate.put(key, value == null ? JSONObject.NULL : value);
		} catch (JSONException e) {
			Log.e(TAG, "Error setting value for key: " + key, e);
		}
	}

	public String getStringValueForKey(String key) {
		return readString(delegate, key);
	}

	/**
	 * Returns value for relational field name, i.e. specific FirstName for
	 * Contact.Account.Owner.FirstName.
	 */
	public String getRelationalStringValue(String relationalName) {
		return readString(getParentObject(relationalName), getFieldName(relationalName));
	}

	/**
	 * Returns value for relational field name, i.e. specific FirstName for
	 * Contact.Account.Owner.FirstName.
	 */
	public String getRelationalStringValueWithNamespace(Context context, String relationalName) {
		JSONObject jsonObject = getParentObjectWithNamespace(context, relationalName);
		if (jsonObject != null) {
			String objectName = getObjectName(jsonObject);
			if (objectName != null) {
				String fieldName = ManifestUtils.removeNamespaceFromField(objectName, getFieldName(relationalName), context);
				return readString(jsonObject, fieldName);
			}
		}

		return null;
	}

	/**
	 * Returns value for relational field name, i.e. specific FirstName for
	 * Contact.Account.Owner.FirstName.
	 */
	public String getRelationalStringValue(String... relationChain) {
		return readString(getParentObject(relationChain), getFieldName(relationChain));
	}

	private String readString(JSONObject jsonObject, String key) {
		if (jsonObject == null) return StringUtils.EMPTY;

		String result = jsonObject.optString(key, null);
		if (result == null || NULL_STRING.equals(result)) {
			return StringUtils.EMPTY;
		}
		return result;
	}

	protected boolean getBooleanValueForKey(String key) {
		return delegate.optBoolean(key, false);
	}

	protected int getIntValueForKey(String key) {
		return delegate.optInt(key, 0);
	}

	protected long getLongValueForKey(String key) {
		return delegate.optLong(key, 0);
	}

	protected double getDoubleValueForKey(String key) {
		return delegate.optDouble(key);
	}

	protected Date getDateValueForKey(String key) {
		String text = getStringValueForKey(key);

		return getDateValueByString(text);

	}

	protected Date getDateValueByString(String text) {
		SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

		if (!GuavaUtils.isNullOrEmpty(text)) {
			try {
				return parseFormat.parse(text);
			} catch (ParseException e) {
				Log.e(TAG, "Error parsing date: " + text, e);
			}
		}
		return null;
	}

	protected JSONObject getJsonObject(String key) {
		return delegate.optJSONObject(key);
	}

	public void setStringValueForKey(String key, String value) {
		try {
			delegate.put(key, value);
		} catch (JSONException e) {
			Log.e(TAG, "Error setting string value for key: " + key, e);
		}
	}

	protected void setBooleanValueForKey(String key, boolean value) {
		try {
			delegate.put(key, value);
		} catch (JSONException e) {
			Log.e(TAG, "Error setting boolean value for key: " + key, e);
		}
	}

	public void setIntValueForKey(String key, int value) {
		try {
			delegate.put(key, value);
		} catch (JSONException e) {
			Log.e(TAG, "Error setting int value for key: " + key, e);
		}
	}

	public void setLongValueForKey(String key, long value) {
		try {
			delegate.put(key, value);
		} catch (JSONException e) {
			Log.e(TAG, "Error setting long value for key: " + key, e);
		}
	}

	protected void setDoubleValueForKey(String key, double value) {
		try {
			delegate.put(key, value);
		} catch (JSONException e) {
			Log.e(TAG, "Error setting double value for key: " + key, e);
		}
	}

	protected void setJsonObject(String key, JSONObject value) {
		try {
			delegate.put(key, value);
		} catch (JSONException e) {
			Log.e(TAG, "Error setting json value for key: " + key, e);
		}
	}

	protected void setNullValue(String key) {
		try {
			delegate.put(key, JSONObject.NULL);
		} catch (JSONException e) {
			Log.e(TAG, "Error setting json value for key: " + key, e);
		}
	}

	public Address getAddress(String fieldName) {
		Address address = new Address();

		if (!TextUtils.isEmpty(fieldName)) {
			String addressFieldPrefix = Address.getAddressFieldPrefix(fieldName);

			for (String addressFieldSuffix : Address.ADDRESS_FIELD_SUFFIXES) {
				String addressField = addressFieldPrefix + addressFieldSuffix;
				Object baseObjectValue = getValueForKey(addressField);
				address.setValue(addressFieldSuffix.toLowerCase(), baseObjectValue);
			}
		}

		return address;
	}

	public void setAddress(String fieldName, Address address) {
		if (TextUtils.isEmpty(fieldName)) return;

		String addressFieldPrefix = Address.getAddressFieldPrefix(fieldName);

		for (String addressFieldSuffix : Address.ADDRESS_FIELD_SUFFIXES) {
			Object value = address == null ? null : address.getValue(addressFieldSuffix.toLowerCase());
			String addressField = addressFieldPrefix + addressFieldSuffix;
			setValueForKey(addressField, value);
		}
	}

	@Override
	public String toString() {
		return delegate == null ? null : delegate.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SFBaseObject other = (SFBaseObject) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	public List<FilterObject> getAdditionalFilters(Context context) {

		return new ArrayList<FilterObject>();
	}

	public String getReferencedValue(String objectReferenced, String fieldName) {
		try {
			String Id = getStringValueForKey(fieldName);
			if (Id == null) return null;
			JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(objectReferenced, "Id", Id);
			if (jsonObject != null) {
				return jsonObject.optString("Name", null);
			}
		} catch (Exception e) {
			return null;
		}

		return null;
	}

	public static String getReferencedValueForId(String objectReferenced, String referencedId) {
		try {
			if (referencedId == null) return null;
			JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(objectReferenced, "Id", referencedId);
			if (jsonObject != null) {
				return jsonObject.optString("Name", null);
			}
		} catch (Exception e) {
			return null;
		}

		return null;
	}

	public String getReferencedValueObjectField(String objectReferenced, String fieldName, String referencedObjectField) {
		try {
			String Id = getStringValueForKey(fieldName);
			if (Id == null) return null;
			JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(objectReferenced, "Id", Id);
			if (jsonObject != null) {
				return jsonObject.optString(referencedObjectField, null);

			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	/**
     * Returns parent JSONObject for relational field name
	 * (i.e. Owner for Contact.Account.Owner.FirstName).
	 */
	private JSONObject getParentObject(String... relationChain) {
		if (relationChain == null) return null;

		JSONObject currentObject = delegate;

		for (int i = 0; i < relationChain.length - 1 && currentObject != null; i++) {
			currentObject = currentObject.optJSONObject(relationChain[i]);
		}

		return currentObject;
	}

	/**
	 * Returns parent JSONObject for relational field name
	 * (i.e. Owner for Contact.Account.Owner.FirstName).
	 */
	private JSONObject getParentObjectWithNamespace(Context context, String... relationChain) {
		if (relationChain == null) return null;

		JSONObject currentObject = delegate;
		String currentObjectName = getObjectName();

		for (int i = 0; i < relationChain.length - 1 && currentObject != null; i++) {
			if (currentObjectName == null) {
				return null;
			}
			else {
				String fieldName = ManifestUtils.removeNamespaceFromField(currentObjectName, relationChain[i], context);
				currentObject = currentObject.optJSONObject(fieldName);
				currentObjectName = getObjectName(currentObject);
			}
		}

		return currentObject;
	}

	/**
	 * Returns parent JSONObject for relational field name
	 * (i.e. Owner for Contact.Account.Owner.FirstName).
	 */
	private JSONObject getParentObject(String relationalName) {
		return TextUtils.isEmpty(relationalName) ? null : getParentObject(relationalName.split("\\.")); //Split by dot.
	}

	/**
	 * Returns parent JSONObject for relational field name
	 * (i.e. Owner for Contact.Account.Owner.FirstName).
	 */
	private JSONObject getParentObjectWithNamespace(Context context, String relationalName) {
		return TextUtils.isEmpty(relationalName) ? null : getParentObjectWithNamespace(context, relationalName.split("\\.")); //Split by dot.
	}

	/**
	 * Returns field name (last element) from relational name.
	 * I.e. returns FirstName from Contact.Account.Owner.FirstName.
	 */
	private String getFieldName(String relationalName) {
		if (TextUtils.isEmpty(relationalName)) return relationalName;

		int indexOfDot = relationalName.lastIndexOf(CharUtils.DOT);
		return indexOfDot < 0 || indexOfDot >= relationalName.length() ?
				relationalName : relationalName.substring(indexOfDot + 1);
	}

	/**
	 * Returns field name (last element) from relation chain.
	 * I.e. returns FirstName from Contact.Account.Owner.FirstName.
	 */
	private String getFieldName(String... relationChain) {
		return relationChain == null || relationChain.length == 0 ?
				null : relationChain[relationChain.length - 1];
	}

	/* Serializable */
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeUTF(objectName);
		out.writeUTF(delegate.toString());
		out.writeUTF(originalJson.toString());
	}

	/* Serializable */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException, JSONException {
		objectName = in.readUTF();
		delegate = new JSONObject(in.readUTF());
		originalJson = new JSONObject(in.readUTF());
	}

	public static String getObjectName(JSONObject jsonObject) {
		if (jsonObject == null) return null;

		JSONObject attributes = jsonObject.optJSONObject("attributes");
		if (attributes != null) {
			return attributes.optString("type");
		}

		return null;
	}
}
