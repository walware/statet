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

package de.walware.statet.r.ui.text.rd;


/**
 * Keys of TextTokens recognized by text-parser (syntax-highlighting)
 *
 * @author Stephan Wahlbrink
 */
public interface IRdTextTokens {
	
	public static final String ROOT = "text_Rd_";
	
	public static final String DEFAULT = ROOT+"rdDefault";
	public static final String VERBATIM = ROOT+"rdVerbatim";
	public static final String COMMENT = ROOT+"rdComment";
	public static final String PLATFORM_SPECIF = ROOT+"rdPlatformSpecif";
	
	public static final String SECTION_TAG = ROOT+"rdSectionTag";
	public static final String SUBSECTION_TAG = ROOT+"rdSubSectionTag";
	public static final String OTHER_TAG = ROOT+"rdOtherTag";
	public static final String UNLISTED_TAG = ROOT+"rdUnlistedTag";
	
	public static final String BRACKETS = "rdBrackets";
	
	public static final String TASK_TAG = ROOT+"taskTag";
	
}
