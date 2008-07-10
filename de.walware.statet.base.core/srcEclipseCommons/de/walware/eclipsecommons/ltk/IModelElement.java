/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ltk;

import org.eclipse.core.runtime.IAdaptable;


/**
 * 
 */
public interface IModelElement extends IAdaptable {
	
	
	public interface Filter {
		
		boolean include(IModelElement element);
		
	}
	
	
	public String getModelTypeId();
	
	public String getElementName();
	public ISourceUnit getSourceUnit();
	
	public IModelElement getParent();
	public boolean hasChildren(Filter filter);
	public IModelElement[] getChildren(Filter filter);
	
}
