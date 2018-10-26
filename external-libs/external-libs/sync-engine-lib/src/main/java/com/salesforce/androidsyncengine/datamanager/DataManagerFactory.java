package com.salesforce.androidsyncengine.datamanager;

/**
 * This factory class exposes concrete implementation(s) of 
 * {@link DataManager} interface
 * @author usanaga
 *
 */
public final class DataManagerFactory {
	
	private static DataManager INSTANCE = null;
	
	// private constructor to make sure clients don't
	// instantiate this
	private DataManagerFactory() {}
	
	/**
	 * Simple getter to be used in activities 
	 * @return DataManager instance
	 */
	public static DataManager getDataManager() {
		if (INSTANCE == null) {
			INSTANCE = new SmartStoreDataManagerImpl();
		}
		return INSTANCE;
	}

	public static boolean hasDataManager() {
		return INSTANCE != null;
	}

	public static void clearDataManager() {
		INSTANCE = null;
	}
}
