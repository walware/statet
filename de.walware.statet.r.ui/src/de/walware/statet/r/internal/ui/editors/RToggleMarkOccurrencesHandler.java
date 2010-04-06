/*******************************************************************************
 * Copyright (c) 2008-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import de.walware.ecommons.ui.actions.TogglePreferenceEnablementHandler;

import de.walware.statet.r.ui.editors.REditorOptions;


/**
 * Toggles Enablement of Mark Occurrences.
 */
public class RToggleMarkOccurrencesHandler extends TogglePreferenceEnablementHandler {
	
	
	public RToggleMarkOccurrencesHandler() {
		super(	REditorOptions.PREF_MARKOCCURRENCES_ENABLED,
				"org.eclipse.jdt.ui.edit.text.java.toggleMarkOccurrences"); //$NON-NLS-1$
	}
	
}
