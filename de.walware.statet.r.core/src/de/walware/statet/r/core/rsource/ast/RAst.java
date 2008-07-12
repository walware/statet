/*******************************************************************************
 * Copyright (c) 2007-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource.ast;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;

import de.walware.eclipsecommons.ltk.ast.IAstNode;
import de.walware.eclipsecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.RCoreFunctions;
import de.walware.statet.r.core.model.ArgsDefinition.Arg;
import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.IRSourceConstants;


/**
 * 
 */
public class RAst {
	
	/**
	 * AST without any text informations.
	 */
	public static final int LEVEL_MINIMAL = 1;
	
	/**
	 * AST ready for model processing.
	 */
	public static final int LEVEL_MODEL_DEFAULT = 2;
	
	
	private static class LowestFDefAssignmentSearchVisitor extends GenericVisitor implements ICommonAstVisitor {
		
		private final int fStartOffset;
		private final int fStopOffset;
		private boolean fInAssignment;
		private RAstNode fAssignment;
		
		
		public LowestFDefAssignmentSearchVisitor(final int offset) {
			fStartOffset = offset;
			fStopOffset = offset;
		}
		
		
		public void visit(final IAstNode node) throws InvocationTargetException {
			if (node instanceof RAstNode) {
				((RAstNode) node).acceptInR(this);
				return;
			}
			if (node.getStopOffset() >= fStartOffset && fStopOffset >= node.getOffset()) {
				node.acceptInChildren(this);
				return;
			}
		}
		
		@Override
		public void visitNode(final RAstNode node) throws InvocationTargetException {
			if (node.getStopOffset() >= fStartOffset && fStopOffset >= node.getOffset()) {
				node.acceptInRChildren(this);
				return;
			}
		}
		
		@Override
		public void visit(final Assignment node) throws InvocationTargetException {
			if (fInAssignment) {
				node.getSourceChild().acceptInR(this);
				return;
			}
			if (node.getStopOffset() >= fStartOffset && fStopOffset >= node.getOffset()) {
				fInAssignment = true;
				node.getSourceChild().acceptInR(this);
				fInAssignment = false;
				return;
			}
		}
		
		@Override
		public void visit(final FDef node) throws InvocationTargetException {
			if (fInAssignment || 
					(node.getStopOffset() >= fStartOffset && fStopOffset >= node.getOffset())) {
				RAstNode take = node;
				RAstNode candidate = node.getRParent();
				// TODO: use analyzed ElementAccess if possible
				AssignExpr assign = null;
				while ((assign = checkAssign(candidate)) != null && assign.valueNode == take) {
					take = assign.assignNode;
					candidate = take.getRParent();
				}
				fAssignment = take;
				throw new OperationCanceledException();
			}
		}
		
	}
	
	
	public static RAstNode findLowestFDefAssignment(final IAstNode root, final int offset) {
		final LowestFDefAssignmentSearchVisitor visitor = new LowestFDefAssignmentSearchVisitor(offset);
		try {
			root.accept(visitor);
		}
		catch (final OperationCanceledException e) {
		}
		catch (final InvocationTargetException e) {
		}
		return visitor.fAssignment;
	}
	
	private static class DeepestCommandsSearchVisitor extends GenericVisitor implements ICommonAstVisitor {
		
		private final int fStartOffset;
		private final int fStopOffset;
		private RAstNode fContainer;
		private final List<RAstNode> fCommands = new ArrayList<RAstNode>();
		
		
		public DeepestCommandsSearchVisitor(final int startOffset, final int stopOffset) {
			fStartOffset = startOffset;
			fStopOffset = stopOffset;
		}
		
		
		public void visit(final IAstNode node) throws InvocationTargetException {
			if (node instanceof RAstNode) {
				((RAstNode) node).acceptInR(this);
				return;
			}
			if (node.getStopOffset() >= fStartOffset && fStopOffset >= node.getOffset()) {
				node.acceptInChildren(this);
				return;
			}
		}
		
