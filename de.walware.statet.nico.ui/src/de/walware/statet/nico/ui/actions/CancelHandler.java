/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Display;

import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.util.IToolProvider;


/**
 * Handler to cancel tool tasks.
 */
public class CancelHandler extends ToolRetargetableHandler {
	
	
	public static final String MENU_ID = "de.walware.statet.nico.menus.Cancel"; //$NON-NLS-1$
	
	public static final String PAR_OPTIONS = "options"; //$NON-NLS-1$
	
	
	private final int fOptions;
	
	
	public CancelHandler(final IToolProvider toolProvider) {
		super(toolProvider, null);
		fOptions = 0;
		init();
	}
	
	public CancelHandler(final IToolProvider toolProvider, final int options) {
		super(toolProvider, null);
		fOptions = options;
		init();
	}
	
	
	@Override
	protected Object doExecute(final ExecutionEvent event) {
		final String optionsParameter = event.getParameter(PAR_OPTIONS);
		int options = fOptions;
		if (optionsParameter != null) {
			try {
				options = Integer.decode(optionsParameter);
			}
			catch (final NumberFormatException e) {
			}
		}
		
		final ToolProcess tool = getCheckedTool();
		final ToolController controller = (tool != null) ? tool.getController() : null;
		if (controller == null) {
			return null;
		}
		
		if (!controller.cancelTask(options)) {
			Display.getCurrent().beep();
		}
		
		return null;
	}
	
}
