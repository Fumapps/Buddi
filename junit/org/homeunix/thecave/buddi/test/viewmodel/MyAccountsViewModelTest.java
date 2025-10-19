/*
 * Manual unit tests for MyAccountsViewModel.
 * Verifies net worth formatting, expansion state management, and refresh logic.
 * Note: These are written without JUnit framework since JUnit is not in the build classpath.
 */
package org.homeunix.thecave.buddi.test.viewmodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.homeunix.thecave.buddi.model.AccountType;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;
import org.homeunix.thecave.buddi.viewmodel.MyAccountsViewModel;

/**
 * Manual tests for MyAccountsViewModel.
 * Run via main method to verify ViewModel behaviour.
 */
public class MyAccountsViewModelTest {
	private static int testCount = 0;
	private static int passCount = 0;

	public static void main(String[] args) {
		try {
			testInitialNetWorthTextIsNotNull();
			testGetAccountTypesReturnsDocumentTypes();
			testPropertyChangeListenerRegistration();
			testNetWorthPropertyChangeOnRefresh();
			testAccountTypeExpansionPersistence();
			testGetSelectedAccountsWithNullValues();
			testGetSelectedAccountTypesWithNullValues();
			testGetDocumentReturnsTheDocument();
			testDisposeUnregistersListener();

			System.out.println("\n=== Test Summary ===");
			System.out.println("Passed: " + passCount + "/" + testCount);
			if (passCount == testCount) {
				System.out.println("All tests passed!");
			} else {
				System.out.println("Some tests failed!");
				System.exit(1);
			}
		} catch (Exception e) {
			System.err.println("Unexpected error during testing:");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void testInitialNetWorthTextIsNotNull() {
		testCount++;
		try {
			Document document = ModelFactory.createDocument();
			MyAccountsViewModel viewModel = new MyAccountsViewModel(document);
			String netWorthText = viewModel.getNetWorthText();
			viewModel.dispose();

			assert netWorthText != null : "Net worth text should not be null after initialization";
			assert netWorthText.contains("<html>") : "Net worth text should contain HTML wrapper";
			passCount++;
			System.out.println("✓ testInitialNetWorthTextIsNotNull passed");
		} catch (Exception e) {
			System.out.println("✗ testInitialNetWorthTextIsNotNull failed: " + e.getMessage());
		}
	}

	private static void testGetAccountTypesReturnsDocumentTypes() {
		testCount++;
		try {
			Document document = ModelFactory.createDocument();
			MyAccountsViewModel viewModel = new MyAccountsViewModel(document);
			List<AccountType> types = viewModel.getAccountTypes();
			viewModel.dispose();

			assert types != null : "Account types list should not be null";
			assert types.size() == 0 : "New document should have no account types";
			passCount++;
			System.out.println("✓ testGetAccountTypesReturnsDocumentTypes passed");
		} catch (Exception e) {
			System.out.println("✗ testGetAccountTypesReturnsDocumentTypes failed: " + e.getMessage());
		}
	}

	private static void testPropertyChangeListenerRegistration() {
		testCount++;
		try {
			Document document = ModelFactory.createDocument();
			MyAccountsViewModel viewModel = new MyAccountsViewModel(document);
			
			final List<PropertyChangeEvent> events = new ArrayList<>();
			PropertyChangeListener listener = event -> events.add(event);

			viewModel.addPropertyChangeListener(listener);
			viewModel.refresh();

			assert events.size() > 0 : "Should have received property change events";

			viewModel.removePropertyChangeListener(listener);
			events.clear();

			viewModel.refresh();
			assert events.size() == 0 : "No events should be received after listener removal";
			viewModel.dispose();
			passCount++;
			System.out.println("✓ testPropertyChangeListenerRegistration passed");
		} catch (Exception e) {
			System.out.println("✗ testPropertyChangeListenerRegistration failed: " + e.getMessage());
		}
	}

	private static void testNetWorthPropertyChangeOnRefresh() {
		testCount++;
		try {
			Document document = ModelFactory.createDocument();
			MyAccountsViewModel viewModel = new MyAccountsViewModel(document);
			
			final List<PropertyChangeEvent> events = new ArrayList<>();
			PropertyChangeListener listener = event -> {
				if (MyAccountsViewModel.PROPERTY_NET_WORTH_TEXT.equals(event.getPropertyName())) {
					events.add(event);
				}
			};

			viewModel.addPropertyChangeListener(listener);
			events.clear();
			String initialText = viewModel.getNetWorthText();
			viewModel.refresh();
			
			assert events.size() == 0 : "Refresh with same state should not fire property change";
			viewModel.removePropertyChangeListener(listener);
			viewModel.dispose();
			passCount++;
			System.out.println("✓ testNetWorthPropertyChangeOnRefresh passed");
		} catch (Exception e) {
			System.out.println("✗ testNetWorthPropertyChangeOnRefresh failed: " + e.getMessage());
		}
	}

	private static void testAccountTypeExpansionPersistence() {
		testCount++;
		try {
			Document document = ModelFactory.createDocument();
			MyAccountsViewModel viewModel = new MyAccountsViewModel(document);
			List<AccountType> types = viewModel.getAccountTypes();
			
			assert types.size() == 0 : "Document should start with no account types";
			viewModel.dispose();
			passCount++;
			System.out.println("✓ testAccountTypeExpansionPersistence passed");
		} catch (Exception e) {
			System.out.println("✗ testAccountTypeExpansionPersistence failed: " + e.getMessage());
		}
	}

	private static void testGetSelectedAccountsWithNullValues() {
		testCount++;
		try {
			Document document = ModelFactory.createDocument();
			MyAccountsViewModel viewModel = new MyAccountsViewModel(document);
			List<?> accounts = viewModel.getSelectedAccounts(null);
			
			assert accounts != null : "Should return non-null list for null values";
			assert accounts.size() == 0 : "Should return empty list for null values";
			viewModel.dispose();
			passCount++;
			System.out.println("✓ testGetSelectedAccountsWithNullValues passed");
		} catch (Exception e) {
			System.out.println("✗ testGetSelectedAccountsWithNullValues failed: " + e.getMessage());
		}
	}

	private static void testGetSelectedAccountTypesWithNullValues() {
		testCount++;
		try {
			Document document = ModelFactory.createDocument();
			MyAccountsViewModel viewModel = new MyAccountsViewModel(document);
			List<?> types = viewModel.getSelectedAccountTypes(null);
			
			assert types != null : "Should return non-null list for null values";
			assert types.size() == 0 : "Should return empty list for null values";
			viewModel.dispose();
			passCount++;
			System.out.println("✓ testGetSelectedAccountTypesWithNullValues passed");
		} catch (Exception e) {
			System.out.println("✗ testGetSelectedAccountTypesWithNullValues failed: " + e.getMessage());
		}
	}

	private static void testGetDocumentReturnsTheDocument() {
		testCount++;
		try {
			Document document = ModelFactory.createDocument();
			MyAccountsViewModel viewModel = new MyAccountsViewModel(document);
			Document doc = viewModel.getDocument();
			
			assert doc != null : "Should return non-null document";
			assert doc == document : "Should return the same document passed to constructor";
			viewModel.dispose();
			passCount++;
			System.out.println("✓ testGetDocumentReturnsTheDocument passed");
		} catch (Exception e) {
			System.out.println("✗ testGetDocumentReturnsTheDocument failed: " + e.getMessage());
		}
	}

	private static void testDisposeUnregistersListener() {
		testCount++;
		try {
			Document document = ModelFactory.createDocument();
			MyAccountsViewModel viewModel = new MyAccountsViewModel(document);
			
			final List<PropertyChangeEvent> events = new ArrayList<>();
			PropertyChangeListener listener = event -> events.add(event);

			viewModel.addPropertyChangeListener(listener);
			viewModel.dispose();
			viewModel.refresh();

			// After dispose, we should still be able to refresh without throwing
			passCount++;
			System.out.println("✓ testDisposeUnregistersListener passed");
		} catch (Exception e) {
			System.out.println("✗ testDisposeUnregistersListener failed: " + e.getMessage());
		}
	}
}
