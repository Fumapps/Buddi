package org.homeunix.thecave.buddi.view.mvvm.mybudget;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.homeunix.thecave.buddi.model.BudgetCategory;
import org.homeunix.thecave.buddi.model.BudgetCategoryType;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.impl.FilteredLists;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;
import org.homeunix.thecave.buddi.plugin.api.exception.InvalidValueException;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;
import org.homeunix.thecave.buddi.view.mvvm.ViewModel;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MyBudgetViewModel extends ViewModel {

    public static final String PROPERTY_BUDGET_TREE_CHANGED = "budgetTreeChanged";

    private final Document document;
    private final ObjectProperty<BudgetCategoryType> selectedPeriodType = new SimpleObjectProperty<>();
    private final ObjectProperty<Date> selectedDate = new SimpleObjectProperty<>();
    private final StringProperty netIncomeText = new SimpleStringProperty();

    private final Map<String, Date> periodDateMap = new HashMap<>();

    public MyBudgetViewModel(Document document) {
        this.document = document;

        // Initialize with defaults (similar to Swing implementation)
        // We'll need to fetch available types. For now, let's assume we can get them or
        // set a default later.
        // Ideally, we should set it to the preference or the first available type.

        // Listeners for property changes to update net income and notify view
        selectedPeriodType.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateDateForPeriodType(newVal);
                updateNetIncome();
                firePropertyChange(PROPERTY_BUDGET_TREE_CHANGED, null, System.currentTimeMillis());
            }
        });

        selectedDate.addListener((obs, oldVal, newVal) -> {
            if (newVal != null && selectedPeriodType.get() != null) {
                periodDateMap.put(periodKey(selectedPeriodType.get()), newVal);
                updateNetIncome();
                firePropertyChange(PROPERTY_BUDGET_TREE_CHANGED, null, System.currentTimeMillis());
            }
        });
    }

    public ObjectProperty<BudgetCategoryType> selectedPeriodTypeProperty() {
        return selectedPeriodType;
    }

    public ObjectProperty<Date> selectedDateProperty() {
        return selectedDate;
    }

    public StringProperty netIncomeTextProperty() {
        return netIncomeText;
    }

    public List<BudgetCategory> getBudgetCategories() {
        return document.getBudgetCategories();
    }

    public List<BudgetCategoryType> getBudgetCategoryTypes() {
        List<BudgetCategoryType> types = new LinkedList<>();
        for (org.homeunix.thecave.buddi.i18n.keys.BudgetCategoryTypes key : org.homeunix.thecave.buddi.i18n.keys.BudgetCategoryTypes
                .values()) {
            types.add(ModelFactory.getBudgetCategoryType(key.toString()));
        }
        return types;
    }

    private void updateDateForPeriodType(BudgetCategoryType type) {
        Date restoredDate = periodDateMap.get(periodKey(type));
        if (restoredDate != null) {
            selectedDate.set(restoredDate);
        } else {
            // Default to current date normalized for the period
            Date current = selectedDate.get();
            if (current == null)
                current = new Date();
            selectedDate.set(type.getStartOfBudgetPeriod(current));
        }
    }

    private void updateNetIncome() {
        long net = calculateBudgetedNetIncome();
        // Strip HTML for JavaFX Label binding, or keep it if we use a WebView/TextFlow
        // (but Label is better)
        // The Swing one used HTML. We'll strip it here and handle styling in View if
        // needed,
        // but TextFormatter.getFormattedCurrency usually returns a string.
        // If it returns HTML, we strip it.
        String formatted = TextFormatter.getFormattedCurrency(net);
        formatted = formatted.replaceAll("<[^>]+>", "");
        netIncomeText.set("Net Income: " + formatted);
    }

    private long calculateBudgetedNetIncome() {
        BudgetCategoryType periodType = selectedPeriodType.get();
        Date date = selectedDate.get();
        if (periodType == null || date == null) {
            return 0;
        }

        long total = 0;
        // We need to filter categories that match the period type
        List<BudgetCategory> categories = new LinkedList<>(
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

    public void setBudgetAmount(BudgetCategory category, long amount) {
        if (category != null && selectedDate.get() != null) {
            try {
                category.setAmount(selectedDate.get(), amount);
                updateNetIncome();
                // No need to fire tree changed if we bind properties correctly,
                // but for now we might need to refresh if the view doesn't observe the category
                // directly
            } catch (InvalidValueException e) {
                e.printStackTrace();
            }
        }
    }

    public void createNewCategory(BudgetCategory parent) {
        org.homeunix.thecave.buddi.view.mvvm.mybudget.BudgetCategoryEditorDialog dialog = new org.homeunix.thecave.buddi.view.mvvm.mybudget.BudgetCategoryEditorDialog(
                getBudgetCategories(), getBudgetCategoryTypes(), null);

        // Pre-select parent if provided
        // (We might need to expose a way to set parent in dialog, or pass it in
        // constructor)
        // For now, let's just open it.

        java.util.Optional<BudgetCategory> result = dialog.showAndWait();
        result.ifPresent(category -> {
            try {
                if (parent != null) {
                    category.setParent(parent);
                }
                document.addBudgetCategory(category);
                firePropertyChange(PROPERTY_BUDGET_TREE_CHANGED, null, System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void editCategory(BudgetCategory category) {
        if (category == null)
            return;

        org.homeunix.thecave.buddi.view.mvvm.mybudget.BudgetCategoryEditorDialog dialog = new org.homeunix.thecave.buddi.view.mvvm.mybudget.BudgetCategoryEditorDialog(
                getBudgetCategories(), getBudgetCategoryTypes(), category);

        java.util.Optional<BudgetCategory> result = dialog.showAndWait();
        result.ifPresent(updatedCategory -> {
            firePropertyChange(PROPERTY_BUDGET_TREE_CHANGED, null, System.currentTimeMillis());
        });
    }

    public void deleteCategory(BudgetCategory category) {
        if (category == null)
            return;
        try {
            document.removeBudgetCategory(category);
            firePropertyChange(PROPERTY_BUDGET_TREE_CHANGED, null, System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
