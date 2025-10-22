/*
 * ViewModel for MyBudgetPanel.
 * Encapsulates budget period state, net income computation, and selection logic.
 */
package org.homeunix.thecave.buddi.viewmodel;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.homeunix.thecave.buddi.model.BudgetCategory;
import org.homeunix.thecave.buddi.model.BudgetCategoryType;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.impl.FilteredLists;
import org.homeunix.thecave.buddi.model.swing.MyBudgetTreeTableModel;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;

import ca.digitalcave.moss.application.document.DocumentChangeEvent;
import ca.digitalcave.moss.application.document.DocumentChangeListener;

/**
 * ViewModel backing the MyBudgetPanel Swing view.
 * Manages period selection, net income formatting, and exposes observable state for the view.
 */
public class MyBudgetViewModel extends ObservableViewModel {
	public static final String PROPERTY_NET_INCOME_TEXT = "netIncomeText";
	public static final String PROPERTY_TREE_STRUCTURE_CHANGED = "budgetTreeChanged";
	public static final String PROPERTY_PERIOD_TYPE_CHANGED = "budgetPeriodTypeChanged";
	public static final String PROPERTY_SELECTED_DATE_CHANGED = "budgetSelectedDateChanged";

	private final Document document;
	private final MyBudgetTreeTableModel treeTableModel;
	private final DocumentChangeListener documentListener;
	private final Map<String, Date> periodDateMap = new HashMap<String, Date>();

	private String netIncomeText;

	public MyBudgetViewModel(Document document) {
		super();
		this.document = document;
		this.treeTableModel = new MyBudgetTreeTableModel(document);
		this.documentListener = new DocumentChangeListener() {
			@Override
			public void documentChange(DocumentChangeEvent event) {
				refresh();
			}
		};

		this.document.addDocumentChangeListener(this.documentListener);

		// Initialise state so getters return meaningful values before listeners attach.
		refresh();
	}

	public MyBudgetTreeTableModel getTreeTableModel() {
		return treeTableModel;
	}

	public Document getDocument() {
		return document;
	}

	public BudgetCategoryType getSelectedBudgetPeriodType() {
		return treeTableModel.getSelectedBudgetPeriodType();
	}

	public Date getSelectedDate() {
		return treeTableModel.getSelectedDate();
	}

	public void setSelectedDate(Date date) {
		if (date == null) {
			return;
		}
		Date normalized = getSelectedBudgetPeriodType().getStartOfBudgetPeriod(date);
		Date current = treeTableModel.getSelectedDate();
		
		boolean dateChanged = current == null || !current.equals(normalized);
		
		if (dateChanged) {
			treeTableModel.setSelectedDate(normalized);
			firePropertyChange(PROPERTY_SELECTED_DATE_CHANGED, current, normalized);
		}
		
		periodDateMap.put(periodKey(getSelectedBudgetPeriodType()), normalized);
		
		// Always refresh tree and net income, even if date didn't change,
		// because underlying transaction data may have changed
		notifyTreeStructureChanged();
		updateNetIncomeText();
	}

	public void setSelectedBudgetPeriodType(BudgetCategoryType periodType) {
		if (periodType == null) {
			return;
		}

		BudgetCategoryType currentType = getSelectedBudgetPeriodType();
		if (periodType.equals(currentType)) {
			return;
		}

		Date currentDate = treeTableModel.getSelectedDate();
		if (currentType != null && currentDate != null) {
			periodDateMap.put(periodKey(currentType), currentDate);
		}

		treeTableModel.setSelectedBudgetPeriodType(periodType);

		Date restoredDate = periodDateMap.get(periodKey(periodType));
		if (restoredDate != null) {
			treeTableModel.setSelectedDate(restoredDate);
		}
		else {
			Date fallback = periodType.getStartOfBudgetPeriod(treeTableModel.getSelectedDate());
			treeTableModel.setSelectedDate(fallback);
			periodDateMap.put(periodKey(periodType), fallback);
		}

		firePropertyChange(PROPERTY_PERIOD_TYPE_CHANGED, currentType, periodType);
		firePropertyChange(PROPERTY_SELECTED_DATE_CHANGED, currentDate, treeTableModel.getSelectedDate());
		notifyTreeStructureChanged();
		updateNetIncomeText();
	}

	public void setBudgetCategoryExpanded(BudgetCategory category, boolean expanded) {
		if (category != null) {
			category.setExpanded(expanded);
		}
	}

	public List<BudgetCategory> getSelectedBudgetCategories(Object[] rowValues) {
		List<BudgetCategory> categories = new LinkedList<BudgetCategory>();
		if (rowValues == null) {
			return categories;
		}
		for (Object value : rowValues) {
			if (value instanceof BudgetCategory) {
				categories.add((BudgetCategory) value);
			}
		}
		return categories;
	}

	public List<BudgetCategory> getBudgetCategories() {
		return document.getBudgetCategories();
	}

	public String getNetIncomeText() {
		return netIncomeText;
	}

	public void refresh() {
		notifyTreeStructureChanged();
		updateNetIncomeText();
	}

	private void notifyTreeStructureChanged() {
		treeTableModel.fireStructureChanged();
		firePropertyChange(PROPERTY_TREE_STRUCTURE_CHANGED, null, System.currentTimeMillis());
	}

	private void updateNetIncomeText() {
		String previous = netIncomeText;
		long netIncome = calculateBudgetedNetIncome();
		String formatted = TextFormatter.getHtmlWrapper(TextFormatter.getFormattedCurrency(netIncome));
		netIncomeText = formatted;
		if (previous == null || !previous.equals(formatted)) {
			firePropertyChange(PROPERTY_NET_INCOME_TEXT, previous, formatted);
		}
	}

	private long calculateBudgetedNetIncome() {
		BudgetCategoryType periodType = getSelectedBudgetPeriodType();
		Date date = getSelectedDate();
		if (periodType == null || date == null) {
			return 0;
		}

		long total = 0;
		List<BudgetCategory> categories = new LinkedList<BudgetCategory>(
				new FilteredLists.BudgetCategoryListFilteredByPeriodType(document, periodType));
		for (BudgetCategory category : categories) {
			long amount = category.getAmount(date);
			total += category.isIncome() ? amount : -amount;
		}
		return total;
	}

	private String periodKey(BudgetCategoryType type) {
		return type != null ? type.getName() : "";
	}

	@Override
	public void dispose() {
		document.removeDocumentChangeListener(documentListener);
	}
}
