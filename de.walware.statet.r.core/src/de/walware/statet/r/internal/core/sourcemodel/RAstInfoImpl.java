/*******************************************************************************
 * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import de.walware.statet.r.core.rsource.ast.RAstInfo;
import de.walware.statet.r.core.rsource.ast.SourceComponent;


public class RAstInfoImpl extends RAstInfo {
	
	
	public RAstInfoImpl(final int level, final long stamp) {
		super(level, stamp);
	}
	
	public RAstInfoImpl(final int level, final RAstInfo ast) {
		super(level, ast.stamp);
		fRoot = ast.getRootNode();
	}
	
	public void set(final SourceComponent root) {
		fRoot = root;
	}
	
}
