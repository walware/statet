/*=============================================================================#
 # Copyright (c) 2005-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.console.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.ts.ISystemRunnable;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;

import de.walware.statet.nico.core.runtime.ITrack;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolStreamProxy;
import de.walware.statet.nico.core.util.TrackWriter;
import de.walware.statet.nico.core.util.TrackingConfiguration;

import de.walware.statet.r.console.core.RWorkspace.Changes;
import de.walware.statet.r.nico.IRSrcref;


/**
 * Abstract superclass of controllers for R.
 * 
 * All implementations of {@link ToolController} for R should extends this class.
 */
public abstract class AbstractRController extends ToolController
		implements IRBasicAdapter {
	
	
	public static class RCommandRunnable extends ConsoleCommandRunnable {
		
		protected RCommandRunnable(final String text, final SubmitType type) {
			super(text, type);
		}
		
		@Override
		public boolean isRunnableIn(final ITool tool) {
			return (tool.isProvidingFeatureSet(RConsoleTool.R_BASIC_FEATURESET_ID));
		}
		
		@Override
		public void run(final IToolService service,
				final IProgressMonitor monitor) throws CoreException {
			final IRBasicAdapter r = (IRBasicAdapter) service;
			r.briefAboutToChange();
			try {
				super.run(r, monitor);
			}
			finally {
				if ((r.getPrompt().meta & IRBasicAdapter.META_PROMPT_INCOMPLETE_INPUT) == 0) {
					r.briefChanged(RWorkspace.REFRESH_AUTO);
				}
			}
		}
		
	}
	
	
	public static final String INIT_RGRAPHIC_FACTORY_HANDLER_ID = "r/initRGraphicFactory"; //$NON-NLS-1$
	
	protected final List<IToolRunnable> startupsRunnables= new ArrayList<>();
	
	protected String continuePromptText;
	protected String defaultPromptText;
	
	private List<TrackingConfiguration> trackingConfigurations;
	
	
	public AbstractRController(final RProcess process, final Map<String, Object> initData) {
		super(process, initData);
		process.registerFeatureSet(RConsoleTool.R_BASIC_FEATURESET_ID);
	}
	
	
	public void addStartupRunnable(final IToolRunnable runnable) {
		this.startupsRunnables.add(runnable);
	}
	
	@Override
	public RProcess getTool() {
		return (RProcess) super.getTool();
	}
	
	@Override
	public RWorkspace getWorkspaceData() {
		return (RWorkspace) super.getWorkspaceData();
	}
	
	@Override
	protected ISystemRunnable createCancelPostRunnable(final int options) {
		return new ControllerSystemRunnable(
				"common/cancel/post", "Reset prompt") { //$NON-NLS-1$
			
			@Override
			public void run(final IToolService s,
					final IProgressMonitor monitor) throws CoreException {
				if (!isTerminated()) {
					postCancelTask(options, monitor);
				}
			}
			
		};
	}
	
	protected void postCancelTask(final int options, final IProgressMonitor monitor) throws CoreException {
		final ToolStreamProxy streams = getStreams();
		final SubmitType submitType = getCurrentSubmitType();
		final String text = this.fCurrentPrompt.text + (
				((this.fCurrentPrompt.meta & IRBasicAdapter.META_PROMPT_INCOMPLETE_INPUT) != 0) ?
						"(Input cancelled)" : "(Command cancelled)") + 
						this.fLineSeparator;
		streams.getInfoStreamMonitor().append(text, submitType, this.fCurrentPrompt.meta);
	}
	
	public boolean supportsBusy() {
		return false;
	}
	
	public boolean isBusy() {
		return false;
	}
	
	
//-- Runnable Adapter
	@Override
	protected void initRunnableAdapterL() {
		super.initRunnableAdapterL();
		setDefaultPromptTextL("> "); //$NON-NLS-1$
		setContinuePromptText("+ "); //$NON-NLS-1$
	}
	
	protected void setTracksConfig(final List<TrackingConfiguration> config) {
		this.trackingConfigurations= config;
	}
	
	protected void initTracks(final String directory, final IProgressMonitor monitor, final List<IStatus> warnings)
			throws CoreException {
		if (this.trackingConfigurations != null) {
			final List<ITrack> tracks= new ArrayList<>(this.trackingConfigurations.size());
			for (final TrackingConfiguration trackingConfig : this.trackingConfigurations) {
				final TrackWriter tracker = new TrackWriter(this, trackingConfig);
				final IStatus status = tracker.init(monitor);
				if (status.getSeverity() == IStatus.OK) {
					tracks.add(tracker);
					addDisposable(tracker);
				}
				else {
					warnings.add(status);
				}
			}
			setTracks(tracks);
		}
	}
	
	
	@Override
	public IToolRunnable createCommandRunnable(final String command, final SubmitType type) {
		return new RCommandRunnable(command, type);
	}
	
	@Override
	public void setDefaultPromptTextL(String text) {
		if (text == null || text.equals(this.defaultPromptText)) {
			return;
		}
		text = text.intern();
		this.defaultPromptText = text;
		super.setDefaultPromptTextL(text);
	}
	
	public void setContinuePromptText(String text) {
		if (text == null || text.equals(this.continuePromptText)) {
			return;
		}
		text = text.intern();
		this.continuePromptText = text;
	}
	
	
	@Override
	protected void doRunSuspendedLoopL(final int o, final int level) {
		briefChanged(RWorkspace.REFRESH_AUTO);
		final Changes savedChanges= getWorkspaceData().saveChanges();
		try {
			super.doRunSuspendedLoopL(o, level);
		}
		finally {
			getWorkspaceData().restoreChanges(savedChanges);
		}
	}
	
	public Set<Long> getLazyEnvironments(final IProgressMonitor monitor) {
		return null;
	}
	
	public void submitFileCommandToConsole(final String[] lines, final ISourceUnit file,
			final IProgressMonitor monitor) throws CoreException {
		for (final String line : lines) {
			submitToConsole(line, monitor);
		}
	}
	
	public void submitCommandToConsole(final String[] lines, final IRSrcref srcref,
			final IProgressMonitor monitor) throws CoreException {
		for (final String line : lines) {
			submitToConsole(line, monitor);
		}
	}
	
	
	@Override
	protected void doQuitL(final IProgressMonitor monitor) throws CoreException {
		submitToConsole("q()", monitor); //$NON-NLS-1$
	}
	
	@Override
	public void quit(final IProgressMonitor monitor) throws CoreException {
		doQuitL(monitor);
	}
	
}
