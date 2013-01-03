/*******************************************************************************
 * Copyright (c) 2010-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rhelp;

import java.util.regex.Pattern;


public interface IRHelpSearchMatch {
	
	
	String PRE_TAGS_PREFIX = "<EMATCH-"; //$NON-NLS-1$
	
	String[] PRE_TAGS = new String[] {
			"<EMATCH-A>", //$NON-NLS-1
			"<EMATCH-B>", //$NON-NLS-1$
			"<EMATCH-C>", //$NON-NLS-1$
			"<EMATCH-D>", //$NON-NLS-1$
			"<EMATCH-E>", //$NON-NLS-1$
			"<EMATCH-F>", //$NON-NLS-1$
			"<EMATCH-G>", //$NON-NLS-1$
			"<EMATCH-H>", //$NON-NLS-1$
			"<EMATCH-I>", //$NON-NLS-1$
			"<EMATCH-J>", //$NON-NLS-1$
	};
	String[] POST_TAGS = new String[] {
			"</EMATCH-A>", //$NON-NLS-1$
			"</EMATCH-B>", //$NON-NLS-1$
			"</EMATCH-C>", //$NON-NLS-1$
			"</EMATCH-D>", //$NON-NLS-1$
			"</EMATCH-E>", //$NON-NLS-1$
			"</EMATCH-F>", //$NON-NLS-1$
			"</EMATCH-G>", //$NON-NLS-1$
			"</EMATCH-H>", //$NON-NLS-1$
			"</EMATCH-I>", //$NON-NLS-1$
			"</EMATCH-J>", //$NON-NLS-1$
	};
	Pattern ALL_TAGS_PATTERN = Pattern.compile("\\<\\/?EMATCH\\-[A-Z]\\>"); //$NON-NLS-1$
	
	interface MatchFragment {
		
		String getField();
		
		String getFieldLabel();
		
		String getText();
		
	}
	
	
	IRHelpPage getPage();
	
	float getScore();
	
	int getMatchesCount();
	
	MatchFragment[] getBestFragments();
	
}
