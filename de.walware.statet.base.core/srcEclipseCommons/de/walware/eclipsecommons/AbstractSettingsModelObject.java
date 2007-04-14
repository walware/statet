/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


/**
 * 
 */
public abstract class AbstractSettingsModelObject {

	
	private PropertyChangeSupport fBeanSupport;
	private boolean fIsDirty;
	
	
	protected AbstractSettingsModelObject() {
		
		fBeanSupport = new PropertyChangeSupport(this);
		fIsDirty = false;
		fBeanSupport.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				fIsDirty = true;
			}
		});
	}
	
	/**
	 * @see PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		
		fBeanSupport.addPropertyChangeListener(listener);
	}

	/**
	 * @see PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		
		fBeanSupport.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * @see PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		
		fBeanSupport.removePropertyChangeListener(listener);
	}

	/**
	 * @see PropertyChangeSupport#removePropertyChangeListener(String, PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		
		fBeanSupport.removePropertyChangeListener(propertyName, listener);
	}

	/**
	 * @see PropertyChangeSupport#firePropertyChange(String, Object, Object)
	 */
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		
		fBeanSupport.firePropertyChange(propertyName, oldValue, newValue);
	}
	
	
	public boolean isDirty() {
		
		return fIsDirty;
	}
	
	public void resetDirty() {
		
		fIsDirty = false;
	}
	
}
