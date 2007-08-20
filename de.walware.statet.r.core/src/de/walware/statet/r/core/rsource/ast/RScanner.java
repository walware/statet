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

import org.eclipse.core.runtime.IStatus;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.text.SourceParseInput;

import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.ast.RAstNode.Assoc;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 *
 */
public class RScanner {

	
	private final class ExprContext {
		final RAstNode rootNode;
		Expression rootExpr;
		RAstNode lastNode;
		Expression openExpr;
		final boolean eatLines;
		
		public ExprContext(RAstNode node, Expression expr, boolean eatLines) {
			this.rootNode = this.lastNode = node;
			this.rootExpr = this.openExpr = expr;
			this.eatLines = eatLines;
		}
		
		void setOpenExpr(Expression expr) {
			if (expr != null && expr.node != null) {
				openExpr = null;
			}
			else {
				openExpr = expr;
			}
		}
	}
	
	
	private final RScannerLexer fLexer;
	private final RScannerLexer.ScannerToken fNext;
	private final RScannerPostExprVisitor fPostVisitor = new RScannerPostExprVisitor();
	private final AstInfo fAst;

	private RTerminal fNextType;
	private boolean fWasLinebreak;
	
	
	/**
	 */
	public RScanner(SourceParseInput input, AstInfo ast) {
		if (ast.level == 1) {
			fLexer = new RScannerLexer(input);
		}
		else {
			fLexer = new RScannerDefaultLexer(input);
		}
		fNext = fLexer.getToken();
		fAst = ast;
	}


	public SourceComponent scanSourceUnit() {
		try {
			consumeToken();
			SourceComponent rootNode = scanSourceUnit(null);
			rootNode.updateStartOffset();
			rootNode.updateStopOffset();
			return rootNode;
		}
		catch (Exception e) {
			RCorePlugin.logError(-1, "Error occured while parsing R code", e);
			SourceComponent dummy = new SourceComponent();
			dummy.fStatus = RAst.STATUS_PARSE_ERROR;
			return dummy;
		}
	}
	
	final SourceComponent scanSourceUnit(final RAstNode parent) {
		SourceComponent node = new SourceComponent();
		node.fParent = parent;
		scanInExprList(node, true);
//		if (fNextType == RTerminal.EOF) {
//			fNext.type = null;
//		}
		return node;
	}
	
	final void scanInExprList(final ExpressionList node, boolean force) {
		ITER_TOKEN : while(fNext != null) {
			switch (fNextType) {
			
			case EOF:
				break ITER_TOKEN;
				
			case LINEBREAK:
				consumeToken();
				continue ITER_TOKEN;
			
			default:
				{
					Expression expr = node.appendNewExpr();
					ExprContext context = new ExprContext(node, expr, false);
					scanInExpression(context);
					
					if (expr.node == null) {
						node.fExpressions.remove(context.rootExpr);
						expr = null;
					}
					else {
						checkExpression(context);
					}
					switch (fNextType) {
					
					case SEMI:
						if (expr != null) {
							node.setSeparator(fNext.offset);
							consumeToken();
							continue ITER_TOKEN;
						}
						// else error like comma
					case COMMA:
						{
							expr = node.appendNewExpr();
							expr.node = errorFromNext(node);
						}
						continue ITER_TOKEN;
						
					case SUB_INDEXED_CLOSE:
					case BLOCK_CLOSE:
					case GROUP_CLOSE:
						if (force) {
							expr = node.appendNewExpr();
							expr.node = errorFromNext(node);
							continue ITER_TOKEN;
						}
						break ITER_TOKEN;
					}
				}
			}
		}
	}
	
	final void scanInGroup(RAstNode node, Expression expr) {
		ExprContext context = new ExprContext(node, expr, true);
		scanInExpression(context);
		checkExpression(context);
	}
	
