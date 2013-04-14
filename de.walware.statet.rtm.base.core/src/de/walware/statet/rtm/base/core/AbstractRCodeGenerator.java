/*******************************************************************************
 * Copyright (c) 2012-2013 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.rtm.base.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import de.walware.ecommons.collections.ConstList;

import de.walware.statet.rtm.rtdata.types.RTypedExpr;


public abstract class AbstractRCodeGenerator {
	
	
	protected static final RTypedExpr R_NUM_ZERO_EXPR = new RTypedExpr(RTypedExpr.R, "0"); //$NON-NLS-1$
	
	
	protected static String quoteChar(final String s) {
		final StringBuilder result = new StringBuilder(s.length());
		result.append('"');
		for (int i = 0; i < s.length(); ) {
			final char c = s.charAt(i++);
			switch (c) {
			case '\\':
			case '\'':
			case '"':
				result.append('\\');
				result.append(c);
				continue;
			default:
				result.append(c);
				continue;
			}
		}
		result.append('"');
		return result.toString();
	}
	
	
	protected static interface IRExprProcessor {
		
		String getValue(RTypedExpr expr);
		
	}
	
	protected static final IRExprProcessor DIRECT_PROCESSOR = new IRExprProcessor() {
		
		@Override
		public String getValue(final RTypedExpr expr) {
			return expr.getExpr();
		}
		
	};
	
	protected static final IRExprProcessor TEXT_PROCESSOR = new IRExprProcessor() {
		
		@Override
		public String getValue(final RTypedExpr expr) {
			return quoteChar(expr.getExpr());
		}
		
	};
	
	protected static final IRExprProcessor QUOTE_PROCESSOR = new IRExprProcessor() {
		
		@Override
		public String getValue(final RTypedExpr expr) {
			return quoteChar(expr.getExpr());
		}
		
	};
	
	
	protected class FunBuilder {
		
		
		private final int fOffset;
		private final int fEmtpyOffset;
		
		
		public FunBuilder(final int offset, final String funName) {
			fOffset = offset;
			fBuilder.append(funName);
			fBuilder.append('(');
			fEmtpyOffset = fBuilder.length();
		}
		
		public boolean isEmpty() {
			return (fBuilder.length() == fEmtpyOffset);
		}
		
		public boolean append(final String argName, final String argValue) {
			if (argValue == null) {
				return false;
			}
			doAppendArg(argName, argValue);
			return true;
		}
		
		public boolean append(final String argName, final String argValue, final boolean appendEmpty) {
			if (argValue == null && !appendEmpty) {
				return false;
			}
			doAppendArg(argName, argValue);
			return true;
		}
		
		public boolean appendEmpty(final String argName) {
			doAppendArg(argName, null);
			return true;
		}
		
		public boolean appendExpr(final String argName, final RTypedExpr argValue) {
			if (argValue == null) {
				return false;
			}
			doAppendArg(argName, getRExprPRocessor(argValue.getTypeKey()).getValue(argValue));
			return true;
		}
		
		public boolean appendExpr(final String argName, final RTypedExpr argValue,
				final String requiredType) {
			if (argValue == null || argValue.getTypeKey() != requiredType) {
				return false;
			}
			doAppendArg(argName, getRExprPRocessor(argValue.getTypeKey()).getValue(argValue));
			return true;
		}
		
		public boolean appendExpr(final String argName, final RTypedExpr argValue,
				final Collection<String> requiredTypes) {
			if (argValue == null || !requiredTypes.contains(argValue.getTypeKey())) {
				return false;
			}
			doAppendArg(argName, getRExprPRocessor(argValue.getTypeKey()).getValue(argValue));
			return true;
		}
		
		public FunBuilder appendFun(final String argName, final String funName) {
			final int offset = fBuilder.length();
			doAppendArg(argName, null);
			return new FunBuilder(offset, funName);
		}
		
		private void doAppendArg(final String argName, final String argValue) {
			if (!isEmpty()) {
				fBuilder.append(", "); //$NON-NLS-1$
			}
			if (argName != null) {
				fBuilder.append(argName);
				fBuilder.append(fArgAssign);
			}
			if (argValue != null) {
				fBuilder.append(argValue);
			}
		}
		
		public void close() {
			fBuilder.append(")"); //$NON-NLS-1$
		}
		
		public void closeOrRemove() {
			if (isEmpty()) {
				fBuilder.delete(fOffset, fBuilder.length());
				return;
			}
			fBuilder.append(")"); //$NON-NLS-1$
		}
		
	}
	
	
	protected final List<String> fRequiredPkgs = new ArrayList<String>();
	protected final StringBuilder fBuilder = new StringBuilder();
	
	private final String fNewLine = "\n";
	private final String fMainAssign = " <- ";
	private final String fArgAssign = " = ";
	private final int fIndent = 0;
	
	
	public AbstractRCodeGenerator() {
	}
	
	
	protected void reset() {
		fRequiredPkgs.clear();
		fBuilder.setLength(0);
	}
	
	protected void addRequirePackage(final String pkgName) {
		if (!fRequiredPkgs.contains(pkgName)) {
			fRequiredPkgs.add(pkgName);
			
			final FunBuilder fun = appendFun("library"); //$NON-NLS-1$
			fun.append(null, pkgName);
			fun.close();
			appendNewLine();
		}
	}
	
	protected void appendNewLine() {
		fBuilder.append(fNewLine);
	}
	
	protected void appendAssign(final String to) {
		fBuilder.append(to);
		fBuilder.append(fMainAssign);
	}
	
	protected FunBuilder appendFun(final String funName) {
		return new FunBuilder(fBuilder.length(), funName);
	}
	
	protected void appendExprList(final List<? extends RTypedExpr> list,
			final String op, final String empty) {
		appendExprList(list, DIRECT_PROCESSOR, op, empty);
	}
	
	protected void appendExprList(final List<? extends RTypedExpr> list,
			final IRExprProcessor processor, final String op, final String empty) {
		final int offset = fBuilder.length();
		for (int i = 0; i < list.size(); i++) {
			final RTypedExpr expr = list.get(i);
			if (expr.getTypeKey() == RTypedExpr.MAPPED) {
				if (fBuilder.length() > offset) {
					fBuilder.append(op);
				}
				fBuilder.append(processor.getValue(list.get(i)));
			}
		}
		if (fBuilder.length() == offset && empty != null) {
			fBuilder.append(empty);
		}
	}
	
	protected void appendExprsC(final List<? extends RTypedExpr> list,
			final IRExprProcessor processor) {
		fBuilder.append("c("); //$NON-NLS-1$
		appendExprList(list, processor, ", ", null); //$NON-NLS-1$
		fBuilder.append(")"); //$NON-NLS-1$
	}
	
	protected IRExprProcessor getRExprPRocessor(final String typeKey) {
		if (typeKey == RTypedExpr.CHAR) {
			return TEXT_PROCESSOR;
		}
		return DIRECT_PROCESSOR;
	}
	
	
	public abstract void generate(EObject root);
	
	
	public List<String> getRequiredPkgs() {
		return new ConstList<String>(fRequiredPkgs);
	}
	
	public String getRCode() {
		return fBuilder.toString();
	}
	
}
