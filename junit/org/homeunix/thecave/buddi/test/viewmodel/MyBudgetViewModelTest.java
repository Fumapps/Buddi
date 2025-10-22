/*
 * JUnit 4 tests for MyBudgetViewModel.
 */
package org.homeunix.thecave.buddi.test.viewmodel;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.homeunix.thecave.buddi.i18n.keys.BudgetCategoryTypes;
import org.homeunix.thecave.buddi.model.Account;
import org.homeunix.thecave.buddi.model.BudgetCategory;
import org.homeunix.thecave.buddi.model.BudgetCategoryType;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.Transaction;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;
import org.homeunix.thecave.buddi.model.swing.MyBudgetTreeTableModel;
import org.homeunix.thecave.buddi.plugin.api.exception.ModelException;
import org.homeunix.thecave.buddi.viewmodel.MyBudgetViewModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ca.digitalcave.moss.common.DateUtil;

import static org.junit.Assert.*;

public class MyBudgetViewModelTest {
	private Document document;
	private MyBudgetViewModel viewModel;

	@Before
	public void setUp() throws ModelException {
		// Delete autosave file before each test to prevent dialog
		File autosaveFile = ModelFactory.getAutoSaveLocation(null);
		if (autosaveFile.exists()) {
			autosaveFile.delete();
		}
		
		document = ModelFactory.createDocument();
		viewModel = new MyBudgetViewModel(document);
	}

	@After
	public void tearDown() {
		viewModel.dispose();
	}

	@Test
	public void testInitialStateProvidesValues() {
		assertNotNull("Tree table model should be available", viewModel.getTreeTableModel());
		String netIncomeText = viewModel.getNetIncomeText();
		assertNotNull("Net income text should not be null", netIncomeText);
		assertTrue("Net income text should include HTML wrapper", netIncomeText.contains("<html>"));
	}

	@Test
	public void testSelectedDatePersistsAcrossPeriodTypes() {
		Date originalMonthlyDate = DateUtil.getDate(2025, Calendar.AUGUST, 15);
		viewModel.setSelectedDate(originalMonthlyDate);

		BudgetCategoryType weekly = ModelFactory.getBudgetCategoryType(BudgetCategoryTypes.BUDGET_CATEGORY_TYPE_WEEK.toString());
		viewModel.setSelectedBudgetPeriodType(weekly);
		
		Date weeklyDate = DateUtil.getDate(2025, Calendar.AUGUST, 1);
		viewModel.setSelectedDate(weeklyDate);

		BudgetCategoryType monthly = ModelFactory.getBudgetCategoryType(BudgetCategoryTypes.BUDGET_CATEGORY_TYPE_MONTH.toString());
		viewModel.setSelectedBudgetPeriodType(monthly);

		Date normalizedOriginal = monthly.getStartOfBudgetPeriod(originalMonthlyDate);
		assertEquals("Monthly date should be restored after switching back", normalizedOriginal, viewModel.getSelectedDate());
	}

	@Test
	public void testGetSelectedBudgetCategoriesWithNullValues() {
		assertTrue("Should return empty list for null values", viewModel.getSelectedBudgetCategories(null).isEmpty());
	}

	@Test
	public void testGetTreeTableModelReturnsNonNull() {
		assertNotNull("TreeTableModel should not be null", viewModel.getTreeTableModel());
	}

	@Test
	public void testTreeTableModelHasBudgetCategories() throws ModelException {
		MyBudgetTreeTableModel treeModel = viewModel.getTreeTableModel();
		
		// The document should have default budget categories created by ModelFactory
		int childCount = treeModel.getChildCount(treeModel.getRoot());
		assertTrue("Tree should have budget categories from default document", childCount > 0);
		
		System.out.println("Budget tree has " + childCount + " root categories");
	}