	final void scanInExpression(ExprContext context) {
		fWasLinebreak = false;
		ITER_TOKEN : while(true) {
			
			if (fWasLinebreak && !context.eatLines && context.openExpr == null) {
				break ITER_TOKEN;
			}

			switch (fNextType) {
			
			case LINEBREAK:
				if (!context.eatLines && context.openExpr == null) {
					break ITER_TOKEN;
				}
				consumeToken();
				continue ITER_TOKEN;
				
			case SYMBOL:
			case ELLIPSIS:
				if (fWasLinebreak && context.openExpr == null) {
					break ITER_TOKEN;
				}
				appendNonOp(context, createSymbol(null));
				continue ITER_TOKEN;
				
			case TRUE:
			case FALSE:
			case NUM_NUM:
			case NUM_INT:
			case NUM_COMPLEX:
			case NA:
			case NA_REAL:
			case NA_INT:
			case NA_CPLX:
			case NA_CHAR:
			case NAN:
			case INF:
				if (fWasLinebreak && context.openExpr == null) {
					break ITER_TOKEN;
				}
				appendNonOp(context, createNumberConst(null));
				continue ITER_TOKEN;
			case NULL:
				if (fWasLinebreak && context.openExpr == null) {
					break ITER_TOKEN;
				}
				appendNonOp(context, createNullConst(null));
				continue ITER_TOKEN;
				
			case STRING_D:
			case STRING_S:
				if (fWasLinebreak && context.openExpr == null) {
					break ITER_TOKEN;
				}
				appendNonOp(context, createStringConst(null));
				continue ITER_TOKEN;
				
			case ARROW_LEFT_S:
			case ARROW_LEFT_D:
			case ARROW_RIGHT_S:
			case ARROW_RIGHT_D:
			case EQUAL:
				appendOp(context, createAssignment());
				continue ITER_TOKEN;
				
			case TILDE:
				if (context.openExpr != null) {
					appendNonOp(context, createModel());
				}
				else {
					appendOp(context, createModel());
				}
				continue ITER_TOKEN;
				
			case PLUS:
			case MINUS:
				if (context.openExpr != null) {
					appendNonOp(context, createSign());
				}
				else {
					appendOp(context, createArithmetic());
				}
				continue ITER_TOKEN;
			case MULT:
			case DIV:
				appendOp(context, createArithmetic());
				continue ITER_TOKEN;
			case POWER:
				appendOp(context, createPower());
				continue ITER_TOKEN;
				
			case SEQ:
				appendOp(context, createSeq());
				continue ITER_TOKEN;
				
			case SPECIAL:
				appendOp(context, createSpecial());
				continue ITER_TOKEN;
				
			case REL_LT:
			case REL_LE:
			case REL_EQ:
			case REL_GE:
			case REL_GT:
			case REL_NE:
				appendOp(context, createRelational());
				continue ITER_TOKEN;
				
			case OR:
			case OR_D:
			case AND:
			case AND_D:
				appendOp(context, createLogical());
				continue ITER_TOKEN;
				
			case NOT:
				if (fWasLinebreak && context.openExpr == null) {
					break ITER_TOKEN;
				}
				appendNonOp(context, createSign());
				continue ITER_TOKEN;

			case IF:
				if (fWasLinebreak && context.openExpr == null) {
					break ITER_TOKEN;
				}
				appendNonOp(context, scanCIf(context));
				continue ITER_TOKEN;
			case ELSE:
				if (context.rootNode.getNodeType() == NodeType.C_IF) {
					break ITER_TOKEN;
				}
				if (fWasLinebreak && context.openExpr == null) {
					break ITER_TOKEN;
				}
				appendNonOp(context, scanCElse(context));
				continue ITER_TOKEN;

			case FOR:
				if (fWasLinebreak && context.openExpr == null) {
					break ITER_TOKEN;
				}
				appendNonOp(context, scanCForLoop(context));
				continue ITER_TOKEN;
			case REPEAT:
				if (fWasLinebreak && context.openExpr == null) {
					break ITER_TOKEN;
				}
				appendNonOp(context, scanCRepeatLoop(context));
				continue ITER_TOKEN;
			case WHILE:
				if (fWasLinebreak && context.openExpr == null) {
					break ITER_TOKEN;
				}
				appendNonOp(context, scanCWhileLoop(context));
				continue ITER_TOKEN;

			case BREAK:
				if (fWasLinebreak && context.openExpr == null) {
					break ITER_TOKEN;
				}
				appendNonOp(context, createLoopCommand());
				continue ITER_TOKEN;
			case NEXT:
				if (fWasLinebreak && context.openExpr == null) {
					break ITER_TOKEN;
				}
				appendNonOp(context, createLoopCommand());
				continue ITER_TOKEN;
			
			case FUNCTION:
				if (fWasLinebreak && context.openExpr == null) {
					break ITER_TOKEN;
				}
				appendNonOp(context, scanFDef(context));
				continue ITER_TOKEN;
				
			case GROUP_OPEN:
				if (context.openExpr != null) {
					appendNonOp(context, scanGroup());
				}
				else {
					appendOp(context, scanFCall());
				}
				continue ITER_TOKEN;
				
			case BLOCK_OPEN:
				if (fWasLinebreak && context.openExpr == null) {
					break ITER_TOKEN;
				}
				appendNonOp(context, scanBlock());
				continue ITER_TOKEN;

			case EOF:
				break ITER_TOKEN;
				
			case NS_GET:
			case NS_GET_INT:
				if (fWasLinebreak && context.openExpr == null) {
					break ITER_TOKEN;
				}
				appendNonOp(context, scanNSGet(context));
				continue ITER_TOKEN;
				
			case SUB_INDEXED_S_OPEN:
			case SUB_INDEXED_D_OPEN:
				appendOp(context, scanSubIndexed(context));
				continue ITER_TOKEN;
				
			case SUB_NAMED:
			case SUB_AT:
				appendOp(context, scanSubNamed(context));
				continue ITER_TOKEN;
				
			case QUESTIONMARK:
				if (context.openExpr != null) {
					appendNonOp(context, createHelp());
					continue ITER_TOKEN;
				}
				else {
					appendOp(context, createHelp());
					continue ITER_TOKEN;
				}
				
			case UNKNOWN:
			case IN:
				appendNonOp(context, errorFromNext(null));
				continue ITER_TOKEN;
				
			case COMMA:
			case SEMI:
			case SUB_INDEXED_CLOSE:
			case BLOCK_CLOSE:
			case GROUP_CLOSE:
				break ITER_TOKEN;
				
			default:
				throw new IllegalStateException("Unhandled token in expr-scanner: "+fNextType.name());
			}
				
		}
	}
	
