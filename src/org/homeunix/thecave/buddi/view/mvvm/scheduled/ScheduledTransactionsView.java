package org.homeunix.thecave.buddi.view.mvvm.scheduled;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.homeunix.thecave.buddi.model.ScheduledTransaction;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;
import org.homeunix.thecave.buddi.view.mvvm.View;

public class ScheduledTransactionsView implements View<ScheduledTransactionsViewModel> {

    private final BorderPane root;
    private final TableView<ScheduledTransaction> table;
    private final Button newButton;
    private final Button editButton;
    private final Button deleteButton;

    private ScheduledTransactionsViewModel viewModel;

    public ScheduledTransactionsView() {
        root = new BorderPane();
        table = new TableView<>();
        newButton = new Button("New");
        editButton = new Button("Edit");
        deleteButton = new Button("Delete");

        initializeUI();
    }

    private void initializeUI() {
        // Table Columns
        TableColumn<ScheduledTransaction, String> nameCol = new TableColumn<>("Schedule Name");
        nameCol.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getScheduleName()));

        TableColumn<ScheduledTransaction, String> freqCol = new TableColumn<>("Frequency");
        freqCol.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getFrequencyType()));

        TableColumn<ScheduledTransaction, String> dateCol = new TableColumn<>("Start Date");
        dateCol.setCellValueFactory(
                p -> new ReadOnlyStringWrapper(TextFormatter.getFormattedDate(p.getValue().getStartDate())));

        TableColumn<ScheduledTransaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getDescription()));

        TableColumn<ScheduledTransaction, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(p -> new ReadOnlyStringWrapper(
                TextFormatter.getFormattedCurrency(p.getValue().getAmount()).replaceAll("<[^>]+>", "")));

        TableColumn<ScheduledTransaction, String> fromCol = new TableColumn<>("From");
        fromCol.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getFrom().getFullName()));

        TableColumn<ScheduledTransaction, String> toCol = new TableColumn<>("To");
        toCol.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getTo().getFullName()));

        table.getColumns().addAll(nameCol, freqCol, dateCol, descCol, amountCol, fromCol, toCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Buttons
        HBox buttonPanel = new HBox(10, newButton, editButton, deleteButton);
        buttonPanel.setPadding(new Insets(10));

        root.setCenter(table);
        root.setBottom(buttonPanel);
    }

    @Override
    public void bind(ScheduledTransactionsViewModel viewModel) {
        this.viewModel = viewModel;

        table.setItems(viewModel.getScheduledTransactions());
        viewModel.selectedTransactionProperty().bind(table.getSelectionModel().selectedItemProperty());

        newButton.setOnAction(e -> viewModel.createNewTransaction());

        editButton.setOnAction(e -> viewModel.editSelectedTransaction());
        editButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        deleteButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Scheduled Transaction");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete this scheduled transaction?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    viewModel.deleteSelectedTransaction();
                }
            });
        });
        deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        // Double click to edit
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                viewModel.editSelectedTransaction();
            }
        });
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
