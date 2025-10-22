/*
 * JUnit 4 tests for MyAccountsViewModel.
 */
package org.homeunix.thecave.buddi.test.viewmodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.homeunix.thecave.buddi.model.AccountType;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;
import org.homeunix.thecave.buddi.plugin.api.exception.ModelException;
import org.homeunix.thecave.buddi.viewmodel.MyAccountsViewModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MyAccountsViewModelTest {
	private Document document;
	private MyAccountsViewModel viewModel;

	@Before
	public void setUp() throws ModelException {
		// Delete autosave file before each test to prevent dialog
		File autosaveFile = ModelFactory.getAutoSaveLocation(null);
		if (autosaveFile.exists()) {
			autosaveFile.delete();
		}
		
		document = ModelFactory.createDocument();
		viewModel = new MyAccountsViewModel(document);
	}

	@After
	public void tearDown() {
		viewModel.dispose();
	}

	@Test
	public void testInitialNetWorthTextIsNotNull() {
		String netWorthText = viewModel.getNetWorthText();
		assertNotNull("Net worth text should not be null", netWorthText);
		assertTrue("Net worth text should contain HTML wrapper", netWorthText.contains("<html>"));
	}

	@Test
	public void testGetAccountTypesReturnsDocumentTypes() {
		List<AccountType> types = viewModel.getAccountTypes();
		assertNotNull("Account types list should not be null", types);
		assertEquals("Account types size should match document", types.size(), document.getAccountTypes().size());
	}

	@Test
	public void testPropertyChangeListenerRegistration() {
		final List<PropertyChangeEvent> events = new ArrayList<>();
		PropertyChangeListener listener = event -> events.add(event);

		viewModel.addPropertyChangeListener(listener);
		viewModel.refresh();

		assertTrue("Should have received property change events", events.size() > 0);

		viewModel.removePropertyChangeListener(listener);
		events.clear();

		viewModel.refresh();
		assertEquals("No events should be received after listener removal", 0, events.size());
	}

	@Test
	public void testGetSelectedAccountsWithNullValues() {
		List<?> accounts = viewModel.getSelectedAccounts(null);
		assertNotNull("Should return non-null list for null values", accounts);
		assertEquals("Should return empty list for null values", 0, accounts.size());
	}

	@Test
	public void testGetDocumentReturnsTheDocument() {
		Document doc = viewModel.getDocument();
		assertNotNull("Should return non-null document", doc);
		assertSame("Should return the same document", doc, document);
	}
}
