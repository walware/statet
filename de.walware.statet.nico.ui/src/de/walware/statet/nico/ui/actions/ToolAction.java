/*******************************************************************************
 * Copyright (c) 2006 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

import de.walware.statet.nico.core.runtime.ToolProcess;


/**
 * Can be used for actions for tools.
 */
public class ToolAction extends Action implements IToolAction {

	
	private ToolProcess fTool;
	private boolean fDisableOnTermination;
	
	
	public ToolAction(IToolActionSupport support, boolean disableOnTermination) {
		
		this(support, SWT.NONE, disableOnTermination);
	}
	
	public ToolAction(IToolActionSupport support, int style, boolean disableOnTermination) {
		
		super(null, style);
		
		support.addToolAction(this);
		fTool = support.getTool();
		fDisableOnTermination = disableOnTermination;
	}
	
	public void setTool(ToolProcess tool) {
		
		fTool = tool;
		handleToolChanged();
	}
	
	public void handleToolChanged() {
		
		update();
	}
	
	public void handleToolTerminated() {
		
		update();
	}
	
	public void update() {
		
		setEnabled(fTool != null 
				&& (!fDisableOnTermination || !fTool.isTerminated()));
	}
	
	public ToolProcess getTool() {
		
		return fTool;
	}
}
