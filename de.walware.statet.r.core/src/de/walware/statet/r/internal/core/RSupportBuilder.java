/*=============================================================================#
 # Copyright (c) 2005-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.ICommonStatusConstants;

import de.walware.statet.base.core.preferences.StatetCorePreferenceNodes;

import de.walware.statet.r.core.IRProject;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProjects;
import de.walware.statet.r.internal.core.builder.RBuilder;


public class RSupportBuilder extends IncrementalProjectBuilder {
	
	
	public static final String ID = "de.walware.statet.r.builders.RSupport";  //$NON-NLS-1$
	
	
	static class ExceptionCollector {
		
		private final ArrayList<IStatus> fExceptionList = new ArrayList<IStatus>(20);
		
		public void reset() {
			fExceptionList.clear();
			if (fExceptionList.size() > 20) {
				fExceptionList.trimToSize();
			}
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
	
	private class SettingsListener implements IEclipsePreferences.IPreferenceChangeListener {
		
		@Override
		public void preferenceChange(final PreferenceChangeEvent event) {
			fInitialized = false;
		}
	}
	
	
	private IRProject fRProject;
	private SettingsListener fSettingsListener;
	private ExceptionCollector fExceptions;
	
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
			
			fRProject = (IRProject) getProject().getNature(RProjects.R_NATURE_ID);
			if (fRProject == null) {
				throw new CoreException(new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1, "R Project Nature is missing", null)); //$NON-NLS-1$
			}
			fRProject.addPreferenceNodeListener(
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
		if (!fStartupSuccessfull) {
			throw new CoreException(new Status(
					IStatus.ERROR, RCore.PLUGIN_ID,	ICommonStatusConstants.BUILD_ERROR,
					Messages.Builder_error_OnStartup_message, null));
		}
		
		fExceptions = new ExceptionCollector();
		
		fInitialized = true;
	}
	
	@Override
	protected IProject[] build(final int kind, final Map args, final IProgressMonitor monitor)
			throws CoreException {
		if (!fInitialized) {
			init();
		}
		
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
		
		final IStatus status = fRBuilder.buildFull(fRProject, monitor);
		if (!status.isOK()) {
			fExceptions.add(status);
		}
		
		fExceptions.checkException();
	}
	
	protected void doIncrementalBuild(final IResourceDelta delta, final IProgressMonitor monitor)
			throws CoreException {
		fExceptions.reset();
		
		final IStatus status = fRBuilder.buildIncremental(fRProject, delta, monitor);
		if (!status.isOK()) {
			fExceptions.add(status);
		}
		
		fExceptions.checkException();
	}
	
	@Override
	protected void clean(final IProgressMonitor monitor) throws CoreException {
//		if (!fInitialized)
		init();
		
		fRBuilder.clean(getProject(), monitor);
	}
	
}
