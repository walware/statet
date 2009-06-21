/*******************************************************************************
 * Copyright (c) 2005-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ICommonStatusConstants;

import de.walware.statet.base.core.preferences.StatetCorePreferenceNodes;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.internal.core.builder.RBuilder;
import de.walware.statet.r.internal.core.builder.RdParser;
import de.walware.statet.r.internal.core.builder.TaskMarkerHandler;


public class RSupportBuilder extends IncrementalProjectBuilder {
	
	
	public static final String ID = "de.walware.statet.r.builders.RSupport";  //$NON-NLS-1$
	
	
	static class ExceptionCollector {
		
		private ArrayList<IStatus> fExceptionList = new ArrayList<IStatus>(20);
		
		public void reset() {
			fExceptionList.clear();
			if (fExceptionList.size() > 20)
				fExceptionList.trimToSize();
		}
		
		private void add(final IStatus e) {
			fExceptionList.add(e);
		}
		
		void checkException() throws CoreException {
			if (fExceptionList != null && fExceptionList.size() > 0) {
				final IStatus[] allStatus = fExceptionList.toArray(new IStatus[fExceptionList.size()]);
				
				final IStatus status = new MultiStatus(
						RCore.PLUGIN_ID, ICommonStatusConstants.BUILD_ERROR, allStatus,
						NLS.bind(Messages.Builder_error_MultipleErrors_message, Integer.toString(allStatus.length)),
						null);
				
				throw new CoreException(status);
			}
		}
	}
	
	class RResourceVisitor implements IResourceVisitor {
		
		public boolean visit(final IResource resource) {
			if (resource instanceof IFile) {
				check((IFile) resource);
			}
			//return true to continue visiting children.
			return true;
		}
	}
	
	class RResourceDeltaVisitor implements IResourceDeltaVisitor {
		
		public boolean visit(final IResourceDelta delta) throws CoreException {
			final IResource resource = delta.getResource();
			switch (delta.getKind()) {
			
			case IResourceDelta.ADDED:
			case IResourceDelta.CHANGED:
				// handle added resource
				if (resource instanceof IFile) {
					check((IFile) resource);
				}
				break;
			
			case IResourceDelta.REMOVED:
				// handle removed resource
				// markers are automatically removed
				break;
			}
			//return true to continue visiting children.
			return true;
		}
	}
	
	private class SettingsListener implements IEclipsePreferences.IPreferenceChangeListener {
		
		public void preferenceChange(final PreferenceChangeEvent event) {
			fInitialized = false;
		}
	}
	
	
	private RProject fRProject;
	private SettingsListener fSettingsListener;
	private ExceptionCollector fExceptions;
	private TaskMarkerHandler fResourceMarkers;
	
	private boolean fStartupSuccessfull = false;
	private boolean fInitialized = false;
	
	private RBuilder fRBuilder;
	
	
	public RSupportBuilder() {
		super();
	}
	
	@Override
	public void setInitializationData(final IConfigurationElement config, final String propertyName, final Object data) throws CoreException {
		super.setInitializationData(config, propertyName, data);
	}
	
	@Override
	protected void startupOnInitialize() {
		fStartupSuccessfull = false;
		super.startupOnInitialize();
		
		// Listen to preference changes
		try {
			fSettingsListener = new SettingsListener();
			
			fRProject = (RProject) getProject().getNature(RProject.NATURE_ID);
			if (fRProject == null) {
				throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "R Project Nature is missing", null)); //$NON-NLS-1$
			}
			fRProject.getStatetProject().addPreferenceNodeListener(
					StatetCorePreferenceNodes.CAT_MANAGMENT_QUALIFIER,
					fSettingsListener);
			fRBuilder = new RBuilder();
			fStartupSuccessfull = true;
		}
		catch (final CoreException e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.BUILD_ERROR,
					NLS.bind("Error occured while initizalizing the builder (''{0}'').", ID), e)); //$NON-NLS-1$
		}
	}
	
	private void init() throws CoreException {
		if (!fStartupSuccessfull)
			throw new CoreException(new Status(
					IStatus.ERROR, RCore.PLUGIN_ID,	ICommonStatusConstants.BUILD_ERROR,
					Messages.Builder_error_OnStartup_message, null));
		
		fExceptions = new ExceptionCollector();
		if (fResourceMarkers == null) {
			fResourceMarkers = new TaskMarkerHandler();
		}
		fResourceMarkers.init(fRProject);
		
		fInitialized = true;
	}
	
	@Override
	protected IProject[] build(final int kind, final Map args, final IProgressMonitor monitor)
			throws CoreException {
		if (!fInitialized)
			init();
		
		if (kind == IncrementalProjectBuilder.FULL_BUILD) {
			doFullBuild(monitor);
		}
		else {
			final IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				doFullBuild(monitor);
			}
			else {
				doIncrementalBuild(delta, monitor);
			}
		}
		monitor.done();
		return null;
	}
	
	protected void doFullBuild(final IProgressMonitor monitor) throws CoreException {
		fExceptions.reset();
		
		fRBuilder.buildFull(getProject(), monitor);
		
		getProject().accept(new RResourceVisitor());
		
		fExceptions.checkException();
	}
	
	protected void doIncrementalBuild(final IResourceDelta delta, final IProgressMonitor monitor)
			throws CoreException {
		fExceptions.reset();
		
		final IStatus status = fRBuilder.buildIncremental(getProject(), delta, monitor);
		if (!status.isOK()) {
			fExceptions.add(status);
		}
		
		delta.accept(new RResourceDeltaVisitor());
		
		fExceptions.checkException();
	}
	
	@Override
	protected void clean(final IProgressMonitor monitor) throws CoreException {
//		if (!fInitialized)
		init();
		
		fRBuilder.clean(getProject(), monitor);
	}
	
	protected void check(final IFile file) {
		try {
			final IContentDescription description = file.getContentDescription();
			if (description == null) {
				return;
			}
			final IContentType type = description.getContentType();
			if (type == null) {
				return;
			}
			if (IRSourceUnit.RD_CONTENT.equals(type.getId())) {
				doParseRd(file);
			}
		}
		catch (final CoreException e) {
			fExceptions.add(new Status(IStatus.ERROR, RCore.PLUGIN_ID, ICommonStatusConstants.BUILD_ERROR,
					NLS.bind("An error occurred when checking ''{0}''", file.getFullPath().toString()), e));
		}
	}
	
/* **/
	
	
	protected void doParseRd(final IFile file) throws CoreException {
		fResourceMarkers.setup(file);
		new RdParser(readFile(file), fResourceMarkers).check();
	}
	
	protected char[] readFile(final IFile file) throws CoreException {
		String charset = null;
		try {
			final InputStream input = file.getContents();
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
	}
	
}
