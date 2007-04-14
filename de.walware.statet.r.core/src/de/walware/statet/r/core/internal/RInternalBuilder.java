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

package de.walware.statet.r.core.internal;

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

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.base.core.preferences.StatetCorePreferenceNodes;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.core.internal.builder.MarkerHandler;
import de.walware.statet.r.core.internal.builder.RParser;
import de.walware.statet.r.core.internal.builder.RdParser;


public class RInternalBuilder extends IncrementalProjectBuilder {

	
	public static final String ID = "de.walware.statet.r.core.RInternalBuilder";
	
	
	static class ExceptionCollector {
		
		private ArrayList<CoreException> fExceptionList = new ArrayList<CoreException>(20);
		
		public void reset() {
			
			fExceptionList.clear();
			if (fExceptionList.size() > 20)
				fExceptionList.trimToSize();
		}

		private void add(CoreException e) {
			
			fExceptionList.add(e);
		}

		void checkException() throws CoreException {
			
			if (fExceptionList != null && fExceptionList.size() > 0) {
				IStatus[] allStatus = new IStatus[fExceptionList.size()];
				for (int i = 0; i < allStatus.length; i++) {
					allStatus[i] = fExceptionList.get(i).getStatus();
				}
				
				IStatus status = new MultiStatus(
						RCore.PLUGIN_ID, StatetCore.STATUSCODE_BUILD_ERROR,	allStatus,
						NLS.bind(Messages.Builder_error_MultipleErrors_message, Integer.toString(allStatus.length)),
						null);
				
				throw new CoreException(status);
			}
		}
	}

	class RResourceVisitor implements IResourceVisitor {
		
		public boolean visit(IResource resource) {
			try {
				check(resource);
			} catch (CoreException e) {
				fExceptions.add(e);
			}
			//return true to continue visiting children.
			return true;
		}
	}

	class RResourceDeltaVisitor implements IResourceDeltaVisitor {
		
		public boolean visit(IResourceDelta delta) throws CoreException {
			try {
				IResource resource = delta.getResource();
				switch (delta.getKind()) {
				
				case IResourceDelta.ADDED:
				case IResourceDelta.CHANGED:
					// handle added resource
					check(resource);
					break;

				case IResourceDelta.REMOVED:
					// handle removed resource
					// markers are automatically removed
					break;
				}
			} catch (CoreException e) {
				fExceptions.add(e);
			}
			//return true to continue visiting children.
			return true;
		}
	}
	
	private class SettingsListener implements IEclipsePreferences.IPreferenceChangeListener {
		
		public void preferenceChange(PreferenceChangeEvent event) {
			
			fInitialized = false;
		}
	}
	
	
	private RProject fRProject;
	private SettingsListener fSettingsListener;
	private ExceptionCollector fExceptions;
	private MarkerHandler fResourceMarkers;
	
	private boolean fStartupSuccessfull = false;
	private boolean fInitialized = false;
	
	
	public RInternalBuilder() {

		super();
	}
	
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {

		super.setInitializationData(config, propertyName, data);
	}
	
	@Override
	protected void startupOnInitialize() {

		super.startupOnInitialize();

		// Listen to preference changes
		try {
			fSettingsListener = new SettingsListener();
			
			fRProject = (RProject) getProject().getNature(RProject.ID);
			IEclipsePreferences[] nodes = fRProject.getStatetProject().getPreferenceNodes(
					StatetCorePreferenceNodes.CAT_MANAGMENT_QUALIFIER);
			
			for (IEclipsePreferences node : nodes) {
				node.addPreferenceChangeListener(fSettingsListener);
			}
			fStartupSuccessfull = true;
		} catch (CoreException e) {
			RCorePlugin.log(new Status(IStatus.ERROR, RCore.PLUGIN_ID, StatetCore.STATUSCODE_BUILD_ERROR,
					"Error occured while initizalizing the builder ("+ID+").", e));
		}
	}
	
	private void init() throws CoreException {
		
		if (!fStartupSuccessfull)
			throw new CoreException(new Status(
					IStatus.ERROR, RCore.PLUGIN_ID,	StatetCore.STATUSCODE_BUILD_ERROR, 
					Messages.Builder_error_OnStartup_message, null));
		
		fExceptions = new ExceptionCollector();
		fResourceMarkers = new MarkerHandler(fRProject);
		
		fInitialized = true;
	}
	
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		
		if (!fInitialized)
			init();

		if (kind == IncrementalProjectBuilder.FULL_BUILD) {
			doFullBuild(monitor);
		} 
		else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				doFullBuild(monitor);
			} else {
				doIncrementalBuild(delta, monitor);
			}
		}
		monitor.done();
		return null;
	}

	protected void doFullBuild(IProgressMonitor monitor) 
			throws CoreException {
		
		fExceptions.reset();

		RResourceVisitor visitor = new RResourceVisitor();
		getProject().accept(visitor);
		
		fExceptions.checkException();
	}
	
	protected void doIncrementalBuild(IResourceDelta delta, IProgressMonitor monitor) 
			throws CoreException {
		
		fExceptions.reset();

		delta.accept(new RResourceDeltaVisitor());
		
		fExceptions.checkException();
	}
	
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {

//		if (!fInitialized)
		init();

		fResourceMarkers.setup(getProject());
		fResourceMarkers.clean();
	}
	
	protected void check(IResource resource) throws CoreException {
		
		fResourceMarkers.setup(resource);
		
		if (resource instanceof IFile) {
			
			IFile file = (IFile) resource;
			fResourceMarkers.clean();
				
			IContentDescription description = file.getContentDescription();
			if (description == null)
				return;
			IContentType type = description.getContentType();
			if (type == null)
				return;
			
			if (RResourceUnit.R_CONTENT.equals(type.getId()))
				doParseR(file);
			else 
			if (RResourceUnit.RD_CONTENT.equals(type.getId()))
				doParseRd(file);
		}
	}
	
/* **/
	
	
	
	
	protected void doParseR(IFile file) throws CoreException {
		
		new RParser(readFile(file), fResourceMarkers).check();
	}
	
	protected void doParseRd(IFile file) throws CoreException {
		
		new RdParser(readFile(file), fResourceMarkers).check();
	}
	
	protected char[] readFile(IFile file) throws CoreException {
		
		String charset = null;
		try {
			InputStream input = file.getContents();
			charset = file.getCharset();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset));
			
			StringBuilder text = new StringBuilder(1000);
			char[] readBuffer = new char[2048];
			int n;
			while ((n = reader.read(readBuffer)) > 0) {
				text.append(readBuffer, 0, n);
			}
			
			char[] chars = new char[text.length()];
			text.getChars(0, chars.length, chars, 0);
			return chars;
			
		} catch (UnsupportedEncodingException e) {
			throw new CoreException(new Status(
					IStatus.ERROR, RCore.PLUGIN_ID, StatetCore.STATUSCODE_BUILD_ERROR,
					NLS.bind(Messages.Builder_error_UnsupportedEncoding_message, new String[] {
							charset, file.getName() } ), e));
		} catch (IOException e) {
			throw new CoreException(new Status(
					IStatus.ERROR, RCore.PLUGIN_ID, StatetCore.STATUSCODE_BUILD_ERROR,
					NLS.bind(Messages.Builder_error_IOReadingFile_message, file.getName() ), e));
		}
	}
}
