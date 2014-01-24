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

package de.walware.statet.r.internal.sweave.editors;

import de.walware.ecommons.ui.actions.TogglePreferenceEnablementHandler;


/**
 * Toggles Enablement of Mark Occurrences.
 */
public class SweaveToggleMarkOccurrencesHandler extends TogglePreferenceEnablementHandler {
	
	
	public SweaveToggleMarkOccurrencesHandler() {
		super(	SweaveEditorOptions.MARKOCCURRENCES_ENABLED_PREF,
				"org.eclipse.jdt.ui.edit.text.java.toggleMarkOccurrences"); //$NON-NLS-1$
	}
	
}
