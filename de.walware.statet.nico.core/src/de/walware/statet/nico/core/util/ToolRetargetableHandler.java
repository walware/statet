/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.util;

import de.walware.eclipsecommons.UpdateableHandler;

import de.walware.statet.nico.core.runtime.ToolProcess;


/**
 * 
 */
public abstract class ToolRetargetableHandler extends UpdateableHandler implements IToolRetargetable {
	
	
	private ToolProcess fTool;
	private boolean fDisableOnTermination;
	
	
	public ToolRetargetableHandler(final ToolProcess tool, final boolean disableOnTermination) {
		fTool = tool;
		fDisableOnTermination = disableOnTermination;
		handleToolChanged();
	}
	
	public ToolRetargetableHandler(final IToolProvider toolProvider, final boolean disableOnTermination) {
		toolProvider.addToolRetargetable(this);
		fTool = toolProvider.getTool();
		fDisableOnTermination = disableOnTermination;
		handleToolChanged();
	}
	
	public void setTool(final ToolProcess tool) {
		fTool = tool;
		handleToolChanged();
	}
	
	public ToolProcess getTool() {
		return fTool;
	}
	
	public void handleToolChanged() {
		setEnabled(evaluateEnabled());
	}
	
	public void handleToolTerminated() {
		if (fDisableOnTermination) {
			setEnabled(evaluateEnabled());
		}
	}
	
	protected boolean evaluateEnabled() {
		return (fTool != null
				&& (!fDisableOnTermination || !fTool.isTerminated()));
	}
	
}
