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
