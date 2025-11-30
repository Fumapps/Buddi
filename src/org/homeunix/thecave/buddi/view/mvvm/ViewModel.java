package org.homeunix.thecave.buddi.view.mvvm;

/**
 * Base class for all ViewModels in the Buddi MVVM architecture.
 * ViewModels should expose state via JavaFX Properties.
 */
public abstract class ViewModel {

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
