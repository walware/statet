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

import org.eclipse.swt.widgets.Display;

import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.internal.ui.Messages;
import de.walware.statet.nico.ui.NicoUI;


/**
 *
 */
public class CancelAction extends ToolAction {

	
	public CancelAction(IToolActionSupport support) {
		
		super(support, true);
		
		setText(Messages.CancelAction_name);
		setToolTipText(Messages.CancelAction_tooltip);
		setImageDescriptor(NicoUI.getImageDescriptor(NicoUI.IMG_LOCTOOL_CANCEL));
		setDisabledImageDescriptor(NicoUI.getImageDescriptor(NicoUI.IMG_LOCTOOLD_CANCEL));
		
		handleToolChanged();
	}
	
	
	public void run() {
		
		ToolProcess tool = getTool();
		ToolController controller = (tool != null) ? tool.getController() : null;
		if (controller == null) {
			return;
		}
		
		if (!controller.cancelTask()) {
			Display.getCurrent().beep();
		}
	}
}
