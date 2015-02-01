/*=============================================================================#
 # Copyright (c) 2013-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.console.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolService;

import de.walware.rj.data.RReference;

import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.tool.AbstractStatetRRunnable;
import de.walware.statet.r.nico.AbstractRDbgController;


public class LoadReferenceRunnable extends AbstractStatetRRunnable implements ISystemRunnable {
	
	
	public static RProcess findRProcess(ICombinedRElement element) {
		while (element != null) {
			if (element instanceof ICombinedREnvironment) {
				return ((ICombinedREnvironment) element).getSource();
			}
			element = element.getModelParent();
		}
		return null;
	}
	
	
	private final RReference reference;
	private final RProcess process;
	private final int stamp;
	
	private ICombinedRElement resolvedElement;
	
	private boolean cancel;
	private int state;
	
	private Runnable finishRunnable;
	
	
	public LoadReferenceRunnable(final RReference reference, final RProcess process,
			final int stamp, final String cause) {
		super("r/workspace/loadElements", //$NON-NLS-1$
				NLS.bind("Load elements of {0} (requested for {1})", 
						((ICombinedRElement) reference).getElementName().getDisplayName(),
						cause ));
		
		this.process = process;
		this.reference = reference;
		this.stamp = stamp;
	}
	
	
	public void cancel() {
		this.cancel = true;
	}
	
	public ICombinedRElement getResolvedElement() {
		return this.resolvedElement;
	}
	
	public boolean isFinished() {
		return ((this.state & MASK_EVENT_GROUP) == FINISHING_EVENT_GROUP);
	}
	
	public void setFinishRunnable(final Runnable runnable) {
		this.finishRunnable = runnable;
	}
	
	
	@Override
	public boolean isRunnableIn(final ITool tool) {
		return (tool == this.process);
	}
	
	@Override
	public boolean changed(final int event, final ITool tool) {
		Runnable runnable = null;
		switch (event) {
		case REMOVING_FROM:
			if (this.cancel) {
				synchronized (LoadReferenceRunnable.this) {
					this.state = event;
					LoadReferenceRunnable.this.notifyAll();
					return true;
				}
			}
			return false;
		case MOVING_FROM:
			return false;
		case BEING_ABANDONED:
		case FINISHING_OK:
		case FINISHING_ERROR:
		case FINISHING_CANCEL:
			synchronized (LoadReferenceRunnable.this) {
				this.state = event;
				runnable = this.finishRunnable;
				LoadReferenceRunnable.this.notifyAll();
			}
			break;
		default:
			break;
		}
		
		if (runnable != null) {
			runnable.run();
		}
		
		return true;
	}
	
	@Override
	public void run(final IToolService service,
			final IProgressMonitor monitor) throws CoreException {
		final AbstractRDbgController r = (AbstractRDbgController) service;
		if (this.stamp != 0 && this.stamp != r.getCounter()) {
			return;
		}
		final RWorkspace workspace = r.getWorkspaceData();
		this.resolvedElement = workspace.resolve(this.reference, monitor);
	}
	
}
