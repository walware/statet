/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.text;

import java.util.List;

import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.ltk.IModelElement;


/**
 * Represents Source structure instead of model structure
 */
public interface ISourceStructElement extends IModelElement {
	
	
	public IRegion getNameSourceRange();
	public IRegion getSourceRange();
	
	public List<? extends ISourceStructElement> getChildren(Filter filter);
	
}
