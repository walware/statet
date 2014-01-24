/*=============================================================================#
 # Copyright (c) 2007-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.ui.text.r;

import org.eclipse.jface.preference.IPreferenceStore;

import de.walware.ecommons.preferences.IPreferenceAccess;
import de.walware.ecommons.ui.ColorManager;

import de.walware.statet.ext.ui.text.CommentScanner;

import de.walware.statet.r.ui.RUIPreferenceConstants;


/**
 * Scanner for R comments.
 */
public class RCommentScanner extends CommentScanner {
	
	
	public RCommentScanner(final ColorManager colorManager, final IPreferenceStore preferenceStore, final IPreferenceAccess corePrefs) {
		super(colorManager, preferenceStore, corePrefs, 
				RUIPreferenceConstants.R.TS_GROUP_ID,
				IRTextTokens.COMMENT_KEY, IRTextTokens.TASK_TAG_KEY);
	}
	
}
