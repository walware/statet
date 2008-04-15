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

import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_CC_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_CONDITION_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_CONDITION_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_ELEMENTNAME_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AFTER_OP_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AS_ARGVALUE_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AS_BODY_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AS_CONDITION_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_AS_FORSEQ_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_BEFORE_OP_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_EXPR_IN_GROUP_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_FCALL_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_FDEF_ARGS_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_FDEF_ARGS_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_FLOAT_EXP_INVALID;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_FLOAT_WITH_L;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_IF_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_IN_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_OPERATOR_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_SYMBOL_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_TOKEN_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_TOKEN_UNEXPECTED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_TOKEN_UNKNOWN;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUSFLAG_SUBSEQUENT;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_MASK_12;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_MASK_3;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_OK;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_RUNTIME_ERROR;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.osgi.util.NLS;

import de.walware.eclipsecommons.ltk.AstInfo;
import de.walware.eclipsecommons.ltk.IProblem;
import de.walware.eclipsecommons.ltk.IProblemRequestor;
import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.Problem;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.core.rsource.IRSourceConstants;
import de.walware.statet.r.core.rsource.ast.Arithmetic;
import de.walware.statet.r.core.rsource.ast.Assignment;
import de.walware.statet.r.core.rsource.ast.Block;
import de.walware.statet.r.core.rsource.ast.CForLoop;
import de.walware.statet.r.core.rsource.ast.CIfElse;
import de.walware.statet.r.core.rsource.ast.CLoopCommand;
import de.walware.statet.r.core.rsource.ast.CRepeatLoop;
import de.walware.statet.r.core.rsource.ast.CWhileLoop;
import de.walware.statet.r.core.rsource.ast.Dummy;
import de.walware.statet.r.core.rsource.ast.FCall;
import de.walware.statet.r.core.rsource.ast.FDef;
import de.walware.statet.r.core.rsource.ast.Group;
import de.walware.statet.r.core.rsource.ast.Help;
import de.walware.statet.r.core.rsource.ast.Logical;
import de.walware.statet.r.core.rsource.ast.Model;
import de.walware.statet.r.core.rsource.ast.NSGet;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.NullConst;
import de.walware.statet.r.core.rsource.ast.NumberConst;
import de.walware.statet.r.core.rsource.ast.Power;
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
import de.walware.statet.r.internal.core.RCorePlugin;


/**
 * Reports syntax problems in AST to {@link IProblemRequestor} of source units
 */
public class SyntaxProblemReporter extends RAstVisitor {
	
	
	public static final int OK = IProblem.SEVERITY_INFO;
	public static final int WARNING = IProblem.SEVERITY_WARNING;
	public static final int ERROR = IProblem.SEVERITY_ERROR;
	
	
	private static final int FULL_TEXT_LIMIT = 100;
	private static final int START_TEXT_LIMIT = 25;
	private static final int BUFFER_SIZE = 100;
	
