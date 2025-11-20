package org.homeunix.thecave.buddi.view.mvvm.main;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.impl.ModelFactory;
import org.homeunix.thecave.buddi.view.mvvm.ViewModel;
import org.homeunix.thecave.buddi.viewmodel.MyAccountsViewModel;

public class MainViewModel extends ViewModel {

    private final StringProperty title = new SimpleStringProperty("Buddi");

    private Document document;
    private MyAccountsViewModel myAccountsViewModel;

    public StringProperty titleProperty() {
        return title;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public MyAccountsViewModel getMyAccountsViewModel() {
        return myAccountsViewModel;
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            // Load default or autosaved document
            this.document = ModelFactory.createDocument();
            this.myAccountsViewModel = new MyAccountsViewModel(document);
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: Handle error gracefully (show alert)
        }
    }
}
