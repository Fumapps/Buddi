package org.homeunix.thecave.buddi.view.mvvm.mybudget;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.homeunix.thecave.buddi.i18n.keys.BudgetCategoryTypes;
import org.homeunix.thecave.buddi.model.BudgetCategory;
import org.homeunix.thecave.buddi.model.BudgetCategoryType;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.impl.FilteredLists;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;
import org.homeunix.thecave.buddi.view.mvvm.DialogService;
import org.homeunix.thecave.buddi.view.mvvm.ViewModel;

import ca.digitalcave.moss.application.document.DocumentChangeEvent;
import ca.digitalcave.moss.application.document.DocumentChangeListener;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MyBudgetViewModel extends ViewModel {

	private final Document document;
	private final DialogService dialogService;
	private final DocumentChangeListener documentListener;

	// Properties
	private final ObjectProperty<BudgetCategoryType> selectedPeriodType = new SimpleObjectProperty<>();
	private final ObjectProperty<Date> selectedDate = new SimpleObjectProperty<>();
	private final StringProperty netIncomeText = new SimpleStringProperty();

	// Data for View
	private final ObservableList<BudgetCategory> budgetCategories = FXCollections.observableArrayList();

	public MyBudgetViewModel(Document document, DialogService dialogService) {
		this.document = document;
		this.dialogService = dialogService;

		this.documentListener = new DocumentChangeListener() {
			@Override
			public void documentChange(DocumentChangeEvent event) {
				refresh();
			}
		};
		this.document.addDocumentChangeListener(this.documentListener);

		// Initialize defaults
		this.selectedPeriodType.addListener((obs, oldVal, newVal) -> {
			updateDateForPeriod(newVal);
			refresh();
		});

		this.selectedDate.addListener((obs, oldVal, newVal) -> {
			refresh();
		});

		// Set initial period type (e.g., Month)
		List<BudgetCategoryType> types = getBudgetCategoryTypes();
		if (!types.isEmpty()) {
			// Default to Month if available, else first one
			BudgetCategoryType month = null;
			for (BudgetCategoryType t : types) {
				if (BudgetCategoryTypes.BUDGET_CATEGORY_TYPE_MONTH.toString().equals(t.getName())) {
					month = t;
					break;
				}
			}
			setSelectedPeriodType(month != null ? month : types.get(0));
		}

		// Set initial date
		setSelectedDate(new Date());

		refresh();
	}

	private void updateDateForPeriod(BudgetCategoryType type) {
		if (type == null)
			return;
		Date current = getSelectedDate();
		if (current == null)
			current = new Date();
		setSelectedDate(type.getStartOfBudgetPeriod(current));
	}

	public void refresh() {
		Platform.runLater(() -> {
			updateBudgetCategories();
			updateNetIncomeText();
		});
	}

	private void updateBudgetCategories() {
		// In a real implementation, we might want to be smarter about diffing the list
		// to preserve expansion state if the View doesn't handle it.
		// For now, reload all.
		budgetCategories.setAll(document.getBudgetCategories());
	}

	private void updateNetIncomeText() {
		long budgetedNet = calculateBudgetedNetIncome();
		// Format currency, remove HTML if present (TextFormatter might return HTML)
		String formatted = TextFormatter.getFormattedCurrency(budgetedNet);
		if (formatted.startsWith("<html>")) {
			formatted = formatted.replaceAll("<[^>]+>", "");
		}
		netIncomeText.set(formatted);
	}

	private long calculateBudgetedNetIncome() {
		BudgetCategoryType periodType = getSelectedPeriodType();
		Date date = getSelectedDate();
		if (periodType == null || date == null) {
			return 0;
		}

		long total = 0;
		// FilteredLists is a helper from Buddi model
		List<BudgetCategory> categories = new LinkedList<BudgetCategory>(
				new FilteredLists.BudgetCategoryListFilteredByPeriodType(document, periodType));

		for (BudgetCategory category : categories) {
			long amount = category.getAmount(date);
			total += category.isIncome() ? amount : -amount;
		}
		return total;
	}

	// Actions

	public void createNewCategory(BudgetCategory parent) {
		dialogService.showBudgetCategoryEditor(getBudgetCategories(), getBudgetCategoryTypes(), null)
				.ifPresent(newCategory -> {
					try {
						if (parent != null) {
							newCategory.setParent(parent);
						}
						document.addBudgetCategory(newCategory);
						// Document change listener will trigger refresh
					} catch (Exception e) {
						dialogService.showError("Error creating category", e.getMessage());
					}
				});
	}

	public void editCategory(BudgetCategory category) {
		if (category == null)
			return;
		dialogService.showBudgetCategoryEditor(getBudgetCategories(), getBudgetCategoryTypes(), category)
				.ifPresent(updated -> {
					// Document change listener will trigger refresh
					// If the category object itself was mutated, we might need to force refresh
					refresh();
				});
	}

	public void deleteCategory(BudgetCategory category) {
		if (category == null)
			return;
		if (dialogService.showConfirmation("Delete Category",
				"Are you sure you want to delete " + category.getName() + "?")) {
			try {
				document.removeBudgetCategory(category);
			} catch (Exception e) {
				dialogService.showError("Error deleting category", e.getMessage());
			}
		}
	}

	public void setBudgetAmount(BudgetCategory category, long amount) {
		if (category == null || getSelectedDate() == null)
			return;
		try {
			category.setAmount(getSelectedDate(), amount);
			refresh();
		} catch (Exception e) {
			dialogService.showError("Error setting amount", e.getMessage());
		}
	}

	public void setBudgetCategoryExpanded(BudgetCategory category, boolean expanded) {
		if (category != null) {
			category.setExpanded(expanded);
		}
	}

	// Getters / Property Accessors

	public ObjectProperty<BudgetCategoryType> selectedPeriodTypeProperty() {
		return selectedPeriodType;
	}

	public BudgetCategoryType getSelectedPeriodType() {
		return selectedPeriodType.get();
	}

	public void setSelectedPeriodType(BudgetCategoryType type) {
		this.selectedPeriodType.set(type);
	}

	public ObjectProperty<Date> selectedDateProperty() {
		return selectedDate;
	}

	public Date getSelectedDate() {
		return selectedDate.get();
	}

	public void setSelectedDate(Date date) {
		this.selectedDate.set(date);
	}

	public StringProperty netIncomeTextProperty() {
		return netIncomeText;
	}

	public ObservableList<BudgetCategory> getBudgetCategories() {
		return budgetCategories;
	}

	public List<BudgetCategoryType> getBudgetCategoryTypes() {
		List<BudgetCategoryType> types = new LinkedList<>();
		for (org.homeunix.thecave.buddi.i18n.keys.BudgetCategoryTypes type : org.homeunix.thecave.buddi.i18n.keys.BudgetCategoryTypes
				.values()) {
			types.add(ModelFactory.getBudgetCategoryType(type));
		}
		return types;
	}

	@Override
	public void dispose() {
		document.removeDocumentChangeListener(documentListener);
	}
}
