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

import java.util.List;

import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import de.walware.eclipsecommons.ltk.ISourceUnit;
import de.walware.eclipsecommons.ltk.ui.ElementInfoController;
import de.walware.eclipsecommons.preferences.PreferencesUtil;

import de.walware.statet.base.core.StatetCore;
import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.ext.ui.editors.EditorMessages;
import de.walware.statet.ext.ui.editors.IEditorAdapter;
import de.walware.statet.ext.ui.editors.IFoldingStructureProvider;
import de.walware.statet.ext.ui.editors.SourceViewerConfigurator;
import de.walware.statet.ext.ui.editors.StatextEditor1;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.core.rmodel.IRSourceUnit;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.RUIPreferenceInitializer;
import de.walware.statet.r.internal.ui.editors.DefaultRFoldingProvider;
import de.walware.statet.r.internal.ui.editors.RDoubleCommentAction;
import de.walware.statet.r.internal.ui.help.IRUIHelpContextIds;
import de.walware.statet.r.ui.RUIHelp;


public class REditor extends StatextEditor1<RProject> {
	
	
	protected RSourceViewerConfigurator fRConfig;
	private IRSourceUnit fRUnit;
	protected IContextProvider fHelpContextProvider;
	protected REditorOptions fOptions;
	
	private ElementInfoController fModelProvider;
	
	
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
		enableStructureSupport(fModelProvider);
		
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
	protected IFoldingStructureProvider createFoldingStructureProvider() {
		return new DefaultRFoldingProvider();
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
		
		if (sourceViewer instanceof ProjectionViewer) {
			final ProjectionViewer projViewer = (ProjectionViewer) sourceViewer;
			final boolean pref = PreferencesUtil.getInstancePrefs().getPreferenceValue(RUIPreferenceInitializer.PREF_FOLDING_ASDEFAULT_ENABLED);
			if (pref != projViewer.isProjectionMode()) {
				projViewer.doOperation(ProjectionViewer.TOGGLE);
			}
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
	
	public IRCoreAccess getRCoreAccess() {
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
	
	
//	/**
//	 * Creates the outline page used with this editor.
//	 *
//	 * @return the created R outline page
//	 */
//	@Override
//	protected StatextOutlinePage<REditor> createOutlinePage() {
//		final ROutlinePage page = new ROutlinePage(this);
////		fOutlineSelectionChangedListener.install(page);
//		return page;
//	}
//
//	@Override
//	protected void updateOutlinePageInput(final StatextOutlinePage page) {
//		((ROutlinePage) page).setInput(fRUnit);
//	}

}
