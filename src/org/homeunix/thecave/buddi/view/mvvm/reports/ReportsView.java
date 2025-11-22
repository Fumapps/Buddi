package org.homeunix.thecave.buddi.view.mvvm.reports;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.util.StringConverter;
import org.homeunix.thecave.buddi.plugin.api.BuddiReportPlugin;
import org.homeunix.thecave.buddi.view.mvvm.View;

public class ReportsView implements View<ReportsViewModel> {

    private final SplitPane root;
    private final ListView<BuddiReportPlugin> reportList;
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final Button generateButton;
    private final WebView webView;

    private ReportsViewModel viewModel;

    public ReportsView() {
        root = new SplitPane();
        reportList = new ListView<>();
        startDatePicker = new DatePicker();
        endDatePicker = new DatePicker();
        generateButton = new Button("Generate Report");
        webView = new WebView();

        initializeUI();
    }

    private void initializeUI() {
        // Left Panel: Controls
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(250);

        leftPanel.getChildren().add(new Label("Select Report:"));
        leftPanel.getChildren().add(reportList);

        GridPane dateGrid = new GridPane();
        dateGrid.setHgap(10);
        dateGrid.setVgap(5);
        dateGrid.add(new Label("Start Date:"), 0, 0);
        dateGrid.add(startDatePicker, 0, 1);
        dateGrid.add(new Label("End Date:"), 0, 2);
        dateGrid.add(endDatePicker, 0, 3);

        leftPanel.getChildren().add(dateGrid);
        leftPanel.getChildren().add(generateButton);

        // Right Panel: WebView
        BorderPane rightPanel = new BorderPane();
        rightPanel.setCenter(webView);

        root.getItems().addAll(leftPanel, rightPanel);
        root.setDividerPositions(0.3);

        // Report List Cell Factory
        reportList.setCellFactory(param -> new ListCell<BuddiReportPlugin>() {
            @Override
            protected void updateItem(BuddiReportPlugin item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Ideally we should use a translator here, but for now we use the description
                    // key or name
                    // The plugin usually returns a key for translation.
                    // We'll just use toString() or try to get a name if available.
                    // BuddiReportPlugin doesn't have a getName(), but it has getDescription().
                    // Let's assume getDescription() returns a key or readable string.
                    setText(item.getClass().getSimpleName()); // Fallback to class name for now if description is a key
                }
            }
        });
    }

    @Override
    public void bind(ReportsViewModel viewModel) {
        this.viewModel = viewModel;

        reportList.setItems(viewModel.getAvailableReports());
        reportList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.selectedReportProperty().set(newVal);
        });

        // Select first if available
        if (!viewModel.getAvailableReports().isEmpty()) {
            reportList.getSelectionModel().selectFirst();
        }

        startDatePicker.valueProperty().bindBidirectional(viewModel.startDateProperty());
        endDatePicker.valueProperty().bindBidirectional(viewModel.endDateProperty());

        generateButton.setOnAction(e -> viewModel.generateReport());

        viewModel.reportHtmlProperty().addListener((obs, oldVal, newVal) -> {
            webView.getEngine().loadContent(newVal);
        });
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
