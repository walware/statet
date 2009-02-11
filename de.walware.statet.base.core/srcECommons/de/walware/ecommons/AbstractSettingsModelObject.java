/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * 
 */
public abstract class AbstractSettingsModelObject {
	
	
	private ReadWriteLock fLock;
	private PropertyChangeSupport fBeanSupport;
	private boolean fIsDirty;
	
	
	protected AbstractSettingsModelObject() {
		fBeanSupport = new PropertyChangeSupport(this);
		fIsDirty = false;
		fBeanSupport.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent evt) {
				fIsDirty = true;
			}
		});
	}
	
	
	protected void installLock() {
		fLock = new ReentrantReadWriteLock(true);
	}
	public Lock getReadLock() {
		return fLock.readLock();
	}
	public Lock getWriteLock() {
		return fLock.writeLock();
	}
	
	/**
	 * @see PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
	 */
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		fBeanSupport.addPropertyChangeListener(listener);
	}
	
	/**
	 * @see PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)
	 */
	public void addPropertyChangeListener(final String propertyName,
			final PropertyChangeListener listener) {
		fBeanSupport.addPropertyChangeListener(propertyName, listener);
	}
	
	/**
	 * @see PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)
	 */
	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		fBeanSupport.removePropertyChangeListener(listener);
	}
	
	/**
	 * @see PropertyChangeSupport#removePropertyChangeListener(String, PropertyChangeListener)
	 */
	public void removePropertyChangeListener(final String propertyName,
			final PropertyChangeListener listener) {
		fBeanSupport.removePropertyChangeListener(propertyName, listener);
	}
	
	/**
	 * @see PropertyChangeSupport#firePropertyChange(String, Object, Object)
	 */
	protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
		fBeanSupport.firePropertyChange(propertyName, oldValue, newValue);
	}
	
	
	public boolean isDirty() {
		return fIsDirty;
	}
	
	public void resetDirty() {
		fIsDirty = false;
	}
	
}
