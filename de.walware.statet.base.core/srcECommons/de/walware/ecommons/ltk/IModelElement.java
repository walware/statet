/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;


/**
 * Interface for all types of elements in the ltk model
 */
public interface IModelElement extends IAdaptable {
	
	
	interface Filter<T extends IModelElement> {
		
		boolean include(T element);
		
	}
	
	
	static final int MASK_C1 =            0xf00;
	static final int MASK_C2 =            0xff0;
	static final int MASK_C3 =            0xfff;
	
	static final int C1_BUNDLE =          0x100;
	static final int C1_SOURCE =          0x200;
	static final int C1_IMPORT =          0x300;
	static final int C1_CLASS =           0x400;
	static final int C1_METHOD =          0x500;
	static final int C1_VARIABLE =        0x600;
	
	static final int C2_SOURCE_FILE =     C1_SOURCE | 0x10;
	static final int C2_SOURCE_CHUNK =    C1_SOURCE | 0x80;
	
	
	static final List<IModelElement> NO_CHILDREN = Collections.emptyList();
	
	
	String getModelTypeId();
	
	int getElementType();
	IElementName getElementName();
	String getId();
	
	boolean exists();
	boolean isReadOnly();
	
	IModelElement getModelParent();
	boolean hasModelChildren(Filter<? super IModelElement> filter); // can also be used to visit children
	List<? extends IModelElement> getModelChildren(Filter<? super IModelElement> filter);
	
	
}
