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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.OperationCanceledException;

import de.walware.eclipsecommons.ltk.AstInfo;

import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rmodel.ArgsDefinition;
import de.walware.statet.r.core.rmodel.IRModelInfo;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.core.rmodel.IScope;
import de.walware.statet.r.core.rmodel.RCoreFunctions;
import de.walware.statet.r.core.rsource.IRSourceConstants;
import de.walware.statet.r.core.rsource.ast.Arithmetic;
import de.walware.statet.r.core.rsource.ast.Assignment;
import de.walware.statet.r.core.rsource.ast.CForLoop;
import de.walware.statet.r.core.rsource.ast.CIfElse;
import de.walware.statet.r.core.rsource.ast.CLoopCommand;
import de.walware.statet.r.core.rsource.ast.CRepeatLoop;
import de.walware.statet.r.core.rsource.ast.CWhileLoop;
import de.walware.statet.r.core.rsource.ast.Dummy;
import de.walware.statet.r.core.rsource.ast.FCall;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.Help;
import de.walware.statet.r.core.rsource.ast.Logical;
import de.walware.statet.r.core.rsource.ast.Model;
import de.walware.statet.r.core.rsource.ast.NSGet;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.NullConst;
import de.walware.statet.r.core.rsource.ast.NumberConst;
import de.walware.statet.r.core.rsource.ast.Power;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.RAstVisitor;
import de.walware.statet.r.core.rsource.ast.Relational;
import de.walware.statet.r.core.rsource.ast.Seq;
import de.walware.statet.r.core.rsource.ast.Sign;
import de.walware.statet.r.core.rsource.ast.SourceComponent;
import de.walware.statet.r.core.rsource.ast.Special;
import de.walware.statet.r.core.rsource.ast.StringConst;
import de.walware.statet.r.core.rsource.ast.SubIndexed;
import de.walware.statet.r.core.rsource.ast.SubNamed;
import de.walware.statet.r.core.rsource.ast.Symbol;


/**
 * Walks through the AST, analyzes element access, ...
 * 
 * Saves the information in {@link IRModelInfo}
 */
public class ScopeAnalyzer extends RAstVisitor {
	
	
	private static final int S_GLOBAL = 0;
	private static final int S_LOCAL = 1;
	private static final int S_SEARCH = 2;
	
	
	private IRSourceUnit fCurrentUnit;
	private int fAnonymCount;
	private LinkedHashMap<String, Scope> fScopes;
	private final List<Scope> fCurrentScopes = new ArrayList<Scope>();
	private Scope fDefaultScope;
	private Scope fTopScope;
	private final List<String> fIdComponents = new ArrayList<String>();
	private final List<RAstNode> fArgValueToIgnore = new LinkedList<RAstNode>();
	
