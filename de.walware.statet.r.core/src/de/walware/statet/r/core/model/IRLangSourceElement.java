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

import de.walware.ecommons.ltk.ISourceStructElement;


public interface IRLangSourceElement extends IRLangElement, ISourceStructElement {
	
	
	boolean hasModelChildren(Filter<? super IRLangSourceElement> filter);
	List<? extends IRLangSourceElement> getModelChildren(Filter<? extends IRLangSourceElement> filter);
	
	boolean hasSourceChildren(Filter<? super IRLangSourceElement> filter);
	List<? extends IRLangSourceElement> getSourceChildren(Filter<? super IRLangSourceElement> filter);
	
}
