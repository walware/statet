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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.OperationCanceledException;

import de.walware.eclipsecommons.ltk.ast.IAstNode;
import de.walware.eclipsecommons.ltk.ast.ICommonAstVisitor;


/**
 * 
 */
public class RAst {
	
	
	public static final int LEVEL_MINIMAL = 1;
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
			if (node.getStopOffset() >= fStartOffset && fStopOffset >= node.getStartOffset()) {
				node.acceptInChildren(this);
				return;
			}
		}
		
		@Override
		public void visitNode(final RAstNode node) throws InvocationTargetException {
			if (node.getStopOffset() >= fStartOffset && fStopOffset >= node.getStartOffset()) {
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
			if (node.getStopOffset() >= fStartOffset && fStopOffset >= node.getStartOffset()) {
				fInAssignment = true;
				node.getSourceChild().acceptInR(this);
				fInAssignment = false;
				return;
			}
		}
		
		@Override
		public void visit(final FDef node) throws InvocationTargetException {
			if (fInAssignment || 
					(node.getStopOffset() >= fStartOffset && fStopOffset >= node.getStartOffset())) {
				RAstNode take = node;
				RAstNode cand = node.getParent();
				AssignExpr assign = null;
				while ((assign = checkAssign(cand)) != null && assign.valueNode == take) {
					take = assign.assignNode;
					cand = take.getParent();
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
	
	private static class CommandsSearchVisitor extends GenericVisitor implements ICommonAstVisitor {
		
		private RAstNode fContainer;
		private final int fStartOffset;
		private final int fStopOffset;
		final List<RAstNode> fCommands = new ArrayList<RAstNode>();
		
		
		public CommandsSearchVisitor(final int startOffset, final int stopOffset) {
			fStartOffset = startOffset;
			fStopOffset = stopOffset;
		}
		
		
		public void visit(final IAstNode node) throws InvocationTargetException {
			if (node instanceof RAstNode) {
				((RAstNode) node).acceptInR(this);
				return;
			}
			if (node.getStopOffset() >= fStartOffset && fStopOffset >= node.getStartOffset()) {
				node.acceptInChildren(this);
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
			}
		}
		
		private void visitList(final RAstNode node) throws InvocationTargetException {
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
			}
		}
		
		@Override
		public void visit(final Block node) throws InvocationTargetException {
			visitList(node);
		}
		
		@Override
		public void visit(final SourceComponent node) throws InvocationTargetException {
			visitList(node);
		}
		
	}
	
	public static RAstNode[] findDeepestCommands(final IAstNode root, final int startOffset, final int stopOffset) {
		final CommandsSearchVisitor visitor = new CommandsSearchVisitor(startOffset, stopOffset);
		try {
			root.accept(visitor);
		}
		catch (final InvocationTargetException e) {
		}
		return visitor.fCommands.toArray(new RAstNode[visitor.fCommands.size()]);
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
	
	private static final String F_ASSIGN_NAME = "assign";
	private static final List<String> F_ASSIGN_ARGS = Arrays.asList(new String[] {
			"x", "value", "pos", "envir", "inherits", "immediate" });
	
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
					if (symbol.fText.equals(F_ASSIGN_NAME)) {
						final RAstNode[] args = readArgs(callNode.getArgsChild(), F_ASSIGN_ARGS);
						return new AssignExpr(node, AssignExpr.LOCAL, args[0], args[1]);
					}
				}
			}
		case F_CALL_ARGS:
			return checkAssign(node.getParent());
		case F_CALL_ARG:
			return checkAssign(node.getParent().getParent());
		}
		return null;
	}
	
	public static RAstNode[] readArgs(final FCall.Args args, final List<String> names) {
		final int argsCount = args.getChildCount();
		final RAstNode[] values = new RAstNode[names.size()];
		final ArrayList<RAstNode> defaults = new ArrayList<RAstNode>();
		for (int i = 0; i < argsCount; i++) {
			final FCall.Arg child = (FCall.Arg) args.getChild(i);
			final RAstNode nameNode = child.getNameChild();
			if (nameNode == null) {
				defaults.add(child.getValueChild());
			}
			else {
				final int idx = names.indexOf(nameNode.getText());
				if (idx >= 0) {
					values[idx] = child.getValueChild();
				}
			}
		}
		final Iterator<RAstNode> iter = defaults.iterator();
		int idx = 0;
		ITER_ARGS: while (iter.hasNext()) {
			while (idx < values.length) {
				if (values[idx] == null) {
					values[idx] = iter.next();
					continue ITER_ARGS;
				}
				else {
					idx++;
				}
			}
			break ITER_ARGS;
		}
		return values;
	}
	
}
