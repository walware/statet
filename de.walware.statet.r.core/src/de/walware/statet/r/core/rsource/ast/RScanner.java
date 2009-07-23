/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.rsource.ast;

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_CC_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_CONDITION_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_CONDITION_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_ELEMENTNAME_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AS_BODY_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AS_CONDITION_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_BEFORE_OP_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_FCALL_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_FDEF_ARGS_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_FDEF_ARGS_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_IF_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_IN_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_OPERATOR_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_SUBINDEXED_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_SYMBOL_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_TOKEN_UNEXPECTED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_TOKEN_UNKNOWN;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS3_FDEF;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS3_FOR;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS3_IF;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS3_WHILE;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUSFLAG_REAL_ERROR;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUSFLAG_SUBSEQUENT;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_OK;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_RUNTIME_ERROR;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_SYNTAX_SEQREL_UNEXPECTED;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.text.IStringCache;
import de.walware.ecommons.text.SourceParseInput;

import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.ast.RAstNode.Assoc;
import de.walware.statet.r.core.rsource.ast.RScannerLexer.ScannerToken;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * Scanner to create a R AST.
 */
public final class RScanner {
	
	
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
	
	private static final class RoxygenCollector {
		
		private Comment[] fLines = new Comment[64];
		private int fLineCount;
		private DocuComment fCurrent;
		
		void add(final Comment comment) {
			if (fCurrent == null) {
				fCurrent = new DocuComment();
			}
			comment.fRParent = fCurrent;
			
			if (fLineCount == fLines.length) {
				final Comment[] lines = new Comment[fLineCount+64];
				System.arraycopy(fLines, 0, lines, 0, fLineCount);
				fLines = lines;
			}
			fLines[fLineCount++] = comment;
		}
		
		boolean hasComment() {
			return (fCurrent != null);
		}
		
		DocuComment finish(final ScannerToken next) {
			final DocuComment comment = new DocuComment();
			final Comment[] lines = new Comment[fLineCount];
			System.arraycopy(fLines, 0, lines, 0, fLineCount);
			comment.fLines = lines;
			comment.fStartOffset = lines[0].fStartOffset;
			comment.fStopOffset = lines[fLineCount-1].fStopOffset;
			comment.fNextOffset = (next != null && next.type != RTerminal.EOF) ? next.offset : Integer.MIN_VALUE;
			
			fLineCount = 0;
			fCurrent = null;
			return comment;
		}
		
		void clear() {
			for (int i = 0; i < fLines.length; i++) {
				fLines[i] = null;
			}
		}
		
	}
	
	private final static RScannerPostExprVisitor POST_VISITOR = new RScannerPostExprVisitor();
	
	
	private final RScannerLexer fLexer;
	private final RScannerLexer.ScannerToken fNext;
	private final AstInfo fAst;
	
	private RTerminal fNextType;
	private boolean fWasLinebreak;
	
	private IntList fLineOffset = new ArrayIntList();
	private List<RAstNode> fComments;
	private RoxygenCollector fRoxygen;
	private int fCommentsLevel;
	
	
	public RScanner(final SourceParseInput input, final AstInfo ast) {
		this(input, ast, null);
	}
	
