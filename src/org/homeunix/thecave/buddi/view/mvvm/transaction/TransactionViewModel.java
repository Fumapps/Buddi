package org.homeunix.thecave.buddi.view.mvvm.transaction;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.homeunix.thecave.buddi.model.*;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;
import org.homeunix.thecave.buddi.plugin.api.exception.InvalidValueException;
import org.homeunix.thecave.buddi.plugin.api.exception.ModelException;
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

    // Split Management
    private final ObservableList<TransactionSplit> fromSplits = FXCollections.observableArrayList();
    private final ObservableList<TransactionSplit> toSplits = FXCollections.observableArrayList();

    public static final Source SPLIT_SOURCE = new SplitSource();

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

        // Listen for Split selection
        from.addListener((obs, oldVal, newVal) -> {
            if (newVal == SPLIT_SOURCE) {
                openSplitEditor(fromSplits, "Edit From Splits");
                updateAmountFromSplits(fromSplits);
            }
        });

        to.addListener((obs, oldVal, newVal) -> {
            if (newVal == SPLIT_SOURCE) {
                openSplitEditor(toSplits, "Edit To Splits");
                updateAmountFromSplits(toSplits);
            }
        });
    }

    private void openSplitEditor(ObservableList<TransactionSplit> splits, String title) {
        SplitTransactionView dialog = new SplitTransactionView(splits, availableSources);
        dialog.setTitle(title);
        dialog.showAndWait();
    }

    private void updateAmountFromSplits(ObservableList<TransactionSplit> splits) {
        long total = splits.stream().mapToLong(TransactionSplit::getAmount).sum();
        amount.set(TextFormatter.getFormattedCurrency(total).replaceAll("[^0-9.,]", ""));
    }

    private void loadTransactions() {
        if (document != null && account != null) {
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
            availableSources.add(SPLIT_SOURCE); // Add Split option
            availableSources.addAll(document.getAccounts());
            availableSources.addAll(document.getBudgetCategories());
        }
    }

    private void loadTransactionIntoEditor(Transaction t) {
        date.set(t.getDate());
        description.set(t.getDescription());
        amount.set(TextFormatter.getFormattedCurrency(t.getAmount()).replaceAll("[^0-9.,]", ""));

        // Handle Splits
        fromSplits.clear();
        toSplits.clear();

        if (t.getFrom() instanceof Split) {
            from.set(SPLIT_SOURCE);
            fromSplits.addAll(t.getFromSplits());
        } else {
            from.set(t.getFrom());
        }

        if (t.getTo() instanceof Split) {
            to.set(SPLIT_SOURCE);
            toSplits.addAll(t.getToSplits());
        } else {
            to.set(t.getTo());
        }

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
        fromSplits.clear();
        toSplits.clear();
        selectedTransaction.set(null);
    }

    public void createNewTransaction() {
        clearEditor();
        from.set(account);
    }

    public void delete() {
        try {
            Transaction t = selectedTransaction.get();
            if (t != null) {
                document.removeTransaction(t);
                transactions.remove(t);
                clearEditor();
                account.updateBalance();
            }
        } catch (ModelException e) {
            e.printStackTrace();
        }
    }

    public String validate() {
        if (description.get() == null || description.get().trim().isEmpty()) {
            return "Description cannot be empty.";
        }
        if (date.get() == null) {
            return "Date cannot be empty.";
        }

        if (from.get() == null) {
            return "Source (From) cannot be empty.";
        }
        if (to.get() == null) {
            return "Destination (To) cannot be empty.";
        }

        long totalAmount = parseAmount(amount.get());

        // Validate Splits
        if (from.get() == SPLIT_SOURCE) {
            long splitTotal = fromSplits.stream().mapToLong(TransactionSplit::getAmount).sum();
            if (splitTotal != totalAmount) {
                return "From splits total (" + TextFormatter.getFormattedCurrency(splitTotal) +
                        ") does not match transaction amount (" + TextFormatter.getFormattedCurrency(totalAmount)
                        + ").";
            }
        }

        if (to.get() == SPLIT_SOURCE) {
            long splitTotal = toSplits.stream().mapToLong(TransactionSplit::getAmount).sum();
            if (splitTotal != totalAmount) {
                return "To splits total (" + TextFormatter.getFormattedCurrency(splitTotal) +
                        ") does not match transaction amount (" + TextFormatter.getFormattedCurrency(totalAmount)
                        + ").";
            }
        }

        return null; // Valid
    }

    public boolean save() {
        try {
            String validationError = validate();
            if (validationError != null) {
                // View should handle this, but for now we return false to indicate failure
                // In a pure MVVM way, we might have a validationError property
                return false;
            }

            Transaction t = selectedTransaction.get();
            boolean isNew = (t == null);

            // Determine actual From/To sources (handle SplitSource)
            Source actualFrom = from.get() == SPLIT_SOURCE ? ModelFactory.createSplit() : from.get();
            Source actualTo = to.get() == SPLIT_SOURCE ? ModelFactory.createSplit() : to.get();

            if (isNew) {
                t = ModelFactory.createTransaction(date.get(), description.get(), parseAmount(amount.get()), actualFrom,
                        actualTo);
                document.addTransaction(t);
            } else {
                t.setDate(date.get());
                t.setDescription(description.get());
                t.setAmount(parseAmount(amount.get()));
                t.setFrom(actualFrom);
                t.setTo(actualTo);
            }

            t.setMemo(memo.get());
            t.setNumber(number.get());

            // Save Splits if applicable
            if (actualFrom instanceof Split) {
                t.setFromSplits(fromSplits);
            }
            if (actualTo instanceof Split) {
                t.setToSplits(toSplits);
            }

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
                int index = transactions.indexOf(t);
                if (index >= 0) {
                    transactions.set(index, t);
                }
            }

            account.updateBalance();
            return true;

        } catch (InvalidValueException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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

    public ObservableList<TransactionSplit> getFromSplits() {
        return fromSplits;
    }

    public ObservableList<TransactionSplit> getToSplits() {
        return toSplits;
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

    // Dummy Source for Split Selection
    private static class SplitSource implements Source {
        @Override
        public String getFullName() {
            return "Split";
        }

        @Override
        public String getName() {
            return "Split";
        }

        @Override
        public String getNotes() {
            return "";
        }

        @Override
        public boolean isDeleted() {
            return false;
        }

        @Override
        public void setDeleted(boolean deleted) throws InvalidValueException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setName(String name) throws InvalidValueException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setNotes(String notes) throws InvalidValueException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int compareTo(ModelObject o) {
            return 0;
        }

        @Override
        public Document getDocument() {
            return null;
        }

        @Override
        public String getUid() {
            return "SPLIT";
        }

        @Override
        public void setDocument(Document document) {
        }

        @Override
        public String toString() {
            return "Split";
        }

        @Override
        public void setChanged() {
        }

        @Override
        public Date getModified() {
            return new Date();
        }
    }
}
