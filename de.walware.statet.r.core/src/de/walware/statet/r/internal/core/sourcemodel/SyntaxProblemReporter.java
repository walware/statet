/*=============================================================================#
 # Copyright (c) 2008-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.core.sourcemodel;

import static de.walware.ecommons.ltk.ast.IAstNode.NA_OFFSET;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_NUMBER_HEX_DIGIT_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_NUMBER_HEX_FLOAT_EXP_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_NUMBER_INT_WITH_DEC_POINT;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_NUMBER_NON_INT_WITH_L;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_HEX_DIGIT_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNKOWN;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS123_SYNTAX_TEXT_NULLCHAR;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS12_SYNTAX_TEXT_INVALID;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS12_SYNTAX_TOKEN_NOT_CLOSED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS12_SYNTAX_TOKEN_UNEXPECTED;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS12_SYNTAX_TOKEN_UNKNOWN;
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
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_IF_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_IN_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_OPERATOR_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS2_SYNTAX_SYMBOL_MISSING;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUSFLAG_SUBSEQUENT;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_MASK_12;
import static de.walware.statet.r.core.rsource.IRSourceConstants.STATUS_MASK_123;
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

import de.walware.ecommons.MessageBuilder;
import de.walware.ecommons.ltk.IProblem;
import de.walware.ecommons.ltk.IProblemRequestor;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.ltk.ast.StatusDetail;
import de.walware.ecommons.ltk.core.SourceContent;
import de.walware.ecommons.ltk.core.impl.Problem;
import de.walware.ecommons.ltk.core.model.ISourceUnit;
import de.walware.ecommons.text.core.ILineInformation;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
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
	
	
	private static final int BUFFER_SIZE= 100;
	
	private static final int FULL_TEXT_LIMIT= 100;
	private static final int START_TEXT_LIMIT= 25;
	
	private static final int MASK= 0x00ffffff;
	
	
	private final boolean reportSubsequent= false;
	
	private ISourceUnit sourceUnit;
	private SourceContent sourceContent;
	private String fCurrentText;
	private IProblemRequestor fCurrentRequestor;
	
	private final StringBuilder tmpBuilder= new StringBuilder();
	private final MessageBuilder messageBuilder= new MessageBuilder();
	private final List<IProblem> problemBuffer= new ArrayList<>(BUFFER_SIZE);
//	private int fMaxOffset;
	
	
	public SyntaxProblemReporter() {
	}
	
	
	public void run(final IRSourceUnit su, final SourceContent content,
			final RAstNode node, final IProblemRequestor problemRequestor) {
		try {
			this.sourceUnit= su;
			this.sourceContent= content;
			this.fCurrentText= content.getText();
//			fCurrentDoc= su.getDocument(null);
//			fMaxOffset= fCurrentDoc.getLength();
			this.fCurrentRequestor= problemRequestor;
			node.acceptInR(this);
			if (this.problemBuffer.size() > 0) {
				this.fCurrentRequestor.acceptProblems(RModel.R_TYPE_ID, this.problemBuffer);
			}
		}
		catch (final OperationCanceledException e) {}
		catch (final InvocationTargetException e) {}
		finally {
			this.problemBuffer.clear();
			this.sourceUnit= null;
//			fCurrentDoc= null;
			this.fCurrentRequestor= null;
		}
	}
	
	
	private boolean requiredCheck(final int code) {
		return code != STATUS_OK &&
				(this.reportSubsequent || ((code & STATUSFLAG_SUBSEQUENT) == 0));
	}
	
	protected final void addProblem(final int severity, final int code, final String message,
			int startOffset, int stopOffset) {
		if (startOffset < this.sourceContent.getBeginOffset()) {
			startOffset= this.sourceContent.getBeginOffset();
		}
		if (stopOffset < startOffset) {
			stopOffset= startOffset;
		}
		else if (stopOffset > this.sourceContent.getEndOffset()) {
			stopOffset= this.sourceContent.getEndOffset();
		}
		
		this.problemBuffer.add(new Problem(RModel.R_TYPE_ID, severity, code, message,
				this.sourceUnit, startOffset, stopOffset ));
		
		if (this.problemBuffer.size() >= BUFFER_SIZE) {
			this.fCurrentRequestor.acceptProblems(RModel.R_TYPE_ID, this.problemBuffer);
			this.problemBuffer.clear();
		}
	}
	
	
	protected final StringBuilder getStringBuilder() {
		this.tmpBuilder.setLength(0);
		return this.tmpBuilder;
	}
	
	protected String getStartText(final RAstNode node, final int offset)
			throws BadLocationException {
		final String text= node.getText();
		if (text != null) {
			if (text.length() > START_TEXT_LIMIT) {
				final StringBuilder sb= getStringBuilder();
				sb.append(text, 0, START_TEXT_LIMIT);
				sb.append('…');
				return sb.toString();
			}
			else {
				return text;
			}
		}
		else {
			if (node.getLength() - offset > START_TEXT_LIMIT) {
				final StringBuilder sb= getStringBuilder();
				sb.append(this.sourceContent.getText(), 
						node.getOffset() + offset, node.getOffset() + offset + START_TEXT_LIMIT);
				sb.append('…');
				return sb.toString();
			}
			else {
				return this.sourceContent.getText().substring(
						node.getOffset() + offset, node.getEndOffset() + offset );
			}
		}
	}
	
	protected String getDetailText(final RAstNode node, final int offset, final StatusDetail detail)
			throws BadLocationException {
		final String text= node.getText();
		if (text != null) {
			final int begin= detail.getOffset() - node.getOffset() - offset;
			return text.substring(begin, begin + detail.getLength());
		}
		else {
			return this.sourceContent.getText().substring(
					detail.getOffset(), detail.getOffset() + detail.getLength() );
		}
	}
	
	
	private void handleCommonCodes(final RAstNode node, final int code)
			throws BadLocationException, InvocationTargetException {
		STATUS: switch (code & STATUS_MASK_12) {
		case STATUS_RUNTIME_ERROR:
			throw new InvocationTargetException(new CoreException(
					new Status(IStatus.ERROR, RCore.PLUGIN_ID, -1,
							"Error occurred when parsing source code. Please submit a bug report with a code snippet.", //$NON-NLS-1$
							null)));
		
		case STATUS12_SYNTAX_TOKEN_UNEXPECTED:
			addProblem(IProblem.SEVERITY_ERROR, code,
					this.messageBuilder.bind(ProblemMessages.Syntax_TokenUnexpected_message, getFullText(node)),
					node.getOffset(), node.getEndOffset() );
			break STATUS;
			
		default:
			handleUnknownCodes(node);
			break STATUS;
		}
	}
	
	protected void handleUnknownCodes(final RAstNode node) {
		final int code= (node.getStatusCode() & MASK);
		final StringBuilder sb= new StringBuilder();
		sb.append("Unhandled/Unknown code of R AST node:"); //$NON-NLS-1$
		sb.append('\n');
		sb.append("  Code: 0x").append(Integer.toHexString(code)); //$NON-NLS-1$
		sb.append('\n');
		sb.append("  Node: ").append(node);
		sb.append(" (").append(node.getOffset()).append(", ").append(node.getLength()).append(')'); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append('\n');
		if (this.sourceContent != null) {
			try {
				final ILineInformation lines= this.sourceContent.getLines();
				final int line= lines.getLineOfOffset(node.getOffset());
				sb.append("  Line ").append((line+1)).append(" (offset )").append(lines.getLineOffset(line)); //$NON-NLS-1$ //$NON-NLS-2$
				sb.append('\n');
				
				final int firstLine= Math.max(0, line-2);
				final int lastLine= Math.min(lines.getNumberOfLines()-1, lines.getLineOfOffset(line)+2);
				sb.append("  Source (line ").append((firstLine+1)).append('-').append((lastLine)).append("): \n"); //$NON-NLS-1$ //$NON-NLS-2$
				sb.append(this.fCurrentText.substring(lines.getLineOffset(firstLine),
						lines.getLineOffset(lastLine) ));
			}
			catch (final BadLocationException e) {
			}
		}
		RCorePlugin.log(new Status(IStatus.WARNING, RCore.PLUGIN_ID, sb.toString()));
	}
	
	
	@Override
	public void visit(final SourceComponent node) throws InvocationTargetException {
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & IRSourceConstants.STATUS_MASK_2)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
				STATUS: switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_CC_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_BlockNotClosed_message,
							node.getEndOffset() - 1, node.getEndOffset() + 1 );
					break STATUS;
				default:
					handleCommonCodes(node, code);
					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
				STATUS: switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_CC_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_GroupNotClosed_message,
							node.getEndOffset()-1, node.getEndOffset()+1 );
					break STATUS;
				default:
					handleCommonCodes(node, code);
					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
				STATUS: switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_IF_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_IfOfElseMissing_message,
							node.getOffset(), node.getOffset()+1 );
					break STATUS;
				case STATUS2_SYNTAX_CONDITION_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ConditionMissing_If_message,
							node.getOffset()+1, node.getOffset()+3 );
					break STATUS;
				case STATUS2_SYNTAX_CONDITION_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ConditionNotClosed_If_message,
							node.getCondChild().getEndOffset()-1, node.getCondChild().getEndOffset()+1 );
					break STATUS;
				default:
					handleCommonCodes(node, code);
					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
				STATUS: switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_CONDITION_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ConditionMissing_For_message,
							node.getOffset()+2, node.getOffset()+4 );
					break STATUS;
				case STATUS2_SYNTAX_CONDITION_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ConditionNotClosed_For_message,
							node.getCondChild().getEndOffset()-1, node.getCondChild().getEndOffset()+1 );
					break STATUS;
				case STATUS2_SYNTAX_IN_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_InOfForConditionMissing_message,
							node.getVarChild().getEndOffset()-1, node.getVarChild().getEndOffset()+1 );
					break STATUS;
				default:
					handleCommonCodes(node, code);
					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
				STATUS: switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_CONDITION_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ConditionMissing_While_message,
							node.getOffset()+4, node.getOffset()+6 );
					break;
				case STATUS2_SYNTAX_CONDITION_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ConditionNotClosed_While_message,
							node.getCondChild().getEndOffset()-1, node.getCondChild().getEndOffset()+1 );
					break STATUS;
				default:
					handleCommonCodes(node, code);
					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
				STATUS: switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_FCALL_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_FcallArgsNotClosed_message,
							node.getArgsChild().getEndOffset()-1, node.getArgsChild().getEndOffset()+1 );
					break STATUS;
				default:
					handleCommonCodes(node, code);
					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
				STATUS: switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_FDEF_ARGS_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_FdefArgsMissing_message,
							node.getOffset()+7, node.getOffset()+9 );
					break STATUS;
				case STATUS2_SYNTAX_FDEF_ARGS_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_FdefArgsNotClosed_message,
							node.getArgsChild().getEndOffset()-1, node.getArgsChild().getEndOffset()+1 );
					break STATUS;
				default:
					handleCommonCodes(node, code);
					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
				if ((code & STATUS_MASK_123) == (IRSourceConstants.STATUS123_SYNTAX_SEQREL_UNEXPECTED)) {
					addProblem(IProblem.SEVERITY_ERROR, code, this.messageBuilder.bind(
							ProblemMessages.Syntax_TokenUnexpected_SeqRel_message,
							node.getOperator(0).text ),
							node.getOffset(), node.getEndOffset() );
				}
				else {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	@Override
	public void visit(final Logical node) throws InvocationTargetException {
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
				STATUS: switch ((code & STATUS_MASK_12)) {
				case STATUS12_SYNTAX_TOKEN_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code, this.messageBuilder.bind(
							ProblemMessages.Syntax_SpecialNotClosed_message,
							getStartText(node, 1) ),
							node.getEndOffset()-1, node.getEndOffset()+1 );
					break STATUS;
				default:
					handleCommonCodes(node, code);
					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
				STATUS: switch ((code & STATUS_MASK_12)) {
				case IRSourceConstants.STATUS2_SYNTAX_SUBINDEXED_NOT_CLOSED:
					if (node.getNodeType() == NodeType.SUB_INDEXED_S) {
						addProblem(IProblem.SEVERITY_ERROR, code, this.messageBuilder.bind(
								ProblemMessages.Syntax_SubindexedNotClosed_S_message,
								getStartText(node, 0) ),
								node.getEndOffset()-1, node.getEndOffset()+1 );
						break STATUS;
					}
					else if (node.getSublistCloseOffset() != NA_OFFSET) {
						addProblem(IProblem.SEVERITY_ERROR, code, this.messageBuilder.bind(
								ProblemMessages.Syntax_SubindexedNotClosed_Done_message,
								getStartText(node, 0) ),
								node.getEndOffset()-1, node.getEndOffset()+1 );
						break STATUS;
					}
					else {
						addProblem(IProblem.SEVERITY_ERROR, code, this.messageBuilder.bind(
								ProblemMessages.Syntax_SubindexedNotClosed_Dboth_message,
								getStartText(node, 0) ),
								node.getEndOffset()-1, node.getEndOffset()+1 );
						break STATUS;
					}
				default:
					handleCommonCodes(node, code);
					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
//				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	private void handleTextInvalid(final RAstNode node, final int code) throws BadLocationException {
		final StatusDetail detail= StatusDetail.getStatusDetail(node);
		switch ((code & STATUS_MASK_123)) {
		case STATUS123_SYNTAX_TEXT_NULLCHAR:
			addProblem(IProblem.SEVERITY_ERROR, code,
					ProblemMessages.Syntax_Text_NullCharNotAllowed_message,
					detail.getOffset(), detail.getOffset() + detail.getLength() );
			return;
		case STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_HEX_DIGIT_MISSING:
			addProblem(IProblem.SEVERITY_ERROR, code, this.messageBuilder.bind(
					ProblemMessages.Syntax_Text_EscapeSeqHexDigitMissing_message,
					detail.getText() ),
					detail.getOffset(), detail.getOffset() + detail.getLength() );
			return;
		case STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_NOT_CLOSED:
			addProblem(IProblem.SEVERITY_ERROR, code, this.messageBuilder.bind(
					ProblemMessages.Syntax_Text_EscapeSeqNotClosed_message,
					detail.getText() ),
					detail.getOffset(), detail.getOffset() + detail.getLength() );
			return;
		case STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNEXPECTED:
			addProblem(IProblem.SEVERITY_ERROR, code, this.messageBuilder.bind(
					ProblemMessages.Syntax_Text_QuotedSymbol_EscapeSeqUnexpected_message,
					detail.getText() ),
					detail.getOffset(), detail.getOffset() + detail.getLength() );
			return;
		case STATUS123_SYNTAX_TEXT_ESCAPE_SEQ_UNKOWN:
			addProblem(IProblem.SEVERITY_ERROR, code, this.messageBuilder.bind(
					ProblemMessages.Syntax_Text_EscapeSeqUnknown_message,
					detail.getText() ),
					detail.getOffset(), detail.getOffset() + detail.getLength() );
			return;
		default:
			handleUnknownCodes(node);
			return;
		}
	}
	
	@Override
	public void visit(final StringConst node) throws InvocationTargetException {
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
				STATUS: switch ((code & STATUS_MASK_12)) {
				case STATUS12_SYNTAX_TOKEN_NOT_CLOSED:
					addProblem(IProblem.SEVERITY_ERROR, code, this.messageBuilder.bind(
							ProblemMessages.Syntax_StringNotClosed_message,
							getStartText(node, 1), node.getOperator(0).text ),
							node.getEndOffset()-1, node.getEndOffset()+1 );
					break STATUS;
				case STATUS12_SYNTAX_TEXT_INVALID:
					handleTextInvalid(node, code);
					break STATUS;
				default:
					handleCommonCodes(node, code);
					break STATUS;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
	}
	
	@Override
	public void visit(final NumberConst node) throws InvocationTargetException {
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
				STATUS: switch ((code & STATUS_MASK_123)) {
				case STATUS123_SYNTAX_NUMBER_HEX_DIGIT_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code, this.messageBuilder.bind(
							ProblemMessages.Syntax_Number_HexDigitMissing_message,
							getFullText(node) ),
							node.getOffset(), node.getEndOffset() );
					break STATUS;
				case STATUS123_SYNTAX_NUMBER_HEX_FLOAT_EXP_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code, this.messageBuilder.bind(
							ProblemMessages.Syntax_Number_HexFloatExpMissing_message,
							getFullText(node) ),
							node.getOffset(), node.getEndOffset() );
					break STATUS;
				case STATUS123_SYNTAX_NUMBER_EXP_DIGIT_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code, this.messageBuilder.bind(
							ProblemMessages.Syntax_Number_ExpDigitMissing_message,
							getFullText(node) ),
							node.getOffset(), node.getEndOffset() );
					break STATUS;
				case STATUS123_SYNTAX_NUMBER_NON_INT_WITH_L:
					addProblem(IProblem.SEVERITY_WARNING, code, this.messageBuilder.bind(
							ProblemMessages.Syntax_Number_NonIntWithLLiteral_message,
							getFullText(node) ),
							node.getOffset(), node.getEndOffset() );
					break STATUS;
				case STATUS123_SYNTAX_NUMBER_INT_WITH_DEC_POINT:
					addProblem(IProblem.SEVERITY_WARNING, code, this.messageBuilder.bind(
							ProblemMessages.Syntax_Number_IntWithDecPoint_message,
							getFullText(node) ),
							node.getOffset(), node.getEndOffset() );
					break STATUS;
				default:
					handleCommonCodes(node, code);
					break STATUS;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
	}
	
	@Override
	public void visit(final NullConst node) throws InvocationTargetException {
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
//					break STATUS;
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
				STATUS: switch ((code & STATUS_MASK_12)) {
				case STATUS2_SYNTAX_SYMBOL_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_SymbolMissing_message,
							node.getOffset() - 1, node.getEndOffset() + 1 );
					break STATUS;
				case STATUS2_SYNTAX_ELEMENTNAME_MISSING:
					// this can be a status for string too, but never used there
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ElementnameMissing_message,
							node.getOffset() - 1, node.getEndOffset() + 1 );
					break STATUS;
				case STATUS12_SYNTAX_TOKEN_NOT_CLOSED:
					// assert(node.getOperator(0) == RTerminal.SYMBOL_G)
					addProblem(IProblem.SEVERITY_ERROR, code, this.messageBuilder.bind(
							ProblemMessages.Syntax_QuotedSymbolNotClosed_message,
							getStartText(node, 1) ),
							node.getEndOffset() - 1, node.getEndOffset() + 1 );
					break STATUS;
				case STATUS12_SYNTAX_TEXT_INVALID:
					// assert(node.getOperator(0) == RTerminal.SYMBOL_G)
					handleTextInvalid(node, code);
					break STATUS;
				default:
					handleCommonCodes(node, code);
					break STATUS;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
	}
	
	@Override
	public void visit(final Help node) throws InvocationTargetException {
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
//				STATUS: switch ((code & STATUS_MASK_12)) {
//				default:
					handleCommonCodes(node, code);
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
		final int code= (node.getStatusCode() & MASK);
		if (requiredCheck(code)) {
			try {
				STATUS: switch ((code & STATUS_MASK_12)) {
				case STATUS12_SYNTAX_TOKEN_UNKNOWN:
					addProblem(IProblem.SEVERITY_ERROR, code, this.messageBuilder.bind(
							ProblemMessages.Syntax_TokenUnknown_message,
							getFullText(node) ),
							node.getOffset(), node.getEndOffset() );
					break STATUS;
				case STATUS2_SYNTAX_EXPR_BEFORE_OP_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ExprBeforeOpMissing_message,
							node.getOffset()-1, node.getEndOffset()+1 );
					break STATUS;
				case STATUS2_SYNTAX_EXPR_AFTER_OP_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code, this.messageBuilder.bind(
							ProblemMessages.Syntax_ExprAfterOpMissing_message,
							getFullText(node) ),
							node.getOffset()-1, node.getEndOffset()+1 );
					break STATUS;
//					case STATUS2_SYNTAX_EXPR_AS_REF_MISSING:
//					addProblem(IProblem.ERROR, code,
//							ProblemMessages.,
//							node.getStartOffset()-1, node.getStopOffset()+1);
//					break;
				case STATUS2_SYNTAX_EXPR_AS_CONDITION_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ExprAsConditionMissing_message,
							node.getOffset()-1, node.getEndOffset()+1 );
					break STATUS;
				case STATUS2_SYNTAX_EXPR_AS_FORSEQ_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ExprAsForSequenceMissing_message,
							node.getOffset()-1, node.getEndOffset()+1 );
					break STATUS;
				case STATUS2_SYNTAX_EXPR_AS_BODY_MISSING:
				{
					final String message;
					STATUS3: switch (code & IRSourceConstants.STATUS_MASK_3) {
					case IRSourceConstants.STATUS3_IF:
						message= ProblemMessages.Syntax_ExprAsThenBodyMissing_message;
						break STATUS3;
					case IRSourceConstants.STATUS3_ELSE:
						message= ProblemMessages.Syntax_ExprAsElseBodyMissing_message;
						break STATUS3;
					case IRSourceConstants.STATUS3_FOR:
					case IRSourceConstants.STATUS3_WHILE:
					case IRSourceConstants.STATUS3_REPEAT:
						message= ProblemMessages.Syntax_ExprAsLoopBodyMissing_message;
						break STATUS3;
					case IRSourceConstants.STATUS3_FDEF:
						message= ProblemMessages.Syntax_ExprAsFdefBodyMissing_message;
						break STATUS3;
					default:
						handleUnknownCodes(node);
						break STATUS;
					}
					if (node.getLength() > 0) {
						addProblem(IProblem.SEVERITY_ERROR, code, message,
								node.getOffset(), node.getEndOffset() );
					}
					else {
						addProblem(IProblem.SEVERITY_ERROR, code, message,
								node.getOffset()-1, node.getEndOffset()+1 );
					}
					break STATUS;
				}
				case STATUS2_SYNTAX_EXPR_IN_GROUP_MISSING:
					addProblem(IProblem.SEVERITY_ERROR, code,
							ProblemMessages.Syntax_ExprInGroupMissing_message,
							node.getOffset()-1, node.getEndOffset()+1 );
					break STATUS;
				case STATUS2_SYNTAX_EXPR_AS_ARGVALUE_MISSING:
					if ((code & STATUS_MASK_3) == IRSourceConstants.STATUS3_FDEF) {
						addProblem(IProblem.SEVERITY_ERROR, code,
								ProblemMessages.Syntax_ExprAsFdefArgDefaultMissing_message,
								node.getOffset()-1, node.getEndOffset() );
					}
					else {
						handleUnknownCodes(node);
					}
					break STATUS;
				case STATUS2_SYNTAX_OPERATOR_MISSING:
					if (node.getChildCount() == 2) {
						addProblem(IProblem.SEVERITY_ERROR, code,
								ProblemMessages.Syntax_OperatorMissing_message,
								node.getChild(0).getEndOffset()-1, node.getChild(1).getOffset()+1 );
					}
					else {
						handleUnknownCodes(node);
					}
					break STATUS;
				default:
					handleCommonCodes(node, code);
					break STATUS;
				}
			}
			catch (final BadLocationException e) {
				throw new InvocationTargetException(e);
			}
		}
		node.acceptInRChildren(this);
	}
	
	
	protected String getFullText(final RAstNode node) throws BadLocationException {
		final String text= node.getText();
		if (text != null) {
			if (text.length() > FULL_TEXT_LIMIT) {
				return text.substring(0, FULL_TEXT_LIMIT) + '…';
			}
			else {
				return text;
			}
		}
		else {
			if (node.getLength() > FULL_TEXT_LIMIT) {
				return this.fCurrentText.substring(node.getOffset(),
						node.getOffset()+FULL_TEXT_LIMIT ) + '…';
			}
			else {
				return this.fCurrentText.substring(node.getOffset(), node.getEndOffset());
			}
		}
	}
	
}
