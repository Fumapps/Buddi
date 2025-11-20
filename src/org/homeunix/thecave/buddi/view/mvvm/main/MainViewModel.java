package org.homeunix.thecave.buddi.view.mvvm.main;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.homeunix.thecave.buddi.view.mvvm.ViewModel;

public class MainViewModel extends ViewModel {

    private final StringProperty title = new SimpleStringProperty("Buddi");

    public StringProperty titleProperty() {
        return title;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    @Override
    public void initialize() {
        super.initialize();
        // TODO: Load initial state, check for updates, etc.
    }
}
