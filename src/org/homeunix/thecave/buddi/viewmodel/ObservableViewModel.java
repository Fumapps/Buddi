/*
 * Base class for ViewModels that use PropertyChangeSupport for observable state.
 */
package org.homeunix.thecave.buddi.viewmodel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Convenience base class for ViewModels that emit property change events.
 * Subclasses use firePropertyChange() to notify listeners of state updates.
 */
public abstract class ObservableViewModel implements ViewModel {
	protected final PropertyChangeSupport propertyChangeSupport;

	public ObservableViewModel() {
		this.propertyChangeSupport = new PropertyChangeSupport(this);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	@Override
	public void dispose() {
		// Default implementation: no-op. Subclasses override to clean up.
	}
}
