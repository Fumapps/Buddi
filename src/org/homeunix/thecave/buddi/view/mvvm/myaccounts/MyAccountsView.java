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

import org.homeunix.thecave.buddi.util.InternalFormatter;

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

        // Bind net worth text
        netWorthLabel.textProperty().bind(viewModel.netWorthProperty());

        // Update net worth color based on value
        updateNetWorthColor();
        // Listen for changes to update color (since we can't easily bind color to a
        // long value without a converter or listener)
        viewModel.netWorthProperty().addListener((obs, oldVal, newVal) -> updateNetWorthColor());

        // Bind selection
        accountTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.selectedItemProperty().set(newVal.getValue());
            } else {
                viewModel.selectedItemProperty().set(null);
            }
        });

        // Handle double-click
        accountTree.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<Object> selectedItem = accountTree.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.getValue() instanceof Account) {
                    Account account = (Account) selectedItem.getValue();
                    viewModel.openTransactions(account);

                    // Open placeholder view (for verification)
                    openTransactionView(account);
                }
            }
        });

        // Populate tree (initial)
        populateTree();

        // Listen for tree changes (legacy property change)
        viewModel.addPropertyChangeListener(evt -> {
            if (MyAccountsViewModel.PROPERTY_ACCOUNT_TREE_CHANGED.equals(evt.getPropertyName())) {
                javafx.application.Platform.runLater(this::populateTree);
            } else if (MyAccountsViewModel.PROPERTY_NET_WORTH_TEXT.equals(evt.getPropertyName())) {
                javafx.application.Platform.runLater(this::updateNetWorthColor);
            }
        });
    }

    private void openTransactionView(Account account) {
        org.homeunix.thecave.buddi.view.mvvm.transaction.TransactionViewModel txViewModel = viewModel
                .createTransactionViewModel(account);
        org.homeunix.thecave.buddi.view.mvvm.transaction.TransactionView txView = new org.homeunix.thecave.buddi.view.mvvm.transaction.TransactionView();
        txView.bind(txViewModel);

        javafx.scene.Scene scene = new javafx.scene.Scene(txView.getRoot(), 800, 600);
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Transactions - " + account.getName());
        stage.setScene(scene);
        stage.show();
    }

    private void updateNetWorthColor() {
        long netWorth = viewModel.getNetWorthValue();
        if (netWorth < 0) {
            netWorthLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10; -fx-text-fill: red;");
        } else {
            netWorthLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10; -fx-text-fill: black;");
        }
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
                    setGraphic(null);
                    setStyle("");
                    setContextMenu(null);
                } else {
                    if (item instanceof AccountType) {
                        AccountType type = (AccountType) item;
                        setText(type.getName());

                        // Style based on credit/debit
                        if (type.isCredit()) {
                            setStyle("-fx-text-fill: red;");
                        } else {
                            setStyle("-fx-text-fill: black;");
                        }

                        // Context Menu for Account Type
                        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
                        javafx.scene.control.MenuItem newItem = new javafx.scene.control.MenuItem("New Account");
                        newItem.setOnAction(e -> viewModel.createNewAccount(type));
                        contextMenu.getItems().add(newItem);
                        setContextMenu(contextMenu);

                    } else if (item instanceof Account) {
                        Account account = (Account) item;
                        String name = account.getName();
                        String balance = org.homeunix.thecave.buddi.plugin.api.util.TextFormatter
                                .getFormattedCurrency(account.getBalance(), false, account.getAccountType().isCredit());

                        // Strip HTML from balance if present (TextFormatter might still return it
                        // depending on flags)
                        balance = balance.replaceAll("<[^>]+>", "");

                        setText(name + " - " + balance);

                        // Style based on credit/debit and negative balance
                        boolean isRed = InternalFormatter.isRed(account, account.getBalance());
                        if (isRed) {
                            setStyle("-fx-text-fill: red;");
                        } else {
                            setStyle("-fx-text-fill: black;");
                        }

                        // Strikethrough for deleted accounts (though they are filtered out in
                        // populateTree currently)
                        if (account.isDeleted()) {
                            setStyle(getStyle() + "-fx-strikethrough: true;");
                        }

                        // Context Menu for Account
                        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
                        javafx.scene.control.MenuItem openItem = new javafx.scene.control.MenuItem("Open Transactions");
                        openItem.setOnAction(e -> {
                            viewModel.openTransactions(account);
                            openTransactionView(account);
                        });

                        javafx.scene.control.MenuItem editItem = new javafx.scene.control.MenuItem("Edit");
                        editItem.setOnAction(e -> viewModel.editAccount(account));

                        javafx.scene.control.MenuItem deleteItem = new javafx.scene.control.MenuItem("Delete");
                        deleteItem.setOnAction(e -> viewModel.deleteAccount(account));

                        contextMenu.getItems().addAll(openItem, editItem, deleteItem);
                        setContextMenu(contextMenu);

                    } else {
                        setText(item.toString());
                        setStyle("");
                        setContextMenu(null);
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
