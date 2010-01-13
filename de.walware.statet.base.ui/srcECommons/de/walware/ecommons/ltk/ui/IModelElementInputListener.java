/*******************************************************************************
 * Copyright (c) 2007-2010 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.ui;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.IModelElementDelta;


/**
 * 
 */
public interface IModelElementInputListener {
	
	
	/**
	 * The element of the provider has changed.
	 * 
	 * Directly called while changing the input.
	 * For longer tasks, wait for {@link #elementInitialInfo(IModelElement)}.
	 */
	public void elementChanged(IModelElement element);
	
	/**
	 * First detail info for the element.
	 */
	public void elementInitialInfo(IModelElement element);
	
	/**
	 * Detail info changed.
	 */
	public void elementUpdatedInfo(IModelElement element, IModelElementDelta delta);
	
}
