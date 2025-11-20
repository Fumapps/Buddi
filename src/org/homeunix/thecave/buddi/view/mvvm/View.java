package org.homeunix.thecave.buddi.view.mvvm;

import javafx.scene.Parent;

/**
 * Interface for all Views in the Buddi MVVM architecture.
 * 
 * @param <VM> The type of ViewModel this View is bound to.
 */
public interface View<VM extends ViewModel> {

    /**
     * Binds the View to the given ViewModel.
     * This is where data binding and event handling setup should occur.
     * 
     * @param viewModel The ViewModel to bind to.
     */
    void bind(VM viewModel);

    /**
     * Returns the root JavaFX node of this View.
     * 
     * @return The root Parent node.
     */
    Parent getRoot();
}
