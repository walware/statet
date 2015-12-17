/*=============================================================================#
 # Copyright (c) 2008-2015 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import de.walware.ecommons.ltk.IElementName;
import de.walware.ecommons.ltk.ui.IElementLabelProvider;
import de.walware.ecommons.ltk.ui.sourceediting.assist.AssistInvocationContext;
import de.walware.ecommons.ltk.ui.sourceediting.assist.ElementNameCompletionProposal;
import de.walware.ecommons.ltk.ui.sourceediting.assist.IInfoHover;
import de.walware.ecommons.text.ui.BracketLevel.InBracketPosition;

import de.walware.statet.nico.ui.console.InputSourceViewer;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCodeStyleSettings;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RSymbolComparator;
import de.walware.statet.r.core.model.ArgsDefinition;
import de.walware.statet.r.core.model.IRElement;
import de.walware.statet.r.core.model.IRMethod;
import de.walware.statet.r.core.model.RElementName;
import de.walware.statet.r.core.pkgmanager.IRPkgInfo;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.rhelp.IREnvHelp;
import de.walware.statet.r.core.rhelp.IRHelpManager;
import de.walware.statet.r.core.source.RHeuristicTokenScanner;
import de.walware.statet.r.internal.ui.rhelp.RHelpInfoHoverCreator;
import de.walware.statet.r.internal.ui.rhelp.RHelpUIServlet;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.sourceediting.RAssistInvocationContext;
import de.walware.statet.r.ui.sourceediting.RBracketLevel;


public class RElementCompletionProposal extends ElementNameCompletionProposal<IRElement>
		implements ICompletionProposalExtension5 {
	
	
	private static final int PACKAGE_NAME= 1;
	private static final int ARGUMENT_NAME= 2;
	private static final int FUNCTION= 3;
	
	
	public static class ArgumentProposal extends RElementCompletionProposal {
		
		
		public ArgumentProposal(final RAssistInvocationContext context, 
				final IElementName replacementName, final int replacementOffset,
				final IRElement element, final int relevance, final IElementLabelProvider labelProvider) {
			super(context, replacementName, replacementOffset, element, relevance, labelProvider);
		}
		
		
		@Override
		public Image getImage() {
			return RUI.getImage(RUI.IMG_OBJ_ARGUMENT_ASSIGN);
		}
		
		@Override
		public String getDisplayString() {
			return getReplacementName().getDisplayName();
		}
		
		@Override
		public StyledString getStyledDisplayString() {
			return new StyledString(getReplacementName().getDisplayName());
		}
		
		@Override
		protected int getMode() {
			return ARGUMENT_NAME;
		}
		
	}
	
	public static class RPkgProposal extends RElementCompletionProposal {
		
		
		private final IRPkgInfo pkgInfo;
		
		
		public RPkgProposal(final RAssistInvocationContext context, 
				final IElementName replacementName, final int replacementOffset,
				final IRPkgInfo pkgInfo, final int relevance) {
			super(context, replacementName, replacementOffset, null, relevance, null);
			
			this.pkgInfo= pkgInfo;
		}
		
		
		@Override
		public Image getImage() {
			return RUI.getImage(RUI.IMG_OBJ_R_PACKAGE);
		}
		
		@Override
		public String getDisplayString() {
			return getReplacementName().getSegmentName();
		}
		
		@Override
		public StyledString getStyledDisplayString() {
			final StyledString s= new StyledString(getReplacementName().getSegmentName());
			if (this.pkgInfo != null) {
				s.append(QUALIFIER_SEPARATOR, StyledString.QUALIFIER_STYLER);
				s.append(this.pkgInfo.getTitle(), StyledString.QUALIFIER_STYLER);
			}
			return s;
		}
		
		@Override
		protected int getMode() {
			return PACKAGE_NAME;
		}
		
	}
	
	public static class ContextInformationProposal extends RElementCompletionProposal {
		
		
		public ContextInformationProposal(final RAssistInvocationContext context,
				final IElementName elementName, final int replacementOffset,
				final IRElement element, final int relevance,
				final IElementLabelProvider labelProvider) {
			super(context, elementName, replacementOffset, element, relevance, labelProvider);
		}
		
		
		@Override
		public boolean validate(final IDocument document, final int offset, final DocumentEvent event) {
			return (offset == getInvocationContext().getInvocationOffset());
		}
		
		@Override
		public boolean isAutoInsertable() {
			return true;
		}
		
		@Override
		protected void doApply(final char trigger, final int stateMask,
				final int caretOffset, final int replacementOffset, final int replacementLength)
				throws BadLocationException {
			final ApplyData data= getApplyData();
			setCursorPosition(-1);
			data.setContextInformation(new RArgumentListContextInformation(replacementOffset,
					(IRMethod) getElement() ));
		}
		
	}
	
	
	static final class ApplyData {
		
		private final RAssistInvocationContext context;
		private final SourceViewer viewer;
		private final IDocument document;
		
		private IContextInformation contextInformation;
		
		ApplyData(final RAssistInvocationContext context) {
			this.context= context;
			this.viewer= context.getSourceViewer();
			this.document= this.viewer.getDocument();
		}
		
		public SourceViewer getViewer() {
			return this.viewer;
		}
		
		public IDocument getDocument() {
			return this.document;
		}
		
		public RHeuristicTokenScanner getScanner() {
			return this.context.getRHeuristicTokenScanner();
		}
		
		public void setContextInformation(final IContextInformation info) {
			this.contextInformation= info;
		}
		
		public IContextInformation getContextInformation() {
			return this.contextInformation;
		}
		
	}
	
	
	private static final boolean isFollowedByOpeningBracket(final ApplyData util, final int forwardOffset) {
		final RHeuristicTokenScanner scanner= util.getScanner();
		scanner.configure(util.getDocument());
		final int idx= scanner.findAnyNonBlankForward(forwardOffset, RHeuristicTokenScanner.UNBOUND, false);
		return (idx >= 0
				&& scanner.getChar() == '(' );
	}
	
	private static final boolean isClosedBracket(final ApplyData data, final int backwardOffset, final int forwardOffset) {
		final int searchType= RHeuristicTokenScanner.ROUND_BRACKET_TYPE;
		int[] balance= new int[3];
		balance[searchType]++;
		final RHeuristicTokenScanner scanner= data.getScanner();
		scanner.configureDefaultParitions(data.getDocument());
		balance= scanner.computeBracketBalance(backwardOffset, forwardOffset, balance, searchType);
		return (balance[searchType] <= 0);
	}
	
	private static final boolean isFollowedByEqualAssign(final ApplyData data, final int forwardOffset) {
		final RHeuristicTokenScanner scanner= data.getScanner();
		scanner.configure(data.getDocument());
		final int idx= scanner.findAnyNonBlankForward(forwardOffset, RHeuristicTokenScanner.UNBOUND, false);
		return (idx >= 0
				&& scanner.getChar() == '=' );
	}
	
	private static final boolean isFollowedByAssign(final ApplyData util, final int forwardOffset) {
		final RHeuristicTokenScanner scanner= util.getScanner();
		scanner.configure(util.getDocument());
		final int idx= scanner.findAnyNonBlankForward(forwardOffset, RHeuristicTokenScanner.UNBOUND, false);
		return (idx >= 0
				&& (scanner.getChar() == '=' || scanner.getChar() == '<') );
	}
	
	
	private ApplyData applyData;
	
	private IInformationControlCreator informationControlCreator;
	
	
	public RElementCompletionProposal(final RAssistInvocationContext context, final IElementName elementName, 
			final int replacementOffset, final IRElement element,
			final int relevance, final IElementLabelProvider labelProvider) {
		super(context, elementName, replacementOffset, element, relevance, labelProvider);
	}
	
	
	@Override
	protected String getPluginId() {
		return RUI.PLUGIN_ID;
	}
	
	@Override
	public RAssistInvocationContext getInvocationContext() {
		return (RAssistInvocationContext) super.getInvocationContext();
	}
	
	protected final ApplyData getApplyData() {
		if (this.applyData == null) {
			this.applyData= new ApplyData(getInvocationContext());
		}
		return this.applyData;
	}
	
	protected IRCoreAccess getRCoreAccess() {
		return getInvocationContext().getEditor().getRCoreAccess();
	}
	
	
	@Override
	protected int computeReplacementLength(final int replacementOffset, final Point selection, final int caretOffset, final boolean overwrite) {
		// keep in synch with RSimpleCompletionProposal
		final int end= Math.max(caretOffset, selection.x + selection.y);
		if (overwrite) {
			final ApplyData data= getApplyData();
			final RHeuristicTokenScanner scanner= data.getScanner();
			scanner.configure(data.getDocument());
			final IRegion word= scanner.findRWord(end, false, true);
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
			int start= getReplacementOffset();
			int length= offset - start;
			if (length > 0 && document.getChar(start) == '`') {
				start++;
				length--;
			}
			if (length > 0 && document.getChar(start+length-1) == '`') {
				length--;
			}
			final String prefix= document.get(start, length);
			final String replacement= getReplacementName().getSegmentName();
			if (new RSymbolComparator.PrefixPattern(prefix).matches(replacement)) {
				return true;
			}
		}
		catch (final BadLocationException e) {
			// ignore concurrently modified document
		}
		return false;
	}
	
	@Override
	protected void doApply(final char trigger, final int stateMask, final int caretOffset,
			final int replacementOffset, int replacementLength) throws BadLocationException {
		final ApplyData data= getApplyData();
		final IDocument document= data.getDocument();
		
		final IElementName replacementName= getReplacementName();
		final int mode= getMode();
		final boolean assignmentFunction= (mode == FUNCTION)
				&& replacementName.getNextSegment() == null
				&& replacementName.getSegmentName().endsWith("<-"); //$NON-NLS-1$
		final IElementName elementName;
		if (assignmentFunction) {
			elementName= RElementName.create(RElementName.MAIN_DEFAULT,
					replacementName.getSegmentName().substring(0, replacementName.getSegmentName().length()-2) );
		}
		else {
			elementName= replacementName;
		}
		final StringBuilder replacement= new StringBuilder((mode == PACKAGE_NAME) ?
				elementName.getSegmentName() :
				elementName.getDisplayName() );
		int cursor= replacement.length();
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
		
		int subMode= 0;
		int linkedMode= -1;
		switch (mode) {
		
		case FUNCTION:
			subMode= 1;
			final IRMethod rMethod= (IRMethod) getElement();
			
			if (replacementOffset+replacementLength < document.getLength()-1
					&& document.getChar(replacementOffset+replacementLength) == '(') {
				cursor ++;
				subMode= 10;
			}
			else if (!isFollowedByOpeningBracket(data, replacementOffset+replacementLength)) {
				replacement.append('(');
				cursor ++;
				subMode= 11;
			}
			if (subMode >= 10) {
				if (subMode == 11
						&& !isClosedBracket(data, replacementOffset, replacementOffset+replacementLength)) {
					replacement.append(')');
					linkedMode= 2;
					
					if (assignmentFunction && !isFollowedByAssign(data, replacementOffset+replacementLength)) {
						replacement.append(" <- "); //$NON-NLS-1$
						if (linkedMode >= 0) {
							linkedMode += 4;
						}
					}
				}
				
				final ArgsDefinition argsDef= rMethod.getArgsDefinition();
				if (argsDef == null || argsDef.size() > 0 || (subMode == 11 && linkedMode < 0)) {
					data.setContextInformation(new RArgumentListContextInformation(replacementOffset + cursor, rMethod));
				}
				else {
					cursor ++;
					linkedMode= -1;
				}
			}
			break;
		
		case ARGUMENT_NAME:
			if (!isFollowedByEqualAssign(data, replacementOffset+replacementLength)) {
				final RCodeStyleSettings codeStyle= getRCoreAccess().getRCodeStyle();
				final String argAssign= codeStyle.getArgAssignString();
				replacement.append(argAssign);
				cursor += argAssign.length();
			}
			break;
		
		}
		
		document.replace(replacementOffset, replacementLength, replacement.toString());
		setCursorPosition(replacementOffset + cursor);
		if (linkedMode >= 0) {
			createLinkedMode(data, replacementOffset + cursor - 1, linkedMode).enter();
		}
	}
	
	private LinkedModeUI createLinkedMode(final ApplyData util, final int offset, final int mode)
			throws BadLocationException {
		final AssistInvocationContext context= getInvocationContext();
		
		final LinkedModeModel model= new LinkedModeModel();
		int pos= 0;
		
		final LinkedPositionGroup group= new LinkedPositionGroup();
		final InBracketPosition position= RBracketLevel.createPosition('(', util.getDocument(),
				offset + 1, 0, pos++);
		group.addPosition(position);
		model.addGroup(group);
		
		model.forceInstall();
		
		final RBracketLevel level= new RBracketLevel(model,
				util.getDocument(), context.getEditor().getDocumentContentInfo(),
				position, (util.getViewer() instanceof InputSourceViewer), true);
		
		/* create UI */
		final LinkedModeUI ui= new LinkedModeUI(model, util.getViewer());
		ui.setCyclingMode(LinkedModeUI.CYCLE_NEVER);
		ui.setExitPosition(util.getViewer(), offset + (mode & 0xff), 0, pos);
		ui.setSimpleMode(true);
		ui.setExitPolicy(level);
		return ui;
	}
	
	protected int getMode() {
		return (getElement() != null
						&& (getElement().getElementType() & IRElement.MASK_C1) == IRElement.C1_METHOD) ?
				FUNCTION : 0;
	}
	
	
	@Override
	public IContextInformation getContextInformation() {
		return getApplyData().getContextInformation();
	}
	
	
	@Override
	public IInformationControlCreator getInformationControlCreator() {
		final Shell shell= getInvocationContext().getSourceViewer().getTextWidget().getShell();
		if (shell == null || !RHelpInfoHoverCreator.isAvailable(shell)) {
			return null;
		}
		
		if (this.informationControlCreator == null) {
			this.informationControlCreator= new RHelpInfoHoverCreator(IInfoHover.MODE_PROPOSAL_INFO);
		}
		return this.informationControlCreator;
	}
	
	@Override
	public Object getAdditionalProposalInfo(final IProgressMonitor monitor) {
		final IRHelpManager rHelpManager= RCore.getRHelpManager();
		final int mode= getMode();
		
		Object helpObject= null;
		
		switch (mode) {
		
		case PACKAGE_NAME: {
				final IElementName elementName= getReplacementName();
				if (elementName.getType() == RElementName.SCOPE_PACKAGE) {
					final String pkgName= elementName.getSegmentName();
					
					if (pkgName == null) {
						return null;
					}
					
					final IREnvHelp help= rHelpManager.getHelp(getRCoreAccess().getREnv());
					if (help != null) {
						try {
							helpObject= help.getPkgHelp(pkgName);
						}
						finally {
							help.unlock();
						}
					}
				}
				break;
			}
		
		default: {
				final IRElement element= getElement();
				if (element == null) {
					return null;
				}
				
				final RElementName elementName= element.getElementName();
				if (elementName.getType() == RElementName.MAIN_DEFAULT) {
					RElementName scope= elementName.getScope();
					if (scope == null && (element.getModelParent() instanceof IRElement)) {
						scope= element.getModelParent().getElementName();
					}
					if (scope == null || !RElementName.isPackageFacetScopeType(scope.getType())) {
						return null;
					}
					final IREnv rEnv= getRCoreAccess().getREnv();
					final String pkgName= scope.getSegmentName();
					final String topic= elementName.getSegmentName();
					
					if (rEnv == null || pkgName == null || topic == null) {
						return null;
					}
					
					final IREnvHelp help= rHelpManager.getHelp(rEnv);
					if (help != null) {
						try {
							helpObject= help.getPageForTopic(pkgName, topic);
						}
						finally {
							help.unlock();
						}
					}
				}
				break;
			}
		}
		
		if (Thread.interrupted() || helpObject == null) {
			return null;
		}
		{	final String httpUrl= rHelpManager.toHttpUrl(helpObject, RHelpUIServlet.INFO_TARGET);
			if (httpUrl != null) {
				return new RHelpInfoHoverCreator.Data(getInvocationContext().getSourceViewer().getTextWidget(),
						helpObject, httpUrl );
			}
		}
		
		return null;
	}
	
}