		@Override
		public void visitNode(final RAstNode node) throws InvocationTargetException {
			if (node.fStopOffset >= fStartOffset && fStopOffset >= node.fStartOffset) {
				if (fContainer != null && fContainer == node.fRParent) {
					fCommands.add(node);
				}
			}
			
			if (node.fStartOffset <= fStartOffset && fStopOffset <= node.fStopOffset) {
				node.acceptInRChildren(this);
				return;
			}
		}
		
		@Override
		public void visit(final Block node) throws InvocationTargetException {
			if (node.fStartOffset <= fStartOffset && fStopOffset <= node.fStopOffset) {
				fCommands.clear();
				if (node.fStartOffset == fStartOffset && fStopOffset == node.fStopOffset) {
					fCommands.add(node);
					fContainer = null;
					return;
				}
				fContainer = node;
				
				node.acceptInRChildren(this);
				
				if (fCommands.isEmpty() && node.fStopOffset > fStartOffset) {
					fCommands.add(node);
				}
				return;
			}
		}
		
		@Override
		public void visit(final SourceComponent node) throws InvocationTargetException {
			if (node.fStopOffset >= fStartOffset && fStopOffset >= node.fStartOffset) {
				fCommands.clear();
				fContainer = node;
				
				node.acceptInRChildren(this);
				return;
			}
		}
		
	}
	
	private static class NextCommandsSearchVisitor extends GenericVisitor implements ICommonAstVisitor {
		
		private final int fOffset;
		private RAstNode fContainer;
		private RAstNode fNext;
		
		
		public NextCommandsSearchVisitor(final int offset) {
			fOffset = offset;
		}
		
		
		public void visit(final IAstNode node) throws InvocationTargetException {
			if (node instanceof RAstNode) {
				((RAstNode) node).acceptInR(this);
				return;
			}
			if (node.getStopOffset() >= fOffset && fOffset >= node.getOffset()) {
				node.acceptInChildren(this);
				return;
			}
		}
		
		@Override
		public void visitNode(final RAstNode node) throws InvocationTargetException {
			if (fNext == null) {
				if (node.fStartOffset >= fOffset) {
					if (fContainer != null && fContainer == node.fRParent) {
						fNext = node;
						return;
					}
					else {
						node.acceptInRChildren(this);
						return;
					}
				}
			}
		}
		
		@Override
		public void visit(final Block node) throws InvocationTargetException {
			if (fNext == null) {
				if (node.fStartOffset >= fOffset) {
					if (fContainer != null && fContainer == node.fRParent) {
						fNext = node;
						return;
					}
					else {
						node.acceptInRChildren(this);
						return;
					}
				}
				if (node.fStopOffset >= fOffset) {
					fContainer = node;
					node.acceptInRChildren(this);
					return;
				}
			}
		}
		
		@Override
		public void visit(final SourceComponent node) throws InvocationTargetException {
			if (fNext == null) {
				final IAstNode parent = node.getParent();
				if (node.fStopOffset >= fOffset &&
						// R script file or inside R chunk
						(parent == null || (parent.getOffset() <= fOffset && fOffset <= parent.getStopOffset())) ) {
					fContainer = node;
					node.acceptInRChildren(this);
					return;
				}
			}
		}
		
	}
	
	public static RAstNode[] findDeepestCommands(final IAstNode root, final int startOffset, final int stopOffset) {
		final DeepestCommandsSearchVisitor visitor = new DeepestCommandsSearchVisitor(startOffset, stopOffset);
		try {
			root.accept(visitor);
		}
		catch (final InvocationTargetException e) {
		}
		return visitor.fCommands.toArray(new RAstNode[visitor.fCommands.size()]);
	}
	
	public static RAstNode findNextCommands(final IAstNode root, final int offset) {
		final NextCommandsSearchVisitor visitor = new NextCommandsSearchVisitor(offset);
		try {
			root.accept(visitor);
		}
		catch (final InvocationTargetException e) {
		}
		return visitor.fNext;
	}
	
	public static class AssignExpr {
		
		public static final Object GLOBAL = new Object();
		public static final Object LOCAL = new Object();
		
		public final Object environment;
		public final RAstNode assignNode;
		public final RAstNode targetNode;
		public final RAstNode valueNode;
		
