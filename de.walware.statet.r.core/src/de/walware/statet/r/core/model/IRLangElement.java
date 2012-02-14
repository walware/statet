/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.model;

import java.util.List;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceElement;


public interface IRLangElement extends IRElement, ISourceElement {
	
	
	@Override
	boolean hasModelChildren(IModelElement.Filter filter);
	@Override
	List<? extends IRLangElement> getModelChildren(IModelElement.Filter filter);
	
}
