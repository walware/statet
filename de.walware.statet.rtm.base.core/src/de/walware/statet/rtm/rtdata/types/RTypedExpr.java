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


public class RTypedExpr extends RExpr {
	
	
	public static final String R = "r"; //$NON-NLS-1$
	public static final String MAPPED = "map"; //$NON-NLS-1$
	public static final String CHAR = "chr"; //$NON-NLS-1$
	
	
	private final String fTypeKey;
	
	
	public RTypedExpr(final String typeKey, final String expr) {
		super(expr);
		
		fTypeKey = typeKey;
	}
	
	
	public String getTypeKey() {
		return fTypeKey;
	}
	
	
	@Override
	public int hashCode() {
		return fTypeKey.hashCode() * 17 + fExpr.hashCode();
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
		return (fTypeKey.equals(other.fTypeKey) && fExpr.equals(other.fExpr));
	}
	
	@Override
	public String toString() {
		return fTypeKey + ':' + fExpr;
	}
	
}
