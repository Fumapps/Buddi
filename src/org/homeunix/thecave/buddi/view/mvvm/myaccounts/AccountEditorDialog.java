package org.homeunix.thecave.buddi.view.mvvm.myaccounts;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.homeunix.thecave.buddi.model.Account;
import org.homeunix.thecave.buddi.model.AccountType;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;
import org.homeunix.thecave.buddi.util.Formatter;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class AccountEditorDialog extends Dialog<Account> {

    private final TextField nameField;
    private final ComboBox<AccountType> typeCombo;
    private final TextField balanceField;
    private final DatePicker startDatePicker;
    private final TextArea notesArea;

    private final Account existingAccount;

    public AccountEditorDialog(List<AccountType> accountTypes, Account account) {
        this.existingAccount = account;

        setTitle(account == null ? "Create New Account" : "Edit Account");
        setHeaderText(account == null ? "Enter account details" : "Edit account details");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Fields
        nameField = new TextField();
        typeCombo = new ComboBox<>(FXCollections.observableArrayList(accountTypes));
        balanceField = new TextField();
        startDatePicker = new DatePicker();
        notesArea = new TextArea();
        notesArea.setPrefRowCount(3);

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Starting Balance:"), 0, 2);
        grid.add(balanceField, 1, 2);
        grid.add(new Label("Start Date:"), 0, 3);
        grid.add(startDatePicker, 1, 3);
        grid.add(new Label("Notes:"), 0, 4);
        grid.add(notesArea, 1, 4);

        getDialogPane().setContent(grid);

        // Converter for AccountType
        typeCombo.setConverter(new StringConverter<AccountType>() {
            @Override
            public String toString(AccountType object) {
                return object != null ? object.getName() : "";
            }

            @Override
            public AccountType fromString(String string) {
                return null;
            }
        });

        // Populate if editing
        if (account != null) {
            nameField.setText(account.getName());
            typeCombo.setValue(account.getAccountType());
            balanceField.setText(
                    TextFormatter.getFormattedCurrency(account.getStartingBalance()).replaceAll("[^0-9.,]", ""));
            if (account.getStartDate() != null) {
                startDatePicker
                        .setValue(account.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            notesArea.setText(account.getNotes());
        } else {
            startDatePicker.setValue(java.time.LocalDate.now());
        }

        // Result Converter
        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return createOrUpdateAccount();
            }
            return null;
        });
    }

    private Account createOrUpdateAccount() {
        try {
            String name = nameField.getText();
            AccountType type = typeCombo.getValue();
            long balance = parseAmount(balanceField.getText());
            Date startDate = Date.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            String notes = notesArea.getText();

            if (existingAccount == null) {
                Account newAccount = ModelFactory.createAccount(name, type);
                newAccount.setStartingBalance(balance);
                newAccount.setStartDate(startDate);
                newAccount.setNotes(notes);
                return newAccount;
            } else {
                existingAccount.setName(name);
                existingAccount.setAccountType(type);
                existingAccount.setStartingBalance(balance);
                existingAccount.setStartDate(startDate);
                existingAccount.setNotes(notes);
                return existingAccount;
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
