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

package de.walware.statet.rtm.ftable.core;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import de.walware.statet.rtm.base.core.AbstractRCodeGenerator;
import de.walware.statet.rtm.ftable.FTable;
import de.walware.statet.rtm.ftable.FTablePackage.Literals;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;


public class FTableRCodeGen extends AbstractRCodeGenerator {
	
	
	private final String fTableVar = "ft"; //$NON-NLS-1$
	
	
	@Override
	public void generate(final EObject root) {
		reset();
//		addRequirePackage("stats"); //$NON-NLS-1$
		if (root == null) {
			return;
		}
		if (root.eClass() != Literals.FTABLE) {
			throw new IllegalArgumentException("root: " + root.eClass().getName()); //$NON-NLS-1$
		}
		appendNewLine();
		genRCode((FTable) root);
		
		final FunBuilder printFun = appendFun("openInEditor"); //$NON-NLS-1$
		printFun.append("elementName", fTableVar); //$NON-NLS-1$
		printFun.close();
		appendNewLine();
	}
	
	public void genRCode(final FTable table) {
		appendAssign(fTableVar);
		{	
			final FunBuilder fun = appendFun("ftable"); //$NON-NLS-1$
			appendData(fun, table);
			appendVars(fun, "col.vars", table.getColVars()); //$NON-NLS-1$
			appendVars(fun, "row.vars", table.getRowVars()); //$NON-NLS-1$
			fun.close();
			appendNewLine();
		}
	}
	
	private void appendData(final FunBuilder fun, final FTable obj) {
		if (obj.getData() != null) {
			String expr = obj.getData().getExpr();
			final RTypedExpr dataFilter = obj.getDataFilter();
			if (dataFilter != null) {
				expr += dataFilter.getExpr();
			}
			fun.append("x", expr); //$NON-NLS-1$
		}
	}
	
	private void appendVars(final FunBuilder fun, final String argName, final List<RTypedExpr> exprs) {
		if (exprs.isEmpty()) {
			return;
		}
		fun.appendEmpty(argName);
		appendExprsC(exprs, QUOTE_PROCESSOR);
	}
	
}