	public RScanner(final SourceParseInput input, final AstInfo ast, final IStringCache stringCache) {
		if (ast != null && (ast.level & AstInfo.DEFAULT_LEVEL_MASK) <= RAst.LEVEL_MINIMAL) {
			fLexer = new RScannerLexer(input);
		}
		else {
			fLexer = new RScannerDefaultLexer(input, stringCache);
		}
		fNext = fLexer.getToken();
		fAst = ast;
	}
	
	
	public void setCommentLevel(final int level) {
		fCommentsLevel = level;
		if (level > 0) {
			fComments = new ArrayList<RAstNode>();
			if (level > 0x4) {
				fRoxygen = new RoxygenCollector();
			}
		}
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
			dummy.fStatus = STATUS_RUNTIME_ERROR;
			return dummy;
		}
	}
	
	public SourceComponent scanSourceRange(final IAstNode parent, final int offset, final int length) {
		try {
			fLexer.setRange(offset, length);
			init();
			final SourceComponent rootNode = scanSourceUnit(null);
			rootNode.fParent = parent;
			return rootNode;
		}
		catch (final Exception e) {
			RCorePlugin.logError(-1, "Error occured while parsing R code", e);
			final SourceComponent dummy = new SourceComponent();
			dummy.fStatus = STATUS_RUNTIME_ERROR;
			return dummy;
		}
	}
	
	public FDef scanFDef() {
		try {
			fLexer.setFull();
			init();
			if (fNextType == RTerminal.FUNCTION) {
				return scanFDef(null);
			}
		}
		catch (final Exception e) {
			RCorePlugin.logError(-1, "Error occured while parsing R code", e);
		}
		return null;
	}
	
	public FCall.Args scanFCallArgs(final boolean expand, final int offset, final int length) {
		try {
			fLexer.setRange(offset, length);
			init();
			final FCall call = new FCall();
			call.fStopOffset = Integer.MIN_VALUE;
			scanInSpecArgs(call.fArgs);
			if (expand) {
				call.fArgs.fStartOffset = 0;
				call.fArgs.fStopOffset = fNext.offset;
			}
			return call.fArgs;
		}
		catch (final Exception e) {
			RCorePlugin.logError(-1, "Error occured while parsing R code", e);
		}
		return null;
	}
	
	private void init() {
		fNextType = fNext.type = RTerminal.LINEBREAK;
		fLineOffset.clear();
		fLineOffset.add(fNext.offset);
		consumeToken();
	}
	
	public int[] getLineOffsets() {
		return fLineOffset.toArray();
	}
	
	final SourceComponent scanSourceUnit(final RAstNode parent) {
		final SourceComponent node = new SourceComponent();
		node.fRParent = parent;
		scanInExprList(node, true);
//		if (fNextType == RTerminal.EOF) {
//			fNext.type = null;
//		}
		if (fCommentsLevel > 0) {
			node.fComments = Collections.unmodifiableList(fComments);
		}
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
				
			case SUB_NAMED_PART:
			case SUB_NAMED_SLOT:
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
			return node;
		}
		else {
			node.fStopOffset = fNext.offset;
			node.fStatus = STATUS2_SYNTAX_CC_NOT_CLOSED;
			return node;
		}
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
			return node;
		}
		else {
			node.fStopOffset = fNext.offset;
			node.fStatus = STATUS2_SYNTAX_CC_NOT_CLOSED;
			return node;
		}
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
				final RAstNode base = context.lastNode.fRParent;
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
			node.fNamespace = errorNonExistingSymbol(node, node.fStartOffset, STATUS2_SYNTAX_ELEMENTNAME_MISSING);
			break;
		}
		
		// element
		switch (fNextType) {
		case STRING_S:
		case STRING_D:
			node.fElement = createStringConst(node);
			node.fStopOffset = node.fElement.fStopOffset;
			return node;
		case SYMBOL:
		case SYMBOL_G:
			node.fElement = createSymbol(node);
			node.fStopOffset = node.fElement.fStopOffset;
			return node;
		default:
			node.fElement = errorNonExistingSymbol(node, node.fStopOffset, STATUS2_SYNTAX_ELEMENTNAME_MISSING);
			return node;
		}
	}
	
	final SubNamed scanSubNamed(final ExprContext context) {
		final SubNamed node;
		switch (fNextType) {
		case SUB_NAMED_PART:
			node = new SubNamed.Named();
			break;
		case SUB_NAMED_SLOT:
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
			return node;
		case SYMBOL:
		case SYMBOL_G:
			node.fSubname = createSymbol(node);
			node.fStopOffset = node.fSubname.fStopOffset;
			return node;
		default:
			node.fSubname = errorNonExistingSymbol(node, node.fStopOffset, STATUS2_SYNTAX_ELEMENTNAME_MISSING);
			return node;
		}
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
					return node;
				}
				else {
					node.fStopOffset = node.fCloseOffset+1;
					node.fStatus = STATUS2_SYNTAX_SUBINDEXED_NOT_CLOSED;
					return node;
				}
			}
			else {
				node.fStopOffset = node.fCloseOffset+1;
				return node;
			}
		}
		else {
			node.fStopOffset = node.fSublist.fStopOffset;
			node.fStatus = STATUS2_SYNTAX_SUBINDEXED_NOT_CLOSED;
			return node;
		}
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
				node.fStatus = STATUS2_SYNTAX_CONDITION_NOT_CLOSED;
			}
		}
		else {
			node.fStatus = STATUS2_SYNTAX_CONDITION_MISSING;
			node.fCondExpr.node = errorNonExistExpression(node, node.fStopOffset,
					(STATUS2_SYNTAX_EXPR_AS_CONDITION_MISSING | STATUSFLAG_SUBSEQUENT | STATUS3_IF));
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
			node.fThenExpr.node = errorNonExistExpression(node, node.fCondExpr.node.fStopOffset,
					(STATUS2_SYNTAX_EXPR_AS_BODY_MISSING | STATUSFLAG_SUBSEQUENT | STATUS3_IF));
		}
		
		// else
		if (fNextType == RTerminal.ELSE) {
			node.fWithElse = true;
			node.fElseOffset = fNext.offset;
			consumeToken();
			// else body is added via common expression procession
		}
		
		return node;
	}
	
	final CIfElse scanCElse(final ExprContext context) { // else without if
		final CIfElse node = new CIfElse();
		setupFromSourceToken(node);
		node.fStatus = STATUS2_SYNTAX_IF_MISSING;
		node.fCondExpr.node = errorNonExistExpression(node, node.fStartOffset,
				(STATUS2_SYNTAX_EXPR_AS_CONDITION_MISSING | STATUSFLAG_SUBSEQUENT | STATUS3_IF));
		node.fThenExpr.node = errorNonExistExpression(node, node.fStartOffset,
				(STATUS2_SYNTAX_EXPR_AS_BODY_MISSING | STATUSFLAG_SUBSEQUENT | STATUS3_IF));
		node.fElseOffset = fNext.offset;
		node.fWithElse = true;
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
				node.fVarSymbol = errorNonExistingSymbol(node, node.fCondOpenOffset+1, STATUS2_SYNTAX_SYMBOL_MISSING);
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
				node.fStatus = (ok >= 0) ? STATUS2_SYNTAX_IN_MISSING :
						(STATUS2_SYNTAX_IN_MISSING | STATUSFLAG_SUBSEQUENT);
				node.fCondExpr.node = errorNonExistExpression(node, node.fStopOffset,
						(STATUS2_SYNTAX_EXPR_AS_CONDITION_MISSING | STATUSFLAG_SUBSEQUENT));
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
				if (node.fStatus == STATUS_OK) {
					node.fStatus = (ok >= 0) ? STATUS2_SYNTAX_CONDITION_NOT_CLOSED :
							(STATUS2_SYNTAX_CONDITION_NOT_CLOSED | STATUSFLAG_SUBSEQUENT);
				}
			}
		}
		else { // missing GROUP_OPEN
			node.fStatus = STATUS2_SYNTAX_CONDITION_MISSING;
			node.fVarSymbol = errorNonExistingSymbol(node, node.fStopOffset,
					STATUS2_SYNTAX_SYMBOL_MISSING | STATUSFLAG_SUBSEQUENT);
			node.fCondExpr.node = errorNonExistExpression(node, node.fStopOffset,
					(STATUS2_SYNTAX_EXPR_AS_CONDITION_MISSING | STATUSFLAG_SUBSEQUENT | STATUS3_FOR));
		}
		
		// loop
		if (ok <= 0 && !recoverCCont()) {
			node.fLoopExpr.node = errorNonExistExpression(node, node.fStopOffset,
					(STATUS2_SYNTAX_EXPR_AS_BODY_MISSING | STATUSFLAG_SUBSEQUENT | STATUS3_FOR));
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
				node.fStatus = (ok >= 0) ? STATUS2_SYNTAX_CONDITION_NOT_CLOSED :
						(STATUS2_SYNTAX_CONDITION_NOT_CLOSED | STATUSFLAG_SUBSEQUENT);
			}
		}
		else {
			node.fStatus = STATUS2_SYNTAX_CONDITION_MISSING;
			node.fCondExpr.node = errorNonExistExpression(node, node.fStopOffset,
					(STATUS2_SYNTAX_EXPR_AS_CONDITION_MISSING | STATUSFLAG_SUBSEQUENT | STATUS3_WHILE));
		}
		
		// loop
		if (ok <= 0 && !recoverCCont()) {
			node.fLoopExpr.node = errorNonExistExpression(node, node.fStopOffset,
					(STATUS2_SYNTAX_EXPR_AS_BODY_MISSING | STATUSFLAG_SUBSEQUENT | STATUS3_WHILE));
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
				node.fStatus = STATUS2_SYNTAX_FDEF_ARGS_NOT_CLOSED;
			}
		}
		else {
			node.fArgs.fStartOffset = node.fArgs.fStopOffset = node.fStopOffset;
			node.fStatus = STATUS2_SYNTAX_FDEF_ARGS_MISSING;
		}
		
		// body
		if (ok <= 0 && !recoverCCont()) {
			node.fExpr.node = errorNonExistExpression(node, node.fStopOffset,
					(STATUS2_SYNTAX_EXPR_AS_BODY_MISSING | STATUSFLAG_SUBSEQUENT | STATUS3_FDEF));
		}
		
		return node;
	}
	
	final FCall scanFCall() {
		final FCall node = new FCall();
		
		setupFromSourceToken(node);
		node.fArgsOpenOffset = fNext.offset;
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
			node.fStatus = STATUS2_SYNTAX_FCALL_NOT_CLOSED;
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
				arg.fArgName = errorNonExistingSymbol(arg, arg.fStopOffset, STATUS2_SYNTAX_SYMBOL_MISSING);
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
	
	final void scanInSpecArgs(final FCall.Args args) {
		args.fStartOffset = args.fStopOffset = args.fRParent.fStopOffset;
		ITER_ARGS : while (true) {
			final FCall.Arg arg = new FCall.Arg(args);
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
				arg.fArgName = errorNonExistingSymbol(arg, fNext.offset, STATUS2_SYNTAX_ELEMENTNAME_MISSING);
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
				args.fSepList.add(fNext.offset);
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
	
	final void scanInSpecArgs(final SubIndexed.Args args) {
		args.fStartOffset = args.fStopOffset = args.fRParent.fStopOffset;
		ITER_ARGS : while (true) {
			final SubIndexed.Arg arg = new SubIndexed.Arg(args);
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
				arg.fArgName = errorNonExistingSymbol(arg, fNext.offset, STATUS2_SYNTAX_ELEMENTNAME_MISSING);
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
			final Dummy.Operator error = new Dummy.Operator(STATUS2_SYNTAX_OPERATOR_MISSING);
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
			context.openExpr.node = errorNonExistExpression(context.lastNode, newNode.fStartOffset, STATUS2_SYNTAX_EXPR_BEFORE_OP_MISSING);
			context.update(context.openExpr.node, null);
		}
		
		final int newP = newNode.getNodeType().opPrec;
		RAstNode left = context.lastNode;
		RAstNode cand = context.lastNode;
		
		ITER_CAND : while (cand != null && cand != context.rootNode) {
			final NodeType candType = cand.getNodeType();
			if (candType.opPrec == newP) {
				switch (candType.opAssoc) {
				case Assoc.NOSTD:
					left = cand;
					if ((newNode.fStatus & STATUSFLAG_REAL_ERROR) == 0) {
						newNode.fStatus = STATUS_SYNTAX_SEQREL_UNEXPECTED;
					}
					break ITER_CAND;
				case Assoc.LEFTSTD:
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
			cand = cand.fRParent;
		}
		
		final RAstNode baseNode = left.fRParent;
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
	
	Dummy.Terminal errorNonExistExpression(final RAstNode parent, final int stopHint, final int status) {
		final Dummy.Terminal error = new Dummy.Terminal(status);
		error.fRParent = parent;
		error.fStartOffset = error.fStopOffset = (stopHint != Integer.MIN_VALUE) ? stopHint : parent.fStopOffset;
		error.fText = ""; //$NON-NLS-1$
		return error;
	}
	
	Dummy.Terminal errorFromNext(final RAstNode parent) {
		final Dummy.Terminal error = new Dummy.Terminal((fNextType == RTerminal.UNKNOWN) ?
				STATUS2_SYNTAX_TOKEN_UNKNOWN : STATUS2_SYNTAX_TOKEN_UNEXPECTED);
		error.fRParent = parent;
		error.fStartOffset = fNext.offset;
		error.fStopOffset = fNext.offset+fNext.length;
		error.fText = fNext.text;
		consumeToken();
		return error;
	}
	
	Symbol errorNonExistingSymbol(final RAstNode parent, final int offset, final int status) {
		final Symbol error = new Symbol.Std();
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
			symbol = new Symbol.Std();
			break;
		default:
			throw new IllegalStateException();
		}
		symbol.fRParent = parent;
		setupFromSourceToken(symbol);
		consumeToken();
		return symbol;
	}
	
	protected NumberConst createNumberConst(final RAstNode parent) {
		final NumberConst num = new NumberConst(fNextType);
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
	
	private final void setupFromSourceToken(final RAstNode node) {
		node.fStartOffset = fNext.offset;
		node.fStopOffset = fNext.offset+fNext.length;
		node.fStatus = fNext.status;
	}
	
	private final void setupFromSourceToken(final SingleValue node) {
		node.fStartOffset = fNext.offset;
		node.fStopOffset = fNext.offset+fNext.length;
		node.fText = fNext.text;
		node.fStatus = fNext.status;
	}
	
	private final int checkExpression(final ExprContext context) {
		int state = 0;
		if (context.openExpr != null && context.openExpr.node == null) {
			context.openExpr.node = errorNonExistExpression(context.lastNode, context.lastNode.fStopOffset,
					context.lastNode.getMissingExprStatus(context.openExpr));
			state = -1;
		}
		try {
			context.rootExpr.node.acceptInR(POST_VISITOR);
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
		fLexer.nextToken();
		fNextType = fNext.type;
		switch (fNextType) {
		case COMMENT:
		case ROXYGEN_COMMENT:
			if (fCommentsLevel > 0x4) {
				consumeCommentWithRoxygen();
			}
			else {
				consumeComment();
			}
			return;
		case LINEBREAK:
			fLineOffset.add(fNext.offset+fNext.length);
		default:
			return;
		}
	}
	
	
	private void consumeCommentWithRoxygen() {
		while (true) {
			final Comment comment;
			switch (fNextType) {
			case COMMENT:
				if (fRoxygen.hasComment()) {
					fComments.add(fRoxygen.finish(fNext));
				}
				comment = new Comment.CommonLine();
				setupFromSourceToken(comment);
				fComments.add(comment);
				
				fLexer.nextToken();
				fNextType = fNext.type;
				continue;
				
			case ROXYGEN_COMMENT:
				comment = new Comment.RoxygenLine();
				setupFromSourceToken(comment);
				fRoxygen.add(comment);
				
				fLexer.nextToken();
				fNextType = fNext.type;
				continue;
				
			case LINEBREAK:
				fLineOffset.add(fNext.offset+fNext.length);
				fLexer.nextToken();
				fNextType = fNext.type;
				if (fNextType == RTerminal.LINEBREAK && fRoxygen.hasComment()) {
					fComments.add(fRoxygen.finish(null));
				}
				continue;
				
			default:
				if (fRoxygen.hasComment()) {
					fComments.add(fRoxygen.finish(fNext));
				}
				
				fWasLinebreak = true;
				return;
			}
		}
	}
	
	private void consumeComment() {
		while (true) {
			switch (fNextType) {
			case COMMENT:
			case ROXYGEN_COMMENT:
				if (fCommentsLevel > 0) {
					final Comment comment = (fNextType == RTerminal.ROXYGEN_COMMENT) ?
							new Comment.RoxygenLine() :
							new Comment.CommonLine();
					setupFromSourceToken(comment);
					fComments.add(comment);
				} // no break
				
				fLexer.nextToken();
				fNextType = fNext.type;
				continue;
				
			case LINEBREAK:
				fLineOffset.add(fNext.offset+fNext.length);
				fLexer.nextToken();
				fNextType = fNext.type;
				continue;
				
			default:
				fWasLinebreak = true;
				return;
			}
		}
	}
	
}
