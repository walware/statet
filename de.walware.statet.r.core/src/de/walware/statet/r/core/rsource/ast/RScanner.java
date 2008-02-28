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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.text.SourceParseInput;

import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.ast.RAstNode.Assoc;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * Scanner to create a R AST.
 */
public class RScanner {
	
	
	private static final class ExprContext {
		final RAstNode rootNode;
		final Expression rootExpr;
		RAstNode lastNode;
		Expression openExpr;
		final boolean eatLines;
		
		public ExprContext(final RAstNode node, final Expression expr, final boolean eatLines) {
			this.rootNode = this.lastNode = node;
			this.rootExpr = this.openExpr = expr;
			this.eatLines = eatLines;
		}
		
		final void update(final RAstNode lastNode, final Expression openExpr) {
			this.lastNode = lastNode;
			if (openExpr == null || openExpr.node != null) {
				this.openExpr = null;
			}
			else {
				this.openExpr = openExpr;
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
	public RScanner(final SourceParseInput input, final AstInfo ast) {
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
			fLexer.setFull();
			init();
			final SourceComponent rootNode = scanSourceUnit(null);
			return rootNode;
		}
		catch (final Exception e) {
			RCorePlugin.logError(-1, "Error occured while parsing R code", e);
			final SourceComponent dummy = new SourceComponent();
			dummy.fStatus = RAst.STATUS_PARSE_ERROR;
			return dummy;
		}
	}
	
	public SourceComponent scanSourceRange(final int offset, final int length) {
		try {
			fLexer.setRange(offset, length);
			init();
			final SourceComponent rootNode = scanSourceUnit(null);
			return rootNode;
		}
		catch (final Exception e) {
			RCorePlugin.logError(-1, "Error occured while parsing R code", e);
			final SourceComponent dummy = new SourceComponent();
			dummy.fStatus = RAst.STATUS_PARSE_ERROR;
			return dummy;
		}
	}
	
	private void init() {
		fNextType = fNext.type = RTerminal.LINEBREAK;
		consumeToken();
	}
	
	final SourceComponent scanSourceUnit(final RAstNode parent) {
		final SourceComponent node = new SourceComponent();
		node.fRParent = parent;
		scanInExprList(node, true);
//		if (fNextType == RTerminal.EOF) {
//			fNext.type = null;
//		}
		node.updateStartOffset();
		node.updateStopOffset();
		return node;
	}
	
	final void scanInExprList(final ExpressionList node, final boolean force) {
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
					final ExprContext context = new ExprContext(node, expr, false);
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
	
	final int scanInGroup(final RAstNode node, final Expression expr) {
		final ExprContext context = new ExprContext(node, expr, true);
		scanInExpression(context);
		return checkExpression(context);
	}
	
	final void scanInExpression(final ExprContext context) {
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
			case SYMBOL_G:
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
		final Group node = new Group();
		setupFromSourceToken(node);
		consumeToken();
		scanInGroup(node, node.fExpr);
		if (fNextType == RTerminal.GROUP_CLOSE) {
			node.fGroupCloseOffset = fNext.offset;
			node.fStopOffset = node.fGroupCloseOffset+1;
			consumeToken();
		}
		else {
			node.fStopOffset = fNext.offset;
			node.fStatus = RAst.STATUS_MISSING_OPERATOR;
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
			node.fStopOffset = node.fBlockCloseOffset+1;
			consumeToken();
		}
		else {
			node.fStopOffset = fNext.offset;
			node.fStatus = RAst.STATUS_MISSING_OPERATOR;
		}
		return node;
	}
	
	final NSGet scanNSGet(final ExprContext context) {
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
				node.fNamespace.fRParent = node;
				final Expression expr = base.getExpr(node.fNamespace);
				if (expr != null) {
					expr.node = null;
				}
				else {
					throw new IllegalStateException(); // ?
				}
				context.update(base, expr);
				node.fStartOffset = node.fNamespace.fStartOffset;
				break;
			}
		default:
			node.fNamespace = errorNonExistingSymbol(node, node.fStartOffset, RAst.STATUS_MISSING_SYMBOL);
			break;
		}
		
		// element
		switch (fNextType) {
		case STRING_S:
		case STRING_D:
			node.fElement = createStringConst(node);
			node.fStopOffset = node.fElement.fStopOffset;
			break;
		case SYMBOL:
		case SYMBOL_G:
			node.fElement = createSymbol(node);
			node.fStopOffset = node.fElement.fStopOffset;
			break;
		default:
			node.fElement = errorNonExistingSymbol(node, node.fStopOffset, RAst.STATUS_MISSING_SYMBOL);
			break;
		}
		return node;
	}
	
