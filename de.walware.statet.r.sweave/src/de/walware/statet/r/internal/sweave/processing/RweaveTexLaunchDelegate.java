/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.sweave.processing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.progress.IProgressConstants;

import de.walware.ecommons.ICommonStatusConstants;
import de.walware.ecommons.debug.core.OverlayLaunchConfiguration;
import de.walware.ecommons.debug.ui.LaunchConfigUtil;
import de.walware.ecommons.io.FileValidator;
import de.walware.ecommons.ui.util.UIAccess;
import de.walware.ecommons.variables.core.StringVariable;
import de.walware.ecommons.variables.core.VariableText;

import net.sourceforge.texlipse.TexPathConfig;
import net.sourceforge.texlipse.Texlipse;
import net.sourceforge.texlipse.builder.Builder;
import net.sourceforge.texlipse.builder.BuilderRegistry;

import de.walware.statet.r.cmd.ui.launching.RCmdLaunching;
import de.walware.statet.r.internal.sweave.Messages;
import de.walware.statet.r.internal.sweave.SweavePlugin;
import de.walware.statet.r.launching.core.RLaunching;
import de.walware.statet.r.launching.ui.REnvTab;


public class RweaveTexLaunchDelegate extends LaunchConfigurationDelegate {
	
	
	public static final String VARNAME_SWEAVE_FILE= "source_file_path"; //$NON-NLS-1$
	public static final String VARNAME_LATEX_FILE= "latex_file_path"; //$NON-NLS-1$
	public static final String VARNAME_OUTPUT_FILE= "output_file_path"; //$NON-NLS-1$
	
	public static final IStringVariable VARIABLE_SWEAVE_FILE= new StringVariable(VARNAME_SWEAVE_FILE, "Returns the workspace relative path of the Sweave file.");
	public static final IStringVariable VARIABLE_LATEX_FILE= new StringVariable(VARNAME_LATEX_FILE, "Returns the workspace relative path of the LaTeX file.");
	public static final IStringVariable VARIABLE_OUTPUT_FILE= new StringVariable(VARNAME_OUTPUT_FILE, "Returns the workspace relative path of the output file.");
	
	
	public static final int STEP_WEAVE= 0x1;
	public static final int STEP_TEX= 0x2;
	public static final int STEP_PREVIEW= 0x4;
	
	public static final String SWEAVE_CONSOLE= "console"; //$NON-NLS-1$
	public static final String SWEAVE_LAUNCH= "cmdlaunch"; //$NON-NLS-1$
	public static final String DEFAULT_SWEAVE_R_COMMANDS= "Sweave(file= \"${resource_loc:${"+VARNAME_SWEAVE_FILE+"}}\")"; //$NON-NLS-1$
	public static final int SWEAVE_TYPE_DISABLED= 0;
	public static final int SWEAVE_TYPE_RCMD= 1;
	public static final int SWEAVE_TYPE_RCONSOLE= 2;
	
	public static final int BUILDTEX_TYPE_DISABLED= 0;
	public static final int BUILDTEX_TYPE_ECLIPSE= 1;
	public static final int BUILDTEX_TYPE_RCONSOLE= 2;
	public static final int DEFAULT_BUILDTEX_TYPE= BUILDTEX_TYPE_ECLIPSE;
	
	public static final String DEFAULT_BUILDTEX_R_COMMANDS= "require(tools)\ntexi2dvi(file= \"${resource_loc:${"+VARNAME_LATEX_FILE+"}}\", pdf= TRUE)"; //$NON-NLS-1$
	public static final String DEFAULT_BUILDTEX_FORMAT= "pdf"; //$NON-NLS-1$
	
