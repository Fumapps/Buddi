package org.homeunix.thecave.buddi.view.mvvm.main;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.io.File;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;
import org.homeunix.thecave.buddi.view.mvvm.ViewModel;
import org.homeunix.thecave.buddi.viewmodel.MyAccountsViewModel;

public class MainViewModel extends ViewModel {

    private final StringProperty title = new SimpleStringProperty("Buddi");
    private File currentFile;
    private Document document;

    private final ObjectProperty<MyAccountsViewModel> myAccountsViewModel = new SimpleObjectProperty<>();
    private final ObjectProperty<org.homeunix.thecave.buddi.view.mvvm.mybudget.MyBudgetViewModel> myBudgetViewModel = new SimpleObjectProperty<>();
    private final ObjectProperty<org.homeunix.thecave.buddi.view.mvvm.scheduled.ScheduledTransactionsViewModel> scheduledTransactionsViewModel = new SimpleObjectProperty<>();
    private final ObjectProperty<org.homeunix.thecave.buddi.view.mvvm.reports.ReportsViewModel> reportsViewModel = new SimpleObjectProperty<>();

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

    public ObjectProperty<org.homeunix.thecave.buddi.view.mvvm.mybudget.MyBudgetViewModel> myBudgetViewModelProperty() {
        return myBudgetViewModel;
    }

    public org.homeunix.thecave.buddi.view.mvvm.mybudget.MyBudgetViewModel getMyBudgetViewModel() {
        return myBudgetViewModel.get();
    }

    public ObjectProperty<org.homeunix.thecave.buddi.view.mvvm.scheduled.ScheduledTransactionsViewModel> scheduledTransactionsViewModelProperty() {
        return scheduledTransactionsViewModel;
    }

    public org.homeunix.thecave.buddi.view.mvvm.scheduled.ScheduledTransactionsViewModel getScheduledTransactionsViewModel() {
        return scheduledTransactionsViewModel.get();
    }

    public ObjectProperty<org.homeunix.thecave.buddi.view.mvvm.reports.ReportsViewModel> reportsViewModelProperty() {
        return reportsViewModel;
    }

    public org.homeunix.thecave.buddi.view.mvvm.reports.ReportsViewModel getReportsViewModel() {
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
            // TODO: Handle error gracefully (show alert)
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
            // TODO: Propagate error to view
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
            // TODO: Propagate error
        }
    }

    private void updateChildViewModels() {
        this.myAccountsViewModel.set(new MyAccountsViewModel(document));
        this.myBudgetViewModel.set(new org.homeunix.thecave.buddi.view.mvvm.mybudget.MyBudgetViewModel(document));
        this.scheduledTransactionsViewModel
                .set(new org.homeunix.thecave.buddi.view.mvvm.scheduled.ScheduledTransactionsViewModel(document));
        this.reportsViewModel.set(new org.homeunix.thecave.buddi.view.mvvm.reports.ReportsViewModel(document));
    }

    private void updateTitle() {
        if (currentFile != null) {
            setTitle("Buddi - " + currentFile.getName());
        } else {
            setTitle("Buddi - [Untitled]");
        }
    }
}
