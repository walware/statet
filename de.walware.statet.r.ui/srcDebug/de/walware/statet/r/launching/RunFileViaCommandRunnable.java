/*******************************************************************************
 * Copyright (c) 2011 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import org.eclipse.swt.graphics.Image;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolService;

import de.walware.statet.nico.core.runtime.IConsoleRunnable;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.ui.IToolRunnableDecorator;

import de.walware.statet.r.console.core.RTool;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.nico.AbstractRController;


public class RunFileViaCommandRunnable implements IConsoleRunnable, IToolRunnableDecorator {
	
	
	private final String fCommand;
	private final ISourceUnit fSourceUnit;
	
	private final Image fImage;
	private final String fLabel;
	
	
	public RunFileViaCommandRunnable(final Image image, final String label,
			final String command, final ISourceUnit su) {
		fImage = image;
		fLabel = label;
		fCommand = command;
		fSourceUnit = su;
	}
	
	
	public String getTypeId() {
		return "r/console/runFileCommand"; //$NON-NLS-1$
	}
	
	public SubmitType getSubmitType() {
		return SubmitType.EDITOR;
	}
	
	public Image getImage() {
		return fImage;
	}
	
	public String getLabel() {
		return fLabel;
	}
	
	public boolean isRunnableIn(final ITool tool) {
		return (tool.isProvidingFeatureSet(RTool.R_BASIC_FEATURESET_ID));
	}
	
	public boolean changed(final int event, final ITool tool) {
		return true;
	}
	
	public void run(final IToolService service,
			final IProgressMonitor monitor) throws CoreException {
		final AbstractRController r = (AbstractRController) service;
		try {
			r.submitFileCommandToConsole(new String[] { fCommand }, fSourceUnit, monitor);
		}
		finally {
			r.briefAboutChange(RWorkspace.REFRESH_AUTO);
		}
	}
	
}
