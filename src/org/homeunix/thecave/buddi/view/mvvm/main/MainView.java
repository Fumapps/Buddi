package org.homeunix.thecave.buddi.view.mvvm.main;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.homeunix.thecave.buddi.view.mvvm.View;
import org.homeunix.thecave.buddi.view.mvvm.myaccounts.MyAccountsView;

public class MainView implements View<MainViewModel> {

    private final BorderPane root;
    private MainViewModel viewModel;

    public MainView() {
        this.root = new BorderPane();
        initializeUI();
    }

    private void initializeUI() {
        Label welcomeLabel = new Label("Welcome to Buddi (JavaFX)");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #333;");

        StackPane centerPane = new StackPane(welcomeLabel);
        root.setCenter(centerPane);
    }

    @Override
    public void bind(MainViewModel viewModel) {
        this.viewModel = viewModel;

        // Initialize and bind MyAccountsView
        if (viewModel.getMyAccountsViewModel() != null) {
            MyAccountsView myAccountsView = new MyAccountsView();
            myAccountsView.bind(viewModel.getMyAccountsViewModel());
            root.setCenter(myAccountsView.getRoot());
        }
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
