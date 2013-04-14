
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
