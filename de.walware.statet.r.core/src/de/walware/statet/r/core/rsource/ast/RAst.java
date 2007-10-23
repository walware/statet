/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.walware.statet.r.core.RCore;


/**
 *
 */
public class RAst {
	
	
	public static final IStatus STATUS_MISSING_EXPR = new Status(IStatus.ERROR, RCore.PLUGIN_ID, 20001, "Missing token", null);
	public static final IStatus STATUS_SKIPPED_EXPR = new Status(IStatus.ERROR, RCore.PLUGIN_ID, 20002, "Missing token", null);
	public static final IStatus STATUS_MISSING_SYMBOL = new Status(IStatus.ERROR, RCore.PLUGIN_ID, 20011, "Missing token", null);
	public static final IStatus STATUS_MISSING_OPERATOR = new Status(IStatus.ERROR, RCore.PLUGIN_ID, 20021, "Missing token", null);
	public static final IStatus STATUS_UNEXEPTEC_TOKEN = new Status(IStatus.ERROR, RCore.PLUGIN_ID, 21001, "Unexepted token", null);
	public static final IStatus STATUS_UNKNOWN_TOKEN = new Status(IStatus.ERROR, RCore.PLUGIN_ID, 21002, "Unknown/Invalid token", null);
	public static final IStatus STATUS_PARSE_ERROR = new Status(IStatus.ERROR, RCore.PLUGIN_ID, 10000, "Parse error", null);
	
	
	public static final int LEVEL_MINIMAL = 1;
	public static final int LEVEL_MODEL_DEFAULT = 2;
	
	
	public static RAstNode findLowestFDefAssignment(final RAstNode root, final int offset) {
		final AtomicReference<RAstNode> fdef = new AtomicReference<RAstNode>();
		root.accept(new GenericVisitor() {
			private boolean fInAssignment;
			
			@Override
			public void visitNode(RAstNode node) {
				if (node.fStartOffset <= offset && offset <= node.fStopOffset) {
					node.acceptInChildren(this);
				}
			}

			@Override
			public void visit(Assignment node) {
				if (fInAssignment || (node.fStartOffset <= offset && offset <= node.fStopOffset)) {
					fInAssignment = true;
					node.getSourceChild().accept(this);
					fInAssignment = false;
				}
			}
			
			@Override
			public void visit(FDef node) {
				if (fInAssignment || (node.fStartOffset <= offset && offset <= node.fStopOffset)) {
					RAstNode take = node;
					RAstNode cand = node.getParent();
					AssignExpr assign = null;
					while ((assign = checkAssign(cand)) != null && assign.valueNode == take) {
						take = assign.assignNode;
						cand = take.getParent();
					}
					fdef.set(take);
					return;
				}
			}
		});
		return fdef.get();
	}
	
	public static RAstNode[] findDeepestCommands(final RAstNode root, final int startOffset, final int stopOffset) {
		final List<RAstNode> commands = new ArrayList<RAstNode>();
		root.accept(new GenericVisitor() {
			
			private RAstNode fContainer;
			
			@Override
			public void visitNode(RAstNode node) {
				if (node.fStopOffset >= startOffset && stopOffset >= node.fStartOffset) {
					if (fContainer != null && fContainer == node.fParent) {
						commands.add(node);
					}
				}

				if (node.fStartOffset <= startOffset && stopOffset <= node.fStopOffset) {
					node.acceptInChildren(this);
				}
			}
			
			private void visitList(RAstNode node) {
				if (node.fStartOffset <= startOffset && stopOffset <= node.fStopOffset) {
					commands.clear();
					if (node.fStartOffset == startOffset && stopOffset == node.fStopOffset) {
						commands.add(node);
						fContainer = null;
						return;
					}
					fContainer = node;
					
					node.acceptInChildren(this);
					
					if (commands.isEmpty() && node.fStopOffset > startOffset) {
						commands.add(node);
					}
				}
			}
			
			@Override
			public void visit(Block node) {
				visitList(node);
			}
			
			@Override
			public void visit(SourceComponent node) {
				visitList(node);
			}
			
		});
		return commands.toArray(new RAstNode[commands.size()]);
	}
	
	public static class AssignExpr {
		public static final Object GLOBAL = new Object();
		public static final Object LOCAL = new Object();

		public final Object environment;
		public final RAstNode assignNode;
		public final RAstNode targetNode;
		public final RAstNode valueNode;

		public AssignExpr(RAstNode assign, Object env, RAstNode target, RAstNode source) {
			this.assignNode = assign;
			this.environment = env;
			this.targetNode = target;
			this.valueNode = source;
		}
	}

	private static final String F_ASSIGN_NAME = "assign";
	private static final List<String> F_ASSIGN_ARGS = Arrays.asList(new String[] {
			"x", "value", "pos", "envir", "inherits", "immediate" });
	
	public static AssignExpr checkAssign(RAstNode node) {
		switch (node.getNodeType()) {
		case A_LEFT_S:
		case A_LEFT_E:
		case A_RIGHT_S:
		{
			Assignment assignNode = (Assignment) node;
			return new AssignExpr(node, AssignExpr.LOCAL, assignNode.getTargetChild(), assignNode.getSourceChild());
		}
		case A_LEFT_D:
		case A_RIGHT_D:
		{
			Assignment assignNode = (Assignment) node;
			return new AssignExpr(node, AssignExpr.GLOBAL, assignNode.getTargetChild(), assignNode.getSourceChild());
		}
		case F_CALL:
			FCall callNode = (FCall) node;
			RAstNode refChild = callNode.getRefChild();
			if (refChild.getNodeType() == NodeType.SYMBOL) {
				Symbol symbol = (Symbol) refChild;
				if (symbol.fText != null) {
					if (symbol.fText.equals(F_ASSIGN_NAME)) {
						RAstNode[] args = readArgs(callNode.getArgsChild(), F_ASSIGN_ARGS);
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
	
	public static RAstNode[] readArgs(FCall.Args args, List<String> names) {
		final int argsCount = args.getChildCount();
		RAstNode[] values = new RAstNode[names.size()];
		ArrayList<RAstNode> defaults = new ArrayList<RAstNode>();
		for (int i = 0; i < argsCount; i++) {
			FCall.Arg child = (FCall.Arg) args.getChild(i);
			RAstNode nameNode = child.getNameChild();
			if (nameNode == null) {
				defaults.add(child.getValueChild());
			}
			else {
				final int idx = names.indexOf(getElementName(nameNode));
				if (idx >= 0) {
					values[idx] = child.getValueChild();
				}
			}
		}
		Iterator<RAstNode> iter = defaults.iterator();
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
	
	public static String getElementName(RAstNode node) {
		switch (node.getNodeType()) {
		case SYMBOL:
			return ((Symbol) node).fText;
		case STRING_CONST:
		{
			final String text = ((StringConst) node).fText;
			final int length = text.length();
			if (length <= 1) {
				return "";
			}
			final char c = text.charAt(0);
			if (text.charAt(length-1) == c) {
				return text.substring(1, length-1);
			}
			else {
				return text.substring(1, length);
			}
		}
		default:
			return null;
		}
	}
	
	
}
