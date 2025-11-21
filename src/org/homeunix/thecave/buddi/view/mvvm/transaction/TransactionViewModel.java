package org.homeunix.thecave.buddi.view.mvvm.transaction;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.homeunix.thecave.buddi.model.*;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;
import org.homeunix.thecave.buddi.plugin.api.exception.InvalidValueException;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;
import org.homeunix.thecave.buddi.util.Formatter;
import org.homeunix.thecave.buddi.view.mvvm.ViewModel;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionViewModel extends ViewModel {

    private final Document document;
    private final Account account; // The account we are viewing transactions for

    // List of transactions for the table
    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private final ObjectProperty<Transaction> selectedTransaction = new SimpleObjectProperty<>();

    // Editor Fields
    private final ObjectProperty<Date> date = new SimpleObjectProperty<>(new Date());
    private final StringProperty description = new SimpleStringProperty("");
    private final StringProperty amount = new SimpleStringProperty("");
    private final ObjectProperty<Source> from = new SimpleObjectProperty<>();
    private final ObjectProperty<Source> to = new SimpleObjectProperty<>();
    private final StringProperty memo = new SimpleStringProperty("");
    private final StringProperty number = new SimpleStringProperty("");
    private final BooleanProperty cleared = new SimpleBooleanProperty(false);
    private final BooleanProperty reconciled = new SimpleBooleanProperty(false);

    // Available Sources for ComboBoxes
    private final ObservableList<Source> availableSources = FXCollections.observableArrayList();

    public TransactionViewModel(Document document, Account account) {
        this.document = document;
        this.account = account;

        loadTransactions();
        loadSources();

        // Listen for selection changes to populate editor
        selectedTransaction.addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadTransactionIntoEditor(newVal);
            } else {
                clearEditor();
            }
        });
    }

    private void loadTransactions() {
        if (document != null && account != null) {
            // In a real implementation, we might want to filter or sort
            // For now, just get all transactions involving this account?
            // The Swing implementation likely iterates all transactions and filters.
            // Or Account might have a getTransactions() method?
            // Checking Account interface... it doesn't have getTransactions().
            // Document has getTransactions().

            List<Transaction> allTransactions = document.getTransactions();
            List<Transaction> accountTransactions = allTransactions.stream()
                    .filter(t -> t.getFrom().equals(account) || t.getTo().equals(account))
                    .collect(Collectors.toList());

            transactions.setAll(accountTransactions);
        }
    }

    private void loadSources() {
        if (document != null) {
            availableSources.clear();
            availableSources.addAll(document.getAccounts());
            availableSources.addAll(document.getBudgetCategories());
        }
    }

    private void loadTransactionIntoEditor(Transaction t) {
        date.set(t.getDate());
        description.set(t.getDescription());
        amount.set(TextFormatter.getFormattedCurrency(t.getAmount()).replaceAll("[^0-9.,]", "")); // Simple formatting
        from.set(t.getFrom());
        to.set(t.getTo());
        memo.set(t.getMemo());
        number.set(t.getNumber());

        if (t.getFrom().equals(account)) {
            cleared.set(t.isClearedFrom());
            reconciled.set(t.isReconciledFrom());
        } else {
            cleared.set(t.isClearedTo());
            reconciled.set(t.isReconciledTo());
        }
    }

    public void clearEditor() {
        date.set(new Date());
        description.set("");
        amount.set("");
        from.set(null);
        to.set(null);
        memo.set("");
        number.set("");
        cleared.set(false);
        reconciled.set(false);
        selectedTransaction.set(null); // Deselect to indicate "New" mode
    }

    public void createNewTransaction() {
        clearEditor();
        // Set defaults if needed
        // For example, if we are in "Checking", "From" might default to "Checking"
        from.set(account);
    }

    public void save() {
        try {
            Transaction t = selectedTransaction.get();
            boolean isNew = (t == null);

            if (isNew) {
                t = ModelFactory.createTransaction(date.get(), description.get(), parseAmount(amount.get()), from.get(),
                        to.get());
                document.addTransaction(t);
            } else {
                t.setDate(date.get());
                t.setDescription(description.get());
                t.setAmount(parseAmount(amount.get()));
                t.setFrom(from.get());
                t.setTo(to.get());
            }

            t.setMemo(memo.get());
            t.setNumber(number.get());

            if (t.getFrom().equals(account)) {
                t.setClearedFrom(cleared.get());
                t.setReconciledFrom(reconciled.get());
            } else {
                t.setClearedTo(cleared.get());
                t.setReconciledTo(reconciled.get());
            }

            if (isNew) {
                transactions.add(t);
                selectedTransaction.set(t);
            } else {
                // Refresh list item?
                int index = transactions.indexOf(t);
                if (index >= 0) {
                    transactions.set(index, t);
                }
            }

            // Update balances
            account.updateBalance();
            // Ideally we should trigger a global refresh or event

        } catch (InvalidValueException e) {
            e.printStackTrace();
            // Show error to user
        } catch (Exception e) {
            e.printStackTrace();
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

    // Getters for Properties
    public ObservableList<Transaction> getTransactions() {
        return transactions;
    }

    public ObjectProperty<Transaction> selectedTransactionProperty() {
        return selectedTransaction;
    }

    public ObjectProperty<Date> dateProperty() {
        return date;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty amountProperty() {
        return amount;
    }

    public ObjectProperty<Source> fromProperty() {
        return from;
    }

    public ObjectProperty<Source> toProperty() {
        return to;
    }

    public StringProperty memoProperty() {
        return memo;
    }

    public StringProperty numberProperty() {
        return number;
    }

    public BooleanProperty clearedProperty() {
        return cleared;
    }

    public BooleanProperty reconciledProperty() {
        return reconciled;
    }

    public ObservableList<Source> getAvailableSources() {
        return availableSources;
    }

    public Account getAccount() {
        return account;
    }
}
