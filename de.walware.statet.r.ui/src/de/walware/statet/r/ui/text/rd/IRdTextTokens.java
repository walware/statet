/*******************************************************************************
 * Copyright (c) 2005-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.text.rd;


/**
 * Keys of TextTokens recognized by text-parser (syntax-highlighting)
 */
public interface IRdTextTokens {
	
	public static final String ROOT = "text_Rd_"; //$NON-NLS-1$
	
	public static final String DEFAULT = ROOT+"rdDefault"; //$NON-NLS-1$
	public static final String VERBATIM = ROOT+"rdVerbatim"; //$NON-NLS-1$
	public static final String COMMENT = ROOT+"rdComment"; //$NON-NLS-1$
	public static final String PLATFORM_SPECIF = ROOT+"rdPlatformSpecif"; //$NON-NLS-1$
	
	public static final String SECTION_TAG = ROOT+"rdSectionTag"; //$NON-NLS-1$
	public static final String SUBSECTION_TAG = ROOT+"rdSubSectionTag"; //$NON-NLS-1$
	public static final String OTHER_TAG = ROOT+"rdOtherTag"; //$NON-NLS-1$
	public static final String UNLISTED_TAG = ROOT+"rdUnlistedTag"; //$NON-NLS-1$
	
	public static final String BRACKETS = "rdBrackets"; //$NON-NLS-1$
	
	public static final String TASK_TAG = ROOT+"taskTag"; //$NON-NLS-1$
	
}