	final Group scanGroup() {
		Group node = new Group();
		setupFromSourceToken(node);
		consumeToken();
		scanInGroup(node, node.fExpr);
		checkExpression(node, node.fExpr);
		if (fNextType == RTerminal.GROUP_CLOSE) {
			node.fGroupCloseOffset = fNext.offset;
			consumeToken();
			node.updateStopOffset();
		}
		else {
			node.fStopOffset = fNext.offset;
		}
		return node;
	}
	
	final Block scanBlock() {
		final Block node = new Block();
		setupFromSourceToken(node);
		consumeToken();
		scanInExprList(node, false);
		if (fNextType == RTerminal.BLOCK_CLOSE) {
			node.fBlockCloseOffset = fNext.offset;
			consumeToken();
			node.updateStopOffset();
		}
		else {
			node.fStopOffset = fNext.offset;
		}
		return node;
	}
	
	final NSGet scanNSGet(ExprContext context) {
		final NSGet node;
		switch (fNextType) {
		case NS_GET:
			node = new NSGet.Std();
			break;
		case NS_GET_INT:
			node = new NSGet.Internal();
			break;
		default:
			throw new IllegalStateException();
		}
		setupFromSourceToken(node);
		node.fOperatorOffset = fNext.offset;
		consumeToken();

		// setup ns
		switch (context.lastNode.getNodeType()) {
		case SYMBOL:
		case STRING_CONST:
			{
				node.fNamespace = (SingleValue) context.lastNode;
				final RAstNode base = context.lastNode.getParent();
				node.fNamespace.fParent = node;
				context.lastNode = base;
				context.openExpr = base.getExpr(node.fNamespace);
				context.openExpr.node = null;
				break;
			}
		default:
			{
				node.fNamespace = errorNonExistingSymbol(node, node.fStartOffset);
				break;
			}
		}

		// element
		ITER_TOKEN : while (true) {
			switch (fNextType) {
			
			case STRING_S:
			case STRING_D:
				node.fElement = createStringConst(node);
				break ITER_TOKEN;
			
			case SYMBOL:
				node.fElement = createSymbol(node);
				break ITER_TOKEN;
				
			default:
				break ITER_TOKEN;
			}
		}
		return node;
	}

