/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.editors;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextEditorAction;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.ext.ui.text.ITokenScanner;
import de.walware.statet.ext.ui.text.PairMatcher;
import de.walware.statet.ui.IStatextEditorActionDefinitionIds;
import de.walware.statet.ui.StatetUiPreferenceConstants;


public abstract class StatextEditor1 extends TextEditor {

	public static final String ACTION_ID_GOTO_MATCHING_BRACKET = "de.walware.statet.ui.actions.GotoMatchingBracket";
	public static final String ACTION_ID_TOGGLE_COMMENT = "de.walware.statet.ui.actions.ToggleComment";

	
	private final class GotoMatchingBracketAction extends TextEditorAction {
		
		private char[][] fBrackets;
		
		GotoMatchingBracketAction() {
			super(EditorMessages.getCompatibilityBundle(), "GotoMatchingBracketAction_", StatextEditor1.this);
			
			fBrackets = fBracketMatcher.getPairs();
			setEnabled(true);
		}
		
		public void run() {
			gotoMatchingBracket();
		}
	
		/**
		 * Jumps to the matching bracket.
		 */
		public void gotoMatchingBracket() {
			
			ISourceViewer sourceViewer = getSourceViewer();
			IDocument document = sourceViewer.getDocument();
			if (document == null)
				return;
			
			ITextSelection selection = (ITextSelection) sourceViewer.getSelectionProvider().getSelection();
			int offset = selection.getOffset();
			int selectionLength = selection.getLength();
	
			if (selectionLength == 1) {
				try {
					char c = document.getChar(offset);
					for (int i = 0; i < fBrackets.length; i++) {
						if (c == fBrackets[i][ITokenScanner.OPENING_PEER]) {
							offset++;
							selectionLength = 0;
							break;
						}
						if (c == fBrackets[i][ITokenScanner.CLOSING_PEER]) {
							selectionLength = 0;
							break;
						}
					}
				} catch (BadLocationException e) {
				}
			}
			
			if (selectionLength > 0) {
				setStatusLineErrorMessage(EditorMessages.GotoMatchingBracketAction_error_InvalidSelection);		
				sourceViewer.getTextWidget().getDisplay().beep();
				return;
			}
	
			IRegion region = fBracketMatcher.match(document, offset);
			if (region == null) {
				setStatusLineErrorMessage(EditorMessages.GotoMatchingBracketAction_error_NoMatchingBracket);		
				sourceViewer.getTextWidget().getDisplay().beep();
				return;		
			}
			
			int matchingOffset = region.getOffset();
			int matchingLength = region.getLength();
			
			if (matchingLength < 1)
				return;
				
			int targetOffset = (fBracketMatcher.getAnchor() == PairMatcher.RIGHT) ? matchingOffset+1 : matchingOffset+matchingLength-1;
			
			boolean visible = false;
			if (sourceViewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
				visible = (extension.modelOffset2WidgetOffset(targetOffset) > -1);
			} else {
				IRegion visibleRegion = sourceViewer.getVisibleRegion();
				visible = (targetOffset >= visibleRegion.getOffset() && targetOffset <= visibleRegion.getOffset() + visibleRegion.getLength());
			}
			
			if (!visible) {
				setStatusLineErrorMessage(EditorMessages.GotoMatchingBracketAction_error_BracketOutsideSelectedElement);		
				sourceViewer.getTextWidget().getDisplay().beep();
				return;
			}
			
			sourceViewer.setSelectedRange(targetOffset, 0);
			sourceViewer.revealRange(targetOffset, 0);
		}
	}		
	
	private final class ToggleCommentAction extends TextEditorAction {
		
		/** The text operation target */
		private ITextOperationTarget fOperationTarget;
		/** The document partitioning */
		private String fDocumentPartitioning;
		/** The comment prefixes */
		private Map<String, String[]> fPrefixesMap;

		ToggleCommentAction() {
			
			super(EditorMessages.getCompatibilityBundle(), "ToggleCommentAction_", StatextEditor1.this);
			
			configure();
		}

		public void run() {

			ISourceViewer sourceViewer = getSourceViewer();
			
			if (!validateEditorInputState())
				return;
			
			final int operationCode = (isSelectionCommented(getSelectionProvider().getSelection())) ?
				ITextOperationTarget.STRIP_PREFIX : ITextOperationTarget.PREFIX;
			
			Shell shell = getSite().getShell();
			if (!fOperationTarget.canDoOperation(operationCode)) {
				setStatusLineErrorMessage(EditorMessages.ToggleCommentAction_error);
				sourceViewer.getTextWidget().getDisplay().beep();
				return;
			}
			
			Display display = null;
			if (shell != null && !shell.isDisposed())
				display = shell.getDisplay();

			BusyIndicator.showWhile(display, new Runnable() {
				public void run() {
					fOperationTarget.doOperation(operationCode);
				}
			});
		}

