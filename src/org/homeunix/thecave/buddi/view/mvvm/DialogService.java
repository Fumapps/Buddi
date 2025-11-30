package org.homeunix.thecave.buddi.view.mvvm;

import org.homeunix.thecave.buddi.model.Account;
import org.homeunix.thecave.buddi.model.AccountType;
import org.homeunix.thecave.buddi.model.BudgetCategory;
import org.homeunix.thecave.buddi.model.BudgetCategoryType;
import org.homeunix.thecave.buddi.model.ScheduledTransaction;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for displaying dialogs and alerts.
 * This allows ViewModels to request UI interactions without depending on JavaFX
 * classes directly,
 * enabling easier unit testing via mocking.
 */
public interface DialogService {

        void showError(String title, String message);

        boolean showConfirmation(String title, String message);

        Optional<BudgetCategory> showBudgetCategoryEditor(List<BudgetCategory> categories,
                        List<BudgetCategoryType> types,
                        BudgetCategory categoryToEdit);

        Optional<Account> showAccountEditor(List<AccountType> accountTypes, Account accountToEdit);

        Optional<ScheduledTransaction> showScheduledTransactionEditor(
                        org.homeunix.thecave.buddi.model.Document document,
                        ScheduledTransaction transactionToEdit);

        void showTransactions(org.homeunix.thecave.buddi.model.Account account);
}
