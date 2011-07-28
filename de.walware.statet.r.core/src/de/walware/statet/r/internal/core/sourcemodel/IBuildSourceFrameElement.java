/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.List;

import de.walware.statet.r.core.model.IRLangSourceElement;


public interface IBuildSourceFrameElement extends IRLangSourceElement {
	
	
	public BuildSourceFrame getBuildFrame();
	
	public void setSourceChildren(final List<? extends IRLangSourceElement> children);
	
}
