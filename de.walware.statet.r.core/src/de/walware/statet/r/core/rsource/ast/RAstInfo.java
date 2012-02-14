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

package de.walware.statet.r.core.rsource.ast;

import de.walware.ecommons.ltk.AstInfo;


public class RAstInfo extends AstInfo {
	
	
	protected SourceComponent fRoot;
	
	
	public RAstInfo(final int level, final long stamp, final SourceComponent root) {
		super(level, stamp);
		fRoot = root;
	}
	
	public RAstInfo(final int level, final long stamp) {
		super(level, stamp);
	}
	
	
	@Override
	public SourceComponent getRootNode() {
		return fRoot;
	}
	
}