	private Object fReturnValue;
	
	
	public ScopeAnalyzer() {
	}
	
	
	IRModelInfo update(final IRSourceUnit u, final AstInfo<RAstNode> ast) {
		fAnonymCount = 0;
		fCurrentUnit = u;
		fScopes = new LinkedHashMap<String, Scope>();
		final IResource res = u.getResource();
		final String projId = (res != null) ? res.getProject().getName() : "<noproject:"+u.getElementName(); //$NON-NLS-1$
		
		final Scope fileScope = new Scope(IScope.T_PROJ, projId, new Scope[0]); // ref projects
		fCurrentScopes.add(fileScope);
		fScopes.put(fileScope.getId(), fileScope);
		fDefaultScope = fileScope;
		fTopScope = fCurrentScopes.get(fCurrentScopes.size()-1);
		
		fIdComponents.add(projId);
		
		try {
			ast.root.acceptInR(this);
			
			for (final Scope si : fScopes.values()) {
				si.runLateResolve(false);
			}
			final AstInfo<RAstNode> newAst = new AstInfo<RAstNode>(RAst.LEVEL_MODEL_DEFAULT, ast.stamp);
			newAst.root = ast.root;
			final RSourceInfo model = new RSourceInfo(newAst, fScopes);
			
			fScopes = null;
			return model;
		}
		catch (final OperationCanceledException e) {}
		catch (final InvocationTargetException e) {}
		finally {
			cleanup();
		}
		return null;
	}
	
	
	private void cleanup() {
		fCurrentScopes.clear();
		fIdComponents.clear();
		fArgValueToIgnore.clear();
		
		fReturnValue = null;
		fCurrentUnit = null;
	}
	
	
	@Override
	public void visit(final SourceComponent node) throws InvocationTargetException {
		node.addAttachment(fTopScope);
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final Assignment node) throws InvocationTargetException {
		// Value
		fReturnValue = null;
		node.getSourceChild().acceptInR(this);
		
		// Resolve
		int mode;
		switch (node.getNodeType()) {
		case A_LEFT_D:
		case A_RIGHT_D:
			mode = S_SEARCH;
			break;
		default:
			mode = S_LOCAL;
			break;
		}
		
		final RAstNode target = node.getTargetChild();
		final Object returnValue = fReturnValue;
		final ElementAccess access = new ElementAccess.Default(node);
		access.fFlags = ElementAccess.A_WRITE;
		
		final String name = resolveElementName(target, access, true);
		if (name != null) {
			registerInScope(mode, name, access);
		}
		
		fReturnValue = returnValue;
	}
	
	@Override
	public void visit(final CForLoop node) throws InvocationTargetException {
		final Symbol symbol = node.getVarChild();
		final ElementAccess access = new ElementAccess.Default(symbol);
		access.fFlags = ElementAccess.A_WRITE;
		final String name = resolveElementName(symbol, access, false);
		if (name != null) {
			registerInScope(S_LOCAL, name, access);
		}
		node.getCondChild().acceptInR(this);
		node.getContChild().acceptInR(this);
		
		fReturnValue = null;
	}
	
	
	@Override
	public void visit(final FDef node) throws InvocationTargetException {
		final Scope scope = new Scope(IScope.T_FUNCTION, Integer.toString(++fAnonymCount),
				new Scope[] { fTopScope });
		fCurrentScopes.add(scope);
		fTopScope = scope;
		fScopes.put(scope.getId(), scope);
		node.addAttachment(scope);
		
		node.acceptInRChildren(this);
		
		fCurrentScopes.remove(scope);
		fTopScope = fCurrentScopes.get(fCurrentScopes.size()-1);
		
		fReturnValue = node;
	}
	
	@Override
	public void visit(final FDef.Args node) throws InvocationTargetException {
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final FDef.Arg node) throws InvocationTargetException {
		final RAstNode nameNode = node.getNameChild();
		if ((nameNode.getStatusCode() & IRSourceConstants.STATUSFLAG_REAL_ERROR) == 0) {
			final ElementAccess access = new ElementAccess.Default(node);
			access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_ARG;
			access.fNameNode = nameNode;
			registerInScope(S_LOCAL, nameNode.getText(), access);
		}
		
		if (node.hasDefault()) {
			node.getDefaultChild().acceptInR(this);
		}
		
		fReturnValue = null;
	}
	
	@Override
	public void visit(final FCall node) throws InvocationTargetException {
		// Resolve
		final RAstNode ref = node.getRefChild();
		final ElementAccess access = new ElementAccess.Default(ref);
		access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC;
		
		final String name = resolveElementName(node.getRefChild(), access, true);
		if (name != null) {
			registerInScope(S_SEARCH, name, access);
			final boolean write;
			final RAstNode parent = node.getParent();
			if (parent instanceof Assignment) {
				write = (((Assignment) parent).getTargetChild() == node);
			}
			else {
				write = false;
			}
			
			checkFunction(name, node, write);
		}
		
		// Args
		node.getArgsChild().acceptInR(this);
		
		fReturnValue = null;
	}
	
