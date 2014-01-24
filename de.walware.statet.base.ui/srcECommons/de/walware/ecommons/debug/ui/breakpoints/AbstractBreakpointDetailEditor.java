/*=============================================================================#
 # Copyright (c) 2010-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.ecommons.debug.ui.breakpoints;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableEvent;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.FastList;
import de.walware.ecommons.databinding.DirtyTracker;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.MessageUtil;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


public abstract class AbstractBreakpointDetailEditor {
	
	
	private final boolean fMnemonics;
	
	private Composite fComposite;
	
	private boolean fIsDirty;
	
	private DataBindingContext fDbc;
	private boolean fIgnoreChanges;
	private AggregateValidationStatus fAggregateStatus;
	private IStatus fCurrentStatus;
	
	private final Set<IObservable> fAutosaveBindings;
	
	private final FastList<IPropertyListener> fPropertyChangeListeners;
	
	private boolean fSaveError;
	
	
	protected AbstractBreakpointDetailEditor(final boolean mnemonics, final boolean autosave,
			final FastList<IPropertyListener> listeners) {
		fMnemonics = mnemonics;
		fAutosaveBindings = (autosave) ? new HashSet<IObservable>() : null;
		fPropertyChangeListeners = listeners;
	}
	
	
	public Composite createControl(final Composite parent) {
		fComposite = new Composite(parent, SWT.NONE);
		fComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 1));
		
		addContent(fComposite);
		
		initBindings();
		
		return fComposite;
	}
	
	protected void addContent(final Composite parent) {
	}
	
	protected Control getComposite() {
		return fComposite;
	}
	
	protected String checkLabel(final String text) {
		if (fMnemonics) {
			return text;
		}
		else {
			return MessageUtil.removeMnemonics(text);
		}
	}
	
	protected DataBindingContext getDataBindingContext() {
		return fDbc;
	}
	
	protected void initBindings() {
		final Realm realm = Realm.getDefault();
		fDbc = new DataBindingContext(realm);
		
		addBindings(fDbc, realm);
		
		fAggregateStatus = new AggregateValidationStatus(fDbc, AggregateValidationStatus.MAX_SEVERITY);
		fAggregateStatus.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(final ValueChangeEvent event) {
				fCurrentStatus = (IStatus) event.diff.getNewValue();
//				updateDialogState();
			}
		});
		fCurrentStatus = ValidationStatus.ok();
		
		new DirtyTracker(fDbc) {
			@Override
			public void handleChange(final ObservableEvent event) {
				if (!fIgnoreChanges && fAutosaveBindings != null && event != null
						&& fAutosaveBindings.contains(event.getObservable())) {
					save();
					return;
				}
				if (!isDirty()) {
//					fCurrentStatus = (IStatus) fAggregateStatus.getValue();
					setDirty(true);
//					updateDialogState();
				}
			}
		};
		
		fComposite.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent e) {
				disposeBindings();
			}
		});
	}
	
	
	protected void enableAutosave(final Binding binding) {
		fAutosaveBindings.add(binding.getModel());
	}
	
	private void disposeBindings() {
		if (fAggregateStatus != null) {
			fAggregateStatus.dispose();
			fAggregateStatus = null;
		}
		if (fDbc != null) {
			fDbc.dispose();
			fDbc = null;
		}
		if (fAutosaveBindings != null) {
			fAutosaveBindings.clear();
		}
	}
	
	/**
	 * @param dbc
	 * @param realm
	 */
	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
	}
	
	public boolean isDirty() {
		return fIsDirty;
	}
	
	protected void setDirty(final boolean dirty) {
		if (fIsDirty != dirty) {
			fIsDirty = dirty;
			firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
		}
	}
	
	protected void firePropertyChange(final int propId) {
		if (fPropertyChangeListeners != null) {
			for (final IPropertyListener listener : fPropertyChangeListeners.toArray()) {
				listener.propertyChanged(this, propId);
			}
		}
	}
	
	/**
	 * @return
	 */
	public IStatus getStatus() {
		return fCurrentStatus;
	}
	
	/**
	 * @param input
	 */
	public final void setInput(final Object input) {
		fIgnoreChanges = true;
		try {
			doSetInput(input);
		}
		finally {
			fIgnoreChanges = false;
			setDirty(false);
		}
	}
	
	protected void doSetInput(final Object input) {
	}
	
	protected void logLoadError(final CoreException e) {
		StatusManager.getManager().handle(new Status(IStatus.ERROR, StatetUIPlugin.PLUGIN_ID,
				"An error occurred when loading settings for the selected breakpoint." ));
	}
	
	/**
	 * Applies the settings to the breakpoints
	 * @return 
	 */
	public final IStatus save() {
		fSaveError = false;
		try {
			doSave();
		}
		finally {
			if (!fSaveError) {
				setDirty(false);
			}
		}
		
		final IStatus status = (IStatus) fAggregateStatus.getValue();
		if ((status == null || status.isOK()) && fSaveError) {
			return new Status(IStatus.ERROR, StatetUIPlugin.PLUGIN_ID,
					"Failed to save all changes." );
		}
		return status;
	}
	
	protected void doSave() {
	}
	
	protected void logSaveError(final CoreException e) {
		fSaveError = true;
		StatusManager.getManager().handle(new Status(IStatus.ERROR, StatetUIPlugin.PLUGIN_ID,
				"An error occurred when saving settings for the selected breakpoint." ));
	}
	
}