	final SubNamed scanSubNamed(final ExprContext context) {
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
		node.fOperatorOffset = fNext.offset;
		consumeToken();
		readLines();
		
		switch (fNextType) {
		case STRING_S:
		case STRING_D:
			node.fSubname = createStringConst(node);
			node.fStopOffset = node.fSubname.fStopOffset;
			break;
		case SYMBOL:
		case SYMBOL_G:
			node.fSubname = createSymbol(node);
			node.fStopOffset = node.fSubname.fStopOffset;
			break;
		default:
			node.fSubname = errorNonExistingSymbol(node, node.fStopOffset, RAst.STATUS_MISSING_SYMBOL);
			break;
		}
		return node;
	}
	
	final SubIndexed scanSubIndexed(final ExprContext context) {
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
		
		scanInSpecArgs(node.fSublist);
		
		if (fNextType == RTerminal.SUB_INDEXED_CLOSE) {
			node.fCloseOffset = fNext.offset;
			consumeToken();
			
			if (node.getNodeType() == NodeType.SUB_INDEXED_D) {
				if (fNextType == RTerminal.SUB_INDEXED_CLOSE) {
					node.fClose2Offset = fNext.offset;
					node.fStopOffset = node.fClose2Offset+1;
					consumeToken();
				}
				else {
					node.fStopOffset = node.fCloseOffset+1;
					node.fStatus = RAst.STATUS_MISSING_OPERATOR;
				}
			}
			else {
				node.fStopOffset = node.fCloseOffset+1;
				node.fStatus = RAst.STATUS_MISSING_OPERATOR;
			}
		}
		else {
			node.fStopOffset = node.fSublist.fStopOffset;
			node.fStatus = RAst.STATUS_MISSING_OPERATOR;
		}
		return node;
	}
	
