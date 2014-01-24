/*=============================================================================#
 # Copyright (c) 2008-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.launcher;

import java.util.Map;

import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;


/**
 * Launch shortcut, which submits 
 *  - the current line/selection directly to R (text editor)
 * and does not change the focus by default.
 * 
 * Low requirements: ITextSelection is sufficient
 */
public class SubmitSelectionForTextHandler extends SubmitSelectionHandler implements IElementUpdater {
	
	
	public SubmitSelectionForTextHandler() {
		this(false);
	}
	
	protected SubmitSelectionForTextHandler(final boolean gotoConsole) {
		super(gotoConsole);
	}
	
	
	@Override
	public void updateElement(final UIElement element, final Map parameters) {
		element.setText(appendVariant(RLaunchingMessages.SubmitCode_TextSelection_label));
	}
	
	protected String appendVariant(final String label) {
		return label;
	}
	
}