		public AssignExpr(final RAstNode assign, final Object env, final RAstNode target, final RAstNode source) {
			this.assignNode = assign;
			this.environment = env;
			this.targetNode = target;
			this.valueNode = source;
		}
		
	}
	
	public static AssignExpr checkAssign(final RAstNode node) {
		switch (node.getNodeType()) {
		case A_LEFT_S:
		case A_LEFT_E:
		case A_RIGHT_S:
		{
			final Assignment assignNode = (Assignment) node;
			return new AssignExpr(node, AssignExpr.LOCAL, assignNode.getTargetChild(), assignNode.getSourceChild());
		}
		case A_LEFT_D:
		case A_RIGHT_D:
		{
			final Assignment assignNode = (Assignment) node;
			return new AssignExpr(node, AssignExpr.GLOBAL, assignNode.getTargetChild(), assignNode.getSourceChild());
		}
		case F_CALL:
			final FCall callNode = (FCall) node;
			final RAstNode refChild = callNode.getRefChild();
			if (refChild.getNodeType() == NodeType.SYMBOL) {
				final Symbol symbol = (Symbol) refChild;
				if (symbol.fText != null) {
					if (symbol.fText.equals(RCoreFunctions.BASE_ASSIGN_NAME)) {
						final ReadedFCallArgs args = readArgs(callNode.getArgsChild(), RCoreFunctions.DEFAULT.BASE_ASSIGN_args);
						return new AssignExpr(node, AssignExpr.LOCAL, args.allocatedArgs[0], args.allocatedArgs[1]);
					}
				}
			}
		case F_CALL_ARGS:
			return checkAssign(node.getRParent());
		case F_CALL_ARG:
			return checkAssign(node.getRParent().getRParent());
		}
		return null;
	}
	
	public static final class ReadedFCallArgs {
		
		public final ArgsDefinition argsDef;
		public final FCall.Args argsNode;
		public final FCall.Arg[] allocatedArgs;
		public final FCall.Arg[] ellisisArgs;
		public final FCall.Arg[] otherArgs;
		
		
		private ReadedFCallArgs(
				final ArgsDefinition argsDef, 
				final FCall.Args argsNode, 
				final FCall.Arg[] allocatedArgs, 
				final FCall.Arg[] ellisisArgs, 
				final FCall.Arg[] otherArgs) {
			this.argsDef = argsDef;
			this.argsNode = argsNode;
			this.allocatedArgs = allocatedArgs;
			this.ellisisArgs = ellisisArgs;
			this.otherArgs = otherArgs;
		}
		
		
		public FCall.Arg getArgNode(final String name) {
			final int idx = argsDef.indexOf(name);
			if (idx >= 0) {
				return allocatedArgs[idx];
			}
			else {
				return null;
			}
		}
		
		public FCall.Arg getArgNode(final int idx) {
			if (idx >= 0) {
				return allocatedArgs[idx];
			}
			else {
				return null;
			}
		}
		
		public RAstNode getArgValueNode(final String name) {
			final int idx = argsDef.indexOf(name);
			if (idx >= 0 && allocatedArgs[idx] != null) {
				return allocatedArgs[idx].getValueChild();
			}
			else {
				return null;
			}
		}
		
		public RAstNode getArgValueNode(final int idx) {
			if (idx >= 0 && allocatedArgs[idx] != null) {
				return allocatedArgs[idx].getValueChild();
			}
			else {
				return null;
			}
		}
		
	}
	
	public static final FCall.Arg[] NO_ARGS = new FCall.Arg[0];
	
