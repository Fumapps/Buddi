package org.homeunix.thecave.buddi.view.mvvm.transaction;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.homeunix.thecave.buddi.model.ModelObject;
import org.homeunix.thecave.buddi.model.Source;
import org.homeunix.thecave.buddi.model.TransactionSplit;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;
import org.homeunix.thecave.buddi.util.Formatter;

import java.util.Optional;

public class SplitTransactionView extends Dialog<Boolean> {

    private final TableView<TransactionSplit> table;
    private final ObservableList<TransactionSplit> splits;
    private final ObservableList<Source> sources;
    private final Label totalLabel;

    public SplitTransactionView(ObservableList<TransactionSplit> splits, ObservableList<Source> sources) {
        this.splits = splits;
        this.sources = sources;
        this.table = new TableView<>();
        this.totalLabel = new Label("Total: $0.00");

        setTitle("Edit Splits");
        setHeaderText("Edit Split Transaction Details");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        initializeUI();

        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return true;
            }
            return null;
        });
    }

    private void initializeUI() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Columns
        TableColumn<TransactionSplit, Source> sourceCol = new TableColumn<>("Source");
        sourceCol.setCellValueFactory(p -> new javafx.beans.property.SimpleObjectProperty<>(p.getValue().getSource()));
        sourceCol.setCellFactory(col -> new TableCell<TransactionSplit, Source>() {
            private final ComboBox<Source> comboBox = new ComboBox<>(sources);

            {
                comboBox.setConverter(new StringConverter<Source>() {
                    @Override
                    public String toString(Source object) {
                        return object != null ? object.getFullName() : "";
                    }

                    @Override
                    public Source fromString(String string) {
                        return null;
                    }
                });
                comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (isEditing()) {
                        commitEdit(newVal);
                    } else if (getTableRow() != null && getTableRow().getItem() != null) {
                        // Direct update if not in "editing" mode but value changed
                        try {
                            getTableRow().getItem().setSource(newVal);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                // Filter out "Split" source from options
                comboBox.getItems().removeIf(s -> s.getName().equals("Split"));
            }

            @Override
            protected void updateItem(Source item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    comboBox.setValue(item);
                    setGraphic(comboBox);
                }
            }
        });
        sourceCol.setEditable(true);
        sourceCol.setOnEditCommit(e -> {
            try {
                e.getRowValue().setSource(e.getNewValue());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        TableColumn<TransactionSplit, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(
                TextFormatter.getFormattedCurrency(p.getValue().getAmount()).replaceAll("[^0-9.,]", "")));
        amountCol.setCellFactory(col -> new TableCell<TransactionSplit, String>() {
            private final TextField textField = new TextField();
            {
                textField.setOnAction(e -> {
                    commitEdit(textField.getText());
                    updateTotal();
                });
                textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        commitEdit(textField.getText());
                        updateTotal();
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    textField.setText(item);
                    setGraphic(textField);
                }
            }
        });
        amountCol.setEditable(true);
        amountCol.setOnEditCommit(e -> {
            try {
                long val = parseAmount(e.getNewValue());
                e.getRowValue().setAmount(val);
                updateTotal();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        table.getColumns().addAll(sourceCol, amountCol);
        table.setItems(splits);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Buttons
        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            try {
                // Default to first available source
                Source defaultSource = sources.stream().filter(s -> !s.getName().equals("Split")).findFirst()
                        .orElse(null);
                splits.add(ModelFactory.createTransactionSplit(defaultSource, 0));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button removeButton = new Button("Remove");
        removeButton.setOnAction(e -> {
            TransactionSplit selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                splits.remove(selected);
                updateTotal();
            }
        });

        HBox buttons = new HBox(10, addButton, removeButton, totalLabel);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        root.setCenter(table);
        root.setBottom(buttons);

        getDialogPane().setContent(root);

        updateTotal();
    }

    private void updateTotal() {
        long total = splits.stream().mapToLong(TransactionSplit::getAmount).sum();
        totalLabel.setText("Total: " + TextFormatter.getFormattedCurrency(total));
    }

    private long parseAmount(String text) {
        try {
            Number number = Formatter.getDecimalFormat().parse(text);
            return Math.round(number.doubleValue() * 100.0);
        } catch (Exception e) {
            return 0;
        }
    }
}