	@Override
	public void visit(final FCall.Arg node) throws InvocationTargetException {
		final RAstNode valueNode = node.getValueChild();
		if (valueNode != null) {
			if (!fArgValueToIgnore.remove(valueNode)) {
				valueNode.acceptInR(this);
			}
		}
	}
	
	@Override
	public void visit(final SubIndexed.Arg node) throws InvocationTargetException {
		final RAstNode valueNode = node.getValueChild();
		if (valueNode != null) {
			valueNode.acceptInR(this);
		}
	}
	
	@Override
	public void visit(final NSGet node) throws InvocationTargetException {
		final ElementAccess access = new ElementAccess.Default(node);
		access.fFlags = ElementAccess.A_READ;
		final String name = resolveElementName(node, access);
		if (name != null) {
			registerInScope(S_LOCAL, name, access);
		}
		
		fReturnValue = access;
	}
	
	@Override
	public void visit(final Symbol node) throws InvocationTargetException {
		final ElementAccess access = new ElementAccess.Default(node);
		access.fFlags = ElementAccess.A_READ;
		final String name = resolveElementName(node, access);
		if (name != null) {
			registerInScope(S_SEARCH, name, access);
		}
		
		fReturnValue = access;
	}
	
	@Override
	public void visit(final SubNamed node) throws InvocationTargetException {
		final ElementAccess access = new ElementAccess.Default(node);
		access.fFlags = ElementAccess.A_READ;
		final String name = resolveSlotName(node, access);
		if (name != null) {
			registerInScope(S_SEARCH, name, access);
		}
		
		fReturnValue = access;
	}
	
	@Override
	public void visit(final SubIndexed node) throws InvocationTargetException {
		final ElementAccess access = new ElementAccess.Default(node);
		access.fFlags = ElementAccess.A_READ;
		final String name = resolveElementName(node, access);
		if (name != null) {
			registerInScope(S_SEARCH, name, access);
		}
		
		fReturnValue = access;
	}
	
	@Override
	public void visit(final Model node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		fReturnValue = node;
	}
	
	
	@Override
	public void visit(final Help node) throws InvocationTargetException {
		fReturnValue = null;
	}
	
	@Override
	public void visit(final StringConst node) throws InvocationTargetException {
		fReturnValue = node;
	}
	
	@Override
	public void visit(final NumberConst node) throws InvocationTargetException {
		fReturnValue = null;
	}
	
	@Override
	public void visit(final NullConst node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		fReturnValue = null;
	}
	
	
	@Override
	public void visit(final Special node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		fReturnValue = null;
	}
	
	@Override
	public void visit(final Sign node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		fReturnValue = null;
	}
	
	@Override
	public void visit(final Power node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		fReturnValue = null;
	}
	
	@Override
	public void visit(final Arithmetic node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		fReturnValue = null;
	}
	
	@Override
	public void visit(final Seq node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		fReturnValue = null;
	}
	
	@Override
	public void visit(final Relational node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		fReturnValue = null;
	}
	
	@Override
	public void visit(final Logical node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		fReturnValue = null;
	}
	
	@Override
	public void visit(final CIfElse node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		fReturnValue = null;
	}
	
	@Override
	public void visit(final CRepeatLoop node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		fReturnValue = null;
	}
	
	@Override
	public void visit(final CWhileLoop node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		fReturnValue = null;
	}
	
	@Override
	public void visit(final CLoopCommand node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		fReturnValue = null;
	}
	
