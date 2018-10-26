package com.salesforce.androidsyncengine.syncmanifest;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import com.salesforce.androidsyncengine.datamanager.soql.QueryOp;

public class FilterObject {
	
	@NotNull
	@NotBlank
	private String field;
	
	@NotNull
	@NotBlank
	private Object value;
	
	@NotNull
	private QueryOp op;
	
	public String getField() {
		return field;
	}
	public Object getValue() {
		return value;
	}
	public QueryOp getOp() {
		return op;
	}
	public void setField(String field) {
		this.field = field;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public void setOp(QueryOp op) {
		this.op = op;
	}
}

