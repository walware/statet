/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.console;

import org.eclipse.jface.action.Action;

import de.walware.statet.ui.SharedMessages;
import de.walware.statet.ui.StatetImages;


public class ScrollLockAction extends Action {

	
	public interface Receiver {
		
		public void setAutoScroll(boolean enabled);
	}
	
	
	private Receiver fView;
	
	
	public ScrollLockAction(Receiver view, boolean initialChecked) {
		
		setText(SharedMessages.ToggleScrollLockAction_name);
		setToolTipText(SharedMessages.ToggleScrollLockAction_tooltip);
		
		setImageDescriptor(StatetImages.DESC_LOCTOOL_SCROLLLOCK);
		
		fView = view;
		setChecked(initialChecked);
	}
	
	public void run() {
	
		fView.setAutoScroll(!isChecked());
	}
}