	final SubNamed scanSubNamed(ExprContext context) {
		final SubNamed node;
		switch (fNextType) {
		case SUB_NAMED:
			node = new SubNamed.Named();
			break;
		case SUB_AT:
			node = new SubNamed.Slot();
			break;
		default:
			throw new IllegalStateException();
		}
		setupFromSourceToken(node);
		consumeToken();
		readLines();
		
		switch (fNextType) {
		case STRING_S:
		case STRING_D:
			node.fSubname = createStringConst(node);
			break;
		case SYMBOL:
			node.fSubname = createSymbol(node);
			break;
		default:
			node.fSubname = errorNonExistingSymbol(node, node.fStopOffset);
			break;
		}
		node.updateStopOffset();
		return node;
	}
	
	final SubIndexed scanSubIndexed(ExprContext context) {
		final SubIndexed node;
		switch (fNextType) {
		case SUB_INDEXED_S_OPEN:
			node = new SubIndexed.S();
			break;
		case SUB_INDEXED_D_OPEN:
			node = new SubIndexed.D();
			break;
		default:
			throw new IllegalStateException();
		}
		setupFromSourceToken(node);
		node.fOpenOffset = fNext.offset;
		consumeToken();
		readLines();
		
		node.fSublist.fStartOffset = node.fStopOffset;
		scanInSpecArgs(node.fSublist);
		node.fSublist.fStopOffset = fNext.offset;

		if (fNextType == RTerminal.SUB_INDEXED_CLOSE) {
			node.fCloseOffset = fNext.offset;
			consumeToken();
			
			if (node.getNodeType() == NodeType.SUB_INDEXED_D
					&& fNextType == RTerminal.SUB_INDEXED_CLOSE) {
				node.fClose2Offset = fNext.offset;
				consumeToken();
			}
		}
		node.updateStopOffset();
		return node;
	}

	final CIfElse scanCIf(final ExprContext context) {
		final CIfElse node = new CIfElse();
		setupFromSourceToken(node);
		consumeToken();
		boolean ok = false;
		readLines();
		
		if (fNextType == RTerminal.GROUP_OPEN) {
			node.fCondOpenOffset = fNext.offset;
			consumeToken();
			readLines();
			
			// condition
			node.updateStopOffset();
			scanInGroup(node, node.fCondExpr);
			
			if (fNextType == RTerminal.GROUP_CLOSE) {
				node.fCondCloseOffset = fNext.offset;
				consumeToken();
				ok = true;
				readLines();
			}
		}
		else {
			node.updateStopOffset();
			checkExpression(node, node.fCondExpr);
		}

		// then
		node.updateStopOffset();
		if (ok || recoverCCont()) {
			ExprContext thenContext = new ExprContext(node, node.fThenExpr, context.eatLines);
			scanInExpression(thenContext);
			checkExpression(thenContext);
			readLines();
		}
		else {
			node.fThenExpr.node = errorNonExistExpression(node, node.fCondExpr.node.fStopOffset, RAst.STATUS_SKIPPED_EXPR);
		}
		
		// else
		if (fNextType == RTerminal.ELSE) {
			node.fWithElse = true;
			node.fElseOffset = fNext.offset;
			consumeToken();
		}
		
		return node;
	}
	
	final CIfElse scanCElse(final ExprContext context) {
		final CIfElse node = new CIfElse();
		setupFromSourceToken(node);
		node.fCondExpr.node = errorNonExistExpression(node, node.fStartOffset, RAst.STATUS_SKIPPED_EXPR);
		node.fThenExpr.node = errorNonExistExpression(node, node.fStartOffset, RAst.STATUS_SKIPPED_EXPR);
		node.fElseOffset = fNext.offset;
		node.fWithElse = true;
		consumeToken();

		return node;
	}

