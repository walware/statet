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

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.LinkedHashMap;
import java.util.Map;

import de.walware.ecommons.ltk.ISourceStructElement;

import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.rsource.ast.RAstInfo;


public class RSourceInfo implements IRModelInfo {
	
	
	private final RAstInfo fAst;
	private final LinkedHashMap<String, Envir> fScope;
	private final ISourceStructElement fSourceElement;
	
	
	RSourceInfo(final RAstInfo ast, final LinkedHashMap<String, Envir> scopes, final ISourceStructElement unitElement) {
		fAst = ast;
		fScope = scopes;
		fSourceElement = unitElement;
	}
	
	public final long getStamp() {
		return fAst.stamp;
	}
	
	public final RAstInfo getAst() {
		return fAst;
	}
	
	public ISourceStructElement getSourceElement() {
		return fSourceElement;
	}
	
	public final Map<String, Envir> getSourceFrames() {
		return fScope;
	}
	
}