	public static ReadedFCallArgs readArgs(final FCall.Args argsNode, final ArgsDefinition argsDef) {
		final int nodeArgsCount = argsNode.getChildCount();
		final int defArgsCount = argsDef.size();
		final FCall.Arg[] allocatedArgs = (defArgsCount > 0) ? new FCall.Arg[defArgsCount] : NO_ARGS;
		List<FCall.Arg> autoArgs = null;
		final int ellipsisIdx = argsDef.indexOf("..."); //$NON-NLS-1$
		List<FCall.Arg> ellipsisArgs = null;
		for (int i = 0; i < nodeArgsCount; i++) {
			final FCall.Arg argNode = argsNode.getChild(i);
			if (argNode.hasName()) {
				final RAstNode nameNode = argNode.getNameChild();
				final Arg arg = argsDef.get(nameNode.getText());
				if (arg != null && arg.index != ellipsisIdx) {
					allocatedArgs[arg.index] = argNode;
				}
				else {
					if (ellipsisArgs == null) {
						ellipsisArgs = new ArrayList<FCall.Arg>(argsNode.getChildCount()-i);
					}
					ellipsisArgs.add(argNode);
				}
			}
			else {
				if (autoArgs == null) {
					autoArgs = new ArrayList<FCall.Arg>(argsNode.getChildCount()-i);
				}
				autoArgs.add(argNode);
			}
		}
		if (autoArgs != null) {
			final Iterator<FCall.Arg> iter = autoArgs.iterator();
			int idx = 0;
			ITER_ARGS: while (iter.hasNext()) {
				while (idx < defArgsCount) {
					if (allocatedArgs[idx] == null) {
						if (ellipsisIdx == idx) {
							if (ellipsisArgs == null) {
								ellipsisArgs = autoArgs.subList(idx, autoArgs.size());
								break ITER_ARGS;
							}
							else {
								ellipsisArgs.addAll(autoArgs.subList(idx, autoArgs.size()));
								break ITER_ARGS;
							}
						}
						else {
							allocatedArgs[idx++] = iter.next();
							continue ITER_ARGS;
						}
					}
					else {
						idx++;
					}
				}
				break ITER_ARGS;
			}
		}
		return new ReadedFCallArgs(
				argsDef, argsNode, allocatedArgs,
				(ellipsisIdx >= 0 && ellipsisArgs != null) ? ellipsisArgs.toArray(new FCall.Arg[ellipsisArgs.size()]) : NO_ARGS,
				(ellipsisIdx < 0 && ellipsisArgs != null) ? ellipsisArgs.toArray(new FCall.Arg[ellipsisArgs.size()]) : NO_ARGS);
	}
	
	/**
	 * @return position of the element name, if possible (symbol or strings), otherwise null
	 */
	public static Position getElementNamePosition(final RAstNode node) {
		switch (node.getNodeType()) {
		case SYMBOL:
			if (node.getOperator(0) == RTerminal.SYMBOL_G) {
				if ((node.getStatusCode() & IRSourceConstants.STATUS_MASK_12) == IRSourceConstants.STATUS2_SYNTAX_TOKEN_NOT_CLOSED) {
					return new Position(node.getOffset()+1, node.getLength()-1);
				}
				return new Position(node.getOffset()+1, node.getLength()-2);
			}
			return new Position(node.getOffset(), node.getLength());
		case STRING_CONST:
			if ((node.getStatusCode() & IRSourceConstants.STATUS_MASK_12) == IRSourceConstants.STATUS2_SYNTAX_TOKEN_NOT_CLOSED) {
				return new Position(node.getOffset()+1, node.getLength()-1);
			}
			return new Position(node.getOffset()+1, node.getLength()-2);
		default:
			return null;
		}
	}
	
	/**
	 * @return position of the element name, if possible (symbol or strings), otherwise the node range
	 */
	public static IRegion getElementNameRegion(final RAstNode node) {
		switch (node.getNodeType()) {
		case SYMBOL:
			if (node.getOperator(0) == RTerminal.SYMBOL_G) {
				if ((node.getStatusCode() & IRSourceConstants.STATUS_MASK_12) == IRSourceConstants.STATUS2_SYNTAX_TOKEN_NOT_CLOSED) {
					return new Region(node.getOffset()+1, node.getLength()-1);
				}
				return new Region(node.getOffset()+1, node.getLength()-2);
			}
			return node;
		case STRING_CONST:
			if ((node.getStatusCode() & IRSourceConstants.STATUS_MASK_12) == IRSourceConstants.STATUS2_SYNTAX_TOKEN_NOT_CLOSED) {
				return new Region(node.getOffset()+1, node.getLength()-1);
			}
			return new Region(node.getOffset()+1, node.getLength()-2);
		default:
			return node;
		}
	}
	
}