	public static final String PREVIEW_IDE= "ide"; //$NON-NLS-1$
	public static final String PREVIEW_SPECIAL= "tex"; //$NON-NLS-1$
	
	
	static ILaunchConfigurationDelegate getRunDelegate(final ILaunchConfiguration configuration) throws CoreException {
		final ILaunchConfigurationType type= configuration.getType();
		final Set<String> modes= new HashSet<String>();
		modes.add(ILaunchManager.RUN_MODE);
		ILaunchDelegate preferredDelegate= configuration.getPreferredDelegate(modes);
		if (preferredDelegate == null) {
			preferredDelegate= type.getPreferredDelegate(modes);
		}
		if (preferredDelegate != null) {
			return preferredDelegate.getDelegate();
		}
		final ILaunchDelegate[] delegates= type.getDelegates(modes);
		if (delegates.length > 0) {
			return delegates[0].getDelegate();
		}
		throw new CoreException(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, ICommonStatusConstants.LAUNCHING,
				"Missing Delegate", null)); //$NON-NLS-1$
	}
	
	
	public RweaveTexLaunchDelegate() {
	}
	
	
	@Override
	protected IProject[] getBuildOrder(final ILaunchConfiguration configuration, final String mode) throws CoreException {
		final IResource resource= DebugUITools.getSelectedResource();
		if (resource != null) {
			return LaunchConfigUtil.getProjectList(resource);
		}
		return null;
	}
	
	@Override
	public void launch(final ILaunchConfiguration configuration, final String mode,
			final ILaunch launch, final IProgressMonitor monitor) throws CoreException {
		LaunchConfigUtil.initProgressMonitor(configuration, monitor, 100);
		final int buildFlags= configuration.getAttribute(SweaveProcessing.ATT_BUILDSTEPS, 0);
		if (configuration instanceof ILaunchConfigurationWorkingCopy) {
			((ILaunchConfigurationWorkingCopy) configuration).setAttribute(SweaveProcessing.ATT_BUILDSTEPS, (String) null);
		}
		
		final IResource selectedResource= DebugUITools.getSelectedResource();
		final IWorkbenchPage workbenchPage= UIAccess.getActiveWorkbenchPage(false);
		IFile sweaveFile;
		if (selectedResource instanceof IFile && selectedResource.exists()) {
			sweaveFile= (IFile) selectedResource;
		}
		else {
			throw new CoreException(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
					Messages.ProcessingConfig_error_NoFileSelected_message, null));
		}
		
		final RweaveTexTool task= new RweaveTexTool(configuration.getName(), launch, workbenchPage, sweaveFile);
		
		// Tex config (for output format, before sweave)
		{	task.fTexOpenEditor= configuration.getAttribute(TexTab.ATTR_OPENTEX_ENABLED, TexTab.OPEN_OFF);
			
			final VariableText outputDir= new VariableText(
					replaceOldVariables(configuration.getAttribute(TexTab.ATTR_BUILDTEX_OUTPUTDIR, "")),
					RweaveTexTool.OUTPUT_DIR_VARNAMES);
			final String outputFormat;
			
			int texType= configuration.getAttribute(TexTab.ATTR_BUILDTEX_TYPE, -2);
			if (texType == -2) {
				texType= configuration.getAttribute(TexTab.ATTR_BUILDTEX_ENABLED, false) ? BUILDTEX_TYPE_ECLIPSE : BUILDTEX_TYPE_DISABLED;
			}
			switch (texType) {
			case BUILDTEX_TYPE_ECLIPSE:
				final Builder builder= BuilderRegistry.get(configuration.getAttribute(TexTab.ATTR_BUILDTEX_ECLIPSE_BUILDERID, -1));
				task.setBuildTex(builder);
				outputFormat= (builder != null) ? builder.getOutputFormat() : null;
				break;
			case BUILDTEX_TYPE_RCONSOLE:
				task.setBuildTex(new VariableText(
						configuration.getAttribute(TexTab.ATTR_BUILDTEX_R_COMMANDS, ""), //$NON-NLS-1$
						RweaveTexTool.TEX_COMMAND_VARNAMES) );
				//$FALL-THROUGH$
			default:
				outputFormat= configuration.getAttribute(TexTab.ATTR_BUILDTEX_FORMAT, ""); //$NON-NLS-1$
			}
			task.setOutput(outputDir, outputFormat);
			
			task.fRunTex= SweaveProcessing.isEnabled(STEP_TEX, buildFlags);
		}
		// Sweave config
		{	final String sweaveFolderRaw= configuration.getAttribute(RweaveTab.ATTR_SWEAVE_FOLDER, (String) null);
			if (sweaveFolderRaw != null && !sweaveFolderRaw.isEmpty()) {
				task.setWorkingDir(new VariableText(sweaveFolderRaw,
						RweaveTexTool.SWEAVE_FOLDER_VARNAMES ));
			}
			
			final String sweaveProcessing= configuration.getAttribute(RweaveTab.ATTR_SWEAVE_ID, (String) null);
			if (sweaveProcessing.startsWith(RweaveTexLaunchDelegate.SWEAVE_LAUNCH)) {
				final String[] split= sweaveProcessing.split(":", 2); //$NON-NLS-1$
				final String sweaveConfigName= (split.length == 2) ? split[1] : ""; //$NON-NLS-1$
				
				final Map<String, Object> attributes= new HashMap<String, Object>();
				attributes.put(RCmdLaunching.ATTR_R_CMD_RESOURCE, sweaveFile.getLocation().toOSString());
				attributes.put(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
				
				final ILaunchConfiguration sweaveConfig= getRCmdSweaveConfig(sweaveConfigName, attributes);
				if (sweaveConfig == null && SweaveProcessing.isEnabled(STEP_WEAVE, buildFlags)) {
					throw new CoreException(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
							NLS.bind(Messages.ProcessingConfig_error_MissingRCmdConfig_message, sweaveConfigName), null));
				}
				task.setSweave(sweaveConfig);
				
				IFileStore wd= task.getWorkingDirectory();
				if (wd == null) {
					final FileValidator workingDirectory= REnvTab.getWorkingDirectoryValidator(sweaveConfig, true);
					final IStatus status= task.setWorkingDir(workingDirectory.getFileStore(), (IContainer) workingDirectory.getWorkspaceResource(), false);
					if (status.getSeverity() >= IStatus.ERROR && SweaveProcessing.isEnabled(STEP_WEAVE, buildFlags)) {
						throw new CoreException(status);
					}
					wd= workingDirectory.getFileStore();
				}
				attributes.put(RLaunching.ATTR_WORKING_DIRECTORY, wd.toURI().toString());
			}
			else if (sweaveProcessing.startsWith(RweaveTexLaunchDelegate.SWEAVE_CONSOLE)) {
				final String[] split= sweaveProcessing.split(":", 2); //$NON-NLS-1$
				task.setSweave(new VariableText(
						(split.length == 2 && split[1].length() > 0) ? replaceOldVariables(split[1]) : DEFAULT_SWEAVE_R_COMMANDS,
						RweaveTexTool.SWEAVE_COMMAND_VARNAMES) );
			}
			else if (task.getWorkingDirectory() == null) {
				task.setWorkingDir(null, sweaveFile.getParent(), true);
			}
			
			task.fRunSweave= SweaveProcessing.isEnabled(RweaveTexLaunchDelegate.STEP_WEAVE, buildFlags);
			
		}
		// Preview config
		final String preview= configuration.getAttribute(PreviewTab.ATTR_VIEWER_CODE, ""); //$NON-NLS-1$
		if ((RweaveTexLaunchDelegate.STEP_PREVIEW & buildFlags) != 0) {
			task.fRunPreview= RweaveTexTool.EXPLICITE;
		}
		else if ((0xf & buildFlags) == 0 && preview.length() > 0) {
			task.fRunPreview= RweaveTexTool.AUTO;
		}
		else {
			task.fRunPreview= RweaveTexTool.NO;
		}
		if (task.fRunPreview > RweaveTexTool.NO) {
			if (preview.startsWith(PREVIEW_SPECIAL)) {
				final String previewConfigName= preview.split(":", -1)[1]; //$NON-NLS-1$
				task.fPreviewConfig= Texlipse.getViewerManager().getConfiguration(previewConfigName);
				if (task.fPreviewConfig == null) {
					throw new CoreException(new Status(IStatus.ERROR, SweavePlugin.PLUGIN_ID, ICommonStatusConstants.LAUNCHCONFIG_ERROR,
							NLS.bind(Messages.ProcessingConfig_error_MissingViewerConfig_message, previewConfigName), null));
				}
			}
		}
		
		switch (buildFlags & 0xf) {
		case (STEP_WEAVE | STEP_TEX | STEP_PREVIEW):
		case (STEP_WEAVE | STEP_PREVIEW):
		case (STEP_TEX | STEP_PREVIEW):
			runAsJob(task,
					SweavePlugin.getDefault().getImageRegistry().getDescriptor(SweavePlugin.IMG_TOOL_BUILDANDPREVIEW) );
			return;
		case (STEP_WEAVE | STEP_TEX):
			runAsJob(task,
					SweavePlugin.getDefault().getImageRegistry().getDescriptor(SweavePlugin.IMG_TOOL_BUILD) );
			return;
		case STEP_WEAVE:
			runAsJob(task,
					SweavePlugin.getDefault().getImageRegistry().getDescriptor(SweavePlugin.IMG_TOOL_RWEAVE) );
			return;
		case STEP_TEX:
			runAsJob(task,
					SweavePlugin.getDefault().getImageRegistry().getDescriptor(SweavePlugin.IMG_TOOL_BUILDTEX) );
			return;
		case STEP_PREVIEW:
			task.run(new SubProgressMonitor(monitor, 50));
			return;
		default:
			runAsJob(task, null);
			return;
		}
	}
	
	private void runAsJob(final RweaveTexTool task, final ImageDescriptor icon) {
		final Job job= new ProcessingJob(task);
		if (icon != null) {
			job.setProperty(IProgressConstants.ICON_PROPERTY, icon);
		}
		job.schedule();
	}
	
	private OverlayLaunchConfiguration getRCmdSweaveConfig(final String name, final Map<String, Object> attributes) throws CoreException {
		final ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		final ILaunchConfigurationType launchType= launchManager.getLaunchConfigurationType(RCmdLaunching.R_CMD_CONFIGURATION_TYPE_ID); //$NON-NLS-1
		final ILaunchConfiguration[] launchConfigurations= launchManager.getLaunchConfigurations(launchType);
		
		if (name != null && name.length() > 0) {
			for (final ILaunchConfiguration config : launchConfigurations) {
				if (config.getName().equals(name)) {
					return new OverlayLaunchConfiguration(config, attributes);
				}
			}
		}
		return null;
	}
	
	private String replaceOldVariables(String text) {
		text= text.replace(TexPathConfig.SOURCEFILE_LOC_VARIABLE, "${resource_loc:${"+VARNAME_SWEAVE_FILE+"}}");
		text= text.replace(TexPathConfig.SOURCEFILE_PATH_VARIABLE, "${"+VARNAME_SWEAVE_FILE+"}");
		text= text.replace(TexPathConfig.TEXFILE_LOC_VARIABLE, "${resource_loc:${"+VARNAME_LATEX_FILE+"}}");
		text= text.replace(TexPathConfig.TEXFILE_PATH_VARIABLE, "${"+VARNAME_LATEX_FILE+"}");
		return text;
	}
	
	
}