	final CForLoop scanCForLoop(final ExprContext context) {
		final CForLoop node = new CForLoop();
		setupFromSourceToken(node);
		consumeToken();
		boolean ok = false;
		readLines();
		
		if (fNextType == RTerminal.GROUP_OPEN) {
			node.fCondOpenOffset = fNext.offset;
			consumeToken();
			readLines();
			
			// condition
			if (fNextType == RTerminal.SYMBOL) {
				node.fVarSymbol = createSymbol(node);
				readLines();
			}
			if (fNextType == RTerminal.IN) {
				node.fInOffset = fNext.offset;
				consumeToken();
				readLines();
			}
			scanInGroup(node, node.fCondExpr);
			
			if (fNextType == RTerminal.GROUP_CLOSE) {
				node.fCondCloseOffset = fNext.offset;
				consumeToken();
				ok = true;
				readLines();
			}
		}
		
		node.updateStopOffset();
		if (node.fVarSymbol == null) {
			node.fVarSymbol = errorNonExistingSymbol(node, node.fStopOffset);
		}
		checkExpression(node, node.fCondExpr);

		// loop
		node.updateStopOffset();;
		if (!ok && !recoverCCont()) {
			node.fLoopExpr.node = errorNonExistExpression(node, node.fStopOffset, RAst.STATUS_SKIPPED_EXPR);
		}
		
		return node;
	}
	
	final CWhileLoop scanCWhileLoop(final ExprContext context) {
		final CWhileLoop node = new CWhileLoop();
		setupFromSourceToken(node);
		consumeToken();
		boolean ok = false;
		readLines();
		
		if (fNextType == RTerminal.GROUP_OPEN) {
			node.fCondOpenOffset = fNext.offset;
			consumeToken();
			readLines();
			
			// condition
			node.updateStopOffset();
			scanInGroup(node, node.fCondExpr);
			
			if (fNextType == RTerminal.GROUP_CLOSE) {
				node.fCondCloseOffset = fNext.offset;
				consumeToken();
				ok = true;
				readLines();
			}
		}
		else {
			node.updateStopOffset();
			checkExpression(node, node.fCondExpr);
		}
		
		// loop
		node.updateStopOffset();
		if (!ok && !recoverCCont()) {
			node.fLoopExpr.node = errorNonExistExpression(node, node.fStopOffset, RAst.STATUS_SKIPPED_EXPR);
		}
		
		return node;
	}

	final CRepeatLoop scanCRepeatLoop(final ExprContext context) {
		final CRepeatLoop node = new CRepeatLoop();
		setupFromSourceToken(node);
		consumeToken();

		return node;
	}

	final FDef scanFDef(final ExprContext context) {
		final FDef node = new FDef();
		setupFromSourceToken(node);
		consumeToken();
		boolean ok = false;
		readLines();

		if (fNextType == RTerminal.GROUP_OPEN) {
			node.fArgsOpenOffset = fNext.offset;
			consumeToken();
			readLines();

			// args
			node.fArgs.fStartOffset = node.fArgsOpenOffset+1;
			scanInFDefArgs(node.fArgs);
			node.fArgs.fStopOffset = fNext.offset;
			
			if (fNextType == RTerminal.GROUP_CLOSE) {
				node.fArgsCloseOffset = fNext.offset;
				consumeToken();
				ok = true;
				readLines();
			}
		}
		else {
			node.fArgs.fStartOffset = node.fArgs.fStopOffset = node.fStopOffset;
		}
		
		// content
		node.updateStopOffset();
		if (!ok && !recoverCCont()) {
			node.fExpr.node = errorNonExistExpression(node, node.fStopOffset, RAst.STATUS_SKIPPED_EXPR);
		}

		return node;
	}
	
	final FCall scanFCall() {
		final FCall node = new FCall();
		node.fArgsOpenOffset = fNext.offset;
		setupFromSourceToken(node);
		consumeToken();
		readLines();

		node.fArgs.fStartOffset = node.fStopOffset;
		scanInSpecArgs(node.fArgs);
		node.fArgs.fStopOffset = fNext.offset;
		if (fNextType == RTerminal.GROUP_CLOSE) {
			node.fArgsCloseOffset = fNext.offset;
			consumeToken();
		}
		node.updateStopOffset();
		return node;
	}
	
