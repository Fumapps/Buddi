package org.homeunix.thecave.buddi.view.mvvm.mybudget;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.homeunix.thecave.buddi.model.BudgetCategory;
import org.homeunix.thecave.buddi.model.BudgetCategoryType;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;
import org.homeunix.thecave.buddi.view.mvvm.View;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyBudgetView implements View<MyBudgetViewModel> {

    private final BorderPane root;
    private final TreeTableView<BudgetCategory> treeTableView;
    private final ComboBox<BudgetCategoryType> periodTypeCombo;
    private final Label dateLabel; // Placeholder for spinner/date display
    private final Label netIncomeLabel;
    private final Button prevButton;
    private final Button nextButton;

    private MyBudgetViewModel viewModel;

    public MyBudgetView() {
        root = new BorderPane();
        treeTableView = new TreeTableView<>();
        periodTypeCombo = new ComboBox<>();
        dateLabel = new Label();
        netIncomeLabel = new Label();
        prevButton = new Button("<");
        nextButton = new Button(">");

        initializeUI();
    }

    private void initializeUI() {
        // Top Panel: Period Selection and Navigation
        HBox topPanel = new HBox(10);
        topPanel.setPadding(new Insets(10));
        topPanel.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #d0d0d0; -fx-border-width: 0 0 1 0;");
        topPanel.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        periodTypeCombo.setConverter(new StringConverter<BudgetCategoryType>() {
            @Override
            public String toString(BudgetCategoryType object) {
                return object != null ? object.getName() : "";
            }

            @Override
            public BudgetCategoryType fromString(String string) {
                return null; // Not needed for read-only combo
            }
        });

        topPanel.getChildren().addAll(
                new Label("Period:"),
                periodTypeCombo,
                prevButton,
                dateLabel,
                nextButton);

        // Center: TreeTableView
        treeTableView.setShowRoot(false);
        treeTableView.setEditable(true); // Enable editing
        @SuppressWarnings("deprecation")
        javafx.scene.control.TreeTableView.TreeTableViewSelectionModel<BudgetCategory> sm = treeTableView
                .getSelectionModel(); // Just to use the variable if needed, but mainly to suppress deprecation on the
                                      // next line if it was a method call.
        // Actually, CONSTRAINED_RESIZE_POLICY is a static field.
        treeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

        TreeTableColumn<BudgetCategory, String> nameColumn = new TreeTableColumn<>("Budget Category");
        nameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue().getName()));

        TreeTableColumn<BudgetCategory, BudgetCategory> amountColumn = new TreeTableColumn<>("Budgeted Amount");
        amountColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue()));
        amountColumn.setCellFactory(param -> new EditingCell());
        amountColumn.setEditable(true);

        treeTableView.getColumns().addAll(nameColumn, amountColumn);

        // Bottom: Net Income
        HBox bottomPanel = new HBox(10);
        bottomPanel.setPadding(new Insets(10));
        bottomPanel.setStyle("-fx-background-color: #e0e0e0;");
        bottomPanel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        bottomPanel.getChildren().add(netIncomeLabel);

        root.setTop(topPanel);
        root.setCenter(treeTableView);
        root.setBottom(bottomPanel);
    }

    @Override
    public void bind(MyBudgetViewModel viewModel) {
        this.viewModel = viewModel;

        // Bind Period Types
        periodTypeCombo.getItems().setAll(viewModel.getBudgetCategoryTypes());
        periodTypeCombo.valueProperty().bindBidirectional(viewModel.selectedPeriodTypeProperty());

        // Select first type if none selected
        if (periodTypeCombo.getSelectionModel().getSelectedItem() == null && !periodTypeCombo.getItems().isEmpty()) {
            periodTypeCombo.getSelectionModel().selectFirst();
        }

        // Bind Date Display
        viewModel.selectedDateProperty().addListener((obs, oldVal, newVal) -> updateDateLabel(newVal));
        updateDateLabel(viewModel.selectedDateProperty().get());

        // Bind Net Income
        netIncomeLabel.textProperty().bind(viewModel.netIncomeTextProperty());

        // Navigation Actions
        prevButton.setOnAction(e -> navigatePeriod(-1));
        nextButton.setOnAction(e -> navigatePeriod(1));

        // Populate Tree
        populateTree();

        // Listen for Tree Changes
        viewModel.addPropertyChangeListener(evt -> {
            if (MyBudgetViewModel.PROPERTY_BUDGET_TREE_CHANGED.equals(evt.getPropertyName())) {
                javafx.application.Platform.runLater(() -> {
                    treeTableView.refresh(); // Refresh cells to update amounts
                    // If structure changed, we might need to re-populate, but usually amounts
                    // change
                    // If categories added/removed, we need populateTree().
                    // For now, let's assume structure is stable or we can re-populate if needed.
                    // But refresh() is enough for amount updates.
                });
            }
        });
    }

    private void navigatePeriod(int offset) {
        BudgetCategoryType type = viewModel.selectedPeriodTypeProperty().get();
        Date current = viewModel.selectedDateProperty().get();
        if (type != null && current != null) {
            Date newDate = type.getBudgetPeriodOffset(current, offset);
            viewModel.selectedDateProperty().set(newDate);
        }
    }

    private void updateDateLabel(Date date) {
        if (date == null) {
            dateLabel.setText("");
            return;
        }
        BudgetCategoryType type = viewModel.selectedPeriodTypeProperty().get();
        java.text.DateFormat df;
        if (type != null) {
            df = new SimpleDateFormat(type.getDateFormat());
        } else {
            df = new SimpleDateFormat("yyyy-MM-dd");
        }
        dateLabel.setText(df.format(date));
    }

    private void populateTree() {
        TreeItem<BudgetCategory> rootItem = new TreeItem<>();
        rootItem.setExpanded(true);

        for (BudgetCategory category : viewModel.getBudgetCategories()) {
            rootItem.getChildren().add(createTreeItem(category));
        }

        treeTableView.setRoot(rootItem);
    }

    private TreeItem<BudgetCategory> createTreeItem(BudgetCategory category) {
        TreeItem<BudgetCategory> item = new TreeItem<>(category);
        item.setExpanded(category.isExpanded());

        // Listen for expansion changes to update model
        item.expandedProperty().addListener((obs, oldVal, newVal) -> category.setExpanded(newVal));

        for (BudgetCategory child : category.getChildren()) {
            if (!child.isDeleted()) { // Filter deleted
                item.getChildren().add(createTreeItem(child));
            }
        }
        return item;
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    private class EditingCell extends TreeTableCell<BudgetCategory, BudgetCategory> {

        private javafx.scene.control.TextField textField;

        public EditingCell() {
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItemText());
            setGraphic(null);
        }

        @Override
        public void updateItem(BudgetCategory item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null || viewModel == null || viewModel.selectedDateProperty().get() == null) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getItemText());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            textField = new javafx.scene.control.TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.focusedProperty().addListener((arg0, arg1, arg2) -> {
                if (!arg2) {
                    commitEdit(getItem());
                }
            });

            textField.setOnKeyPressed(t -> {
                if (t.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    commitEdit(getItem());
                } else if (t.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : String.valueOf(getItem().getAmount(viewModel.selectedDateProperty().get()));
        }

        private String getItemText() {
            if (getItem() == null || viewModel == null || viewModel.selectedDateProperty().get() == null) {
                return "";
            }
            long amount = getItem().getAmount(viewModel.selectedDateProperty().get());
            String formatted = TextFormatter.getFormattedCurrency(amount);
            return formatted.replaceAll("<[^>]+>", ""); // Strip HTML
        }

        @Override
        public void commitEdit(BudgetCategory item) {
            if (isEditing()) {
                super.commitEdit(item);
            } else {
                // If we are not in editing mode (e.g. focus lost), we still want to save
                // But super.commitEdit() might not trigger if not editing.
                // However, for TreeTableCell, usually startEdit is called.
            }

            if (textField != null && item != null) {
                String text = textField.getText();
                try {
                    // Remove any currency symbols or non-numeric characters that might interfere,
                    // although NumberFormat.parse usually handles symbols if they match the locale.
                    // But to be safe and robust against simple input like "100" vs "$100.00":

                    Number number = org.homeunix.thecave.buddi.util.Formatter.getDecimalFormat().parse(text);
                    long amount = Math.round(number.doubleValue() * 100.0);

                    viewModel.setBudgetAmount(item, amount);
                } catch (java.text.ParseException e) {
                    // Try parsing as simple integer if currency parse fails (e.g. user entered
                    // "100" without decimals)
                    try {
                        long simpleAmount = Long.parseLong(text.replaceAll("[^0-9-]", ""));
                        // Assume user entered dollars if no decimal point? Or cents?
                        // Buddi usually assumes dollars if typing "100".
                        // Let's stick to the DecimalFormat parser which handles "100" as 100.0
                        // correctly.
                        // If ParseException happened, it's likely invalid input.
                        // We can just ignore or show error.
                        // But wait, if we parse "100", we should probably treat it as 100.00 (10000
                        // cents)
                        // If we just parseLong("100") -> 100 cents = $1.00. That might be confusing.
                        // Let's assume if it fails decimal format, we try to treat it as a plain number
                        // and multiply by 100.

                        long amount = simpleAmount * 100;
                        viewModel.setBudgetAmount(item, amount);

                    } catch (NumberFormatException nfe) {
                        // Ignore invalid input
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
