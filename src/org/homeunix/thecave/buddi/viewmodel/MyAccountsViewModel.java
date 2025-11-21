/*
 * ViewModel for MyAccountsPanel.
 * Encapsulates account tree state, net worth computation, and selection logic.
 */
package org.homeunix.thecave.buddi.viewmodel;

import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.homeunix.thecave.buddi.i18n.BuddiKeys;
import org.homeunix.thecave.buddi.model.Account;
import org.homeunix.thecave.buddi.model.AccountType;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.prefs.PrefsModel;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;

import ca.digitalcave.moss.application.document.DocumentChangeEvent;
import ca.digitalcave.moss.application.document.DocumentChangeListener;

/**
 * ViewModel for the MyAccountsPanel.
 * Manages state related to account display: tree expansion, net worth, and
 * selection queries.
 * Reacts to document changes and exposes observable state for the view to
 * consume.
 */
import org.homeunix.thecave.buddi.view.mvvm.ViewModel;

/**
 * ViewModel for the MyAccountsPanel.
 * Manages state related to account display: tree expansion, net worth, and
 * selection queries.
 * Reacts to document changes and exposes observable state for the view to
 * consume.
 */
public class MyAccountsViewModel extends ViewModel {
	private final Document document;
	private final DocumentChangeListener documentListener;

	// Property names for change events
	public static final String PROPERTY_NET_WORTH_TEXT = "netWorthText";
	public static final String PROPERTY_ACCOUNT_TREE_CHANGED = "accountTreeChanged";

	private final StringProperty netWorth = new SimpleStringProperty();

	public MyAccountsViewModel(Document document) {
		super();
		this.document = document;
		this.documentListener = new DocumentChangeListener() {
			@Override
			public void documentChange(DocumentChangeEvent event) {
				refresh();
			}
		};

		// Register to document changes
		this.document.addDocumentChangeListener(this.documentListener);

		// Initialize state
		refresh();
	}

	/**
	 * Refresh the ViewModel state based on the current document state.
	 * Called when the document changes.
	 */
	public void refresh() {
		// Compute net worth and format it
		long netWorthValue = document.getNetWorth(null);
		// Remove HTML wrapper for JavaFX
		String newText = PrefsModel.getInstance().getTranslator().get(BuddiKeys.NET_WORTH)
				+ ": "
				+ TextFormatter.getFormattedCurrency(netWorthValue, false, false).replaceAll("<[^>]+>", ""); // Strip
																												// any
																												// remaining
																												// HTML
																												// just
																												// in
																												// case

		// Update on FX thread
		Platform.runLater(() -> {
			String oldText = netWorth.get();
			if (!newText.equals(oldText)) {
				netWorth.set(newText);
				// Fire legacy property change for compatibility
				firePropertyChange(PROPERTY_NET_WORTH_TEXT, oldText, newText);
			}
		});

		// Signal that the tree structure has changed
		// TODO: Use a better mechanism for tree updates in FX (e.g. ObservableList)
		firePropertyChange(PROPERTY_ACCOUNT_TREE_CHANGED, null, System.currentTimeMillis());
	}

	/**
	 * Get the formatted net worth text to display in the UI.
	 */
	public String getNetWorthText() {
		return netWorth.get();
	}

	public StringProperty netWorthProperty() {
		return netWorth;
	}

	public long getNetWorthValue() {
		return document.getNetWorth(null);
	}

	/**
	 * Get all account types from the document.
	 * Used by the view to populate the tree.
	 */
	public List<AccountType> getAccountTypes() {
		return document.getAccountTypes();
	}

	/**
	 * Get accounts for a specific account type.
	 */
	public List<Account> getAccounts(AccountType type) {
		List<Account> accounts = new LinkedList<>();
		for (Account a : document.getAccounts()) {
			if (a.getAccountType().equals(type)) {
				accounts.add(a);
			}
		}
		return accounts;
	}

	/**
	 * Persist the expansion state of an account type.
	 * 
	 * @param accountType the account type to update
	 * @param expanded    true if expanded, false if collapsed
	 */
	public void setAccountTypeExpanded(AccountType accountType, boolean expanded) {
		accountType.setExpanded(expanded);
	}

	/**
	 * Get all selected accounts from the given row indices and values.
	 * Filters the values to extract only Account objects.
	 * 
	 * @param rowValues the selected row values (typically from tree model)
	 * @return list of selected Account objects
	 */
	public List<Account> getSelectedAccounts(Object[] rowValues) {
		List<Account> accounts = new LinkedList<>();
		if (rowValues == null) {
			return accounts;
		}
		for (Object value : rowValues) {
			if (value instanceof Account) {
				accounts.add((Account) value);
			}
		}
		return accounts;
	}

	/**
	 * Get all selected account types from the given row values.
	 * Filters the values to extract only AccountType objects.
	 * 
	 * @param rowValues the selected row values (typically from tree model)
	 * @return list of selected AccountType objects
	 */
	public List<AccountType> getSelectedAccountTypes(Object[] rowValues) {
		List<AccountType> types = new LinkedList<>();
		if (rowValues == null) {
			return types;
		}
		for (Object value : rowValues) {
			if (value instanceof AccountType) {
				types.add((AccountType) value);
			}
		}
		return types;
	}

	/**
	 * Get the underlying document.
	 * Used by the view to access the model directly (tree model, etc.).
	 */
	public Document getDocument() {
		return document;
	}

	@Override
	public void dispose() {
		// Unregister from document changes to prevent memory leaks
		document.removeDocumentChangeListener(documentListener);
	}
}
