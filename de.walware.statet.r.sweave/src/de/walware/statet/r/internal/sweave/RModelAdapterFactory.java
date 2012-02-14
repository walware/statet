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

package de.walware.statet.r.internal.sweave;

import org.eclipse.core.runtime.IAdapterFactory;

import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.sweave.text.RweaveChunkHeuristicScanner;


public class RModelAdapterFactory implements IAdapterFactory {
	
	
	private static final Class<?>[] ADAPTERS = new Class<?>[] {
		RHeuristicTokenScanner.class,
	};
	
	
	public RModelAdapterFactory() {
	}
	
	
	@Override
	public Class<?>[] getAdapterList() {
		return ADAPTERS;
	}
	
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (RHeuristicTokenScanner.class.equals(adapterType)) {
			return new RweaveChunkHeuristicScanner();
		}
		return null;
	}
	
}
