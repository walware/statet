/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.rtm.rtdata.types;


public class RColor extends RTypedExpr {
	
	
	public RColor(final String type, final String expr) {
		super(type, expr);
	}
	
	
	@Override
	public int hashCode() {
		return fExpr.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		return (obj instanceof RColor && fExpr.equals(((RColor) obj).fExpr));
	}
	
}
