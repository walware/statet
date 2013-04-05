/*******************************************************************************
 * Copyright (c) 2011-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import de.walware.ecommons.ts.ui.IToolRunnableDecorator;

import de.walware.statet.nico.core.runtime.IConsoleRunnable;
import de.walware.statet.nico.core.runtime.SubmitType;

import de.walware.statet.r.console.core.RConsoleTool;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.nico.AbstractRController;


public class SubmitFileViaCommandRunnable implements IConsoleRunnable, IToolRunnableDecorator {
	
	
	private final String fCommand;
	private final ISourceUnit fSourceUnit;
	
	private final Image fImage;
	private final String fLabel;
	
	
	public SubmitFileViaCommandRunnable(final Image image, final String label,
			final String command, final ISourceUnit su) {
		fImage = image;
		fLabel = label;
		fCommand = command;
		fSourceUnit = su;
	}
	
	
	@Override
	public String getTypeId() {
		return "r/console/runFileCommand"; //$NON-NLS-1$
	}
	
	@Override
	public SubmitType getSubmitType() {
		return SubmitType.EDITOR;
	}
	
	@Override
	public Image getImage() {
		return fImage;
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
	public boolean changed(final int event, final ITool tool) {
		return true;
	}
	
	@Override
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
