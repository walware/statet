/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.ext.ui.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.IProjectionListener;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import de.walware.eclipsecommons.preferences.SettingsChangeNotifier;
import de.walware.eclipsecommons.ui.util.UIAccess;

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.base.internal.ui.StatetUIPlugin;
import de.walware.statet.base.ui.IStatetUICommandIds;
import de.walware.statet.ext.core.StatextProject;
import de.walware.statet.ext.ui.text.PairMatcher;


public abstract class StatextEditor1<ProjectT extends StatextProject, OutlineT extends IContentOutlinePage> extends TextEditor
		implements SettingsChangeNotifier.ChangeListener {

	
	public static final String ACTION_ID_TOGGLE_COMMENT = "de.walware.statet.ui.actions.ToggleComment"; //$NON-NLS-1$


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
			StatetUIPlugin.logUnexpectedError(x);					// should not happen
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
			StatetUIPlugin.logUnexpectedError(x);	// should not happen
		}
		return -1;
	}

	
/*- Inner classes -----------------------------------------------------------*/
	
	private class EditorAdapter implements IEditorAdapter {
		
		public void setStatusLineErrorMessage(String message) {
			StatextEditor1.this.setStatusLineErrorMessage(message);
		}
		
		public SourceViewer getSourceViewer() {
			return (SourceViewer) StatextEditor1.this.getSourceViewer();
		}
		
		public boolean isEditable(boolean validate) {
			if (validate) {
				return StatextEditor1.this.validateEditorInputState();
			}
			return StatextEditor1.this.isEditorInputModifiable();
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
			super(EditorMessages.getCompatibilityBundle(), "ToggleCommentAction_", StatextEditor1.this); //$NON-NLS-1$
			setActionDefinitionId(IStatetUICommandIds.TOGGLE_COMMENT);
			
			configure();
		}

		@Override
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
		@Override
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
					String[] prefixes = fPrefixesMap.get(regions[i].getType());
					if (prefixes != null && prefixes.length > 0 && lines[j] >= 0 && lines[j + 1] >= 0)
						if (!isBlockCommented(lines[j], lines[j + 1], prefixes, document))
							return false;
				}
				return true;

			} catch (BadLocationException x) {
				StatetUIPlugin.logUnexpectedError(x);		// should not happen
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
				StatetUIPlugin.logUnexpectedError(x);		// should not happen
			}
			return false;
		}
		
	}
	

/*- Fields -----------------------------------------------------------------*/
	
	private IEditorAdapter fEditorAdapter = new EditorAdapter();
	private SourceViewerConfigurator fConfigurator;
	private String fProjectNatureId;
	private ProjectT fProject;

	/** The outline page */
	private OutlineT fOutlinePage;
	
	private boolean fEnableFoldingSupport;
	private ProjectionSupport fFoldingSupport;
	private IFoldingStructureProvider fFoldingProvider;
	private FoldingActionGroup fFoldingActionGroup ;

	
/*- Contructors ------------------------------------------------------------*/

	public StatextEditor1() {
		super();
	}
	

