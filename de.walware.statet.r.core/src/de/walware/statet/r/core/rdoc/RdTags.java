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

package de.walware.statet.r.core.rdoc;


/**
 * Provides definition and util-method for tokens of the R-documentations.
 * 'Tokes' means tokens according to R-documentation definition, they defines not
 * directly <code>Token</code>, implementions of <code>IToken</code>.  
 *
 * @author Stephan Wahlbrink
 */
public class RdTags {

	//---- main-section-tags --------------------------------------------------
	public static final String NAME = "\\name";
	public static final String DOCTYPE = "\\docType";  // dataset
	public static final String ALIAS = "\\alias";
	public static final String TITLE = "\\title";
	public static final String DESCRIPTION = "\\description";
	public static final String SYNOPSIS = "\\synopsis";
	public static final String USAGE = "\\usage";
	public static final String ARGUMENTS = "\\arguments";
	public static final String DETAILS = "\\details";
	public static final String VALUE = "\\value";
	public static final String FORMAT = "\\format";  // dataset
	public static final String SOURCE = "\\source";  // dataset
	public static final String REFERENCES = "\\references";
	public static final String NOTE = "\\note";
	public static final String AUTHOR = "\\author";
	public static final String SEEALSO = "\\seealso";
	public static final String EXAMPLES = "\\examples";
	public static final String KEYWORD = "\\keyword";
	public static final String USER_SECTION = "\\section";
	public static final String CONCEPT = "\\concept";
	
	public static final String[] MAIN_SECTIONS = {
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
			"\\emph", "\\strong", "\\bold", "\\sQuote", "\\dQuote",
			"\\code", "\\preformatted", "\\kbd", "\\samp", 
			"\\pkg", "\\file", "\\email", "\\url",
			"\\var", "\\env", "\\option", "\\comand", 
			"\\dfn", "\\dfn", "\\cite", "\\acronym",
	};
	public static final String[] LIST_TABLE_TAGS = new String[] {
			"\\itemize", "\\enumerate", "\\describe",
			//"\\item",
			"\\tabular", "\\tab",
	};
	public static final String[] MATH_TAGS = new String[] {
			"\\eqn", "\\deqn" 
	};
	public static final String[] INSERTIONS = new String[] {
			"\\R", "\\dot", "\\ldot", "\\cr"
	};
	public static final String[] ESCAPED_CHARS = new String[] {
			"\\\\", "\\%", "\\{", "\\}", "\\&", "\\$", "\\#", "\\_", 
	};
	public static final String[] ESCAPED_CHARS_INVERBATIM = new String[] {
			"\\\\", "\\%", "\\{", "\\}",
	};
	
	//---- sub-section-tags
	// usage
	public static final String USAGE_METHOD = "\\method";
	// arguments, value
	public static final String ITEM = "\\item";
	// 
	public static final String EXAMPLES_DONTRUN = "\\dontrun";
	public static final String EXAMPLES_DONTSHOW = "\\dontshow";
	
	public static final String[] SUB_SECTIONS = { 
			USAGE_METHOD, ITEM };
	public static final String[] SUB_SECTIONS_INVERBATIM = { 
			EXAMPLES_DONTRUN, EXAMPLES_DONTSHOW	};
	
	public static final String[] BRACKETS = { "{", "}" };
	
}
