/*
 * ViewModel for MyAccountsPanel.
 * Encapsulates account tree state, net worth computation, and selection logic.
 */
package org.homeunix.thecave.buddi.viewmodel;

import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

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
 * Manages state related to account display: tree expansion, net worth, and selection queries.
 * Reacts to document changes and exposes observable state for the view to consume.
 */
public class MyAccountsViewModel extends ObservableViewModel {
	private final Document document;
	private final DocumentChangeListener documentListener;

	// Property names for change events
	public static final String PROPERTY_NET_WORTH_TEXT = "netWorthText";
	public static final String PROPERTY_ACCOUNT_TREE_CHANGED = "accountTreeChanged";

	private String netWorthText;

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
		long netWorth = document.getNetWorth(null);
		String newText = TextFormatter.getHtmlWrapper(
				PrefsModel.getInstance().getTranslator().get(BuddiKeys.NET_WORTH)
				+ ": "
				+ TextFormatter.getFormattedCurrency(netWorth));

		// Fire property change if text changed
		if (!newText.equals(netWorthText)) {
			firePropertyChange(PROPERTY_NET_WORTH_TEXT, netWorthText, newText);
			netWorthText = newText;
		}

		// Signal that the tree structure has changed
		firePropertyChange(PROPERTY_ACCOUNT_TREE_CHANGED, null, System.currentTimeMillis());
	}

	/**
	 * Get the formatted net worth text to display in the UI.
	 */
	public String getNetWorthText() {
		return netWorthText;
	}

	/**
	 * Get all account types from the document.
	 * Used by the view to populate the tree.
	 */
	public List<AccountType> getAccountTypes() {
		return document.getAccountTypes();
	}

	/**
	 * Persist the expansion state of an account type.
	 * @param accountType the account type to update
	 * @param expanded true if expanded, false if collapsed
	 */
	public void setAccountTypeExpanded(AccountType accountType, boolean expanded) {
		accountType.setExpanded(expanded);
	}

	/**
	 * Get all selected accounts from the given row indices and values.
	 * Filters the values to extract only Account objects.
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
