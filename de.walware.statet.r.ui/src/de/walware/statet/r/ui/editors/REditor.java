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

package de.walware.statet.r.ui.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.ext.ui.editors.EditorMessages;
import de.walware.statet.ext.ui.editors.IEditorAdapter;
import de.walware.statet.ext.ui.editors.StatextEditor1;
import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.core.RResourceUnit;
import de.walware.statet.r.internal.ui.RDoubleCommentAction;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.help.IRUIHelpContextIds;
import de.walware.statet.r.ui.RUIHelp;


public class REditor extends StatextEditor1<RProject> {


	private RSourceViewerConfigurator fRConfig;
	private RResourceUnit fRResourceUnit;
	private IContextProvider fHelpContextProvider;
	
	
	public REditor() {
		super();
	}
	
	@Override
	protected void initializeEditor() {
		configureStatetProjectNatureId(RProject.NATURE_ID);
		setDocumentProvider(RUIPlugin.getDefault().getRDocumentProvider());

		IPreferenceStore store = RUIPlugin.getDefault().getEditorPreferenceStore();
		fRConfig = new RSourceViewerConfigurator(RCore.getWorkbenchAccess(), store);
		fRConfig.setConfiguration(new RSourceViewerConfiguration(this, 
				fRConfig, store, StatetUIServices.getSharedColorManager()));
		initializeEditor(fRConfig); // super
		
		setHelpContextId(IRUIHelpContextIds.R_EDITOR);
		configureInsertMode(SMART_INSERT, true);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		fRConfig.setTarget(this, getSourceViewer());
		// Editor Help:
		fHelpContextProvider = RUIHelp.createEnrichedRHelpContextProvider(this, IRUIHelpContextIds.R_EDITOR);
		getSourceViewer().getTextWidget().addHelpListener(new HelpListener() {
			public void helpRequested(HelpEvent e) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp(fHelpContextProvider.getContext(null));
			}
		});
	}
	
	@Override
	public void dispose() {
		super.dispose();
		fRResourceUnit = null;
	}

	
	@Override
	protected void handlePreferenceStoreChanged(org.eclipse.jface.util.PropertyChangeEvent event) {
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
	
	void updateSettings(boolean indentChanged) {
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
	protected void setupConfiguration(RProject prevProject, RProject newProject, IEditorInput newInput) {
		fRResourceUnit = new RResourceUnit((IFile) newInput.getAdapter(IFile.class));
		fRConfig.setSource(fRResourceUnit);

		if (fRConfig.getPrefs().getPreferenceValue(REditorOptions.PREF_SMARTINSERT_ASDEFAULT)) {
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
	protected String[] collectContextMenuPreferencePages() {
		String[] ids = super.collectContextMenuPreferencePages();
		String[] more = new String[ids.length + 3];
		more[0] = "de.walware.statet.r.preferencePages.RSyntaxColoring"; //$NON-NLS-1$
		more[1] = "de.walware.statet.r.preferencePages.REditorTemplates"; //$NON-NLS-1$
		more[2] = "de.walware.statet.r.preferencePages.RCodeStyle"; //$NON-NLS-1$
		System.arraycopy(ids, 0, more, 3, ids.length);
		return more;
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

	public RResourceUnit getRResourceUnit() {
		return fRResourceUnit;
	}
	
	public IRCoreAccess getRCoreAccess() {
		return fRConfig;
	}
	
	@Override
	public Object getAdapter(Class adapter) {
		if (IContextProvider.class.equals(adapter)) {
			return fHelpContextProvider;
		}
		return super.getAdapter(adapter);
	}
	
}
