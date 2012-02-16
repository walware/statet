/*******************************************************************************
 * Copyright (c) 2011-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.sweave.model;

import org.eclipse.jface.text.IRegion;

import de.walware.ecommons.ltk.ISourceStructElement;

import de.walware.statet.r.core.model.RChunkElement;
import de.walware.statet.r.core.model.RElementName;


public class TexRChunkElement extends RChunkElement {
	
	
	public TexRChunkElement(final ISourceStructElement parent, final RChunkNode astNode,
			final RElementName name, final IRegion nameRegion) {
		super(parent, astNode, name, nameRegion);
	}
	
	
	@Override
	protected RChunkNode getNode() {
		return (RChunkNode) super.getNode();
	}
	
	@Override
	protected Object getSourceComponents() {
		return getNode().fRSources;
	}
	
}
