/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ui.IElementLabelProvider;
import de.walware.ecommons.ltk.ui.sourceediting.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.ElementNameCompletionProposal;
import de.walware.ecommons.text.ui.BracketLevel;

import de.walware.statet.nico.ui.console.InputSourceViewer;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.rsource.RHeuristicTokenScanner;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.editors.RBracketLevel;


public class RElementCompletionProposal extends ElementNameCompletionProposal {
	
	
	public static class ArgumentProposal extends RElementCompletionProposal {
		
		
		public ArgumentProposal(final AssistInvocationContext context, 
				final IElementName replacementName, final int replacementOffset,
				final int relevance, final IElementLabelProvider labelProvider) {
			super(context, replacementName, replacementOffset, null, relevance+100, labelProvider);
		}
		
		
		@Override
		public Image getImage() {
			return RUI.getImage(RUI.IMG_OBJ_ARGUMENT_ASSIGN);
		}
		
		@Override
		public String getDisplayString() {
			return fReplacementName.getDisplayName();
		}
		
		@Override
		public StyledString getStyledDisplayString() {
			return new StyledString(fReplacementName.getDisplayName());
		}
		
		@Override
		protected boolean isArgumentName() {
			return true;
		}
		
	}
	
	public static class ContextInformationProposal extends RElementCompletionProposal {
		
		
		public ContextInformationProposal(final AssistInvocationContext context,
				final IElementName elementName, final int replacementOffset,
				final IModelElement element, final int relevance,
				final IElementLabelProvider labelProvider) {
			super(context, elementName, replacementOffset, element, relevance, labelProvider);
		}
		
		
		@Override
		public boolean validate(final IDocument document, final int offset, final DocumentEvent event) {
			return (offset == fContext.getInvocationOffset());
		}
		
		@Override
		public boolean isAutoInsertable() {
			return true;
		}
		
		@Override
		protected void doApply(final char trigger, final int stateMask, final int caretOffset, final int replacementOffset, final int replacementLength) throws BadLocationException {
			final ApplyData data = getApplyData();
			setCursorPosition(-1);
			data.setContextInformation(new RArgumentListContextInformation(getReplacementOffset(), (IRMethod) fElement));
		}
		
	}
	
	
	static final class ApplyData {
		
		private final AssistInvocationContext fContext;
		private final SourceViewer fViewer;
		private final IDocument fDocument;
		private RHeuristicTokenScanner fScanner;
		
		private IContextInformation fContextInformation;
		
		ApplyData(final AssistInvocationContext context) {
			fContext = context;
			fViewer = context.getSourceViewer();
			fDocument = fViewer.getDocument();
		}
		
		public SourceViewer getViewer() {
			return fViewer;
		}
		
		public IDocument getDocument() {
			return fDocument;
		}
		
		public RHeuristicTokenScanner getScanner() {
			if (fScanner == null) {
				fScanner = (RHeuristicTokenScanner) fContext.getEditor().getAdapter(RHeuristicTokenScanner.class);
			}
			return fScanner;
		}
		
		public void setContextInformation(final IContextInformation info) {
			fContextInformation = info;
		}
		
		public IContextInformation getContextInformation() {
			return fContextInformation;
		}
		
	}
	
	
	private ApplyData fApplyData;
	
	
	public RElementCompletionProposal(final AssistInvocationContext context, final IElementName elementName, 
			final int replacementOffset, final IModelElement element,
			final int relevance, final IElementLabelProvider labelProvider) {
		super(context, elementName, replacementOffset, element, 80+relevance, labelProvider);
	}
	
	
	@Override
	protected String getPluginId() {
		return RUI.PLUGIN_ID;
	}
	
