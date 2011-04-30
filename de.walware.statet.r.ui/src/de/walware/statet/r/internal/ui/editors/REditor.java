/*******************************************************************************
 * Copyright (c) 2005-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.editors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.templates.ITemplatesPage;

import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.ast.AstSelection;
import de.walware.ecommons.ltk.ui.ElementInfoController;
import de.walware.ecommons.ltk.ui.ISelectionWithElementInfoListener;
import de.walware.ecommons.ltk.ui.LTKInputData;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorAddon;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorCommandIds;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditor1OutlinePage;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ltk.ui.sourceediting.actions.SpecificContentAssistHandler;
import de.walware.ecommons.text.ui.presentation.ITextPresentationConstants;
import de.walware.ecommons.ui.SharedUIResources;

import de.walware.statet.base.ui.IStatetUIMenuIds;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RElementAccess;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.ast.DocuComment;
import de.walware.statet.r.core.rsource.ast.DocuTag;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.help.IRUIHelpContextIds;
import de.walware.statet.r.launching.RCodeLaunching;
import de.walware.statet.r.ui.RUI;
import de.walware.statet.r.ui.RUIHelp;
import de.walware.statet.r.ui.editors.InsertAssignmentAction;
import de.walware.statet.r.ui.editors.RCorrectIndentAction;
import de.walware.statet.r.ui.editors.REditorOptions;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfiguration;
import de.walware.statet.r.ui.sourceediting.RSourceViewerConfigurator;


public class REditor extends SourceEditor1 {
	
	public static IRCoreAccess getRCoreAccess(final ISourceEditor editor) {
		final IRCoreAccess adapter = (IRCoreAccess) editor.getAdapter(IRCoreAccess.class);
		return (adapter != null) ? adapter : RCore.getWorkbenchAccess();
	}
	
	
	private class MarkOccurrencesProvider implements ISourceEditorAddon, ISelectionWithElementInfoListener {
		
		private final class RunData {
			
			final AbstractDocument doc;
			final long stamp;
			Point range;
			Annotation[] annotations;
			String[] name;
			
			public RunData(final AbstractDocument doc, final long stamp) {
				this.doc = doc;
				this.stamp = stamp;
			}
			
		}
		
		private boolean fIsMarkEnabled;
		private RunData fLastRun;
		
		public void install(final ISourceEditor editor) {
			fIsMarkEnabled = true;
			addPostSelectionWithElementInfoListener(this);
		}
		
		public void uninstall() {
			fIsMarkEnabled = false;
			removePostSelectionWithElementInfoListener(this);
			removeAnnotations();
		}
		
		
		public void inputChanged() {
			fLastRun = null;
		}
		
		public void stateChanged(final LTKInputData state) {
			final boolean ok = update((IRSourceUnit) state.getInputElement(), state.getAstSelection(), state.getSelection());
			if (!ok && state.isStillValid()) {
				removeAnnotations();
			}
		}
		
		/**
		 * Updates the occurrences annotations based on the current selection.
		 */
		protected boolean update(final IRSourceUnit inputElement, final AstSelection astSelection, final ISelection orgSelection) {
			if (!fIsMarkEnabled) {
				return false;
			}
			try {
				final IRModelInfo info = (IRModelInfo) inputElement.getModelInfo(RModel.TYPE_ID, IModelManager.NONE, new NullProgressMonitor());
				if (getSourceUnit() != inputElement || info == null || astSelection == null) {
					return false;
				}
				final RunData run = new RunData(inputElement.getDocument(null), info.getStamp());
				if (run.doc == null) {
					return false;
				}
				if (isValid(fLastRun)) {
					return true;
				}
				
				RAstNode node = (RAstNode) astSelection.getCovering();
				if (checkForAccess(run, node)) {
					return true;
				}
				
				if (orgSelection instanceof ITextSelection) {
					final ITextSelection textSelection = (ITextSelection) orgSelection;
					final int start = textSelection.getOffset();
					final int stop = start + textSelection.getLength();
					final List<RAstNode> comments = info.getAst().root.getComments();
					for (final RAstNode comment : comments) {
						if (comment.getStopOffset() < start) {
							continue;
						}
						if (comment.getOffset() > stop) {
							break;
						}
						if (comment.getNodeType() == NodeType.DOCU_AGGREGATION) {
							final DocuComment docuComment = (DocuComment) comment;
							final List<DocuTag> tags = docuComment.getTags();
							for (final DocuTag tag : tags) {
								if (tag.getStopOffset() < start) {
									continue;
								}
								if (tag.getOffset() > stop) {
									break;
								}
								final AstSelection selection = AstSelection.search(tag, start, stop, AstSelection.MODE_COVERING_SAME_LAST);
								node = (RAstNode) selection.getCovering();
								if (checkForAccess(run, node)) {
									return true;
								}
							}
						}
					}
				}
				
				return checkClear(run, orgSelection);
			}
			catch (final BadLocationException e) {
			}
			catch (final BadPartitioningException e) {
			}
			catch (final UnsupportedOperationException e) {
			}
			return false;
		}
		
		private boolean checkForAccess(final RunData run, RAstNode node) throws BadLocationException {
			if (node == null
					|| !(node.getNodeType() == NodeType.SYMBOL || node.getNodeType() == NodeType.STRING_CONST)) {
				return false;
			}
			do {
				final Object[] attachments = node.getAttachments();
				for (int i = 0; i < attachments.length; i++) {
					if (attachments[i] instanceof RElementAccess) {
						final RElementAccess access = (RElementAccess) attachments[i];
						final Map<Annotation, Position> annotations = checkDefault(run, access);
						
						if (annotations != null) {
							updateAnnotations(run, annotations);
							return true;
						}
					}
				}
				node = node.getRParent();
			} while (node != null);
			
			return false;
		}
		
		private Map<Annotation, Position> checkDefault(final RunData run, RElementAccess access) throws BadLocationException {
			while (access != null) {
				final RAstNode nameNode = access.getNameNode();
				if (nameNode == null) {
					return null;
				}
				run.range = new Point(nameNode.getOffset(), nameNode.getStopOffset());
				if (isValid(run)) {
					run.name = new String[] { access.getSegmentName() };
					final RElementAccess[] accessList = access.getAllInUnit();
					final Map<Annotation, Position> annotations = new LinkedHashMap<Annotation, Position>(accessList.length);
					for (int i = 0; i < accessList.length; i++) {
						final RElementAccess item = accessList[i];
						final String message = run.doc.get(item.getNode().getOffset(), item.getNode().getLength());
						annotations.put(
								new Annotation(item.isWriteAccess() ? 
										ITextPresentationConstants.ANNOTATIONS_WRITE_OCCURRENCES_TYPE:
										ITextPresentationConstants.ANNOTATIONS_COMMON_OCCURRENCES_TYPE,
										false, message),
								RAst.getElementNamePosition(item.getNameNode()));
					}
					return annotations;
				}
				access = access.getNextSegment();
			}
			return null;
		}
		
		private boolean isValid(final RunData run) {
			final Point currentSelection = fCurrentSelection;
			return (fIsMarkEnabled && run != null && currentSelection.x >= run.range.x
					&& currentSelection.x+currentSelection.y <= run.range.y
					&& run.doc.getModificationStamp() == run.stamp);
		}
		
		private boolean checkClear(final RunData run, final ISelection selection) throws BadLocationException, BadPartitioningException {
			if (fLastRun == null || fLastRun.stamp != run.stamp) {
				return false;
			}
			if (selection instanceof ITextSelection) {
				final ITextSelection textSelection = (ITextSelection) selection;
				final Point currentSelection = fCurrentSelection;
				final int offset = textSelection.getOffset();
				final int docLength = run.doc.getLength();
				final ITypedRegion partition = run.doc.getPartition(IRDocumentPartitions.R_PARTITIONING, offset, false);
				if (docLength > 0 &&
						(	(currentSelection.y > 0)
						||  (offset != currentSelection.x)
						||	(textSelection.getLength() == 0
							&& partition != null && partition.getType().equals(IRDocumentPartitions.R_DEFAULT)
							&& (offset <= 0 || !Character.isLetterOrDigit(run.doc.getChar(offset-1)) )
							&& (offset >= docLength || !Character.isLetter(run.doc.getChar(offset)) ) )
						)) {
					return true;
				}
			}
			return false;
		}
		
		private void updateAnnotations(final RunData run, final Map<Annotation, Position> annotations) throws BadLocationException {
			if (!isValid(run)) {
				return;
			}
			
			// Add occurrence annotations
			final IAnnotationModel annotationModel = getAnnotationModel();
//			create diff ?
//			if (fLastRun != null && Arrays.equals(run.name, fLastRun.name)) {
//			}
			final Annotation[] lastAnnotations = (fLastRun != null) ? fLastRun.annotations : null;
			synchronized (getLockObject(annotationModel)) {
				if (!isValid(run)) {
					return;
				}
				((IAnnotationModelExtension) annotationModel).replaceAnnotations(lastAnnotations, annotations);
				run.annotations = annotations.keySet().toArray(new Annotation[annotations.keySet().size()]);
				fLastRun = run;
			}
		}
		
		private void removeAnnotations() {
			final IAnnotationModel annotationModel = getAnnotationModel();
			synchronized (getLockObject(annotationModel)) {
				if (fLastRun == null) {
					return;
				}
				((IAnnotationModelExtension) annotationModel).replaceAnnotations(fLastRun.annotations, null);
				fLastRun = null;
			}
		}
		
		private IAnnotationModel getAnnotationModel() {
			final IDocumentProvider documentProvider = getDocumentProvider();
			if (documentProvider == null) {
				throw new UnsupportedOperationException();
			}
			final IAnnotationModel annotationModel = documentProvider.getAnnotationModel(getEditorInput());
			if (annotationModel == null || !(annotationModel instanceof IAnnotationModelExtension)) {
				throw new UnsupportedOperationException();
			}
			return annotationModel;
		}
		
	}
	
	
	protected RSourceViewerConfigurator fRConfig;
	protected IContextProvider fHelpContextProvider;
	protected REditorOptions fOptions;
	
	protected ElementInfoController fModelProvider;
	
	
	public REditor() {
	}
	
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		
		setHelpContextId(IRUIHelpContextIds.R_EDITOR);
		setEditorContextMenuId("#REditorContext"); //$NON-NLS-1$
	}
	
	
	@Override
	protected SourceEditorViewerConfigurator createConfiguration() {
		fModelProvider = new ElementInfoController(RCore.getRModelManager(), LTK.EDITOR_CONTEXT);
		enableStructuralFeatures(fModelProvider,
				REditorOptions.PREF_FOLDING_ENABLED,
				REditorOptions.PREF_MARKOCCURRENCES_ENABLED);
		
		setDocumentProvider(RUIPlugin.getDefault().getRDocumentProvider());
		
		final IRCoreAccess basicContext = RCore.getWorkbenchAccess();
		fOptions = RUIPlugin.getDefault().getREditorSettings(basicContext.getPrefs());
		
		fRConfig = new RSourceViewerConfigurator(basicContext);
		fRConfig.setConfiguration(new RSourceViewerConfiguration(this,
				fRConfig,
				RUIPlugin.getDefault().getEditorPreferenceStore(),
				SharedUIResources.getColors()));
		return fRConfig;
	}
	
	@Override
	protected SourceEditorViewerConfigurator createInfoConfigurator() {
		final RSourceViewerConfigurator config = new RSourceViewerConfigurator(getRCoreAccess());
		config.setConfiguration(new RSourceViewerConfiguration(
				config, 
				RUIPlugin.getDefault().getEditorPreferenceStore(),
				SharedUIResources.getColors()));
		return config;
	}
	
	@Override
	public void createPartControl(final Composite parent) {
		super.createPartControl(parent);
		
		// Editor Help:
		final SourceViewer viewer = (SourceViewer) getSourceViewer();
		fHelpContextProvider = RUIHelp.createEnrichedRHelpContextProvider(this, IRUIHelpContextIds.R_EDITOR);
		viewer.getTextWidget().addHelpListener(new HelpListener() {
			public void helpRequested(final HelpEvent e) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp(fHelpContextProvider.getContext(null));
			}
		});
	}
	
	@Override
	protected ISourceEditorAddon createCodeFoldingProvider() {
		return new DefaultRFoldingProvider();
	}
	
	@Override
	protected ISourceEditorAddon createMarkOccurrencesProvider() {
		return new MarkOccurrencesProvider();
	}
	
	
	public String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	@Override
	public IRSourceUnit getSourceUnit() {
		return (IRSourceUnit) super.getSourceUnit();
	}
	
	protected IRCoreAccess getRCoreAccess() {
		return fRConfig;
	}
	
	@Override
	protected void setupConfiguration(final IEditorInput newInput) {
		super.setupConfiguration(newInput);
		final IRSourceUnit su = getSourceUnit();
		fModelProvider.setInput(su);
		fRConfig.setSource((su != null) ? (IRCoreAccess) su.getRCoreAccess() : null);
	}
	
	@Override
	protected void doSetInput(final IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (fOptions.isSmartModeByDefaultEnabled()) {
			setInsertMode(SMART_INSERT);
		}
		else {
			setInsertMode(INSERT);
		}
	}
	
	
	@Override
	protected void handlePreferenceStoreChanged(final org.eclipse.jface.util.PropertyChangeEvent event) {
		if (AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH.equals(event.getProperty())
				|| AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS.equals(event.getProperty())) {
			return;
		}
		super.handlePreferenceStoreChanged(event);
	}
	
	
	@Override
	protected boolean isTabsToSpacesConversionEnabled() {
		return false;
	}
	
	public void updateSettings(final boolean indentChanged) {
		if (indentChanged) {
			updateIndentPrefixes();
			if (fRConfig.getRCodeStyle().getReplaceOtherTabsWithSpaces()) {
				installTabsToSpacesConverter();
			}
			else {
				uninstallTabsToSpacesConverter();
			}
		}
	}
	
	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] {
				"de.walware.statet.r.contexts.REditor", //$NON-NLS-1$
		});
	}
	
	@Override
	protected void collectContextMenuPreferencePages(final List<String> pageIds) {
		super.collectContextMenuPreferencePages(pageIds);
		pageIds.add("de.walware.statet.r.preferencePages.REditorOptions"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.RTextStylesPage"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.REditorTemplates"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.RCodeStyle"); //$NON-NLS-1$
	}
	
	@Override
	protected void createActions() {
		super.createActions();
		final IHandlerService handlerService = (IHandlerService) getServiceLocator().getService(IHandlerService.class);
		Action action;
		
		action = new InsertAssignmentAction(this);
		setAction(action.getId(), action);
		markAsStateDependentAction(action.getId(), true);
		
		action = new RDoubleCommentAction(this, getRCoreAccess());
		setAction(action.getId(), action);
		markAsStateDependentAction(action.getId(), true);
		
		final SpecificContentAssistHandler handler = new SpecificContentAssistHandler(this, RUIPlugin.getDefault().getREditorContentAssistRegistry());
		handlerService.activateHandler(ISourceEditorCommandIds.SPECIFIC_CONTENT_ASSIST_COMMAND_ID, handler);
	}
	
	@Override
	protected IAction createCorrectIndentAction() {
		return new RCorrectIndentAction(this);
	}
	
	@Override
	protected void editorContextMenuAboutToShow(final IMenuManager m) {
		super.editorContextMenuAboutToShow(m);
		
		m.insertBefore(SharedUIResources.ADDITIONS_MENU_ID, new Separator(IStatetUIMenuIds.GROUP_RUN_STAT_ID));
		final IContributionItem additions = m.find(SharedUIResources.ADDITIONS_MENU_ID);
		if (additions != null) {
			additions.setVisible(false);
		}
		
		m.remove(ITextEditorActionConstants.SHIFT_RIGHT);
		m.remove(ITextEditorActionConstants.SHIFT_LEFT);
		
		m.appendToGroup(IStatetUIMenuIds.GROUP_RUN_STAT_ID, new CommandContributionItem(new CommandContributionItemParameter(
				getSite(), null, RCodeLaunching.RUN_SELECTION_COMMAND_ID, CommandContributionItem.STYLE_PUSH)));
		m.appendToGroup(IStatetUIMenuIds.GROUP_RUN_STAT_ID, new CommandContributionItem(new CommandContributionItemParameter(
				getSite(), null, RCodeLaunching.RUN_SELECTION_PASTEOUTPUT_COMMAND_ID, CommandContributionItem.STYLE_PUSH)));
	}
	
	
	@Override
	public Object getAdapter(final Class required) {
		if (IContextProvider.class.equals(required)) {
			return fHelpContextProvider;
		}
		if (IRCoreAccess.class.equals(required)) {
			return getRCoreAccess();
		}
		return super.getAdapter(required);
	}
	
	@Override
	protected SourceEditor1OutlinePage createOutlinePage() {
		return new ROutlinePage(this);
	}
	
	@Override
	protected ITemplatesPage createTemplatesPage() {
		return new REditorTemplatesPage(this, getSourceViewer());
	}
	
	@Override
	public String[] getShowInTargetIds() {
		return new String[] { IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.ID_OUTLINE, RUI.R_HELP_VIEW_ID };
	}
	
}
