/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.dialogs;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.databinding.DirtyTracker;
import de.walware.ecommons.ui.ECommonsUI;


/**
 * 
 */
public class DatabindingSupport {
	
	
	private Realm fRealm;
	private DataBindingContext fDbc;
	
	
	public DatabindingSupport(final Control rootControl) {
		fRealm = Realm.getDefault();
		fDbc = new DataBindingContext(fRealm);
		
		rootControl.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(final DisposeEvent e) {
				dispose();
			}
		});
	}
	
	
	public DataBindingContext getContext() {
		return fDbc;
	}
	
	public Realm getRealm() {
		return fRealm;
	}
	
	private void dispose() {
		if (fDbc != null) {
			try {
				fDbc.dispose();
			}
			catch (final Throwable e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, ECommonsUI.PLUGIN_ID,
						"An error occurend when dispose databinding", e)); //$NON-NLS-1$
			}
			fDbc = null;
		}
	}
	
	public void installStatusListener(final IStatusChangeListener listener) {
		final AggregateValidationStatus validationStatus = new AggregateValidationStatus(fDbc, AggregateValidationStatus.MAX_SEVERITY);
		validationStatus.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(final ValueChangeEvent event) {
				final IStatus status = (IStatus) event.diff.getNewValue();
				listener.statusChanged(status);
			}
		});
		
		listener.statusChanged((IStatus) validationStatus.getValue());
		new DirtyTracker(fDbc) { // sets initial status on first change again, because initial errors are suppressed
			@Override
			public void handleChange() {
				if (!isDirty()) {
					listener.statusChanged((IStatus) validationStatus.getValue());
					super.handleChange();
				}
			}
		};
	}
	
}
