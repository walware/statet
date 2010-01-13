/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.walware.ecommons.FastList;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ui.text.sourceediting.ISourceEditor;


/**
 * Reconciler for {@link ISourceEditor}s using Eclipse Job API.
 */
public class EcoReconciler2 implements IReconciler {
	
	
	public static interface ISourceUnitStrategy {
		
		void setInput(ISourceUnit su);
		
	}
	
	protected static class StrategyEntry {
		
		final IReconcilingStrategy strategy;
		final IReconcilingStrategyExtension strategyExtension;
		final ISourceUnitStrategy suStrategy;
		boolean initialed;
		
		StrategyEntry(final IReconcilingStrategy strategy) {
			this.strategy = strategy;
			this.strategyExtension = (strategy instanceof IReconcilingStrategyExtension) ?
					(IReconcilingStrategyExtension) strategy : null;
			this.initialed = false;
			this.suStrategy = (strategy instanceof ISourceUnitStrategy) ?
					(ISourceUnitStrategy) strategy : null;
		}
		
		@Override
		public int hashCode() {
			return strategy.hashCode();
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (obj instanceof StrategyEntry) {
				return ( ((StrategyEntry) obj).strategy == strategy);
			}
			return false;
		}
	}
	
	
	private class ReconcileJob extends Job implements ISchedulingRule {
		
		ReconcileJob(final String name) {
			super("Reconciler '"+name+"'"); //$NON-NLS-1$ //$NON-NLS-2$
			setPriority(Job.SHORT);
			setRule(this);
			setSystem(true);
			setUser(false);
		}
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			if (!monitor.isCanceled()) {
				processReconcile(monitor);
			}
			return Status.OK_STATUS;
		}
		
		public boolean contains(final ISchedulingRule rule) {
			return rule == this;
		}
		
