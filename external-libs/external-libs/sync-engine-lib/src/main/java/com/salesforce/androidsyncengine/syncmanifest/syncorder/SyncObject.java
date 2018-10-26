package com.salesforce.androidsyncengine.syncmanifest.syncorder;

import java.util.LinkedHashSet;
import java.util.Set;

public class SyncObject implements Comparable<SyncObject> {
	
	private String name;
	private Set<SyncObject> dependencies = new LinkedHashSet<SyncObject>();
	
	public SyncObject(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<SyncObject> getDependencies() {
		return dependencies;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SyncObject other = (SyncObject) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public int compareTo(SyncObject o) {
		if (o == null) {
			return 1;
		}
		
		if (this.name == null) {
			if (o.name == null) {
				return 0;
			} else {
				return -1;
			}
		}
		
		return this.name.compareTo(o.name);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
