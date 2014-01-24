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

package de.walware.statet.r.internal.ui.editors;

import de.walware.ecommons.ui.actions.TogglePreferenceEnablementHandler;

import de.walware.statet.r.ui.editors.REditorBuild;


/**
 * Toggles activation of report problems when typing in R editors.
 */
public class RToggleLiveReportProblemsHandler extends TogglePreferenceEnablementHandler {
	
	
	public RToggleLiveReportProblemsHandler() {
		super(	REditorBuild.PROBLEMCHECKING_ENABLED_PREF,
				"de.walware.ecommons.ltk.commands.ToggleLiveReportProblems"); //$NON-NLS-1$
	}
	
}
