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

import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import de.walware.eclipsecommons.ui.preferences.ICombinedPreferenceStore;

import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.ext.ui.editors.EditorMessages;
import de.walware.statet.ext.ui.editors.StatextEditor1;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.internal.ui.help.IRUIHelpContextIds;
import de.walware.statet.r.ui.RUIHelp;
import de.walware.statet.r.ui.text.r.RBracketPairMatcher;


public class REditor extends StatextEditor1<RProject> {


	private IContextProvider fHelpContextProvider;
	
	
	public REditor() {
		super();
	}
	
	@Override
	protected void initializeEditor() {
		
		setDocumentProvider(RUIPlugin.getDefault().getRDocumentProvider());
		initStatext(new RBracketPairMatcher());
		// help init in #createActions() to avoid default trigger
		
		super.initializeEditor();
	}
	
	@Override
	protected RProject getProject(IEditorInput input) {
		
		return (RProject) getProject(input, RProject.NATURE_ID);
	}

	@Override
	protected void setupConfiguration(RProject project) {
		
		ICombinedPreferenceStore preferenceStore = RSourceViewerConfiguration.createCombinedPreferenceStore(
				RUIPlugin.getDefault().getPreferenceStore(), project);
		setPreferenceStore(preferenceStore);
		setSourceViewerConfiguration(new RSourceViewerConfiguration(this, 
				StatetUIServices.getSharedColorManager(), preferenceStore));
	}
	
	@Override
	protected void initializeKeyBindingScopes() {
		
		setKeyBindingScopes(new String[] { "de.walware.statet.r.ui.contexts.REditorScope" }); //$NON-NLS-1$
	}
	
	@Override
	protected String[] collectContextMenuPreferencePages() {
		
		String[] ids = super.collectContextMenuPreferencePages();
		String[] more = new String[ids.length + 3];
		more[0] = "de.walware.statet.r.ui.preferencePages.RSyntaxColoring"; //$NON-NLS-1$
		more[1] = "de.walware.statet.r.ui.preferencePages.REditorTemplates"; //$NON-NLS-1$
		more[2] = "de.walware.statet.r.ui.preferencePages.RCodeStyle"; //$NON-NLS-1$
		System.arraycopy(ids, 0, more, 3, ids.length);
		return more;
	}
	
	@Override
	protected void createActions() {

		super.createActions();
		
		// Editor Help
//		setHelpContextId(IRUIHelpContextIds.R_EDITOR);
		fHelpContextProvider = RUIHelp.createEnrichedRHelpContextProvider(this, IRUIHelpContextIds.R_EDITOR);
		getSourceViewer().getTextWidget().addHelpListener(new HelpListener() {
			public void helpRequested(HelpEvent e) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp(fHelpContextProvider.getContext(null));
			}
		});
		
		Action action = new ContentAssistAction(
				EditorMessages.getCompatibilityBundle(), "ContentAssistProposal_", this); //$NON-NLS-1$
        action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        setAction("ContentAssistProposal", action); //$NON-NLS-1$
   	}

	@Override
	public Object getAdapter(Class adapter) {
		
		if (IContextProvider.class.equals(adapter)) {
			return fHelpContextProvider;
		}
		return super.getAdapter(adapter);
	}
	
	@Override
	public void dispose() {
		
		super.dispose();
	}

}
