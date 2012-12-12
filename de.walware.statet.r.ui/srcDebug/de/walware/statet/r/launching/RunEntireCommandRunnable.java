/*******************************************************************************
 * Copyright (c) 2011-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolService;

import de.walware.statet.nico.core.runtime.IConsoleRunnable;
import de.walware.statet.nico.core.runtime.SubmitType;

import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.nico.IRSrcref;


public class RunEntireCommandRunnable implements IConsoleRunnable {
	
	
	private final String[] fLines;
	
	private final String fLabel;
	private final IRSrcref fSrcref;
	
	
	public RunEntireCommandRunnable(final String[] lines, final IRSrcref srcref) {
		fLines = lines;
		fLabel = createLabel();
		fSrcref = srcref;
		if (fSrcref instanceof RCodeLaunching.SourceRegion) {
			((RCodeLaunching.SourceRegion) fSrcref).installMarker();
		}
	}
	
	
	private String createLabel() {
		final String label = fLines[0];
		if (fLines.length > 0) {
			return label + " \u2026";
		}
		return label;
	}
	
	
	@Override
	public String getTypeId() {
		return "r/console/runCommand"; //$NON-NLS-1$
	}
	
	@Override
	public SubmitType getSubmitType() {
		return SubmitType.EDITOR;
	}
	
	@Override
	public String getLabel() {
		return fLabel;
	}
	
	@Override
	public boolean isRunnableIn(final ITool tool) {
		return (tool.isProvidingFeatureSet(RConsoleTool.R_BASIC_FEATURESET_ID));
	}
	
	@Override
	public boolean changed(final int event, final ITool process) {
		switch (event) {
		case REMOVING_FROM:
		case BEING_ABANDONED:
		case FINISHING_OK:
		case FINISHING_ERROR:
		case FINISHING_CANCEL:
			if (fSrcref instanceof RCodeLaunching.SourceRegion) {
				((RCodeLaunching.SourceRegion) fSrcref).disposeMarker();
			}
		}
		return true;
	}
	
	@Override
	public void run(final IToolService service,
			final IProgressMonitor monitor) throws CoreException {
		final AbstractRController r = (AbstractRController) service;
		try {
			r.submitCommandToConsole(fLines, fSrcref, monitor);
		}
		finally {
			r.briefAboutChange(RWorkspace.REFRESH_AUTO);
		}
	}
	
}