		/**
		 * Implementation of the <code>IUpdate</code> prototype method discovers
		 * the operation through the current editor's
		 * <code>ITextOperationTarget</code> adapter, and sets the enabled state
		 * accordingly.
		 */
		public void update() {
			super.update();

			if (!canModifyEditor()) {
				setEnabled(false);
				return;
			}

			ITextEditor editor = getTextEditor();
			if (fOperationTarget == null && editor != null)
				fOperationTarget = (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);

			setEnabled(fOperationTarget != null 
					&& fOperationTarget.canDoOperation(ITextOperationTarget.PREFIX) 
					&& fOperationTarget.canDoOperation(ITextOperationTarget.STRIP_PREFIX) );
		}
		
		private void configure() {
			SourceViewerConfiguration configuration = getSourceViewerConfiguration();
			ISourceViewer sourceViewer = getSourceViewer();
			
			String[] types = configuration.getConfiguredContentTypes(sourceViewer);
			fPrefixesMap = new HashMap<String, String[]>(types.length);
			for (int i= 0; i < types.length; i++) {
				String type = types[i];
				String[] prefixes = configuration.getDefaultPrefixes(sourceViewer, type);
				if (prefixes != null && prefixes.length > 0) {
					int emptyPrefixes = 0;
					for (int j= 0; j < prefixes.length; j++)
						if (prefixes[j].length() == 0)
							emptyPrefixes++;

					if (emptyPrefixes > 0) {
						String[] nonemptyPrefixes = new String[prefixes.length - emptyPrefixes];
						for (int j = 0, k = 0; j < prefixes.length; j++) {
							String prefix= prefixes[j];
							if (prefix.length() != 0) {
								nonemptyPrefixes[k]= prefix;
								k++;
							}
						}
						prefixes = nonemptyPrefixes;
					}

					fPrefixesMap.put(type, prefixes);
				}
			}

			fDocumentPartitioning = configuration.getConfiguredDocumentPartitioning(sourceViewer);
		}
		
		/**
		 * Is the given selection single-line commented?
		 *
		 * @param selection Selection to check
		 * @return <code>true</code> iff all selected lines are commented
		 */
		private boolean isSelectionCommented(ISelection selection) {
			if (!(selection instanceof ITextSelection))
				return false;

			ITextSelection textSelection = (ITextSelection) selection;
			if (textSelection.getStartLine() < 0 || textSelection.getEndLine() < 0)
				return false;

			IDocument document = getDocumentProvider().getDocument(getEditorInput());

			try {

				IRegion block = getTextBlockFromSelection(textSelection, document);
				ITypedRegion[] regions = TextUtilities.computePartitioning(document, fDocumentPartitioning, block.getOffset(), block.getLength(), false);

				int lineCount = 0;
				int[] lines = new int[regions.length * 2]; // [startline, endline, startline, endline, ...]
				for (int i = 0, j = 0; i < regions.length; i++, j+= 2) {
					// start line of region
					lines[j] = getFirstCompleteLineOfRegion(regions[i], document);
					// end line of region
					int length = regions[i].getLength();
					int offset = regions[i].getOffset() + length;
					if (length > 0)
						offset--;
					lines[j + 1]= (lines[j] == -1 ? -1 : document.getLineOfOffset(offset));
					lineCount += lines[j + 1] - lines[j] + 1;
				}

				// Perform the check
				for (int i = 0, j = 0; i < regions.length; i++, j+=2) {
					String[] prefixes = (String[]) fPrefixesMap.get(regions[i].getType());
					if (prefixes != null && prefixes.length > 0 && lines[j] >= 0 && lines[j + 1] >= 0)
						if (!isBlockCommented(lines[j], lines[j + 1], prefixes, document))
							return false;
				}
				return true;

			} catch (BadLocationException x) {
				StatetPlugin.logUnexpectedError(x);		// should not happen
			}
			return false;
		}

		/**
		 * Determines whether each line is prefixed by one of the prefixes.
		 *
		 * @param startLine Start line in document
		 * @param endLine End line in document
		 * @param prefixes Possible comment prefixes
		 * @param document The document
		 * @return <code>true</code> iff each line from <code>startLine</code>
		 *             to and including <code>endLine</code> is prepended by one
		 *             of the <code>prefixes</code>, ignoring whitespace at the
		 *             begin of line
		 */
		private boolean isBlockCommented(int startLine, int endLine, String[] prefixes, IDocument document) {

			try {
				// check for occurrences of prefixes in the given lines
				for (int i = startLine; i <= endLine; i++) {

					IRegion line = document.getLineInformation(i);
					String text = document.get(line.getOffset(), line.getLength());

					int[] found = TextUtilities.indexOf(prefixes, text, 0);

					if (found[0] == -1)
						// found a line which is not commented
						return false;

					String s = document.get(line.getOffset(), found[0]);
					s = s.trim();
					if (s.length() != 0)
						// found a line which is not commented
						return false;
				}
				return true;

			} catch (BadLocationException x) {
				StatetPlugin.logUnexpectedError(x);		// should not happen
			}
			return false;
		}
		
	}
	