	final void scanInFDefArgs(FDef.Args args) {
		ITER_ARGS : while (true) {
			FDef.Arg arg = new FDef.Arg(args);
			switch(fNextType) {
			case SYMBOL:
			case ELLIPSIS:
				arg.fArgName = createSymbol(arg);
				readLines();
				break;
			case EQUAL:
			case COMMA:
				break;
			default:
				if (args.fSpecs.isEmpty()) {
					break ITER_ARGS;
				}
				break;
			}
			if (arg.fArgName == null) {
				arg.fArgName = errorNonExistingSymbol(arg, fNext.offset);
			}
			
			if (fNextType == RTerminal.EQUAL) {
				arg.fStopOffset = fNext.offset+1;
				consumeToken();
				
				Expression expr = arg.addDefault();
				scanInGroup(arg, expr);
			}

			arg.updateStartOffset();
			arg.updateStopOffset();
			args.fSpecs.add(arg);
			if (fNextType == RTerminal.COMMA) {
				consumeToken();
				readLines();
				continue ITER_ARGS;
			}
			else {
				break ITER_ARGS;
			}
		}
	}

	final void scanInSpecArgs(SpecList args) {
		ITER_ARGS : while (true) {
			SpecItem arg = args.createItem();
			arg.fStartOffset = fNext.offset;
			switch(fNextType) {
			case SYMBOL:
				arg.fArgName = createSymbol(arg);
				readLines();
				break;
			case STRING_S:
			case STRING_D:
				arg.fArgName = createStringConst(arg);
				readLines();
				break;
			case NULL:
				arg.fArgName = createNullConst(arg);
				readLines();
				break;
			case EQUAL:
				arg.fArgName = errorNonExistingSymbol(arg, fNext.offset);
				break;
			default:
				break;
			}
			if (arg.fArgName != null) {
				if (fNextType == RTerminal.EQUAL) {
					arg.fEqualsOffset = fNext.offset;
					consumeToken();
					arg.updateStopOffset();

					ExprContext valueContext = new ExprContext(arg, arg.fValueExpr, true);
					scanInExpression(valueContext);
					if (arg.fValueExpr.node != null) { // empty items are allowed
						checkExpression(valueContext);
					}
				}
				else {
					// symbol not argName, but valueExpr
					arg.fValueExpr.node = arg.fArgName;
					arg.fArgName = null;

					ExprContext valueContext = new ExprContext(arg, arg.fValueExpr, true);
					valueContext.lastNode = arg.fValueExpr.node;
					valueContext.openExpr = null;
					scanInExpression(valueContext);
					checkExpression(valueContext);
				}
			}
			else {
				ExprContext valueContext = new ExprContext(arg, arg.fValueExpr, true);
				scanInExpression(valueContext);
				if (arg.fValueExpr.node != null) { // empty items are allowed
					checkExpression(valueContext);
				}
			}
			
			arg.updateStopOffset();
			if (fNextType == RTerminal.COMMA) {
				args.fSpecs.add(arg);
				consumeToken();
				readLines();
				continue ITER_ARGS;
			}
			else if (!args.fSpecs.isEmpty() || arg.hasChildren()) {
				args.fSpecs.add(arg);
			}
			break ITER_ARGS;
		}
	}

	final boolean recoverCCont() {
		return !fWasLinebreak
			&& (fNextType == RTerminal.SYMBOL || fNextType == RTerminal.BLOCK_OPEN);
	}
	
	final void appendNonOp(final ExprContext context, final RAstNode newNode) {
		if (context.openExpr != null) {
			newNode.fParent = context.lastNode;
			context.openExpr.node = newNode;
		}
		else {
			// setup missing op
			Dummy.Operator error = new Dummy.Operator(RAst.STATUS_MISSING_OPERATOR);
			error.fParent = context.rootNode;
			error.fLeftExpr.node = context.rootExpr.node;
			error.fStartOffset = error.fStopOffset = newNode.fStartOffset;
			context.rootExpr.node = error;
			// append news
			newNode.fParent = error;
			error.fRightExpr.node = newNode;
			context.rootExpr.node = error;
		}
		context.lastNode = newNode;
		context.setOpenExpr(newNode.getRightExpr());
		return;
	}
	
