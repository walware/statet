/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk;

import org.eclipse.jface.text.IDocumentExtension4;


/**
 * Container for model information of an source unit
 */
public interface ISourceUnitModelInfo {
	
	
	/**
	 * Modification stamp, same as
	 *   {@link IDocumentExtension4#getModificationStamp()}
	 * 
	 * @return the stamp
	 */
	public long getStamp();
	
	/**
	 * The AST used to create this info.
	 * The AST has the same stamp as this info.
	 * 
	 * @return the AST information
	 */
	public AstInfo getAst();
	
	/**
	 * The source unit as source structure model element.
	 * 
	 * @return the element
	 */
	public ISourceStructElement getSourceElement();
	
}
