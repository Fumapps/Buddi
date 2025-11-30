package org.homeunix.thecave.buddi.view.mvvm.main;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.io.File;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;
import org.homeunix.thecave.buddi.view.mvvm.DialogService;
import org.homeunix.thecave.buddi.view.mvvm.ViewModel;

import org.homeunix.thecave.buddi.view.mvvm.myaccounts.MyAccountsViewModel;
import org.homeunix.thecave.buddi.view.mvvm.mybudget.MyBudgetViewModel;
import org.homeunix.thecave.buddi.view.mvvm.scheduled.ScheduledTransactionsViewModel;
import org.homeunix.thecave.buddi.view.mvvm.reports.ReportsViewModel;

public class MainViewModel extends ViewModel {

    private final DialogService dialogService;
    private final StringProperty title = new SimpleStringProperty("Buddi");
    private File currentFile;
    private Document document;

    private final ObjectProperty<MyAccountsViewModel> myAccountsViewModel = new SimpleObjectProperty<>();
    private final ObjectProperty<MyBudgetViewModel> myBudgetViewModel = new SimpleObjectProperty<>();
    private final ObjectProperty<ScheduledTransactionsViewModel> scheduledTransactionsViewModel = new SimpleObjectProperty<>();
    private final ObjectProperty<ReportsViewModel> reportsViewModel = new SimpleObjectProperty<>();

    public MainViewModel(DialogService dialogService) {
        this.dialogService = dialogService;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public ObjectProperty<MyAccountsViewModel> myAccountsViewModelProperty() {
        return myAccountsViewModel;
    }

    public MyAccountsViewModel getMyAccountsViewModel() {
        return myAccountsViewModel.get();
    }

    public ObjectProperty<MyBudgetViewModel> myBudgetViewModelProperty() {
        return myBudgetViewModel;
    }

    public MyBudgetViewModel getMyBudgetViewModel() {
        return myBudgetViewModel.get();
    }

    public ObjectProperty<ScheduledTransactionsViewModel> scheduledTransactionsViewModelProperty() {
        return scheduledTransactionsViewModel;
    }

    public ScheduledTransactionsViewModel getScheduledTransactionsViewModel() {
        return scheduledTransactionsViewModel.get();
    }

    public ObjectProperty<ReportsViewModel> reportsViewModelProperty() {
        return reportsViewModel;
    }

    public ReportsViewModel getReportsViewModel() {
        return reportsViewModel.get();
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            // Load default or autosaved document
            load(null);
        } catch (Exception e) {
            e.printStackTrace();
            dialogService.showError("Initialization Error", "Failed to load default document: " + e.getMessage());
        }
    }

    public void load(File file) {
        try {
            if (file == null) {
                this.document = ModelFactory.createDocument();
                this.currentFile = null;
            } else {
                this.document = ModelFactory.createDocument(file);
                this.currentFile = file;
            }
            updateChildViewModels();
            updateTitle();
        } catch (Exception e) {
            e.printStackTrace();
            dialogService.showError("Load Error", "Failed to load file: " + e.getMessage());
        }
    }

    public void save() {
        if (currentFile != null) {
            saveAs(currentFile);
        } else {
            // View should handle "Save As" if currentFile is null, but if we get here:
            // We can't do much without a file.
            // In a real app, we might throw an exception or return false.
        }
    }

    public void saveAs(File file) {
        try {
            document.saveAs(file);
            this.currentFile = file;
            updateTitle();
        } catch (Exception e) {
            e.printStackTrace();
            dialogService.showError("Save Error", "Failed to save file: " + e.getMessage());
        }
    }

    private void updateChildViewModels() {
        // Dispose old ViewModels if they exist
        if (myAccountsViewModel.get() != null)
            myAccountsViewModel.get().dispose();
        if (myBudgetViewModel.get() != null)
            myBudgetViewModel.get().dispose();
        if (scheduledTransactionsViewModel.get() != null)
            scheduledTransactionsViewModel.get().dispose();
        if (reportsViewModel.get() != null)
            reportsViewModel.get().dispose();

        this.myAccountsViewModel.set(new MyAccountsViewModel(document, dialogService));
        this.myBudgetViewModel.set(new MyBudgetViewModel(document, dialogService));
        this.scheduledTransactionsViewModel.set(new ScheduledTransactionsViewModel(document, dialogService));
        this.reportsViewModel.set(new ReportsViewModel(document));
    }

    private void updateTitle() {
        if (currentFile != null) {
            setTitle("Buddi - " + currentFile.getName());
        } else {
            setTitle("Buddi - [Untitled]");
        }
    }

    public boolean isChanged() {
        return document != null && document.isChanged();
    }

    /**
     * Request to close the application/window.
     * 
     * @return true if the window can be closed, false if the close should be
     *         aborted.
     */
    public boolean requestClose() {
        if (isChanged()) {
            return false; // Signal that we need a prompt
        }
        return true; // Clean, can close
    }
}