	final void appendOp(final ExprContext context, final RAstNode newNode) {
		if (context.openExpr != null) {
			context.openExpr.node = errorNonExistExpression(context.lastNode, fNext.offset, RAst.STATUS_MISSING_EXPR);
			context.openExpr = null;
		}
		
		int newP = newNode.getNodeType().opPrec;
		RAstNode left = context.lastNode;
		RAstNode cand = context.lastNode;
		
		ITER_CAND : while (cand != null && cand != context.rootNode) {
			final NodeType candType = cand.getNodeType();
			if (candType.opPrec == newP) {
				switch (candType.opAssoc) {
				case Assoc.LEFTSTD:
				case Assoc.NOSTD:
				case Assoc.LEFTMULTI:
					left = cand;
					break ITER_CAND;
				case Assoc.RIGHTSTD:
				default:
					break ITER_CAND;
				}
			}
			if (candType.opPrec > newP) {
				break ITER_CAND;
			}
			left = cand;
			cand = cand.getParent();
		}
		
		RAstNode baseNode = left.getParent();
		if (baseNode == null) {
			return; // XXX ??
		}
		if (left.getNodeType().opPrec == newP) {
			if (left.getNodeType().opAssoc == Assoc.LEFTMULTI) {
				FlatMulti leftMulti = (FlatMulti) left;
				FlatMulti newMulti = (FlatMulti) newNode;
				context.lastNode = leftMulti;
				context.setOpenExpr(leftMulti.appendComponent(newMulti.getStopOffset(), newMulti.getOperator(1)));
				return;
			}
		}
		Expression baseExpr = baseNode.getExpr(left);
		newNode.getLeftExpr().node = left;
		left.fParent = newNode;
		baseExpr.node = newNode;
		newNode.fParent = baseNode;
		
		context.lastNode = newNode;
		context.setOpenExpr(newNode.getRightExpr());
		return;
	}
	
	Dummy.Terminal errorNonExistExpression(RAstNode parent, int stopHint, IStatus status) {
		final Dummy.Terminal error = new Dummy.Terminal(status);
		error.fParent = parent;
		error.fStartOffset = error.fStopOffset = (stopHint >= 0) ? stopHint : parent.fStopOffset;
		error.fText = "";
		return error;
	}
	
	Dummy.Terminal errorFromNext(final RAstNode parent) {
		final Dummy.Terminal error = new Dummy.Terminal((fNextType == RTerminal.UNKNOWN) ?
				RAst.STATUS_UNEXEPTEC_TOKEN : RAst.STATUS_UNKNOWN_TOKEN);
		error.fParent = parent;
		setupFromSourceToken(error);
		error.fText = fNext.text;
		consumeToken();
		return error;
	}
	
	Symbol errorNonExistingSymbol(final RAstNode parent, final int offset) {
		final Symbol error = new Symbol();
		error.fParent = parent;
		error.fStartOffset = error.fStopOffset = offset;
		error.fText = "";
//		error.fStatus = ;
		return error;
	}
	
	protected Symbol createSymbol(final RAstNode parent) {
		final Symbol symbol = new Symbol();
		symbol.fParent = parent;
		setupFromSourceToken(symbol);
		consumeToken();
		return symbol;
	}
	
	protected NumberConst createNumberConst(final RAstNode parent) {
		NumberConst num = new NumberConst();
		num.fParent = parent;
		setupFromSourceToken(num);
		consumeToken();
		return num;
	}

	protected NullConst createNullConst(final RAstNode parent) {
		final NullConst num = new NullConst();
		num.fParent = parent;
		setupFromSourceToken(num);
		consumeToken();
		return num;
	}

	protected StringConst createStringConst(final RAstNode parent) {
		final StringConst str = new StringConst();
		str.fParent = parent;
		setupFromSourceToken(str);
		consumeToken();
		return str;
	}
	
	protected Assignment createAssignment() {
		Assignment node;
		switch (fNextType) {
		case ARROW_LEFT_S:
			node = new Assignment.LeftS();
			break;
		case ARROW_LEFT_D:
			node = new Assignment.LeftD();
			break;
		case ARROW_RIGHT_S:
			node = new Assignment.RightS();
			break;
		case ARROW_RIGHT_D:
			node = new Assignment.RightD();
			break;
		case EQUAL:
			node = new Assignment.LeftE();
			break;
		default:
			throw new IllegalArgumentException();
		}
		setupFromSourceToken(node);
		consumeToken();
		return node;
	}
	
	protected Model createModel() {
		final Model node = new Model();
		setupFromSourceToken(node);
		consumeToken();
		return node;
	}
	
