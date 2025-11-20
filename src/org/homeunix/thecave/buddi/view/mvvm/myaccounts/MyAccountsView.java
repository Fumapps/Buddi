package org.homeunix.thecave.buddi.view.mvvm.myaccounts;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.homeunix.thecave.buddi.model.Account;
import org.homeunix.thecave.buddi.model.AccountType;
import org.homeunix.thecave.buddi.view.mvvm.View;
import org.homeunix.thecave.buddi.viewmodel.MyAccountsViewModel;

public class MyAccountsView implements View<MyAccountsViewModel> {

    private final BorderPane root;
    private final Label netWorthLabel;
    private final TreeView<Object> accountTree;
    private MyAccountsViewModel viewModel;

    public MyAccountsView() {
        this.root = new BorderPane();
        this.netWorthLabel = new Label();
        this.accountTree = new TreeView<>();

        initializeUI();
    }

    private void initializeUI() {
        VBox topBox = new VBox(netWorthLabel);
        netWorthLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10;");

        root.setTop(topBox);
        root.setCenter(accountTree);
    }

    @Override
    public void bind(MyAccountsViewModel viewModel) {
        this.viewModel = viewModel;

        // Bind net worth
        netWorthLabel.textProperty().bind(viewModel.netWorthProperty());

        // Populate tree (initial)
        populateTree();

        // Listen for tree changes (legacy property change)
        viewModel.addPropertyChangeListener(evt -> {
            if (MyAccountsViewModel.PROPERTY_ACCOUNT_TREE_CHANGED.equals(evt.getPropertyName())) {
                javafx.application.Platform.runLater(this::populateTree);
            }
        });
    }

    private void populateTree() {
        TreeItem<Object> rootItem = new TreeItem<>("Accounts");
        rootItem.setExpanded(true);

        for (AccountType type : viewModel.getAccountTypes()) {
            TreeItem<Object> typeItem = new TreeItem<>(type);
            typeItem.setExpanded(type.isExpanded());

            for (Account account : viewModel.getAccounts(type)) {
                if (!account.isDeleted()) {
                    TreeItem<Object> accountItem = new TreeItem<>(account);
                    typeItem.getChildren().add(accountItem);
                }
            }

            rootItem.getChildren().add(typeItem);
        }

        accountTree.setRoot(rootItem);
        accountTree.setShowRoot(false);

        // Set CellFactory to render AccountType and Account objects correctly
        accountTree.setCellFactory(tv -> new javafx.scene.control.TreeCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item instanceof org.homeunix.thecave.buddi.model.AccountType) {
                        setText(((org.homeunix.thecave.buddi.model.AccountType) item).getName());
                    } else if (item instanceof org.homeunix.thecave.buddi.model.Account) {
                        setText(((org.homeunix.thecave.buddi.model.Account) item).getName());
                    } else {
                        setText(item.toString());
                    }
                }
            }
        });
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
