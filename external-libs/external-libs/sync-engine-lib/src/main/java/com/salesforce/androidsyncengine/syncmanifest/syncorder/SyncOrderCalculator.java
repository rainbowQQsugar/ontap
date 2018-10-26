package com.salesforce.androidsyncengine.syncmanifest.syncorder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class SyncOrderCalculator {
	private Map<String, SyncObject> syncObjectsCache = new HashMap<String, SyncObject>();
	private List<SyncObject> rootSyncObjects = new LinkedList<SyncObject>();
	
	public void addSyncObject(String name, List<String> dependencies) {
		// get from cache if available
		SyncObject obj = getSyncObject(name);
		
		// if already added as root object, then fail
		if (rootSyncObjects.contains(obj)) {
			throw new IllegalArgumentException(name + " is already added");
		}
		
		// iterate through dependencies and add them to the sObject
		for (String dependency : dependencies) {
			SyncObject dependencyObj = getSyncObject(dependency);
			
			// check for cyclic dependency
			Set<String> dependencySyncOrder = getSyncOrder(dependency);
			if (dependencySyncOrder.contains(obj.getName())) {
				throw new IllegalArgumentException(obj + " already listed as a dependency for " + dependency);
			}
			obj.getDependencies().add(dependencyObj);
		}

		// mark as root object
		rootSyncObjects.add(obj);
	}
	
	/**
	 * Gets an SObject instance from cache if available
	 * or creates a new one
	 * @param name
	 * @return SObject instance
	 */
	private SyncObject getSyncObject(String name) {
		if (!syncObjectsCache.containsKey(name)) {
			syncObjectsCache.put(name, new SyncObject(name));
		}

		return syncObjectsCache.get(name);
	}
	
	/**
	 * Return the sync order for an object using DF Traversal
	 * @param name
	 * @return List of SyncObject names in order for sync
	 */
	public Set<String> getSyncOrder(String name) {
		Set<String> depthFirstOrder = getDepthFirstOrder(syncObjectsCache.get(name));
		
		return depthFirstOrder;
	}
	
	/**
	 * Return sorted list with all objects in the manifest based on dependencies 
	 * @return Set of SyncObject names in order for sync
	 */
	public List<String> getSortedSetForSync() {
		Set<String> orderedSet = new LinkedHashSet<String>();
		for (SyncObject syncObject : rootSyncObjects) {
			Set<String> set = getSyncOrder(syncObject.getName());
			orderedSet.addAll(set);
		}

		return new ArrayList<String>(orderedSet);
	}	

	/**
	 * Simple DFT
	 * @param root
	 * @return
	 */
	private Set<String> getDepthFirstOrder(SyncObject root) {
		Set<String> order = new LinkedHashSet<String>();
		if (!root.getDependencies().isEmpty()) {
			for (SyncObject dependency : root.getDependencies()) {
				order.addAll(getDepthFirstOrder(dependency));
			}
		}
		order.add(root.getName());

		return order;
	}

	public List<List<String>> getGroupsForBatchSync() {
		List<List<String>> result = new ArrayList<>();
		List<SyncObject> allSyncObjects = new ArrayList<>(rootSyncObjects);
		Set<SyncObject> removedItems = new HashSet<>();

		// Sensitive data should be fetched first, before all other objects.
		int sensitiveDataIndex = allSyncObjects.indexOf(new SyncObject("SensitiveData__c"));
		if (sensitiveDataIndex >= 0) {
			SyncObject sensitiveData = allSyncObjects.remove(sensitiveDataIndex);
			if (!sensitiveData.getDependencies().isEmpty()) {
				throw new IllegalStateException("SensitiveData cannot have any dependencies.");
			}

			result.add(Collections.singletonList(sensitiveData.getName()));
		}

		while (!allSyncObjects.isEmpty()) {
			List<SyncObject> syncObjectGroup = new ArrayList<>();
			List<String> syncNameGroup = new ArrayList<>();
			ListIterator<SyncObject> iterator = allSyncObjects.listIterator();

			while (iterator.hasNext()) {
				SyncObject syncObject = iterator.next();
				Set<SyncObject> dependencies = syncObject.getDependencies();
				if (dependencies.isEmpty() || removedItems.containsAll(dependencies)) {
					syncObjectGroup.add(syncObject);
					syncNameGroup.add(syncObject.getName());
					iterator.remove();
				}
			}

			if (syncObjectGroup.isEmpty()) {
				throw new IllegalStateException("Circular dependency on: " + allSyncObjects);
			}

			removedItems.addAll(syncObjectGroup);
			result.add(syncNameGroup);
		}

		return result;
	}
}