package com.salesforce.androidsyncengine.syncmanifest.syncorder;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import android.test.ActivityTestCase;
import android.util.Log;

public class SyncOrderCalculatorTest extends ActivityTestCase {
	
	private SyncOrderCalculator calculator;
	
	public void setUp() {
		calculator = new SyncOrderCalculator();
	}

	public void testValidSyncObjectOrder() {
		calculator.addSyncObject("Account", Arrays.asList("Contact", "Opportunity", "Case"));
		calculator.addSyncObject("Contact", Arrays.asList("Case"));
		calculator.addSyncObject("Case", Arrays.asList("Lead"));
		
		Set<String> order = calculator.getSyncOrder("Account");
		assertEquals("[Lead, Case, Contact, Opportunity, Account]", Arrays.toString(order.toArray()));
		
		order = calculator.getSyncOrder("Contact");
		assertEquals("[Lead, Case, Contact]", Arrays.toString(order.toArray()));

		order = calculator.getSyncOrder("Opportunity");
		assertEquals("[Opportunity]", Arrays.toString(order.toArray()));
	}
	
	public void testDuplicateRootObject() throws Exception {
		calculator.addSyncObject("Account", Arrays.asList("Contact", "Case"));
		
		try {
			calculator.addSyncObject("Account", Arrays.asList("Opportunity"));
			fail("Add must have failed");
		} catch(IllegalArgumentException iae) {
			assertEquals("Account is already added", iae.getMessage());
		}
	}

	public void testCyclicDependency() throws Exception {
		calculator.addSyncObject("Account", Arrays.asList("Contact", "Case"));
		
		try {
			calculator.addSyncObject("Contact", Arrays.asList("Account"));
			calculator.getSyncOrder("Account");
			fail("Add must have failed");
		} catch(IllegalArgumentException iae) {
			assertEquals("Contact already listed as a dependency for Account", iae.getMessage());
		}
	}
	
	public void testValidSyncOrderForAllObjects() {
		calculator.addSyncObject("Account", Arrays.asList("Contact", "Opportunity", "Case"));
		calculator.addSyncObject("Contact", Arrays.asList("Case"));
		calculator.addSyncObject("User", Arrays.asList("Lead"));
		
		List<String> order = calculator.getSortedSetForSync();
		Log.e("Order", Arrays.toString(order.toArray()));
		assertEquals("[Case, Contact, Opportunity, Account, Lead, User]", Arrays.toString(order.toArray()));
	}
}