	@Override
	public void visit(final Dummy node) throws InvocationTargetException {
		node.acceptInRChildren(this);
		fReturnValue = null;
	}
	
	
	private void registerInScope(final int search, final String name, final ElementAccess access) {
		switch (search) {
		case S_LOCAL:
			fTopScope.add(name, access);
			return;
		case S_GLOBAL:
			fDefaultScope.add(name, access);
			return;
		case S_SEARCH:
			fTopScope.addLateResolve(name, access);
			return;
		default:
			throw new IllegalArgumentException("Illegal mode"); //$NON-NLS-1$
		}
	}
	
	
	public String resolveElementName(final RAstNode node, final ElementAccess access, final boolean allowString) throws InvocationTargetException {
		switch (node.getNodeType()) {
		case SYMBOL:
			return resolveElementName((Symbol) node, access);
		case STRING_CONST:
			if (allowString && ((node.getStatusCode() & IRSourceConstants.STATUSFLAG_REAL_ERROR) == 0)) {
				access.fNameNode = node;
				return node.getText();
			}
			return null;
		case SUB_INDEXED_S:
		case SUB_INDEXED_D:
			return resolveElementName((SubIndexed) node, access);
		case SUB_NAMED_PART:
			return resolvePartName((SubNamed) node, access);
		case SUB_NAMED_SLOT:
			return resolveSlotName((SubNamed) node, access);
		case NS_GET:
		case NS_GET_INT:
			return resolveElementName((NSGet) node, access);
		}
		if (node == access.fFullNode) {
			node.acceptInRChildren(this);
		}
		else {
			node.acceptInR(this);
		}
		return null;
	}
	
	private String resolveElementName(final Symbol node, final ElementAccess access) {
		if ((node.getStatusCode() & IRSourceConstants.STATUSFLAG_REAL_ERROR) == 0) {
			access.fNameNode = node;
			return node.getText();
		}
		return null;
	}
	
	private String resolveElementName(final SubIndexed node, final ElementAccess access) throws InvocationTargetException {
		final RAstNode child = node.getRefChild();
		final String name = resolveElementName(child, access, false);
		node.getArgsChild().acceptInR(this);
		if (name != null) {
			access.fFlags |= ElementAccess.A_SUB;
			access.appendSubElement(new SubIndexedElementAccess(access, node));
			return name;
		}
		return null;
	}
	
	private String resolvePartName(final SubNamed node, final ElementAccess access) throws InvocationTargetException {
		final RAstNode child = node.getRefChild();
		final String name = resolveElementName(child, access, false);
		if (name != null) {
			access.fFlags |= ElementAccess.A_SUB;
			access.appendSubElement(new SubNamedPartSyntacticElementAccess(access, node));
			return name;
		}
		return null;
	}
	
	private String resolveSlotName(final SubNamed node, final ElementAccess access) throws InvocationTargetException {
		final RAstNode child = node.getRefChild();
		final String name = resolveElementName(child, access, false);
		if (name != null) {
			access.fFlags |= ElementAccess.A_SUB;
			access.appendSubElement(new SubNamedSlotSyntacticElementAccess(access, node));
			return name;
		}
		return null;
	}
	
