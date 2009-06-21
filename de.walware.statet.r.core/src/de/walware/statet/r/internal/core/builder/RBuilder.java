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

package de.walware.statet.r.internal.core.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.ltk.ECommonsLTK;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.internal.core.RCorePlugin;
import de.walware.statet.r.internal.core.sourcemodel.RModelManager;


public class RBuilder implements IResourceDeltaVisitor, IResourceVisitor {
	
	
	public static void clearMarkers(final IResource resource) {
		try {
			resource.deleteMarkers("de.walware.statet.r.markers.Tasks", false, IResource.DEPTH_INFINITE); //$NON-NLS-1$
		}
		catch (final CoreException e) {
			RCorePlugin.logError(ICommonStatusConstants.BUILD_ERROR, "R Builder: Failed to remove old markers.", e);
		}
	}
	
	
	private final List<String> fRemovedRSU = new ArrayList<String>();
	private final List<IRSourceUnit> fToUpdateRSU = new ArrayList<IRSourceUnit>();
	
	private final RModelManager fModelManager;
	
	private MultiStatus fStatusCollector;
	
	
	public RBuilder() {
		fModelManager = RCorePlugin.getDefault().getRModelManager();
	}
	
	
	public IStatus buildIncremental(final IProject project, final IResourceDelta delta, final IProgressMonitor monitor) {
		fStatusCollector = new MultiStatus(RCore.PLUGIN_ID, 0, "R build status for "+project.getName(), null);
		final SubMonitor progress = SubMonitor.convert(monitor);
		try {
			delta.accept(this);
			
			fModelManager.getIndex().update(project, fRemovedRSU, fToUpdateRSU, fStatusCollector, progress);
		}
		catch (final CoreException e) {
			RCorePlugin.logError(ICommonStatusConstants.BUILD_ERROR, "R Builder: Failed to build project " + project.getName(), e);
		}
		finally {
			for (final IRSourceUnit su : fToUpdateRSU) {
				if (su != null) {
					su.disconnect(progress);
				}
			}
			fRemovedRSU.clear();
			fToUpdateRSU.clear();
		}
		return fStatusCollector;
	}
	
	public boolean visit(final IResourceDelta delta) throws CoreException {
		final IResource resource = delta.getResource();
		switch (delta.getKind()) {
		
		case IResourceDelta.ADDED:
		case IResourceDelta.CHANGED:
			if (resource instanceof IFile) {
				clearMarkers(resource);
				final IFile file = (IFile) resource;
				final IContentDescription contentDescription = file.getContentDescription();
				if (contentDescription == null) {
					break;
				}
				final IContentType contentType = contentDescription.getContentType();
				if (contentType == null) {
					break;
				}
				if (IRSourceUnit.R_CONTENT.equals(contentType.getId())) {
					final IRSourceUnit unit = (IRSourceUnit) ECommonsLTK.PERSISTENCE_CONTEXT.getUnit(file, RModel.TYPE_ID, true, null);
					fToUpdateRSU.add(unit);
				}
			}
			break;
		
		case IResourceDelta.REMOVED:
			if ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
				clearMarkers(resource);
			}
			if (resource instanceof IFile) {
				fRemovedRSU.add(RResourceUnit.createResourceId(resource));
			}
			break;
		}
		return true;
	}
	
	public IStatus buildFull(final IProject project, final IProgressMonitor monitor) {
		fStatusCollector = new MultiStatus(RCore.PLUGIN_ID, 0, "R build status for "+project.getName(), null);
		final SubMonitor progress = SubMonitor.convert(monitor);
		try {
			project.accept(this);
			
			fModelManager.getIndex().update(project, null, fToUpdateRSU, fStatusCollector, progress);
		}
		catch (final CoreException e) {
			RCorePlugin.logError(ICommonStatusConstants.BUILD_ERROR, "R Builder: Failed to build project " + project.getName(), e);
		}
		finally {
			for (final IRSourceUnit su : fToUpdateRSU) {
				if (su != null) {
					su.disconnect(progress);
				}
			}
			fRemovedRSU.clear();
			fToUpdateRSU.clear();
		}
		return fStatusCollector;
	}
	
	public boolean visit(final IResource resource) throws CoreException {
		clearMarkers(resource);
		if (resource instanceof IFile) {
			final IFile file = (IFile) resource;
			final IContentDescription contentDescription = file.getContentDescription();
			if (contentDescription == null) {
				return true;
			}
			final IContentType contentType = contentDescription.getContentType();
			if (contentType == null) {
				return true;
			}
			if (IRSourceUnit.R_CONTENT.equals(contentType.getId())) {
				final IRSourceUnit unit = (IRSourceUnit) ECommonsLTK.PERSISTENCE_CONTEXT.getUnit(file, RModel.TYPE_ID, true, null);
				if (unit != null) {
					fToUpdateRSU.add(unit);
				}
			}
		}
		return true;
	}
	
	public void clean(final IProject project, final IProgressMonitor monitor) {
		clearMarkers(project);
		
		fModelManager.getIndex().clear(project);
	}
	
}