	protected final ApplyData getApplyData() {
		if (fApplyData == null) {
			fApplyData = new ApplyData(fContext);
		}
		return fApplyData;
	}
	
	
	@Override
	protected int computeReplacementLength(final int replacementOffset, final Point selection, final int caretOffset, final boolean overwrite) {
		// keep in synch with RSimpleCompletionProposal
		final int end = Math.max(caretOffset, selection.x + selection.y);
		if (overwrite) {
			final ApplyData data = getApplyData();
			final RHeuristicTokenScanner scanner = data.getScanner();
			scanner.configure(data.getDocument());
			final IRegion word = scanner.findRWord(end, false, true);
			if (word != null) {
				return (word.getOffset() + word.getLength() - replacementOffset);
			}
		}
		return (end - replacementOffset);
	}
	
	@Override
	public boolean validate(final IDocument document, final int offset, final DocumentEvent event) {
		// keep in synch with RSimpleCompletionProposal
		try {
			int start = getReplacementOffset();
			int length = offset - getReplacementOffset();
			if (length > 0 && document.getChar(start) == '`') {
				start++;
				length--;
			}
			if (length > 0 && document.getChar(start+length-1) == '`') {
				length--;
			}
			final String prefix = document.get(start, length);
			final String replacement = fReplacementName.getSegmentName();
			if (new RElementsCompletionComputer.PrefixPattern(prefix).matches(replacement)) {
				return true;
			}
		}
		catch (final BadLocationException e) {
			// ignore concurrently modified document
		}
		return false;
	}
	
	@Override
	protected void doApply(final char trigger, final int stateMask, final int caretOffset, final int replacementOffset, int replacementLength) throws BadLocationException {
		final ApplyData data = getApplyData();
		final IDocument document = data.getDocument();
		
		final boolean assignmentFunction = isFunction()
				&& fReplacementName.getNextSegment() == null && fReplacementName.getSegmentName().endsWith("<-");
		final IElementName elementName;
		if (assignmentFunction) {
			elementName = RElementName.create(RElementName.MAIN_DEFAULT, fReplacementName.getSegmentName().substring(0, fReplacementName.getSegmentName().length()-2));
		}
		else {
			elementName = fReplacementName;
		}
		final StringBuilder replacement = new StringBuilder(elementName.getDisplayName());
		int cursor = replacement.length();
		if (replacementLength > 0 && document.getChar(replacementOffset) == '`' && replacement.charAt(0) != '`') {
			if (replacement.length() == elementName.getSegmentName().length() 
					&& replacementOffset+replacementLength < document.getLength()
					&& document.getChar(replacementOffset+replacementLength) == '`') {
				replacementLength++;
			}
			replacement.insert(elementName.getSegmentName().length(), '`');
			replacement.insert(0, '`');
			cursor += 2;
		}
		
		int fmode = 0;
		if (isArgumentName()) {
			if (!isFollowedByEqualAssign(data, replacementOffset+replacementLength)) {
				final RCodeStyleSettings codeStyle = getCodeStyleSettings();
				final String argAssign = codeStyle.getArgAssignString();
				replacement.append(argAssign);
				cursor += argAssign.length();
			}
		}
		else if (isFunction()) {
			fmode = 1;
			final IRMethod rMethod = (IRMethod) fElement;
			
			if (replacementOffset+replacementLength < document.getLength()-1
					&& document.getChar(replacementOffset+replacementLength) == '(') {
				cursor ++;
				fmode = 10;
			}
			else if (!isFollowedByOpeningBracket(data, replacementOffset+replacementLength)) {
				replacement.append('(');
				cursor ++;
				fmode = 11;
			}
			if (fmode >= 10) {
				if (fmode == 11
						&& !isClosedBracket(data, replacementOffset, replacementOffset+replacementLength)) {
					replacement.append(')');
					fmode = 101;
					
					if (assignmentFunction && !isFollowedByAssign(data, replacementOffset+replacementLength)) {
						replacement.append(" <- ");
						fmode += 4;
					}
				}
				
				final ArgsDefinition argsDef = rMethod.getArgsDefinition();
				if (argsDef == null || argsDef.size() > 0 || fmode == 11) {
					data.setContextInformation(new RArgumentListContextInformation(replacementOffset + cursor, rMethod));
				}
				else {
					cursor ++;
					fmode = 200;
				}
			}
			
		}
		
		document.replace(replacementOffset, replacementLength, replacement.toString());
		setCursorPosition(replacementOffset + cursor);
		if (fmode > 100 && fmode < 125) {
			createLinkedMode(data, replacementOffset + cursor - 1, (fmode-100)).enter();
		}
	}
	
