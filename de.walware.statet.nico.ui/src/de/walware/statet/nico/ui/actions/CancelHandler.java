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

package de.walware.statet.nico.ui.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;

import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.util.IToolProvider;
import de.walware.statet.nico.core.util.ToolRetargetableHandler;


/**
 * Handler to cancel tool tasks.
 */
public class CancelHandler extends ToolRetargetableHandler {
	
	
	public static final String MENU_ID = "de.walware.statet.nico.menus.Cancel"; //$NON-NLS-1$
	
	public static final String COMMAND_CURRENT = "de.walware.statet.nico.commands.CancelCurrent"; //$NON-NLS-1$
	public static final String COMMAND_ALL = "de.walware.statet.nico.commands.CancelAll"; //$NON-NLS-1$
	public static final String COMMAND_CURRENTPAUSE = "de.walware.statet.nico.commands.CancelCurrentAndPause"; //$NON-NLS-1$
	
	public static final String PAR_OPTIONS = "options"; //$NON-NLS-1$
	
	
	private int fOptions;
	
	
	public CancelHandler(final IToolProvider toolProvider) {
		super(toolProvider, true);
		fOptions = 0;
	}
	
	public CancelHandler(final IToolProvider toolProvider, final int options) {
		super(toolProvider, true);
		fOptions = options;
	}
	
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final String optionsParameter = event.getParameter(PAR_OPTIONS);
		int options = fOptions;
		if (optionsParameter != null) {
			try {
				options = Integer.decode(PAR_OPTIONS);
			}
			catch (final NumberFormatException e) {
			}
		}
		
		final ToolProcess tool = getTool();
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
