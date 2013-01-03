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

package de.walware.statet.r.core.rdoc;


/**
 * Provides definition and util-method for tokens of the R-documentations.
 * 'Tokes' means tokens according to R-documentation definition, they defines not
 * directly <code>Token</code>, implementions of <code>IToken</code>.  
 */
public class RdTags {
	
	//---- main-section-tags --------------------------------------------------
	public static final String ENCODING = "\\encoding"; //$NON-NLS-1$
	public static final String NAME = "\\name"; //$NON-NLS-1$
	public static final String DOCTYPE = "\\docType";  // dataset //$NON-NLS-1$
	public static final String ALIAS = "\\alias"; //$NON-NLS-1$
	public static final String TITLE = "\\title"; //$NON-NLS-1$
	public static final String DESCRIPTION = "\\description"; //$NON-NLS-1$
	public static final String SYNOPSIS = "\\synopsis"; //$NON-NLS-1$
	public static final String USAGE = "\\usage"; //$NON-NLS-1$
	public static final String ARGUMENTS = "\\arguments"; //$NON-NLS-1$
	public static final String DETAILS = "\\details"; //$NON-NLS-1$
	public static final String VALUE = "\\value"; //$NON-NLS-1$
	public static final String FORMAT = "\\format";  // dataset //$NON-NLS-1$
	public static final String SOURCE = "\\source";  // dataset //$NON-NLS-1$
	public static final String REFERENCES = "\\references"; //$NON-NLS-1$
	public static final String NOTE = "\\note"; //$NON-NLS-1$
	public static final String AUTHOR = "\\author"; //$NON-NLS-1$
	public static final String SEEALSO = "\\seealso"; //$NON-NLS-1$
	public static final String EXAMPLES = "\\examples"; //$NON-NLS-1$
	public static final String KEYWORD = "\\keyword"; //$NON-NLS-1$
	public static final String USER_SECTION = "\\section"; //$NON-NLS-1$
	public static final String CONCEPT = "\\concept"; //$NON-NLS-1$
	
	public static final String[] MAIN_SECTIONS = {
		ENCODING,
		NAME, DOCTYPE, ALIAS, TITLE, DESCRIPTION, 
		SYNOPSIS, USAGE, ARGUMENTS, DETAILS, VALUE,
		FORMAT, SOURCE, 
		REFERENCES, NOTE, AUTHOR, SEEALSO, EXAMPLES, KEYWORD,
		USER_SECTION, CONCEPT
	};
	
	//---- common
//	public static final String CODE = "\\code";
//	public static final String LINK = "\\link";
//	public static final String URL = "\\url";
//	public static final String EMAIL = "\\email";
	
	public static final String[] TEXT_MARKUP_TAGs = new String[] {
			"\\emph", "\\strong", "\\bold", "\\sQuote", "\\dQuote", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"\\code", "\\preformatted", "\\kbd", "\\samp",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"\\pkg", "\\file", "\\email", "\\url", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"\\var", "\\env", "\\option", "\\comand",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"\\dfn", "\\dfn", "\\cite", "\\acronym", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	};
	public static final String[] LIST_TABLE_TAGS = new String[] {
			"\\itemize", "\\enumerate", "\\describe", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			//"\\item",
			"\\tabular", "\\tab", //$NON-NLS-1$ //$NON-NLS-2$
	};
	public static final String[] MATH_TAGS = new String[] {
			"\\eqn", "\\deqn"  //$NON-NLS-1$ //$NON-NLS-2$
	};
	public static final String[] INSERTIONS = new String[] {
			"\\R", "\\dot", "\\ldot", "\\cr" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	};
	public static final String[] ESCAPED_CHARS = new String[] {
			"\\\\", "\\%", "\\{", "\\}", "\\&", "\\$", "\\#", "\\_",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
	};
	public static final String[] ESCAPED_CHARS_INVERBATIM = new String[] {
			"\\\\", "\\%", "\\{", "\\}", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	};
	
	//---- sub-section-tags
	// usage
	public static final String USAGE_METHOD = "\\method"; //$NON-NLS-1$
	// arguments, value
	public static final String ITEM = "\\item"; //$NON-NLS-1$
	// 
	public static final String EXAMPLES_DONTRUN = "\\dontrun"; //$NON-NLS-1$
	public static final String EXAMPLES_DONTSHOW = "\\dontshow"; //$NON-NLS-1$
	
	public static final String[] SUB_SECTIONS = { 
			USAGE_METHOD, ITEM };
	public static final String[] SUB_SECTIONS_INVERBATIM = { 
			EXAMPLES_DONTRUN, EXAMPLES_DONTSHOW	};
	
	public static final String[] BRACKETS = { "{", "}" }; //$NON-NLS-1$ //$NON-NLS-2$
	
}
