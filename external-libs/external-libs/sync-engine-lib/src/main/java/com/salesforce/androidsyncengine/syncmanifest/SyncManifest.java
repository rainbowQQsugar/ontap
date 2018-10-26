/**
 * 
 * @author <a href="mailto:usanaga@salesforce.com">Usha Sanaga</a>
 * Copyright (c) 2014 Salesforce.com, inc. 
 *
 */

package com.salesforce.androidsyncengine.syncmanifest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.sf.oval.constraint.AssertValid;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.constraint.Size;

public class SyncManifest {
	
	@NotNull
	@AssertValid // make sure this nested object is validated
	private Configuration configuration;
	//TODO: adjust the max value
	@NotNull
	@Size(min = 1, max = 100) // make sure we have at least object
	@AssertValid
	private ConfigObject[] objects;
	
	private Map<String, ConfigObject> nameToConfigObjectMap;

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public ConfigObject[] getObjects() {
		return objects;
	}

	public void setObjects(ConfigObject[] objects) {
		this.objects = objects;
	}

	@Override
	public String toString() {
		return "SyncManifest [configuration=" + configuration + ", objects="
				+ Arrays.toString(objects) + "]";
	}
	
	public ConfigObject getConfigObject(String name) {
		
		if (nameToConfigObjectMap == null) {
			nameToConfigObjectMap = new HashMap<String, ConfigObject>();
			for (ConfigObject configObject : getObjects()) {
				nameToConfigObjectMap.put(configObject.getObjectName(), configObject);
			}
		}
		
		ConfigObject object = nameToConfigObjectMap.get(name);
		return object;
	}
}
