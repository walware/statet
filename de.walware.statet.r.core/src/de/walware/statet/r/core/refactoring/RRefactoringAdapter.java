/*******************************************************************************
 * Copyright (c) 2008-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.TextEdit;

import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.core.refactoring.RefactoringAdapter;
import de.walware.ecommons.text.IPartitionConstraint;
import de.walware.ecommons.text.IndentUtil;
import de.walware.ecommons.text.SourceParseInput;
import de.walware.ecommons.text.StringParseInput;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.IRSourceConstants;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.core.rsource.RIndentUtil;
import de.walware.statet.r.core.rsource.RSourceIndenter;
import de.walware.statet.r.core.rsource.RSourceToken;
import de.walware.statet.r.core.rsource.RSourceTokenLexer;
import de.walware.statet.r.core.rsource.ast.Assignment;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstInfo;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.core.rsource.ast.RScanner;
import de.walware.statet.r.internal.core.refactoring.Messages;


/**
 * RefactoringAdapter for R
 */
public class RRefactoringAdapter extends RefactoringAdapter {
	
	
	private RSourceTokenLexer fLexer;
	
	
	public RRefactoringAdapter() {
		super(new RHeuristicTokenScanner());
	}
	
	
	@Override
	public String getPluginIdentifier() {
		return RCore.PLUGIN_ID;
	}
	
	@Override
	public boolean isCommentContent(final ITypedRegion partition) {
		return (partition != null) && partition.getType().equals(IRDocumentPartitions.R_COMMENT);
	}
	
	public IRegion trimToAstRegion(final AbstractDocument document, final IRegion region) {
		fScanner.configure(document, new IPartitionConstraint() {
			public boolean matches(final String partitionType) {
				return (fPartitioning.getDefaultPartitionConstraint().matches(partitionType)
						|| partitionType == IRDocumentPartitions.R_STRING
						|| partitionType == IRDocumentPartitions.R_QUOTED_SYMBOL
						|| partitionType == IRDocumentPartitions.R_INFIX_OPERATOR);
			}
		});
		int start = region.getOffset();
		int stop = start+region.getLength();
		int result;
		
		while (stop > start) {
			result = fScanner.findNonBlankBackward(stop, start, true);
			if (result >= 0) {
				if (fScanner.getChar() == ';') {
					stop = result;
					continue;
				}
				else {
					stop = result + 1;
					break;
				}
			}
			else {
				stop = start;
				break;
			}
		}
		
		while (start < stop) {
			result = fScanner.findNonBlankForward(start, stop, true);
			if (result >= 0) {
				if (fScanner.getChar() == ';') {
					start = result + 1;
					continue;
				}
				else {
					start = result;
					break;
				}
			}
			else {
				start = stop;
				break;
			}
		}
		
		return new Region(start, stop-start);
	}
	
	public IRegion expandSelectionRegion(final AbstractDocument document, final IRegion region, final IRegion limit) {
		fScanner.configure(document, new IPartitionConstraint() {
			public boolean matches(final String partitionType) {
				return (partitionType != IRDocumentPartitions.R_COMMENT);
			}
		});
		final int min = limit.getOffset();
		final int max = limit.getOffset()+limit.getLength();
		int start = region.getOffset();
		int stop = start+region.getLength();
		int result;
		
		while (start > min) {
			result = fScanner.findNonBlankBackward(start, min, true);
			if (result >= 0) {
				if (fPartitioning.getDefaultPartitionConstraint().matches(fScanner.getPartition(result).getType())
						&& fScanner.getChar() == ';') {
					start = result;
					continue;
				}
				else {
					start = result + 1;
					break;
				}
			}
			else {
				start = min;
				break;
			}
		}
		
		while (stop < max) {
			result = fScanner.findNonBlankForward(stop, max, true);
			if (result >= 0) {
				if (fPartitioning.getDefaultPartitionConstraint().matches(fScanner.getPartition(result).getType())
						&& fScanner.getChar() == ';') {
					stop = result + 1;
					continue;
				}
				else {
					stop = result;
					break;
				}
			}
			else {
				stop = max;
				break;
			}
		}
		
		return new Region(start, stop-start);
	}
	
	
	public String validateIdentifier(final String value, final String identifierMessageName) {
		if (value == null || value.length() == 0) { 
			return (identifierMessageName != null) ?
					NLS.bind(Messages.RIdentifiers_error_EmptyFor_message, identifierMessageName, Messages.RIdentifiers_error_Empty_message) :
					Messages.RIdentifiers_error_Empty_message;
		}
		if (fLexer == null) {
			fLexer = new RSourceTokenLexer();
		}
		fLexer.reset(new StringParseInput(value), null);
		final RSourceToken nextToken = fLexer.nextToken();
		if (nextToken.getTokenType() == RTerminal.EOF) {
			return (identifierMessageName != null) ?
					NLS.bind(Messages.RIdentifiers_error_EmptyFor_message, identifierMessageName, Messages.RIdentifiers_error_Empty_message) :
					Messages.RIdentifiers_error_Empty_message;
		}
		if ((nextToken.getTokenType() != RTerminal.SYMBOL && nextToken.getTokenType() != RTerminal.SYMBOL_G)
				|| ((nextToken.getStatusCode() & IRSourceConstants.STATUSFLAG_REAL_ERROR) != 0)
				|| (fLexer.nextToken().getTokenType() != RTerminal.EOF)) {
			return (identifierMessageName != null) ?
					NLS.bind(Messages.RIdentifiers_error_InvalidFor_message, identifierMessageName, Messages.RIdentifiers_error_Empty_message) :
					Messages.RIdentifiers_error_Invalid_message;
		}
		return null;
	}
	
