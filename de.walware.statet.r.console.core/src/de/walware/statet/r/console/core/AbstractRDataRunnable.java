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

package de.walware.statet.r.console.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;
import de.walware.ecommons.ts.IToolService;


public abstract class AbstractRDataRunnable implements IToolRunnable {
	
	
	private final String fTypeId;
	private final String fLabel;
	
	
	public AbstractRDataRunnable(final String typeId, final String label) {
		fTypeId = typeId;
		fLabel = label;
	}
	
	
	public String getTypeId() {
		return fTypeId;
	}
	
	public String getLabel() {
		return fLabel;
	}
	
	public boolean isRunnableIn(final ITool tool) {
		return tool.isProvidingFeatureSet(RTool.R_BASIC_FEATURESET_ID);
	}
	
	public boolean changed(final int event, final ITool tool) {
		return true;
	}
	
	public void run(final IToolService service,
			final IProgressMonitor monitor) throws CoreException {
		run((IRDataAdapter) service, monitor);
	}
	
	protected abstract void run(IRDataAdapter r,
			IProgressMonitor monitor) throws CoreException;
	
}
