/*=============================================================================#
 # Copyright (c) 2009-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.model;

import java.util.List;

import de.walware.ecommons.ltk.core.model.IModelElement;


// TODO extend IRElement? return as model parent of elements
public interface IRFrame {
	
	/** Simple project */
	static final int PROJECT = 1;
	/** Package project */
	static final int PACKAGE = 2;
	
	static final int EXPLICIT = 4;
	
	/** Function frame */
	static final int FUNCTION = 5;
	/** Class context (not a real frame) */
	static final int CLASS = 6;
	
	
	int getFrameType();
//	String getFrameName();
	/** Combination of frametype and name */
	String getFrameId();
	RElementName getElementName();
	
	List<? extends IRElement> getModelElements();
	boolean hasModelChildren(final IModelElement.Filter filter);
	List<? extends IRLangElement> getModelChildren(final IModelElement.Filter filter);
	List<? extends IRFrame> getPotentialParents();
	
}
