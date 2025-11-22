package org.homeunix.thecave.buddi.view.mvvm.scheduled;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.ScheduledTransaction;
import org.homeunix.thecave.buddi.model.Source;
import org.homeunix.thecave.buddi.view.mvvm.ViewModel;

import java.util.List;

public class ScheduledTransactionsViewModel extends ViewModel {

    private final Document document;
    private final ObservableList<ScheduledTransaction> scheduledTransactions = FXCollections.observableArrayList();
    private final ObjectProperty<ScheduledTransaction> selectedTransaction = new SimpleObjectProperty<>();

    public ScheduledTransactionsViewModel(Document document) {
        this.document = document;
        loadScheduledTransactions();
    }

    private void loadScheduledTransactions() {
        if (document != null) {
            scheduledTransactions.setAll(document.getScheduledTransactions());
        }
    }

    public ObservableList<ScheduledTransaction> getScheduledTransactions() {
        return scheduledTransactions;
    }

    public ObjectProperty<ScheduledTransaction> selectedTransactionProperty() {
        return selectedTransaction;
    }

    public void deleteSelectedTransaction() {
        ScheduledTransaction t = selectedTransaction.get();
        if (t != null) {
            try {
                document.removeScheduledTransaction(t);
                scheduledTransactions.remove(t);
                selectedTransaction.set(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void createNewTransaction() {
        ScheduledTransactionEditorDialog dialog = new ScheduledTransactionEditorDialog(document, null);
        dialog.showAndWait().ifPresent(t -> {
            try {
                document.addScheduledTransaction(t);
                scheduledTransactions.add(t);
                selectedTransaction.set(t);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void editSelectedTransaction() {
        ScheduledTransaction t = selectedTransaction.get();
        if (t != null) {
            ScheduledTransactionEditorDialog dialog = new ScheduledTransactionEditorDialog(document, t);
            dialog.showAndWait().ifPresent(updated -> {
                // Refresh list item to update view
                int index = scheduledTransactions.indexOf(t);
                if (index >= 0) {
                    scheduledTransactions.set(index, updated);
                }
            });
        }
    }

    public List<Source> getSources() {
        return document.getSources();
    }
}
