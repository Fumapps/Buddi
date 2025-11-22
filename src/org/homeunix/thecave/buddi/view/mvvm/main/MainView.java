package org.homeunix.thecave.buddi.view.mvvm.main;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.homeunix.thecave.buddi.view.mvvm.View;
import org.homeunix.thecave.buddi.view.mvvm.myaccounts.MyAccountsView;
import org.homeunix.thecave.buddi.view.mvvm.mybudget.MyBudgetView;

import org.homeunix.thecave.buddi.view.mvvm.scheduled.ScheduledTransactionsView;

import org.homeunix.thecave.buddi.view.mvvm.reports.ReportsView;

public class MainView implements View<MainViewModel> {

    private final BorderPane root;
    private MainViewModel viewModel;

    private final MyAccountsView myAccountsView;
    private final MyBudgetView myBudgetView;
    private final ScheduledTransactionsView scheduledTransactionsView;
    private final ReportsView reportsView;

    public MainView() {
        this.root = new BorderPane();
        this.myAccountsView = new MyAccountsView();
        this.myBudgetView = new MyBudgetView();
        this.scheduledTransactionsView = new ScheduledTransactionsView();
        this.reportsView = new ReportsView();
        initializeUI();
    }

    private void initializeUI() {
        TabPane tabPane = new TabPane();

        Tab accountsTab = new Tab("My Accounts");
        accountsTab.setClosable(false);
        accountsTab.setContent(myAccountsView.getRoot());

        Tab budgetTab = new Tab("My Budget");
        budgetTab.setClosable(false);
        budgetTab.setContent(myBudgetView.getRoot());

        // Scheduled Transactions Tab
        Tab scheduledTab = new Tab("Scheduled Transactions", scheduledTransactionsView.getRoot());
        scheduledTab.setClosable(false);

        // Reports Tab
        Tab reportsTab = new Tab("Reports", reportsView.getRoot());
        reportsTab.setClosable(false);

        tabPane.getTabs().addAll(accountsTab, budgetTab, scheduledTab, reportsTab);

        root.setCenter(tabPane);
    }

    @Override
    public void bind(MainViewModel viewModel) {
        this.viewModel = viewModel;

        // Bind child views
        myAccountsView.bind(viewModel.getMyAccountsViewModel());
        myBudgetView.bind(viewModel.getMyBudgetViewModel());
        scheduledTransactionsView.bind(viewModel.getScheduledTransactionsViewModel());
        reportsView.bind(viewModel.getReportsViewModel());
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
