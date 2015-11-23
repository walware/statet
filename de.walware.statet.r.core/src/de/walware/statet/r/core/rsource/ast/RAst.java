/*=============================================================================#
 # Copyright (c) 2007-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.rsource.ast;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;

import de.walware.ecommons.ltk.ast.Ast;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;

import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.ArgsDefinition.Arg;
import de.walware.statet.r.core.model.RCoreFunctions;
import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.IRSourceConstants;


/**
 * 
 */
public class RAst extends Ast {
	
	
	private static class LowestFDefAssignmentSearchVisitor extends GenericVisitor implements ICommonAstVisitor {
		
		private final int fStartOffset;
		private final int fStopOffset;
		private boolean fInAssignment;
		private RAstNode fAssignment;
		
		
		public LowestFDefAssignmentSearchVisitor(final int offset) {
			fStartOffset = offset;
			fStopOffset = offset;
		}
		
		
		@Override
		public void visit(final IAstNode node) throws InvocationTargetException {
			if (node instanceof RAstNode) {
				((RAstNode) node).acceptInR(this);
				return;
			}
			if (node.getEndOffset() >= fStartOffset && fStopOffset >= node.getOffset()) {
				node.acceptInChildren(this);
				return;
			}
		}
		
		@Override
		public void visitNode(final RAstNode node) throws InvocationTargetException {
			if (node.getEndOffset() >= fStartOffset && fStopOffset >= node.getOffset()) {
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
			if (node.getEndOffset() >= fStartOffset && fStopOffset >= node.getOffset()) {
				fInAssignment = true;
				node.getSourceChild().acceptInR(this);
				fInAssignment = false;
				return;
			}
		}
		
		@Override
		public void visit(final FDef node) throws InvocationTargetException {
			if (fInAssignment || 
					(node.getEndOffset() >= fStartOffset && fStopOffset >= node.getOffset())) {
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
		private final List<RAstNode> fCommands= new ArrayList<>();
		
		
		public DeepestCommandsSearchVisitor(final int startOffset, final int stopOffset) {
			fStartOffset = startOffset;
			fStopOffset = stopOffset;
		}
		
		
		@Override
		public void visit(final IAstNode node) throws InvocationTargetException {
			if (node instanceof RAstNode) {
				((RAstNode) node).acceptInR(this);
				return;
			}
			if (node.getEndOffset() >= fStartOffset && fStopOffset >= node.getOffset()) {
				node.acceptInChildren(this);
				return;
			}
		}
		
		@Override
		public void visitNode(final RAstNode node) throws InvocationTargetException {
			if (node.fStopOffset >= fStartOffset && ((fStartOffset == fStopOffset) ?
					fStopOffset >= node.fStartOffset : fStopOffset > node.fStartOffset) ) {
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
		
		
		@Override
		public void visit(final IAstNode node) throws InvocationTargetException {
			if (node instanceof RAstNode) {
				((RAstNode) node).acceptInR(this);
				return;
			}
			if (node.getEndOffset() >= fOffset && fOffset >= node.getOffset()) {
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
						(parent == null || (parent.getOffset() <= fOffset && fOffset <= parent.getEndOffset())) ) {
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
		case A_LEFT:
		case A_RIGHT:
		case A_EQUALS:
			final Assignment assignNode = (Assignment) node;
			if (assignNode.isSearchOperator()) {
				return new AssignExpr(node, AssignExpr.GLOBAL, assignNode.getTargetChild(), assignNode.getSourceChild());
			}
			else {
				return new AssignExpr(node, AssignExpr.LOCAL, assignNode.getTargetChild(), assignNode.getSourceChild());
			}
		case F_CALL:
			final FCall callNode = (FCall) node;
			final RAstNode refChild = callNode.getRefChild();
			if (refChild.getNodeType() == NodeType.SYMBOL) {
				final Symbol symbol = (Symbol) refChild;
				if (symbol.fText != null) {
					if (symbol.fText.equals(RCoreFunctions.BASE_ASSIGN_NAME)) {
						final FCallArgMatch args = matchArgs(callNode.getArgsChild(), RCoreFunctions.DEFAULT.BASE_ASSIGN_args);
						return new AssignExpr(node, AssignExpr.LOCAL, args.allocatedArgs[0], args.allocatedArgs[1]);
					}
				}
			}
			break;
		case F_CALL_ARGS:
			return checkAssign(node.getRParent());
		case F_CALL_ARG:
			return checkAssign(node.getRParent().getRParent());
		}
		return null;
	}
	
	public static final class FCallArgMatch {
		
		public final ArgsDefinition argsDef;
		public final FCall.Args argsNode;
		public final FCall.Arg[] allocatedArgs;
		public final FCall.Arg[] ellipsisArgs;
		public final FCall.Arg[] otherArgs;
		public final int[] argsNode2argsDef;
		
		
		private FCallArgMatch(
				final ArgsDefinition argsDef, 
				final FCall.Args argsNode, 
				final FCall.Arg[] allocatedArgs, 
				final FCall.Arg[] ellipsisArgs, 
				final FCall.Arg[] otherArgs,
				final int[] argsNode2argsDef) {
			this.argsDef = argsDef;
			this.argsNode = argsNode;
			this.allocatedArgs = allocatedArgs;
			this.ellipsisArgs = ellipsisArgs;
			this.otherArgs = otherArgs;
			this.argsNode2argsDef = argsNode2argsDef;
		}
		
		
		public FCall.Arg getArgNode(final String name) {
			final int idx = this.argsDef.indexOf(name);
			if (idx >= 0) {
				return this.allocatedArgs[idx];
			}
			else {
				return null;
			}
		}
		
		public FCall.Arg getArgNode(final int callArgIdx) {
			if (callArgIdx >= 0) {
				return this.allocatedArgs[callArgIdx];
			}
			else {
				return null;
			}
		}
		
		public RAstNode getArgValueNode(final String name) {
			final int idx = this.argsDef.indexOf(name);
			if (idx >= 0 && this.allocatedArgs[idx] != null) {
				return this.allocatedArgs[idx].getValueChild();
			}
			else {
				return null;
			}
		}
		
		public RAstNode getArgValueNode(final int callArgIdx) {
			if (callArgIdx >= 0 && this.allocatedArgs[callArgIdx] != null) {
				return this.allocatedArgs[callArgIdx].getValueChild();
			}
			else {
				return null;
			}
		}
		
		public ArgsDefinition.Arg getArgDef(final int callArgIdx) {
			if (callArgIdx >= 0 && this.argsDef.size() > 0) {
				if (callArgIdx < this.argsNode2argsDef.length) {
					if (this.argsNode2argsDef[callArgIdx] >= 0) {
						return this.argsDef.get(this.argsNode2argsDef[callArgIdx]);
					}
				}
				else if (callArgIdx == 0 && this.argsNode2argsDef.length == 0){
					return this.argsDef.get(0);
				}
			}
			return null;
		}
		
		
		@Override
		public String toString() {
			return Arrays.toString(this.argsNode2argsDef);
		}
		
	}
	
	public static final FCall.Arg[] NO_ARGS = new FCall.Arg[0];
	
	private static final int FAIL = -1;
	private static final int ELLIPSIS = -2;
	private static final int TEST_PARTIAL = -3;
	private static final int TEST_POSITION = -4;
		
	/**
	 * Reads the arguments of a function call for the given function definition
	 * 
	 * If follows mainly the R rules expect if R would fail.
	 * 
	 * 1) Exact matching on tags. 
	 *    a) First supplied named argument matching exactly a formal name
	 *       is assigned to its formal arguments (position) in <code>argsNode</code>
	 *    b) Additional supplied arguments matching exactly the formal name
	 *       with an assignment of a) (error) are added to <code>otherArgs</code>
	 * 2) Partial matching on tags
	 *    a) If the name of the supplied argument matches partially more than one 
	 *       unmatched formal name (error), it is added to <code>otherArgs</code>
	 *    b) First supplied argument matching partially one unmatched formal name
	 *       is assigned to its formal arguments (position) in <code>argsNode</code>
	 *    c) Additional supplied arguments matching partially the unmatched formal name 
	 *       with an assignment of b) (error) are treated like arguments without any match
	 *       in 1) and 2) continue with 3).
	 * 3) Positional matching.
	 *    a) Any unnamed supplied argument is assigned to unmatched formal
	 *       arguments (empty positions) in <code>argsNode</code>, in order.
	 *       Until all If there is a ‘...’ argument or there is no more empty position.
	 *    b) If there is a ‘...’ argument, all remaining supplied arguments
	 *       are added to <code>ellipsisArgs</code>
	 * 4) Remaining supplied arguments (error) are assigned to <code>otherArgs</code>
	 * 
	 * (see paragraph 'Argument matching' in 'R Language Definition')
	 * 
	 * @param argsNode the arguments of the call
	 * @param argsDef the arguments definition
	 * @return
	 */
	public static FCallArgMatch matchArgs(final FCall.Args argsNode, final ArgsDefinition argsDef) {
		final int nodeArgsCount = argsNode.getChildCount();
		final int defArgsCount = argsDef.size();
		final FCall.Arg[] allocatedArgs = (defArgsCount > 0) ? new FCall.Arg[defArgsCount] : NO_ARGS;
		final int ellipsisDefIdx = argsDef.indexOf("..."); //$NON-NLS-1$
		
		final int[] match = new int[nodeArgsCount];
		int ellipsisCount = 0;
		int failCount = 0;
		boolean testPartial = false;
		boolean testPosition = false;
		
		for (int nodeIdx = 0; nodeIdx < nodeArgsCount; nodeIdx++) {
			final FCall.Arg argNode = argsNode.getChild(nodeIdx);
			if (argNode.hasName()) {
				final Arg arg = argsDef.get(argNode.getNameChild().getText());
				if (arg != null && arg.index != ellipsisDefIdx) {
					if (allocatedArgs[arg.index] == null) {
						allocatedArgs[arg.index] = argNode;
						match[nodeIdx] = arg.index;
					}
					else {
						failCount++;
						match[nodeIdx] = FAIL;
					}
				}
				else {
					testPartial = true;
					match[nodeIdx] = TEST_PARTIAL;
				}
			}
			else {
				testPosition = true;
				match[nodeIdx] = TEST_POSITION;
			}
		}
		
		final int ellipsisType = (ellipsisDefIdx >= 0) ? ELLIPSIS : FAIL;
		final int testStop = (ellipsisDefIdx >= 0) ? ellipsisDefIdx : defArgsCount;
		if (testPartial) {
			FCall.Arg[] partialArgs = null;
			ITER_ARGS: for (int nodeIdx = 0; nodeIdx < nodeArgsCount; nodeIdx++) {
				if (match[nodeIdx] == TEST_PARTIAL) {
					final FCall.Arg argNode = argsNode.getChild(nodeIdx);
					final String name = argNode.getNameChild().getText();
					int matchIdx = -1;
					for (int defIdx = 0; defIdx < testStop; defIdx++) {
						if (allocatedArgs[defIdx] == null && argsDef.get(defIdx).name.startsWith(name)) {
							if (matchIdx < 0) {
								matchIdx = defIdx;
							}
							else {
								failCount++;
								match[nodeIdx] = FAIL;
								continue ITER_ARGS;
							}
						}
					}
					if (matchIdx >= 0) {
						if (partialArgs == null) {
							partialArgs = new FCall.Arg[testStop];
							partialArgs[matchIdx] = argNode;
							match[nodeIdx] = matchIdx;
							continue ITER_ARGS;
						}
						if (partialArgs[matchIdx] == null) {
							partialArgs[matchIdx] = argNode;
							match[nodeIdx] = matchIdx;
							continue ITER_ARGS;
						}
					}
					ellipsisCount++;
					match[nodeIdx] = ellipsisType;
					continue ITER_ARGS;
				}
			}
			if (partialArgs != null) {
				for (int i = 0; i < testStop; i++) {
					if (partialArgs[i] != null) {
						allocatedArgs[i] = partialArgs[i];
					}
				}
			}
		}
		
		if (testPosition) {
			int defIdx = 0;
			ITER_ARGS: for (int nodeIdx = 0; nodeIdx < nodeArgsCount; nodeIdx++) {
				if (match[nodeIdx] == TEST_POSITION) {
					ITER_DEFS: while (defIdx < testStop) {
						if (allocatedArgs[defIdx] == null) {
							match[nodeIdx] = defIdx;
							allocatedArgs[defIdx++] = argsNode.getChild(nodeIdx);
							continue ITER_ARGS;
						}
						else {
							defIdx++;
							continue ITER_DEFS;
						}
					}
					match[nodeIdx] = ellipsisType;
					ellipsisCount++;
					continue ITER_ARGS;
				}
			}
		}
		
		if (ellipsisType != ELLIPSIS) {
			failCount += ellipsisCount;
			ellipsisCount = 0;
		}
		final FCall.Arg[] ellipsisArgs = (ellipsisCount > 0) ? new FCall.Arg[ellipsisCount] : NO_ARGS;
		final FCall.Arg[] otherArgs = (failCount > 0) ? new FCall.Arg[failCount] : NO_ARGS;
		if (ellipsisCount > 0 || failCount > 0) {
			int ellipsisIdx = 0;
			int otherIdx = 0;
			ITER_ARGS: for (int nodeIdx = 0; nodeIdx < nodeArgsCount; nodeIdx++) {
				switch (match[nodeIdx]) {
				case ELLIPSIS:
					ellipsisArgs[ellipsisIdx++] = argsNode.getChild(nodeIdx);
					match[nodeIdx] = ellipsisDefIdx;
					continue ITER_ARGS;
				case FAIL:
					otherArgs[otherIdx++] = argsNode.getChild(nodeIdx);
					continue ITER_ARGS;
				}
			}
		}
		return new FCallArgMatch(argsDef, argsNode,
				allocatedArgs, ellipsisArgs, otherArgs, match);
	}
	
	/**
	 * @return position of the element name, if possible (symbol or strings), otherwise null
	 */
	public static Position getElementNamePosition(final RAstNode node) {
		switch (node.getNodeType()) {
		case SYMBOL:
			if (node.getOperator(0) == RTerminal.SYMBOL_G) {
				if ((node.getStatusCode() & IRSourceConstants.STATUS_MASK_12) == IRSourceConstants.STATUS12_SYNTAX_TOKEN_NOT_CLOSED) {
					return new Position(node.getOffset()+1, node.getLength()-1);
				}
				return new Position(node.getOffset()+1, node.getLength()-2);
			}
			return new Position(node.getOffset(), node.getLength());
		case STRING_CONST:
			if ((node.getStatusCode() & IRSourceConstants.STATUS_MASK_12) == IRSourceConstants.STATUS12_SYNTAX_TOKEN_NOT_CLOSED) {
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
				if ((node.getStatusCode() & IRSourceConstants.STATUS_MASK_12) == IRSourceConstants.STATUS12_SYNTAX_TOKEN_NOT_CLOSED) {
					return new Region(node.getOffset()+1, node.getLength()-1);
				}
				return new Region(node.getOffset()+1, node.getLength()-2);
			}
			return node;
		case STRING_CONST:
			if ((node.getStatusCode() & IRSourceConstants.STATUS_MASK_12) == IRSourceConstants.STATUS12_SYNTAX_TOKEN_NOT_CLOSED) {
				return new Region(node.getOffset()+1, node.getLength()-1);
			}
			return new Region(node.getOffset()+1, node.getLength()-2);
		default:
			return node;
		}
	}
	
	public static boolean hasErrors(final RAstNode node) {
		return ((node.getStatusCode() & 
				(IRSourceConstants.STATUSFLAG_REAL_ERROR
						| IRSourceConstants.STATUSFLAG_ERROR_IN_CHILD )) != 0 );
	}
	
	public static int[] computeRExpressionIndex(RAstNode node, final RAstNode baseNode) {
		final IntList topdown = new ArrayIntList();
		while (node != baseNode) {
			final RAstNode parent = node.getRParent();
			switch (parent.getNodeType()) {
			
			// list
			case SOURCELINES:
			case F_DEF_ARGS:
			// [[1]] = name
			case F_CALL:
				topdown.add(parent.getChildIndex(node) + 1);
				node = parent;
				continue;
			
			// [[1]] = operator
			case BLOCK:
			case GROUP:
			case SUB_INDEXED_S:
			case SUB_INDEXED_D:
			case NS_GET:
			case NS_GET_INT:
			case SUB_NAMED_PART:
			case SUB_NAMED_SLOT:
			case POWER:
			case SIGN:
			case SEQ:
			case SPECIAL:
			case MULT:
			case ADD:
			case RELATIONAL:
			case NOT:
			case AND:
			case OR:
			case MODEL:
			case A_LEFT:
			case A_RIGHT:
			case A_EQUALS:
			case A_COLON:
			case HELP:
			case C_IF:
			case C_FOR:
			case C_WHILE:
			case C_REPEAT:
			case F_DEF:
				topdown.add(parent.getChildIndex(node) + 2);
				node = parent;
				continue;
			
			// part of parent element
			case SUB_INDEXED_ARGS:
				if (parent == baseNode) {
					break;
				}
				topdown.add(parent.getChildIndex(node) + 3);
				node = parent.getRParent(); 
				break;
			case C_IN:
			case F_CALL_ARGS:
				if (parent == baseNode) {
					break;
				}
				topdown.add(parent.getChildIndex(node) + 2);
				node = parent.getRParent(); 
				break;
			
			case SUB_INDEXED_ARG:
			case F_DEF_ARG:
			case F_CALL_ARG:
				node = parent;
				continue;
			
			case ERROR:
			case ERROR_TERM:
			case DUMMY:
				return null;
			
			default:
				throw new IllegalStateException("Unexpected parent");
			}
		}
		final int l = topdown.size();
		final int[] path = new int[l];
		for (int i = 0; i < l;) {
			path[i] = topdown.get(l - ++i);
		}
		return path;
	}
	
	public static RAstNode getRRootNode(RAstNode node, final IRegion region) {
		if (region == null) {
			return node.getRRoot();
		}
		while (node.getRParent() != null) {
			final RAstNode parent = node.getRParent();
			final int beginDiff;
			final int endDiff;
			if ((beginDiff = region.getOffset() - parent.getOffset()) > 0
					|| (endDiff = region.getOffset()+region.getLength() - parent.getOffset()-parent.getLength()) < 0 ) {
				return node;
			}
			else if (beginDiff == 0 && endDiff == 0) {
				return (parent.getNodeType() == NodeType.SOURCELINES) ? node : parent;
			}
			else {
				node = parent;
				continue;
			}
		}
		return node;
	}
	
	public static boolean isParentChild(final RAstNode parent, RAstNode child) {
		while ((child = child.getRParent()) != null) {
			if (child == parent) {
				return true;
			}
		}
		return false;
	}
	
	
	public static Object toJava(RAstNode node) {
		while (node != null) {
			switch (node.getNodeType()) {
			case NUM_CONST:
				switch (node.getOperator(0)) {
				case NUM_NUM:
					return parseNum(node.getText());
				case NUM_INT:
					return parseInt(node.getText());
				case TRUE:
					return Boolean.TRUE;
				case FALSE:
					return Boolean.FALSE;
				default:
					break;
				}
				return null;
			case F_CALL_ARG:
				node = ((FCall.Arg) node).getValueChild();
				continue;
			default:
				return null;
			}
		}
		return null;
	}
	
	public static Integer toJavaInt(RAstNode node) {
		while (node != null) {
			switch (node.getNodeType()) {
			case NUM_CONST:
				switch (node.getOperator(0)) {
				case NUM_NUM: {
					final Double num = parseNum(node.getText());
					if (num != null && num.doubleValue() == Math.rint(num.doubleValue())) {
						return num.intValue();
					}
					break; }
				case NUM_INT:
					return parseInt(node.getText());
				case TRUE:
					return 1;
				case FALSE:
					return 0;
				default:
					break;
				}
				return null;
			case F_CALL_ARG:
				node = ((FCall.Arg) node).getValueChild();
				continue;
			default:
				return null;
			}
		}
		return null;
	}
	
	public static Float toJavaFloat(RAstNode node) {
		while (node != null) {
			switch (node.getNodeType()) {
			case NUM_CONST:
				switch (node.getOperator(0)) {
				case NUM_NUM: {
					final Double num = parseNum(node.getText());
					if (num != null && Math.abs(num.doubleValue()) <= Float.MAX_VALUE) {
						return num.floatValue();
					}
					break; }
				case NUM_INT: {
					final Integer num = parseInt(node.getText());
					if (num != null) {
						return num.floatValue();
					}
					break; }
				case TRUE:
					return 1f;
				case FALSE:
					return 0f;
				default:
					break;
				}
				return null;
			case F_CALL_ARG:
				node = ((FCall.Arg) node).getValueChild();
				continue;
			default:
				return null;
			}
		}
		return null;
	}
	
	public static Double toJavaDouble(RAstNode node) {
		while (node != null) {
			switch (node.getNodeType()) {
			case NUM_CONST:
				switch (node.getOperator(0)) {
				case NUM_NUM: {
					final Double num = parseNum(node.getText());
					if (num != null) {
						return num.doubleValue();
					}
					break; }
				case NUM_INT: {
					final Integer num = parseInt(node.getText());
					if (num != null) {
						return num.doubleValue();
					}
					break; }
				case TRUE:
					return 1.0;
				case FALSE:
					return 0.0;
				default:
					break;
				}
				return null;
			case F_CALL_ARG:
				node = ((FCall.Arg) node).getValueChild();
				continue;
			default:
				return null;
			}
		}
		return null;
	}
	
	private static Double parseNum(final String text) {
		if (text != null && !text.isEmpty()) {
			try {
				return Double.valueOf(text);
			}
			catch (final NumberFormatException e) {}
		}
		return null;
	}
	
	private static Integer parseInt(String text) {
		if (text != null && !text.isEmpty()) {
			try {
				if (text.endsWith("L")) { //$NON-NLS-1$
					text = text.substring(0, text.length() - 1);
				}
				if (text.startsWith("0x")) { //$NON-NLS-1$
					text = text.substring(2);
					return Integer.parseInt(text, 16);
				}
				else {
					return Integer.parseInt(text);
				}
			}
			catch (final NumberFormatException e) {}
		}
		return null;
	}
	
}
