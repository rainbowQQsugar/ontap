package com.salesforce.androidsyncengine.datamanager.soql;

import com.salesforce.androidsyncengine.syncmanifest.FilterObject;
import com.salesforce.androidsyncengine.syncmanifest.OrderByField;
import com.salesforce.androidsyncengine.utils.Guava.Joiner;

import java.util.ArrayList;
import java.util.List;

/**
 * Query class that encapsulates building soql query
 * @author usanaga
 */
public class Query {
	
	private static final String COMMA = ",";
	private static final String ORDER_BY = " ORDER BY ";
	private static final String LIMIT = " LIMIT ";
	private static final char SPACE = ' ';
	private static final String AND = " AND ";
	private static final String TICK = "'";
	private static final String WHERE = " WHERE ";
	private static final String FROM = " FROM ";
	private static final String SELECT = "SELECT ";
	private static Joiner JOINER = Joiner.on(COMMA);
	
	private StringBuilder selectClauseBuilder;
	private String fromClause;
	private StringBuilder whereClauseBuilder;
	private StringBuilder orderByClauseBuilder;
	private String limitClause;

	private int page = -1;
	private int pageSize;
	
	Query(int page, int pageSize) {
		this.page = page;
		this.pageSize = pageSize;
	}
	
	/**
	 * Builds the SELECT clause from a given list of fields 
	 * @param fields
	 * @return
	 */
	public Query select(List<String> fields) {
		if (selectClauseBuilder == null) {
			selectClauseBuilder = new StringBuilder(SELECT);
		} else {
			selectClauseBuilder.append(COMMA);
		}
		
		selectClauseBuilder.append(JOINER.join(fields));
		
		return this;
	}
	/**
	 * Replaces the existing select query with new fields
	 * @param fields
	 * @return
	 */
	public Query replaceSelect(List<String> fields) {
		selectClauseBuilder = null;
		return select(fields);
	}
	
	/**
	 * Builds FROM clause with objectName
	 * @param objectName
	 * @return
	 */
	public Query from(String objectName) {
		fromClause = FROM + objectName;
		return this;
	}
	
	/**
	 * Applies the > filter on a given field and value
	 * @param field
	 * @param value
	 * @return
	 */
	public Query gt(String field, Object value) {
		return filter(field, value, QueryOp.gt);
	}
	
	public Query filter(FilterObject filterObject) {
		return filter(filterObject.getField(), filterObject.getValue(), filterObject.getOp());
	}
	
	public Query filter(List<FilterObject> filterObjects) {
		for (FilterObject filterObject : filterObjects) {
			filter(filterObject);
		}
		return this;
	}

	public Query filter(String filter) {
		if (whereClauseBuilder == null) {
			whereClauseBuilder = new StringBuilder(WHERE);
		} else {
			whereClauseBuilder.append(AND);
		}

		// the field and op
		whereClauseBuilder.append(filter);

		return this;
	}
	
	/**
	 * Builds LIMIT clause with the given value
	 * @param limit
	 * @return
	 */
	public Query limit(int limit) {
		this.limitClause = LIMIT + limit;
		
		return this;
	}
	
	public Query orderBy(List<OrderByField> orderBy) {
		if (this.orderByClauseBuilder == null) {
			this.orderByClauseBuilder = new StringBuilder(ORDER_BY);
		} else {
			this.orderByClauseBuilder.append(COMMA);
		}
		
		this.orderByClauseBuilder.append(JOINER.join(orderBy));
		
		return this;
	}
	
	/**
	 * Builds WHERE clause with a given field, value and op
	 * @param field
	 * @param value
	 * @param op
	 * @return
	 */
	private Query filter(String field, Object value, QueryOp op) {
		StringBuilder whereClause;

		if (whereClauseBuilder == null) {
			whereClause = new StringBuilder(WHERE);
		} else {
			whereClause = new StringBuilder(AND);
		}
		
		// the field and op
		whereClause.append(field).append(SPACE).append(op).append(SPACE);
		
		if (QueryOp.in == op) {
			whereClause.append("(");
		}
		
		// the value
		if (value instanceof String) {
			whereClause.append(TICK).append(value).append(TICK);
		} else if (value instanceof Number 
				|| value instanceof Boolean 
				|| value instanceof DateString) {
			whereClause.append(value);
		} else if (value instanceof List) {
			List<Object> data = (List<Object>) value;

			List<Object> validData;
			if (page == -1) {
				validData = data;
			} else if (data.size() > (pageSize * (page - 1))) {
				int end = data.size() > (pageSize * page) ? (pageSize * page) : data.size();
				validData = data.subList(pageSize * (page - 1), end);
			} else {
				validData = new ArrayList<Object>();
			}

			if (validData.isEmpty()) return this;

			for (Object o: validData) {
				if (o instanceof String) {
					whereClause.append(TICK);
				}
				whereClause.append(o);
				if (o instanceof String) {
					whereClause.append(TICK);
				}
				whereClause.append(COMMA);
			}

			// remove the extra comma
			whereClause.deleteCharAt(whereClause.length() - 1);
		} else {
			throw new IllegalArgumentException("Unsupported value type: " + value);
		}
		
		if (QueryOp.in == op) {
			whereClause.append(")");
		}

		if (whereClauseBuilder == null) {
			whereClauseBuilder = new StringBuilder();
		}
		whereClauseBuilder.append(whereClause);

		return this;
	}

	@Override
	public String toString() {
		StringBuilder soqlBuilder = new StringBuilder();
		soqlBuilder.append(selectClauseBuilder).append(fromClause);

		if (whereClauseBuilder != null)
			soqlBuilder.append(whereClauseBuilder);

		if (orderByClauseBuilder != null)
			soqlBuilder.append(orderByClauseBuilder);

		if (limitClause != null)
			soqlBuilder.append(limitClause);

		return soqlBuilder.toString();
	}
}
