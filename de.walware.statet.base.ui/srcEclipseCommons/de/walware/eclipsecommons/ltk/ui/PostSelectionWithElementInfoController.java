/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.eclipsecommons.FastList;
import de.walware.eclipsecommons.ICommonStatusConstants;
import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.IModelElement;
import de.walware.eclipsecommons.ltk.IModelElementDelta;
import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.ast.AstSelection;
import de.walware.eclipsecommons.ltk.ast.IAstNode;
import de.walware.eclipsecommons.ltk.ui.ISelectionWithElementInfoListener.StateData;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * Combines {@link IPostSelectionProvider} and {@link IModelElementInputProvider}
 * to provide support for {@link ISelectionWithElementInfoListener}
 */
public class PostSelectionWithElementInfoController {
	
	
	private class SelectionTask extends Job {
		
		
		private final class Data implements StateData {
			
			int stateNr;
			SelectionChangedEvent selectionEvent;
			ISourceUnit inputElement;
			AstSelection astSelection;
			
			
			Data(final ISourceUnit input, final int runNr, final SelectionChangedEvent selection) {
				this.stateNr = runNr;
				this.inputElement = input;
				this.selectionEvent = selection;
			}
			
			
			public ISelection getLastSelection() {
				return selectionEvent.getSelection();
			}
			
			public IModelElement getInputElement() {
				return inputElement;
			}
			
			public AstSelection getAstSelection() {
				return astSelection;
			}
			
			public boolean isStillValid() {
				return (fCurrentNr == stateNr);
			}
		}
		
		private int fLastNr;
		
		
		public SelectionTask() {
			super("PostSelection with Model Updater"); // //$NON-NLS-1$
			setPriority(Job.SHORT);
			setSystem(true);
			setUser(false);
			
			fLastNr = fCurrentNr = Integer.MIN_VALUE;
		}
		
		
		@Override
		protected synchronized IStatus run(final IProgressMonitor monitor) {
			ISourceUnit input = null;
			try {
				checkNewInput();
				
				AstInfo<? extends IAstNode> astInfo;
				final Data run;
				synchronized (fInputLock) {
					run = new Data(fInput, fCurrentNr, fCurrentSelection);
					if (run.inputElement == null || run.selectionEvent == null
							|| (fLastNr == run.stateNr && fNewListeners.isEmpty())) {
						return Status.OK_STATUS;
					}
					input = run.inputElement;
					input.connect(monitor);
					astInfo = input.getAstInfo(null, false, null);
					if (astInfo == null || astInfo.level < fInfoLevel || astInfo.stamp != input.getDocument(monitor).getModificationStamp()) {
						return Status.OK_STATUS;
					}
				}
				
				if (run.selectionEvent.getSelection() instanceof ITextSelection) {
					final ITextSelection textSelection = (ITextSelection) run.selectionEvent.getSelection();
					run.astSelection = AstSelection.search(astInfo.root, textSelection.getOffset(), textSelection.getOffset()+textSelection.getLength(), AstSelection.MODE_COVERING_SAME_LAST);
				}
				
				ISelectionWithElementInfoListener[] listeners = fNewListeners.clear();
				if (run.stateNr != fLastNr) {
					listeners = fListeners.toArray();
					fLastNr = run.stateNr;
				}
				for (int i = 0; i < listeners.length; i++) {
					if (!run.isStillValid()) {
						return Status.CANCEL_STATUS;
					}
					try {
						listeners[i].stateChanged(run);
					}
					catch (final Exception e) {
						logListenerError(e);
					}
				}
			}
			finally {
				if (input != null) {
					input.disconnect(monitor);
				}
			}
			
			return Status.OK_STATUS;
		}
		
		private void checkNewInput() {
			if (fInputChanged) {
				synchronized (fInputLock) {
					fInputChanged = false;
				}
				final ISelectionWithElementInfoListener[] listeners = fListeners.toArray();
				for (int i = 0; i < listeners.length; i++) {
					try {
						listeners[i].inputChanged();
					}
					catch (final Exception e) {
						logListenerError(e);
					}
				}
			}
		}
	}
	
