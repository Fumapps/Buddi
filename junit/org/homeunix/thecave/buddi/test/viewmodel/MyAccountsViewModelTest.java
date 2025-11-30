/*
 * JUnit 4 tests for MyAccountsViewModel.
 */
package org.homeunix.thecave.buddi.test.viewmodel;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.homeunix.thecave.buddi.model.Account;
import org.homeunix.thecave.buddi.model.AccountType;
import org.homeunix.thecave.buddi.model.BudgetCategory;
import org.homeunix.thecave.buddi.model.BudgetCategoryType;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.ScheduledTransaction;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;
import org.homeunix.thecave.buddi.plugin.api.exception.ModelException;
import org.homeunix.thecave.buddi.view.mvvm.DialogService;
import org.homeunix.thecave.buddi.view.mvvm.myaccounts.MyAccountsViewModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MyAccountsViewModelTest {
	private Document document;
	private MyAccountsViewModel viewModel;
	private DialogService mockDialogService;

	@Before
	public void setUp() throws ModelException {
		try {
			javafx.application.Platform.startup(() -> {
			});
		} catch (IllegalStateException e) {
			// Platform already started
		}

		File autosaveFile = ModelFactory.getAutoSaveLocation(null);
		if (autosaveFile.exists()) {
			autosaveFile.delete();
		}

		document = ModelFactory.createDocument();

		mockDialogService = new DialogService() {
			@Override
			public void showError(String title, String message) {
			}

			@Override
			public boolean showConfirmation(String title, String message) {
				return true;
			}

			@Override
			public Optional<BudgetCategory> showBudgetCategoryEditor(List<BudgetCategory> categories,
					List<BudgetCategoryType> types, BudgetCategory categoryToEdit) {
				return Optional.empty();
			}

			@Override
			public Optional<Account> showAccountEditor(List<AccountType> accountTypes, Account accountToEdit) {
				return Optional.empty();
			}

			@Override
			public Optional<ScheduledTransaction> showScheduledTransactionEditor(Document document,
					ScheduledTransaction transactionToEdit) {
				return Optional.empty();
			}

			@Override
			public void showTransactions(Account account) {
			}
		};

		viewModel = new MyAccountsViewModel(document, mockDialogService);
	}

	@After
	public void tearDown() {
		viewModel.dispose();
	}

	@Test
	public void testInitialState() {
		viewModel.refresh();
		waitForFxEvents();
		assertNotNull(viewModel.netWorthProperty().get());
	}

	private void waitForFxEvents() {
		try {
			java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
			javafx.application.Platform.runLater(latch::countDown);
			latch.await(1, java.util.concurrent.TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
