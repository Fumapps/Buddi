package org.homeunix.thecave.buddi.view.mvvm.transaction;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import org.homeunix.thecave.buddi.model.Source;
import org.homeunix.thecave.buddi.model.Transaction;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;
import org.homeunix.thecave.buddi.view.mvvm.View;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class TransactionView implements View<TransactionViewModel> {

    private final BorderPane root;
    private final TableView<Transaction> table;
    private final GridPane editorForm;

    // Form Controls
    private DatePicker datePicker;
    private TextField descriptionField;
    private TextField amountField;
    private ComboBox<Source> fromCombo;
    private ComboBox<Source> toCombo;
    private TextField memoField;
    private TextField numberField;
    private CheckBox clearedCheck;
    private CheckBox reconciledCheck;
    private Button saveButton;
    private Button newButton;
    private Button deleteButton; // Placeholder

    private TransactionViewModel viewModel;

    public TransactionView() {
        root = new BorderPane();
        table = new TableView<>();
        editorForm = new GridPane();

        initializeUI();
    }

    private void initializeUI() {
        // Table Setup
        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(
                p -> new ReadOnlyStringWrapper(TextFormatter.getFormattedDate(p.getValue().getDate())));

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getDescription()));

        TableColumn<Transaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(p -> new ReadOnlyStringWrapper(
                TextFormatter.getFormattedCurrency(p.getValue().getAmount()).replaceAll("<[^>]+>", "")));

        TableColumn<Transaction, String> fromCol = new TableColumn<>("From");
        fromCol.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getFrom().getFullName()));

        TableColumn<Transaction, String> toCol = new TableColumn<>("To");
        toCol.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getTo().getFullName()));

        @SuppressWarnings("unchecked")
        TableColumn<Transaction, ?>[] columns = new TableColumn[] { dateCol, descCol, amountCol, fromCol, toCol };
        table.getColumns().addAll(columns);

        @SuppressWarnings("deprecation")
        javafx.util.Callback<TableView.ResizeFeatures, Boolean> policy = TableView.CONSTRAINED_RESIZE_POLICY;
        table.setColumnResizePolicy(policy);

        root.setCenter(table);

        // Editor Form Setup
        editorForm.setPadding(new Insets(10));
        editorForm.setHgap(10);
        editorForm.setVgap(10);
        editorForm.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #d0d0d0; -fx-border-width: 1 0 0 0;");

        datePicker = new DatePicker();
        descriptionField = new TextField();
        amountField = new TextField();
        fromCombo = new ComboBox<>();
        toCombo = new ComboBox<>();
        memoField = new TextField();
        numberField = new TextField();
        clearedCheck = new CheckBox("Cleared");
        reconciledCheck = new CheckBox("Reconciled");

        saveButton = new Button("Save");
        saveButton.setDefaultButton(true);
        newButton = new Button("New");
        deleteButton = new Button("Delete");

        // Layout Form
        // Row 0
        editorForm.add(new Label("Date:"), 0, 0);
        editorForm.add(datePicker, 1, 0);
        editorForm.add(new Label("Number:"), 2, 0);
        editorForm.add(numberField, 3, 0);

        // Row 1
        editorForm.add(new Label("Description:"), 0, 1);
        editorForm.add(descriptionField, 1, 1, 3, 1); // Span 3 cols

        // Row 2
        editorForm.add(new Label("Amount:"), 0, 2);
        editorForm.add(amountField, 1, 2);

        // Row 3
        editorForm.add(new Label("From:"), 0, 3);
        editorForm.add(fromCombo, 1, 3);
        editorForm.add(new Label("To:"), 2, 3);
        editorForm.add(toCombo, 3, 3);

        // Row 4
        editorForm.add(new Label("Memo:"), 0, 4);
        editorForm.add(memoField, 1, 4, 3, 1);

        // Row 5
        HBox checks = new HBox(10, clearedCheck, reconciledCheck);
        editorForm.add(checks, 1, 5);

        // Row 6 - Buttons
        HBox buttons = new HBox(10, newButton, saveButton, deleteButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        editorForm.add(buttons, 0, 6, 4, 1);

        root.setBottom(editorForm);

        // Source Converter
        StringConverter<Source> sourceConverter = new StringConverter<Source>() {
            @Override
            public String toString(Source object) {
                return object != null ? object.getFullName() : "";
            }

            @Override
            public Source fromString(String string) {
                return null;
            }
        };
        fromCombo.setConverter(sourceConverter);
        toCombo.setConverter(sourceConverter);
    }

    @Override
    public void bind(TransactionViewModel viewModel) {
        this.viewModel = viewModel;

        // Bind Table
        table.setItems(viewModel.getTransactions());
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.selectedTransactionProperty().set(newVal);
        });

        // Bind Form Fields
        // DatePicker <-> Date
        // Need to convert LocalDate <-> Date
        viewModel.dateProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                datePicker.setValue(newVal.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            } else {
                datePicker.setValue(null);
            }
        });
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.dateProperty().set(Date.from(newVal.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
        });

        descriptionField.textProperty().bindBidirectional(viewModel.descriptionProperty());
        amountField.textProperty().bindBidirectional(viewModel.amountProperty());
        memoField.textProperty().bindBidirectional(viewModel.memoProperty());
        numberField.textProperty().bindBidirectional(viewModel.numberProperty());

        clearedCheck.selectedProperty().bindBidirectional(viewModel.clearedProperty());
        reconciledCheck.selectedProperty().bindBidirectional(viewModel.reconciledProperty());

        // ComboBoxes
        fromCombo.setItems(viewModel.getAvailableSources());
        fromCombo.valueProperty().bindBidirectional(viewModel.fromProperty());

        toCombo.setItems(viewModel.getAvailableSources());
        toCombo.valueProperty().bindBidirectional(viewModel.toProperty());

        // Buttons
        saveButton.setOnAction(e -> {
            String error = viewModel.validate();
            if (error != null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Validation Error");
                alert.setHeaderText(null);
                alert.setContentText(error);
                alert.showAndWait();
            } else {
                viewModel.save();
            }
        });

        newButton.setOnAction(e -> {
            table.getSelectionModel().clearSelection();
            viewModel.createNewTransaction();
        });

        deleteButton.setOnAction(e -> {
            if (viewModel.selectedTransactionProperty().get() == null) {
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Transaction");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete this transaction?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    viewModel.delete();
                }
            });
        });
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