	private IPostSelectionProvider fPostSelectionProvider;
	private IModelElementInputProvider fModelProvider;
	private int fInfoLevel = 1;
	private final FastList<ISelectionWithElementInfoListener> fListeners = new FastList<ISelectionWithElementInfoListener>(ISelectionWithElementInfoListener.class);
	private final FastList<ISelectionWithElementInfoListener> fNewListeners = new FastList<ISelectionWithElementInfoListener>(ISelectionWithElementInfoListener.class);
	private final Object fInputLock = new Object();
	
	private IModelElementInputListener fElementChangeListener;
	private ISelectionChangedListener fPostSelectionListener;
	private PostSelectionCancelExtension fCancelExtension;
	
	private ISourceUnit fInput; // current input
	private boolean fInputChanged;
	private SelectionChangedEvent fCurrentSelection; // current post selection
	private volatile int fCurrentNr; // stamp to check, if information still up-to-date
	
	private final SelectionTask fUpdateJob = new SelectionTask();
	
	
	public PostSelectionWithElementInfoController(final IModelElementInputProvider modelProvider,
			final IPostSelectionProvider postProvider, final PostSelectionCancelExtension cancelExt) {
		fPostSelectionProvider = postProvider;
		
		fModelProvider = modelProvider;
		
		fElementChangeListener = new IModelElementInputListener() {
			public void elementChanged(final IModelElement element) {
				synchronized (fInputLock) {
					if (fUpdateJob.getState() == Job.WAITING) {
						fUpdateJob.cancel();
					}
					fInput = (ISourceUnit) element;
					fInputChanged = true;
					fCurrentNr++;
					fCurrentSelection = null;
					fUpdateJob.schedule();
				}
			}
			public void elementInitialInfo(final IModelElement element) {
				checkUpdate(element);
			}
			public void elementUpdatedInfo(final IModelElement element, final IModelElementDelta delta) {
				checkUpdate(element);
			}
			private void checkUpdate(final IModelElement element) {
				synchronized (fInputLock) {
					fCurrentNr++;
					if (fCurrentSelection == null) {
						return;
					}
				}
				fUpdateJob.run(null);
			}
		};
		fPostSelectionListener = new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				synchronized (PostSelectionWithElementInfoController.this) {
					fCurrentNr++;
					fCurrentSelection = event;
					fUpdateJob.schedule();
				}
			}
		};
		
		fPostSelectionProvider.addPostSelectionChangedListener(fPostSelectionListener);
		fModelProvider.addListener(fElementChangeListener);
		if (fCancelExtension != null) {
			fCancelExtension.fController = this;
			fCancelExtension = cancelExt;
			fCancelExtension.init();
		}
	}
	
	
	public void cancel() {
		synchronized (fInputLock) {
			fCurrentNr++;
			fCurrentSelection = null;
		}
	}
	
	public void dispose() {
		cancel();
		fModelProvider.removeListener(fElementChangeListener);
		fPostSelectionProvider.removePostSelectionChangedListener(fPostSelectionListener);
		if (fCancelExtension != null) {
			fCancelExtension.dispose();
		}
	}
	
	
	public void addListener(final ISelectionWithElementInfoListener listener) {
		fListeners.add(listener);
		fNewListeners.add(listener);
		fUpdateJob.schedule();
	}
	
	public void remove(final ISelectionWithElementInfoListener listener) {
		fNewListeners.remove(listener);
		fListeners.remove(listener);
	}
	
	
	private void logListenerError(final Throwable e) {
		StatusManager.getManager().handle(new Status(
				IStatus.ERROR, StatetUIPlugin.PLUGIN_ID, ICommonStatusConstants.INTERNAL_PLUGGED_IN,
				"An error occurred when calling a registered listener.", e)); //$NON-NLS-1$
	}
	
}
