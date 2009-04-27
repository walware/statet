/*******************************************************************************
 * Copyright (c) 2006-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.util.IToolProvider;
import de.walware.statet.nico.core.util.IToolRetargetable;


/**
 * Can be used for actions for tools.
 * <p>
 * Same as {@link ToolRetargetableHandler} for actions
 */
public class ToolAction extends Action implements IToolRetargetable {
	
	
	private ToolProcess fTool;
	private boolean fDisableOnTermination;
	
	
	public ToolAction(final IToolProvider support, final boolean disableOnTermination) {
		this(support, SWT.NONE, disableOnTermination);
	}
	
	public ToolAction(final IToolProvider support, final int style, final boolean disableOnTermination) {
		super(null, style);
		
		support.addToolRetargetable(this);
		fTool = support.getTool();
		fDisableOnTermination = disableOnTermination;
	}
	
	
	public void setTool(final ToolProcess tool) {
		fTool = tool;
		handleToolChanged();
	}
	
	public void handleToolChanged() {
		update();
	}
	
	public void toolTerminated() {
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