	private ISourceUnit fCurrentUnit;
	private IDocument fCurrentDoc;
	private IProblemRequestor fCurrentRequestor;
	private final List<IProblem> fProblemBuffer = new ArrayList<IProblem>(BUFFER_SIZE);
	private boolean fReportSubsequent = false;
	
	
	public SyntaxProblemReporter() {
	}
	
	
	public void run(final IRSourceUnit unit, final AstInfo<RAstNode> ast, final IProblemRequestor problemRequestor) {
		try {
			fCurrentUnit = unit;
			fCurrentDoc = unit.getDocument();
			fCurrentRequestor = problemRequestor;
			ast.root.acceptInR(this);
			if (fProblemBuffer.size() > 0) {
				fCurrentRequestor.acceptProblems("r", fProblemBuffer); //$NON-NLS-1$
			}
		}
		catch (final OperationCanceledException e) {}
		catch (final InvocationTargetException e) {}
		finally {
			fProblemBuffer.clear();
			fCurrentUnit = null;
			fCurrentDoc = null;
			fCurrentRequestor = null;
		}
	}
	
	
	private void handleCommonCodes(final RAstNode node) throws BadLocationException, InvocationTargetException {
		final int code = node.getStatusCode();
		switch (code & STATUS_MASK_12) {
		case STATUS_RUNTIME_ERROR:
			throw new InvocationTargetException(new CoreException(
					new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
							"Error occurred when parsing source code. Please submit a bug report with a code snippet.", //$NON-NLS-1$
							null)));
		
		case STATUS2_SYNTAX_TOKEN_UNEXPECTED:
			addProblem(IProblem.SEVERITY_ERROR, code, NLS.bind(
					ProblemMessages.Syntax_TokenUnexpected_message,
					getFullText(node)),
					node.getOffset(), node.getStopOffset());
			return;
		}
		handleUnknownCodes(node);
	}
	
	protected void handleUnknownCodes(final RAstNode node) {
		final int code = node.getStatusCode();
		String message = "Unhandled/Unknown code of R AST node:\n"+ //$NON-NLS-1$
				"  Code: 0x"+Integer.toHexString(code)+"\n"+ //$NON-NLS-1$ //$NON-NLS-2$
				"  Node: "+node.toString()+" ("+node.getOffset()+", "+node.getLength()+")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (fCurrentDoc != null) {
			try {
				final int line = fCurrentDoc.getLineOfOffset(node.getOffset());
				final IRegion lineInfo = fCurrentDoc.getLineInformation(line);
				message += "  Line "+line+" at offset "+lineInfo.getOffset()+"(can be wrong, if out of synch): \n    "+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						fCurrentDoc.get(lineInfo.getOffset(), lineInfo.getLength());
			}
			catch (final BadLocationException e) {
			}
		}
		RCorePlugin.log(new Status(IStatus.WARNING, RCore.PLUGIN_ID, message));
	}
	
	
	@Override
	public void visit(final SourceComponent node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & IRSourceConstants.STATUS_MASK_2)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final Block node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
				switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_CC_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_BlockNotClosed_message,
							node.getOffset(), node.getOffset()+1);
					break;
				default:
					handleCommonCodes(node);
					break;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final Group node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
				switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_CC_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_GroupNotClosed_message,
							node.getOffset(), node.getOffset()+1);
					break;
				default:
					handleCommonCodes(node);
					break;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final CIfElse node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
				switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_IF_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_IfOfElseMissing_message,
							node.getOffset(), node.getOffset()+1);
					break;
				case STATUS2_SYNTAX_CONDITION_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ConditionMissing_If_message,
							node.getOffset()+1, node.getOffset()+3);
					break;
				case STATUS2_SYNTAX_CONDITION_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ConditionNotClosed_If_message,
							node.getCondOpenOffset(), node.getCondOpenOffset()+1);
					break;
				default:
					handleCommonCodes(node);
					break;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final CForLoop node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
				switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_CONDITION_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ConditionMissing_For_message,
							node.getOffset()+2, node.getOffset()+4);
					break;
				case STATUS2_SYNTAX_CONDITION_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ConditionNotClosed_For_message,
							node.getCondOpenOffset(), node.getCondOpenOffset()+1);
					break;
				case STATUS2_SYNTAX_IN_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_InOfForConditionMissing_message,
							node.getVarChild().getStopOffset()-1, node.getVarChild().getStopOffset()+1);
					break;
				default:
					handleCommonCodes(node);
					break;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	
	@Override
	public void visit(final CRepeatLoop node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final CWhileLoop node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
				switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_CONDITION_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ConditionMissing_While_message,
							node.getOffset()+4, node.getOffset()+6);
					break;
				case STATUS2_SYNTAX_CONDITION_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ConditionNotClosed_While_message,
							node.getCondOpenOffset(), node.getCondOpenOffset()+1);
					break;
				default:
					handleCommonCodes(node);
					break;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final CLoopCommand node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final FCall node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
				switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_FCALL_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_FcallArgsNotClosed_message,
							node.getArgsOpenOffset(), node.getArgsOpenOffset()+1);
					break;
				default:
					handleCommonCodes(node);
					break;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final FCall.Args node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final FCall.Arg node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final FDef node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
				switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_FDEF_ARGS_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_FdefArgsMissing_message,
							node.getOffset()+7, node.getOffset()+9);
					break;
				case STATUS2_SYNTAX_FDEF_ARGS_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_FdefArgsNotClosed_message,
							node.getArgsOpenOffset(), node.getArgsOpenOffset()+1);
					break;
				default:
					handleCommonCodes(node);
					break;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final FDef.Args node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final FDef.Arg node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final Assignment node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final Model node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final Relational node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final Logical node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final Arithmetic node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final Power node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final Seq node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final Special node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
				switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_TOKEN_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code, NLS.bind(
							ProblemMessages.Syntax_SpecialNotClosed_message,
							getStartText(node)),
							node.getStopOffset()-1, node.getStopOffset()+1);
					break;
				default:
					handleCommonCodes(node);
					break;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final Sign node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final SubIndexed node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
				switch ((code & STATUS_MASK_12)) {
				case IRSourceConstants.STATUS2_SYNTAX_SUBINDEXED_NOT_CLOSED:
				{
					if (node.getNodeType() == NodeType.SUB_INDEXED_S) {
						addProblem(IProblem.SEVERITY_ERROR, code, NLS.bind(
								ProblemMessages.Syntax_SubindexedNotClosed_S_message,
								getStartText(node)),
								node.getSublistOpenOffset(), node.getSublistOpenOffset()+2);
					}
					else if (node.getSublistCloseOffset() >= 0) {
						addProblem(IProblem.SEVERITY_ERROR, code, NLS.bind(
								ProblemMessages.Syntax_SubindexedNotClosed_Done_message,
								getStartText(node)),
								node.getSublistCloseOffset(), node.getSublistCloseOffset()+1);
					}
					else {
						addProblem(IProblem.SEVERITY_ERROR, code, NLS.bind(
								ProblemMessages.Syntax_SubindexedNotClosed_Dboth_message,
								getStartText(node)),
								node.getSublistOpenOffset(), node.getSublistOpenOffset()+2);
					}
					break;
				}
				default:
					handleCommonCodes(node);
					break;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final SubIndexed.Args node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final SubIndexed.Arg node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final SubNamed node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final NSGet node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final StringConst node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
				switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_TOKEN_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code, NLS.bind(
							ProblemMessages.Syntax_StringNotClosed_message,
							getStartText(node), node.getOperator(0).text),
							node.getStopOffset()-1, node.getStopOffset()+1);
					break;
				default:
					handleCommonCodes(node);
					break;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
	}
	
	@Override
	public void visit(final NumberConst node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
				switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_FLOAT_WITH_L:
					addProblem(IProblem.SEVERITY_WARNING, code, NLS.bind(
							ProblemMessages.Syntax_FloatWithLLiteral_message,
							getFullText(node)),
							node.getOffset(), node.getStopOffset());
					break;
				case STATUS2_SYNTAX_FLOAT_EXP_INVALID:
					addProblem(IProblem.SEVERITY_ERROR, code, NLS.bind(
							ProblemMessages.Syntax_FloatExpInvalid_message,
							getFullText(node), node.getOperator(0).text),
							node.getOffset(), node.getStopOffset());
					break;
				default:
					handleCommonCodes(node);
					break;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
	}
	
	@Override
	public void visit(final NullConst node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final Symbol node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
				switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_SYMBOL_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_SymbolMissing_message,
							node.getOffset()-1, node.getStopOffset()+1);
					break;
				case STATUS2_SYNTAX_ELEMENTNAME_MISSING:
					// this can be a status for string too, but never used there
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ElementnameMissing_message,
							node.getOffset()-1, node.getStopOffset()+1);
					break;
				case STATUS2_SYNTAX_TOKEN_NOT_CLOSED:
					// assert(node.getOperator(0) == RTerminal.SYMBOL_G)
					addProblem(IProblem.SEVERITY_ERROR, code, NLS.bind(
							ProblemMessages.Syntax_QuotedSymbolNotClosed_message,
							getStartText(node)),
							node.getStopOffset()-1, node.getStopOffset()+1);
					break;
				default:
					handleCommonCodes(node);
					break;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
	}
	
	@Override
	public void visit(final Help node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
//				switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node);
//					break;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final Dummy node) throws InvocationTargetException {
		final int code = node.getStatusCode();
		if (code != STATUS_OK &&
				(fReportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0))) {
			try {
				STATUS2: switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_TOKEN_UNKNOWN:
					addProblem(IProblem.SEVERITY_ERROR, code, NLS.bind(
							ProblemMessages.Syntax_TokenUnknown_message,
							getFullText(node)),
							node.getOffset(), node.getStopOffset());
					break STATUS2;
				case STATUS2_SYNTAX_EXPR_BEFORE_OP_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ExprBeforeOpMissing_message,
							node.getOffset()-1, node.getStopOffset()+1);
					break STATUS2;
				case STATUS2_SYNTAX_EXPR_AFTER_OP_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code, NLS.bind(
							ProblemMessages.Syntax_ExprAfterOpMissing_message,
							getFullText(node)),
							node.getOffset()-1, node.getStopOffset()+1);
					break STATUS2;
