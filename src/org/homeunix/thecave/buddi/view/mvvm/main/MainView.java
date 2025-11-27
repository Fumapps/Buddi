package org.homeunix.thecave.buddi.view.mvvm.main;

import javafx.scene.Parent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.homeunix.thecave.buddi.view.mvvm.View;
import org.homeunix.thecave.buddi.view.mvvm.myaccounts.MyAccountsView;
import org.homeunix.thecave.buddi.view.mvvm.mybudget.MyBudgetView;
import org.homeunix.thecave.buddi.view.mvvm.reports.ReportsView;
import org.homeunix.thecave.buddi.view.mvvm.scheduled.ScheduledTransactionsView;

import java.io.File;

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
        // Menu Bar
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");

        MenuItem openItem = new MenuItem("Open...");
        openItem.setOnAction(e -> handleOpen());

        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> handleSave());

        MenuItem saveAsItem = new MenuItem("Save As...");
        saveAsItem.setOnAction(e -> handleSaveAs());

        fileMenu.getItems().addAll(openItem, saveItem, saveAsItem);
        menuBar.getMenus().add(fileMenu);

        root.setTop(menuBar);

        // Tabs
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

    private void handleOpen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Buddi File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Buddi Files", "*.buddi3", "*.buddi"));
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file != null) {
            viewModel.load(file);
        }
    }

    private void handleSave() {
        viewModel.save();
    }

    private void handleSaveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Buddi File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Buddi Files", "*.buddi3"));
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (file != null) {
            viewModel.saveAs(file);
        }
    }

    @Override
    public void bind(MainViewModel viewModel) {
        this.viewModel = viewModel;

        // Bind title
        if (root.getScene() != null && root.getScene().getWindow() instanceof Stage) {
            ((Stage) root.getScene().getWindow()).titleProperty().bind(viewModel.titleProperty());

            // Handle window close request
            ((Stage) root.getScene().getWindow()).setOnCloseRequest(event -> {
                if (!viewModel.requestClose()) {
                    // Dirty state: prompt user
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Unsaved Changes");
                    alert.setHeaderText("You have unsaved changes.");
                    alert.setContentText("Do you want to save your changes before closing?");

                    javafx.scene.control.ButtonType buttonTypeSave = new javafx.scene.control.ButtonType("Save");
                    javafx.scene.control.ButtonType buttonTypeDontSave = new javafx.scene.control.ButtonType(
                            "Don't Save");
                    javafx.scene.control.ButtonType buttonTypeCancel = new javafx.scene.control.ButtonType("Cancel",
                            javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

                    alert.getButtonTypes().setAll(buttonTypeSave, buttonTypeDontSave, buttonTypeCancel);

                    java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
                    if (result.isPresent()) {
                        if (result.get() == buttonTypeSave) {
                            viewModel.save();
                            // Proceed with close
                        } else if (result.get() == buttonTypeDontSave) {
                            // Proceed with close (discard changes)
                        } else {
                            // Cancel close
                            event.consume();
                        }
                    } else {
                        // Dialog closed without selection (treat as cancel)
                        event.consume();
                    }
                }
            });
        }

        // Bind child views to ViewModel properties
        viewModel.myAccountsViewModelProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null)
                myAccountsView.bind(newVal);
        });
        // Initial bind
        if (viewModel.getMyAccountsViewModel() != null) {
            myAccountsView.bind(viewModel.getMyAccountsViewModel());
        }

        viewModel.myBudgetViewModelProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null)
                myBudgetView.bind(newVal);
        });
        if (viewModel.getMyBudgetViewModel() != null) {
            myBudgetView.bind(viewModel.getMyBudgetViewModel());
        }

        viewModel.scheduledTransactionsViewModelProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null)
                scheduledTransactionsView.bind(newVal);
        });
        if (viewModel.getScheduledTransactionsViewModel() != null) {
            scheduledTransactionsView.bind(viewModel.getScheduledTransactionsViewModel());
        }

        viewModel.reportsViewModelProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null)
                reportsView.bind(newVal);
        });
        if (viewModel.getReportsViewModel() != null) {
            reportsView.bind(viewModel.getReportsViewModel());
        }
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
