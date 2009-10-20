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

package de.walware.statet.nico.core.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.FileUtil;
import de.walware.ecommons.IDisposable;
import de.walware.ecommons.variables.core.ILocationVariable;
import de.walware.ecommons.variables.core.VariableText;
import de.walware.ecommons.variables.core.WrappedDynamicVariable;

import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.ITrack;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolStreamMonitor;
import de.walware.statet.nico.core.runtime.ToolStreamProxy;
import de.walware.statet.nico.core.runtime.ToolWorkspace;
import de.walware.statet.nico.internal.core.NicoPlugin;


public class TrackWriter implements ITrack, IStreamListener, IDisposable {
	
	
	private static final String TRUNCATE_INFO = "[...] (truncated)\n\n";
	
	public static String getTruncateInfo() {
		return TRUNCATE_INFO;
	}
	
	public static String resolveVariables(final String path, final ToolWorkspace workspace) throws CoreException {
		final List<IDynamicVariable> variables = workspace.getStringVariables();
		final List<IDynamicVariable> checkedVariables = new ArrayList<IDynamicVariable>(variables.size());
		for (final IDynamicVariable variable : variables) {
			if (variable instanceof ILocationVariable) {
				checkedVariables.add(variable);
			}
			else {
				checkedVariables.add(new WrappedDynamicVariable(variable) {
					@Override
					public String getValue(final String argument) throws CoreException {
						return super.getValue(argument).replaceAll("\\\\|\\/|\\:", "-"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				});
			}
		}
		final VariableText text = new VariableText(path, checkedVariables, true);
		text.performInitialStringSubstitution(true);
		text.performFinalStringSubstitution(null);
		return text.getText();
	}
	
	
	private ToolController fController;
	
	private TrackingConfiguration fConfig;
	
	private IFileStore fStoreFile;
	private Writer fOutputWriter;
	
	private IStreamListener fInputListener;
	private IStreamListener fOutputListener;
	
	private int fTrumcateMax;
	private int fTruncateCurrent;
	
	
	public TrackWriter(final ToolController controller, final TrackingConfiguration config) {
		if (controller == null || config == null) {
			throw new NullPointerException();
		}
		fController = controller;
		fConfig = config;
	}
	
	
	public IStatus init(final IProgressMonitor monitor)
			throws CoreException {
		OutputStream outputStream = null;
		try {
			try {
				fStoreFile = resolveTrackingPath(fConfig.getFilePath());
			}
			catch (final CoreException e) {
				throw new CoreException(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, -1, "Failed to resolve path of the tracking file.", e));
			}
			
			if (fConfig.getId().equals(HistoryTrackingConfiguration.HISTORY_TRACKING_ID)
					&& ((HistoryTrackingConfiguration) fConfig).getLoadHistory()
					&& fStoreFile.fetchInfo().exists()) {
				fController.getProcess().getHistory().load(fStoreFile, fConfig.getFileEncoding(), false, monitor); //$NON-NLS-1$
			}
			
			outputStream = fStoreFile.openOutputStream(fConfig.getFileMode(), monitor);
			if (fStoreFile.fetchInfo().getLength() <= 0L) {
				FileUtil.prepareTextOutput(outputStream, fConfig.getFileEncoding());
			}
			fOutputWriter = new BufferedWriter(new OutputStreamWriter(outputStream, fConfig.getFileEncoding()));
			
			final EnumSet<SubmitType> submitTypes = fConfig.getSubmitTypes();
			final ToolStreamProxy streams = fController.getStreams();
			if (fConfig.getTrackStreamInfo()) {
				streams.getInfoStreamMonitor().addListener(this, submitTypes);
			}
			if (fConfig.getTrackStreamInput()) {
				fInputListener = (fConfig.getTrackStreamInputHistoryOnly()) ?
						new IStreamListener() {
							public void streamAppended(final String text, final IStreamMonitor monitor) {
								if ((((ToolStreamMonitor) monitor).getMeta() & IToolRunnableControllerAdapter.META_HISTORY_DONTADD) == 0) {
									TrackWriter.this.streamAppendedNL(text);
								}
							}
						} :
						new IStreamListener() {
							public void streamAppended(final String text, final IStreamMonitor monitor) {
								TrackWriter.this.streamAppended(text, monitor);
							}
						};
				streams.getInputStreamMonitor().addListener(fInputListener, submitTypes);
			}
			if (fConfig.getTrackStreamOutput()) {
				if (fConfig.getTrackStreamOutputTruncate()) {
					fTrumcateMax = fConfig.getTrackStreamOutputTruncateLines();
					fOutputListener = new IStreamListener() {
						public void streamAppended(final String text, final IStreamMonitor monitor) {
							TrackWriter.this.streamAppendedTruncateOutput(text);
						}
					};
				}
				else {
					fOutputListener = this;
				}
				streams.getOutputStreamMonitor().addListener(fOutputListener, submitTypes);
				streams.getErrorStreamMonitor().addListener(this, submitTypes);
			}
			
			if (fConfig.getPrependTimestamp()) {
				final ToolProcess process = fController.getProcess();
				final String comment = process.createTimestampComment(process.getConnectionTimestamp());
				try {
					fOutputWriter.write(comment);
				}
				catch (final Exception e) {
					onError();
					throw e;
				}
			}
			
			return Status.OK_STATUS;
		}
		catch (final Exception e) {
			onError();
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (final IOException ignore) {}
			}
			return new Status(IStatus.WARNING, NicoCore.PLUGIN_ID, -1, NLS.bind("Could not initialize tracking ''{0}''.", fConfig.getName()), e);
		}
	}
	
