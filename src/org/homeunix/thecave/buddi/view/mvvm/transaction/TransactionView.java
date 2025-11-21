package org.homeunix.thecave.buddi.view.mvvm.transaction;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.homeunix.thecave.buddi.model.Account;
import org.homeunix.thecave.buddi.view.mvvm.View;
import org.homeunix.thecave.buddi.view.mvvm.ViewModel;

public class TransactionView implements View<ViewModel> {

    private final StackPane root;
    private final Account account;

    public TransactionView(Account account) {
        this.account = account;
        this.root = new StackPane();
        initializeUI();
    }

    private void initializeUI() {
        Label label = new Label("Transactions for: " + account.getName() + "\n(Placeholder)");
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        root.getChildren().add(label);
    }

    @Override
    public void bind(ViewModel viewModel) {
        // No specific ViewModel for this placeholder yet
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
