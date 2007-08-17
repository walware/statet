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
	
	
	public static Assignment findLowestFDefAssignment(final RAstNode root, final int offset) {
		final AtomicReference<Assignment> fdef = new AtomicReference<Assignment>();
		root.accept(new GenericVisitor() {
			@Override
			public void visitNode(RAstNode node) {
				if (node.fStartOffset <= offset && offset <= node.fStopOffset) {
					node.acceptInChildren(this);
				}
			}
			@Override
			public void visit(Assignment node) {
				if (node.fStartOffset <= offset && offset <= node.fStopOffset) {
					if (node.getSourceChild().getNodeType() == NodeType.F_DEF) {
						fdef.set(node);
						return;
					}
					node.acceptInChildren(this);
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
}
