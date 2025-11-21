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
        amountColumn.setCellFactory(param -> new TreeTableCell<BudgetCategory, BudgetCategory>() {
            @Override
            protected void updateItem(BudgetCategory item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || viewModel == null || viewModel.selectedDateProperty().get() == null) {
                    setText(null);
                } else {
                    long amount = item.getAmount(viewModel.selectedDateProperty().get());
                    String formatted = TextFormatter.getFormattedCurrency(amount);
                    formatted = formatted.replaceAll("<[^>]+>", ""); // Strip HTML
                    setText(formatted);
                }
            }
        });

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
}
