/*******************************************************************************
 * Copyright (c) 2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rhelp;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;

import de.walware.statet.r.internal.core.rhelp.analyzer.StandardAnalyzer;


public interface IREnvIndex {
	
	/**
	 * Lucene field name for exact doc type.
	 */
	String DOCTYPE_FIELD_NAME = "doctype"; //$NON-NLS-1$
	
	/**
	 * Lucene field name for exact package name.
	 */
	String PACKAGE_FIELD_NAME = "pkg"; //$NON-NLS-1$
	
	/**
	 * Lucene field name for exact page name.
	 */
	String PAGE_FIELD_NAME = "page"; //$NON-NLS-1$
	
	/**
	 * Lucene field name for exact page alias.
	 */
	String ALIAS_FIELD_NAME = "alias"; //$NON-NLS-1$
	
	/**
	 * Lucene field name for analyzed page alias.
	 */
	String ALIAS_TXT_FIELD_NAME = "alias.txt"; //$NON-NLS-1$
	
	/**
	 * Lucene field name for analyzed page title.
	 */
	String TITLE_TXT_FIELD_NAME = "title.txt"; //$NON-NLS-1$
	
	/**
	 * Lucene field name for exact help keyword.
	 */
	String KEYWORD_FIELD_NAME = "keyword"; //$NON-NLS-1$
	
	/**
	 * Lucene field name for analyzed help concept.
	 */
	String CONCEPT_TXT_FIELD_NAME = "concept.txt"; //$NON-NLS-1$
	
	/**
	 * Lucene field name for analyzed page text.
	 */
	String DOC_TXT_FIELD_NAME = "doc.txt"; //$NON-NLS-1$
	
	/**
	 * Lucene field name for analyzed page text.
	 */
	String DOC_HTML_FIELD_NAME = "doc.html"; //$NON-NLS-1$
	
	/**
	 * Lucene field name for exact package priority.
	 */
	String PKG_PRIORITY_FIELD_NAME = "priority"; //$NON-NLS-1$ 
	
	/**
	 * Lucene field name for analyzed description.
	 */
	String DESCRIPTION_TXT_FIELD_NAME = "descr.txt"; //$NON-NLS-1$ 
	
	/**
	 * Lucene field name for analyzed authors.
	 */
	String AUTHORS_TXT_FIELD_NAME = "authors.txt"; //$NON-NLS-1$ 
	
	/**
	 * Lucene field name for analyzed package maintainer.
	 */
	String MAINTAINER_TXT_FIELD_NAME = "maintainer.txt"; //$NON-NLS-1$
	
	/**
	 * Lucene field name for analyzed package maintainer.
	 */
	String URL_TXT_FIELD_NAME = "url.txt"; //$NON-NLS-1$
	
	/**
	 * Lucene field name for analyzed authors.
	 */
	String EXAMPLES_TXT_FIELD_NAME = "examples.txt"; //$NON-NLS-1$ 
	
	
	String PACKAGE_DOC_TYPE = "pkgdescr"; //$NON-NLS-1$
	
	String PAGE_DOC_TYPE = "helppage"; //$NON-NLS-1$
	
	
	Version USED_VERSION = Version.LUCENE_30;
	
	Analyzer WRITE_ANALYZER = new StandardAnalyzer(USED_VERSION, true);
	
	Analyzer READ_ANALYZER = new StandardAnalyzer(USED_VERSION, false);
	
}
