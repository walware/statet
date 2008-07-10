/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.AbstractDocument;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.IModelElement;
import de.walware.eclipsecommons.ltk.IProblemRequestor;
import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.ISourceUnitModelInfo;
import de.walware.eclipsecommons.ltk.SourceContent;
import de.walware.eclipsecommons.ltk.SourceDocumentRunnable;
import de.walware.eclipsecommons.ltk.WorkingBuffer;
import de.walware.eclipsecommons.ltk.WorkingContext;

import de.walware.statet.base.core.StatetCore;

import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * Generic source unit for R related files.
 */
public abstract class RResourceUnit implements ISourceUnit {
	
	
	public static String createResourceId(final IResource file) {
		if (file != null) {
			final IPath path = file.getFullPath();
			if (path != null) {
				return "wr:"+path.toPortableString(); //$NON-NLS-1$
			}
		}
		return null;
	}
	
	public static RResourceUnit createTempUnit(final IResource file, final String typeId) {
		return new RResourceUnit(file) {
			@Override
			public String getTypeId() {
				return typeId;
			}
		};
	}
	
	
	private IResource fFile;
	private String fId;
	private String fName;
	protected int fCounter;
	
	
	public RResourceUnit(final IResource file) {
		fFile = file;
		fName = (fFile != null) ? fFile.getName() : "<no file info>"; //$NON-NLS-1$
		fId = createResourceId(fFile);
		if (fId == null) {
			fId = "xx:"+fName; //$NON-NLS-1$
		}
	}
	
	
	protected void init() {
	}
	
	protected void dispose() {
	}
	
	public abstract String getTypeId();
	
	public String getId() {
		return fId;
	}
	
	
	public WorkingContext getWorkingContext() {
		return StatetCore.PERSISTENCE_CONTEXT;
	}
	
	public synchronized final void connect() {
		fCounter++;
		if (fCounter == 1) {
			init();
		}
	}
	
	public synchronized final void disconnect() {
		fCounter--;
		if (fCounter == 0) {
			dispose();
		}
	}
	
	public ISourceUnit getSourceUnit() {
		return this;
	}
	
	public ISourceUnit getUnderlyingUnit() {
		return null;
	}
	
	
	public IModelElement getParent() {
		return null; // directory
	}
	
	public boolean hasChildren(final Object filter) {
		return true;
	}
	
	public IModelElement[] getChildren(final Object filter) {
		return new IModelElement[0];
	}
	
	
	public String getElementName() {
		return fName;
	}
	
	public IResource getResource() {
		return fFile;
	}
	
	public IPath getPath() {
		return fFile.getFullPath();
	}
	
	public AbstractDocument getDocument() {
		return null;
	}
	
	public SourceContent getContent() {
		return new WorkingBuffer(this).getContent();
	}
	
	public void syncExec(final SourceDocumentRunnable runnable) throws InvocationTargetException {
		throw new UnsupportedOperationException();
	}
	
	public RProject getRProject() {
		if (fFile != null) {
			final IProject proj =  fFile.getProject();
			try {
				if (proj.hasNature(RProject.NATURE_ID)) {
					return (RProject) proj.getNature(RProject.NATURE_ID);
				}
			} catch (final CoreException e) {
				RCorePlugin.log(new Status(Status.ERROR, RCore.PLUGIN_ID, -1, "An error occurred while access R project nature.", e));
			}
		}
		return null;
	}
	
	public Object getAdapter(final Class required) {
		if (required.equals(IRCoreAccess.class)) {
			final RProject rproj = getRProject();
			if (rproj != null) {
				return rproj;
			}
			return RCore.getWorkbenchAccess();
		}
		return null;
	}
	
	
	protected final void register() {
		if (getTypeId().equals("r")) { //$NON-NLS-1$
			RCorePlugin.getDefault().getRModelManager().registerWorkingCopy((IRSourceUnit) this);
		}
		else {
			RCorePlugin.getDefault().getRModelManager().registerWorksheetCopy(this);
		}
	}
	
	protected final void unregister() {
		if (getTypeId().equals("r")) { //$NON-NLS-1$
			RCorePlugin.getDefault().getRModelManager().removeWorkingCopy((IRSourceUnit) this);
		}
		else {
			RCorePlugin.getDefault().getRModelManager().removeWorksheetCopy(this);
		}
	}
	
	public AstInfo<?> getAstInfo(final String type, final boolean ensureSync, final IProgressMonitor monitor) {
		return null;
	}
	
	public ISourceUnitModelInfo getModelInfo(final String type, final int syncLevel, final IProgressMonitor monitor) {
		return null;
	}
	
	public IProblemRequestor getProblemRequestor() {
		return null;
	}
	
}
