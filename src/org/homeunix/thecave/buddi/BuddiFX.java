package org.homeunix.thecave.buddi;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.homeunix.thecave.buddi.view.mvvm.main.MainView;
import org.homeunix.thecave.buddi.view.mvvm.main.MainViewModel;

public class BuddiFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainViewModel viewModel = new MainViewModel();
        MainView view = new MainView();

        viewModel.initialize();
        view.bind(viewModel);

        Scene scene = new Scene(view.getRoot(), 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle(viewModel.getTitle());

        // Bind title
        viewModel.titleProperty().addListener((obs, oldVal, newVal) -> primaryStage.setTitle(newVal));

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
