package org.homeunix.thecave.buddi.test.plugin.builtin.report;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Date;

import org.homeunix.thecave.buddi.i18n.keys.BudgetCategoryTypes;
import org.homeunix.thecave.buddi.model.Account;
import org.homeunix.thecave.buddi.model.BudgetCategory;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.Transaction;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;
import org.homeunix.thecave.buddi.plugin.api.model.impl.ImmutableDocumentImpl;
import org.homeunix.thecave.buddi.plugin.api.util.HtmlPage;
import org.homeunix.thecave.buddi.plugin.builtin.report.IncomeExpenseReportByCategory;
import org.junit.Before;
import org.junit.Test;

import ca.digitalcave.moss.common.DateUtil;

public class IncomeExpenseReportByCategoryTest {

        private Document d;
        private IncomeExpenseReportByCategory report;

        @Before
        public void setup() throws Exception {
                ModelFactory.setPromptHandler(new org.homeunix.thecave.buddi.model.impl.PromptHandler() {
                        @Override
                        public int askUser(String message, String title, String[] options, String defaultOption) {
                                return 1; // "No"
                        }

                        @Override
                        public void showMessage(String message) {
                                // Do nothing
                        }
                });
                d = ModelFactory.createDocument();
                report = new IncomeExpenseReportByCategory();
        }

        @Test
        public void testReportContent() throws Exception {
                // Create Accounts
                Account account = ModelFactory.createAccount("Cash", d.getAccountTypes().get(0));
                d.addAccount(account);

                // Create Categories
                BudgetCategory incomeCat = ModelFactory.createBudgetCategory("Salary",
                                ModelFactory.getBudgetCategoryType(BudgetCategoryTypes.BUDGET_CATEGORY_TYPE_MONTH),
                                true);
                d.addBudgetCategory(incomeCat);

                BudgetCategory expenseCat = ModelFactory.createBudgetCategory("Groceries",
                                ModelFactory.getBudgetCategoryType(BudgetCategoryTypes.BUDGET_CATEGORY_TYPE_MONTH),
                                false);
                d.addBudgetCategory(expenseCat);

                BudgetCategory emptyCat = ModelFactory.createBudgetCategory("Unused",
                                ModelFactory.getBudgetCategoryType(BudgetCategoryTypes.BUDGET_CATEGORY_TYPE_MONTH),
                                false);
                d.addBudgetCategory(emptyCat);

                // Create Transactions
                Transaction t1 = ModelFactory.createTransaction(DateUtil.getDate(2023, 10, 1), "Paycheck", 500000,
                                incomeCat,
                                account);
                d.addTransaction(t1);

                Transaction t2 = ModelFactory.createTransaction(DateUtil.getDate(2023, 10, 5), "Food", 10000, account,
                                expenseCat);
                d.addTransaction(t2);

                // Generate Report
                Date startDate = DateUtil.getDate(2023, 10, 1);
                Date endDate = DateUtil.getDate(2023, 10, 31);
                HtmlPage page = report.getReport(new ImmutableDocumentImpl(d), null, startDate, endDate);
                String html = page.getHtml();

                // Check for Pie Chart (Should pass now)
                assertTrue("Pie chart should be present",
                                html.contains("graph.png") || html.contains("data:image/png;base64"));

                // Check for Empty Category (Should pass now)
                assertTrue("Empty category should be present", html.contains("Unused"));

                // Check for Used Category
                assertTrue("Used category should be present", html.contains("Groceries"));
        }
}
