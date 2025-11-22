package org.homeunix.thecave.buddi.view.mvvm.scheduled;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.homeunix.thecave.buddi.i18n.keys.ScheduleFrequency;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.ScheduledTransaction;
import org.homeunix.thecave.buddi.model.Source;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;
import org.homeunix.thecave.buddi.util.Formatter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;

public class ScheduledTransactionEditorDialog extends Dialog<ScheduledTransaction> {

    private final TextField nameField;
    private final ComboBox<String> frequencyCombo;
    private final DatePicker startDatePicker;
    private final TextField descriptionField;
    private final TextField amountField;
    private final ComboBox<Source> fromCombo;
    private final ComboBox<Source> toCombo;
    private final TextField memoField;

    private final ScheduledTransaction existingTransaction;
    private final Document document;

    public ScheduledTransactionEditorDialog(Document document, ScheduledTransaction transaction) {
        this.document = document;
        this.existingTransaction = transaction;

        setTitle(transaction == null ? "New Scheduled Transaction" : "Edit Scheduled Transaction");
        setHeaderText(null);

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Fields
        nameField = new TextField();
        frequencyCombo = new ComboBox<>();
        for (ScheduleFrequency sf : ScheduleFrequency.values()) {
            frequencyCombo.getItems().add(sf.toString());
        }

        startDatePicker = new DatePicker(LocalDate.now());
        descriptionField = new TextField();
        amountField = new TextField();
        fromCombo = new ComboBox<>();
        toCombo = new ComboBox<>();
        memoField = new TextField();

        // Populate Sources
        fromCombo.setItems(FXCollections.observableArrayList(document.getSources().stream()
                .filter(s -> !(s instanceof org.homeunix.thecave.buddi.model.BudgetCategory)
                        || ((org.homeunix.thecave.buddi.model.BudgetCategory) s).isIncome())
                .collect(Collectors.toList())));

        toCombo.setItems(FXCollections.observableArrayList(document.getSources().stream()
                .filter(s -> !(s instanceof org.homeunix.thecave.buddi.model.BudgetCategory)
                        || !((org.homeunix.thecave.buddi.model.BudgetCategory) s).isIncome())
                .collect(Collectors.toList())));

        // Converters
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

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Schedule Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Frequency:"), 0, 1);
        grid.add(frequencyCombo, 1, 1);
        grid.add(new Label("Start Date:"), 0, 2);
        grid.add(startDatePicker, 1, 2);

        grid.add(new Separator(), 0, 3, 2, 1);

        grid.add(new Label("Description:"), 0, 4);
        grid.add(descriptionField, 1, 4);
        grid.add(new Label("Amount:"), 0, 5);
        grid.add(amountField, 1, 5);
        grid.add(new Label("From:"), 0, 6);
        grid.add(fromCombo, 1, 6);
        grid.add(new Label("To:"), 0, 7);
        grid.add(toCombo, 1, 7);
        grid.add(new Label("Memo:"), 0, 8);
        grid.add(memoField, 1, 8);

        getDialogPane().setContent(grid);

        // Populate if editing
        if (transaction != null) {
            nameField.setText(transaction.getScheduleName());
            frequencyCombo.setValue(transaction.getFrequencyType());
            startDatePicker
                    .setValue(transaction.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            descriptionField.setText(transaction.getDescription());
            amountField.setText(TextFormatter.getFormattedCurrency(transaction.getAmount()).replaceAll("[^0-9.,]", ""));
            fromCombo.setValue(transaction.getFrom());
            toCombo.setValue(transaction.getTo());
            memoField.setText(transaction.getMemo());
        } else {
            frequencyCombo.getSelectionModel().selectFirst();
        }

        // Validation
        Button okButton = (Button) getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(transaction == null);

        nameField.textProperty().addListener((obs, old, newVal) -> validate(okButton));
        amountField.textProperty().addListener((obs, old, newVal) -> validate(okButton));
        fromCombo.valueProperty().addListener((obs, old, newVal) -> validate(okButton));
        toCombo.valueProperty().addListener((obs, old, newVal) -> validate(okButton));

        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return createOrUpdateTransaction();
            }
            return null;
        });
    }

    private void validate(Button okButton) {
        boolean valid = !nameField.getText().isEmpty() &&
                !amountField.getText().isEmpty() &&
                fromCombo.getValue() != null &&
                toCombo.getValue() != null;
        okButton.setDisable(!valid);
    }

    private ScheduledTransaction createOrUpdateTransaction() {
        try {
            String name = nameField.getText();
            String frequency = frequencyCombo.getValue();
            Date startDate = Date.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            String desc = descriptionField.getText();
            long amount = parseAmount(amountField.getText());
            Source from = fromCombo.getValue();
            Source to = toCombo.getValue();
            String memo = memoField.getText();

            if (existingTransaction == null) {
                ScheduledTransaction t = ModelFactory.createScheduledTransaction(name, null, startDate, null, frequency,
                        0, 0, 0, desc, amount, from, to);
                t.setMemo(memo);
                return t;
            } else {
                existingTransaction.setScheduleName(name);
                existingTransaction.setFrequencyType(frequency);
                existingTransaction.setStartDate(startDate);
                existingTransaction.setDescription(desc);
                existingTransaction.setAmount(amount);
                existingTransaction.setFrom(from);
                existingTransaction.setTo(to);
                existingTransaction.setMemo(memo);
                return existingTransaction;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
