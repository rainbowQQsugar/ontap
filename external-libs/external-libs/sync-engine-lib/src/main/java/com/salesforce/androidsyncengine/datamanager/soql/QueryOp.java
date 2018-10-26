package com.salesforce.androidsyncengine.datamanager.soql;

public enum QueryOp {
	gt (">"),
	lt ("<"),
	eq ("="),
	in ("IN"),
	like ("like");
	
	private String op;

	private QueryOp(String op) {
		this.op = op;
	}
	
	@Override
	public String toString() {
		return op;
	}
	
}
