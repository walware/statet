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

package de.walware.statet.r.core.model;

import java.util.List;

import de.walware.ecommons.ltk.IModelElement;


public interface IFrameInSource extends IFrame {
	
	
	public List<? extends IElementAccess> getAllAccessOfElement(final String name);
	
	public List<? extends IFrameInSource> getUnderneathEnvirs();
	
	public IModelElement getModelElement();
	
}
