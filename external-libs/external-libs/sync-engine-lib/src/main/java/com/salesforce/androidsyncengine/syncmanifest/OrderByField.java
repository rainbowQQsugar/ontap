/**
 * 
 * @author <a href="mailto:usanaga@salesforce.com">Usha Sanaga</a>
 * Copyright (c) 2014 Salesforce.com, inc. 
 *
 */
package com.salesforce.androidsyncengine.syncmanifest;

import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class OrderByField {
	@NotNull
	@NotEmpty
	private String name;
	@NotNull (message = " should be either ASC or DESC")
	private Order order;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Order getOrder() {
		return order;
	}

	private void setOrder(Order order) {
		this.order = order;
	}

	/**
	 * Since order by fields have dynamic field names,
	 * we will use a custom setter
	 * @param key - field name
	 * @param value - orderby value (see {@link Order})
	 */
	@JsonAnySetter
	public void setProperties(String key, Object value) {
		setName(key);
		try {
			setOrder(Order.valueOf(String.valueOf(value)));
		} catch (Exception e) {
			Log.w("OrderByField", e);
		}
	}

	@Override
	public String toString() {
		return name + " " + order;
	}
}
