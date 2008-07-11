/*******************************************************************************
 * Copyright (c) 2005-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.templates.ITemplatesPage;

import de.walware.eclipsecommons.ltk.IModelManager;
import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.ISourceUnitModelInfo;
import de.walware.eclipsecommons.ltk.ast.AstSelection;
import de.walware.eclipsecommons.ltk.ast.IAstNode;
import de.walware.eclipsecommons.ltk.ui.ElementInfoController;
import de.walware.eclipsecommons.ltk.ui.ISelectionWithElementInfoListener;

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.base.ui.sourceeditors.EditorMessages;
import de.walware.statet.base.ui.sourceeditors.IEditorAdapter;
import de.walware.statet.base.ui.sourceeditors.IEditorInstallable;
import de.walware.statet.base.ui.sourceeditors.SourceViewerConfigurator;
import de.walware.statet.base.ui.sourceeditors.StatextEditor1;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.core.rmodel.IElementAccess;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.core.rsource.IRDocumentPartitions;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAst;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.editors.DefaultRFoldingProvider;
import de.walware.statet.r.internal.ui.editors.RDocumentProvider;
import de.walware.statet.r.internal.ui.editors.RDoubleCommentAction;
import de.walware.statet.r.internal.ui.editors.REditorTemplatesPage;
import de.walware.statet.r.internal.ui.help.IRUIHelpContextIds;
import de.walware.statet.r.ui.RUIHelp;


public class REditor extends StatextEditor1<RProject> {
	
	public static IRCoreAccess getRCoreAccess(final StatextEditor1 editor) {
		final IRCoreAccess adapter = (IRCoreAccess) editor.getAdapter(IRCoreAccess.class);
		return (adapter != null) ? adapter : RCore.getWorkbenchAccess();
	}
	
	
	private class MarkOccurrencesProvider implements IEditorInstallable, ISelectionWithElementInfoListener {
		
		// CHECK: Eclipse Bug #205585
		private static final String READ_ANNOTATION_KEY = "org.eclipse.jdt.ui.occurrences"; //$NON-NLS-1$
		private static final String WRITE_ANNOTATION_KEY = "org.eclipse.jdt.ui.occurrences.write"; //$NON-NLS-1$
		
		private class RunData {
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
		
		public void install(final IEditorAdapter editor) {
			fIsMarkEnabled = true;
			fModelPostSelection.addListener(this);
		}
		
		public void uninstall() {
			fIsMarkEnabled = false;
			fModelPostSelection.remove(this);
			removeAnnotations();
		}
		
		
		public void inputChanged() {
			fLastRun = null;
		}
		
		public void stateChanged(final StateData state) {
			final boolean ok = update((IRSourceUnit) state.getInputElement(), state.getAstSelection(), state.getLastSelection());
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
				final ISourceUnitModelInfo info = inputElement.getModelInfo("r", IModelManager.NONE, new NullProgressMonitor());
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
				if (node != null) {
					IElementAccess access = null;
					while (node != null && access == null) {
						final Object[] attachments = node.getAttachments();
						for (int i = 0; i < attachments.length; i++) {
							if (attachments[i] instanceof IElementAccess) {
								access = (IElementAccess) attachments[i];
								final Map<Annotation, Position> annotations = checkDefault(run, access);
								
								if (annotations != null) {
									updateAnnotations(run, annotations);
									return true;
								}
							}
						}
						node = node.getRParent();
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
		
		private Map<Annotation, Position> checkDefault(final RunData run, IElementAccess access) throws BadLocationException {
			while (access != null) {
				final RAstNode nameNode = access.getNameNode();
				if (nameNode == null) {
					return null;
				}
				run.range = new Point(nameNode.getOffset(), nameNode.getStopOffset());
				if (isValid(run)) {
					run.name = new String[] { access.getName() };
					final IElementAccess[] accessList = access.getAllInUnit();
					final Map<Annotation, Position> annotations = new LinkedHashMap<Annotation, Position>(accessList.length);
					for (int i = 0; i < accessList.length; i++) {
						final IElementAccess item = accessList[i];
						final String message = run.doc.get(item.getNode().getOffset(), item.getNode().getLength());
						annotations.put(
								new Annotation(item.isWriteAccess() ? WRITE_ANNOTATION_KEY : READ_ANNOTATION_KEY, false, message),
								RAst.getElementNamePosition(item.getNameNode()));
					}
					return annotations;
				}
				access = access.getSubElementAccess();
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
	private IRSourceUnit fRUnit;
	protected IContextProvider fHelpContextProvider;
	protected REditorOptions fOptions;
	
	protected ElementInfoController fModelProvider;
	
	
	public REditor() {
		super();
	}
	
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		
		setHelpContextId(IRUIHelpContextIds.R_EDITOR);
		setEditorContextMenuId("#REditorContext"); //$NON-NLS-1$
		configureInsertMode(SMART_INSERT, true);
	}
	
	@Override
	protected SourceViewerConfigurator createConfiguration() {
		fModelProvider = new ElementInfoController(RCore.getRModelManger(), StatetCore.EDITOR_CONTEXT);
		enableStructuralFeatures(fModelProvider,
				REditorOptions.PREF_FOLDING_ENABLED,
				REditorOptions.PREF_MARKOCCURRENCES_ENABLED);
		
		configureStatetProjectNatureId(RProject.NATURE_ID);
		setDocumentProvider(RUIPlugin.getDefault().getRDocumentProvider());
		
		final IRCoreAccess basicContext = RCore.getWorkbenchAccess();
		fOptions = RUIPlugin.getDefault().getREditorSettings(basicContext.getPrefs());
		
		final IPreferenceStore store = RUIPlugin.getDefault().getEditorPreferenceStore();
		fRConfig = new RSourceViewerConfigurator(basicContext, store);
		fRConfig.setConfiguration(new RSourceViewerConfiguration(this,
				fRConfig, store, StatetUIServices.getSharedColorManager()));
		return fRConfig;
	}
	
	@Override
	public void createPartControl(final Composite parent) {
		super.createPartControl(parent);
		fRConfig.setTarget(this);
		
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
	protected IEditorInstallable createCodeFoldingProvider() {
		return new DefaultRFoldingProvider();
	}
	
	@Override
	protected IEditorInstallable createMarkOccurrencesProvider() {
		return new MarkOccurrencesProvider();
	}
	
	@Override
	protected Point getRangeToHighlight(final AstSelection element) {
		final IAstNode covering = element.getCovering();
		if (covering instanceof RAstNode) {
			RAstNode node = (RAstNode) covering;
			while (node != null) {
				if (node.getNodeType() == NodeType.F_DEF) {
					return new Point(node.getOffset(), node.getLength());
				}
				node = node.getRParent();
			}
		}
		return null;
	}
	
	@Override
	public void dispose() {
		if (fModelProvider != null) {
			fModelProvider.dispose();
			fModelProvider = null;
		}
		super.dispose();
		fRUnit = null;
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
	
	void updateSettings(final boolean indentChanged) {
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
	protected void setupConfiguration(final RProject prevProject, final RProject newProject, final IEditorInput newInput) {
		fRUnit = ((RDocumentProvider) getDocumentProvider()).getWorkingCopy(newInput);
		fRConfig.setSource((fRUnit != null) ? (IRCoreAccess) fRUnit.getAdapter(IRCoreAccess.class) : null);
		fModelProvider.setInput(fRUnit);
	}
	
	@Override
	protected void setupConfiguration(final RProject prevProject, final RProject newProject, final IEditorInput newInput,
			final ISourceViewer sourceViewer) {
		super.setupConfiguration(prevProject, newProject, newInput, sourceViewer);
		if (fOptions.isSmartModeByDefaultEnabled()) {
			setInsertMode(SMART_INSERT);
		}
		else {
			setInsertMode(INSERT);
		}
	}
	
	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "de.walware.statet.r.contexts.REditorScope" }); //$NON-NLS-1$
	}
	
	@Override
	protected void collectContextMenuPreferencePages(final List<String> pageIds) {
		pageIds.add("de.walware.statet.r.preferencePages.REditorOptions"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.RSyntaxColoring"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.REditorTemplates"); //$NON-NLS-1$
		pageIds.add("de.walware.statet.r.preferencePages.RCodeStyle"); //$NON-NLS-1$
	}
	
	@Override
	protected void createActions() {
		super.createActions();
		
		Action action = new ContentAssistAction(
				EditorMessages.getCompatibilityBundle(), "ContentAssistProposal_", this); //$NON-NLS-1$
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action); //$NON-NLS-1$
		
		action = new InsertAssignmentAction((IEditorAdapter) getAdapter(IEditorAdapter.class));
		setAction(action.getId(), action);
		markAsContentDependentAction(action.getId(), true);
		
		action = new RDoubleCommentAction((IEditorAdapter) getAdapter(IEditorAdapter.class), getRCoreAccess());
		setAction(action.getId(), action);
		markAsContentDependentAction(action.getId(), true);
	}
	
	@Override
	protected IAction createCorrectIndentAction() {
		return new RCorrectIndentAction(this);
	}
	
	@Override
	protected void editorContextMenuAboutToShow(final IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		
		menu.remove(ITextEditorActionConstants.SHIFT_RIGHT);
		menu.remove(ITextEditorActionConstants.SHIFT_LEFT);
	}
	
	
	@Override
	public ISourceUnit getSourceUnit() {
		return fRUnit;
	}
	
	protected IRCoreAccess getRCoreAccess() {
		return fRConfig;
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
	protected ITemplatesPage createTemplatesPage() {
		return new REditorTemplatesPage(this, getSourceViewer());
	}
	
}
