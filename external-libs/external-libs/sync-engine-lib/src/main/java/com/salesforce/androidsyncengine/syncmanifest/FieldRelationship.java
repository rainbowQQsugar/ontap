package com.salesforce.androidsyncengine.syncmanifest;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

public class FieldRelationship {
	
	@NotNull
	@NotBlank
	private String fieldName;
	@NotNull
	@NotBlank
	private String value;
	
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
