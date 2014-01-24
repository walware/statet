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


public class RExpr {
	
	
	protected final String fExpr;
	
	
	public RExpr(final String expr) {
		if (expr == null) {
			throw new NullPointerException("expr"); //$NON-NLS-1$
		}
		fExpr = expr;
	}
	
	
	public String getExpr() {
		return fExpr;
	}
	
	
	@Override
	public int hashCode() {
		return fExpr.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RTypedExpr)) {
			return false;
		}
		final RTypedExpr other = (RTypedExpr) obj;
		return (fExpr.equals(other.fExpr));
	}
	
}
