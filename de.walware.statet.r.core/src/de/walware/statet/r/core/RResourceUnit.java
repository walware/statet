/*******************************************************************************
 * Copyright (c) 2005 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.AbstractDocument;

import de.walware.eclipsecommons.ltk.IModelElement;
import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.SourceContent;
import de.walware.eclipsecommons.ltk.WorkingContext;
import de.walware.eclipsecommons.preferences.IPreferenceAccess;

import de.walware.statet.base.core.StatetProject;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * 
 */
public class RResourceUnit implements ISourceUnit {

	
	public static String createResourceId(IResource file) {
		if (file != null) {
			IPath path = file.getFullPath();
			if (path != null) {
				return "wr:"+path.toPortableString();
			}
		}
		return null;
	}
	
	private IResource fFile;
	private String fId;
	private String fName;
	protected int fCounter;
	

	public RResourceUnit(IResource file) {
		fFile = file;
		fName = (fFile != null) ? fFile.getName() : "<no file info>";
		fId = createResourceId(fFile);
		if (fId == null) {
			fId = "xx:"+fName;
		}
	}
		
	protected void init() {
	}

	protected void dispose() {
	}
	
	public String getId() {
		return fId;
	}

	
	public WorkingContext getWorkingContext() {
		return RCore.PERSISTENCE_CONTEXT;
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

	public ISourceUnit getWorkingCopy(WorkingContext context, boolean create) {
		throw new UnsupportedOperationException();
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
	
	public boolean hasChildren(Object filter) {
		return true;
	}

	public IModelElement[] getChildren(Object filter) {
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
		return RCore.PERSISTENCE_CONTEXT.createWorkingBuffer(this).getContent();
	}
	
	public RProject getRProject() {
		if (fFile != null) {
			IProject proj =  fFile.getProject();
			try {
				if (proj.hasNature(RProject.NATURE_ID)) {
					return (RProject) proj.getNature(RProject.NATURE_ID);
				}
			} catch (CoreException e) {
				RCorePlugin.log(new Status(Status.ERROR, RCore.PLUGIN_ID, -1, "An error occurred while access R project nature.", e));
			}
		}
		return null;
	}
	
	public StatetProject getStatetProject() {
		RProject rproj = getRProject();
		if (rproj != null) {
			try {
				return rproj.getStatetProject();
			} catch (CoreException e) {
				RCorePlugin.log(new Status(Status.ERROR, RCore.PLUGIN_ID, -1, "An error occurred while access Statet project nature.", e));
			}
		}
		return null;
	}
	
	public IPreferenceAccess getPrefs() {
		RProject rproj = getRProject();
		if (rproj != null) {
			return rproj.getPrefs();
		}
		return RCore.getWorkbenchAccess().getPrefs();
	}

	public RCodeStyleSettings getRCodeStyle() {
		RProject rproj = getRProject();
		if (rproj != null) {
			return rproj.getRCodeStyle();
		}
		return RCore.getWorkbenchAccess().getRCodeStyle();
	}
	
}