	protected IFileStore resolveTrackingPath(String filePath) throws CoreException {
		filePath = resolveVariables(filePath, fController.getWorkspaceData());
		return FileUtil.getFileStore(filePath);
	}
	
	public void streamAppended(final String text, final IStreamMonitor monitor) {
		fTruncateCurrent = 0;
		try {
			fOutputWriter.write(text);
		}
		catch (final IOException e) {
			NicoPlugin.log(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, -1, "An error occurred when writing to the tracking file. Tracking is stopped.", e));
			onError();
		}
	}
	
	private void streamAppendedTruncateOutput(String text) {
		if (fTruncateCurrent == Integer.MAX_VALUE) {
			return;
		}
		String text2 = null;
		if (fTruncateCurrent > fTrumcateMax) {
			fTruncateCurrent = Integer.MAX_VALUE;
			text = TRUNCATE_INFO;
		}
		else {
			int next = -1;
			while ((next = text.indexOf('\n', next+1)) >= 0) {
				if (++fTruncateCurrent > fTrumcateMax) {
					if (text.length() != next+1) {
						fTruncateCurrent = Integer.MAX_VALUE;
						text = text.substring(0, next+1);
						text2 = TRUNCATE_INFO;
					}
					break;
				}
			}
		}
		try {
			fOutputWriter.write(text);
			if (text2 != null) {
				fOutputWriter.write(text2);
			}
		}
		catch (final IOException e) {
			NicoPlugin.log(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, -1, "An error occurred when writing to the tracking file. Tracking is stopped.", e));
			onError();
		}
	}
	
	private void streamAppendedNL(final String text) {
		fTruncateCurrent = 0;
		try {
			fOutputWriter.write(text);
			fOutputWriter.write('\n');
		}
		catch (final IOException e) {
			NicoPlugin.log(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, -1, "An error occurred when writing to the tracking file. Tracking is stopped.", e));
			onError();
		}
	}
	
	private void onError() {
		final ToolStreamProxy streams = fController.getStreams();
		streams.getInfoStreamMonitor().removeListener(this);
		if (fInputListener != null) {
			streams.getInputStreamMonitor().removeListener(fInputListener);
		}
		if (fOutputListener != null) {
			streams.getOutputStreamMonitor().removeListener(fOutputListener);
			streams.getErrorStreamMonitor().removeListener(this);
		}
		if (fOutputWriter != null) {
			try {
				fOutputWriter.close();
			}
			catch (final IOException ignore) {}
			finally {
				fOutputWriter = null;
			}
		}
		dispose();
	}
	
	public void dispose() {
		if (fOutputWriter != null) {
			try {
				fOutputWriter.close();
			}
			catch (final IOException e) {
				NicoPlugin.log(new Status(IStatus.ERROR, NicoCore.PLUGIN_ID, -1, "An error occurred when closing the tracking file. Tracking is stopped.", e));
			}
			finally {
				fOutputWriter = null;
			}
		}
	}
	
	public String getName() {
		return fConfig.getName();
	}
	
	public void flush() {
		final Writer writer = fOutputWriter;
		if (writer != null) {
			try {
				writer.flush();
			}
			catch (final IOException e) {
			}
		}
	}
	public IFileStore getFile() {
		return fStoreFile;
	}
	
}
