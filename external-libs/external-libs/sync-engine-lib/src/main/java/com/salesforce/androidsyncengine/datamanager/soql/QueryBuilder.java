package com.salesforce.androidsyncengine.datamanager.soql;

/**
 * This class exposes {@link Query} outside of the package
 * All usages must instantiate it through this class 
 * @author usanaga
 */
public class QueryBuilder {
	
	private QueryBuilder() {};
	
	/*
	 * Use this to start building a new query
	 */
	public static Query query(int page, int pageSize) {
		return new Query(page, pageSize);
	}

}