	/** The editor's bracket matcher */
	protected PairMatcher fBracketMatcher = null;

	
	public StatextEditor1() {
		super();
	}
	
	/*
	 * Method declared on AbstractTextEditor
	 */
	protected void initializeEditor() {
		
		super.initializeEditor();
		setCompatibilityMode(false);
	}
	
	/**
	 * Initialize the extensions of StatextEditor
	 * 
	 * @param bracketMatcher a PairMatcher finding the matching brackets, <code>null</code> is allowed. 
	 */
	protected void initStatext(PairMatcher bracketMatcher) {
		
		fBracketMatcher = bracketMatcher;
	}
	
	protected void initializeKeyBindingScopes() {
		
		setKeyBindingScopes(new String[] { "de.walware.statet.ui.contexts.TextEditorScope" });
	}

	public void dispose() {
		
		if (fBracketMatcher != null) {
			fBracketMatcher.dispose();
			fBracketMatcher= null;
		}

		super.dispose();
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.ExtendedTextEditor#configureSourceViewerDecorationSupport(org.eclipse.ui.texteditor.SourceViewerDecorationSupport)
	 */
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		
		if (fBracketMatcher != null) {
			support.setCharacterPairMatcher(fBracketMatcher);
			support.setMatchingCharacterPainterPreferenceKeys(
					StatetUiPreferenceConstants.EDITOR_MATCHING_BRACKETS, 
					StatetUiPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR);
		}

		super.configureSourceViewerDecorationSupport(support);
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextEditor#createActions()
	 */
	protected void createActions() {
		
		super.createActions();
		Action action;
		
		if (fBracketMatcher != null) {
			action = new GotoMatchingBracketAction();
			action.setActionDefinitionId(IStatextEditorActionDefinitionIds.GOTO_MATCHING_BRACKET);				
			setAction(ACTION_ID_GOTO_MATCHING_BRACKET, action);
		}

		action = new ToggleCommentAction();
		action.setActionDefinitionId(IStatextEditorActionDefinitionIds.TOGGLE_COMMENT);		
		setAction(ACTION_ID_TOGGLE_COMMENT, action); //$NON-NLS-1$
		markAsStateDependentAction(ACTION_ID_TOGGLE_COMMENT, true); //$NON-NLS-1$
		//WorkbenchHelp.setHelp(action, IJavaHelpContextIds.TOGGLE_COMMENT_ACTION);
	}

	/**
	 * Sets the given message as error message to this editor's status line.
	 *
	 * @param msg message to be set
	 */
	protected void setStatusLineErrorMessage(String msg) {
		
		IEditorStatusLine statusLine = (IEditorStatusLine) getAdapter(IEditorStatusLine.class);
		if (statusLine != null)
			statusLine.setMessage(true, msg, null);
	}

	@Override
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		
		return ((StatextSourceViewerConfiguration) getSourceViewerConfiguration()).affectsTextPresentation(event) || super.affectsTextPresentation(event);
	}
	
	@Override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		
		StatextSourceViewerConfiguration viewerConfiguration = (StatextSourceViewerConfiguration) getSourceViewerConfiguration();
		viewerConfiguration.handlePropertyChangeEvent(event);
		
		super.handlePreferenceStoreChanged(event);
	}


	/**
	 * Creates a region describing the text block (something that starts at
	 * the beginning of a line) completely containing the current selection.
	 *
	 * @param selection The selection to use
	 * @param document The document
	 * @return the region describing the text block comprising the given selection
	 */
	protected static IRegion getTextBlockFromSelection(ITextSelection selection, IDocument document) {

		try {
			IRegion line = document.getLineInformationOfOffset(selection.getOffset());
			int length = (selection.getLength() == 0) ? 
					line.getLength() : selection.getLength() + (selection.getOffset() - line.getOffset());
			return new Region(line.getOffset(), length);

		} catch (BadLocationException x) {
			StatetPlugin.logUnexpectedError(x);					// should not happen
		}
		return null;
	}

	/**
	 * Returns the index of the first line whose start offset is in the given text range.
	 *
	 * @param region the text range in characters where to find the line
	 * @param document The document
	 * @return the first line whose start index is in the given range, -1 if there is no such line
	 */
	protected static int getFirstCompleteLineOfRegion(IRegion region, IDocument document) {

		try {
			int startLine = document.getLineOfOffset(region.getOffset());

			int offset = document.getLineOffset(startLine);
			if (offset >= region.getOffset())
				return startLine;

			offset = document.getLineOffset(startLine + 1);
			return (offset > region.getOffset() + region.getLength() ? -1 : startLine + 1);

		} catch (BadLocationException x) {
			StatetPlugin.logUnexpectedError(x);				// should not happen
		}
		return -1;
	}

}