	@Test
	public void testTreeTableModelWithTransactions() throws ModelException {
		// Find an expense budget category for testing
		BudgetCategory category = null;
		for (BudgetCategory bc : document.getBudgetCategories()) {
			if (!bc.isIncome()) {
				category = bc;
				break;
			}
		}
		assertNotNull("Should have at least one expense budget category", category);
		
		// Create an account
		Account account = ModelFactory.createAccount("Test Account", document.getAccountTypes().get(0));
		document.addAccount(account);
		
		// Create a transaction from account to budget category (expense)
		Date testDate = DateUtil.getDate(2025, Calendar.OCTOBER, 15);
		Transaction transaction = ModelFactory.createTransaction(testDate, "Test expense", 10000, account, category);
		document.addTransaction(transaction);
		
		// CRITICAL: Set the selected date AFTER creating the transaction
		// This ensures the tree table model knows which period to query
		viewModel.setSelectedDate(testDate);
		
		// Force a refresh to ensure the tree table model updates
		viewModel.refresh();
		
		// Verify the tree table model can access the data
		MyBudgetTreeTableModel treeModel = viewModel.getTreeTableModel();
		
		// Debug: Check if the document has the transaction
		System.out.println("Total transactions in document: " + document.getTransactions().size());
		System.out.println("Transaction in document: " + transaction);
		System.out.println("Transaction amount: " + transaction.getAmount());
		System.out.println("Transaction from: " + transaction.getFrom());
		System.out.println("Transaction to: " + transaction.getTo());
		
		// Check transactions for this category
		BudgetCategoryType periodType = treeModel.getSelectedBudgetPeriodType();
		Date startDate = periodType.getStartOfBudgetPeriod(testDate);
		Date endDate = periodType.getEndOfBudgetPeriod(testDate);
		List<Transaction> categoryTransactions = document.getTransactions(category, startDate, endDate);
		System.out.println("Transactions for category between " + startDate + " and " + endDate + ": " + categoryTransactions.size());
		
		// Column 0 is the category name
		// monthOffset is 2, so:
		// column 1 = offset -1 (previous month)
		// column 2 = offset 0 (current month) 
		// column 3 = offset +1 (next month)
		// We want the current month where our transaction is
		int currentMonthColumn = 2;
		
		// Get the value at the category row  
		Object valueObj = treeModel.getValueAt(category, currentMonthColumn);
		assertNotNull("Tree table should return value for budget category", valueObj);
		
		System.out.println("Tree table value at category: " + valueObj);
		System.out.println("Selected date in tree model: " + treeModel.getSelectedDate());
		System.out.println("Transaction date: " + transaction.getDate());
		
		// The value should be an Object[] with budget information
		if (valueObj instanceof Object[]) {
			Object[] values = (Object[]) valueObj;
			assertTrue("Value array should have at least 6 elements", values.length >= 6);
			System.out.println("Value array length: " + values.length);
			if (values.length >= 5) {
				System.out.println("  [0] Budget Category: " + values[0]);
				System.out.println("  [1] Budgeted Amount: " + values[1]);
				System.out.println("  [2] Child Total: " + values[2]);
				System.out.println("  [3] Depth: " + values[3]);
				System.out.println("  [4] Actual (no children): " + values[4]);
				if (values.length >= 6) {
					System.out.println("  [5] Actual (with children): " + values[5]);
					
					// Verify that the actual amount reflects the transaction
					// For expenses, the actual is negative (money flowing OUT to the budget category)
					Long actual = (Long) values[4];
					assertEquals("Actual amount should match transaction (negative for expenses)", -10000L, actual.longValue());
				}
			}
		}
	}

	@Test
	public void testRefreshUpdatesTreeStructure() throws ModelException {
		MyBudgetTreeTableModel treeModel = viewModel.getTreeTableModel();
		
		int initialChildCount = treeModel.getChildCount(treeModel.getRoot());
		
		// Refresh should not crash and should fire property change events
		viewModel.refresh();
		
		int afterRefreshCount = treeModel.getChildCount(treeModel.getRoot());
		assertEquals("Child count should remain the same after refresh", initialChildCount, afterRefreshCount);
	}
}

