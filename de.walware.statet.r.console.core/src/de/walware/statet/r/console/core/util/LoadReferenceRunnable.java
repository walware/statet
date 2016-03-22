/*=============================================================================#
 # Copyright (c) 2013-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.console.core.util;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

import de.walware.jcommons.collections.ImCollections;
import de.walware.jcommons.collections.ImSet;

import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;

import de.walware.rj.data.RReference;
import de.walware.rj.eclient.IRToolService;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.console.core.RWorkspace.ICombinedREnvironment;
import de.walware.statet.r.core.data.ICombinedRElement;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.tool.AbstractStatetRRunnable;
import de.walware.statet.r.nico.ICombinedRDataAdapter;


public class LoadReferenceRunnable extends AbstractStatetRRunnable implements ISystemRunnable {
	
	
	public static RProcess findRProcess(ICombinedRElement element) {
		while (element != null) {
			if (element instanceof ICombinedREnvironment) {
				return ((ICombinedREnvironment) element).getSource();
			}
			element= element.getModelParent();
		}
		return null;
	}
	
	
	private static final ImSet<String> LOAD_PKG_EXCLUDE_LIST= ImCollections.newSet(
			System.getProperty("de.walware.statet.r.console.namespaces.load.exclude", "") //$NON-NLS-1$ //$NON-NLS-2$
					.split(",") ); //$NON-NLS-1$
	
	public static boolean isAccessAllowed(final RElementName name, final RWorkspace rWorkspace) {
		final Set<String> excludePkgs= LOAD_PKG_EXCLUDE_LIST;
		if (excludePkgs.isEmpty()) {
			return true;
		}
		
		final String pkgName;
		if (RElementName.isPackageFacetScopeType(name.getType())) {
			pkgName= name.getSegmentName();
		}
		else if (name.getScope() != null
				&& RElementName.isPackageFacetScopeType(name.getScope().getType()) ) {
			pkgName= name.getScope().getSegmentName();
		}
		else {
			return true;
		}
		
		return (!(excludePkgs.contains("*") || excludePkgs.contains(pkgName)) //$NON-NLS-1$
				|| rWorkspace.isNamespaceLoaded(pkgName) );
	}
	
	
	private final RReference reference;
	private final RElementName name;
	
	private final RProcess process;
	
	private final int stamp;
	private int loadOptions;
	
	private ICombinedRElement resolvedElement;
	
	private boolean cancel;
	private int state;
	
	private Runnable finishRunnable;
	
	
	public LoadReferenceRunnable(final RReference reference, final RProcess tool,
			final int stamp, final String cause) {
		super("r/workspace/loadElements", //$NON-NLS-1$
				NLS.bind("Load elements of {0} (requested for {1})", 
						((ICombinedRElement) reference).getElementName().getDisplayName(),
						cause ));
		
		this.reference= reference;
		this.name= null;
		
		this.process= tool;
		this.stamp= stamp;
	}
	
	public LoadReferenceRunnable(final RElementName name, final RProcess tool,
			final int stamp, final String cause) {
		super("r/workspace/loadElements", //$NON-NLS-1$
				NLS.bind("Load elements of {0} (requested for {1})", 
						name.getDisplayName(),
						cause ));
		
		this.reference= null;
		this.name= name;
		
		this.process= tool;
		this.stamp= stamp;
	}
	
	
	public final RProcess getTool() {
		return this.process;
	}
	
	public int getLoadOptions() {
		return this.loadOptions;
	}
	
	public void setLoadOptions(final int options) {
		this.loadOptions= options;
	}
	
	public int getRequiredStamp() {
		return this.stamp;
	}
	
	
	public void cancel() {
		this.cancel= true;
	}
	
	public ICombinedRElement getResolvedElement() {
		return this.resolvedElement;
	}
	
	
	public boolean isStarted() {
		return (this.state == STARTING);
	}
	
	public boolean isFinished() {
		return ((this.state & MASK_EVENT_GROUP) == FINISHING_EVENT_GROUP);
	}
	
	public void setFinishRunnable(final Runnable runnable) {
		this.finishRunnable= runnable;
	}
	
	
	@Override
	public boolean isRunnableIn(final ITool tool) {
		return (tool == this.process);
	}
	
	@Override
	public boolean changed(final int event, final ITool tool) {
		Runnable runnable= null;
		switch (event) {
		case REMOVING_FROM:
			if (this.cancel) {
				synchronized (this) {
					this.state= event;
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
			synchronized (this) {
				this.state= event;
				runnable= this.finishRunnable;
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
	public void run(final IRToolService service,
			final IProgressMonitor monitor) throws CoreException {
		final ICombinedRDataAdapter r= (ICombinedRDataAdapter) service;
		if (this.stamp != 0 && this.stamp != r.getChangeStamp()) {
			return;
		}
		final int loadOptions;
		synchronized (this) {
			this.state= STARTING;
			loadOptions= this.loadOptions;
		}
		final RWorkspace workspace= r.getWorkspaceData();
		if (this.reference != null) {
			this.resolvedElement= workspace.resolve(this.reference, RWorkspace.RESOLVE_UPTODATE,
					loadOptions, monitor );
		}
		else {
			this.resolvedElement= workspace.resolve(this.name, RWorkspace.RESOLVE_UPTODATE,
					loadOptions, monitor );
		}
	}
	
}
