package com.salesforce.androidsyncengine.datamanager.soql;

public class QueryInfo {
	
	private String query;
	private boolean overflow;
	private int page = 1;

	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public boolean isOverflow() {
		return overflow;
	}
	public void setOverflow(boolean overflow) {
		this.overflow = overflow;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public void incPage() {
		this.page++;
	}
}
