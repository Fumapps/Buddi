package org.homeunix.thecave.buddi.view.mvvm;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Base class for all ViewModels in the Buddi MVVM architecture.
 * Provides support for property change notifications.
 */
public abstract class ViewModel {

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Lifecycle method called when the View is initializing.
     */
    public void initialize() {
        // Default no-op
    }

    /**
     * Lifecycle method called when the ViewModel is being destroyed.
     * Subclasses should override this to clean up resources (listeners, etc.).
     */
    public void dispose() {
        // Default no-op
    }
}
