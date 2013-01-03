/*******************************************************************************
 * Copyright (c) 2008-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.ui.editors.text.IFoldingCommandIds;

import de.walware.ecommons.ui.actions.TogglePreferenceEnablementHandler;

import de.walware.statet.r.ui.editors.REditorOptions;


/**
 * Toggles Enablement of Code Folding.
 */
public class RToggleFoldingHandler extends TogglePreferenceEnablementHandler {
	
	
	public RToggleFoldingHandler() {
		super(REditorOptions.PREF_FOLDING_ENABLED, IFoldingCommandIds.FOLDING_TOGGLE);
	}
	
}
