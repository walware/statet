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

package de.walware.statet.r.ui.text.r;

import org.eclipse.jface.preference.IPreferenceStore;

import de.walware.eclipsecommons.preferences.IPreferenceAccess;
import de.walware.eclipsecommons.ui.util.ColorManager;

import de.walware.statet.ext.ui.text.CommentScanner;
import de.walware.statet.r.ui.RUIPreferenceConstants;


/**
 * Scanner for R comments.
 */
public class RCommentScanner extends CommentScanner {
	
	public RCommentScanner(ColorManager colorManager, IPreferenceStore preferenceStore, IPreferenceAccess corePrefs) {
		super(colorManager, preferenceStore, corePrefs, 
				RUIPreferenceConstants.R.TS_CONTEXT_ID,
				IRTextTokens.COMMENT_KEY, IRTextTokens.TASK_TAG_KEY);
	}
	
}