	private LinkedModeUI createLinkedMode(final ApplyData util, int offset, final int exitAddition) throws BadLocationException {
		int pos = 0;
		offset++;
		final LinkedPositionGroup group1 = new LinkedPositionGroup();
		final LinkedPosition position = new LinkedPosition(util.getDocument(), offset, pos++);
		group1.addPosition(position);
		
		/* set up linked mode */
		final LinkedModeModel model = new LinkedModeModel();
		model.addGroup(group1);
		model.forceInstall();
		
		final BracketLevel level = RBracketLevel.createBracketLevel('(', util.getDocument(), fContext.getEditor().getPartitioning(), 
				position, (util.getViewer() instanceof InputSourceViewer));
		
		/* create UI */
		final LinkedModeUI ui = new LinkedModeUI(model, util.getViewer());
		ui.setCyclingMode(LinkedModeUI.CYCLE_NEVER);
		ui.setExitPosition(util.getViewer(), offset+exitAddition, 0, pos);
		ui.setSimpleMode(true);
		ui.setExitPolicy(level);
		return ui;
	}
	
	protected boolean isFunction() {
		return (fElement != null
				&& (fElement.getElementType() & IRElement.MASK_C1) == IRElement.C1_METHOD);
	}
	
	protected boolean isArgumentName() {
		return false;
	}
	
	private final boolean isFollowedByOpeningBracket(final ApplyData util, final int forwardOffset) {
		final RHeuristicTokenScanner scanner = util.getScanner();
		scanner.configure(util.getDocument());
		final int idx = scanner.findAnyNonBlankForward(forwardOffset, RHeuristicTokenScanner.UNBOUND, true);
		if (idx >= 0) {
			return (scanner.getChar() == '(');
		}
		return false;
	}
	
	private final boolean isClosedBracket(final ApplyData util, final int backwardOffset, final int forwardOffset) {
		final int searchType = 1;
		int[] balance = new int[3];
		balance[searchType]++;
		final RHeuristicTokenScanner scanner = util.getScanner();
		scanner.configureDefaultParitions(util.getDocument());
		balance = scanner.computeBracketBalance(backwardOffset, forwardOffset, balance, searchType);
		return (balance[searchType] <= 0);
	}
	
	private final boolean isFollowedByEqualAssign(final ApplyData util, final int forwardOffset) {
		final RHeuristicTokenScanner scanner = util.getScanner();
		scanner.configure(util.getDocument());
		final int idx = scanner.findAnyNonBlankForward(forwardOffset, RHeuristicTokenScanner.UNBOUND, true);
		if (idx >= 0) {
			return (scanner.getChar() == '=');
		}
		return false;
	}
	
	private final boolean isFollowedByAssign(final ApplyData util, final int forwardOffset) {
		final RHeuristicTokenScanner scanner = util.getScanner();
		scanner.configure(util.getDocument());
		final int idx = scanner.findAnyNonBlankForward(forwardOffset, RHeuristicTokenScanner.UNBOUND, true);
		if (idx >= 0) {
			return (scanner.getChar() == '=' || scanner.getChar() == '<');
		}
		return false;
	}
	
	protected RCodeStyleSettings getCodeStyleSettings() {
		final IRCoreAccess access = (IRCoreAccess) fContext.getEditor().getAdapter(IRCoreAccess.class);
		if (access != null) {
			return access.getRCodeStyle();
		}
		return RCore.getWorkbenchAccess().getRCodeStyle();
	}
	
	@Override
	public IContextInformation getContextInformation() {
		return getApplyData().getContextInformation();
	}
	
}