//					case STATUS2_SYNTAX_EXPR_AS_REF_MISSING:
//					addProblem(IProblem.ERROR, code,
//							ProblemMessages.,
//							node.getStartOffset()-1, node.getStopOffset()+1);
//					break;
				case STATUS2_SYNTAX_EXPR_AS_CONDITION_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ExprAsConditionMissing_message,
							node.getOffset()-1, node.getStopOffset()+1);
					break STATUS2;
				case STATUS2_SYNTAX_EXPR_AS_FORSEQ_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ExprAsForSequenceMissing_message,
							node.getOffset()-1, node.getStopOffset()+1);
					break STATUS2;
				case STATUS2_SYNTAX_EXPR_AS_BODY_MISSING:
				{
					final String message;
					STATUS3: switch (code & IRSourceConstants.STATUS_MASK_3) {
					case IRSourceConstants.STATUS3_IF:
						message = ProblemMessages.Syntax_ExprAsThenBodyMissing_message;
						break STATUS3;
					case IRSourceConstants.STATUS3_ELSE:
						message = ProblemMessages.Syntax_ExprAsElseBodyMissing_message;
						break STATUS3;
					case IRSourceConstants.STATUS3_FOR:
					case IRSourceConstants.STATUS3_WHILE:
					case IRSourceConstants.STATUS3_REPEAT:
						message = ProblemMessages.Syntax_ExprAsLoopBodyMissing_message;
						break STATUS3;
					case IRSourceConstants.STATUS3_FDEF:
						message = ProblemMessages.Syntax_ExprAsFdefBodyMissing_message;
						break STATUS3;
					default:
						handleUnknownCodes(node);
						break STATUS2;
					}
					if (node.getLength() > 0) {
						addProblem(IProblem.SEVERITY_ERROR, code, message,
								node.getOffset(), node.getStopOffset());
					}
					else {
						addProblem(IProblem.SEVERITY_ERROR, code, message,
								node.getOffset()-1, node.getStopOffset()+1);
					}
					break STATUS2;
				}
				case STATUS2_SYNTAX_EXPR_IN_GROUP_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ExprInGroupMissing_message,
							node.getOffset()-1, node.getStopOffset()+1);
					break STATUS2;
				case STATUS2_SYNTAX_EXPR_AS_ARGVALUE_MISSING:
					if ((code & STATUS_MASK_3) == IRSourceConstants.STATUS3_FDEF) {
						addProblem(IProblem.SEVERITY_ERROR, code,
								ProblemMessages.Syntax_ExprAsFdefArgDefaultMissing_message,
								node.getOffset()-1, node.getStopOffset());
					}
					else {
						handleUnknownCodes(node);
					}
					break STATUS2;
				case STATUS2_SYNTAX_OPERATOR_MISSING:
					if (node.getChildCount() == 2) {
						addProblem(IProblem.SEVERITY_ERROR, code,
								ProblemMessages.Syntax_OperatorMissing_message,
								node.getChild(0).getStopOffset()-1, node.getChild(1).getOffset()+1);
					}
					else {
						handleUnknownCodes(node);
					}
					break STATUS2;
				default:
					handleCommonCodes(node);
					break STATUS2;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	
	protected String getFullText(final RAstNode node) throws BadLocationException {
		final String text = node.getText();
		if (text != null) {
			if (text.length() > FULL_TEXT_LIMIT) {
				return text.substring(0, FULL_TEXT_LIMIT)+'…';
			}
			else {
				return text;
			}
		}
		else {
			if (node.getLength() > FULL_TEXT_LIMIT) {
				return fCurrentDoc.get(node.getOffset(), FULL_TEXT_LIMIT)+'…';
			}
			else {
				return fCurrentDoc.get(node.getOffset(), node.getLength());
			}
		}
	}
	
	protected String getStartText(final RAstNode node) throws BadLocationException {
		final String text = node.getText();
		if (text != null) {
			if (text.length() > START_TEXT_LIMIT) {
				return text.substring(0, START_TEXT_LIMIT)+'…';
			}
			else {
				return text;
			}
		}
		else {
			if (node.getLength() > START_TEXT_LIMIT) {
				return fCurrentDoc.get(node.getOffset(), START_TEXT_LIMIT)+'…';
			}
			else {
				return fCurrentDoc.get(node.getOffset(), node.getLength());
			}
		}
	}
	
	
	protected final void addProblem(final int severity, final int code, final String message, final int startOffset, final int stopOffset) {
		fProblemBuffer.add(new Problem(severity, code, message,
				fCurrentUnit, startOffset, stopOffset));
		if (fProblemBuffer.size() >= BUFFER_SIZE) {
			fCurrentRequestor.acceptProblems("r", fProblemBuffer); //$NON-NLS-1$
			fProblemBuffer.clear();
		}
	}
	
}
