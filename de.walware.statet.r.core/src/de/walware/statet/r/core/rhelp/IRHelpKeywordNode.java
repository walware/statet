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

package de.walware.statet.r.core.rhelp;

import java.util.List;


/**
 * A node of the R help keywords tree.
 */
public interface IRHelpKeywordNode {
	
	
	/**
	 * @return the description
	 */
	String getDescription();
	
	/**
	 * @return a list with nested keywords
	 */
	List<IRHelpKeyword> getNestedKeywords();
	
	/**
	 * @return the nested keywords if exists
	 */
	IRHelpKeyword getNestedKeyword(String keyword);
	
}