		public boolean isConflicting(final ISchedulingRule rule) {
			return rule == this;
		}
	}
	
	
	private class VisibleListener implements Listener {
		public void handleEvent(final Event event) {
			switch (event.type) {
			case SWT.Show:
				fIsEditorVisible = true;
				return;
			case SWT.Hide:
				fIsEditorVisible = false;
				return;
			}
		}
	}
	
	/**
	 * Internal document listener and text input listener.
	 */
	private class DocumentListener implements IDocumentListener, ITextInputListener {
		
		public void documentAboutToBeChanged(final DocumentEvent e) {
		}
		
		public void documentChanged(final DocumentEvent e) {
			scheduleReconcile();
		}
		
		public void inputDocumentAboutToBeChanged(final IDocument oldInput, final IDocument newInput) {
			if (fDocument != null && oldInput == fDocument && newInput != fDocument) {
				disconnectDocument();
			}
		}
		
		public void inputDocumentChanged(final IDocument oldInput, final IDocument newInput) {
			connectDocument();
		}
	}
	
	/** Internal document and text input listener. */
	private DocumentListener fDocumentListener = new DocumentListener();
	private VisibleListener fVisibleListener;
	/** Job for scheduled background reconciling */
	private ReconcileJob fJob;
	/** The background thread delay. */
	private int fDelay = 500;
	
	/** The text viewer's document. */
	private IDocument fDocument;
	/** The text viewer */
	private ITextViewer fViewer;
	/** optional editor */
	private ISourceEditor fEditor;
	
	/** Tells whether this reconciler's editor is active. */
	private volatile boolean fIsEditorVisible;
	
	/** The current source unit */
	private ISourceUnit fSourceUnit;
	
	private FastList<StrategyEntry> fStrategies = new FastList<StrategyEntry>(StrategyEntry.class, ListenerList.EQUALITY);
	
	
	/**
	 * Creates a new reconciler without configuring it.
	 */
	public EcoReconciler2() {
		super();
	}
	
	/**
	 * Creates a new reconciler without configuring it.
	 */
	public EcoReconciler2(final ISourceEditor editor) {
		super();
		fEditor = editor;
	}
	
	
	/**
	 * Tells the reconciler how long it should wait for further text changes before
	 * activating the appropriate reconciling strategies.
	 * 
	 * @param delay the duration in milliseconds of a change collection period.
	 */
	public void setDelay(final int delay) {
		fDelay = delay;
	}
	
	/**
	 * Returns the input document of the text viewer this reconciler is installed on.
	 * 
	 * @return the reconciler document
	 */
	protected IDocument getDocument() {
		return fDocument;
	}
	
	
	/**
	 * Returns the text viewer this reconciler is installed on.
	 * 
	 * @return the text viewer this reconciler is installed on
	 */
	protected ITextViewer getTextViewer() {
		return fViewer;
	}
	
	/**
	 * Tells whether this reconciler's editor is active.
	 * 
	 * @return <code>true</code> if the editor is active
	 */
	protected boolean isEditorVisible() {
		return fIsEditorVisible;
	}
	
	
	public void install(final ITextViewer textViewer) {
		Assert.isNotNull(textViewer);
		fViewer = textViewer;
		
		fVisibleListener = new VisibleListener();
		final StyledText textWidget = fViewer.getTextWidget();
		textWidget.addListener(SWT.Show, fVisibleListener);
		textWidget.addListener(SWT.Hide, fVisibleListener);
		fIsEditorVisible = textWidget.isVisible();
		
		fViewer.addTextInputListener(fDocumentListener);
		connectDocument();
	}
	
	public void uninstall() {
		if (fViewer != null) {
			disconnectDocument();
			fViewer.removeTextInputListener(fDocumentListener);
			fViewer = null;
		}
	}
	
	protected void connectDocument() {
		final IDocument document = fViewer.getDocument();
		if (document == null || fDocument == document) {
			return;
		}
		fDocument = document;
		fSourceUnit = fEditor.getSourceUnit();
		reconcilerDocumentChanged(fDocument);
		
		fJob = new ReconcileJob(getInputName());
		fDocument.addDocumentListener(fDocumentListener);
		
		scheduleReconcile();
	}
	
	protected String getInputName() {
		if (fSourceUnit != null) {
			return fSourceUnit.getId();
		}
		return "-"; //$NON-NLS-1$
	}
	
	/**
	 * Hook called when the document whose contents should be reconciled
	 * has been changed, i.e., the input document of the text viewer this
	 * reconciler is installed on. Usually, subclasses use this hook to
	 * inform all their reconciling strategies about the change.
	 * 
	 * @param newDocument the new reconciler document
	 */
	protected void reconcilerDocumentChanged(final IDocument newDocument) {
	}
	
	protected void disconnectDocument() {
		if (fDocument != null) {
			fDocument.removeDocumentListener(fDocumentListener);
			fDocument = null;
			fSourceUnit = null;
		}
		if (fJob != null) {
			fJob.cancel();
			fJob = null;
		}
	}
	
	private synchronized void scheduleReconcile() {
		if ((fJob.getState() & (Job.SLEEPING | Job.WAITING)) == 0) {
			aboutToBeReconciled();
		}
		fJob.cancel();
		fJob.schedule(fDelay);
	}
	
	/**
	 * Hook for subclasses which want to perform some
	 * action as soon as reconciliation is needed.
	 * <p>
	 * Default implementation is to do nothing.
	 */
	protected void aboutToBeReconciled() {
	}
	
	protected void processReconcile(final IProgressMonitor monitor) {
		final IDocument document = getDocument();
		final ISourceUnit input = fSourceUnit;
		if (document == null || input == null) {
			return;
		}
		final IRegion region = new Region(0, document.getLength());
		final StrategyEntry[] reconcilingStrategies = getReconcilingStrategies();
		for (final StrategyEntry s : reconcilingStrategies) {
			synchronized (s.strategy) {
				if (s.suStrategy != null && input != null) {
					s.suStrategy.setInput(input);
				}
				else {
					s.strategy.setDocument(document);
				}
				if (!prepareStrategyReconcile(s)) {
					continue;
				}
				if (monitor.isCanceled()) {
					return;
				}
				if (s.strategyExtension != null) {
					s.strategyExtension.setProgressMonitor(monitor);
					if (!s.initialed) {
						s.strategyExtension.initialReconcile();
						s.initialed = true;
						continue;
					}
				}
				s.strategy.reconcile(region);
			}
		}
	}
	
	protected boolean prepareStrategyReconcile(final StrategyEntry s) {
		return true;
	}
	
	public void addReconcilingStrategy(final IReconcilingStrategy strategy) {
		fStrategies.add(new StrategyEntry(strategy));
	}
	
	protected StrategyEntry[] getReconcilingStrategies() {
		return fStrategies.toArray();
	}
	
	public IReconcilingStrategy getReconcilingStrategy(final String contentType) {
		return null;
	}
	
}
