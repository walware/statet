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

package de.walware.statet.r.core.rsource;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.ICommonAstVisitor;
import de.walware.ecommons.text.IndentUtil.IndentEditAction;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rsource.ast.Block;
import de.walware.statet.r.core.rsource.ast.CForLoop;
import de.walware.statet.r.core.rsource.ast.CIfElse;
import de.walware.statet.r.core.rsource.ast.CRepeatLoop;
import de.walware.statet.r.core.rsource.ast.CWhileLoop;
import de.walware.statet.r.core.rsource.ast.FCall;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.GenericVisitor;
import de.walware.statet.r.core.rsource.ast.Group;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAstInfo;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.SourceComponent;
import de.walware.statet.r.core.rsource.ast.SubIndexed;
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * Indents R source code in a document
 */
public class RSourceIndenter {
	
	
	private RIndentUtil fUtil;
	private RHeuristicTokenScanner fScanner;
	private ComputeIndentVisitor fComputeVisitor;
	
	private AbstractDocument fDocument;
	private AstInfo<?> fAst;
	private RCodeStyleSettings fCodeStyle;
	
	private int fRefLine;
	private int fFirstLine;
	private int fLastLine;
	
	private int[] fLineOffsets;
	private int[] fLineLevels;
	
	private ScopeFactory fFactory;
	
	
	private class ComputeIndentVisitor extends GenericVisitor implements ICommonAstVisitor {
		
		
		private int fStartOffset;
		private int fStopOffset;
		private int fCurrentLine;
		
		
		void computeIndent() throws InvocationTargetException {
			try {
				fCurrentLine = (fRefLine >= 0) ? fRefLine : fFirstLine;
				fStartOffset = fDocument.getLineOffset(fCurrentLine);
				fStopOffset = fDocument.getLineOffset(fLastLine)+fDocument.getLineLength(fLastLine);
				fAst.root.accept(this);
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		
		private final boolean checkOffset(final int offset) {
			if (offset >= fLineOffsets[fCurrentLine]) { // offset is first char in line
				do {
					fLineLevels[fCurrentLine] = fFactory.getIndent(fCurrentLine);
				} while (offset >= fLineOffsets[++fCurrentLine]);
				return true;
			}
			return false;
		}
		
		private void checkBeforeOffset(final int offset) {
			if (offset >= fLineOffsets[fCurrentLine+1]) { // offset is first char in line
				final int level = fFactory.getIndent(fCurrentLine);
				do {
					fLineLevels[fCurrentLine++] = level;
				} while (offset >= fLineOffsets[fCurrentLine+1]);
			}
		}
		
		private boolean checkNode(final RAstNode node) throws InvocationTargetException {
			final int offset = node.getOffset();
			if (checkOffset(offset)) {
				return (node.getStopOffset() >= fLineOffsets[fCurrentLine]);
			}
			// touches format region
			if (node.getStopOffset() >= fStartOffset && offset <= fStopOffset) {
				return true;
			}
			// not interesting
			return false;
		}
		
		
		private final void checkExprListChilds(final RAstNode node) throws InvocationTargetException {
			try {
				final int count = node.getChildCount();
				for (int i = 0; i < count; i++) {
					final RAstNode child = node.getChild(i);
					fFactory.createCommonExprScope(child.getOffset(), child);
					child.acceptInR(this);
					fFactory.leaveScope();
				}
			} catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		public void visit(final IAstNode node) throws InvocationTargetException {
			if (node.getStopOffset() >= fStartOffset && node.getOffset() <= fStopOffset) {
				if (node instanceof RAstNode) {
					((RAstNode) node).acceptInR(this);
				}
				else {
					node.acceptInChildren(this);
				}
			}
		}
		
		@Override
		public void visit(final SourceComponent node) throws InvocationTargetException {
			try {
				fFactory.createSourceScope(0, node);
				if (node.getStopOffset() >= fStartOffset && node.getOffset() <= fStopOffset) {
					checkExprListChilds(node);
				}
				checkOffset(Integer.MAX_VALUE-2);
				fFactory.leaveScope();
			} catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		@Override
		public void visit(final Block node) throws InvocationTargetException {
			try {
				fFactory.createBlockScope(node.getOffset(), node);
				if (checkNode(node)) {
					fFactory.updateEnterBrackets();
					checkExprListChilds(node);
					checkBeforeOffset(node.getStopOffset());
					fFactory.updateLeaveBrackets();
					checkOffset(node.getStopOffset());
				}
				fFactory.leaveScope();
			} catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		@Override
		public void visit(final Group node) throws InvocationTargetException {
			try {
				if (checkNode(node)) {
					fFactory.createGroupContScope(node.getOffset()+1, node.getExprChild());
					node.getExprChild().acceptInR(this);
					checkBeforeOffset(node.getStopOffset());
					
					checkOffset(node.getStopOffset());
					fFactory.leaveScope();
				}
			} catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		private final void checkControlCondChild(final int open, final RAstNode child, final int close) throws InvocationTargetException {
			try {
				if (open >= 0) {
					checkOffset(open);
					fFactory.createControlCondScope(open+1, child);
					child.acceptInR(this);
					checkBeforeOffset(close);
					
					checkOffset(close);
					fFactory.leaveScope();
				}
			} catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		private final void checkControlContChild(final RAstNode child) throws InvocationTargetException {
			try {
				fFactory.createControlContScope(child.getOffset(), child);
				child.acceptInR(this);
				fFactory.leaveScope();
			} catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		@Override
		public void visit(final CIfElse node) throws InvocationTargetException {
			try {
				boolean inElseIf = false;
				if (node.getRParent().getNodeType() == NodeType.C_IF
						&& ((CIfElse) node.getParent()).getElseChild() == node) {
					fFactory.leaveScope();
					inElseIf = true;
				}
				else {
					fFactory.createControlScope(node.getOffset(), node);
				}
				if (checkNode(node)) {
					checkControlCondChild(node.getCondOpenOffset(), node.getCondChild(), node.getCondCloseOffset());
					checkControlContChild(node.getThenChild());
					if (node.hasElse()) {
						checkOffset(node.getElseOffset());
						checkControlContChild(node.getElseChild());
					}
					checkOffset(node.getStopOffset());
				}
				if (inElseIf) {
					fFactory.createDummy();
				}
				else {
					fFactory.leaveScope();
				}
			} catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		@Override
		public void visit(final CForLoop node) throws InvocationTargetException {
			try {
				fFactory.createControlScope(node.getOffset(), node);
				if (checkNode(node)) {
					checkControlCondChild(node.getCondOpenOffset(), node.getCondChild(), node.getCondCloseOffset());
					checkControlContChild(node.getContChild());
					checkOffset(node.getStopOffset());
				}
				fFactory.leaveScope();
			} catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		@Override
		public void visit(final CWhileLoop node) throws InvocationTargetException {
			try {
				fFactory.createControlScope(node.getOffset(), node);
				if (checkNode(node)) {
					checkControlCondChild(node.getCondOpenOffset(), node.getCondChild(), node.getCondCloseOffset());
					checkControlContChild(node.getContChild());
					checkOffset(node.getStopOffset());
				}
				fFactory.leaveScope();
			} catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		@Override
		public void visit(final CRepeatLoop node) throws InvocationTargetException {
			try {
				fFactory.createControlScope(node.getOffset(), node);
				if (checkNode(node)) {
					checkControlContChild(node.getContChild());
					checkOffset(node.getStopOffset());
				}
				fFactory.leaveScope();
			} catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		private final void checkArglist(final RAstNode node) throws InvocationTargetException {
			try {
				fFactory.createArglistScope(node.getOffset(), node);
				if (checkNode(node)) {
					node.acceptInRChildren(this);
		//			checkBeforeOffset(node.getStopOffset());
					checkOffset(node.getStopOffset());
				}
				fFactory.leaveScope();
			} catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		private final void checkFDeflist(final RAstNode node) throws InvocationTargetException {
			try {
				fFactory.createFDeflistScope(node.getOffset(), node);
				if (checkNode(node)) {
					node.acceptInRChildren(this);
		//			checkBeforeOffset(node.getStopOffset());
					checkOffset(node.getStopOffset());
				}
				fFactory.leaveScope();
			} catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		private final void checkArg(final RAstNode node) throws InvocationTargetException {
			try {
				fFactory.createCommonExprScope(node.getOffset(), node);
				if (checkNode(node)) {
					node.acceptInRChildren(this);
					checkOffset(node.getStopOffset());
				}
				fFactory.leaveScope();
			} catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		@Override
		public void visit(final FDef node) throws InvocationTargetException {
			try {
				fFactory.createFDefScope(node.getOffset(), node);
				if (checkNode(node)) {
					node.getArgsChild().acceptInR(this);
					fFactory.updateEnterFDefBody();
					checkControlContChild(node.getContChild());
					checkOffset(node.getStopOffset());
				}
				fFactory.leaveScope();
			} catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		@Override
		public void visit(final FDef.Args node) throws InvocationTargetException {
			checkFDeflist(node);
		}
		
		@Override
		public void visit(final FDef.Arg node) throws InvocationTargetException {
			checkArg(node);
		}
		
		@Override
		public void visit(final FCall node) throws InvocationTargetException {
			try {
				fFactory.createFCallScope(node.getOffset(), node);
				if (checkNode(node)) {
					node.getRefChild().acceptInR(this);
					node.getArgsChild().acceptInR(this);
					checkOffset(node.getStopOffset());
				}
				fFactory.leaveScope();
			} catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		@Override
		public void visit(final FCall.Args node) throws InvocationTargetException {
			checkArglist(node);
		}
		
		@Override
		public void visit(final FCall.Arg node) throws InvocationTargetException {
			checkArg(node);
		}
		
		@Override
		public void visit(final SubIndexed node) throws InvocationTargetException {
			try {
				fFactory.createFCallScope(node.getOffset(), node);
				if (checkNode(node)) {
					node.getRefChild().acceptInR(this);
					node.getArgsChild().acceptInR(this);
					checkOffset(node.getStopOffset());
				}
				fFactory.leaveScope();
			} catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		
		@Override
		public void visit(final SubIndexed.Args node) throws InvocationTargetException {
			checkArglist(node);
		}
		
		@Override
		public void visit(final SubIndexed.Arg node) throws InvocationTargetException {
			checkArg(node);
		}
		
		@Override
		public void visitNode(final RAstNode node) throws InvocationTargetException {
			if (checkNode(node)) {
				node.acceptInRChildren(this);
				checkOffset(node.getStopOffset());
			}
		}
		
	}
	
	
	/**
	 * 
	 */
	public RSourceIndenter() {
		fScanner = new RHeuristicTokenScanner();
		fComputeVisitor = new ComputeIndentVisitor();
	}
	
	public RSourceIndenter(final IRCoreAccess access) {
		this();
		setup(access);
	}
	
	
	public int getNewIndentColumn(final int line) throws BadLocationException {
		final int lineOffset = fDocument.getLineOffset(line);
		if (getDocumentChar(lineOffset) == '#' && getDocumentChar(lineOffset+1) != '#') {
			return 0;
		}
		return fLineLevels[line];
	}
	
	public int getNewIndentOffset(final int line) {
		try {
			return fUtil.getIndentedOffsetAt(line, fLineLevels[line]);
		} catch (final BadLocationException e) {
			return -1;
		}
	}
	
//	public void indent(final AbstractDocument document, final AstInfo<RAstNode> ast, final int firstLine, final int lastLine,
//			final IRCoreAccess access, final WorkingContext context) throws CoreException {
//		try {
//			setup(document, ast, access);
//			computeIndent(firstLine, lastLine);
//			final MultiTextEdit edits = createEdits();
//			if (edits != null && edits.getChildrenSize() > 0) {
//				context.syncExec(new SourceDocumentRunnable(fDocument, fAst.stamp,
//						(edits.getChildrenSize() > 50) ? DocumentRewriteSessionType.SEQUENTIAL : DocumentRewriteSessionType.SEQUENTIAL) {
//					@Override
//					public void run(final AbstractDocument document) throws InvocationTargetException {
//						try {
//							edits.apply(document, TextEdit.NONE);
//						}
//						catch (final MalformedTreeException e) {
//							throw new InvocationTargetException(e);
//						}
//						catch (final BadLocationException e) {
//							throw new InvocationTargetException(e);
//						}
//					}
//				});
//			}
//		}
//		catch (final InvocationTargetException e) {
//			throw createFailedException(e);
//		}
//		catch (final BadLocationException e) {
//			throw createFailedException(e);
//		}
//	}
	
//	public void indentLine(final AbstractDocument document, final AstInfo<RAstNode> ast, final int line,
//			final IRCoreAccess access) throws CoreException {
//		try {
//			setup(document, ast, access);
//			computeIndent(line, 0);
//			final MultiTextEdit edits = createEdits();
//			if (edits != null && edits.getChildrenSize() > 0) {
//				edits.apply(document);
//			}
//		}
//		catch (BadLocationException e) {
//			throw createFailedException(e);
//		}
//	}
	
	public void setup(final IRCoreAccess access) {
		fCodeStyle = access.getRCodeStyle();
	}
	
	/**
	 * Release resources from last computation.
	 * After clear, you can not longer call the <code>get...(...)</code> methods.
	 */
	public void clear() {
		fDocument = null;
		fAst = null;
		fCodeStyle = null;
		fUtil = null;
		fLineLevels = null;
	}
	
	public TextEdit getIndentEdits(final AbstractDocument document, final RAstInfo ast, final int codeOffset, final int firstLine, final int lastLine) throws CoreException {
		try {
			fDocument = document;
			fAst = ast;
			computeIndent(codeOffset, firstLine, lastLine);
			return createEdits();
		}
		catch (final BadLocationException e) {
			throw createFailedException(e);
		}
	}
	
	protected void computeIndent(final int codeOffset, final int firstLine, final int lastLine) throws BadLocationException {
		try {
			fCodeStyle.getReadLock().lock();
			fUtil = new RIndentUtil(fDocument, fCodeStyle);
			fFirstLine = firstLine;
			fLastLine = lastLine;
			
			fScanner.configure(fDocument);
			
			fRefLine = -1;
			int cand = fFirstLine;
			SEARCH_REF_LINE : while (cand > 0) {
				int refOffset = fScanner.findAnyNonBlankBackward(fDocument.getLineOffset(cand), RHeuristicTokenScanner.UNBOUND, true);
				if (refOffset >= codeOffset) { // line found
					cand = fDocument.getLineOfOffset(refOffset);
					refOffset = fScanner.findAnyNonBlankForward(fDocument.getLineOffset(cand), refOffset+1, true);
					if (fDocument.getChar(refOffset) != '#' || fDocument.getChar(refOffset+1) == '#') {
						fRefLine = cand;
						break SEARCH_REF_LINE;
					}
				}
				break SEARCH_REF_LINE;
			}
			
			final int startLine = (fRefLine >= 0) ? fRefLine : fFirstLine;
			final int count = fDocument.getNumberOfLines(0, fDocument.getLineOffset(fLastLine));
			fLineLevels = new int[count+2];
			Arrays.fill(fLineLevels, -1);
			fLineOffsets = new int[count+3];
			for (int i = startLine; i < count; i++) {
				fLineOffsets[i] = fDocument.getLineOffset(i);
			}
			fLineOffsets[count] = (count < fDocument.getNumberOfLines()) ? fDocument.getLineOffset(count) : fDocument.getLength();
			fLineOffsets[count] = Integer.MAX_VALUE;
			fLineOffsets[count+1] = Integer.MAX_VALUE;
			fLineOffsets[count+2] = Integer.MAX_VALUE;
			
			fFactory = new ScopeFactory(fUtil, fCodeStyle, fDocument);
			fComputeVisitor.computeIndent();
			correctLevels();
		} catch (final InvocationTargetException e) {
			final Throwable targetException = e.getTargetException();
			if (targetException instanceof BadLocationException) {
				throw (BadLocationException) targetException;
			}
			else {
				RCorePlugin.logError(-1, "Unexpected error while indent sources", e); //$NON-NLS-1$
			}
		}
		finally {
			if (fCodeStyle != null) {
				fCodeStyle.getReadLock().unlock();
				fCodeStyle = null;
			}
			fFactory = null;
			fLineOffsets = null;
		}
	}
	
	protected void correctLevels() throws BadLocationException {
		int shift = 0;
		if (fRefLine >= 0) {
			fLineLevels[fRefLine] = fLineLevels[fRefLine];
			shift = fUtil.getLineIndent(fRefLine, false)[RIndentUtil.COLUMN_IDX]-fLineLevels[fRefLine];
			fLineLevels[fRefLine] += shift;
		}
		else {
			shift = 0;
		}
		
//		System.out.println("SHIFT="+shift);
//		if (fRefLine > 0) {
//			System.out.println("REF="+" "+(fRefLine+1)+" ("+fLineOffsets[fRefLine]+" ): "+fLineLevels[fRefLine]);
//		}
//		else {
//			System.out.println("NOREF");
//		}
//		for (int i = fFirstLine; i <= fLastLine; i++) {
//			System.out.println(" "+(i+1)+" ("+fLineOffsets[i]+" ): "+fLineLevels[i]);
//		}
//		System.out.println();
		
		fLineLevels[fFirstLine] += + shift;
		if (fLineLevels[fFirstLine] < 0) {
			shift -= fLineLevels[fFirstLine];
			fLineLevels[fFirstLine] = 0;
		}
		for (int line = fFirstLine+1; line <= fLastLine; line++) {
			fLineLevels[line] += shift;
			if (fLineLevels[line] < 0) {
				fLineLevels[line] = 0;
			}
		}
	}
	
	protected MultiTextEdit createEdits() throws BadLocationException, CoreException {
		final MultiTextEdit edits = new MultiTextEdit();
		final IndentEditAction action = new IndentEditAction() {
			@Override
			public int getIndentColumn(final int line, final int lineOffset) throws BadLocationException {
				if (getDocumentChar(lineOffset) == '#' && getDocumentChar(lineOffset+1) != '#') {
					return -1;
				}
				return fLineLevels[line];
			}
			@Override
			public void doEdit(final int line, final int offset, final int length, final StringBuilder text)
					throws BadLocationException {
				if (text != null) {
					edits.addChild(new ReplaceEdit(offset, length, text.toString()));
				}
			}
		};
		fUtil.changeIndent(fFirstLine, fLastLine, action);
		return edits;
	}
	
	protected final int getDocumentChar(final int idx) throws BadLocationException {
		if (idx >= 0 && idx < fDocument.getLength()) {
			return fDocument.getChar(idx);
		}
		return -1;
	}
	
	protected CoreException createFailedException(final Throwable e) {
		return new CoreException(new Status(Status.ERROR, RCore.PLUGIN_ID, -1, "Indentation failed", e));
	}
}


class ScopeFactory {
	
	
	private static interface IndentStrategy {
		
		int getIndent(Scope scope, int line);
	
	}
	
	public final static class Scope {
		
		int baseColumn;
		int startLine;
		RAstNode commandNode;
		Scope parent;
		IndentStrategy strategy;
		
		int getIndent(final int line) {
			return strategy.getIndent(this, line);
		}
	
	}
	
	
	private static final int POOL_SIZE = 50;
	private final int fLevelMult;
	private final int fWrappedCol;
	private final int fBlockCol;
	private Scope fScope;
	private final Scope[] fPool = new Scope[POOL_SIZE];
	private int fPoolPointer = 0;
	
	private RIndentUtil fUtil;
	private RCodeStyleSettings fStyle;
	private AbstractDocument fDoc;
	
	
	public ScopeFactory(final RIndentUtil util, final RCodeStyleSettings style, final AbstractDocument doc) {
		fUtil = util;
		fStyle = style;
		fDoc = doc;
		fLevelMult = fUtil.getLevelColumns();
		fWrappedCol = fStyle.getIndentWrappedCommandDepth()*fLevelMult;
		fBlockCol = fStyle.getIndentBlockDepth()*fLevelMult;
	}
	
	private class FirstLineStrategy implements IndentStrategy {
		public int getIndent(final Scope scope, final int line) {
			if (line <= scope.startLine) {
				return scope.baseColumn;
			}
			else {
				return scope.baseColumn+fWrappedCol;
			}
		}
	}
	private class FixStrategy implements IndentStrategy {
		public int getIndent(final Scope scope, final int line) {
			return scope.baseColumn;
		}
	}
	
	
	private final IndentStrategy FIX_STRAT = new FixStrategy();
	private final IndentStrategy FIRSTLINE_STRAT = new FirstLineStrategy();
	
	
	private final void initNew(final int offset, final int line, final RAstNode node, final IndentStrategy strat, final int baseColumn) {
		Scope scope;
		if (fPoolPointer < POOL_SIZE) {
			if (fPool[fPoolPointer] == null) {
				fPool[fPoolPointer] = new Scope();
			}
			scope = fPool[fPoolPointer];
		}
		else {
			scope = new Scope();
		}
		fPoolPointer++;
		scope.parent = fScope;
		scope.baseColumn = baseColumn;
		scope.strategy = strat;
		scope.startLine = line;
		scope.commandNode = node;
		
		fScope = scope;
	}
	
	
//	private final void updateCommandLine(final int offset, final RAstNode node) {
//		if (fScope.commandDepth <= 1) {
//			while (offset >= fLineOffsets[fCommandStartLine+1]) {
//				fCommandStartLine++;
//			}
//			fScope.commandNode = node;
//			fScope.commandStartLine = fCommandStartLine;
//		}
//	}
	
	public final Scope createDummy() {
		initNew(0, 0, null, null, 0);
		return fScope;
	}
	
	public final void createSourceScope(final int offset, final RAstNode node) throws BadLocationException {
		final int line = fDoc.getLineOfOffset(offset);
		initNew(offset, line, node, FIX_STRAT, 0);
	}
	
	public final void createBlockScope(final int offset, final RAstNode node) throws BadLocationException {
		final int line = fDoc.getLineOfOffset(offset);
		switch (node.getRParent().getNodeType()) {
		case C_IF:
		case C_FOR:
		case C_WHILE:
		case F_DEF:
			if (node.getRParent().getChild(0) == node) {
				// first are conditions
				break;
			}
		case C_REPEAT:
			// use control level instead of cont level
			initNew(node.getOffset(), line, node, FIX_STRAT, fScope.parent.baseColumn);
			return;
		default:
			break;
		}
		
		initNew(node.getOffset(), line, node, FIX_STRAT, fScope.getIndent(line));
	}
	
	public final void createCommonExprScope(final int offset, final RAstNode node) throws BadLocationException {
		final int line = fDoc.getLineOfOffset(offset);
		initNew(offset, line, node, FIRSTLINE_STRAT, fScope.getIndent(line));
	}
	
	public final void createGroupContScope(final int offset, final RAstNode node) throws BadLocationException {
		final int line = fDoc.getLineOfOffset(offset);
		initNew(offset, line, node, FIX_STRAT, fScope.getIndent(line+1)+fStyle.getIndentGroupDepth()*fLevelMult);
	}
	
	public final void createControlScope(final int offset, final RAstNode node) throws BadLocationException {
		final int line = fDoc.getLineOfOffset(offset);
		initNew(offset, line, node, FIX_STRAT, fScope.getIndent(line));
		boolean compact = true;
		if (compact && node.getNodeType() == NodeType.C_IF
				&& ((CIfElse) node).hasElse()) {
			compact = false;
		}
		if (!useParent(compact, false, node)) {
			fScope.baseColumn = fScope.parent.getIndent(line+1);
		}
	}
	
	public final void createControlCondScope(final int offset, final RAstNode node) throws BadLocationException {
		final int line = fDoc.getLineOfOffset(offset);
		initNew(offset, line, node, FIRSTLINE_STRAT, fScope.getIndent(line));
	}
	
	public final void createControlContScope(final int offset, final RAstNode node) throws BadLocationException {
		final int line = fDoc.getLineOfOffset(offset);
		initNew(offset, line, node, FIRSTLINE_STRAT, fScope.getIndent(line)+fBlockCol);
	}
	
	public final void createFCallScope(final int offset, final RAstNode node) throws BadLocationException {
		final int line = fDoc.getLineOfOffset(offset);
		initNew(offset, line, node, FIX_STRAT, fScope.getIndent(line));
		if (!useParent(true, true, node)) {
			fScope.baseColumn = fScope.parent.getIndent(line+1);
		}
	}
	
	public final void createFDefScope(final int offset, final RAstNode node) throws BadLocationException {
		final int line = fDoc.getLineOfOffset(offset);
		initNew(offset, line, node, FIX_STRAT, fScope.getIndent(line));
	}
	
	public final void createFDeflistScope(final int offset, final RAstNode node) throws BadLocationException {
		final int line = fDoc.getLineOfOffset(offset);
		initNew(offset, line, node, FIX_STRAT, fScope.getIndent(line)+fWrappedCol);
	}
	
	public final void createArglistScope(final int offset, final RAstNode node) throws BadLocationException {
		final int line = fDoc.getLineOfOffset(offset);
		// TODO: this can cause deep indentation - use compact detection of parent?
		initNew(offset, line, node, FIX_STRAT, fScope.getIndent(line)+fWrappedCol);
	}
	
	public final void leaveScope() {
		fScope = fScope.parent;
		fPoolPointer--;
	}
	
	private final boolean useParent(final boolean compact, final boolean onlyAssignments, RAstNode node) throws BadLocationException {
		if (fScope.parent.commandNode == node) {
			return true;
		}
		if (compact
				&& fScope.startLine == fScope.parent.startLine
				&& sameLine(fScope.commandNode.getStopOffset(), fScope.parent.commandNode.getStopOffset())
				) {
			return true;
		}
		if (onlyAssignments) {
			ITER_OPS : while (true) {
				node = node.getRParent();
				switch (node.getNodeType()) {
				case A_LEFT:
				case A_EQUALS:
				case A_RIGHT:
					continue ITER_OPS;
				case BLOCK:
				case SOURCELINES:
				case C_IF:
				case C_FOR:
				case C_WHILE:
				case C_REPEAT:
				case F_CALL_ARG:
				case F_DEF_ARG:
				case SUB_INDEXED_ARG:
					return true;
				default:
					break ITER_OPS;
				}
			}
		}
		return false;
	}
	
	private final boolean sameLine(final int offset1, final int offset2) throws BadLocationException {
		return (offset1 == offset2
				|| fDoc.getLineOfOffset(offset1) == fDoc.getLineOfOffset(offset2));
	}
	
	public final void updateEnterBrackets() {
		fScope.baseColumn += fBlockCol;
	}
	
	public final void updateLeaveBrackets() {
		fScope.baseColumn -= fBlockCol;
	}
	
	public final void updateEnterFDefBody() throws BadLocationException {
		if (useParent(true, true, fScope.commandNode)) {
			fScope.baseColumn = fScope.parent.baseColumn;
		}
	}
	
	public final int getIndent(final int line) {
		return fScope.getIndent(line);
	}
	
}