	final CIfElse scanCIf(final ExprContext context) {
		final CIfElse node = new CIfElse();
		setupFromSourceToken(node);
		consumeToken();
		int ok = 0;
		readLines();
		
		if (fNextType == RTerminal.GROUP_OPEN) {
			node.fCondOpenOffset = fNext.offset;
			node.fStopOffset = fNext.offset+1;
			consumeToken();
			readLines();
			
			// condition
			scanInGroup(node, node.fCondExpr);
			
			if (fNextType == RTerminal.GROUP_CLOSE) {
				node.fCondCloseOffset = fNext.offset;
				node.fStopOffset = node.fCondCloseOffset+1;
				consumeToken();
				ok = 1;
				readLines();
			}
			else {
				node.fStopOffset = node.fCondExpr.node.fStopOffset;
				node.fStatus = RAst.STATUS_MISSING_OPERATOR;
			}
		}
		else {
			node.fCondExpr.node = errorNonExistExpression(node, node.fStopOffset, RAst.STATUS_SKIPPED_EXPR);
			node.fStatus = RAst.STATUS_MISSING_OPERATOR;
		}
		
		// then
		if (ok > 0 || recoverCCont()) {
			final ExprContext thenContext = new ExprContext(node, node.fThenExpr, context.eatLines);
			scanInExpression(thenContext);
			checkExpression(thenContext);
			node.fStopOffset = node.fThenExpr.node.fStopOffset;
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
	
	final CIfElse scanCElse(final ExprContext context) { // else without if
		final CIfElse node = new CIfElse();
		setupFromSourceToken(node);
		node.fCondExpr.node = errorNonExistExpression(node, node.fStartOffset, RAst.STATUS_SKIPPED_EXPR);
		node.fThenExpr.node = errorNonExistExpression(node, node.fStartOffset, RAst.STATUS_SKIPPED_EXPR);
		node.fElseOffset = fNext.offset;
		node.fWithElse = true;
		node.fStatus = RAst.STATUS_MISSING_OPERATOR;
		consumeToken();
		
		return node;
	}
	
	final CForLoop scanCForLoop(final ExprContext context) {
		final CForLoop node = new CForLoop();
		setupFromSourceToken(node);
		consumeToken();
		int ok = 0;
		readLines();
		
		if (fNextType == RTerminal.GROUP_OPEN) {
			node.fCondOpenOffset = fNext.offset;
			consumeToken();
			readLines();
			
			// condition
			switch (fNextType) {
			case SYMBOL:
			case SYMBOL_G:
				node.fVarSymbol = createSymbol(node);
				readLines();
				break;
			default:
				node.fVarSymbol = errorNonExistingSymbol(node, node.fCondOpenOffset+1, RAst.STATUS_MISSING_SYMBOL);
				ok--;
				break;
			}
			
			if (fNextType == RTerminal.IN) {
				node.fInOffset = fNext.offset;
				node.fStopOffset = node.fInOffset+2;
				consumeToken();
				readLines();
				
				ok+= scanInGroup(node, node.fCondExpr);
			}
			else {
				node.fStopOffset = node.fVarSymbol.fStopOffset;
				node.fStatus = (ok < 0) ? RAst.STATUS_MISSING_OPERATOR : RAst.STATUS_MISSING_OPERATOR;
				node.fCondExpr.node = errorNonExistExpression(node, node.fStopOffset, RAst.STATUS_SKIPPED_EXPR);
			}
			
			if (fNextType == RTerminal.GROUP_CLOSE) {
				node.fCondCloseOffset = fNext.offset;
				node.fStopOffset = node.fCondCloseOffset+1;
				consumeToken();
				ok = 1;
				readLines();
			}
			else {
				node.fStopOffset = node.fCondExpr.node.fStopOffset;
				node.fStatus = (ok < 0) ? RAst.STATUS_MISSING_OPERATOR : RAst.STATUS_MISSING_OPERATOR;
			}
		}
		else { // missing GROUP_OPEN
			node.fStatus = RAst.STATUS_MISSING_OPERATOR;
			node.fVarSymbol = errorNonExistingSymbol(node, node.fStopOffset, RAst.STATUS_MISSING_SYMBOL);
			node.fCondExpr.node = errorNonExistExpression(node, node.fStopOffset, RAst.STATUS_SKIPPED_EXPR);
		}
		
		// loop
		if (ok <= 0 && !recoverCCont()) {
			node.fLoopExpr.node = errorNonExistExpression(node, node.fStopOffset, RAst.STATUS_SKIPPED_EXPR);
		}
		
		return node;
	}
	
	final CWhileLoop scanCWhileLoop(final ExprContext context) {
		final CWhileLoop node = new CWhileLoop();
		setupFromSourceToken(node);
		consumeToken();
		int ok = 0;
		readLines();
		
		if (fNextType == RTerminal.GROUP_OPEN) {
			node.fCondOpenOffset = fNext.offset;
			node.fStopOffset = node.fCondOpenOffset+1;
			consumeToken();
			readLines();
			
			// condition
			ok += scanInGroup(node, node.fCondExpr);
			
			if (fNextType == RTerminal.GROUP_CLOSE) {
				node.fCondCloseOffset = fNext.offset;
				node.fStopOffset = node.fCondCloseOffset+1;
				consumeToken();
				ok = 1;
				readLines();
			}
			else {
				node.fStopOffset = node.fCondExpr.node.fStopOffset;
				node.fStatus = (ok < 0) ? RAst.STATUS_MISSING_OPERATOR : RAst.STATUS_MISSING_OPERATOR;
			}
		}
		else {
			node.fStatus = RAst.STATUS_MISSING_OPERATOR;
			node.fCondExpr.node = errorNonExistExpression(node, node.fStopOffset, RAst.STATUS_SKIPPED_EXPR);
		}
		
		// loop
		if (ok <= 0 && !recoverCCont()) {
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
		int ok = 0;
		readLines();
		
		if (fNextType == RTerminal.GROUP_OPEN) {
			node.fArgsOpenOffset = fNext.offset;
			node.fStopOffset = node.fArgsOpenOffset+1;
			consumeToken();
			readLines();
			
			// args
			scanInFDefArgs(node.fArgs);
			
			if (fNextType == RTerminal.GROUP_CLOSE) {
				node.fArgsCloseOffset = fNext.offset;
				node.fStopOffset = node.fArgsCloseOffset+1;
				consumeToken();
				ok = 1;
				readLines();
			}
			else {
				node.fStopOffset = node.fArgs.fStopOffset;
				node.fStatus = RAst.STATUS_MISSING_OPERATOR;
			}
		}
		else {
			node.fArgs.fStartOffset = node.fArgs.fStopOffset = node.fStopOffset;
			node.fStatus = RAst.STATUS_MISSING_OPERATOR;
		}
		
		// content
		if (ok <= 0 && !recoverCCont()) {
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
		
		scanInSpecArgs(node.fArgs);
		
		if (fNextType == RTerminal.GROUP_CLOSE) {
			node.fArgsCloseOffset = fNext.offset;
			node.fStopOffset = node.fArgsCloseOffset+1;
			consumeToken();
		}
		else {
			node.fStopOffset = node.fArgs.fStopOffset;
			node.fStatus = RAst.STATUS_MISSING_OPERATOR;
		}
		
		return node;
	}
	
	final void scanInFDefArgs(final FDef.Args args) {
		args.fStartOffset = args.fStopOffset = args.fRParent.fStopOffset;
		ITER_ARGS : while (true) {
			final FDef.Arg arg = new FDef.Arg(args);
			switch(fNextType) {
			case SYMBOL:
			case SYMBOL_G:
				arg.fArgName = createSymbol(arg);
				arg.fStartOffset = arg.fArgName.fStartOffset;
				arg.fStopOffset = arg.fArgName.fStopOffset;
				readLines();
				break;
			case EQUAL:
			case COMMA:
				arg.fStartOffset = arg.fStopOffset = fNext.offset;
				break;
			default:
				if (args.fSpecs.isEmpty()) {
					return;
				}
				arg.fStartOffset = arg.fStopOffset = args.fStopOffset;
				break;
			}
			
			if (arg.fArgName == null) {
				arg.fArgName = errorNonExistingSymbol(arg, arg.fStopOffset, RAst.STATUS_MISSING_SYMBOL);
			}
			
			if (fNextType == RTerminal.EQUAL) {
				arg.fStopOffset = fNext.offset+1;
				consumeToken();
				
				final Expression expr = arg.addDefault();
				scanInGroup(arg, expr);
				arg.fStopOffset = arg.fDefaultExpr.node.fStopOffset;
			}
			
			args.fSpecs.add(arg);
			if (fNextType == RTerminal.COMMA) {
				args.fStopOffset = fNext.offset+1;
				consumeToken();
				readLines();
				continue ITER_ARGS;
			}
			else {
				args.fStartOffset = args.fSpecs.get(0).fStartOffset;
				args.fStopOffset = arg.fStopOffset;
				return;
			}
		}
	}
	
	final void scanInSpecArgs(final SpecList args) {
		args.fStartOffset = args.fStopOffset = args.fRParent.fStopOffset;
		ITER_ARGS : while (true) {
			final SpecItem arg = args.createItem();
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
				arg.fArgName = errorNonExistingSymbol(arg, fNext.offset, RAst.STATUS_MISSING_SYMBOL);
				break;
			default:
				break;
			}
			if (arg.fArgName != null) {
				if (fNextType == RTerminal.EQUAL) {
					arg.fEqualsOffset = fNext.offset;
					arg.fStopOffset = arg.fEqualsOffset+1;
					consumeToken();
					
					final ExprContext valueContext = new ExprContext(arg, arg.fValueExpr, true);
					scanInExpression(valueContext);
					if (arg.fValueExpr.node != null) { // empty items are allowed
						checkExpression(valueContext);
						arg.fStopOffset = arg.fValueExpr.node.fStopOffset;
					}
				}
				else {
					// argName -> valueExpr
					arg.fValueExpr.node = arg.fArgName;
					arg.fArgName = null;
					
					final ExprContext valueContext = new ExprContext(arg, arg.fValueExpr, true);
					valueContext.update(arg.fValueExpr.node, null);
					scanInExpression(valueContext);
					checkExpression(valueContext);
					arg.fStopOffset = arg.fValueExpr.node.fStopOffset;
				}
			}
			else {
				final ExprContext valueContext = new ExprContext(arg, arg.fValueExpr, true);
				scanInExpression(valueContext);
				if (arg.fValueExpr.node != null) { // empty items are allowed
					checkExpression(valueContext);
					arg.fStopOffset = arg.fValueExpr.node.fStopOffset;
				}
				else {
					arg.fStartOffset = arg.fStopOffset = args.fStopOffset;
				}
			}
			
			if (fNextType == RTerminal.COMMA) {
				args.fSpecs.add(arg);
				args.fStopOffset = fNext.offset+1;
				consumeToken();
				readLines();
				continue ITER_ARGS;
			}
			// last arg before )
			if (args.fSpecs.isEmpty() && !arg.hasChildren()) {
				return;
			}
			args.fSpecs.add(arg);
			args.fStartOffset = args.fSpecs.get(0).fStartOffset;
			args.fStopOffset = arg.fStopOffset;
			return;
		}
	}
	
	final boolean recoverCCont() {
		return !fWasLinebreak
			&& (fNextType == RTerminal.SYMBOL || fNextType == RTerminal.SYMBOL_G || fNextType == RTerminal.BLOCK_OPEN);
	}
	
	final void appendNonOp(final ExprContext context, final RAstNode newNode) {
		if (context.openExpr != null) {
			newNode.fRParent = context.lastNode;
			context.openExpr.node = newNode;
		}
		else {
			// setup missing op
			final Dummy.Operator error = new Dummy.Operator(RAst.STATUS_MISSING_OPERATOR);
			error.fRParent = context.rootNode;
			error.fLeftExpr.node = context.rootExpr.node;
			error.fStartOffset = error.fStopOffset = newNode.fStartOffset;
			context.rootExpr.node = error;
			// append news
			newNode.fRParent = error;
			error.fRightExpr.node = newNode;
			context.rootExpr.node = error;
		}
		context.update(newNode, newNode.getRightExpr());
		return;
	}
	
	final void appendOp(final ExprContext context, final RAstNode newNode) {
		if (context.openExpr != null) {
			context.openExpr.node = errorNonExistExpression(context.lastNode, newNode.fStartOffset, RAst.STATUS_MISSING_OPERATOR);
			context.update(context.openExpr.node, null);
		}
		
		final int newP = newNode.getNodeType().opPrec;
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
		
		final RAstNode baseNode = left.getParent();
		if (baseNode == null) {
			throw new IllegalStateException(); // DEBUG
		}
		if (left.getNodeType().opPrec == newP) {
			if (left.getNodeType().opAssoc == Assoc.LEFTMULTI) {
				final FlatMulti leftMulti = (FlatMulti) left;
				final FlatMulti newMulti = (FlatMulti) newNode;
				context.update(leftMulti, leftMulti.appendComponent(newMulti.getStopOffset(), newMulti.getOperator(1)));
				return;
			}
		}
		final Expression baseExpr = baseNode.getExpr(left);
		newNode.getLeftExpr().node = left;
		left.fRParent = newNode;
		baseExpr.node = newNode;
		newNode.fRParent = baseNode;
		
		context.update(newNode, newNode.getRightExpr());
		return;
	}
	
	Dummy.Terminal errorNonExistExpression(final RAstNode parent, final int stopHint, final IStatus status) {
		final Dummy.Terminal error = new Dummy.Terminal(status);
		error.fRParent = parent;
		error.fStartOffset = error.fStopOffset = (stopHint >= 0) ? stopHint : parent.fStopOffset;
		error.fText = ""; //$NON-NLS-1$
		return error;
	}
	
	Dummy.Terminal errorFromNext(final RAstNode parent) {
		final Dummy.Terminal error = new Dummy.Terminal((fNextType == RTerminal.UNKNOWN) ?
				RAst.STATUS_UNKNOWN_TOKEN : RAst.STATUS_UNEXEPTEC_TOKEN);
		error.fRParent = parent;
		error.fStartOffset = fNext.offset;
		error.fStopOffset = fNext.offset+fNext.length;
		error.fText = fNext.text;
		consumeToken();
		return error;
	}
	
	Symbol errorNonExistingSymbol(final RAstNode parent, final int offset, final IStatus status) {
		final Symbol error = new Symbol();
		error.fRParent = parent;
		error.fStartOffset = error.fStopOffset = offset;
		error.fText = ""; //$NON-NLS-1$
		error.fStatus = status;
		return error;
	}
	
	protected Symbol createSymbol(final RAstNode parent) {
		final Symbol symbol;
		switch (fNextType) {
		case SYMBOL_G:
			symbol = new Symbol.G();
			break;
		case SYMBOL:
			symbol = new Symbol();
			break;
		default:
			throw new IllegalStateException();
		}
		symbol.fRParent = parent;
		setupFromSourceToken(symbol);
		consumeToken();
		return symbol;
	}
	
	protected Symbol createEllipsis(final RAstNode parent) { // TODO replace with own type?
		final Symbol symbol = new Symbol();
		symbol.fRParent = parent;
		setupFromSourceToken(symbol);
		symbol.fText = "..."; //$NON-NLS-1$
		consumeToken();
		return symbol;
	}
	
	protected NumberConst createNumberConst(final RAstNode parent) {
		final NumberConst num = new NumberConst();
		num.fRParent = parent;
		setupFromSourceToken(num);
		consumeToken();
		return num;
	}
	
	protected NullConst createNullConst(final RAstNode parent) {
		final NullConst num = new NullConst();
		num.fRParent = parent;
		setupFromSourceToken(num);
		consumeToken();
		return num;
	}
	
	protected StringConst createStringConst(final RAstNode parent) {
		final StringConst str;
		switch (fNextType) {
		case STRING_D:
			str = new StringConst.D();
			break;
		case STRING_S:
			str = new StringConst.S();
			break;
		default:
			throw new IllegalStateException();
		}
		str.fRParent = parent;
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
			throw new IllegalStateException();
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
			throw new IllegalStateException();
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
			throw new IllegalStateException();
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
			throw new IllegalStateException();
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
		node.fQualifier = fNext.text;
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
			throw new IllegalStateException();
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
			throw new IllegalStateException();
		}
		setupFromSourceToken(node);
		consumeToken();
		return node;
	}
	
	protected Help createHelp() {
		final Help node = new Help();
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
	
	int checkExpression(final ExprContext context) {
		int state = 0;
		if (context.openExpr != null && context.openExpr.node == null) {
			context.openExpr.node = errorNonExistExpression(context.lastNode, context.lastNode.fStopOffset, RAst.STATUS_MISSING_EXPR);
			state = -1;
		}
		try {
			context.rootExpr.node.acceptInR(fPostVisitor);
		}
		catch (final OperationCanceledException e) {
		}
		catch (final InvocationTargetException e) {
		}
		return state;
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
