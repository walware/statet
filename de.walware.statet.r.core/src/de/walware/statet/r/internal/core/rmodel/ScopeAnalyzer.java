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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.OperationCanceledException;

import de.walware.eclipsecommons.ltk.AstInfo;

import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.IScope;
import de.walware.statet.r.core.model.RCoreFunctions;
import de.walware.statet.r.core.rlang.RTerminal;
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
	private Map<String, Scope> fDependencyEnvironments;
	private final List<Scope> fCurrentScopes = new ArrayList<Scope>();
	private Scope fGlobalEnvScope;
	private Scope fGenericDefaultScope;
	private Scope fTopEnvScope;
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
		fDependencyEnvironments = new HashMap<String, Scope>();
		final IResource res = u.getResource();
		final String projId = (res != null) ? res.getProject().getName() : "<noproject:"+u.getElementName(); //$NON-NLS-1$
		
		final Scope fileScope = new Scope(IScope.T_PROJ, projId, new Scope[0]); // ref projects
		fCurrentScopes.add(fileScope);
		fScopes.put(fileScope.getId(), fileScope);
		fGenericDefaultScope = fTopEnvScope = fGlobalEnvScope = fileScope;
		fTopScope = fCurrentScopes.get(fCurrentScopes.size()-1);
		
		fIdComponents.add(projId);
		
		try {
			ast.root.acceptInR(this);
			
			for (final Scope si : fDependencyEnvironments.values()) {
				si.runLateResolve(false);
			}
			fTopEnvScope.getParents().addAll(0, fDependencyEnvironments.values());
			for (final Scope si : fScopes.values()) {
				si.runLateResolve(false);
			}
			final AstInfo<RAstNode> newAst = new AstInfo<RAstNode>(RAst.LEVEL_MODEL_DEFAULT, ast.stamp);
			newAst.root = ast.root;
			final RSourceInfo model = new RSourceInfo(newAst, fScopes);
			
			fScopes = null;
			fDependencyEnvironments = null;
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
		final Object returnValue = fReturnValue;
		
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
		}
		final boolean write;
		final RAstNode parent = node.getRParent();
		if (parent instanceof Assignment) {
			write = (((Assignment) parent).getTargetChild() == node);
		}
		else {
			write = false;
		}
		
		fReturnValue = checkFunction(name, node, write);
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
			
			if (name.equals(".GlobalEnv")) {
				fReturnValue = fGlobalEnvScope;
				return;
			}
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
	
	
	private Scope getPkgScope(final String name) {
		Scope scope = fDependencyEnvironments.get(Scope.createId(IScope.T_PKG, name));
		if (scope == null) {
			scope = new Scope(IScope.T_PKG, name, new Scope[0]);
			fDependencyEnvironments.put(scope.getId(), scope);
		}
		return scope;
	}
	
	private void registerInScope(final int search, final String name, final ElementAccess access) {
		switch (search) {
		case S_LOCAL:
			fTopScope.add(name, access);
			return;
		case S_GLOBAL:
			fGlobalEnvScope.add(name, access);
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
			access.fNameNode = node.getElementChild();
			getPkgScope(pkg).add(access.fNameNode.getText(), access);
			return null; // is registered
		}
		return null;
	}
	
	
	private Object checkFunction(final String name, final FCall node, final boolean assignment) throws InvocationTargetException {
		final RCoreFunctions rdef = RCoreFunctions.DEFAULT;
		final ArgsDefinition def = (name != null) ? rdef.getArgs(name) : null;
		// TODO envir/late
		switch ((def != null) ? def.eId : -1) {
		case RCoreFunctions.BASE_ASSIGN_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode x = argValues[rdef.BASE_ASSIGN_arg_x]; 
			if (x != null && x.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_WRITE;
				access.fNameNode = x;
				final Scope scope = readScopeArgs(resolvePos(argValues, def), fTopScope);
				if (resolveAndReadInherits(argValues, def, false)) {
					scope.addLateResolve(x.getText(), access);
				}
				else {
					scope.add(x.getText(), access);
				}
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.BASE_REMOVE_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode xList = argValues[rdef.BASE_REMOVE_arg_x];
			if (xList != null && xList.getNodeType() == NodeType.DUMMY && xList.getOperator(0) == RTerminal.COMMA) {
				final int n = xList.getChildCount();
				for (int i = 0; i < n; i++) {
					final RAstNode x = xList.getChild(i);
					switch (x.getNodeType()) {
					case SYMBOL:
						fArgValueToIgnore.add(x);
						// no break
					case STRING_CONST:
						final ElementAccess access = new ElementAccess.Default(node);
						access.fFlags = ElementAccess.A_DELETE;
						access.fNameNode = x;
						final Scope scope = readScopeArgs(resolvePos(argValues, def), fTopScope);
						if (resolveAndReadInherits(argValues, def, false)) {
							scope.addLateResolve(x.getText(), access);
						}
						else {
							scope.add(x.getText(), access);
						}
					}
				}
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.BASE_EXISTS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode x = argValues[rdef.BASE_EXISTS_arg_x];
			if (x != null && x.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ;
				access.fNameNode = x;
				final Scope scope = readScopeArgs(resolveWhere(argValues, def), fTopScope);
				if (resolveAndReadInherits(argValues, def, false)) {
					scope.addLateResolve(x.getText(), access);
				}
				else {
					scope.add(x.getText(), access);
				}
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.BASE_GET_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode x = argValues[rdef.BASE_GET_arg_x];
			if (x != null && x.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ;
				access.fNameNode = x;
				final Scope scope = readScopeArgs(resolvePos(argValues, def), fTopScope);
				if (resolveAndReadInherits(argValues, def, true)) {
					scope.addLateResolve(x.getText(), access);
				}
				else {
					scope.add(x.getText(), access);
				}
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.BASE_SAVE_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode xList = argValues[rdef.BASE_SAVE_arg_x];
			if (xList != null && xList.getNodeType() == NodeType.DUMMY && xList.getOperator(0) == RTerminal.COMMA) {
				final int n = xList.getChildCount();
				for (int i = 0; i < n; i++) {
					final RAstNode xArg = xList.getChild(i);
					switch (xArg.getNodeType()) {
					case SYMBOL:
						fArgValueToIgnore.add(xArg);
						// no break
					case STRING_CONST:
						final ElementAccess access = new ElementAccess.Default(node);
						access.fFlags = ElementAccess.A_READ;
						access.fNameNode = xArg;
						fTopScope.addLateResolve(xArg.getText(), access);
					}
				}
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.BASE_CALL_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode nameArg = argValues[rdef.BASE_CALL_arg_name];
			if (nameArg != null && nameArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC;
				access.fNameNode = nameArg;
				fTopScope.addLateResolve(nameArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		
		case RCoreFunctions.BASE_GLOBALENV_ID: {
			node.getArgsChild().acceptInR(this);
			return fGlobalEnvScope;
		}
		
		case RCoreFunctions.BASE_TOPENV_ID: {
//			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
//			final RAstNode envir = resolveEnvir(argValues, def);
			node.getArgsChild().acceptInR(this);
			return fTopEnvScope;
		}
		
		case RCoreFunctions.METHODS_SETGENERIC_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode fArg = argValues[rdef.METHODS_SETGENERIC_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fGenericDefaultScope.add(fArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_SETGROUPGENERIC_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode fArg = argValues[rdef.METHODS_SETGROUPGENERIC_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fGenericDefaultScope.add(fArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_REMOVEGENERIC_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode fArg = argValues[rdef.METHODS_REMOVEGENERIC_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_DELETE | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fGenericDefaultScope.add(fArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_ISGENERIC_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode fArg = argValues[rdef.METHODS_ISGENERIC_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fGenericDefaultScope.addLateResolve(fArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_ISGROUP_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode fArg = argValues[rdef.METHODS_ISGROUP_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fGenericDefaultScope.addLateResolve(fArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_SIGNATURE_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode list = argValues[0];
			if (list != null && list.getNodeType() == NodeType.DUMMY && list.getOperator(0) == RTerminal.COMMA) {
				final int n = list.getChildCount();
				for (int i = 0; i < n; i++) {
					if (list.getChild(i).getNodeType() == NodeType.STRING_CONST) {
						final ElementAccess access = new ElementAccess.Default(node);
						access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
						access.fNameNode = list.getChild(i);
						fGenericDefaultScope.addClass(access.fNameNode.getText(), access);
					}
				}
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		
		case RCoreFunctions.METHODS_SETCLASS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode classArg = argValues[rdef.METHODS_SETCLASS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fGenericDefaultScope.addClass(classArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_SETIS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode classArg = argValues[rdef.METHODS_SETIS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fGenericDefaultScope.addClass(classArg.getText(), access);
			}
			final RAstNode classToInherit = argValues[rdef.METHODS_SETIS_arg_classToExtend];
			if (classToInherit != null && classToInherit.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classToInherit;
				fGenericDefaultScope.addClass(classToInherit.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_REMOVECLASS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode classArg = argValues[rdef.METHODS_REMOVECLASS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_DELETE | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fGenericDefaultScope.addClass(classArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_ISCLASS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode classArg = argValues[rdef.METHODS_ISCLASS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fGenericDefaultScope.addClass(classArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_EXTENDS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode classArg = argValues[rdef.METHODS_EXTENDS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fGenericDefaultScope.addClass(classArg.getText(), access);
			}
			final RAstNode classToExtendArg = argValues[rdef.METHODS_EXTENDS_arg_classToExtend];
			if (classToExtendArg != null && classToExtendArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classToExtendArg;
				fGenericDefaultScope.addClass(classToExtendArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_GETCLASS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode classArg = argValues[rdef.METHODS_GETCLASS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fGenericDefaultScope.addClass(classArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_GETCLASSDEF_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode classArg = argValues[rdef.METHODS_GETCLASSDEF_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fGenericDefaultScope.addClass(classArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_FINDCLASS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode classArg = argValues[rdef.METHODS_FINDCLASS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fGenericDefaultScope.addClass(classArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		
		case RCoreFunctions.METHODS_NEW_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode classArg = argValues[rdef.METHODS_NEW_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fGenericDefaultScope.addClass(classArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_AS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode classArg = argValues[rdef.METHODS_AS_arg_class];
			if (classArg != null && classArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Class(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS | ElementAccess.A_S4;
				access.fNameNode = classArg;
				fGenericDefaultScope.addClass(classArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		
		case RCoreFunctions.METHODS_SETMETHOD_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode fArg = argValues[rdef.METHODS_SETMETHOD_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_WRITE | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				access.fSubElement = new SubClassElementAccess(access, argValues[rdef.METHODS_SETMETHOD_arg_signature]);
				
				fGenericDefaultScope.add(fArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_REMOVEMETHOD_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode fArg = argValues[rdef.METHODS_REMOVEMETHOD_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_DELETE | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				access.fSubElement = new SubClassElementAccess(access, argValues[rdef.METHODS_REMOVEMETHOD_arg_signature]);
				fGenericDefaultScope.addLateResolve(fArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_REMOVEMETHODS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode fArg = argValues[rdef.METHODS_REMOVEMETHODS_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_DELETE | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fGenericDefaultScope.addLateResolve(fArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_EXISTSMETHOD_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode fArg = argValues[rdef.METHODS_EXISTSMETHOD_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				access.fSubElement = new SubClassElementAccess(access, argValues[rdef.METHODS_EXISTSMETHOD_arg_signature]);
				fGenericDefaultScope.add(fArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_HASMETHOD_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode fArg = argValues[rdef.METHODS_HASMETHOD_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				access.fSubElement = new SubClassElementAccess(access, argValues[rdef.METHODS_HASMETHOD_arg_signature]);
				fGenericDefaultScope.addLateResolve(fArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_GETMETHOD_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode fArg = argValues[rdef.METHODS_GETMETHOD_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				access.fSubElement = new SubClassElementAccess(access, argValues[rdef.METHODS_GETMETHOD_arg_signature]);
				fGenericDefaultScope.add(fArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_SELECTMETHOD_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode fArg = argValues[rdef.METHODS_SELECTMETHOD_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				access.fSubElement = new SubClassElementAccess(access, argValues[rdef.METHODS_SELECTMETHOD_arg_signature]);
				fGenericDefaultScope.add(fArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_GETMETHODS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode fArg = argValues[rdef.METHODS_GETMETHODS_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fGenericDefaultScope.addLateResolve(fArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_FINDMETHOD_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode fArg = argValues[rdef.METHODS_FINDMETHOD_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				access.fSubElement = new SubClassElementAccess(access, argValues[rdef.METHODS_FINDMETHOD_arg_signature]);
				fGenericDefaultScope.addLateResolve(fArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_DUMPMETHOD_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode fArg = argValues[rdef.METHODS_DUMPMETHOD_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fGenericDefaultScope.addLateResolve(fArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		case RCoreFunctions.METHODS_DUMPMETHODS_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
			final RAstNode fArg = argValues[rdef.METHODS_DUMPMETHODS_arg_f];
			if (fArg != null && fArg.getNodeType() == NodeType.STRING_CONST) {
				final ElementAccess access = new ElementAccess.Default(node);
				access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC | ElementAccess.A_S4;
				access.fNameNode = fArg;
				fGenericDefaultScope.addLateResolve(fArg.getText(), access);
			}
			node.getArgsChild().acceptInR(this);
			return null;
		}
		
		case RCoreFunctions.METHODS_SLOT_ID: {
			final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
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
			node.getArgsChild().acceptInR(this);
			return null;
		}
		
		default:
			if (def != null) {
				final RAstNode[] argValues = RAst.readArgs(node.getArgsChild(), def);
				ITER_ARGS: for (int i = 0; i < argValues.length; i++) {
					final RAstNode arg;
					if ((arg = argValues[i]) != null) {
						if ((def.get(i).type & ArgsDefinition.METHOD_NAME) != 0
								&& arg.getNodeType() == NodeType.STRING_CONST) {
							final ElementAccess access = new ElementAccess.Default(node);
							access.fFlags = ElementAccess.A_READ | ElementAccess.A_FUNC;
							access.fNameNode = arg;
							fGenericDefaultScope.addLateResolve(arg.getText(), access);
							continue ITER_ARGS;
						}
						if ((def.get(i).type & ArgsDefinition.CLASS_NAME) != 0
								&& arg.getNodeType() == NodeType.STRING_CONST) {
							final ElementAccess access = new ElementAccess.Class(node);
							access.fFlags = ElementAccess.A_READ | ElementAccess.A_CLASS;
							access.fNameNode = arg;
							fGenericDefaultScope.addClass(arg.getText(), access);
							continue ITER_ARGS;
						}
						if ((def.get(i).type & ArgsDefinition.UNSPECIFIC_NAME) != 0
								&& arg.getNodeType() == NodeType.STRING_CONST) {
							final ElementAccess access = new ElementAccess.Default(node);
							access.fFlags = ElementAccess.A_READ;
							access.fNameNode = arg;
							fTopScope.addLateResolve(arg.getText(), access);
							continue ITER_ARGS;
						}
					}
				}
			}
			else if (assignment) {
				final FCall.Args args = node.getArgsChild();
				if (args.getChildCount() > 0) {
					final FCall.Arg firstArg = (FCall.Arg) args.getChild(0);
					final RAstNode argName = firstArg.getNameChild();
					final RAstNode argValue = firstArg.getValueChild();
					if (firstArg.hasValue()
							&& (!firstArg.hasName() || argName.getText().equals("x"))) { 
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
			node.getArgsChild().acceptInR(this);
			return null;
		}
	}
	
	
	private RAstNode resolvePos(final RAstNode[] argValues, final ArgsDefinition args) {
		final int idx = args.indexOf("pos");
		if (idx >= 0) {
			return argValues[idx];
		}
		return null;
	}
	
	private RAstNode resolveWhere(final RAstNode[] argValues, final ArgsDefinition args) {
		final int idx = args.indexOf("where");
		if (idx >= 0) {
			return argValues[idx];
		}
		return null;
	}
	
	private boolean resolveAndReadInherits(final RAstNode[] argValues, final ArgsDefinition args, final boolean inheritsByDefault) {
		final int idx = args.indexOf("inherits");
		if (idx >= 0) {
			final RAstNode inherits = argValues[idx];
			if (inherits != null && inherits.getNodeType() == NodeType.NUM_CONST) {
				if (inherits.getOperator(0) == RTerminal.TRUE) {
					return true;
				}
				if (inherits.getOperator(0) == RTerminal.FALSE) {
					return false;
				}
			}
		}
		return inheritsByDefault;
	}
	
	private Scope readScopeArgs(final RAstNode pos, final Scope defaultScope) throws InvocationTargetException {
		fReturnValue = null;
		Scope scope = null;
		if (pos != null) {
			switch (pos.getNodeType()) {
			case NUM_CONST:
				if ("1".equals(pos.getText())) { // search pos
					scope = fGlobalEnvScope;
				}
				break;
			case STRING_CONST: // search name
				if (pos.getText().equals(".GlobalEnv")) {
					scope = fGlobalEnvScope;
				}
				if (pos.getText().startsWith("package:")) {
					scope = getPkgScope(pos.getText().substring(8));
				}
				break;
			default:
				// check for environment
				pos.acceptInR(this);
				if (fReturnValue instanceof Scope) {
					scope = (Scope) fReturnValue;
				}
			}
			fArgValueToIgnore.add(pos);
		}
		if (scope != null) {
			return scope;
		}
		return defaultScope;
	}
	
}
