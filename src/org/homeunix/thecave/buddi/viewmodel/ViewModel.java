/*
 * Base interface for all ViewModels in Buddi.
 * ViewModels encapsulate presentation logic and state separate from Swing components.
 */
package org.homeunix.thecave.buddi.viewmodel;

import java.beans.PropertyChangeListener;

/**
 * Marker interface and contract for ViewModel classes.
 * ViewModels manage state and behaviour for a view, exposing commands and observable properties.
 * They do not depend on Swing or UI libraries.
 */
public interface ViewModel {
	/**
	 * Add a listener to be notified of property changes.
	 * @param listener listener to add
	 */
	void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Remove a listener from property change notifications.
	 * @param listener listener to remove
	 */
	void removePropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Clean up resources held by this ViewModel (e.g., unregister listeners from the model).
	 * Call this when the ViewModel is being disposed.
	 */
	void dispose();
}
