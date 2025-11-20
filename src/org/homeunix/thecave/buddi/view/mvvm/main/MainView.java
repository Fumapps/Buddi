package org.homeunix.thecave.buddi.view.mvvm.main;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.homeunix.thecave.buddi.view.mvvm.View;

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
        // Bind properties here
        // root.titleProperty().bind(viewModel.titleProperty()); // Note: Stage title is
        // handled in Application start
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
