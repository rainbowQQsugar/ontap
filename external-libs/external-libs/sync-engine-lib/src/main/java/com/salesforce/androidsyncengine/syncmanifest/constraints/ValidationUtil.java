/**
 * 
 * @author <a href="mailto:usanaga@salesforce.com">Usha Sanaga</a>
 * Copyright (c) 2014 Salesforce.com, inc. 
 *
 */
package com.salesforce.androidsyncengine.syncmanifest.constraints;

import java.util.List;

import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import net.sf.oval.context.OValContext;

import com.salesforce.androidsyncengine.syncmanifest.SyncManifest;

/**
 * Simple validation util to perform the actual check 
 */
public class ValidationUtil {

	private static final String DOT = ".";
	
	static {
		Validator.setMessageResolver(new ValidationMessages());
	}
	private static final Validator VALIDATOR = new Validator();

	private ValidationUtil() {}
	
	/**
	 * Calls validate using a static validator instance
	 * @param obj - object instance to validate
	 * @return null if valid or violation message
	 */
	public static String validate(SyncManifest obj) {
		List<ConstraintViolation> violations = VALIDATOR.validate(obj);
		
		if (violations != null && !violations.isEmpty()) {
			return buildMessage(violations.toArray(new ConstraintViolation[0]));
		} else {
			return null;
		}
	}

	/**
	 * Iterates recursively through violations to construct a string message
	 * @param violations
	 * @return string - message
	 */
	private static String buildMessage(ConstraintViolation[] violations) {
		StringBuilder sb = new StringBuilder();
		for(ConstraintViolation violation: violations) {
			sb.append(cleanUpContext(violation.getCheckDeclaringContext()))
			.append(violation.getMessage())
			.append("\n");
			
			ConstraintViolation[] causes = violation.getCauses();
			if (causes != null && causes.length > 0 ) {
				sb.append("\t").append(buildMessage(causes));
			}
		}
		
		return sb.toString();
	}
	
	private static String cleanUpContext(OValContext ctx) {
		String[] parts = ctx.toString().split("\\" + DOT);
		
		return parts[parts.length - 2] + DOT + parts[parts.length - 1];
	}
}