	static RAstNode getPotentialNameNode(final RAstNode node, final boolean allowAssignRegion) {
		switch (node.getNodeType()) {
		case A_LEFT:
		case A_EQUALS:
		case A_RIGHT:
			if (allowAssignRegion) {
				final Assignment assignment = (Assignment) node;
				if (assignment.isSearchOperator()) {
					switch (assignment.getTargetChild().getNodeType()) {
					case SYMBOL:
					case STRING_CONST:
						return assignment.getTargetChild();
					}
				}
			}
			return null;
		case SYMBOL:
		case STRING_CONST:
			return node;
		default:
			return null;
		}
	}
	
	static RElementAccess searchElementAccessOfNameNode(final RAstNode symbolNode) {
		RAstNode node = symbolNode;
		while (node != null) {
			final Object[] attachments = node.getAttachments();
			for (final Object attachment : attachments) {
				if (attachment instanceof RElementAccess) {
					final RElementAccess access = (RElementAccess) attachment;
					if (access.getNameNode() == symbolNode) {
						return access;
					}
				}
			}
			node = node.getRParent();
		}
		return null;
	}
	
	public static String getQuotedIdentifier(final String identifier) {
		int length;
		if (identifier == null || (length = identifier.length()) == 0) {
			return "";
		}
		if (identifier.charAt(0) == '`') {
			if (length > 1 && identifier.charAt(length-1) == '`') {
				return identifier;
			}
			else {
				return identifier + '`';
			}
		}
		else {
			return '`' + identifier + '`';
		}
	}
	
	public static String getUnquotedIdentifier(final String identifier) {
		int length;
		if (identifier == null || (length = identifier.length()) == 0) {
			return "";
		}
		if (identifier.charAt(0) == '`') {
			if (length > 1 && identifier.charAt(length-1) == '`') {
				return identifier.substring(1, length-2);
			}
			else {
				return identifier.substring(1, length-1);
			}
		}
		else {
			return identifier;
		}
	}
	
	static String indent(final StringBuilder sb, final AbstractDocument orgDoc, final int offset,
			final ISourceUnit su) throws BadLocationException, CoreException {
		final IRCoreAccess coreConfig = (su instanceof IRSourceUnit) ? ((IRSourceUnit) su).getRCoreAccess() : RCore.getWorkbenchAccess();
		
		final RIndentUtil indentUtil = new RIndentUtil(orgDoc, coreConfig.getRCodeStyle());
		final int column = indentUtil.getColumnAtOffset(offset);
		final String initial = indentUtil.createIndentString(column);
		final String prefix = initial+"1\n"; //$NON-NLS-1$
		sb.insert(0, prefix);
		String text = sb.toString();
		final Document doc = new Document(text);
		final SourceParseInput parseInput = new StringParseInput(text);
		text = null;
		
		final RAstInfo ast = new RAstInfo(RAst.LEVEL_MINIMAL, 0);
		final RScanner scanner = new RScanner(parseInput, ast);
		ast.root = scanner.scanSourceUnit();
		
		final RSourceIndenter indenter = new RSourceIndenter(coreConfig);
		final TextEdit edits = indenter.getIndentEdits(doc, ast, 0, 1, doc.getNumberOfLines()-1);
		edits.apply(doc, 0);
		return doc.get(prefix.length(), doc.getLength()-prefix.length());
	}
	
	/**
	 * Prepare the insertion of a command (text) before another command (offset)
	 * 
	 * The method prepares the insertion by modifying the text and returning the offset
	 * where to insert the modified text
	 * 
	 * @param text the command to insert, will be modified
	 * @param orgDoc the document
	 * @param offset the offset where to insert the command
	 * @param su the source unit, if available
	 * @return the offset to insert the modified text
	 * @throws BadLocationException
	 * @throws CoreException
	 */
	static int prepareInsertBefore(final StringBuilder text, final AbstractDocument orgDoc, final int offset,
			final ISourceUnit su) throws BadLocationException, CoreException {
		final IRCoreAccess coreConfig = (su instanceof IRSourceUnit) ? ((IRSourceUnit) su).getRCoreAccess() : RCore.getWorkbenchAccess();
		
		final RIndentUtil indentUtil = new RIndentUtil(orgDoc, coreConfig.getRCodeStyle());
		final int line = orgDoc.getLineOfOffset(offset);
		final int[] lineIndent = indentUtil.getLineIndent(line, false);
		if (lineIndent[IndentUtil.OFFSET_IDX] == offset) { // first char/command in line
			text.insert(0, indentUtil.createIndentString(lineIndent[IndentUtil.COLUMN_IDX]));
			text.append(orgDoc.getDefaultLineDelimiter());
			return orgDoc.getLineOffset(line);
		}
		else {
			text.append("; "); //$NON-NLS-1$
			return offset;
		}
	}
	
	static RCodeStyleSettings getCodeStyle(final ISourceUnit su) {
		if (su instanceof IRSourceUnit) {
			return ((IRSourceUnit) su).getRCoreAccess().getRCodeStyle();
		}
		return RCore.getWorkbenchAccess().getRCodeStyle();
	}
	
}
