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

package de.walware.eclipsecommons;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.swt.widgets.Display;

import de.walware.eclipsecommons.ui.util.UIAccess;


/**
 * Managed command handler, with
 *   <li>Settable enablement state</li>
 */
public abstract class UpdateableHandler extends AbstractHandler {
	
	
	private boolean fIsEnabled;
	
	
	protected UpdateableHandler(final boolean initialEnablement) {
		fIsEnabled = initialEnablement;
	}
	
	protected UpdateableHandler() {
		this(false);
	}
	
	
	protected void setEnabled(final boolean enabled) {
		if (fIsEnabled != enabled) {
			fIsEnabled = enabled;
			final Display display = UIAccess.getDisplay();
			final Runnable update = new Runnable() {
				public void run() {
					fireHandlerChanged(new HandlerEvent(UpdateableHandler.this, true, false));
				}
			};
			if (display.getThread() == Thread.currentThread()) {
				update.run();
			}
			else {
				display.asyncExec(update);
			}
		}
	}
	
	@Override
	public boolean isEnabled() {
		return fIsEnabled;
	}
	
}
