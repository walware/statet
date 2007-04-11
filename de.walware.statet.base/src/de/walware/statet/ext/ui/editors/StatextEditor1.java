/*******************************************************************************
 * Copyright (c) 2005-2007 StatET-Project (www.walware.de/goto/statet).
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextEditorAction;

import de.walware.statet.base.StatetPlugin;
import de.walware.statet.ext.core.StatextProject;
import de.walware.statet.ext.ui.text.PairMatcher;
import de.walware.statet.ui.IStatextEditorActionDefinitionIds;
import de.walware.statet.ui.StatetUiPreferenceConstants;


public abstract class StatextEditor1<ProjectT extends StatextProject> extends TextEditor {

	
	public static final String ACTION_ID_TOGGLE_COMMENT = "de.walware.statet.ui.actions.ToggleComment";


/*- Static utility methods --------------------------------------------------*/
	
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
		} 
		catch (BadLocationException x) {
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
			StatetPlugin.logUnexpectedError(x);	// should not happen
		}
		return -1;
	}

	/**
	 * Looks for the project nature of the editor input.
	 * @param input the editor input.
	 * @param id the id of the project nature looking for.
	 * @return the nature or <code>null</code>.
	 */
	protected static IProjectNature getProject(IEditorInput input, String id) {
		
		IProjectNature nature = null;
		if (input != null && input instanceof IFileEditorInput) {
			IProject project = ((IFileEditorInput)input).getFile().getProject();
			
			try {
				if (project != null & project.hasNature(id))
					nature = project.getNature(id);
			} catch (CoreException e) {
				// pech gehabt
			}
		}
		return nature;
	}

	
/*- Inner classes -----------------------------------------------------------*/
	
	private class EditorAdapter implements IEditorAdapter {
		
		public void setStatusLineErrorMessage(String message) {

			StatextEditor1.this.setStatusLineErrorMessage(message);
		}
		
		public ISourceViewer getSourceViewer() {
			
			return StatextEditor1.this.getSourceViewer();
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
			setActionDefinitionId(IStatextEditorActionDefinitionIds.TOGGLE_COMMENT);		
			
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
	

/*- Fields -----------------------------------------------------------------*/
	
	/** The editor's bracket matcher */
	private PairMatcher fBracketMatcher;
	private IEditorAdapter fEditorAdapter = new EditorAdapter();
	private ProjectT fProject;
	
	
/*- Contructors ------------------------------------------------------------*/

	public StatextEditor1() {
		super();
	}
	

/*- Methods ----------------------------------------------------------------*/

	@Override
	protected void initializeEditor() {
		
		super.initializeEditor();
		setCompatibilityMode(false);
		setupConfiguration(getProject());
	}
	
	/**
	 * Initialize the extensions of StatextEditor
	 * 
	 * @param bracketMatcher a PairMatcher finding the matching brackets, <code>null</code> is allowed. 
	 */
	protected void initStatext(PairMatcher bracketMatcher) {
		
		fBracketMatcher = bracketMatcher;
	}
	
	@Override
	protected void initializeKeyBindingScopes() {
		
		setKeyBindingScopes(new String[] { "de.walware.statet.ui.contexts.TextEditorScope" });
	}
	
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		
		ProjectT prevProject = fProject;
		fProject = (input != null) ? getProject(input) : null;
		
		if (input != null && prevProject != fProject) {
			// project has changed
			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer != null) {
				((ISourceViewerExtension2) sourceViewer).unconfigure();
			}
			
			disposeConfiguration(prevProject);
			setupConfiguration(fProject);

			if (sourceViewer != null) {
				sourceViewer.configure(getSourceViewerConfiguration());
			}
		}

		super.doSetInput(input);
	}
	
	/**
	 * Subclasses should setup the SourceViewerConfiguration.
	 */
	protected void setupConfiguration(ProjectT project) {
		
	}
	
	protected void disposeConfiguration(ProjectT previousProject) {
		
	}
	
	/**
	 * Subclasses should implement this method, if it want to use project features.
	 * 
	 * @param input a editor input.
	 * @return the project, the input is associated to, or <code>null</code>.
	 */
	protected ProjectT getProject(IEditorInput input) {
		
		return null;
	}
	
	/**
	 * @return the project of current input, or <code>null</code>, if none/not supported.
	 */
	protected final ProjectT getProject() {
		
		return fProject;
	}
		

	@Override
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		
		if (fBracketMatcher != null) {
			support.setCharacterPairMatcher(fBracketMatcher);
			support.setMatchingCharacterPainterPreferenceKeys(
					StatetUiPreferenceConstants.EDITOR_MATCHING_BRACKETS, 
					StatetUiPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR);
		}

		super.configureSourceViewerDecorationSupport(support);
	}

	@Override
	protected void createActions() {
		
		super.createActions();
		Action action;
		
		if (fBracketMatcher != null) {
			action = new GotoMatchingBracketAction(fBracketMatcher, fEditorAdapter);
			setAction(GotoMatchingBracketAction.ACTION_ID, action);
		}

		action = new ToggleCommentAction();
		setAction(ACTION_ID_TOGGLE_COMMENT, action); //$NON-NLS-1$
		markAsStateDependentAction(ACTION_ID_TOGGLE_COMMENT, true); //$NON-NLS-1$
		
		//WorkbenchHelp.setHelp(action, IJavaHelpContextIds.TOGGLE_COMMENT_ACTION);
	}


	@Override
	public Object getAdapter(Class adapter) {
		
		if (IEditorAdapter.class.equals(adapter)) {
			return fEditorAdapter;
		}
		return super.getAdapter(adapter);
	}
	
	
	@Override
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		
		StatextSourceViewerConfiguration viewerConfiguration = (StatextSourceViewerConfiguration) getSourceViewerConfiguration();
		if (viewerConfiguration != null && viewerConfiguration.affectsTextPresentation(event)) {
			return true;
		}
		return super.affectsTextPresentation(event);
	}
	
	@Override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		
		StatextSourceViewerConfiguration viewerConfiguration = (StatextSourceViewerConfiguration) getSourceViewerConfiguration();
		if (viewerConfiguration != null) {
			viewerConfiguration.handlePropertyChangeEvent(event);
		}
		super.handlePreferenceStoreChanged(event);
	}

	@Override
	public void dispose() {
		
		if (fBracketMatcher != null) {
			fBracketMatcher.dispose();
			fBracketMatcher= null;
		}
		disposeConfiguration(getProject());

		super.dispose();
	}

}
