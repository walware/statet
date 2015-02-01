/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ui.sourceediting.EcoReconciler2.ISourceUnitStrategy;

import de.walware.statet.r.internal.sweave.model.LtxRweaveEditorWorkingCopy;


public class LtxRweaveReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension, ISourceUnitStrategy {
	
	
	private ISourceUnit fInput;
	private IProgressMonitor fMonitor;
	
	
	public LtxRweaveReconcilingStrategy() {
	}
	
	
	@Override
	public void initialReconcile() {
		reconcile();
	}
	
	@Override
	public void setDocument(final IDocument document) {
	}
	
	@Override
	public void setInput(final ISourceUnit input) {
		fInput = input;
	}
	
	@Override
	public void reconcile(final IRegion partition) {
		reconcile();
	}
	
	@Override
	public void reconcile(final DirtyRegion dirtyRegion, final IRegion subRegion) {
		reconcile();
	}
	
	@Override
	public void setProgressMonitor(final IProgressMonitor monitor) {
		fMonitor = monitor;
	}
	
	
	protected void reconcile() {
		final ISourceUnit su = fInput;
		if (su == null || fMonitor.isCanceled()) {
			return;
		}
		if (su instanceof LtxRweaveEditorWorkingCopy) {
			((LtxRweaveEditorWorkingCopy) su).reconcileRModel(0, fMonitor);
		}
	}
	
}
