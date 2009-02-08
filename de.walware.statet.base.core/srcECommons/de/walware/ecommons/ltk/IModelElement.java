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

package de.walware.ecommons.ltk;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;


/**
 * Interface for all types of elements in the ltk model
 */
public interface IModelElement extends IAdaptable {
	
	
	public interface Filter {
		
		boolean include(IModelElement element);
		
	}
	
	
	public static final int MASK_C1 =            0xf00;
	public static final int MASK_C2 =            0xff0;
	public static final int MASK_C3 =            0xfff;
	
	public static final int C1_BUNDLE =          0x100;
	public static final int C1_SOURCE =          0x200;
	public static final int C1_IMPORT =          0x300;
	public static final int C1_CLASS =           0x400;
	public static final int C1_METHOD =          0x500;
	public static final int C1_VARIABLE =        0x600;
	
	public static final int C2_SOURCE_FILE =     C1_SOURCE | 0x10;
	public static final int C2_SOURCE_CHUNK =    C1_SOURCE | 0x80;
	
	
	public static final List<IModelElement> NO_CHILDREN = Arrays.asList(new IModelElement[0]);
	
	
	public String getModelTypeId();
	
	public int getElementType();
	public IElementName getElementName();
	public String getId();
	public ISourceUnit getSourceUnit();
	
	public boolean exists();
	public boolean isReadOnly();
	
	public IModelElement getParent();
	public boolean hasChildren(Filter filter);
	public List<? extends IModelElement> getChildren(Filter filter);
	
	
}
