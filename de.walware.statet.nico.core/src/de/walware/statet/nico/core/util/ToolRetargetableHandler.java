/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core.util;

import org.eclipse.core.commands.AbstractHandler;

import de.walware.statet.nico.core.runtime.ToolProcess;


/**
 * 
 */
public abstract class ToolRetargetableHandler extends AbstractHandler implements IToolRetargetable {
	
	
	private ToolProcess fTool;
	private boolean fDisableOnTermination;
	
	
	public ToolRetargetableHandler(final ToolProcess tool, final boolean disableOnTermination) {
		super();
		fTool = tool;
		fDisableOnTermination = disableOnTermination;
		handleToolChanged();
	}
	
	public ToolRetargetableHandler(final IToolProvider toolProvider, final boolean disableOnTermination) {
		super();
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
		setBaseEnabled(evaluateEnabled());
	}
	
	public void handleToolTerminated() {
		if (fDisableOnTermination) {
			setBaseEnabled(evaluateEnabled());
		}
	}
	
	protected boolean evaluateEnabled() {
		return (fTool != null
				&& (!fDisableOnTermination || !fTool.isTerminated()));
	}
	
}
