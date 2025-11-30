package org.homeunix.thecave.buddi.view.mvvm;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.homeunix.thecave.buddi.model.Account;
import org.homeunix.thecave.buddi.model.AccountType;
import org.homeunix.thecave.buddi.model.BudgetCategory;
import org.homeunix.thecave.buddi.model.BudgetCategoryType;
import org.homeunix.thecave.buddi.model.ScheduledTransaction;
import org.homeunix.thecave.buddi.view.mvvm.myaccounts.AccountEditorDialog;
import org.homeunix.thecave.buddi.view.mvvm.mybudget.BudgetCategoryEditorDialog;
import org.homeunix.thecave.buddi.view.mvvm.scheduled.ScheduledTransactionEditorDialog;

import java.util.List;
import java.util.Optional;

public class JavaFXDialogService implements DialogService {

    @Override
    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @Override
    public Optional<BudgetCategory> showBudgetCategoryEditor(List<BudgetCategory> categories,
            List<BudgetCategoryType> types, BudgetCategory categoryToEdit) {
        BudgetCategoryEditorDialog dialog = new BudgetCategoryEditorDialog(categories, types, categoryToEdit);
        return dialog.showAndWait();
    }

    @Override
    public Optional<Account> showAccountEditor(List<AccountType> accountTypes, Account accountToEdit) {
        AccountEditorDialog dialog = new AccountEditorDialog(accountTypes, accountToEdit);
        return dialog.showAndWait();
    }

    @Override
    public Optional<ScheduledTransaction> showScheduledTransactionEditor(
            org.homeunix.thecave.buddi.model.Document document, ScheduledTransaction transactionToEdit) {
        ScheduledTransactionEditorDialog dialog = new ScheduledTransactionEditorDialog(document, transactionToEdit);
        return dialog.showAndWait();
    }

    @Override
    public void showTransactions(org.homeunix.thecave.buddi.model.Account account) {
        try {
            org.homeunix.thecave.buddi.view.mvvm.transaction.TransactionViewModel viewModel = new org.homeunix.thecave.buddi.view.mvvm.transaction.TransactionViewModel(
                    account.getDocument(), account, this);
            org.homeunix.thecave.buddi.view.mvvm.transaction.TransactionView view = new org.homeunix.thecave.buddi.view.mvvm.transaction.TransactionView();
            view.bind(viewModel);

            javafx.scene.Scene scene = new javafx.scene.Scene(view.getRoot(), 800, 600);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Transactions - " + account.getName());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showError("Error opening transactions", e.getMessage());
            e.printStackTrace();
        }
    }
}
