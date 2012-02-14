/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
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
import de.walware.ecommons.ltk.ISourceStructElement;


public interface IRLangSourceElement extends IRLangElement, ISourceStructElement {
	
	
	@Override
	boolean hasModelChildren(IModelElement.Filter filter);
	@Override
	List<? extends IRLangSourceElement> getModelChildren(IModelElement.Filter filter);
	
	@Override
	boolean hasSourceChildren(IModelElement.Filter filter);
	@Override
	List<? extends IRLangSourceElement> getSourceChildren(IModelElement.Filter filter);
	
}
