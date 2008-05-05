/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ltk.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import de.walware.eclipsecommons.FastList;
import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.ElementChangedEvent;
import de.walware.eclipsecommons.ltk.IElementChangedListener;
import de.walware.eclipsecommons.ltk.IModelElementDelta;
import de.walware.eclipsecommons.ltk.IModelManager;
import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.WorkingContext;
import de.walware.eclipsecommons.ltk.ast.IAstNode;


/**
 * Controls input of a part and its model updates.
 */
public class ElementInfoController implements IModelElementInputProvider {
	
	private static int NEWINPUT_DELAY = 100;
	
	
	private IModelManager fModelProvider;
	private WorkingContext fModelContext;
	private IElementChangedListener fElementChangeListener;
	
	private final FastList<IModelElementInputListener> fListenerList = new FastList<IModelElementInputListener>(IModelElementInputListener.class);
	private final FastList<IModelElementInputListener> fNewListenerList = new FastList<IModelElementInputListener>(IModelElementInputListener.class);
	
	private volatile ISourceUnit fInput;
	private final Object fInputLock = new Object();
	private ISourceUnit fNewInput;
	private final NewInputUpdater fNewInputJob = new NewInputUpdater();
		
	private class NewInputUpdater extends Job implements ISchedulingRule {
		
		public NewInputUpdater() {
			super("ViewPart Model Element Updater"); // //$NON-NLS-1$
			setPriority(Job.SHORT);
			setRule(this);
			setSystem(true);
			setUser(false);
		}
			
		public boolean contains(final ISchedulingRule rule) {
			return (rule == this);
		}
		
		public boolean isConflicting(final ISchedulingRule rule) {
			return (rule == this);
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			ISourceUnit input;
			IModelElementInputListener[] listeners;
			synchronized (fInputLock) {
				if (monitor.isCanceled()
						|| (fInput == null && fNewInput == null)) {
					return Status.CANCEL_STATUS;
				}
				if (fNewInput == null) {
					listeners = checkNewListeners();
				}
				else {
					final AstInfo<? extends IAstNode> astInfo = fNewInput.getAstInfo(null, false, null);
					if (astInfo == null || astInfo.level < 1) {
						return Status.CANCEL_STATUS;
					}
					fInput = fNewInput;
					fNewInput = null;
					checkNewListeners();
					listeners = fListenerList.toArray();
				}
				input = fInput;
			}
			
			if (listeners != null && listeners.length > 0) {
				notifyInitial(listeners, input);
			}
			return Status.OK_STATUS;
		}
		
		@Override
		protected void canceling() {
			fNotifyMonitor.setCanceled(true);
		}
		
	};
	private final IProgressMonitor fNotifyMonitor = new NullProgressMonitor();
	
	
	public ElementInfoController(final IModelManager manager, final WorkingContext context) {
		fElementChangeListener = new IElementChangedListener() {
			public void elementChanged(final ElementChangedEvent event) {
				ISourceUnit input;
				IModelElementInputListener[] listeners;
				synchronized (fInputLock) {
					if (fNewInput != null && fNewInput.equals(event.delta.getModelElement())) {
						if (fNewInputJob.getState() != Job.WAITING) {
							fNewInputJob.schedule();
						}
						return;
					}
					if (fInput == null || !fInput.equals(event.delta.getModelElement())) {
						return;
					}
					input = fInput;
					listeners = fListenerList.toArray();
				}
				
				try {
					Job.getJobManager().beginRule(fNewInputJob, null);
					notifyUpdated(listeners, input, event.delta);
				}
				finally {
					Job.getJobManager().endRule(fNewInputJob);
				}
			}
		};
		
		fModelProvider = manager;
		fModelContext = context;
		fModelProvider.addElementChangedListener(fElementChangeListener, fModelContext);
	}
	
	public void dispose() {
		fModelProvider.removeElementChangedListener(fElementChangeListener, fModelContext);
	}
	
	
	public void setInput(final ISourceUnit input) {
		synchronized (fInputLock) {
			fInput = null;
			fNewInput = input;
			
			checkNewListeners();
			fNewInputJob.cancel();
			notifyChanged(fListenerList.toArray(), input);
		}
		
		fNewInputJob.schedule(NEWINPUT_DELAY);
	}
	
	private IModelElementInputListener[] checkNewListeners() {
		final IModelElementInputListener[] listeners = fNewListenerList.clear();
		for (int i = 0; i < listeners.length; i++) {
			fListenerList.add(listeners[i]);
		}
		return listeners;
	}
	
	private void notifyChanged(final IModelElementInputListener[] listeners, final ISourceUnit input) {
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].elementChanged(input);
		}
	}
	
	private void notifyInitial(final IModelElementInputListener[] listeners, final ISourceUnit input) {
		if (input != fInput) {
			return;
		}
		try {
			input.connect();
			for (int i = 0; i < listeners.length; i++) {
				if (input != fInput) {
					return;
				}
				listeners[i].elementInitialInfo(input);
			}
		}
		finally {
			input.disconnect();
		}
	}
	
	private void notifyUpdated(final IModelElementInputListener[] listeners, final ISourceUnit input, final IModelElementDelta delta) {
		if (input != fInput) {
			return;
		}
		try {
			input.connect();
			for (int i = 0; i < listeners.length; i++) {
				if (input != fInput) {
					return;
				}
				listeners[i].elementUpdatedInfo(input, delta);
			}
		}
		finally {
			input.disconnect();
		}
	}
	
	public ISourceUnit getInput() {
		return fInput;
	}
	
	public void addListener(final IModelElementInputListener listener) {
		synchronized (fInputLock) {
			ISourceUnit input = fNewInput;
			if (input == null) {
				input = fInput;
			}
			if (input != null) {
				notifyChanged(new IModelElementInputListener[] { listener }, input);
			}
			if (input == null || fNewInput == input) {
				fListenerList.add(listener);
				return;
			}
			fNewListenerList.add(listener);
		}
		if (fNewInputJob.getState() != Job.WAITING) {
			fNewInputJob.schedule();
		}
	}
	
	public void removeListener(final IModelElementInputListener listener) {
		fNewListenerList.remove(listener);
		fListenerList.remove(listener);
	}
	
}
