/**
 * 
 * @author <a href="mailto:usanaga@salesforce.com">Usha Sanaga</a>
 * Copyright (c) 2014 Salesforce.com, inc. 
 *
 */

package com.salesforce.androidsyncengine.syncmanifest;

import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

public class FieldToIndex {
	@NotNull
	@NotEmpty
	private String name;
	@NotNull
	@NotEmpty
	private String type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "FieldToIndex [name=" + name + ", type=" + type + "]";
	}
}