	private String resolveElementName(final NSGet node, final ElementAccess access) {
		if (((node.getNamespaceChild().getStatusCode() & IRSourceConstants.STATUSFLAG_REAL_ERROR) == 0)
				&& ((node.getElementChild().getStatusCode() & IRSourceConstants.STATUSFLAG_REAL_ERROR) == 0)) {
			access.fFlags = ElementAccess.A_READ;
			final String pkg = node.getNamespaceChild().getText();
			Scope scope = fScopes.get(Scope.createId(IScope.T_PKG, pkg));
			if (scope == null) {
				scope = new Scope(IScope.T_PKG, pkg, new Scope[0]);
				fScopes.put(scope.getId(), scope);
			}
			access.fNameNode = node.getElementChild();
			scope.add(access.fNameNode.getText(), access);
			return null; // is registered
		}
		return null;
	}
	
	
	private void checkFunction(final String name, final FCall node, final boolean assignment) throws InvocationTargetException {
		final RCoreFunctions rdef = RCoreFunctions.DEFAULT;
		final ArgsDefinition def = rdef.getArgs(name);
		// TODO envir/late
		switch ((def != null) ? def.eId : -1) {
		case RCoreFunctions.BASE_ASSIGN_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.BASE_ASSIGN_args);
			final RAstNode x = argValues[rdef.BASE_ASSIGN_arg_x]; 
			if (x != null && x.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_WRITE;
				access.fNameNode = argValues[rdef.BASE_ASSIGN_arg_x];
				fTopScope.add(argValues[rdef.BASE_ASSIGN_arg_x].getText(), access);
			}
			return;
		}
		case RCoreFunctions.BASE_REMOVE_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.BASE_REMOVE_args);
			final RAstNode xList = argValues[rdef.BASE_REMOVE_arg_x];
			if (xList != null && xList.getNodeType() == NodeType.DUMMY && xList.getOperator(0) == RTerminal.COMMA) {
				final int n = xList.getChildCount();
				for (int i = 0; i < n; i++) {
					final RAstNode x = xList.getChild(i);
					switch (x.getNodeType()) {
					case SYMBOL:
						fArgValueToIgnore.add(x);
					case STRING_CONST:
						final ElementAccess access = new ElementAccess.Default(node);
						access.fFlags = ElementAccess.A_WRITE;
						access.fNameNode = x;
						fTopScope.add(x.getText(), access);
					}
				}
			}
			return;
		}
		case RCoreFunctions.BASE_EXISTS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.BASE_GET_args);
			final RAstNode x = argValues[rdef.BASE_EXISTS_arg_x];
			if (x != null && x.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ;
				access.fNameNode = x;
				fTopScope.add(x.getText(), access);
			}
			return;
		}
		case RCoreFunctions.BASE_GET_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.BASE_GET_args);
			final RAstNode x = argValues[rdef.BASE_GET_arg_x];
			if (x != null && x.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ;
				access.fNameNode = x;
				fTopScope.add(x.getText(), access);
			}
			return;
		}
		case RCoreFunctions.BASE_SAVE_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.BASE_SAVE_args);
			final RAstNode xList = argValues[rdef.BASE_SAVE_arg_x];
			if (xList != null && xList.getNodeType() == NodeType.DUMMY && xList.getOperator(0) == RTerminal.COMMA) {
				final int n = xList.getChildCount();
				for (int i = 0; i < n; i++) {
					final RAstNode xArg = xList.getChild(i);
					switch (xArg.getNodeType()) {
					case SYMBOL:
						fArgValueToIgnore.add(xArg);
					case STRING_CONST:
						final ElementAccess access = new ElementAccess.Default(node);
						access.fFlags = ElementAccess.A_READ;
						access.fNameNode = xArg;
						fTopScope.add(xArg.getText(), access);
					}
				}
			}
			return;
		}
		case RCoreFunctions.BASE_CALL_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.BASE_CALL_args);
			final RAstNode nameArg = argValues[rdef.BASE_CALL_arg_name];
			if (nameArg != null && nameArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC;
				access.fNameNode = nameArg;
				fTopScope.addLateResolve(nameArg.getText(), access);
			}
			return;
		}
		
		case RCoreFunctions.METHODS_SETGENERIC_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_SETGENERIC_args);
			final RAstNode fArg = argValues[rdef.METHODS_SETGENERIC_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fDefaultScope.add(fArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_SETGROUPGENERIC_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_SETGROUPGENERIC_args);
			final RAstNode fArg = argValues[rdef.METHODS_SETGROUPGENERIC_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fDefaultScope.add(fArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_REMOVEGENERIC_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_REMOVEGENERIC_args);
			final RAstNode fArg = argValues[rdef.METHODS_REMOVEGENERIC_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fDefaultScope.add(fArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_ISGENERIC_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_ISGENERIC_args);
			final RAstNode fArg = argValues[rdef.METHODS_ISGENERIC_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fDefaultScope.addLateResolve(fArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_ISGROUP_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_ISGROUP_args);
			final RAstNode fArg = argValues[rdef.METHODS_ISGROUP_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fDefaultScope.addLateResolve(fArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_SIGNATURE_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_SIGNATURE_args);
			final RAstNode list = argValues[0];
			if (list != null && list.getNodeType() == NodeType.DUMMY && list.getOperator(0) == RTerminal.COMMA) {
				final int n = list.getChildCount();
				for (int i = 0; i < n; i++) {
					if (list.getChild(i).getNodeType() == NodeType.STRING_CONST) {
						final ElementAccess access = new ElementAccess.Default(node);
						access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
						access.fNameNode = list.getChild(i);
						fDefaultScope.addClass(access.fNameNode.getText(), access);
					}
				}
			}
			return;
		}
		
		case RCoreFunctions.METHODS_SETCLASS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_SETCLASS_args);
			final RAstNode classArg = argValues[rdef.METHODS_SETCLASS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fDefaultScope.addClass(classArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_SETIS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_SETIS_args);
			final RAstNode classArg = argValues[rdef.METHODS_SETIS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fDefaultScope.addClass(classArg.getText(), access);
			}
			final RAstNode classToInherit = argValues[rdef.METHODS_SETIS_arg_classToExtend];
			if (classToInherit != null && classToInherit.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classToInherit;
				fDefaultScope.addClass(classToInherit.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_REMOVECLASS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_REMOVECLASS_args);
			final RAstNode classArg = argValues[rdef.METHODS_REMOVECLASS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fDefaultScope.addClass(classArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_ISCLASS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_ISCLASS_args);
			final RAstNode classArg = argValues[rdef.METHODS_ISCLASS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fDefaultScope.addClass(classArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_EXTENDS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_EXTENDS_args);
			final RAstNode classArg = argValues[rdef.METHODS_EXTENDS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fDefaultScope.addClass(classArg.getText(), access);
			}
			final RAstNode classToExtendArg = argValues[rdef.METHODS_EXTENDS_arg_classToExtend];
			if (classToExtendArg != null && classToExtendArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classToExtendArg;
				fDefaultScope.addClass(classToExtendArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_GETCLASS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_GETCLASS_args);
			final RAstNode classArg = argValues[rdef.METHODS_GETCLASS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fDefaultScope.addClass(classArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_GETCLASSDEF_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_GETCLASSDEF_args);
			final RAstNode classArg = argValues[rdef.METHODS_GETCLASSDEF_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fDefaultScope.addClass(classArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_FINDCLASS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_FINDCLASS_args);
			final RAstNode classArg = argValues[rdef.METHODS_FINDCLASS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fDefaultScope.addClass(classArg.getText(), access);
			}
			return;
		}
		
		case RCoreFunctions.METHODS_NEW_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_NEW_args);
			final RAstNode classArg = argValues[rdef.METHODS_NEW_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fDefaultScope.addClass(classArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_AS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_AS_args);
			final RAstNode classArg = argValues[rdef.METHODS_AS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fDefaultScope.addClass(classArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_IS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_IS_args);
			final RAstNode classArg = argValues[rdef.METHODS_IS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fDefaultScope.addClass(classArg.getText(), access);
			}
			return;
		}
		
		case RCoreFunctions.METHODS_SETMETHOD_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_SETMETHOD_args);
			final RAstNode fArg = argValues[rdef.METHODS_SETMETHOD_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				access.fSubElement = new SubClassElementAccess(access, argValues[rdef.METHODS_SETMETHOD_arg_signature]);
				fDefaultScope.add(fArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_REMOVEMETHOD_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_REMOVEMETHOD_args);
			final RAstNode fArg = argValues[rdef.METHODS_REMOVEMETHOD_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				access.fSubElement = new SubClassElementAccess(access, argValues[rdef.METHODS_REMOVEMETHOD_arg_signature]);
				fDefaultScope.addLateResolve(fArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_REMOVEMETHODS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_REMOVEMETHODS_args);
			final RAstNode fArg = argValues[rdef.METHODS_REMOVEMETHODS_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fDefaultScope.addLateResolve(fArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_EXISTSMETHOD_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_EXISTSMETHOD_args);
			final RAstNode fArg = argValues[rdef.METHODS_EXISTSMETHOD_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				access.fSubElement = new SubClassElementAccess(access, argValues[rdef.METHODS_EXISTSMETHOD_arg_signature]);
				fDefaultScope.add(fArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_HASMETHOD_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_HASMETHOD_args);
			final RAstNode fArg = argValues[rdef.METHODS_HASMETHOD_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				access.fSubElement = new SubClassElementAccess(access, argValues[rdef.METHODS_HASMETHOD_arg_signature]);
				fDefaultScope.addLateResolve(fArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_GETMETHOD_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_GETMETHOD_args);
			final RAstNode fArg = argValues[rdef.METHODS_GETMETHOD_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				access.fSubElement = new SubClassElementAccess(access, argValues[rdef.METHODS_GETMETHOD_arg_signature]);
				fDefaultScope.add(fArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_SELECTMETHOD_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_SELECTMETHOD_args);
			final RAstNode fArg = argValues[rdef.METHODS_SELECTMETHOD_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				access.fSubElement = new SubClassElementAccess(access, argValues[rdef.METHODS_SELECTMETHOD_arg_signature]);
				fDefaultScope.add(fArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_GETMETHODS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_GETMETHODS_args);
			final RAstNode fArg = argValues[rdef.METHODS_GETMETHODS_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fDefaultScope.addLateResolve(fArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_FINDMETHOD_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_FINDMETHOD_args);
			final RAstNode fArg = argValues[rdef.METHODS_FINDMETHOD_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				access.fSubElement = new SubClassElementAccess(access, argValues[rdef.METHODS_FINDMETHOD_arg_signature]);
				fDefaultScope.addLateResolve(fArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_DUMPMETHOD_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_DUMPMETHOD_args);
			final RAstNode fArg = argValues[rdef.METHODS_DUMPMETHOD_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fDefaultScope.addLateResolve(fArg.getText(), access);
			}
			return;
		}
		case RCoreFunctions.METHODS_DUMPMETHODS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_DUMPMETHODS_args);
			final RAstNode fArg = argValues[rdef.METHODS_DUMPMETHODS_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fDefaultScope.addLateResolve(fArg.getText(), access);
			}
			return;
		}
		
		case RCoreFunctions.METHODS_SLOT_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), rdef.METHODS_SLOT_args);
			final RAstNode objectArg = argValues[rdef.METHODS_SLOT_arg_object];
			final RAstNode slotArg = argValues[rdef.METHODS_SLOT_arg_slot];
			if (objectArg != null && objectArg.getNodeType() == NodeType.SYMBOL) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = (assignment) ?
						(ElementAccess.A_WRITE | ElementAccess.A_SUB | ElementAccess.A_S4) :
						(ElementAccess.A_READ | ElementAccess.A_SUB | ElementAccess.A_S4);
				access.fNameNode = objectArg;
				fArgValueToIgnore.add(objectArg);
				fTopScope.addLateResolve(objectArg.getText(), access);
				if (slotArg != null && slotArg.getNodeType() == NodeType.STRING_CONST) {
					access.fSubElement = new SubNamedSlotInFunElementAccess(access, slotArg);
				}
			}
			return;
		}
		
		default:
			if (assignment) {
				final FCall.Args args = node.getArgsChild();
				if (args.getChildCount() > 0) {
					final FCall.Arg firstArg = (FCall.Arg) args.getChild(0);
					final RAstNode argName = firstArg.getNameChild();
					final RAstNode argValue = firstArg.getValueChild();
					if (firstArg.hasValue()
							&& (!firstArg.hasName() || argName.getText().equals("x"))) { //$NON-NLS-1$
						final ElementAccess access = new ElementAccess.Default(node);
						access.fFlags = ElementAccess.A_WRITE;
						final String mainName = resolveElementName(argValue, access, false);
						fArgValueToIgnore.add(argValue);
						if (mainName != null) {
							fTopScope.addLateResolve(mainName, access);
						}
					}
				}
			}
			return;
		}
	}
	
}
