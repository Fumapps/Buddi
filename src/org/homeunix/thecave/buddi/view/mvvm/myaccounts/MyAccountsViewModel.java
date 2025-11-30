package org.homeunix.thecave.buddi.view.mvvm.myaccounts;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.homeunix.thecave.buddi.i18n.BuddiKeys;
import org.homeunix.thecave.buddi.model.Account;
import org.homeunix.thecave.buddi.model.AccountType;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.prefs.PrefsModel;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;
import org.homeunix.thecave.buddi.view.mvvm.DialogService;
import org.homeunix.thecave.buddi.view.mvvm.ViewModel;

import ca.digitalcave.moss.application.document.DocumentChangeEvent;
import ca.digitalcave.moss.application.document.DocumentChangeListener;

import java.util.LinkedList;
import java.util.List;

public class MyAccountsViewModel extends ViewModel {
	private final Document document;
	private final DialogService dialogService;
	private final DocumentChangeListener documentListener;

	private final StringProperty netWorth = new SimpleStringProperty();
	private final ObjectProperty<Object> selectedItem = new SimpleObjectProperty<>();

	// In a real implementation, we might expose an ObservableList of AccountTypes
	// (roots)
	// For now, we keep the getters but they should ideally be observable.
	// Let's expose the root types as an observable list for the view to bind to.
	private final ObservableList<AccountType> accountTypes = FXCollections.observableArrayList();

	public MyAccountsViewModel(Document document, DialogService dialogService) {
		this.document = document;
		this.dialogService = dialogService;

		this.documentListener = new DocumentChangeListener() {
			@Override
			public void documentChange(DocumentChangeEvent event) {
				refresh();
			}
		};

		this.document.addDocumentChangeListener(this.documentListener);
		refresh();
	}

	public void refresh() {
		Platform.runLater(() -> {
			updateNetWorth();
			updateAccountTypes();
		});
	}

	private void updateAccountTypes() {
		accountTypes.setAll(document.getAccountTypes());
	}

	private void updateNetWorth() {
		long netWorthValue = document.getNetWorth(null);
		String newText = PrefsModel.getInstance().getTranslator().get(BuddiKeys.NET_WORTH)
				+ ": "
				+ TextFormatter.getFormattedCurrency(netWorthValue, false, false).replaceAll("<[^>]+>", "");
		netWorth.set(newText);
	}

	public StringProperty netWorthProperty() {
		return netWorth;
	}

	public String getNetWorthText() {
		return netWorth.get();
	}

	public ObservableList<AccountType> getAccountTypes() {
		return accountTypes;
	}

	public List<Account> getAccounts(AccountType type) {
		List<Account> accounts = new LinkedList<>();
		for (Account a : document.getAccounts()) {
			if (a.getAccountType().equals(type)) {
				accounts.add(a);
			}
		}
		return accounts;
	}

	public ObjectProperty<Object> selectedItemProperty() {
		return selectedItem;
	}

	public Object getSelectedItem() {
		return selectedItem.get();
	}

	// Actions

	public void createNewAccount(AccountType type) {
		dialogService.showAccountEditor(document.getAccountTypes(), null)
				.ifPresent(account -> {
					try {
						if (type != null) {
							account.setAccountType(type);
						}
						document.addAccount(account);
						// Refresh handled by listener
					} catch (Exception e) {
						dialogService.showError("Error creating account", e.getMessage());
					}
				});
	}

	public void editAccount(Account account) {
		if (account == null)
			return;
		dialogService.showAccountEditor(document.getAccountTypes(), account)
				.ifPresent(updated -> {
					// Refresh handled by listener
					refresh();
				});
	}

	public void deleteAccount(Account account) {
		if (account == null)
			return;
		if (dialogService.showConfirmation("Delete Account",
				"Are you sure you want to delete " + account.getName() + "?")) {
			try {
				document.removeAccount(account);
			} catch (Exception e) {
				dialogService.showError("Error deleting account", e.getMessage());
			}
		}
		dialogService.showTransactions(account);
	}

	public void openTransactions(Account account) {
		if (account == null)
			return;
		dialogService.showTransactions(account);
	}

	@Override
	public void dispose() {
		document.removeDocumentChangeListener(documentListener);
	}
}
