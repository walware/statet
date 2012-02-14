/*******************************************************************************
 * Copyright (c) 2010-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rhelp;


/**
 * A R help keyword.
 */
public interface IRHelpKeyword extends IRHelpKeywordNode {
	
	
	/**
	 * A group of keyword
	 */
	interface Group extends IRHelpKeywordNode {
		
		
		/**
		 * @return the name of the group
		 */
		String getLabel();
		
	}
	
	
	/**
	 * @return the keyword
	 */
	String getKeyword();
	
}
