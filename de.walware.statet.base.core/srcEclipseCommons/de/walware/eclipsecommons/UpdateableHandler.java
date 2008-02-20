/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.HandlerEvent;


/**
 * Managed command handler, with
 *  <li>Settable enablement state</li>
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
			fireHandlerChanged(new HandlerEvent(this, true, false));
		}
	}
	
	@Override
	public boolean isEnabled() {
		return fIsEnabled;
	}
	
}
