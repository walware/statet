/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.ltk.LTK;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.internal.core.Messages;
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
	
	
	public IStatus buildIncremental(final RProject project, final IResourceDelta delta, final IProgressMonitor monitor) {
		fStatusCollector = new MultiStatus(RCore.PLUGIN_ID, 0, "R build status for "+project.getProject().getName(), null);
		final SubMonitor progress = SubMonitor.convert(monitor);
		try {
			delta.accept(this);
			
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			
			fModelManager.getIndex().update(project.getProject(), fRemovedRSU, fToUpdateRSU, fStatusCollector, progress);
		}
		catch (final CoreException e) {
			fStatusCollector.add(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.BUILD_ERROR,
					"An error occurred when indexing the project", e) );
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
		try {
			switch (delta.getKind()) {
			
			case IResourceDelta.ADDED:
			case IResourceDelta.CHANGED:
				if (resource instanceof IFile) {
					clearMarkers(resource);
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
						final IRSourceUnit unit = (IRSourceUnit) LTK.getSourceUnitManager().getSourceUnit(RModel.TYPE_ID, LTK.PERSISTENCE_CONTEXT, file, true, null);
						fToUpdateRSU.add(unit);
						return true;
					}
					if (IRSourceUnit.RD_CONTENT.equals(contentType.getId())) {
						doParseRd(file);
						return true;
					}
				}
				return true;
			
			case IResourceDelta.REMOVED:
				if ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
					final IResource movedTo = resource.getWorkspace().getRoot().findMember(delta.getMovedToPath());
					if (movedTo != null && !movedTo.getProject().hasNature(RProject.NATURE_ID)) {
						clearMarkers(movedTo);
					}
				}
				if (resource instanceof IFile) {
					fRemovedRSU.add(RResourceUnit.createResourceId(resource));
				}
				return true;
			}
			return true;
		}
		catch (final CoreException e) {
			fStatusCollector.add(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.BUILD_ERROR,
					NLS.bind("An error occurred when checking ''{0}''", resource.getFullPath().toString()), e));
			return false;
		}
	}
	
	public IStatus buildFull(final RProject project, final IProgressMonitor monitor) {
		fStatusCollector = new MultiStatus(RCore.PLUGIN_ID, 0, "R build status for "+project.getProject().getName(), null);
		final SubMonitor progress = SubMonitor.convert(monitor);
		try {
			project.getProject().accept(this);
			
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			
			fModelManager.getIndex().update(project.getProject(), null, fToUpdateRSU, fStatusCollector, progress);
		}
		catch (final CoreException e) {
			fStatusCollector.add(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.BUILD_ERROR,
					"An error occurred when indexing the project", e) );
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
		try {
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
					final IRSourceUnit unit = (IRSourceUnit) LTK.getSourceUnitManager().getSourceUnit(RModel.TYPE_ID, LTK.PERSISTENCE_CONTEXT, file, true, null);
					if (unit != null) {
						fToUpdateRSU.add(unit);
					}
					return true;
				}
				if (IRSourceUnit.RD_CONTENT.equals(contentType.getId())) {
					doParseRd(file);
					return true;
				}
			}
			return true;
		}
		catch (final CoreException e) {
			fStatusCollector.add(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.BUILD_ERROR,
					NLS.bind("An error occurred when checking ''{0}''", resource.getFullPath().toString()), e));
			return false;
		}
	}
	
	public void clean(final IProject project, final IProgressMonitor monitor) {
		clearMarkers(project);
		
		fModelManager.getIndex().clear(project);
	}
	
	
/*-- Rd --*/
	
	private TaskMarkerHandler fTaskMarkerHandler = new TaskMarkerHandler();
	
	protected void initRd(final RProject project) {
		fTaskMarkerHandler.init(project);
	}
	
	protected void doParseRd(final IFile file) throws CoreException {
		try {
			fTaskMarkerHandler.setup(file);
			new RdParser(readFile(file), fTaskMarkerHandler).check();
		}
		catch (final CoreException e) {
			fStatusCollector.add(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.BUILD_ERROR,
					NLS.bind("An error occurred when parsing Rd file ''{0}''", file.getFullPath().toString()), e));
		}
	}
	
	protected char[] readFile(final IFile file) throws CoreException {
		String charset = null;
		InputStream input = null;
		try {
			input = file.getContents();
			charset = file.getCharset();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset));
			
			final StringBuilder text = new StringBuilder(1000);
			final char[] readBuffer = new char[2048];
			int n;
			while ((n = reader.read(readBuffer)) > 0) {
				text.append(readBuffer, 0, n);
			}
			
			final char[] chars = new char[text.length()];
			text.getChars(0, chars.length, chars, 0);
			return chars;
		}
		catch (final UnsupportedEncodingException e) {
			throw new CoreException(new Status(
					IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.BUILD_ERROR,
					NLS.bind(Messages.Builder_error_UnsupportedEncoding_message, new String[] {
							charset, file.getName() } ), e));
		}
		catch (final IOException e) {
			throw new CoreException(new Status(
					IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.BUILD_ERROR,
					NLS.bind(Messages.Builder_error_IOReadingFile_message, file.getName() ), e));
		}
		finally {
			if (input != null) {
				try {
					input.close();
				} catch (final IOException ignore) {}
			}
		}
	}
	
}