/*- Methods ----------------------------------------------------------------*/

	protected void initializeEditor(SourceViewerConfigurator configurator) {
		fConfigurator = configurator;
		super.initializeEditor();
		setCompatibilityMode(false);
		setPreferenceStore(fConfigurator.getPreferenceStore());
		setSourceViewerConfiguration(fConfigurator.getSourceViewerConfiguration());
		
		StatetCore.getSettingsChangeNotifier().addChangeListener(this);
	}
	
	
	protected void enableFoldingSupport() {
		fEnableFoldingSupport = true;
	}
	
	protected void configureStatetProjectNatureId(String id) {
		fProjectNatureId = id;
	}
	
	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "de.walware.statet.base.contexts.TextEditor" }); //$NON-NLS-1$
	}
	
	@Override
	protected String[] collectContextMenuPreferencePages() {
		List<String> list = new ArrayList<String>();
		collectContextMenuPreferencePages(list);
		list.addAll(Arrays.asList(super.collectContextMenuPreferencePages()));
		return list.toArray(new String[list.size()]);
	}
	
	protected void collectContextMenuPreferencePages(List<String> pageIds) {
	}
	
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		ProjectT prevProject = fProject;
		fProject = (input != null) ? getProject(input) : null;
		
		// project has changed
		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer != null) {
			fConfigurator.unconfigureTarget();
		}
		
		super.doSetInput(input);

		if (input != null) {
			setupConfiguration(prevProject, fProject, input);
			if (fFoldingProvider != null) {
				fFoldingProvider.setEditorInput(input);
			}
			if (fOutlinePage != null) {
				updateOutlinePageInput(fOutlinePage);
			}
			if (sourceViewer != null) {
				fConfigurator.configureTarget();
			}
		}
	}
	
	/**
	 * Subclasses should setup the SourceViewerConfiguration.
	 */
	protected void setupConfiguration(ProjectT prevProject, ProjectT newProject, IEditorInput newInput) {
	}
	
	/**
	 * Subclasses should implement this method, if it want to use project features.
	 * 
	 * @param input a editor input.
	 * @return the project, the input is associated to, or <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	protected ProjectT getProject(IEditorInput input) {
		if (fProjectNatureId != null) {
			if (input != null && input instanceof IFileEditorInput) {
				IProject project = ((IFileEditorInput)input).getFile().getProject();
				try {
					if (project != null & project.hasNature(fProjectNatureId)) {
						return (ProjectT) project.getNature(fProjectNatureId);
					}
				} catch (CoreException e) {
					// pech gehabt
				}
			}
		}
		return null;
	}
	
	/**
	 * @return the project of current input, or <code>null</code>, if none/not supported.
	 */
	protected final ProjectT getProject() {
		return fProject;
	}
		

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		if (fEnableFoldingSupport) {
	        ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
	        
	        fFoldingSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
			fFoldingSupport.install();
			viewer.addProjectionListener(new IProjectionListener() {
				public void projectionEnabled() {
					installFoldingSupport();
				}
				public void projectionDisabled() {
					uninstallFoldingSupport();
				}
			});
		}
	}
	
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess = getAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());

		ISourceViewer viewer = fEnableFoldingSupport ?
				new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles) :
					new SourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	protected void installFoldingSupport() {
		fFoldingProvider = createFoldingStructureProvider();
		if (fFoldingProvider != null) {
			fFoldingProvider.install(StatextEditor1.this, (ProjectionViewer) getSourceViewer());
			fFoldingProvider.setEditorInput(getEditorInput());
		}
	}
	
	protected void uninstallFoldingSupport() {
		if (fFoldingProvider != null) {
			fFoldingProvider.uninstall();
			fFoldingProvider = null;
		}
	}
	
	protected IFoldingStructureProvider createFoldingStructureProvider() {
		return null;
	}
	
	protected FoldingActionGroup createFoldingActionGroup() {
		return new FoldingActionGroup(this, (ProjectionViewer) getSourceViewer());
	}

	@Override
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);
		fConfigurator.configureSourceViewerDecorationSupport(support);
	}

	@Override
	protected void createActions() {
		super.createActions();
		Action action;
		
		PairMatcher matcher = fConfigurator.getPairMatcher();
		if (matcher != null) {
			action = new GotoMatchingBracketAction(matcher, fEditorAdapter);
			setAction(GotoMatchingBracketAction.ACTION_ID, action);
		}

		action = new ToggleCommentAction();
		setAction(ACTION_ID_TOGGLE_COMMENT, action);
		markAsStateDependentAction(ACTION_ID_TOGGLE_COMMENT, true);

		if (fEnableFoldingSupport) {
			fFoldingActionGroup = createFoldingActionGroup();
		}
		//WorkbenchHelp.setHelp(action, IJavaHelpContextIds.TOGGLE_COMMENT_ACTION);
	}

	@Override
	protected void rulerContextMenuAboutToShow(IMenuManager menu) {
		super.rulerContextMenuAboutToShow(menu);
		if (fFoldingActionGroup != null) {
			IMenuManager foldingMenu = new MenuManager(EditorMessages.FoldingMenu_label, "projection"); //$NON-NLS-1$
			menu.appendToGroup(ITextEditorActionConstants.GROUP_RULERS, foldingMenu);
			
			fFoldingActionGroup.fillMenu(foldingMenu);
		}
	}
	

	@Override
	public Object getAdapter(Class required) {
		if (IEditorAdapter.class.equals(required)) {
			return fEditorAdapter;
		}
		if (IContentOutlinePage.class.equals(required)) {
			if (fOutlinePage == null) {
				fOutlinePage = createOutlinePage();
				if (fOutlinePage != null) {
					updateOutlinePageInput(fOutlinePage);
				}
			}
			return fOutlinePage;
		}
		if (fFoldingSupport != null) {
			Object adapter = fFoldingSupport.getAdapter(getSourceViewer(), required);
			if (adapter != null)
				return adapter;
		}
		return super.getAdapter(required);
	}
	
	
	public void settingsChanged(final Set<String> contexts) {
		UIAccess.getDisplay().syncExec(new Runnable() {
			public void run() {
				handleSettingsChanged(contexts);
			}
		});
	}

	protected void handleSettingsChanged(Set<String> contexts) {
		fConfigurator.handleSettingsChanged(contexts, null);
	}
	

	protected OutlineT createOutlinePage() {
		return null;
	}
	
	protected void updateOutlinePageInput(OutlineT page) {
	}
	
	void handleOutlinePageClosed() {
		if (fOutlinePage != null) {
			fOutlinePage = null;
			resetHighlightRange();
		}
	}

	
	
	
	
	@Override
	public void dispose() {
		StatetCore.getSettingsChangeNotifier().removeChangeListener(this);
		super.dispose();
	}

}
