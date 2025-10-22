/*
 * JUnit 4 tests for MyBudgetViewModel.
 */
package org.homeunix.thecave.buddi.test.viewmodel;

import java.util.Calendar;
import java.util.Date;

import org.homeunix.thecave.buddi.i18n.keys.BudgetCategoryTypes;
import org.homeunix.thecave.buddi.model.BudgetCategoryType;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;
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
}
