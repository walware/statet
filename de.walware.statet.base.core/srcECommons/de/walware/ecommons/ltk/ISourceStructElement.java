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

import java.util.List;

import org.eclipse.jface.text.IRegion;


/**
 * Represents Source structure instead of model structure
 */
public interface ISourceStructElement extends IModelElement {
	
	
	IRegion getNameSourceRange();
	IRegion getSourceRange();
	
	ISourceStructElement getSourceParent();
	boolean hasSourceChildren(Filter<? extends IModelElement> filter);
	List<? extends ISourceStructElement> getSourceChildren(Filter<? extends IModelElement> filter);
	
}