	protected CLoopCommand createLoopCommand() {
		final CLoopCommand node;
		switch (fNextType) {
		case NEXT:
			node = new CLoopCommand.Next();
			break;
		case BREAK:
			node = new CLoopCommand.Break();
			break;
		default:
			throw new IllegalArgumentException();
		}
		setupFromSourceToken(node);
		consumeToken();
		return node;
	}
	
	protected Sign createSign() {
		final Sign node;
		switch (fNextType) {
		case PLUS:
			node = new Sign.PlusSign();
			break;
		case MINUS:
			node = new Sign.MinusSign();
			break;
		case NOT:
			node = new Sign.Not();
			break;
		default:
			throw new IllegalArgumentException();
		}
		setupFromSourceToken(node);
		consumeToken();
		return node;
	}
	
	protected FlatMulti createArithmetic() {
		final FlatMulti node;
		switch (fNextType) {
		case PLUS:
		case MINUS:
			node = new Arithmetic.Add(fNextType);
			break;
		case MULT:
		case DIV:
			node = new Arithmetic.Mult(fNextType);
			break;
		default:
			throw new IllegalArgumentException();
		}
		setupFromSourceToken(node);
		consumeToken();
		return node;
	}
	
	protected Power createPower() {
		final Power node = new Power();
		setupFromSourceToken(node);
		consumeToken();
		return node;
	}

	protected Seq createSeq() {
		final Seq node = new Seq();
		setupFromSourceToken(node);
		consumeToken();
		return node;
	}

	protected Special createSpecial() {
		final Special node = new Special();
		setupFromSourceToken(node);
		consumeToken();
		return node;
	}

	protected Relational createRelational() {
		final Relational node;
		switch (fNextType) {
		case REL_LT:
			node = new Relational.LT();
			break;
		case REL_LE:
			node = new Relational.LE();
			break;
		case REL_EQ:
			node = new Relational.EQ();
			break;
		case REL_GE:
			node = new Relational.GE();
			break;
		case REL_GT:
			node = new Relational.GT();
			break;
		case REL_NE:
			node = new Relational.NE();
			break;
		default:
			throw new IllegalArgumentException();
		}
		setupFromSourceToken(node);
		consumeToken();
		return node;
	}

	protected FlatMulti createLogical() {
		final FlatMulti node;
		switch (fNextType) {
		case AND:
		case AND_D:
			node = new Logical.And(fNextType);
			break;
		case OR:
		case OR_D:
			node = new Logical.Or(fNextType);
			break;
		default:
			throw new IllegalArgumentException();
		}
		setupFromSourceToken(node);
		consumeToken();
		return node;
	}

	protected Help createHelp() {
		Help node = new Help();
		setupFromSourceToken(node);
		consumeToken();
		return node;
	}
	
	protected void setupFromSourceToken(final RAstNode node) {
		node.fStartOffset = fNext.offset;
		node.fStopOffset = fNext.offset+fNext.length;
		node.fStatus = fNext.status;
	}
	
	protected void setupFromSourceToken(final SingleValue node) {
		node.fStartOffset = fNext.offset;
		node.fStopOffset = fNext.offset+fNext.length;
		node.fText = fNext.text;
		node.fStatus = fNext.status;
	}

	void checkExpression(ExprContext context) {
		if (context.openExpr != null && context.openExpr.node == null) {
			context.openExpr.node = errorNonExistExpression(context.lastNode, context.lastNode.fStopOffset, RAst.STATUS_MISSING_EXPR);
		}
		context.rootExpr.node.accept(fPostVisitor);
	}
	
	void checkExpression(RAstNode node, Expression expr) {
		if (expr != null && expr.node == null) {
			expr.node = errorNonExistExpression(node, node.fStopOffset, RAst.STATUS_MISSING_EXPR);
		}
		else {
			expr.node.accept(fPostVisitor);
		}
	}

	private final void readLines() {
		while (fNextType == RTerminal.LINEBREAK) {
			consumeToken();
		}
	}
	
	private final void consumeToken() {
		fWasLinebreak = (fNextType == RTerminal.LINEBREAK);
		FILTER_TOKEN : while (true) {
			fLexer.nextToken();
			fNextType = fNext.type;
			switch (fNextType) {
			case COMMENT:
				handleComment();
				continue FILTER_TOKEN;
			default:
				break FILTER_TOKEN;
			}
		}
	}
	
	protected void handleComment() {
	}

}
