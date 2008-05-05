/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.rmodel;

import java.util.LinkedHashMap;
import java.util.Map;

import de.walware.eclipsecommons.ltk.AstInfo;

import de.walware.statet.r.core.rmodel.IRModelInfo;
import de.walware.statet.r.core.rsource.ast.RAstNode;


public class RSourceInfo implements IRModelInfo {
	
	
	public final AstInfo<RAstNode> ast;
	public final LinkedHashMap<String, Scope> scopes;
	
	
	RSourceInfo(final AstInfo<RAstNode> ast, final LinkedHashMap<String, Scope> scopes) {
		this.ast = ast;
		this.scopes = scopes;
	}
	
	public final long getStamp() {
		return ast.stamp;
	}
	
	public final AstInfo<RAstNode> getAst() {
		return ast;
	}
	
	public final Map<String, Scope> getAllScopes() {
		return scopes;
	}
	
}
