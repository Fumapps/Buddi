package org.homeunix.thecave.buddi.view.mvvm.myaccounts;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.scene.Parent;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import org.homeunix.thecave.buddi.model.Account;
import org.homeunix.thecave.buddi.model.AccountType;
import org.homeunix.thecave.buddi.view.mvvm.View;

public class MyAccountsView implements View<MyAccountsViewModel> {

    private final BorderPane root;
    private final TreeTableView<Object> treeTableView;
    private MyAccountsViewModel viewModel;

    public MyAccountsView() {
        root = new BorderPane();
        treeTableView = new TreeTableView<>();
        initializeUI();
    }

    private void initializeUI() {
        treeTableView.setShowRoot(false);
        treeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

        TreeTableColumn<Object, String> nameColumn = new TreeTableColumn<>("Account / Type");
        nameColumn.setCellValueFactory(param -> {
            Object value = param.getValue().getValue();
            if (value instanceof AccountType) {
                return new ReadOnlyObjectWrapper<>(((AccountType) value).getName());
            } else if (value instanceof Account) {
                return new ReadOnlyObjectWrapper<>(((Account) value).getName());
            }
            return new ReadOnlyObjectWrapper<>("");
        });

        TreeTableColumn<Object, String> amountColumn = new TreeTableColumn<>("Amount");
        amountColumn.setCellValueFactory(param -> {
            Object value = param.getValue().getValue();
            if (value instanceof Account) {
                // TODO: Format currency
                return new ReadOnlyObjectWrapper<>(String.valueOf(((Account) value).getBalance()));
            }
            return new ReadOnlyObjectWrapper<>("");
        });

        treeTableView.getColumns().addAll(nameColumn, amountColumn);

        // Context Menu
        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
        javafx.scene.control.MenuItem newItem = new javafx.scene.control.MenuItem("New Account");
        javafx.scene.control.MenuItem editItem = new javafx.scene.control.MenuItem("Edit");
        javafx.scene.control.MenuItem deleteItem = new javafx.scene.control.MenuItem("Delete");
        javafx.scene.control.MenuItem openTransactionsItem = new javafx.scene.control.MenuItem("Open Transactions");

        newItem.setOnAction(e -> {
            TreeItem<Object> selected = treeTableView.getSelectionModel().getSelectedItem();
            AccountType type = null;
            if (selected != null && selected.getValue() instanceof AccountType) {
                type = (AccountType) selected.getValue();
            }
            viewModel.createNewAccount(type);
        });

        editItem.setOnAction(e -> {
            TreeItem<Object> selected = treeTableView.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getValue() instanceof Account) {
                viewModel.editAccount((Account) selected.getValue());
            }
        });

        deleteItem.setOnAction(e -> {
            TreeItem<Object> selected = treeTableView.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getValue() instanceof Account) {
                viewModel.deleteAccount((Account) selected.getValue());
            }
        });

        openTransactionsItem.setOnAction(e -> {
            TreeItem<Object> selected = treeTableView.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getValue() instanceof Account) {
                viewModel.openTransactions((Account) selected.getValue());
            }
        });

        contextMenu.getItems().addAll(newItem, editItem, deleteItem, new javafx.scene.control.SeparatorMenuItem(),
                openTransactionsItem);
        treeTableView.setContextMenu(contextMenu);

        root.setCenter(treeTableView);
    }

    @Override
    public void bind(MyAccountsViewModel viewModel) {
        this.viewModel = viewModel;
        populateTree();

        // Listen for changes
        // Since we don't have a direct observable list of everything, we might need to
        // rely on the ViewModel triggering updates
        // But for now, let's just repopulate when the account types list changes (if we
        // exposed it as observable)
        // Or we can add a listener to the document via ViewModel if exposed, but
        // ViewModel should handle it.
        // MyAccountsViewModel has getAccountTypes() which returns
        // ObservableList<AccountType> in my previous edit?
        // Let's check MyAccountsViewModel.

        viewModel.getAccountTypes().addListener((ListChangeListener<AccountType>) c -> populateTree());

        // Also listen for net worth if we displayed it, but we don't in this view (it's
        // usually in the status bar or main view)
    }

    private void populateTree() {
        TreeItem<Object> rootItem = new TreeItem<>();
        rootItem.setExpanded(true);

        for (AccountType type : viewModel.getAccountTypes()) {
            TreeItem<Object> typeItem = new TreeItem<>(type);
            typeItem.setExpanded(true); // Default to expanded

            for (Account account : viewModel.getAccounts(type)) {
                if (!account.isDeleted()) {
                    typeItem.getChildren().add(new TreeItem<>(account));
                }
            }

            rootItem.getChildren().add(typeItem);
        }

        treeTableView.setRoot(rootItem);
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
