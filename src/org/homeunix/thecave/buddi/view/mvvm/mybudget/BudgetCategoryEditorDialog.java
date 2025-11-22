package org.homeunix.thecave.buddi.view.mvvm.mybudget;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.homeunix.thecave.buddi.model.BudgetCategory;
import org.homeunix.thecave.buddi.model.BudgetCategoryType;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;

import java.util.List;

public class BudgetCategoryEditorDialog extends Dialog<BudgetCategory> {

    private final TextField nameField;
    private final ComboBox<BudgetCategory> parentCombo;
    private final ComboBox<BudgetCategoryType> periodTypeCombo;
    private final RadioButton incomeRadio;
    private final RadioButton expenseRadio;
    private final TextArea notesArea;

    private final BudgetCategory existingCategory;

    public BudgetCategoryEditorDialog(List<BudgetCategory> availableParents, List<BudgetCategoryType> periodTypes,
            BudgetCategory category) {
        this.existingCategory = category;

        setTitle(category == null ? "Create Budget Category" : "Edit Budget Category");
        setHeaderText(category == null ? "Enter category details" : "Edit category details");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Fields
        nameField = new TextField();
        parentCombo = new ComboBox<>(FXCollections.observableArrayList(availableParents));
        // Add null option for "No Parent"
        parentCombo.getItems().add(0, null);

        periodTypeCombo = new ComboBox<>(FXCollections.observableArrayList(periodTypes));

        ToggleGroup typeGroup = new ToggleGroup();
        incomeRadio = new RadioButton("Income");
        incomeRadio.setToggleGroup(typeGroup);
        expenseRadio = new RadioButton("Expense");
        expenseRadio.setToggleGroup(typeGroup);
        expenseRadio.setSelected(true); // Default

        notesArea = new TextArea();
        notesArea.setPrefRowCount(3);

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Parent:"), 0, 1);
        grid.add(parentCombo, 1, 1);
        grid.add(new Label("Period Type:"), 0, 2);
        grid.add(periodTypeCombo, 1, 2);
        grid.add(new Label("Type:"), 0, 3);
        grid.add(new javafx.scene.layout.HBox(10, incomeRadio, expenseRadio), 1, 3);
        grid.add(new Label("Notes:"), 0, 4);
        grid.add(notesArea, 1, 4);

        getDialogPane().setContent(grid);

        // Converters
        parentCombo.setConverter(new StringConverter<BudgetCategory>() {
            @Override
            public String toString(BudgetCategory object) {
                return object != null ? object.getFullName() : "No Parent";
            }

            @Override
            public BudgetCategory fromString(String string) {
                return null;
            }
        });

        periodTypeCombo.setConverter(new StringConverter<BudgetCategoryType>() {
            @Override
            public String toString(BudgetCategoryType object) {
                return object != null ? object.getName() : "";
            }

            @Override
            public BudgetCategoryType fromString(String string) {
                return null;
            }
        });

        // Populate if editing
        if (category != null) {
            nameField.setText(category.getName());
            parentCombo.setValue(category.getParent());
            periodTypeCombo.setValue(category.getBudgetPeriodType());
            if (category.isIncome()) {
                incomeRadio.setSelected(true);
            } else {
                expenseRadio.setSelected(true);
            }
            notesArea.setText(category.getNotes());
        } else {
            if (!periodTypes.isEmpty()) {
                periodTypeCombo.getSelectionModel().selectFirst();
            }
        }

        // Result Converter
        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return createOrUpdateCategory();
            }
            return null;
        });
    }

    private BudgetCategory createOrUpdateCategory() {
        try {
            String name = nameField.getText();
            BudgetCategory parent = parentCombo.getValue();
            BudgetCategoryType periodType = periodTypeCombo.getValue();
            boolean isIncome = incomeRadio.isSelected();
            String notes = notesArea.getText();

            if (existingCategory == null) {
                BudgetCategory newCategory = ModelFactory.createBudgetCategory(name, periodType, isIncome);
                newCategory.setParent(parent);
                newCategory.setNotes(notes);
                return newCategory;
            } else {
                existingCategory.setName(name);
                existingCategory.setParent(parent);
                existingCategory.setPeriodType(periodType);
                existingCategory.setIncome(isIncome);
                existingCategory.setNotes(notes);
                return existingCategory;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
