/**
 * 
 * @author <a href="mailto:usanaga@salesforce.com">Usha Sanaga</a>
 * Copyright (c) 2014 Salesforce.com, inc. 
 *
 */
package com.salesforce.androidsyncengine.syncmanifest.constraints;

import java.util.HashMap;
import java.util.Map;

import net.sf.oval.localization.message.MessageResolver;

public class ValidationMessages implements MessageResolver  {

	private static final Map<String, String> MESSAGES = new HashMap<String, String>() {
		private static final long serialVersionUID = -3935568568284474195L;
		{
			put("net.sf.oval.constraint.NotNull.violated", " cannot be null");
			put("net.sf.oval.constraint.Size.violated", " does not have between {min} and {max} elements");
			put("net.sf.oval.constraint.AssertValid.violated", " is invalid");
			put("ObjectIds.violated", " must be 15 or 18 characters long");
			put("FieldsToIgnore.violated", " must have one and only one of fieldsToIgnore, fieldsToFetch, fetchAllFields");
			put("net.sf.oval.constraint.NotEmpty.violated", " cannot be empty");
		}
	}; 

	@Override
	public String getMessage(String key) {
		String message = MESSAGES.get(key);
		return message == null ? key : message;
	}

}


